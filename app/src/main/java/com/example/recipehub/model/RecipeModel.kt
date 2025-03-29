package com.example.recipehub.model

import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.recipehub.base.MyApplication.Companion.context
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import com.example.recipehub.utils.Converters
import com.example.recipehub.utils.getStringShareRef
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.Executors


typealias recipeListCallback = (List<Recipe>) -> Unit
typealias recipeCallback = (Recipe) -> Unit
typealias commentCallback = (Comment) -> Unit
typealias commentListCallback = (List<Comment>) -> Unit
class RecipeModel {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val recipeFirebaseModel = RecipeFirebaseModel()

    companion object{
        val shared = RecipeModel()
    }
    fun create(recipe: Recipe, callback: emptyCallback, onError: onError) {

        recipeFirebaseModel.create(recipe,{ updatedRecipe ->
            executor.execute {
                try{
                    database.recipeDao().insertRecipe(updatedRecipe)
                    val userRef = database.userDao().getUserByUsername(updatedRecipe.author)
                    val updatedRecipes = userRef.recipes.toMutableList().apply {
                        add(updatedRecipe.id)
                    }
                    val updatedUser = userRef.copy(recipes = updatedRecipes)
                    database.userDao().updateUser(updatedUser)
                    mainHandler.post {
                        Log.d("Create", "Recipe created")
                        callback()
                    }
                }catch (exception: Exception){
                    mainHandler.post {
                        Log.e("Create","Failed to add recipe to local database")
                        onError(exception)
                    }
                }
            }
        }, onError)
    }

    fun getAllRecipes(onSuccess: recipeListCallback, onError: onError) {
        executor.execute{
            val localRecipe: List<Recipe> = database.recipeDao().getAll()
            if(true){
                mainHandler.post { onSuccess(localRecipe) }
                return@execute
            }
            else{
                recipeFirebaseModel.getAllRecipes({ firebaseRecipe ->
                    mainHandler.post { onSuccess(firebaseRecipe) }
                }, onError)
            }
        }
    }

    fun getRecipeById(recipeId: String, onSuccess: (Recipe) -> Unit, onError: onError) {
        executor.execute{
            val localRecipe = database.recipeDao().getRecipeById(recipeId)
            if(true){
                mainHandler.post { onSuccess(localRecipe) }
                return@execute
            }
            else{
                recipeFirebaseModel.getRecipeById(recipeId,{ firebaseRecipe ->
                    mainHandler.post { onSuccess(firebaseRecipe) }
                    if (true) {
                        executor.execute {
                            database.recipeDao().insertRecipe(firebaseRecipe)
                        }
                    }
                }, onError)
            }
        }
    }

    fun updateRecipe(recipeId:String, recipe: Recipe, callback: emptyCallback, onError: onError) {
        recipeFirebaseModel.updateRecipe(recipeId, recipe, { updatedRecipe ->
            executor.execute {
                try {
                    database.recipeDao().updateRecipe(updatedRecipe)
                    mainHandler.post {
                        Log.d("Edit", "Recipe updated in local database")
                        callback()
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        Log.e("Edit", "Failed to update recipe in local database")
                        onError(exception)
                    }
                }
            }
        }, onError)
    }

