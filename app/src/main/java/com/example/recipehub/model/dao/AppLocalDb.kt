package com.example.recipehub.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recipehub.base.MyApplication
import com.example.recipehub.model.User


@Database(entities = [User::class], version = 2)
abstract class AppLocalDbRepository: RoomDatabase() {

    abstract fun userDao(): UserDao

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