package com.example.newswatch.data.repository

import android.content.Context
import android.util.Log
import com.example.newswatch.data.api.NewsApiService
import com.example.newswatch.data.local.dao.ArticleDao
import com.example.newswatch.data.local.entity.toArticle
import com.example.newswatch.data.local.entity.toEntity
import com.example.newswatch.data.model.Article
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

    fun getArticlesFlow(category: String? = null): Flow<List<Article>> {
        return articleDao.getArticlesByCategory(category).map { entities ->
            entities.map { it.toArticle() }
        }
    }

    suspend fun refreshArticles(category: String? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val isOnline = NetworkHelper.isNetworkAvailable(context)

                if (isOnline) {
                    Log.d("NewsRepository", "🌐 Online - Fetching from API")

                    val response = apiService.getTopHeadlines(category = category)

                    if (response.isSuccessful && response.body() != null) {
                        val articles = response.body()!!.articles

                        Log.d("NewsRepository", "✅ Got ${articles.size} articles from API")

                        val entities = articles.map { it.toEntity(category) }

                        if (category != null) {
                            articleDao.deleteArticlesByCategory(category)
                        } else {
                            articleDao.deleteAllArticles()
                        }

                        articleDao.insertArticles(entities)

                        Log.d("NewsRepository", "💾 Saved to Room database")

                        Result.success(Unit)
                    } else {
                        Log.e("NewsRepository", "❌ API Error: ${response.code()}")
                        Result.failure(Exception("API Error: ${response.code()}"))
                    }
                } else {
                    Log.d("NewsRepository", "📴 Offline - Using cached data")
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e("NewsRepository", "❌ Exception: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun cleanOldCache() {
        withContext(Dispatchers.IO) {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            articleDao.deleteOldArticles(sevenDaysAgo)
            Log.d("NewsRepository", "🧹 Cleaned old cached articles")
        }
    }
}