package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.recipehub.model.Comment


@Dao
interface CommentDao {
    @Insert
    fun insertComment(vararg comment: Comment)

    @Query("SELECT * FROM comments WHERE recipeId = :recipeId")
    fun getAll(recipeId:String): List<Comment>

    @Query("SELECT * FROM comments WHERE poster=:poster")
    fun getAllByPoster(poster:String): List<Comment>

    @Update
    fun updateComment(vararg comment: Comment)

}