package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.recipehub.model.Recipe

@Dao
interface RecipeDao {

    @Insert
    fun insertRecipe(vararg recipe: Recipe)
}