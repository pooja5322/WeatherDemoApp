package com.example.demoapp.API

import com.example.demoapp.Constants.API_KEY
import com.example.demoapp.Model.WeatherForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserAPI {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): Response<WeatherForecastResponse>
}