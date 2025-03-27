package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.recipehub.model.Recipe
import kotlin.reflect.KClass

@Dao
interface RecipeDao {

    @Insert
    fun insertRecipe(vararg recipe: Recipe)

    @Update
    fun updateRecipe(vararg recipe: Recipe)

    @Query("SELECT * FROM recipes")
    fun getAll(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: String): Recipe

    @Query("DELETE FROM recipes WHERE id = :id")
    fun deleteRecipe(id: String)

    @Query("SELECT * FROM recipes WHERE author = :author")
    fun getRecipesByAuthor(author: String): List<Recipe>


}