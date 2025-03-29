package com.example.recipehub.model

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class RatingModel {
    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val shared = RatingModel()
    }
}