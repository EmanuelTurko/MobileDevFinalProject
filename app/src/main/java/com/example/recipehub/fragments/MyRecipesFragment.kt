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
import com.example.recipehub.R
import com.example.recipehub.adapters.RecipeAdapter
import com.example.recipehub.databinding.FragmentMyRecipesBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.utils.getStringShareRef
import com.example.recipehub.utils.setStringShareRef

class MyRecipesFragment :Fragment(){
    private var binding: FragmentMyRecipesBinding? = null
    private lateinit var recipeAdapter:RecipeAdapter
    private lateinit var recipeList:MutableList<Recipe>

    override fun onCreateView(
        inflator: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyRecipesBinding.inflate(inflator, container, false)
        val recyclerView = binding?.recipesRecyclerView
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
        val author = requireContext().getStringShareRef("username", "userInfo")
        RecipeModel.shared.getRecipesByAuthor(author,
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}