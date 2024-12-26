package com.noisevisionsoftware.fitapplication

import android.app.Application
import com.google.firebase.FirebaseApp

class FitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}