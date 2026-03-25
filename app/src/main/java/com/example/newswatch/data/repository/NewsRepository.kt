package com.example.newswatch.data.repository

import android.content.Context
import android.util.Log
import com.example.newswatch.data.api.NewsApiService
import com.example.newswatch.data.local.dao.ArticleDao
import com.example.newswatch.data.local.entity.toArticle
import com.example.newswatch.data.local.entity.toEntity
import com.example.newswatch.data.model.Article
import com.example.newswatch.utils.Constants
import com.example.newswatch.utils.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class NewsRepository(
    private val apiService: NewsApiService,
    private val articleDao: ArticleDao,
    private val context: Context
) {

    private val TAG = "NewsRepository"

    /**
     * Single Flow — handles everything:
     * 1. Emit cached DB data immediately (first snapshot)
     * 2. Fetch from API in background
     * 3. Save to DB
     * 4. Emit fresh data
     */
    fun getArticles(category: String?): Flow<List<Article>> = flow {

        // Step 1 — get cached snapshot once and emit immediately
        // .first() takes ONE emission and moves on — doesn't block forever
        val cached = articleDao
            .getArticlesByOptionalCategory(category)
            .first()
            .map { it.toArticle() }

        emit(cached)
        Log.d(TAG, "📦 Emitted ${cached.size} cached articles")

        // Step 2 — check network
        if (!NetworkHelper.isNetworkAvailable(context)) {
            Log.d(TAG, "📴 Offline - Showing cached data only")
            return@flow
        }

        // Step 3 — fetch fresh data from API
        Log.d(TAG, "🌐 Fetching from API (category: ${category ?: "all"})")
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getTopHeadlines(category = category)
            }

            if (response.isSuccessful && response.body() != null) {
                val freshEntities = response.body()!!.articles
                    .map { it.toEntity(category) }

                // Step 4 — save to DB
                withContext(Dispatchers.IO) {
                    articleDao.deleteArticlesByOptionalCategory(category)
                    articleDao.insertArticles(freshEntities)
                }

                Log.d(TAG, "💾 Saved ${freshEntities.size} articles")

                // Step 5 — emit fresh data
                val fresh = freshEntities.map { it.toArticle() }
                emit(fresh)
                Log.d(TAG, "✅ Emitted ${fresh.size} fresh articles")

            } else {
                Log.e(TAG, "❌ API Error: ${response.code()} - ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception: ${e.message}", e)
        }

    }.flowOn(Dispatchers.IO)

    /**
     * Clean old cached articles (older than 7 days)
     */
    suspend fun cleanOldCache() {
        withContext(Dispatchers.IO) {
            val expiryTime = System.currentTimeMillis() -
                    (Constants.CACHE_DURATION_DAYS * 24 * 60 * 60 * 1000L)
            articleDao.deleteOldArticles(expiryTime)
            Log.d(TAG, "🧹 Cleaned old cached articles")
        }
    }

    /**
     * Get cached article count
     */
    suspend fun getCachedArticleCount(): Int {
        return withContext(Dispatchers.IO) {
            articleDao.getArticleCount()
        }
    }

    /**
     * Check if network is available
     */
    fun isNetworkAvailable(): Boolean {
        return NetworkHelper.isNetworkAvailable(context)
    }
}
