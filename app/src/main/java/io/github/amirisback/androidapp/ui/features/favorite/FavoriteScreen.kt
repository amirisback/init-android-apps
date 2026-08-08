package io.github.amirisback.androidapp.ui.features.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.components.EmptyState
import io.github.amirisback.androidapp.ui.components.ErrorMessage
import io.github.amirisback.androidapp.ui.components.LoadingIndicator
import io.github.amirisback.androidapp.ui.components.MealCard

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel,
    onItemClick: (MealModel) -> Unit,
    modifier: Modifier = Modifier
) {
    // Refresh favorite list when entering screen
    LaunchedEffect(Unit) {
        viewModel.getData()
    }

    val resourceState by viewModel.mealsState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = resourceState) {
            is Resource.Loading -> {
                LoadingIndicator()
            }
            is Resource.Error -> {
                ErrorMessage(message = state.message ?: stringResource(id = R.string.frogo_is_empty_data))
            }
            is Resource.Success -> {
                val meals = state.data ?: emptyList()
                if (meals.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = meals,
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
}
