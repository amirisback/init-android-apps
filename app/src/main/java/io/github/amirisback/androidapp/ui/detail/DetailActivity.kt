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
import com.frogobox.sdk.ext.getExtraExt
import com.frogobox.sdk.ext.showToast
import com.frogobox.sdk.ext.toJson
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.common.base.BaseActivity
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.ui.features.detail.DetailScreen
import io.github.amirisback.init.ui.theme.InitTheme

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
        viewModel.mealsState.observe(this) {
            when (it) {
                is Resource.Error -> {
                    isLoadingState = false
                    showToast(it.message.toString())
                }

                is Resource.Loading -> {
                    isLoadingState = true
                }

                is Resource.Success -> {
                    isLoadingState = false
                    it.data?.let { items ->
                        if (items.isNotEmpty()) {
                            isFavoriteState = true
                        }
                    }
                }
            }
        }

        viewModel.insertState.observe(this) {
            when (it) {
                is Resource.Error -> {
                    isLoadingState = false
                    showToast(it.message.toString())
                }

                is Resource.Loading -> {
                    isLoadingState = true
                }

                is Resource.Success -> {
                    isLoadingState = false
                    isFavoriteState = true
                    showToast("Berhasil Menambahkan Ke Favorite ${it.data?.strMeal}")
                }
            }
        }

        viewModel.deleteState.observe(this) {
            when (it) {
                is Resource.Error -> {
                    isLoadingState = false
                    showToast(it.message.toString())
                }

                is Resource.Loading -> {
                    isLoadingState = true
                }

                is Resource.Success -> {
                    isLoadingState = false
                    finish()
                }
            }
        }
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
        setupToolbar()

        val extra = getExtraExt<MealModel>(EXTRA_DATA)
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