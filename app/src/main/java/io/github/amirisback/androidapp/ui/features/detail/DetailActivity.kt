package io.github.amirisback.androidapp.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.frogobox.sdk.ext.getExtraExt
import com.frogobox.sdk.ext.showToast
import com.frogobox.sdk.ext.toJson
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.common.base.BaseActivity
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.features.detail.DetailScreen
import io.github.amirisback.androidapp.ui.theme.InitTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_DATA = "EXTRA_DATA"

        fun createIntent(context: Context, data: MealModel): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_DATA, data.toJson())
            }
        }

        fun launch(context: Context, data: MealModel) {
            context.startActivity(createIntent(context, data))
        }
    }

    private val viewModel: DetailViewModel by viewModels()

    private var isLoadingState by mutableStateOf(false)
    private var isFavoriteState by mutableStateOf(false)

    override fun setupViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mealsState.collect { resource ->
                        when (resource) {
                            is Resource.Error -> {
                                isLoadingState = false
                                showToast(resource.message.toString())
                            }
                            is Resource.Loading -> {
                                isLoadingState = true
                            }
                            is Resource.Success -> {
                                isLoadingState = false
                                resource.data?.let { items ->
                                    if (items.isNotEmpty()) {
                                        isFavoriteState = true
                                    }
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.insertState.collect { resource ->
                        when (resource) {
                            is Resource.Error -> {
                                isLoadingState = false
                                showToast(resource.message.toString())
                            }
                            is Resource.Loading -> {
                                isLoadingState = true
                            }
                            is Resource.Success -> {
                                isLoadingState = false
                                isFavoriteState = true
                                showToast("Berhasil Menambahkan Ke Favorite ${resource.data?.strMeal}")
                            }
                            null -> {}
                        }
                    }
                }

                launch {
                    viewModel.deleteState.collect { resource ->
                        when (resource) {
                            is Resource.Error -> {
                                isLoadingState = false
                                showToast(resource.message.toString())
                            }
                            is Resource.Loading -> {
                                isLoadingState = true
                            }
                            is Resource.Success -> {
                                isLoadingState = false
                                finish()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
        setupToolbar()

        val extra = getExtraExt<MealModel>(EXTRA_DATA)
        if (extra == null) {
            showToast("Data meal tidak ditemukan")
            finish()
            return
        }
        viewModel.mealModel = extra
        viewModel.getData()
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            DetailScreen(
                meal = viewModel.mealModel,
                isLoading = isLoadingState,
                isFavorite = isFavoriteState,
                onBackClick = { finish() },
                onInsertClick = { viewModel.insertToDB() },
                onDeleteClick = { viewModel.removeFromDb() }
            )
        }
    }

    private fun setupToolbar() {
        supportActionBar?.hide()
    }
}