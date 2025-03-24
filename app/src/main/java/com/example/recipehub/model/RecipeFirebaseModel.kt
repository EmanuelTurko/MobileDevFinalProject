package com.example.recipehub.model

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import java.sql.Time
import java.util.Date
import android.util.Log

class RecipeFirebaseModel {

    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }

        database.firestoreSettings = setting
    }

        fun create(recipe: Recipe, onSuccess: (Recipe) -> Unit, onError: (Exception) -> Unit) {
            Log.d("Create", "Creating recipe $recipe")
            val recipeRef = database.collection("recipes")
            val documentId = recipeRef.document().id
            Log.d("Create", "Creating recipe 1 $documentId")
            val recipeData = recipe.copy(
                id = documentId,
                comments = "",
                date = Date()
            )
            Log.d("Create", "Creating recipe 2 $recipeData")

            recipeRef.add(recipeData)
                .addOnSuccessListener { document ->
                    recipeRef.document(document.id).get()
                        .addOnSuccessListener { documentSnapshot ->
                            Log.d("Create", "Creating recipe 3 $documentSnapshot")
                            val recipeFromDb = documentSnapshot.toObject(Recipe::class.java)
                            Log.d("Create", "Creating recipe 4 $recipeFromDb")
                            val timestamp = documentSnapshot.get("date") as? Timestamp
                            Log.d("Create", "Creating recipe 5 $timestamp")
                            val date = timestamp?.toDate()
                            Log.d("Create", "Creating recipe 6 $date")
                            val updatedRecipe = recipeFromDb?.copy(date = date)
                            Log.d("Create", "Creating recipe 6 $updatedRecipe")
                            updatedRecipe?.let {
                                onSuccess(updatedRecipe)
                            } ?: onError(Exception("Failed to create recipe"))
                        }
                        .addOnFailureListener {
                            Log.e("Create", "Failed to get recipe from database")
                            onError(it) }
                }
                .addOnFailureListener { onError(it) }

        }
}
