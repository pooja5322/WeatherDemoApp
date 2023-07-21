package com.example.demoapp

import android.app.Application
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //TODO here we can set the common code like show progress bar/hide progress bar/check internet connection, check GPS enabled or not
        // Due to short time of period i didn't write here but usally i write such kind of code in Application class
    }
}