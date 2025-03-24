package com.example.recipehub.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun saveBitmapToFile(bitmap: Bitmap, context: Context): Uri? {
    val randomFileName = "profile_avatar_${System.currentTimeMillis()}.png"
    val file = File(context.cacheDir, randomFileName)

    try {
        FileOutputStream(file).use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        }
        return Uri.fromFile(file)
    } catch (e: IOException) {
        Log.e("Profile", "Error saving Bitmap to file", e)
        return null
    }
}