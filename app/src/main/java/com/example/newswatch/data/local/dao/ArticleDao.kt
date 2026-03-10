package com.example.newswatch.data.local.dao

import androidx.room.*
import com.example.newswatch.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY cachedAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE category = :category OR category IS NULL ORDER BY cachedAt DESC")
    fun getArticlesByCategory(category: String?): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()

    @Query("DELETE FROM articles WHERE cachedAt < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)

    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun deleteArticlesByCategory(category: String?)
}