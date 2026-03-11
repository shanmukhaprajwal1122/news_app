package com.example.newswatch.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.newswatch.R
import com.example.newswatch.data.model.Article
import com.example.newswatch.databinding.ItemNewsBinding

class NewsAdapter(
    private val onItemClick: (Article) -> Unit
) : ListAdapter<Article, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NewsViewHolder(
        private val binding: ItemNewsBinding,
        private val onItemClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                // Set title (overlaid on image)
                tvTitle.text = article.title

                // Set source as author/source name
                tvSource.text = article.source.name

                // Set date
                tvDate.text = article.getShortDate()

                // Load image with Glide
                Glide.with(ivArticleImage.context)
                    .load(article.urlToImage)
                    .placeholder(R.color.gray_light)
                    .error(R.color.gray_medium)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivArticleImage)

                // Click listener
                root.setOnClickListener {
                    onItemClick(article)
                }
            }
        }
    }

    private class NewsDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}