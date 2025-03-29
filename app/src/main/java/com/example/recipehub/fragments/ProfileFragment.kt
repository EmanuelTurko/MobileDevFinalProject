package com.example.recipehub.fragments

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.example.recipehub.R
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.recipehub.databinding.FragmentProfileBinding
import com.example.recipehub.model.UserModel
import com.example.recipehub.model.User
import com.squareup.picasso.Picasso
import androidx.core.net.toUri
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.SimulateLoading
import com.example.recipehub.utils.getCorrectlyOrientedBitmap
import com.example.recipehub.utils.getStringListShareRef
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.setStringShareRef
import com.example.recipehub.utils.setupUI
import com.example.recipehub.utils.uriToBitmap

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var avatarUri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        loadProfile()
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    uri?.let { selectedUri ->
                        val rotatedBitmap =
                            getCorrectlyOrientedBitmap(requireContext(), selectedUri)
                        rotatedBitmap?.let { correctedBitmap ->
                            val fileUri = saveBitmapToFile(correctedBitmap, requireContext())
                            fileUri?.let { uri ->
                                avatarUri = uri.toString()
                                binding?.avatarImageView?.setImageURI(uri)
                            }
                        }
                    }
                }
            }
        binding?.avatarImageView?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
        binding?.resetBtn?.setOnClickListener { loadProfile() }
        binding?.submitBtn?.setOnClickListener { updateProfile() }

        return binding?.root
    }

    private fun loadProfile() {
        binding?.usernameEditText?.setText(
            requireContext().getStringShareRef(
                "username",
                "userInfo"
            )
        )
        binding?.emailEditText?.setText(requireContext().getStringShareRef("email", "userInfo"))
        binding?.passwordEditText?.setText(
            requireContext().getStringShareRef(
                "password",
                "userInfo"
            )
        )
        avatarUri = requireContext().getStringShareRef("avatarUrl", "userInfo")
        if (avatarUri.startsWith("file://")) {
            val uri = avatarUri.toUri()
            binding?.avatarImageView?.setImageURI(uri)
        } else if (avatarUri.startsWith("http")) {
            binding?.avatarImageView?.let { loadImageWithPicasso(avatarUri, it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }

    private fun loadImageWithPicasso(url: String, imageView: ImageView) {
        Picasso.get()
            .load(url)
            .into(imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {}

                override fun onError(e: Exception?) {}
            })
    }


    private fun updateProfile() {
        val updatedUser = User(
            id = requireContext().getStringShareRef("id", "userInfo"),
            username = binding?.usernameEditText?.text.toString().takeIf { it.isNotEmpty() }
                ?: requireContext().getStringShareRef("username", "userInfo"),
            email = binding?.emailEditText?.text.toString().takeIf { it.isNotEmpty() }
                ?: requireContext().getStringShareRef("email", "userInfo"),
            password = binding?.passwordEditText?.text.toString().takeIf { it.isNotEmpty() }
                ?: requireContext().getStringShareRef("password", "userInfo"),
            avatarUrl = avatarUri,
            recipes = requireContext().getStringListShareRef("recipes", "userInfo"),
            comments = requireContext().getStringListShareRef("comments", "userInfo")
        )

        if (updatedUser.username != requireContext().getStringShareRef("username", "userInfo")) {
            RecipeModel.shared.updateAuthorName(
                requireContext().getStringShareRef("username", "userInfo"), updatedUser.username, {
                    requireContext().setStringShareRef("username", updatedUser.username, "userInfo")
                }, { exception ->
                    Log.e("Profile", "Error updating author name: ${exception.message}")
                })
        }
            UserModel.shared.updateUser(updatedUser, {
                requireContext().setStringShareRef("email", updatedUser.email, "userInfo")
                requireContext().setStringShareRef("password", updatedUser.password, "userInfo")
                requireContext().setStringShareRef("avatarUrl", updatedUser.avatarUrl, "userInfo")
                loadProfile()
                binding?.composeView?.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        var isLoading by remember { mutableStateOf(true) }
                        SimulateLoading(
                            onLoadingComplete = { isLoading = false }, 10
                        )
                        LaunchedEffect(isLoading) {
                            if (!isLoading) {
                                binding?.composeView?.visibility = View.GONE
                            }
                        }
                    }
                }
            }, { error ->
                Log.e("Profile", error.message ?: "An error occurred")
            })
        }
    }
