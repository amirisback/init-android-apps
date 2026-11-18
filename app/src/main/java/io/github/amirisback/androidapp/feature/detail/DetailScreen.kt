package io.github.amirisback.androidapp.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.core.ui.component.AppTopAppBar
import io.github.amirisback.androidapp.core.ui.component.EmptyState
import io.github.amirisback.androidapp.core.ui.component.LoadingIndicator
import io.github.amirisback.androidapp.core.ui.theme.InitTheme
import io.github.amirisback.androidapp.domain.model.Meal

@Composable
fun DetailRoute(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { mealName ->
            onShowMessage(resources.getString(R.string.message_added_to_favorite, mealName))
            viewModel.consumeUserMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            onShowMessage(message)
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
        }
    }

    DetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddFavoriteClick = viewModel::addToFavorites,
        onRemoveFavoriteClick = viewModel::removeFromFavorites,
        modifier = modifier,
    )
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBackClick: () -> Unit,
    onAddFavoriteClick: () -> Unit,
    onRemoveFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.title_detail),
                onBackClick = onBackClick,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            val meal = uiState.meal
            if (meal != null) {
                DetailContent(
                    meal = meal,
                    isFavorite = uiState.isFavorite,
                    isLoading = uiState.isLoading,
                    onAddFavoriteClick = onAddFavoriteClick,
                    onRemoveFavoriteClick = onRemoveFavoriteClick,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (!uiState.isLoading) {
                EmptyState(message = stringResource(id = R.string.message_meal_not_found))
            }

            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun DetailContent(
    meal: Meal,
    isFavorite: Boolean,
    isLoading: Boolean,
    onAddFavoriteClick: () -> Unit,
    onRemoveFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val imageDescription = meal.name.ifBlank { stringResource(id = R.string.cd_meal_image) }

    Column(
        modifier = modifier.padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = imageDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.area,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isFavorite) {
            OutlinedButton(
                onClick = onRemoveFavoriteClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(text = stringResource(id = R.string.action_remove_favorite))
            }
        } else {
            Button(
                onClick = onAddFavoriteClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.action_add_favorite))
            }
        }
    }
}

private val previewDetailMeal = Meal(
    id = "52772",
    name = "Creamy Chicken Pasta",
    thumbnailUrl = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
    category = "Pasta",
    area = "Italian",
)

@Preview(showBackground = true)
@Composable
fun DetailScreenNotFavoritePreview() {
    InitTheme {
        DetailScreen(
            uiState = DetailUiState(meal = previewDetailMeal, isFavorite = false, isLoading = false),
            onBackClick = {},
            onAddFavoriteClick = {},
            onRemoveFavoriteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenFavoritePreview() {
    InitTheme {
        DetailScreen(
            uiState = DetailUiState(meal = previewDetailMeal, isFavorite = true, isLoading = false),
            onBackClick = {},
            onAddFavoriteClick = {},
            onRemoveFavoriteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenLoadingPreview() {
    InitTheme {
        DetailScreen(
            uiState = DetailUiState(meal = previewDetailMeal, isFavorite = false, isLoading = true),
            onBackClick = {},
            onAddFavoriteClick = {},
            onRemoveFavoriteClick = {},
        )
    }
}
