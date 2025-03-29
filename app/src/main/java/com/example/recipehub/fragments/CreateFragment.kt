package com.example.recipehub.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.recipehub.databinding.FragmentCreateBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.setupUI
import com.example.recipehub.utils.uriToBitmap

class CreateFragment : Fragment() {
    private var binding: FragmentCreateBinding? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var imageUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateBinding.inflate(inflater, container, false)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                val bitmap = uriToBitmap(selectedUri, requireContext())
                bitmap?.let {
                    val fileUri = saveBitmapToFile(it, requireContext())
                    fileUri?.let { uri ->
                        Log.d("Profile", "new avatar: $uri")
                        imageUrl = uri.toString()
                        binding?.imageUrlImageView?.setImageURI(uri)
                        binding?.imageUrlImageView?.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding?.apply {
            uploadBtn.setOnClickListener { pickImageLauncher.launch("image/*") }
            postBtn.setOnClickListener { onCreate() }
        }
        return binding?.root
    }

    private fun onCreate() {
        binding?.apply {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val author = requireContext().getStringShareRef("username", "userInfo")

            if (title.isEmpty()) {
                titleEditText.error = "Title is required"
                return@apply
            }
            if (description.isEmpty()) {
                descriptionEditText.error = "Description is required"
                return@apply
            }

            val recipe = Recipe(
                title = title,
                description = description,
                imageUrl = imageUrl,
                author = author
            )

            RecipeModel.shared.create(
                recipe,
                {
                    Log.d("Create", "Recipe created successfully")
                    requireActivity().supportFragmentManager.popBackStack()
                },
                { error ->
                    Log.e("Create", "Error creating recipe: $error")
                }
            )
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }
    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
