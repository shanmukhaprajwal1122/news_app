package com.example.newswatch.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.newswatch.R
import com.example.newswatch.data.model.Article
import com.example.newswatch.databinding.FragmentDetailBinding
import com.example.newswatch.ui.viewmodel.NewsViewModel
import com.example.newswatch.ui.viewmodel.NewsViewModelFactory

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val args: DetailFragmentArgs by navArgs()

    private val viewModel: NewsViewModel by activityViewModels {
        NewsViewModelFactory(requireActivity().application)
    }

    private var currentArticle: Article? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadArticle()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadArticle() {
        val articleUrl = args.articleUrl

        // Observe all articles once to find the one we need
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            currentArticle = articles.find { it.url == articleUrl }
            currentArticle?.let { article ->
                displayArticle(article)
                // Stop observing after we find and display the article
                viewModel.articles.removeObservers(viewLifecycleOwner)
            }
        }
    }

    private fun displayArticle(article: Article) {
        binding.apply {
            // Load image
            if (article.urlToImage != null) {
                Glide.with(requireContext())
                    .load(article.urlToImage)
                    .placeholder(R.color.gray_light)
                    .error(R.color.gray_medium)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivArticleImage)
                ivArticleImage.isVisible = true
            } else {
                ivArticleImage.isVisible = false
            }

            // Set title
            tvTitle.text = article.title

            // Set author
            tvAuthor.text = article.author ?: getString(R.string.author_unknown)

            // Set date
            tvDate.text = article.getFormattedDate()

            // Set source
            chipSource.text = getString(R.string.source_prefix, article.source.name)

            // Set description
            if (!article.description.isNullOrBlank()) {
                tvDescription.text = article.description
                tvDescription.isVisible = true
            } else {
                tvDescription.isVisible = false
            }

            // Set content
            if (!article.content.isNullOrBlank()) {
                tvContent.text = article.content
                tvContent.isVisible = true
            } else {
                tvContent.isVisible = false
            }

            // Read more button
            btnReadMore.setOnClickListener {
                openInBrowser(article.url)
            }
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error - no browser available
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}