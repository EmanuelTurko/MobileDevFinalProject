package com.example.recipehub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recipehub.databinding.ItemCommentBinding
import com.example.recipehub.model.Comment

class CommentAdapter(private var comments: List<Comment>): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.commentTextView.text = comment.content
            binding.commentPosterTextView.text = comment.poster
            binding.commentDateTextView.text = comment.date?.toString()
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

}