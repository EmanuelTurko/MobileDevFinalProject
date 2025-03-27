package com.example.recipehub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.databinding.ItemRecipeBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.UserModel
import com.example.recipehub.utils.setRecipeShareRef

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onRecipeClick: (Recipe)->Unit,
    private val onEditClick: (Recipe) -> Unit):RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])

    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {

            itemView.setOnClickListener{
                itemView.context.setRecipeShareRef(recipe)
                onRecipeClick(recipe)
            }
            UserModel.shared.getUserByUsername(recipe.author, { user -> binding.authorAvatarImageView.setImageURI(user?.avatarUrl?.toUri()) },
                { exception -> exception.printStackTrace() })

            binding.titleTextView.text = recipe.title
            if(recipe.imageUrl.isNotEmpty())  binding.recipeImageView.setImageURI(recipe.imageUrl.toUri())

            binding.descriptionTextView.text = if(recipe.description.length > 30) {recipe.description.take(30) + ".." }
            else{ recipe.description }

            binding.ratingRatingBar.rating = recipe.rating ?: 0f

            if(UserModel.shared.currentUser("username") == recipe.author){
                binding.editPostBtn.visibility = View.VISIBLE
            } else{
                binding.editPostBtn.visibility = View.GONE
            }
            binding.editPostBtn.setOnClickListener {
                onEditClick(recipe)
            }

            if(recipe.comments.isNotEmpty() == true) {binding.lastCommentTextView.text = recipe.comments[recipe.comments.size-1]
            }
            else{ binding.lastCommentTextView.text = "No comments yet" }

        }
    }
}