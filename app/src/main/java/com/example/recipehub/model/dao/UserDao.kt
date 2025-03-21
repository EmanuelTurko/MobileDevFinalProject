package com.example.recipehub.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipehub.model.AppUser

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg user: AppUser)


    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): AppUser

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): AppUser

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): AppUser*/
}