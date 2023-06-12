package com.example.demoapp.Activities

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GeocodingManager(private val apiKey: String) {

    private val client = OkHttpClient()

    fun searchForPlaces(query: String, callback: GeocodingCallback) {
        val url = "https://api.opencagedata.com/geocode/v1/json?q=$query&key=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()
                try {
                    val jsonObject = JSONObject(jsonData)
                    val resultsArray = jsonObject.getJSONArray("results")
                    val places = mutableListOf<Place>()

                    for (i in 0 until resultsArray.length()) {
                        val result = resultsArray.getJSONObject(i)
                        val place = Place(
                            result.getString("formatted"),
                            result.getJSONObject("geometry").getDouble("lat"),
                            result.getJSONObject("geometry").getDouble("lng")
                        )
                        places.add(place)
                    }

                    callback.onPlacesReceived(places)

                } catch (e: Exception) {
                    callback.onError(e.localizedMessage)
                }
            }
        })
    }
}

data class Place(val formattedAddress: String, val latitude: Double, val longitude: Double)

interface GeocodingCallback {
    fun onPlacesReceived(places: List<Place>)
    fun onError(errorMessage: String)
}