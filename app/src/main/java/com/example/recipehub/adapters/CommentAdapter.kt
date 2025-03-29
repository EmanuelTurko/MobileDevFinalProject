package com.example.recipehub.adapters

import java.util.concurrent.TimeUnit
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.databinding.ItemCommentBinding
import com.example.recipehub.model.Comment
import com.example.recipehub.model.UserModel
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(private var comments: List<Comment>): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.commentPosterTextView.text = comment.poster
            binding.commentTextView.text = comment.content
            Log.d("CommentAdapter", "Comment date: ${comment.date}")
            val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
            try {
                val date = dateFormat.parse(comment.date.toString())
                if (date != null) {
                    val diffInMillis = System.currentTimeMillis() - date.time
                    val formattedTime = formatRelativeTime(diffInMillis)
                    binding.commentDateTextView.text = formattedTime
                }
            } catch (e: Exception) {
                binding.commentDateTextView.text = e.message
            }
            UserModel.shared.getUserByUsername(comment.poster,{ user ->
                binding.avatarImageView.setImageURI(user?.avatarUrl?.toUri())
            },{ exception ->
                exception.printStackTrace()
            })
        }
    }
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }
    override fun getItemCount(): Int {
        return comments.size
    }
    fun updateCommentList(newComments: List<Comment>){
        val oldSize = comments.size
        comments = newComments
        val newSize = comments.size
        notifyItemRangeInserted(oldSize, newSize-oldSize)
    }
    private fun formatRelativeTime(diffInMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
        return when {
            seconds < 60 -> "just now"
            minutes < 60 -> "$minutes m"
            hours < 24 -> "$hours h"
            else -> "$days d"
        }
    }

}