package com.example.recipehub.fragments
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.recipehub.databinding.FragmentRegisterBinding
import androidx.navigation.fragment.findNavController
import com.example.recipehub.R
import com.example.recipehub.model.Model
import com.example.recipehub.model.AppUser
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi

class RegisterFragment: Fragment() {
    private var binding : FragmentRegisterBinding? = null
    private var avatarUri: String? = null

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ) : View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding?.refLoginTextView?.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri:Uri? ->
            avatarUri = uri?.toString()
            binding?.avatarImageView?.setImageURI(uri)
        }
        binding?.registerBtn?.setOnClickListener(::onRegister)
        binding?.avatarImageView?.setOnClickListener{
            processAvatar()
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    @OptIn(UnstableApi::class)
    fun onRegister(view: View){
        val username = binding?.usernameEditText?.text.toString()
        val email = binding?.emailEditText?.text.toString()
        val password = binding?.passwordEditText?.text.toString()

        if(username.isEmpty()  || username.length<4){
            binding?.usernameEditText?.error = "Username must be at least 4 characters"
            return
        }
        if(email.isEmpty()){
            binding?.emailEditText?.error = "Email is required"
            return
        }
        val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
        if((!email.contains("@") || !email.contains("."))&& !email.matches(emailPattern) ){
            binding?.emailEditText?.error = "Valid email is required"
            return
        }
        if(password.isEmpty()){
            binding?.passwordEditText?.error = "Password is required"
            return
        }
        if(password.length<6 || !password.contains(Regex(".*[A-Z].*"))){
            binding?.passwordEditText?.error = "Password must be at least 6 characters and contain at least one capital letter"
            return
        }
        val user = AppUser(
            username = username,
            email = email,
            password = password,
            avatarUrl = avatarUri ?: "",
            recipes = "",
            comments = ""
        )
        Model.shared.addUser(user) {
            Log.d("Firebase", "User added")
        }


    }
    private fun processAvatar() {
        pickImageLauncher.launch("image/*")
    }


}