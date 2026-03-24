package com.example.newswatch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.newswatch.data.api.NewsApiService
import com.example.newswatch.data.local.database.NewsDatabase
import com.example.newswatch.data.model.Article
import com.example.newswatch.data.repository.NewsRepository
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NewsRepository

    // Current selected category
    private val _selectedCategory = MutableLiveData<String?>(null)

    // Articles from database (auto-updates UI)
    val articles: LiveData<List<Article>>

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Network status
    private val _isOffline = MutableLiveData<Boolean>()
    val isOffline: LiveData<Boolean> = _isOffline

    init {
        val apiService = NewsApiService.create()
        val articleDao = NewsDatabase.getDatabase(application).articleDao()
        repository = NewsRepository(apiService, articleDao, application)

        // .asLiveData() converts Flow → LiveData so switchMap works
        articles = _selectedCategory.switchMap { category ->
            repository.getArticles(category).asLiveData()
        }

        loadArticles()
        cleanCache()
    }

    /**
     * Load articles (refresh from API if online)
     */
    fun loadArticles(category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _selectedCategory.value = category
            _isOffline.value = !repository.isNetworkAvailable()

            val result = repository.refreshArticles(category)

            result.onSuccess {
                _isLoading.value = false
                _isOffline.value = !repository.isNetworkAvailable()
            }

            result.onFailure { exception ->
                _isLoading.value = false
                _isOffline.value = !repository.isNetworkAvailable()

                if (repository.isNetworkAvailable()) {
                    _errorMessage.value = "Failed to fetch articles: ${exception.message}"
                } else {
                    _errorMessage.value = "Offline - Showing cached articles"
                }
            }
        }
    }

    /**
     * Refresh current category
     */
    fun refresh() {
        loadArticles(_selectedCategory.value)
    }

    /**
     * Change category
     */
    fun selectCategory(category: String?) {
        loadArticles(category)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clean old cached articles
     */
    private fun cleanCache() {
        viewModelScope.launch {
            repository.cleanOldCache()
        }
    }

    fun getCurrentCategory(): String? = _selectedCategory.value

    fun isCurrentlyLoading(): Boolean = _isLoading.value ?: false

    /**
     * Get a specific article by its URL
     */
    fun articleByUrl(url: String): LiveData<Article?> = articles.map { list ->
        list.firstOrNull { it.url == url }
    }
}

/**
 * ViewModel Factory
 */
class NewsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}