package com.example.recipehub.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.recipehub.utils.DateConverter
import com.example.recipehub.base.MyApplication
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.User


@Database(entities = [User::class, Recipe::class], version = 3)
@TypeConverters(DateConverter::class)
abstract class AppLocalDbRepository: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao

    companion object{
        var instance: AppLocalDbRepository? = null
    }
}
object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        Room.databaseBuilder(
            context = MyApplication.context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        )   .fallbackToDestructiveMigration()
            .build()
    }
}