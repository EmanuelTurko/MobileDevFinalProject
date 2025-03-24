package com.example.recipehub.model

import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.recipehub.model.dao.AppLocalDb
import com.example.recipehub.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors


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
}