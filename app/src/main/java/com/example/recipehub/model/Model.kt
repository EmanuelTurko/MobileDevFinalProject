package com.example.recipehub.model

import android.os.Looper
import android.util.Log
import android.util.Patterns
import androidx.core.os.HandlerCompat
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors


typealias UserCallback = (User) -> Unit
typealias EmptyCallback = () -> Unit
typealias OnError = (String) -> Unit
class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val firebaseModel = FirebaseModel()

    companion object{
        val shared = Model()
    }

    fun addUser(user: User, callback: EmptyCallback, onError: OnError) {
        firebaseModel.addUser(user, callback, onError)
        executor.execute{
            database.userDao().insertUser(user)
            mainHandler.post {
                Log.e("Firebase", "User added to local database")
                callback()
            }
        }
    }
    fun getUserById(callback: UserCallback, id: String) {
        executor.execute{
            val userId = database.userDao().getUserById(id)
            mainHandler.post {
                callback(userId)
            }
        }
    }
    fun getUserByUsername(username: String, callback: UserCallback) {
        firebaseModel.getUserByUsername(username) { user:User ->
            callback(user)
        }
        executor.execute{
            val userUsername = database.userDao().getUserByUsername(username)
            mainHandler.post {
                callback(userUsername)
            }
        }
    }
    fun getUserByEmail(callback: UserCallback, email: String) {
        executor.execute{
            val userEmail = database.userDao().getUserByEmail(email)
            mainHandler.post {
                callback(userEmail)
            }
        }
    }
    fun login(email: String, password: String, callback: EmptyCallback, onError: OnError) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onError("Invalid email format")
            return
        }
        firebaseModel.login(email, password, {
            executor.execute {
                try {
                    val userEmail = database.userDao().getUserByEmail(email)
                    if (userEmail.password == password) {
                        mainHandler.post {
                            callback()
                        }
                    } else {
                        mainHandler.post {
                            onError("Password is incorrect")
                        }
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        onError("User not found in local database")
                    }
                }
            }
        }, { error ->
            when (error) {
                "ERROR_USER_NOT_FOUND" -> onError("No account found with this email")
                "ERROR_USER_DISABLED" -> onError("Account is disabled")
                else -> onError("Login failed: ${error}")
            }
        })
    }

}