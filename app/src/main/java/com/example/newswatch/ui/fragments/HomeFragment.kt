package com.example.newswatch.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    private val viewModel: NewsViewModel by activityViewModels {
        NewsViewModelFactory(requireActivity().application)
    }

    private lateinit var newsAdapter: NewsAdapter

    // Tab references for managing selection state
    private data class TabInfo(
        val container: LinearLayout,
        val textView: TextView,
        val indicator: View,
        val category: String?
    )

    private val tabs = mutableListOf<TabInfo>()

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
        setupTabs()
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

    private fun setupTabs() {
        binding.apply {
            // Build tab list: container, textView, indicator, category
            tabs.clear()
            tabs.add(TabInfo(tabPopular, tvTabPopular, indicatorPopular, null)) // Popular = general/all
            tabs.add(TabInfo(tabAll, tvTabAll, indicatorAll, Constants.CATEGORY_GENERAL))
            tabs.add(TabInfo(tabPolitics, tvTabPolitics, indicatorPolitics, Constants.CATEGORY_BUSINESS)) // Politics maps to business
            tabs.add(TabInfo(tabTechnology, tvTabTechnology, indicatorTechnology, Constants.CATEGORY_TECHNOLOGY))
            tabs.add(TabInfo(tabHealth, tvTabHealth, indicatorHealth, Constants.CATEGORY_HEALTH))
            tabs.add(TabInfo(tabScience, tvTabScience, indicatorScience, Constants.CATEGORY_SCIENCE))

            // Set click listeners for each tab
            tabs.forEach { tab ->
                tab.container.setOnClickListener {
                    selectTab(tab)
                    viewModel.selectCategory(tab.category)
                }
            }
        }
    }

    private fun selectTab(selected: TabInfo) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.tab_selected)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.tab_unselected)
        val indicatorColor = ContextCompat.getColor(requireContext(), R.color.tab_indicator)

        tabs.forEach { tab ->
            if (tab == selected) {
                tab.textView.setTextColor(selectedColor)
                tab.textView.setTypeface(null, android.graphics.Typeface.BOLD)
                tab.indicator.setBackgroundColor(indicatorColor)
            } else {
                tab.textView.setTextColor(unselectedColor)
                tab.textView.setTypeface(null, android.graphics.Typeface.NORMAL)
                tab.indicator.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
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
                    binding.tvEmpty.text = it
                } else {
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
        tabs.clear()
        _binding = null
    }
}