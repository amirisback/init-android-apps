package io.github.amirisback.androidapp.ui.features.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.components.AppTopAppBar
import io.github.amirisback.androidapp.ui.components.LoadingIndicator
import io.github.amirisback.androidapp.ui.theme.InitTheme

@Composable
fun DetailScreen(
    meal: MealModel?,
    isLoading: Boolean,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onInsertClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Detail Meals",
                onBackClick = onBackClick
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (meal != null) {
                DetailContent(
                    meal = meal,
                    isFavorite = isFavorite,
                    onInsertClick = onInsertClick,
                    onDeleteClick = onDeleteClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun DetailContent(
    meal: MealModel,
    isFavorite: Boolean,
    onInsertClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.strMealThumb)
                    .crossfade(true)
                    .build(),
                contentDescription = meal.strMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.strMeal ?: "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.strArea ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = meal.strCategory ?: "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isFavorite) {
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Remove From Database")
            }
        } else {
            Button(
                onClick = onInsertClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Insert to Database")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    InitTheme {
        DetailScreen(
            meal = MealModel(
                idMeal = "1",
                strMeal = "Creamy Chicken Pasta",
                strMealThumb = "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                strCategory = "Delicious pasta description",
                strArea = "Italian"
            ),
            isLoading = false,
            isFavorite = false,
            onBackClick = {},
            onInsertClick = {},
            onDeleteClick = {}
        )
    }
}
