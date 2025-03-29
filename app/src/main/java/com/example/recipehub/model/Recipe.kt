package com.example.recipehub.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.recipehub.utils.Converters
import java.io.Serializable
import java.util.Date


@Entity(tableName = "recipes")
@TypeConverters(Converters::class)
data class Recipe(
    @ColumnInfo(name = "id")@PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    @ColumnInfo(name = "imageUrl")val imageUrl: String = "",
    val author: String = "",
    @ColumnInfo(name = "rating") val rating: List<Rating> = emptyList(),
    @ColumnInfo(name = "comments") var comments: List<String> = emptyList<String>(),
    @ColumnInfo(name = "date") val date: Date? = null
) : Serializable