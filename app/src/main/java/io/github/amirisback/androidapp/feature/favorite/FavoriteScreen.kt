package io.github.amirisback.androidapp.feature.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun FavoriteRoute(
    viewModel: FavoriteViewModel,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoriteScreen(
        uiState = uiState,
        onMealClick = onMealClick,
        modifier = modifier,
    )
}

@Composable
fun FavoriteScreen(
    uiState: FavoriteUiState,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            is FavoriteUiState.Loading -> LoadingIndicator()
            is FavoriteUiState.Empty -> EmptyState()
            is FavoriteUiState.Error -> ErrorMessage(message = uiState.message)
            is FavoriteUiState.Success -> {
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
fun FavoriteScreenSuccessPreview() {
    InitTheme {
        FavoriteScreen(
            uiState = FavoriteUiState.Success(
                meals = listOf(
                    Meal(
                        id = "1",
                        name = "Creamy Chicken Pasta",
                        thumbnailUrl = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                        category = "Pasta",
                        area = "Italian",
                    ),
                ),
            ),
            onMealClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteScreenEmptyPreview() {
    InitTheme {
        FavoriteScreen(
            uiState = FavoriteUiState.Empty,
            onMealClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteScreenErrorPreview() {
    InitTheme {
        FavoriteScreen(
            uiState = FavoriteUiState.Error(message = "Failed to load favorites"),
            onMealClick = {},
        )
    }
}
