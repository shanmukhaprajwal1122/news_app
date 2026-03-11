package com.example.newswatch.data.api

import com.example.newswatch.data.model.NewsResponse
import com.example.newswatch.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NewsApiService {

    /**
     * Get top headlines by country and category
     */
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = Constants.COUNTRY_US,
        @Query("category") category: String? = null,
        @Query("apiKey") apiKey: String = Constants.API_KEY
    ): Response<NewsResponse>

    companion object {
        fun create(): NewsApiService {
            // Logging interceptor for debugging
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            // OkHttp client with timeout
            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .build()

            // Retrofit instance
            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}