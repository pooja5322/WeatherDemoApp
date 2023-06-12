package com.example.demoapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.demoapp.API.UserAPI
import com.example.demoapp.Model.WeatherForecastResponse
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(private val userApi: UserAPI) {

    private val _userResponseLiveData = MutableLiveData<NetworkResult<WeatherForecastResponse>>()
    val userResponseLiveData: LiveData<NetworkResult<WeatherForecastResponse>>
        get() = _userResponseLiveData

    suspend fun fetchData(latitude: Double, longitude: Double, API_KEY: String) {
        Log.d("Place response latlong", latitude.toString()+" "+longitude.toString() )
        _userResponseLiveData.postValue(NetworkResult.Loading())
        val response = userApi.getWeather(latitude,longitude,API_KEY)
        handleResponse(response)

    }

    private fun handleResponse(response: Response<WeatherForecastResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _userResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _userResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }

}
