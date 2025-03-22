package com.example.recipehub.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class FirebaseModel {

    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }

        database.firestoreSettings = setting
    }

    fun addUser(user: User, callback: EmptyCallback, onError: (String)->Unit) {
        val userRef = database.collection("users")
        userRef.whereEqualTo("username", user.username)
            .get()
            .addOnSuccessListener { document ->
                if (document.isEmpty) {
                    auth.createUserWithEmailAndPassword(user.email, user.password)
                        .addOnCompleteListener { registration ->
                            if (registration.isSuccessful) {
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
                                            callback()
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e(
                                                "Firebase",
                                                "Error adding document: ${exception.message}"
                                            )
                                        }
                                }
                            } else {
                                if (registration.exception is FirebaseAuthUserCollisionException) {
                                    Log.e("Firebase", "Email already exists")
                                    onError("Email already exists")
                                } else {
                                    Log.e(
                                        "Firebase",
                                        "Error registering user: ${registration.exception?.message}"
                                    )
                                }
                            }
                        }

                } else {
                    Log.e("Firebase", "Username already exists")
                    onError("Username already exists")
                }
            }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error getting user by username: ${exception.message}")
                        onError("Error checking username availability")
                    }
    }
    fun getUserByUsername(username: String, callback: UserCallback) {
        database.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].toObject(User::class.java)
                    if (user != null) {
                        callback(user)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting user by username: ${exception.message}")
            }
    }
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { login ->
                if (login.isSuccessful) {
                    onSuccess()
                } else {
                    try {
                        throw login.exception ?: Exception("Unknown error")
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Log.e("Firebase", "FirebaseAuthInvalidUserException: ${e.message}")
                        onError("No account found with this email")
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Log.e("Firebase", "FirebaseAuthInvalidCredentialsException: ${e.message}")
                        onError("Invalid credentials")
                    } catch (e: Exception) {
                        Log.e("Firebase", "Unknown error: ${e.message}")
                        onError("Unknown error")
                    }
                }
            }
    }
}
