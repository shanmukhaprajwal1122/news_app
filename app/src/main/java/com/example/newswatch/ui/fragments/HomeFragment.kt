package com.example.newswatch.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newswatch.R
import com.example.newswatch.data.model.Article
import com.example.newswatch.databinding.FragmentHomeBinding
import com.example.newswatch.ui.adapters.NewsAdapter
import com.example.newswatch.ui.viewmodel.NewsViewModel
import com.example.newswatch.ui.viewmodel.NewsViewModelFactory
import com.example.newswatch.utils.Constants

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by viewModels {
        NewsViewModelFactory(requireActivity().application)
    }

    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupChips()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_refresh -> {
                    viewModel.refresh()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { article ->
            navigateToDetail(article)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupChips() {
        binding.apply {
            chipAll.setOnClickListener { viewModel.selectCategory(null) }
            chipBusiness.setOnClickListener { viewModel.selectCategory(Constants.CATEGORY_BUSINESS) }
            chipTechnology.setOnClickListener { viewModel.selectCategory(Constants.CATEGORY_TECHNOLOGY) }
            chipSports.setOnClickListener { viewModel.selectCategory(Constants.CATEGORY_SPORTS) }
            chipEntertainment.setOnClickListener { viewModel.selectCategory(Constants.CATEGORY_ENTERTAINMENT) }
            chipHealth.setOnClickListener { viewModel.selectCategory(Constants.CATEGORY_HEALTH) }
            chipScience.setOnClickListener { viewModel.selectCategory(Constants.CATEGORY_SCIENCE) }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        // Observe articles
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            newsAdapter.submitList(articles)
            updateEmptyState(articles.isEmpty())
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading && newsAdapter.itemCount == 0
            binding.swipeRefresh.isRefreshing = false
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (newsAdapter.itemCount == 0) {
                    // Show error in empty state
                    binding.tvEmpty.text = it
                } else {
                    // Show toast if we have cached data
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearError()
            }
        }

        // Observe offline status
        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            binding.offlineBanner.isVisible = isOffline
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.isVisible = isEmpty && !viewModel.isCurrentlyLoading()
        binding.recyclerView.isVisible = !isEmpty

        if (isEmpty) {
            binding.tvEmptySubtitle.isVisible = viewModel.isOffline.value ?: false
        }
    }

    private fun navigateToDetail(article: Article) {
        val action = HomeFragmentDirections.actionHomeToDetail(article.url)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}