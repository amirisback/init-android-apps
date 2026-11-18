package io.github.amirisback.androidapp.feature.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.frogobox.compose.view.FrogoComposeActivity
import com.frogobox.sdk.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.core.common.fromJson
import io.github.amirisback.androidapp.core.common.toJson
import io.github.amirisback.androidapp.core.ui.theme.InitTheme
import io.github.amirisback.androidapp.domain.model.Meal

@AndroidEntryPoint
class DetailActivity : FrogoComposeActivity() {

    companion object {
        const val EXTRA_DATA = "EXTRA_DATA"

        fun createIntent(context: Context, data: Meal): Intent =
            Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_DATA, data.toJson())
            }

        fun launch(context: Context, data: Meal) {
            context.startActivity(createIntent(context, data))
        }
    }

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            val meal = fromJson<Meal>(intent.getStringExtra(EXTRA_DATA))
            if (meal == null) {
                showToast(getString(R.string.message_meal_not_found))
                finish()
                return
            }
            viewModel.setMeal(meal)
        }
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            DetailRoute(
                viewModel = viewModel,
                onBackClick = { finish() },
                onDeleted = { finish() },
                onShowMessage = { message -> showToast(message) },
            )
        }
    }
}
