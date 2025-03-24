package com.example.recipehub.model

import android.support.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.FieldValue
import java.util.Date


@Entity(tableName = "recipes")
data class Recipe(
    @ColumnInfo(name = "id")@PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val difficulty: Float = 0f,
    val author: String = "",
    @ColumnInfo(name = "rating") val rating: Float? = 0f,
    @ColumnInfo(name = "comments") val comments: String? = "",
    @ColumnInfo(name = "date") val date: Date? = null
)