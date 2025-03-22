package com.example.recipehub.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(tableName = "users")
data class User(
    @PrimaryKey val id:String,
    val username: String,
    val email: String,
    val password: String,
    val avatarUrl: String,
    val recipes: String,
    val comments: String,
)