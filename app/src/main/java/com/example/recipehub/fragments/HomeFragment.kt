package com.example.recipehub.fragments

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.MainActivity
import com.example.recipehub.adapters.RecipeAdapter
import com.example.recipehub.databinding.FragmentHomeBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.R
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.setStringShareRef
import com.example.recipehub.utils.setupUI

class HomeFragment : Fragment(), RecipeAdapter.OnRatingClickListener {
    private var binding: FragmentHomeBinding? = null
    private lateinit var recipeAdapter:RecipeAdapter
    private lateinit var recipeList:MutableList<Recipe>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val recyclerView = binding?.recipesRecyclerView
        (activity as MainActivity).showBottomNav()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recipeList = mutableListOf()
        recipeAdapter = RecipeAdapter(recipeList, { recipe ->
            findNavController().navigate(R.id.action_to_recipeDetails)
            Log.d("Home", "Selected recipe: ${recipe.title}") }, { recipe ->
                requireContext().setStringShareRef("id", recipe.id, "recipeInfo")
            Log.d("Home", "Selected recipe: ${recipe.id}")
            findNavController().navigate(R.id.action_to_edit)
            Log.d("Home", "recipe id: ${recipe.id}")
            Log.d("Home", "navigate to edit")
        })
        recipeAdapter.onRatingClickListener = this
        recyclerView?.adapter = recipeAdapter
        recyclerView?.addItemDecoration(object: RecyclerView.ItemDecoration(){
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State){
                outRect.bottom = 120
                }
            })
        fetchRecipes()

        return binding?.root
    }

    private fun fetchRecipes(){
        RecipeModel.shared.getAllRecipes(
            { recipes ->
                val initialSize = recipeList.size
                recipeList.addAll(recipes)
                Log.d("Home", "Fetched ${recipes.size} recipes")
                if(recipeList.isNotEmpty()) recipeAdapter.notifyItemRangeInserted(initialSize, recipes.size)
            },
            { exception ->
                (exception)
                Log.e("Home", "Error fetching recipes", exception)
            }
        )
    }
    override fun onRatingClick(recipeId: String, rating: Float) {
        Log.d("Home", "Rating clicked for recipe: $recipeId, rating: $rating")
        saveRating(recipeId, rating)
    }
    private fun saveRating(recipeId: String, rating: Float) {
        RecipeModel.shared.saveRating(recipeId, rating, requireContext().getStringShareRef("username","userInfo"),{ ->
            Log.d("Home", "Rating saved for recipe: $recipeId")
        }, { exception ->
            Log.e("Home", "Error saving rating", exception)
        })
    }
    override fun onResume(){
        super.onResume()
        recipeAdapter.notifyDataSetChanged()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setupUI(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}