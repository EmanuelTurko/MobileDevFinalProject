package com.example.recipehub.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipehub.adapters.CommentAdapter
import com.example.recipehub.databinding.FragmentRecipeDetailsBinding
import com.example.recipehub.model.Comment
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.getRecipeShareRef
import com.example.recipehub.utils.setupUI

class RecipeDetailsFragment:Fragment() {

    private var binding: FragmentRecipeDetailsBinding? = null
    private lateinit var recipe: Recipe

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        recipe = requireContext().getRecipeShareRef()
            ?: throw IllegalArgumentException("Recipe not found")

        if(recipe.imageUrl.isNotEmpty()){
            binding?.recipeImageView?.setImageURI(recipe.imageUrl.toUri())
        }
        else{
            binding?.recipeImageView?.visibility = View.GONE
        }
        RecipeModel.shared.calculateAverage(recipe.id,{ rating ->
            binding?.recipeRatingBar?.rating = rating},{ exception ->
            Log.e("RecipeDetails", "Failed to load rating: ${exception.message}")
        })
        binding?.descriptionTextView?.text = recipe.description
        binding?.commentsRecyclerView?.layoutManager = LinearLayoutManager(context)
        RecipeModel.shared.getAllComments(recipe.id,{ commentList ->
            binding?.commentsRecyclerView?.adapter = CommentAdapter(commentList)
                                                    Log.d("Create", "Loaded ${commentList.size} comments")},
            { exception -> Log.e("RecipeDetails","Failed.. ${exception.message}") })
        binding?.addCommentBtn?.setOnClickListener { addComment() }

        return binding?.root
    }
    private fun addComment() {
        val comment = binding?.addCommentEditText?.text?.toString()?.trim()
        if (comment?.isNotEmpty() == true) {
            RecipeModel.shared.addComment(recipe.id, comment, onSuccess = {
                requireActivity().runOnUiThread {
                    RecipeModel.shared.getAllComments(recipe.id, { commentList ->
                        (binding?.commentsRecyclerView?.adapter as? CommentAdapter)?.updateCommentList(commentList)
                        Log.d("Create", "Added comment: $comment, now i have ${commentList.size} comments")
                        binding?.commentsRecyclerView?.smoothScrollToPosition(commentList.size - 1)
                        binding?.addCommentEditText?.text?.clear()
                    }, { exception ->
                        Log.e("create", "Failed to refresh comments: ${exception.message}")
                    })
                }
            }, onError = { exception ->
                Log.e("create", "Failed to add comment: ${exception.message}")
            })
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }
    override fun onDestroy(){
        super.onDestroy()
        binding = null
    }
}

