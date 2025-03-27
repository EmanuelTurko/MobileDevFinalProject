package com.example.recipehub.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import java.util.Date
import android.util.Log

class RecipeFirebaseModel {

    private val database = FirebaseFirestore.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }

        database.firestoreSettings = setting
    }

    fun create(recipe: Recipe, onSuccess: recipeCallback, onError:onError) {
        Log.d("Create", "Creating recipe $recipe")
        val recipeRef = database.collection("recipes")
        val documentId = recipeRef.document().id
        Log.d("Create", "Creating recipe 1 $documentId")
        val recipeData = recipe.copy(
            id = documentId,
            date = Date()
        )
        UserFirebaseModel().getUserByUsername(recipeData.author,{ user ->
            user?.let {
                val updatedRecipes = it.recipes.toMutableList()
                updatedRecipes.add(documentId)
                val userRef = database.collection("users").document(it.id)
                userRef.update("recipes", updatedRecipes)
                    .addOnSuccessListener { Log.d("Create", "added recipe to user") }
                    .addOnFailureListener { exception ->
                        Log.e("Create", "Failed to add recipe to user", exception)
                        onError(exception)
                    }
            }
        }, onError = { exception ->
            Log.e("Create", "Failed to retrieve user", exception)
            onError(exception)
        })
        recipeRef.document(documentId).set(recipeData)
            .addOnSuccessListener {
                recipeRef.document(documentId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        Log.d("Create", "Creating recipe 3 $documentSnapshot")
                        val recipeFromDb = documentSnapshot.toObject(Recipe::class.java)
                        Log.d("Create", "Creating recipe 4 $recipeFromDb")
                        val timestamp = documentSnapshot.get("date") as? Timestamp
                        Log.d("Create", "Creating recipe 5 $timestamp")
                        val date = timestamp?.toDate()
                        Log.d("Create", "Creating recipe 6 $date")
                        val updatedRecipe = recipeFromDb?.copy(date = date)
                        Log.d("Create", "Creating recipe 7 $updatedRecipe")

                        updatedRecipe?.let {
                            onSuccess(updatedRecipe)
                        } ?: onError(Exception("Failed to create recipe"))
                    }
                    .addOnFailureListener {
                        Log.e("Create", "Failed to get recipe from database")
                        onError(it)
                    }
            }
            .addOnFailureListener { onError(it) }
    }


    fun getAllRecipes(onSuccess: recipeListCallback, onError:onError) {
        database.collection("recipes")
            .get()
            .addOnSuccessListener { result ->
                val recipes = result.documents.mapNotNull { it.toObject(Recipe::class.java) }
                onSuccess(recipes)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun getRecipeById(recipeId:String, onSuccess: recipeCallback, onError:onError){
        val recipeRef = database.collection("recipes").document(recipeId)
        recipeRef.get()
            .addOnSuccessListener{ recipe ->
                val recipeData = recipe.toObject(Recipe::class.java)
                recipeData?.let {
                    onSuccess(it)
                } ?: onError(Exception("Failed to get recipe"))
            }
            .addOnFailureListener { exception->
                onError(exception)
            }

    }

    fun updateRecipe(recipeId:String, recipe:Recipe, onSuccess: recipeCallback, onError:onError){
        val recipeRef = database.collection("recipes").document(recipeId)
        val updatedJson = hashMapOf(
            "title" to recipe.title,
            "author" to recipe.author,
            "rating" to recipe.rating,
            "date" to recipe.date,
            "imageUrl" to recipe.imageUrl,
            "comments" to recipe.comments,
        )
        recipeRef.update(updatedJson as Map<String,Any?>)
            .addOnSuccessListener {
                Log.d("Create", "Recipe updated successfully")
                onSuccess(recipe)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun addComment(recipeId: String, poster: String, comment: String, onSuccess: (Comment) -> Unit, onError: (Exception) -> Unit) {
        val commentRef = database.collection("recipes").document(recipeId).collection("comments")

        getRecipeById(
            recipeId,
            onSuccess = { recipe: Recipe ->
                val newComment = Comment(
                    poster = poster,
                    content = comment,
                    date = Date()
                )
                commentRef.add(newComment)
                    .addOnSuccessListener { document ->
                        Log.e("Create", "Comment added with id: ${document.id}")
                        val updatedComment = newComment.copy(id = document.id)
                        commentRef.document(document.id).set(updatedComment)
                            .addOnSuccessListener {
                                commentRef.document(document.id).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val commentFromDb =
                                            documentSnapshot.toObject(Comment::class.java)
                                        val timestamp = documentSnapshot.get("date") as? Timestamp
                                        val date = timestamp?.toDate()
                                        val finalUpdatedComment = commentFromDb?.copy(
                                            date = date
                                        )
                                        finalUpdatedComment?.let {
                                            Log.d("Create", "Comment updated with ID and date")
                                            onSuccess(it)
                                        } ?: onError(Exception("Failed to add comment"))
                                    }
                            }
                            .addOnFailureListener { exception ->
                                onError(exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            },
            onError = { exception ->
                Log.e("Create", "Failed to retrieve recipe", exception)
                onError(exception)
            }
        )
    }


    fun getAllComments(recipeId:String, onSuccess: commentListCallback,onError:onError){
        val commentRef = database.collection("recipes").document(recipeId).collection("comments")
        commentRef.get()
            .addOnSuccessListener { result ->
                val comments = result.documents.mapNotNull { it.toObject(Comment::class.java) }
                onSuccess(comments)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }

    }
    fun deleteRecipe(recipeId:String, onSuccess: emptyCallback, onError:onError){
        val recipeRef = database.collection("recipes").document(recipeId)
        recipeRef.delete()
            .addOnSuccessListener{ _ ->
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
    fun getRecipeByAuthor(author:String, onSuccess: recipeListCallback, onError: onError){
        database.collection("recipes")
            .whereEqualTo("author", author)
            .get()
            .addOnSuccessListener { recipes ->
                val recipeList = recipes.documents.mapNotNull { it.toObject(Recipe::class.java) }
                onSuccess(recipeList)
            }
            .addOnFailureListener{ exception ->
                onError(exception)
            }
    }
}
