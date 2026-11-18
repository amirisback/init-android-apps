package io.github.amirisback.androidapp.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.amirisback.androidapp.core.ui.component.EmptyState
import io.github.amirisback.androidapp.core.ui.component.ErrorMessage
import io.github.amirisback.androidapp.core.ui.component.LoadingIndicator
import io.github.amirisback.androidapp.core.ui.component.MealCard
import io.github.amirisback.androidapp.core.ui.theme.InitTheme
import io.github.amirisback.androidapp.domain.model.Meal

@Composable
fun MainRoute(
    viewModel: MainViewModel,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreen(
        uiState = uiState,
        onMealClick = onMealClick,
        modifier = modifier,
    )
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            is MainUiState.Loading -> LoadingIndicator()
            is MainUiState.Empty -> EmptyState()
            is MainUiState.Error -> ErrorMessage(message = uiState.message)
            is MainUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(
                        items = uiState.meals,
                        key = { it.id },
                    ) { meal ->
                        MealCard(
                            meal = meal,
                            onClick = { onMealClick(meal) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenSuccessPreview() {
    InitTheme {
        MainScreen(
            uiState = MainUiState.Success(
                meals = listOf(
                    Meal(
                        id = "1",
                        name = "Creamy Chicken Pasta",
                        thumbnailUrl = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                        category = "Pasta",
                        area = "Italian",
                    ),
                    Meal(
                        id = "2",
                        name = "Beef Wellington",
                        thumbnailUrl = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                        category = "Beef",
                        area = "British",
                    ),
                ),
            ),
            onMealClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenEmptyPreview() {
    InitTheme {
        MainScreen(
            uiState = MainUiState.Empty,
            onMealClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenErrorPreview() {
    InitTheme {
        MainScreen(
            uiState = MainUiState.Error(message = "Failed to load meals"),
            onMealClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenLoadingPreview() {
    InitTheme {
        MainScreen(
            uiState = MainUiState.Loading,
            onMealClick = {},
        )
    }
}
