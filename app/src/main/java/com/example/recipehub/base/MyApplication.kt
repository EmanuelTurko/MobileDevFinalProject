package com.example.recipehub.base

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class MyApplication: Application() {

    companion object{
            lateinit var context: Context
        private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        FirebaseApp.initializeApp(context)
    }
}