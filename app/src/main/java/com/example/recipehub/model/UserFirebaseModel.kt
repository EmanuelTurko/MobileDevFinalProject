package com.example.recipehub.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class UserFirebaseModel {

    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }

        database.firestoreSettings = setting
    }

    fun addUser(user: User, callback: (User) -> Unit, onError: (Exception) -> Unit) {
        val existingUser = auth.currentUser
        if (existingUser != null && existingUser.email == user.email) {
            Log.e("Register", "User already exists with email: ${user.email}")
            callback(user)
            return
        }

        val userRef = database.collection("users")
        userRef.whereEqualTo("username", user.username)
            .get()
            .addOnSuccessListener { document ->
                if (document.isEmpty) {
                    auth.createUserWithEmailAndPassword(user.email, user.password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                val json = hashMapOf(
                                    "id" to firebaseUser?.uid,
                                    "username" to user.username,
                                    "email" to user.email,
                                    "password" to user.password,
                                    "avatarUrl" to user.avatarUrl,
                                    "recipes" to user.recipes,
                                    "comments" to user.comments
                                )

                                firebaseUser?.uid?.let { userId ->
                                    database.collection("users").document(userId)
                                        .set(json)
                                        .addOnCompleteListener {
                                            val updatedUser = user.copy(id = userId)
                                            callback(updatedUser)
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e(
                                                "Register",
                                                "Error adding document: ${exception.message}"
                                            )
                                            onError(exception)
                                        }
                                }
                            } else {
                                task.exception?.let { exception ->
                                    if (exception is FirebaseAuthUserCollisionException) {
                                        Log.e(
                                            "Register",
                                            "FirebaseAuthUserCollisionException: ${exception.message}"
                                        )
                                        onError(exception)
                                    } else {
                                        Log.e("Register", "Unknown error: ${exception.message}")
                                        onError(exception)
                                    }
                                }
                            }
                        }
                } else {
                    onError(Exception("Username already exists"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting user by username: ${exception.message}")
                onError(exception)
            }
    }

    fun getUserById(id: String, callback: userCallback) {
        database.collection("users").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        callback(user)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting user by id: ${exception.message}")
            }
    }

    fun updateUser(user: User, callback: emptyCallback, onError: (Exception) -> Unit) {
        Log.d("ProfileFragment", "Updating user.................")
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError(Exception("User not authenticated"))
            return
        }
        val userRef = database.collection("users").document(userId)
        val updatedJson = hashMapOf(
            "username" to user.username,
            "email" to user.email,
            "password" to user.password,
            "avatarUrl" to user.avatarUrl,
            "recipes" to user.recipes,
            "comments" to user.comments
        )
        userRef.update(updatedJson as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("Firebase", "user successfully updated!")
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error updating user: ${exception.message}")
                onError(exception)
            }
    }

    fun getUserByUsername(username: String, callback: (User?) -> Unit, onError: (Exception) -> Unit) {
        database.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].toObject(User::class.java)
                    Log.d("Create", "User found: ${user?.id}")
                    callback(user)
                } else {
                    Log.e("Create", "No user found with username: $username")
                    onError(Exception("No user found with username: $username"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Create", "Error getting user by username: ${exception.message}")
                onError(exception)
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                when (exception) {
                    is FirebaseAuthInvalidUserException -> {
                        Log.e(
                            "Login",
                            "FirebaseAuthInvalidUserException: ${exception.message}"
                        )
                        onError(exception)
                    }

                    is FirebaseAuthInvalidCredentialsException -> {
                        Log.e(
                            "Login",
                            "FirebaseAuthInvalidCredentialsException: ${exception.message}"
                        )
                        onError(exception)
                    }

                    else -> {
                        Log.e("Login", "Unknown error: ${exception.message}")
                        onError(exception)
                    }
                }
            }
    }
}
