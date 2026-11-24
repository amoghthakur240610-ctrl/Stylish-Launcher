package com.stylishlauncher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StylishLauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global singletons here if needed
    }
}