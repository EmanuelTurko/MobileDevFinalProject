package com.example.recipehub.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipehub.R
import com.example.recipehub.databinding.FragmentLoginBinding
import com.example.recipehub.model.Model

class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding?.refRegisterTextView?.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        binding?.loginBtn?.setOnClickListener(::onLogin)

        return binding?.root
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

        Model.shared.login(email, password, {
            Log.e("Firebase", "Login successful")
        }, { error ->
            if (error.isNotEmpty()) {
                Log.e("Firebase", error)
                if (error.contains("Invalid")){
                    binding?.emailEditText?.error = error
                    binding?.passwordEditText?.error = error
                }
            }
        })
    }
}
