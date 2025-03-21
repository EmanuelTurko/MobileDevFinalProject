package com.example.recipehub.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class FirebaseModel {

    private val database = FirebaseFirestore.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }

        database.firestoreSettings = setting
    }

    fun addUser(user: AppUser, callback: EmptyCallback) {
        val json = hashMapOf(
            "id" to user.id,
            "username" to user.username,
            "email" to user.email,
            "password" to user.password,
            "avatarUrl" to user.avatarUrl,
            "recipes" to user.recipes,
            "comments" to user.comments
        )

        // Correct document reference with an even number of segments: users/{userId}
        database.collection("users").document(user.id)
            .set(json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error adding document: ${exception.message}")
            }
    }
}
