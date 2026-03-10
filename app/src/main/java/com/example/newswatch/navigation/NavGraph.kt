package com.example.newswatch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.newswatch.data.model.Article
import com.example.newswatch.ui.screens.DetailScreen
import com.example.newswatch.ui.screens.HomeScreen
import com.example.newswatch.ui.screens.SplashScreen
import com.example.newswatch.ui.viewmodel.NewsViewModel
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Detail : Screen("detail/{article}") {
        fun createRoute(article: Article): String {
            val articleJson = Gson().toJson(article)
            val encodedJson = URLEncoder.encode(articleJson, StandardCharsets.UTF_8.toString())
            return "detail/$encodedJson"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: NewsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onArticleClick = { article ->
                    navController.navigate(Screen.Detail.createRoute(article))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("article") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedArticleJson = backStackEntry.arguments?.getString("article")
            val articleJson = encodedArticleJson?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            }
            val article = articleJson?.let {
                Gson().fromJson(it, Article::class.java)
            }

            if (article != null) {
                DetailScreen(
                    article = article,
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}