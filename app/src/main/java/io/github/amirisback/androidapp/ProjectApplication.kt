package io.github.amirisback.androidapp

import android.content.Context
import com.frogobox.sdk.FrogoApplication
import dagger.hilt.android.HiltAndroidApp
import io.github.amirisback.androidapp.core.common.appIsDebug

@HiltAndroidApp
class ProjectApplication : FrogoApplication() {

    companion object {
        val TAG: String = ProjectApplication::class.java.simpleName

        lateinit var instance: ProjectApplication
        fun getContext(): Context = instance.applicationContext
    }

    override fun isDebugMode(): Boolean = appIsDebug

    override fun onCreateExt() {
        super.onCreateExt()
        instance = this
    }
}
