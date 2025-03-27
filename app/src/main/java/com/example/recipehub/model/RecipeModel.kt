package com.example.recipehub.model

import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.recipehub.base.MyApplication.Companion.context
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import com.example.recipehub.utils.getStringShareRef
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
}