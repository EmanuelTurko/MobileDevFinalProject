package com.example.recipehub.fragments

import kotlinx.coroutines.delay
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.recipehub.R
import com.example.recipehub.databinding.FragmentLoginBinding
import com.example.recipehub.model.UserModel
import com.example.recipehub.utils.SimulateLoading
import com.example.recipehub.utils.setupUI

class LoginFragment : BaseFragment() {

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

    @OptIn(UnstableApi::class)
    private fun onLogin() {
        val email = binding?.emailEditText?.text.toString()
        val password = binding?.passwordEditText?.text.toString()

        if (email.isEmpty()) {
            binding?.emailEditText?.error = "Email is required"
            binding?.emailEditText?.requestFocus()
            return
        }
        val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
        if ((!email.contains("@") || !email.contains(".")) && !email.matches(emailPattern)) {
            binding?.emailEditText?.requestFocus()
            binding?.emailEditText?.error = "Valid email is required"
            return
        }
        if (password.isEmpty()) {
            binding?.passwordEditText?.error = "Password is required"
            binding?.passwordEditText?.requestFocus()
            return
        }
        binding?.loginBtn?.isEnabled = false
        UserModel.shared.login(email, password, {
            bottomNavController?.hideBottomNav()
            binding?.composeView?.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    var isLoading by remember { mutableStateOf(true) }
                    SimulateLoading(
                        onLoadingComplete = { isLoading = false },20

                    )
                    LaunchedEffect(isLoading) {
                        if (!isLoading) {
                            val fragment = requireActivity().supportFragmentManager.findFragmentById(R.id.action_to_login)
                            fragment?.let {
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .remove(it)
                                    .commit()
                            }
                            findNavController().navigate(R.id.action_to_home)
                            delay(1000)
                            try {
                                bottomNavController?.updateBottomNavSelection(R.id.home)
                                bottomNavController?.showBottomNav()
                            } catch (e: Exception) {
                                Log.e("RegisterFragment", "Error updating bottom nav: ${e.message}")
                            }

                        }
                    }
                }
            }
            UserModel.shared.setLoginState(true)
        }, { error ->
            binding?.loginBtn?.isEnabled = true
                binding?.emailEditText?.error = "Invalid credentials"
                binding?.emailEditText?.requestFocus()
                binding?.passwordEditText?.error =  "Invalid credentials"
                binding?.passwordEditText?.requestFocus()

            }
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }

}
