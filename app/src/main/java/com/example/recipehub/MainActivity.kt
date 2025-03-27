package com.example.recipehub

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.fragment.NavHostFragment
import com.example.recipehub.databinding.ActivityMainBinding
import com.example.recipehub.utils.hideSystemUI
import setupUI

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var sharedPref: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
        val navController = navHostFragment?.navController
        setContentView(binding?.root)
        setupUI(findViewById(android.R.id.content))
        hideSystemUI()

        sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE)
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "isLoggedIn") {
                refreshBottomNav()
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        refreshBottomNav()


        binding?.bottomNav?.setOnItemSelectedListener { item ->
            val currentDestinationId = navController?.currentDestination?.id
            when (item.itemId) {
                R.id.login -> {
                    if(currentDestinationId != R.id.loginFragment) {
                        navController?.navigate(R.id.action_to_login)
                        Log.e("MainActivity", "Login clicked")
                    }
                    true
                }

                R.id.register -> {
                    if(currentDestinationId != R.id.registerFragment) {
                        navController?.navigate(R.id.action_to_register)
                        Log.e("MainActivity", "Register clicked")
                    }
                    true
                }

                R.id.myRecipes -> {
                    if(isLoggedIn() && currentDestinationId != R.id.myRecipesFragment) {
                        navController?.navigate(R.id.action_to_myRecipes)
                        Log.e("MainActivity", "My Recipes clicked")
                    }
                    true
                }

                R.id.create -> {
                    if(isLoggedIn() && currentDestinationId != R.id.action_to_create) {
                                navController?.navigate(R.id.action_to_create)
                                Log.e("MainActivity", "Create clicked?")
                    }
                    true
                }

                R.id.home -> {
                    if(currentDestinationId != R.id.homeFragment) {
                        navController?.navigate(R.id.action_to_home)
                        Log.e("MainActivity", "Home clicked")
                    }
                    true
                }

                R.id.profile -> {
                    if(isLoggedIn() && currentDestinationId != R.id.profileFragment) {
                        navController?.navigate(R.id.action_to_profile)
                        Log.e("MainActivity", "Profile clicked")
                    }
                    true
                }

                R.id.logout -> {
                    onLogout()
                    if(currentDestinationId != R.id.homeFragment) {
                        navController?.navigate(R.id.action_to_home)
                        Log.e("MainActivity", "Logout clicked going to home")
                    }
                    else{
                        navController.navigate(R.id.action_to_login)
                        Log.e("MainActivity", "Logout clicked going to login")

                    }
                    refreshBottomNav()
                    true
                }

                else -> false
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }
    private fun refreshBottomNav(){
        val isLoggedIn = isLoggedIn()
        binding?.bottomNav?.menu?.clear()
        if (isLoggedIn) {
            binding?.bottomNav?.inflateMenu(R.menu.bottom_nav_menu_logged_in)
        } else {
            binding?.bottomNav?.inflateMenu(R.menu.bottom_nav_menu_logged_out)
        }
    }
    override fun onDestroy(){
        super.onDestroy()
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
    }
    private fun onLogout() {
        sharedPref.edit(){
            putBoolean("isLoggedIn", false)
            remove("avatarByteArray")
            apply()
        }
        refreshBottomNav()

    }
        fun showBottomNav() {
            binding?.bottomNav?.visibility = View.VISIBLE
        }
    }

