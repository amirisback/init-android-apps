package io.github.amirisback.androidapp.feature.main

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.frogobox.compose.view.FrogoComposeActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.core.ui.component.AppTopAppBar
import io.github.amirisback.androidapp.core.ui.theme.InitTheme
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.feature.about.AboutUsActivity
import io.github.amirisback.androidapp.feature.detail.DetailActivity
import io.github.amirisback.androidapp.feature.favorite.FavoriteRoute
import io.github.amirisback.androidapp.feature.favorite.FavoriteViewModel

@AndroidEntryPoint
class MainActivity : FrogoComposeActivity() {

    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    enum class MainTab(val titleRes: Int, val iconRes: Int) {
        MAIN(R.string.title_main, R.drawable.ic_tv),
        FAVORITE(R.string.title_fav, R.drawable.ic_favorite),
    }

    override fun setupActivityResultExt(result: ActivityResult) {
        super.setupActivityResultExt(result)
        favoriteViewModel.loadFavorites()
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            MainScaffold(
                mainViewModel = mainViewModel,
                favoriteViewModel = favoriteViewModel,
                onMealClick = { meal ->
                    startActivityResultExt(DetailActivity.createIntent(this, meal))
                },
                onAboutClick = {
                    AboutUsActivity.launch(this)
                },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    mainViewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onMealClick: (Meal) -> Unit,
    onAboutClick: () -> Unit,
) {
    var currentTab by rememberSaveable { mutableStateOf(MainActivity.MainTab.MAIN) }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = currentTab.titleRes),
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.title_about_us),
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                MainActivity.MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        label = { Text(text = stringResource(id = tab.titleRes)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = stringResource(id = tab.titleRes),
                            )
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when (currentTab) {
                MainActivity.MainTab.MAIN -> {
                    MainRoute(
                        viewModel = mainViewModel,
                        onMealClick = onMealClick,
                    )
                }
                MainActivity.MainTab.FAVORITE -> {
                    FavoriteRoute(
                        viewModel = favoriteViewModel,
                        onMealClick = onMealClick,
                    )
                }
            }
        }
    }
}
