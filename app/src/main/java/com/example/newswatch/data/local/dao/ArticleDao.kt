package com.example.newswatch.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newswatch.data.local.entity.ArticleEntity

@Dao
interface ArticleDao {

    /**
     * Get all articles sorted by cached time
     */
    @Query("SELECT * FROM articles ORDER BY cachedAt DESC")
    fun getAllArticles(): LiveData<List<ArticleEntity>>

    /**
     * Get articles by category
     */
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY cachedAt DESC")
    fun getArticlesByCategory(category: String): LiveData<List<ArticleEntity>>

    /**
     * Get articles for "All" category (null category)
     */
    @Query("SELECT * FROM articles WHERE category IS NULL ORDER BY cachedAt DESC")
    fun getArticlesForAllCategory(): LiveData<List<ArticleEntity>>

    /**
     * Insert articles (replace if exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    /**
     * Delete all articles
     */
    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()

    /**
     * Delete articles older than specified timestamp
     */
    @Query("DELETE FROM articles WHERE cachedAt < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)

    /**
     * Delete articles by category
     */
    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun deleteArticlesByCategory(category: String)

    /**
     * Delete articles with null category (for "All")
     */
    @Query("DELETE FROM articles WHERE category IS NULL")
    suspend fun deleteArticlesForAllCategory()

    /**
     * Get article count
     */
    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getArticleCount(): Int

    /**
     * Get article by URL
     */
    @Query("SELECT * FROM articles WHERE url = :url LIMIT 1")
    suspend fun getArticleByUrl(url: String): ArticleEntity?
}