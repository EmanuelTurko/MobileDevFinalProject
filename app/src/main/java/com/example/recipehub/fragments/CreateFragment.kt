package com.example.recipehub.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.recipehub.databinding.FragmentCreateBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.SimulateLoading
import com.example.recipehub.utils.getCorrectlyOrientedBitmap
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.saveBitmapToFile
import com.example.recipehub.utils.setupUI
import com.example.recipehub.utils.uriToBitmap

class CreateFragment : Fragment() {
    private var binding: FragmentCreateBinding? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateBinding.inflate(inflater, container, false)

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
        binding?.postBtn?.setOnClickListener { onCreate() }

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
                    binding?.composeView?.apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            var isLoading by remember { mutableStateOf(true) }
                            SimulateLoading(
                                onLoadingComplete = { isLoading = false }, 10
                            )
                            LaunchedEffect(isLoading) {
                                if (!isLoading) {
                                    requireActivity().supportFragmentManager.popBackStack()
                                }
                            }
                        }
                    }
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
