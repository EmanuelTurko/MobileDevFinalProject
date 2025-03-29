package com.example.recipehub.model

import android.content.Context.MODE_PRIVATE
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.recipehub.base.MyApplication.Companion.context
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import com.example.recipehub.utils.getBooleanShareRef
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.setBooleanShareRef
import com.example.recipehub.utils.setStringListShareRef
import com.example.recipehub.utils.setStringShareRef
import java.util.concurrent.Executors


typealias userCallback = (User) -> Unit
typealias userIdCallback = (String) -> Unit
typealias emptyCallback = () -> Unit
typealias onError = (Exception) -> Unit

class UserModel private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val userFirebaseModel = UserFirebaseModel()

    companion object{
        val shared = UserModel()
    }

    private fun userSharedPref(user: User){
        context.setStringShareRef("id",user.id, "userInfo")
        context.setStringShareRef("username",user.username, "userInfo")
        context.setStringShareRef("email",user.email, "userInfo")
        context.setStringShareRef("password",user.password, "userInfo")
        context.setStringShareRef("avatarUrl", user.avatarUrl, "userInfo")
        context.setStringListShareRef("recipes", user.recipes, "userInfo")
        context.setStringListShareRef("comments", user.comments, "userInfo")
        context.setBooleanShareRef("isLoggedIn", true, "userInfo")
    }
    fun printSharedPref(){
        val sharedPref = context.getSharedPreferences("userInfo", MODE_PRIVATE)
        Log.d("Profile","User Shared Pref: ${sharedPref.all}")
    }
    fun setLoginState(isLoggedIn: Boolean){
        val sharedPref = context.getSharedPreferences("userInfo", MODE_PRIVATE)
        sharedPref.edit().apply {
            putBoolean("isLoggedIn", isLoggedIn)
            commit()
        }
    }
    fun currentUser(field:String):String?{
        return context.getStringShareRef(field,"userInfo")
    }
    fun isLoggedIn(): Boolean {
        return context.getBooleanShareRef("isLoggedIn", "userInfo")
    }

    fun addUser(user: User, callback: emptyCallback, onError: onError) {

        userFirebaseModel.addUser(user, { updatedUser ->
            executor.execute {
                try {
                    database.userDao().insertUser(updatedUser)
                    mainHandler.post {
                        callback()
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        Log.e("Register", "Failed to add user to local database")
                        onError(exception)
                    }
                }
            }
        }, onError)
    }
    fun updateUser(user:User ,callback: emptyCallback, onError: onError){
        userFirebaseModel.updateUser(user,callback, onError)
            executor.execute {
                try {

                    database.userDao().updateUser(user)
                    Log.d("Update", "updated user with ${user.avatarUrl}")
                    userSharedPref(user)
                    mainHandler.post {
                        printSharedPref()
                        callback()
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        Log.e("Update", "Failed to update user in local database")
                        onError(exception)
                    }
                }
            }
    }

    fun getUserByUsername(username: String, callback: (User?) -> Unit, onError: (Exception) -> Unit) {
        executor.execute {
            val localUser = database.userDao().getUserByUsername(username)
            if (true) {
                mainHandler.post { callback(localUser) }
                return@execute
            }
            userFirebaseModel.getUserByUsername(username, { firebaseUser ->
                mainHandler.post { callback(firebaseUser) }
                if (firebaseUser != null) {
                    executor.execute {
                        database.userDao().insertUser(firebaseUser)
                    }
                }
            }, onError)
        }
    }

    fun login(email: String, password: String, callback: emptyCallback, onError: onError) {
        executor.execute {
            try {
                val user = database.userDao().getUserByEmail(email)
                if(true){
                    if (user.password == password) {
                        userSharedPref(user)
                        mainHandler.post {
                            callback()
                        }
                    }
                    else{
                        mainHandler.post {
                            onError(Exception("Invalid credentials"))
                        }
                    }
                } else {
                    userFirebaseModel.login(email, password, { user ->
                        executor.execute {
                           if(user != null){
                                database.userDao().insertUser(user)
                                userSharedPref(user)
                                mainHandler.post {
                                    Log.d("Login", "User logged in (Firebase)")
                                    callback()
                                }
                            }
                        } }, { exception ->
                        mainHandler.post {
                            Log.e("Login", "Firebase Login Error: ${exception.message}")
                            onError(exception)
                        }
                    })
                }
            } catch (e: Exception) {
                mainHandler.post {
                    Log.e("Login", "Local DB Error: ${e.message}")
                    onError(e)
                }
            }
        }
    }
}