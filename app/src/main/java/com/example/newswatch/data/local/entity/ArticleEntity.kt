package com.example.newswatch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.newswatch.data.model.Article
import com.example.newswatch.data.model.Source

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val url: String,
    val sourceName: String,
    val author: String?,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?,
    val category: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Convert Entity to Domain Model
 */
fun ArticleEntity.toArticle(): Article {
    return Article(
        source = Source(id = null, name = this.sourceName),
        author = this.author,
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content
    )
}

/**
 * Convert Domain Model to Entity
 */
fun Article.toEntity(category: String?): ArticleEntity {
    return ArticleEntity(
        url = this.url,
        sourceName = this.source.name,
        author = this.author,
        title = this.title,
        description = this.description,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content,
        category = category
    )
}