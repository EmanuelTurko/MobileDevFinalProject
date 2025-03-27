package com.example.recipehub.model

import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import com.squareup.picasso.Callback
import java.util.concurrent.Executors

class CommentModel {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val shared = CommentModel()
    }

    fun insertComment(comment:Comment, callback: emptyCallback, onError: onError) {
        executor.execute {
            try {
                database.commentDao().insertComment(comment)
                mainHandler.post {
                    Log.d("Register", "user added to local database")
                    callback()
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    Log.e("Register", "Failed to add user to local database")
                    onError(exception)
                }
            }
        }
    }
}