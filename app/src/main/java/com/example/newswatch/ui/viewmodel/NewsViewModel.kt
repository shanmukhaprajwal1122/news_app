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

    // Articles — driven entirely by Flow from repository
    val articles: LiveData<List<Article>>

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
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

        // switchMap re-subscribes to Flow whenever category changes
        // Flow handles cache + API + fresh emit — all internally
        articles = _selectedCategory.switchMap { category ->
            _isLoading.value = true
            _isOffline.value = !repository.isNetworkAvailable()
            repository.getArticles(category).asLiveData().also {
                _isLoading.value = false
            }
        }

        cleanCache()
    }

    /**
     * Change category — triggers switchMap → new Flow subscription
     */
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    /**
     * Refresh — re-triggers same category Flow
     */
    fun refresh() {
        _selectedCategory.value = _selectedCategory.value
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
