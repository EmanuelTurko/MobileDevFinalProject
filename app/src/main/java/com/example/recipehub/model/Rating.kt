package com.example.recipehub.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "ratings")
data class Rating(
     val recipeId: String = "",
     @PrimaryKey val username: String = "",
    var rating: Float = 0f
) : Serializable
