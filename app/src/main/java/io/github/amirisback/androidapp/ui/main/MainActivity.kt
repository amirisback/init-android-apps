package io.github.amirisback.androidapp.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.common.base.BaseActivity
import io.github.amirisback.androidapp.databinding.ActivityMainBinding
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.detail.DetailActivity
import io.github.amirisback.androidapp.ui.favorite.FavoriteViewModel
import io.github.amirisback.androidapp.ui.features.favorite.FavoriteScreen
import io.github.amirisback.androidapp.ui.features.main.MainScreen
import io.github.amirisback.androidapp.ui.features.main.MainViewModel
import io.github.amirisback.init.ui.theme.InitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    enum class Tab(val titleRes: Int, val iconRes: Int) {
        MAIN(R.string.title_main, R.drawable.ic_tv),
        FAVORITE(R.string.title_fav, R.drawable.ic_favorite)
    }

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
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
        mainViewModel.searchMeal("Cream") // Trigger search for meals inside MainViewModel

        binding.composeView.setContent {
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
    }

    private fun setupToolbar() {
        supportActionBar?.hide()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScreen(
    mainViewModel: MainViewModel,
    favoriteViewModel: FavoriteViewModel,
    onItemClick: (MealModel) -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(MainActivity.Tab.MAIN) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = currentTab.titleRes)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                MainActivity.Tab.values().forEach { tab ->
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
