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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NewsRepository(
    private val apiService: NewsApiService,
    private val articleDao: ArticleDao,
    private val context: Context
) {

    private val TAG = "NewsRepository"

    /**
     * Get articles as Flow (offline-first)
     * Single function handles both null (Popular/All) and specific category
     */
    fun getArticles(category: String?): Flow<List<Article>> {
        return articleDao.getArticlesByOptionalCategory(category).map { entities ->
            entities.map { it.toArticle() }
        }
    }

    /**
     * Refresh articles from API
     * Saves to database on success
     */
    suspend fun refreshArticles(category: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkHelper.isNetworkAvailable(context)) {
                    Log.d(TAG, "📴 Offline - Using cached data")
                    return@withContext Result.success(Unit)
                }

                Log.d(TAG, "🌐 Online - Fetching from API (category: ${category ?: "all"})")

                val response = apiService.getTopHeadlines(category = category)

                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body()!!.articles

                    Log.d(TAG, "✅ Got ${articles.size} articles from API")

                    val entities = articles.map { it.toEntity(category) }

                    // Single call handles both null and specific category
                    articleDao.deleteArticlesByOptionalCategory(category)

                    articleDao.insertArticles(entities)

                    Log.d(TAG, "💾 Saved ${entities.size} articles to database")

                    Result.success(Unit)
                } else {
                    Log.e(TAG, "❌ API Error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

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