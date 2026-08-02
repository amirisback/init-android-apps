package io.github.amirisback.androidapp.ui.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.components.ErrorMessage
import io.github.amirisback.androidapp.ui.components.LoadingIndicator
import io.github.amirisback.androidapp.ui.components.MealCard
import io.github.amirisback.androidapp.ui.theme.InitTheme

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onItemClick: (MealModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    MainContent(
        uiState = uiState,
        onItemClick = onItemClick,
        modifier = modifier
    )
}

@Composable
fun MainContent(
    uiState: MainUiState,
    onItemClick: (MealModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is MainUiState.Loading -> {
                LoadingIndicator()
            }
            is MainUiState.Error -> {
                ErrorMessage(message = uiState.message)
            }
            is MainUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.meals,
                        key = { it.idMeal ?: "" }
                    ) { meal ->
                        MealCard(
                            meal = meal,
                            onClick = { onItemClick(meal) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentSuccessPreview() {
    InitTheme {
        MainContent(
            uiState = MainUiState.Success(
                meals = listOf(
                    MealModel(
                        idMeal = "1",
                        strMeal = "Creamy Chicken Pasta",
                        strMealThumb = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                        strCategory = "Pasta description here. This is a very delicious and easy meal to make.",
                        strArea = "Italian"
                    ),
                    MealModel(
                        idMeal = "2",
                        strMeal = "Beef Wellington",
                        strMealThumb = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                        strCategory = "Beef description here.",
                        strArea = "British"
                    )
                )
            ),
            onItemClick = {}
        )
    }
}
