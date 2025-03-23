package com.example.recipehub.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipehub.MainActivity
import com.example.recipehub.R
import com.example.recipehub.databinding.FragmentLoginBinding
import com.example.recipehub.model.Model
import androidx.core.content.edit

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding?.refRegisterTextView?.setOnClickListener {
            findNavController().navigate(R.id.action_to_register)
        }
        binding?.loginBtn?.setOnClickListener(::onLogin)

        return binding?.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun onLogin(view: View) {
        val email = binding?.emailEditText?.text.toString()
        val password = binding?.passwordEditText?.text.toString()

        if (email.isEmpty()) {
            binding?.emailEditText?.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            binding?.passwordEditText?.error = "Password is required"
            return
        }
        binding?.loginBtn?.isEnabled = false
        Model.shared.login(email, password, {
            Log.e("Firebase", "Login successful")
            onSuccessfulLogin()
        }, { error ->
            binding?.loginBtn?.isEnabled = true
            if (error.isNotEmpty()) {
                Log.e("Firebase", error)
                if (error.contains("Invalid")){
                    binding?.emailEditText?.error = error
                    binding?.passwordEditText?.error = error
                }
            }
        })
    }
    private fun onSuccessfulLogin() {
        val sharedPref = requireActivity().getSharedPreferences("RecipeHub", MODE_PRIVATE)
        sharedPref.edit() {
            putBoolean("isLoggedIn", true)
        }
        Log.e("Firebase", "User is logged in")
        findNavController().popBackStack(R.id.homeFragment, false)
        Log.e("Firebase", "Navigated to home fragment")
    }
}
