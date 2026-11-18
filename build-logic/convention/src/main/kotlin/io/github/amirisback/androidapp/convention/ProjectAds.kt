package io.github.amirisback.androidapp.convention

object ProjectAds {

    object Admob {

        // AdMob IDs for release builds
        const val APP_ID = ProjectSetting.ADMOB_APP_ID
        const val BANNER_ID = ""
        const val INTERSTITIAL_ID = ""
        const val REWARDED_ID = ""

        // AdMob test IDs for debug builds - Do Not Change
        object Debug {
            const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
            const val BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
            const val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
            const val REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
        }
    }
}
