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
import com.example.recipehub.model.UserModel

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
        binding?.loginBtn?.setOnClickListener{ onLogin() }

        return binding?.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onLogin() {
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
        UserModel.shared.login(email, password, {
            Log.d("Login", "Login successful")
            UserModel.shared.setLoginState(true)
            findNavController().navigate(R.id.action_to_home)
        }, { error ->
                binding?.loginBtn?.isEnabled = true
                if (error.message?.contains("incorrect",true) == true){
                    binding?.emailEditText?.error = "Invalid credentials"
                    binding?.passwordEditText?.error =  "Invalid credentials"
                }
            }
        )
    }

}
