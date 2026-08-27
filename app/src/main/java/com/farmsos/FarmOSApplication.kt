package com.farmsos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FarmOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
