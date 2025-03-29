package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.recipehub.model.Rating

@Dao
interface RatingDao {
    @Insert
    fun insertRating(vararg rating: Rating)

    @Query("SELECT * FROM ratings WHERE recipeId = :recipeId")
    fun getRatingsByRecipeId(recipeId: String): List<Rating>

    @Query("SELECT * FROM ratings WHERE username = :username AND recipeId = :recipeId")
    fun getRatingsByUsername(username: String,recipeId:String): List<Rating>

    @Update
    fun updateRating(rating: Rating)

}