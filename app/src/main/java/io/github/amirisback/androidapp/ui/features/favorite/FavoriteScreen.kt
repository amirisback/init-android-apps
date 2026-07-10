package io.github.amirisback.androidapp.ui.features.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.favorite.FavoriteViewModel
import io.github.amirisback.androidapp.ui.features.main.MealCard

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

    val resourceState = viewModel.mealsState.observeAsState(initial = Resource.Loading())

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = resourceState.value) {
            is Resource.Loading -> {
                CircularProgressIndicator()
            }
            is Resource.Error -> {
                Text(
                    text = state.message ?: stringResource(id = R.string.frogo_is_empty_data),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is Resource.Success -> {
                val meals = state.data ?: emptyList()
                if (meals.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.frogo_is_empty_data),
                        style = MaterialTheme.typography.bodyLarge
                    )
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

/**
 * Extension helper to observe LiveData in Compose without adding extra dependency
 */
@Composable
fun <T> LiveData<T>.observeAsState(initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this) {
        val observer = Observer<T> { state.value = it }
        observeForever(observer)
        onDispose {
            removeObserver(observer)
        }
    }
    return state
}
