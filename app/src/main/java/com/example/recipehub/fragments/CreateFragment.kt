package com.example.recipehub.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipehub.R
import com.example.recipehub.databinding.FragmentCreateBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.uriToBitmap

class CreateFragment: Fragment() {
    private var binding: FragmentCreateBinding? = null
    private lateinit var pickImageLauncher : ActivityResultLauncher<String>
    private lateinit var imageUri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateBinding.inflate(inflater, container, false)
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                val bitmap = uriToBitmap(selectedUri, requireContext())
                bitmap?.let {
                    val fileUri = saveBitmapToFile(it, requireContext())
                    fileUri?.let { uri ->
                        imageUri = uri.toString()
                        binding?.imageUrlImageView?.setImageURI(uri)
                    }
                }
            }
        }
        binding?.imageUrlImageView?.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding?.postBtn?.setOnClickListener { onCreate() }


        return binding?.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
    private fun onCreate() {
        val title = binding?.titleEditText?.text.toString()
        val description = binding?.descriptionEditText?.text.toString()
        val difficulty = binding?.difficultyRatingBar?.rating
        val author = requireContext().getStringShareRef("username","userInfo")
        if(title.isEmpty()){
            binding?.titleEditText?.error = "Title is required"
            return
        }
        if(description.isEmpty()){
            binding?.descriptionEditText?.error = "Description is required"
            return
        }
        if(difficulty == 0f){
            binding?.difficultyRatingBar?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorSelectedText))
            return
        }
        binding?.difficultyRatingBar?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.defaultBG))

        val recipe = Recipe(
            title = title,
            description = description,
            difficulty = difficulty ?: 0f,
            imageUrl = imageUri,
            author = author,
        )
        RecipeModel.shared.create(recipe, {
            Log.d("Create", "Recipe created successfully")
            findNavController().popBackStack()
        },{ error ->
            Log.e("Create", "Error creating recipe: $error")
            }
        )
    }

}
