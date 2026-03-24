package com.example.newswatch.data.local.dao

import androidx.room.*
import com.example.newswatch.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    /**
     * Get all articles sorted by cached time
     */
    @Query("SELECT * FROM articles ORDER BY cachedAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    /**
     * Get articles by category
     */
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY cachedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    /**
     * Get articles for "All" category (null category)
     */
    @Query("SELECT * FROM articles WHERE category IS NULL ORDER BY cachedAt DESC")
    fun getArticlesForAllCategory(): Flow<List<ArticleEntity>>

    /**
     * Insert articles (replace if exists based on unique url index)
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
     * Get article by URL (url is unique index, acts like a key)
     */
    @Query("SELECT * FROM articles WHERE url = :url LIMIT 1")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    /**
     * Get article by ID
     */
    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Int): ArticleEntity?

    /**
     * Update a single article
     */
    @Update
    suspend fun updateArticle(article: ArticleEntity)

    /**
     * Delete a single article
     */
    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    // Add these two at the bottom of the interface

    @Query("SELECT * FROM articles WHERE (:category IS NULL AND category IS NULL) OR category = :category ORDER BY cachedAt DESC")
    fun getArticlesByOptionalCategory(category: String?): Flow<List<ArticleEntity>>

    @Query("DELETE FROM articles WHERE (:category IS NULL AND category IS NULL) OR category = :category")
    suspend fun deleteArticlesByOptionalCategory(category: String?)
}