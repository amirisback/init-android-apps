package io.github.amirisback.androidapp.ui.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import io.github.amirisback.androidapp.common.base.BaseActivity
import io.github.amirisback.androidapp.ui.features.about.AboutUsScreen
import io.github.amirisback.init.ui.theme.InitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutUsActivity : BaseActivity() {

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, AboutUsActivity::class.java)
        }

        fun launch(context: Context) {
            context.startActivity(createIntent(context))
        }

    }

    override fun setupViewModel() {}

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
        setupToolbar()
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            AboutUsScreen(
                onBackClick = { finish() }
            )
        }
    }

    private fun setupToolbar() {
        supportActionBar?.hide()
    }

}
