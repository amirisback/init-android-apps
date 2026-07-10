package io.github.amirisback.androidapp.ui.features.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.init.ui.theme.InitTheme

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
                CircularProgressIndicator()
            }
            is MainUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
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

@Composable
fun MealCard(
    meal: MealModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.strMealThumb)
                    .crossfade(true)
                    .build(),
                contentDescription = meal.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.strMeal ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = meal.strArea ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.strCategory ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MealCardPreview() {
    InitTheme {
        MealCard(
            meal = MealModel(
                idMeal = "1",
                strMeal = "Creamy Chicken Pasta",
                strMealThumb = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                strCategory = "Pasta description here. This is a very delicious and easy meal to make.",
                strArea = "Italian"
            ),
            onClick = {}
        )
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
