package com.example.newswatch.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.newswatch.utils.Constants

@Composable
fun CategoryTabs(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        "All" to null,
        "Business" to Constants.CATEGORY_BUSINESS,
        "Technology" to Constants.CATEGORY_TECHNOLOGY,
        "Sports" to Constants.CATEGORY_SPORTS,
        "Entertainment" to Constants.CATEGORY_ENTERTAINMENT,
        "Health" to Constants.CATEGORY_HEALTH,
        "Science" to Constants.CATEGORY_SCIENCE
    )

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.second == selectedCategory },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        categories.forEach { (label, category) ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(label) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}