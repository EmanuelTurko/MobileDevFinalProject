package com.example.recipehub.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "comments")
data class Comment (
    @ColumnInfo(name = "id")@PrimaryKey val id: String = "",
    val recipeId: String = "",
    val poster: String = "",
    val content: String = "",
    @ColumnInfo(name = "date") val date: Date? = null
)
