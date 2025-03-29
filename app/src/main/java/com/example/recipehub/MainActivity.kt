package com.example.recipehub

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.edit
import androidx.navigation.fragment.NavHostFragment
import com.example.recipehub.databinding.ActivityMainBinding
import com.example.recipehub.utils.BottomNavController
import com.example.recipehub.utils.SimulateLoading
import com.example.recipehub.utils.hideSystemUI
import com.example.recipehub.utils.setupUI
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavOptions

class MainActivity : AppCompatActivity(), BottomNavController {

    private var binding: ActivityMainBinding? = null
    private lateinit var sharedPref: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as? NavHostFragment
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
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    if (currentDestinationId != R.id.loginFragment) {
                        navController?.navigate(R.id.action_to_login,null,navOptions)
                    }
                    true
                }

                R.id.register -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    if (currentDestinationId != R.id.registerFragment) {
                        navController?.navigate(R.id.action_to_register,null,navOptions)
                    }
                    true
                }

                R.id.myRecipes -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_out_left)
                        .build()
                        if (isLoggedIn() && currentDestinationId != R.id.action_to_myRecipes) {
                            navController?.navigate(R.id.action_to_myRecipes,null, navOptions)
                        } else {
                            navController?.navigate(R.id.action_to_login,null, navOptions.apply{NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)})
                            binding?.bottomNav?.post{
                                updateBottomNavSelection(R.id.login)
                            }
                        }
                    true
                }

                R.id.create -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_up_enter)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_down_pop_exit)
                        .build()


                    if (isLoggedIn() && currentDestinationId != R.id.action_to_create) {
                        navController?.navigate(R.id.action_to_create, null, navOptions)
                    } else {
                        if (currentDestinationId != R.id.loginFragment) {
                            navController?.navigate(R.id.action_to_login)
                            binding?.bottomNav?.post{
                                updateBottomNavSelection(R.id.login)
                            }
                        } else {
                            navController.navigate(R.id.action_to_register)
                            binding?.bottomNav?.post{
                                updateBottomNavSelection(R.id.register)
                            }
                        }
                    }
                    true
                }

                R.id.home -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_out_left)
                        .build()
                    if (currentDestinationId != R.id.homeFragment) {
                        navController?.navigate(R.id.action_to_home,null, navOptions)
                    }
                    true
                }

                R.id.profile -> {

                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()

                    if (isLoggedIn() && currentDestinationId != R.id.profileFragment) {
                        navController?.navigate(R.id.action_to_profile,null, navOptions)
                    } else {
                        if(!isLoggedIn()) {

                            if (currentDestinationId != R.id.loginFragment) {
                                navController?.navigate(R.id.action_to_login, null, navOptions)
                                binding?.bottomNav?.post {
                                    updateBottomNavSelection(R.id.login)
                                }
                            } else {
                                navController.navigate(R.id.action_to_register, null, navOptions)
                                binding?.bottomNav?.post {
                                    updateBottomNavSelection(R.id.register)
                                }
                            }
                        }
                    }
                    true
                }

                R.id.logout -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(android.R.anim.fade_out)
                        .setPopEnterAnim(android.R.anim.fade_in)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    onLogout {
                        if (currentDestinationId != R.id.homeFragment) {
                            navController?.navigate(R.id.action_to_home)
                            binding?.bottomNav?.post {
                                updateBottomNavSelection(R.id.action_to_home)
                            }
                        } else {
                            navController.navigate(R.id.action_to_login,null,navOptions)
                            binding?.bottomNav?.post {
                                updateBottomNavSelection(R.id.login)
                            }

                        }
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    private fun refreshBottomNav() {
        val isLoggedIn = isLoggedIn()
        binding?.bottomNav?.menu?.clear()
        if (isLoggedIn) {
            binding?.bottomNav?.inflateMenu(R.menu.bottom_nav_menu_logged_in)
        } else {
            binding?.bottomNav?.inflateMenu(R.menu.bottom_nav_menu_logged_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun onLogout(onComplete:() -> Unit) {
        showSpinner(15)
        hideBottomNav()
        sharedPref.edit {
            remove("username")
            putBoolean("isLoggedIn", false)
            remove("avatarByteArray")
            apply()
        }
        binding?.composeView?.postDelayed({
            refreshBottomNav()
            showBottomNav()
        }, 1700)
    }

    override fun showBottomNav() {
        binding?.bottomNav?.visibility = View.VISIBLE
    }
    override fun hideBottomNav() {
        binding?.bottomNav?.visibility = View.GONE
    }

    override fun updateBottomNavSelection(menuItemId: Int) {
        binding?.bottomNav?.selectedItemId = menuItemId
    }
    private fun showSpinner(durationMillis: Long) {
        binding?.composeView?.apply {
            visibility = View.VISIBLE
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var isLoading by remember { mutableStateOf(true) }
                SimulateLoading(
                    onLoadingComplete = { isLoading = false },
                    durationMillis
                )

                LaunchedEffect(isLoading) {
                    if (isLoading == false) {
                        binding?.composeView?.visibility = View.GONE
                    }
                }
            }
        }
    }
}

