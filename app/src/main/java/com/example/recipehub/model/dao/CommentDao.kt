package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.recipehub.model.Comment


@Dao
interface CommentDao {
    @Insert
    fun insertComment(vararg comment: Comment)

    @Query("SELECT * FROM comments")
    fun getAll(): List<Comment>

}