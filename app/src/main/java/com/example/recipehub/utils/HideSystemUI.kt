package com.example.recipehub.utils

import android.view.View
import android.view.WindowInsets
import androidx.activity.ComponentActivity

fun ComponentActivity.hideSystemUI() {
        val windowInsetsController = window.insetsController
        windowInsetsController?.hide(WindowInsets.Type.navigationBars())
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
}