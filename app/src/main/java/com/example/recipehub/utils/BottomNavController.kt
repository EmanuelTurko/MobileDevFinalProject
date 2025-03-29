package com.example.recipehub.utils

import androidx.appcompat.app.AppCompatActivity

interface BottomNavController {
    fun updateBottomNavSelection(menuItemId: Int)
    fun hideBottomNav()
    fun showBottomNav()

    companion object {
        fun from(activity: AppCompatActivity): BottomNavController {
            return activity as? BottomNavController ?: throw IllegalArgumentException(
                "Activity must implement BottomNavController"
            )
        }
    }
}