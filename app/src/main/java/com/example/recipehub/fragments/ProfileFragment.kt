package com.example.recipehub.fragments

import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.os.Bundle
import com.example.recipehub.R
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.recipehub.databinding.FragmentProfileBinding
import com.example.recipehub.model.UserModel
import com.example.recipehub.model.User
import com.squareup.picasso.Picasso
import androidx.core.net.toUri
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.uriToBitmap

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var avatarUri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        loadProfile()
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                val bitmap = uriToBitmap(selectedUri, requireContext())
                bitmap?.let {
                    val fileUri = saveBitmapToFile(it, requireContext())
                    fileUri?.let { uri ->
                        Log.d("Profile", "new avatar: $uri")
                        avatarUri = uri.toString()
                        binding?.avatarImageView?.setImageURI(uri)
                    }
                }
            }
        }
        binding?.avatarImageView?.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding?.resetBtn?.setOnClickListener { loadProfile() }
        binding?.submitBtn?.setOnClickListener { updateProfile() }
        binding?.updateProgressBar?.visibility = View.GONE

        return binding?.root
    }

    private fun loadProfile() {
        binding?.usernameEditText?.setText(getUserShareRef("username"))
        binding?.emailEditText?.setText(getUserShareRef("email"))
        binding?.passwordEditText?.setText(getUserShareRef("password"))
        val comments = getUserShareRef("comments")
        val recipes = getUserShareRef("recipes")
        binding?.profileTextView?.text = getString(R.string.profile_textView, comments.length, recipes.length)
        avatarUri = getUserShareRef("avatarUrl")
        Log.d("Profile", "Avatar URL: $avatarUri")
        if(avatarUri.startsWith("file://")){
            Log.d("Profile", "Loading image from file")
            val uri = avatarUri.toUri()
            binding?.avatarImageView?.setImageURI(uri)
        }
        else if(avatarUri.startsWith("http")){
            binding?.avatarImageView?.let { loadImageWithPicasso(avatarUri,it)}
        }
    }

    private fun loadImageWithPicasso(url: String, imageView: ImageView) {
        Picasso.get()
            .load(url)
            .into(imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    Log.d("Profile", "Image loaded successfully from URL: $url")
                }

                override fun onError(e: Exception?) {
                    Log.e("Profile", "Error loading image from URL: $url", e)
                }
            })
    }


    private fun updateProfile() {
        val updatedUser = User(
            id = getUserShareRef("id"),
            username = binding?.usernameEditText?.text.toString().takeIf { it.isNotEmpty() } ?: getUserShareRef("username"),
            email = binding?.emailEditText?.text.toString().takeIf { it.isNotEmpty() } ?: getUserShareRef("email"),
            password = binding?.passwordEditText?.text.toString().takeIf { it.isNotEmpty() } ?: getUserShareRef("password"),
            avatarUrl = avatarUri,
            recipes = getUserShareRef("recipes"),
            comments = getUserShareRef("comments")
        )

        UserModel.shared.updateUser(updatedUser, {
            binding?.updateProgressBar?.visibility = View.VISIBLE
            Log.d("Profile", "User updated")
            setUserShareRef("username", updatedUser.username)
            setUserShareRef("email", updatedUser.email)
            setUserShareRef("password", updatedUser.password)
            setUserShareRef("avatarUrl", updatedUser.avatarUrl)
            loadProfile()
        }, { error ->
            Log.e("Profile", error.message ?: "An error occurred")
        })
    }

    private fun getUserShareRef(resource: String): String {
        val sharedPref = requireContext().getSharedPreferences("userInfo", MODE_PRIVATE)
        return sharedPref?.getString(resource, "") ?: ""
    }

    private fun setUserShareRef(resource: String, value: String) {
        val sharedPref = requireContext().getSharedPreferences("userInfo", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString(resource, value)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }
}
