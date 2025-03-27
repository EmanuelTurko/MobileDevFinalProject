package com.example.recipehub.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Date


@Entity(tableName = "recipes")
data class Recipe(
    @ColumnInfo(name = "id")@PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    @ColumnInfo(name = "imageUrl")val imageUrl: String = "",
    val author: String = "",
    @ColumnInfo(name = "rating") val rating: Float? = 0f,
    @ColumnInfo(name = "comments") var comments: List<String> = emptyList<String>(),
    @ColumnInfo(name = "date") val date: Date? = null
) : Serializable