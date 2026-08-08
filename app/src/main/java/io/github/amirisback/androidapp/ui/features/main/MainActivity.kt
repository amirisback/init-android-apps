package io.github.amirisback.androidapp.ui.features.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.common.base.BaseActivity
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.components.AppTopAppBar
import io.github.amirisback.androidapp.ui.detail.DetailActivity
import io.github.amirisback.androidapp.ui.features.favorite.FavoriteScreen
import io.github.amirisback.androidapp.ui.features.favorite.FavoriteViewModel
import io.github.amirisback.androidapp.ui.theme.InitTheme

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    enum class Tab(val titleRes: Int, val iconRes: Int) {
        MAIN(R.string.title_main, R.drawable.ic_tv),
        FAVORITE(R.string.title_fav, R.drawable.ic_favorite)
    }

    override fun setupActivityResultExt(result: ActivityResult) {
        super.setupActivityResultExt(result)
        favoriteViewModel.getData()
    }

    override fun setupViewModel() {}

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
        setupToolbar()
        mainViewModel.searchMeal("Cream")
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            MainActivityScreen(
                mainViewModel = mainViewModel,
                favoriteViewModel = favoriteViewModel,
                onItemClick = { meal ->
                    startActivityResultExt(DetailActivity.createIntent(this, meal))
                }
            )
        }
    }

    private fun setupToolbar() {
        supportActionBar?.hide()
    }
}

@Composable
fun MainActivityScreen(
    mainViewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onItemClick: (MealModel) -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(MainActivity.Tab.MAIN) }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = currentTab.titleRes)
            )
        },
        bottomBar = {
            NavigationBar {
                MainActivity.Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        label = { Text(text = stringResource(id = tab.titleRes)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = stringResource(id = tab.titleRes)
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainActivity.Tab.MAIN -> {
                    MainScreen(
                        viewModel = mainViewModel,
                        onItemClick = onItemClick
                    )
                }
                MainActivity.Tab.FAVORITE -> {
                    FavoriteScreen(
                        viewModel = favoriteViewModel,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}
