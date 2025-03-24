package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.recipehub.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg user: User)

    @Update
    fun updateUser(vararg user: User)


    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): User

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): User

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): User
}