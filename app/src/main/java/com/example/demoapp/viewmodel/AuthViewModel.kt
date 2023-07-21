package com.example.demoapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.Model.WeatherForecastResponse
import com.example.demoapp.repository.NetworkResult
import com.example.demoapp.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val data: LiveData<NetworkResult<WeatherForecastResponse>>
    get() = userRepository.userResponseLiveData

    fun fetchData(latitude: Double, longitude: Double, API_KEY: String) {
        viewModelScope.launch {
         userRepository.fetchData(latitude,longitude,API_KEY)
        }
    }
}