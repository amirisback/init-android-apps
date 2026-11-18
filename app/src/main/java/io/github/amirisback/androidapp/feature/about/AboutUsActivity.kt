package io.github.amirisback.androidapp.feature.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.frogobox.compose.view.FrogoComposeActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.core.ui.theme.InitTheme

@AndroidEntryPoint
class AboutUsActivity : FrogoComposeActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, AboutUsActivity::class.java)

        fun launch(context: Context) {
            context.startActivity(createIntent(context))
        }
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            AboutUsScreen(
                onBackClick = { finish() },
            )
        }
    }
}
