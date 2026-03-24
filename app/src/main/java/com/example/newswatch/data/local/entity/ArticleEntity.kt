package com.example.newswatch.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.newswatch.data.model.Article
import com.example.newswatch.data.model.Source

@Entity(
    tableName = "articles",
    indices = [Index(value = ["url"], unique = true)]  // prevents duplicate URLs, fast lookup
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // efficient integer primary key
    val url: String,                    // unique but no longer the PK
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
        // id = 0 tells Room to auto-generate the ID
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