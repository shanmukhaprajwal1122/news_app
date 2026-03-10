package com.example.newswatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.newswatch.data.api.NewsApiService
import com.example.newswatch.navigation.NavGraph
import com.example.newswatch.ui.theme.NewsWatchTheme
import com.example.newswatch.ui.viewmodel.NewsViewModel
import com.example.newswatch.ui.viewmodel.NewsViewModelFactory

class MainActivity : ComponentActivity() {

    // Lazy initialization - only created when first accessed
    private val apiService by lazy { NewsApiService.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsWatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: NewsViewModel = viewModel(
                        factory = remember { NewsViewModelFactory(apiService, applicationContext) }
                    )

                    NavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}