package com.example.recipehub.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.databinding.ItemRecipeBinding
import com.example.recipehub.model.Recipe
import com.example.recipehub.model.RecipeModel
import com.example.recipehub.model.UserModel
import com.example.recipehub.utils.setRecipeShareRef

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onRecipeClick: (Recipe)->Unit,
    private val onEditClick: (Recipe) -> Unit):RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    var onRatingClickListener: OnRatingClickListener? = null

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
    fun getReversedRecipes(): List<Recipe> {
        return recipes.reversed()
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {

            itemView.setOnClickListener {
                itemView.context.setRecipeShareRef(recipe)
                onRecipeClick(recipe)
            }
            UserModel.shared.getUserByUsername(
                recipe.author,
                    { user -> binding.apply(){
                    authorAvatarImageView.setImageURI(user?.avatarUrl?.toUri())
                    authorTextView.text = user?.username
                    }
                },
                { exception -> exception.printStackTrace() })

            binding.titleTextView.text = recipe.title

            if (recipe.imageUrl.isNotEmpty()) binding.recipeImageView.setImageURI(recipe.imageUrl.toUri())

            binding.descriptionTextView.text = if (recipe.description.length > 30) {
                recipe.description.take(30) + ".."
            } else {
                recipe.description
            }
            RecipeModel.shared.calculateAverage(recipe.id, { average ->
                binding.ratingRatingBar.rating = average
            }, { exception ->
                Log.e("RecipeAdapter", "Failed to calculate average: ${exception.message}")
            })
            binding.ratingRatingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
                if(!fromUser) return@setOnRatingBarChangeListener
                onRatingClickListener?.onRatingClick(recipe.id, rating)
            }
            val user= UserModel.shared.currentUser("username")
            val logged = UserModel.shared.isLoggedIn()
            Log.d("Home", "$user and $logged")
            if (UserModel.shared.currentUser("username") == recipe.author && UserModel.shared.isLoggedIn() && user!= null) {
                  binding.editPostBtn.visibility = View.VISIBLE
            } else {
                binding.editPostBtn.visibility = View.GONE
            }
            binding.editPostBtn.setOnClickListener {
                onEditClick(recipe)
            }
            RecipeModel.shared.getAllComments(recipe.id, { commentList ->
                if (commentList.isNotEmpty()) {
                    val latestComment = commentList.last()
                    val formattedText =
                        SpannableString("${latestComment.poster}: ${latestComment.content}")
                    formattedText.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        latestComment.poster.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    formattedText.setSpan(
                        UnderlineSpan(),
                        0,
                        latestComment.poster.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.lastCommentTextView.text = formattedText
                } else {
                    binding.lastCommentTextView.gravity = Gravity.CENTER
                    binding.lastCommentTextView.text = "No comments yet"
                }
            }, { exception ->
                Log.e("RecipeAdapter", "Failed to load comments: ${exception.message}")
            })


        }
    }

    interface OnRatingClickListener {
        fun onRatingClick(recipeId: String, rating: Float)
    }
}
