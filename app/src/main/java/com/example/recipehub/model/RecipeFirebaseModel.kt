package com.example.recipehub.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import java.util.Date
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.recipehub.utils.getStringShareRef

class RecipeFirebaseModel {

    private val database = FirebaseFirestore.getInstance()

    init {
        val setting = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }

        database.firestoreSettings = setting
    }

    fun create(recipe: Recipe, onSuccess: recipeCallback, onError: onError) {
        val recipeRef = database.collection("recipes")
        val documentId = recipeRef.document().id
        val recipeData = recipe.copy(
            id = documentId,
            date = Date()
        )
        UserFirebaseModel().getUserByUsername(recipeData.author, { user ->
            user?.let {
                val updatedRecipes = it.recipes.toMutableList()
                updatedRecipes.add(documentId)
                val userRef = database.collection("users").document(it.id)
                userRef.update("recipes", updatedRecipes)
                    .addOnSuccessListener {  }
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
                        val recipeFromDb = documentSnapshot.toObject(Recipe::class.java)
                        val timestamp = documentSnapshot.get("date") as? Timestamp
                        val date = timestamp?.toDate()
                        val updatedRecipe = recipeFromDb?.copy(date = date)

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


    fun getAllRecipes(onSuccess: recipeListCallback, onError: onError) {
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

    fun getRecipeById(recipeId: String, onSuccess: recipeCallback, onError: onError) {
        val recipeRef = database.collection("recipes").document(recipeId)
        recipeRef.get()
            .addOnSuccessListener { recipe ->
                val recipeData = recipe.toObject(Recipe::class.java)
                recipeData?.let {
                    onSuccess(it)
                } ?: onError(Exception("Failed to get recipe"))
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }

    }

    fun updateRecipe(
        recipeId: String,
        recipe: Recipe,
        onSuccess: recipeCallback,
        onError: onError
    ) {
        val recipeRef = database.collection("recipes").document(recipeId)
        val updatedJson = hashMapOf(
            "title" to recipe.title,
            "author" to recipe.author,
            "rating" to recipe.rating,
            "date" to recipe.date,
            "imageUrl" to recipe.imageUrl,
            "comments" to recipe.comments,
        )
        recipeRef.update(updatedJson as Map<String, Any?>)
            .addOnSuccessListener {
                onSuccess(recipe)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun addComment(
        recipeId: String,
        poster: String,
        comment: String,
        onSuccess: (Comment) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val commentRef = database.collection("recipes").document(recipeId).collection("comments")

        getRecipeById(
            recipeId,
            onSuccess = { recipe: Recipe ->
                val newComment = Comment(
                    recipeId = recipeId,
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


    fun getAllComments(recipeId: String, onSuccess: commentListCallback, onError: onError) {
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

    fun deleteRecipe(recipeId: String, onSuccess: emptyCallback, onError: onError) {
        val recipeRef = database.collection("recipes").document(recipeId)
        recipeRef.delete()
            .addOnSuccessListener { _ ->
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun getRecipeByAuthor(author: String, onSuccess: recipeListCallback, onError: onError) {
        database.collection("recipes")
            .whereEqualTo("author", author)
            .get()
            .addOnSuccessListener { recipes ->
                val recipeList = recipes.documents.mapNotNull { it.toObject(Recipe::class.java) }
                onSuccess(recipeList)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun saveRating(
        recipeId: String,
        rating: Float,
        username: String,
        onSuccess: (ratingList: MutableList<Rating>) -> Unit,
        onError: (exception: Exception) -> Unit
    ) {
        val recipeRef = database.collection("recipes").document(recipeId)

        recipeRef.get()
            .addOnSuccessListener { documentRating ->
                val currentRating =
                    documentRating.get("rating") as? List<Map<String, Any>> ?: mutableListOf()
                Log.d("rating", "Step 3: Extracted current ratings")
                val updatedRating = currentRating.toMutableList()

                val newRating = Rating(
                    recipeId = recipeId,
                    username = username,
                    rating = rating.toFloat()
                )
                val newRatingMap = mapOf(
                    "recipeId" to recipeId,
                    "username" to newRating.username,
                    "rating" to newRating.rating
                )
                updatedRating.add(newRatingMap)
                recipeRef.update("rating", updatedRating)
                    .addOnSuccessListener {
                        val updatedRatingList = updatedRating.map {
                            Rating(
                                username = it["username"] as String,
                                rating = (it["rating"] as Number).toFloat()
                            )
                        }.toMutableList()
                        onSuccess(updatedRatingList)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("rating", "Step 8: Failed to update Firebase", exception)
                        onError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("rating", "Step 9: Failed to fetch recipe data", exception)
                onError(exception)
            }

    }
    fun updateAuthorName(authorName: String, newAuthorName: String, onSuccess: emptyCallback, onError: onError) {
        getRecipeByAuthor(authorName, { recipes ->
            var recipesUpdated = 0
            var commentsUpdated = 0
            val totalRecipes = recipes.size
            var totalComments = 0
            for (recipe in recipes) {
                getAllComments(recipe.id, { comments ->
                    totalComments += comments.size
                    for (comment in comments) {
                        val commentRef = database.collection("recipes").document(recipe.id)
                            .collection("comments").document(comment.id)
                        commentRef.update("poster", newAuthorName)
                            .addOnSuccessListener {
                                commentsUpdated++
                                if (commentsUpdated == totalComments && recipesUpdated == totalRecipes) {
                                    onSuccess()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Update", "Failed to update comment", exception)
                            }
                    }
                }, onError)
                val recipeRef = database.collection("recipes").document(recipe.id)
                recipeRef.update("author", newAuthorName)
                    .addOnSuccessListener {
                        recipesUpdated++
                        if (recipesUpdated == totalRecipes && commentsUpdated == totalComments) {
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Update", "Failed to update recipe", exception)
                    }
            }
        }, onError)
    }
}
