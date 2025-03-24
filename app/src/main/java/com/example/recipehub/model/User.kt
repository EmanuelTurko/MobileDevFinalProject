package com.example.recipehub.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "id")@PrimaryKey val id:String = "",
    val username: String,
    val email: String,
    val password: String,
    val avatarUrl: String,
    @ColumnInfo(name = "recipes")val recipes: String? = "",
    @ColumnInfo(name = "comments")val comments: String? ="",
)