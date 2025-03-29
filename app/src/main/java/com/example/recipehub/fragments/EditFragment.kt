package com.example.recipehub.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipehub.MainActivity
import com.example.recipehub.R
import com.example.recipehub.databinding.FragmentEditBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.uriToBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.recipehub.utils.getCorrectlyOrientedBitmap
import com.example.recipehub.utils.setupUI

class EditFragment : Fragment() {
    private var binding: FragmentEditBinding? = null
    private lateinit var editableRecipe: Recipe
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUrl: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        RecipeModel.shared.getRecipeById(
            requireContext().getStringShareRef("id", "recipeInfo"),
            { recipe ->
                editableRecipe = recipe
                binding?.apply {
                    titleEditText.setText(editableRecipe.title)
                    descriptionEditText.setText(editableRecipe.description)
                    if(imageUrlImageView.toString().isNotEmpty()){
                        imageUrlImageView.setImageURI(editableRecipe.imageUrl.toUri())
                    }
                    else{
                        imageUrlImageView.visibility = View.INVISIBLE
                    }
                }
            },
            { exception ->
                Log.e("Edit", "Failed to load recipe: ${exception.message}")
            }
        )
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let { selectedUri ->
                    val rotatedBitmap = getCorrectlyOrientedBitmap(requireContext(), selectedUri)
                    rotatedBitmap?.let { correctedBitmap ->
                        val fileUri = saveBitmapToFile(correctedBitmap, requireContext())
                        fileUri?.let { uri ->
                            imageUrl = uri.toString()
                            binding?.imageUrlImageView?.setImageURI(uri)
                        }
                    }
                }
            }
        }
        binding?.uploadBtn?.setOnClickListener{ val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent) }
        binding?.imageUrlImageView?.setOnClickListener{ val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent) }
        binding?.apply {
            updateBtn.setOnClickListener { onUpdate() }
            deleteBtn.setOnClickListener { onDelete() }
        }
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }
    private fun onUpdate() {
        binding?.apply {
            val title = titleEditText.text.toString().takeIf { it.isNotEmpty() }
                ?: editableRecipe.title
            val description = descriptionEditText.text.toString().takeIf { it.isNotEmpty() }
                ?: editableRecipe.description
            val imageUrl = this@EditFragment.imageUrl.takeIf { it.isNotEmpty() }
                ?: editableRecipe.imageUrl

            val updatedRecipe = Recipe(
                editableRecipe.id,
                title,
                description,
                imageUrl,
                editableRecipe.author
            )
            RecipeModel.shared.updateRecipe(
                updatedRecipe.id,
                updatedRecipe,
                {
                    findNavController().popBackStack()
                },
                { exception ->
                    Log.e("Edit", "Failed to update recipe: ${exception.message}")
                }
            )
        }
    }

    private fun onDelete() {
        RecipeModel.shared.deleteRecipe(
            editableRecipe.id,
            {
                findNavController().popBackStack()
            },
            { exception ->
                Log.e("Edit", "Failed to delete recipe: ${exception.message}")
            }
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
