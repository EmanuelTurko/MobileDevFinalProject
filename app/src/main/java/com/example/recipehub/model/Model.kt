package com.example.recipehub.model

import android.os.Looper
import androidx.core.os.HandlerCompat
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors


typealias UserCallback = (AppUser) -> Unit
typealias EmptyCallback = () -> Unit
class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val firebaseModel = FirebaseModel()

    companion object{
        val shared = Model()
    }

    fun addUser(user: AppUser, callback: EmptyCallback) {
        firebaseModel.addUser(user, callback)
        executor.execute{
            database.userDao().insertUser(user)
            mainHandler.post {
                callback()
            }
        }
    }
    fun getUserById(callback: UserCallback, id: String) {
        executor.execute{
            val user = database.userDao().getUserById(id)
            mainHandler.post {
                callback(user)
            }
        }
    }
    fun getUserByUsername(callback: UserCallback, username: String) {
        executor.execute{
            val user = database.userDao().getUserByUsername(username)
            mainHandler.post {
                callback(user)
            }
        }
    }
    fun getUserByEmail(callback: UserCallback, email: String) {
        executor.execute{
            val user = database.userDao().getUserByEmail(email)
            mainHandler.post {
                callback(user)
            }
        }
    }
}