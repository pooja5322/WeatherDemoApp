package com.example.demoapp.Activities

import android.Manifest
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.demoapp.Constants.API_KEY
import com.example.demoapp.Constants.PLACES_API_KEY
import com.example.demoapp.Model.WeatherForecastResponse
import com.example.demoapp.R
import com.example.demoapp.databinding.ActivityHomeScreenBinding
import com.example.demoapp.repository.NetworkResult
import com.example.demoapp.viewmodel.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeScreen : AppCompatActivity() {

    private lateinit var _binding: ActivityHomeScreenBinding
    private val LOCATION_PERMISSION_REQ_CODE = 1000;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocodingManager: GeocodingManager
    private val authviewModel by viewModels<AuthViewModel>()
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_home_screen)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val formatter = SimpleDateFormat("EEEE")
        val formatter1 = SimpleDateFormat("MMM-dd")

        val date = Date()
        val current = formatter.format(date)
        val current1 = formatter1.format(date)
        _binding.txtCurrentday.text = "${current} ${" | "}${current1}"

        getCurrentLocation()

        geocodingManager = GeocodingManager(PLACES_API_KEY)

    }

    private fun bindObserver() {
        showProgressDialog()
        authviewModel.data.observe(this, androidx.lifecycle.Observer {
            when (it) {
                is NetworkResult.Success -> {
                    //TODO set data to UI
                    setDataToUI(it)
                    dismissProgressDialog()
                }
                is NetworkResult.Error -> {
                    showAlertDialog()
                }
                is NetworkResult.Loading -> {
                   // showProgressDialog()
                }
            }
        })
    }

    private fun setDataToUI(it: NetworkResult<WeatherForecastResponse>) {
        _binding.txtCurrenttemp.text = "${it.data?.main?.temp} ${"temp"}"
        _binding.txtCurrentcity.text = it.data?.name
        _binding.txtWind.text = it.data?.wind?.speed.toString()
        _binding.txtPressure.text = it.data?.main?.pressure.toString()
        _binding.txtTemp.text = it.data?.main?.temp.toString()
        _binding.txtHumidity.text = it.data?.main?.humidity.toString()
        val sunriseTime = convertUnixTimestampToSunrise(it.data?.sys?.sunrise!!)
        _binding.txtSunrise.text = sunriseTime.toString()
        val sunsetTime = convertUnixTimestampToSunrise(it.data?.sys?.sunset!!)
        _binding.txtSunset.text = sunsetTime.toString()
        _binding.txtWeather.text = it.data?.weather?.get(0)?.main
        _binding.txtLatlong.text = "${it.data?.coord?.lat} ${it.data?.coord?.lon}"
        if (it.data?.weather?.get(0)?.main.equals("Clouds")) {
            //TODO cache image using glide
            Glide.with(this).load(R.drawable.rainy).into(_binding.weatherimg);
        } else {
            //TODO cache image using glide
            Glide.with(this).load(R.drawable.splashlogo).into(_binding.weatherimg);
        }
    }


    private fun showProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }


    private fun showAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Alert !")
        alertDialogBuilder.setMessage("Please enter correct city name.")
        alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun dismissProgressDialog() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun searchForPlaces(query: String) {
        geocodingManager.searchForPlaces(query, object : GeocodingCallback {
            override fun onPlacesReceived(places: List<Place>) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) {
                            authviewModel.fetchData(
                                places[0].latitude,
                                places[0].longitude,
                                API_KEY
                            )
                            bindObserver()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                        }
                    }
                }
            }

            override fun onError(errorMessage: String) {
                TODO("Not yet implemented")
            }
        });


    }


    private fun handleIntent(intent: Intent) {
        val suggestionProvider = SearchRecentSuggestions(
            this,
            MySuggestionProvider.AUTHORITY,
            MySuggestionProvider.MODE
        )
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                // doMySearch(query)
                suggestionProvider.saveRecentQuery(query, null)
                searchForPlaces(query)
            }
        }

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val search = menu.findItem(R.id.action_search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchForPlaces(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
/*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.apply {
            //current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(false)
        }
        return true
    }
*/

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val list: List<Address> =
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) as List<Address>

                authviewModel.fetchData(list[0].latitude, list[0].latitude, API_KEY)
                bindObserver()

            }
        }
    }


    fun convertUnixTimestampToSunrise(unixTimestamp: Long): String {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sunriseDate = Date(unixTimestamp * 1000)
        return dateFormat.format(sunriseDate)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    getCurrentLocation()
                } else {
                    // permission denied
                    Toast.makeText(
                        this, "You need to grant permission to access location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}