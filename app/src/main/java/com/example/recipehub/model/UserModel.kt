package com.example.recipehub.model

import android.content.Context.MODE_PRIVATE
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.recipehub.base.MyApplication.Companion.context
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
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

    fun addUser(user: User, callback: emptyCallback, onError: onError) {

        userFirebaseModel.addUser(user, { updatedUser ->
            executor.execute {
                try {
                    database.userDao().insertUser(updatedUser)
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
        }, onError)
    }
    fun updateUser(user:User ,callback: emptyCallback, onError: onError){
        userFirebaseModel.updateUser(user,callback, onError)
            executor.execute {
                try {
                    database.userDao().updateUser(user)
                    userSharedPref(user)
                    mainHandler.post {
                        Log.d("Profile", "user updated in local database")
                        printSharedPref()
                        callback()
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        Log.e("Profile", "Failed to update user in local database")
                        onError(exception)
                    }
                }
            }
    }

    fun getUserById(callback: userCallback, id: String) {
        executor.execute{
            val userId = database.userDao().getUserById(id)
            mainHandler.post {
                callback(userId)
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

    fun getUserByEmail(callback: userCallback, email: String) {
        executor.execute{
            val userEmail = database.userDao().getUserByEmail(email)
            mainHandler.post {
                callback(userEmail)
            }
        }
    }

    fun login(email: String, password: String, callback: emptyCallback, onError: onError) {
        userFirebaseModel.login(email, password, {
            executor.execute {
                try {
                    val userEmail = database.userDao().getUserByEmail(email)
                    if (userEmail.password == password) {
                        mainHandler.post {
                            userSharedPref(userEmail)
                            callback()
                        }
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        onError(exception)
                    }
                }
            }
        }, onError)
    }
}