    fun addComment(recipeId: String, comment: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val poster = context.getStringShareRef("username","userInfo")
        recipeFirebaseModel.addComment(recipeId,poster, comment, onSuccess = { commentFromDb ->
            executor.execute {
                try {
                    val recipe = database.recipeDao().getRecipeById(recipeId)
                    val updatedComments = recipe.comments.toMutableList().apply {
                        add(commentFromDb.content)
                    }
                    val updatedRecipe = recipe.copy(comments = updatedComments)
                    database.recipeDao().updateRecipe(updatedRecipe)
                    database.commentDao().insertComment(commentFromDb)
                    mainHandler.post {
                        Log.d("Create", "Comment added to local database")
                        onSuccess()
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        Log.e("Create", "Failed to add comment to local database", exception)
                        onError(exception)
                    }
                }
            }
        }, onError = { exception ->
            Log.e("Create", "Failed to add comment to Firebase", exception)
            onError(exception)
        })
    }
    fun getAllComments(recipeId: String, onSuccess: commentListCallback, onError: onError) {
        executor.execute{
            val localComment = database.commentDao().getAll()
            if(true){
                mainHandler.post { onSuccess(localComment) }
                return@execute
            }
            else{
                recipeFirebaseModel.getAllComments(recipeId,{ firebaseComment ->
                    mainHandler.post { onSuccess(firebaseComment) }
                }, onError)
            }
        }
    }
    fun deleteRecipe(recipeId:String, onSuccess: emptyCallback,onError:onError){
        recipeFirebaseModel.deleteRecipe( recipeId,{
            executor.execute{
                try{
                    database.recipeDao().deleteRecipe(recipeId)
                    mainHandler.post{
                        Log.d("Delete","Recipe deleted from local database")
                        onSuccess()
                    }
                } catch (exception: Exception){
                    mainHandler.post{
                        Log.e("Delete","Failed to delete recipe from local database")
                        onError(exception)
                    }
                }
            }
        }, onError)
    }
    fun getRecipesByAuthor(author:String,onSuccess: recipeListCallback, onError:onError){
        executor.execute{
            val localRecipes = database.recipeDao().getRecipesByAuthor(author)
            if(true){
                mainHandler.post { onSuccess(localRecipes) }
                return@execute
            }
            else{
                recipeFirebaseModel.getRecipeByAuthor(author,{ firebaseRecipes ->
                    mainHandler.post { onSuccess(firebaseRecipes) }
                }, onError)
            }
        }
    }
    fun saveRating(
        recipeId: String,
        rating: Float,
        username: String,
        onSuccess: emptyCallback,
        onError: onError
    ) {
        Log.d("rating", "saveRating: $recipeId, $rating, $username")
        recipeFirebaseModel.saveRating(recipeId, rating, username, { updatedRating ->
            Log.d("rating", "Data type1: ${rating::class.java}")
            Log.d("rating", "Data type2: ${updatedRating::class.java}")
            executor.execute {
                try {
                    val existingEntry =  database.ratingDao().getRatingsByUsername(username,recipeId)
                    if(existingEntry.isNotEmpty()){
                        val existingRating = existingEntry[0]
                        val updatedRatingList = existingRating.copy(rating = rating)
                        database.ratingDao().updateRating(updatedRatingList)
                        mainHandler.post{
                            Log.d("rating", "Rating already exists in local database, updated it")
                            onSuccess()
                        }
                        return@execute
                    }
                    Log.d("rating", "continue..")
                    // Create a new Rating object for local database
                    val newRating = Rating(recipeId = recipeId, username = username, rating = rating)

                    // Insert the new rating into the RatingDao table
                    database.ratingDao().insertRating(newRating)

                    // Fetch the recipe and update it with the new rating list
                    val recipe = database.recipeDao().getRecipeById(recipeId)
                    val updatedRecipe = recipe.copy(rating = updatedRating)

                    // Update the recipe in the local database
                    database.recipeDao().updateRecipe(updatedRecipe)

                    mainHandler.post {
                        Log.d("rating", "Rating saved to local database")
                        onSuccess()
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        Log.e("rating", "Failed to save rating to local database", exception)
                        onError(exception)
                    }
                }
            }
        }, onError)
    }
    fun calculateAverage(
        recipeId: String,
        onSuccess: (averageRating: Float) -> Unit,
        onError: (exception: Exception) -> Unit
    ) {
        executor.execute {
            try {
                // Get the list of ratings directly from the database
                val ratings = database.ratingDao().getRatingsByRecipeId(recipeId)

                var totalRating = 0f
                var userCount = 0

                // Loop through the list of Rating objects and calculate the total and user count
                for (rating in ratings) {
                    val ratingValue = rating.rating
                    if (true) {
                        totalRating += ratingValue
                        userCount++
                    }
                }

                // Calculate the average rating
                val averageRating = if (userCount > 0) {
                    totalRating / userCount
                } else {
                    0f
                }
                val updatedRating = averageRating
                Log.d("rating", "Average rating calculated: $averageRating type: ${ratings.javaClass}")
                // Return the result on the main thread
                mainHandler.post {
                    onSuccess(averageRating)
                }
            } catch (exception: Exception) {
                // Handle errors and pass the exception to the error callback
                mainHandler.post {
                    Log.e("rating", "Failed to calculate average rating", exception)
                    onError(exception)
                }
            }
        }
    }
}