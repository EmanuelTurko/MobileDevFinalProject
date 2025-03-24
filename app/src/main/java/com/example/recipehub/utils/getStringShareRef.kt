package com.example.recipehub.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

fun Context.getStringShareRef(resource: String, library:String): String {
    val sharedPref = this.getSharedPreferences(library, MODE_PRIVATE)
    return sharedPref?.getString(resource, "") ?: ""
}