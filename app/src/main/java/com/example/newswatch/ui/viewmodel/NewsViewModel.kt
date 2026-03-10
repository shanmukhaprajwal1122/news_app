package com.example.newswatch.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newswatch.data.api.NewsApiService
import com.example.newswatch.data.local.database.NewsDatabase
import com.example.newswatch.data.model.Article
import com.example.newswatch.data.repository.NewsRepository
import com.example.newswatch.utils.NetworkHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NewsUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val isOffline: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(
    private val repository: NewsRepository,
    private val appContext: Context
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val articlesFlow = _selectedCategory.flatMapLatest { category ->
        repository.getArticlesFlow(category)
    }

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NewsUiState> = combine(
        articlesFlow,
        _isLoading,
        _isRefreshing,
        _error,
        _selectedCategory
    ) { articles, loading, refreshing, error, category ->
        NewsUiState(
            articles = articles,
            isLoading = loading,
            isRefreshing = refreshing,
            error = error,
            selectedCategory = category,
            isOffline = !NetworkHelper.isNetworkAvailable(appContext)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsUiState(isLoading = true)
    )

    init {
        loadArticles()
        cleanCache()
    }

    fun loadArticles(category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedCategory.value = category

            val result = repository.refreshArticles(category)

            result.onSuccess {
                _isLoading.value = false
            }

            result.onFailure { exception ->
                _error.value = if (NetworkHelper.isNetworkAvailable(appContext)) {
                    "Failed to fetch articles: ${exception.message}"
                } else {
                    "Offline - Showing cached articles"
                }
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            val result = repository.refreshArticles(_selectedCategory.value)

            result.onSuccess {
                _isRefreshing.value = false
            }

            result.onFailure { exception ->
                _error.value = if (NetworkHelper.isNetworkAvailable(appContext)) {
                    "Refresh failed: ${exception.message}"
                } else {
                    "Can't refresh - No internet connection"
                }
                _isRefreshing.value = false
            }
        }
    }

    fun selectCategory(category: String?) {
        loadArticles(category)
    }

    private fun cleanCache() {
        viewModelScope.launch {
            repository.cleanOldCache()
        }
    }
}

class NewsViewModelFactory(
    private val apiService: NewsApiService,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            // Database is created lazily, actual initialization happens on IO thread
            val database = NewsDatabase.getDatabase(context.applicationContext)
            val repository = NewsRepository(apiService, database.articleDao(), context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(repository, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}