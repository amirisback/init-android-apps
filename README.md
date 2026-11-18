![ScreenShoot Apps](docs/image/ss_banner.png?raw=true)

# init-android-apps

[![Android-CI](https://github.com/frogobox/frogo-kick-start-android/actions/workflows/android-ci.yml/badge.svg)](https://github.com/frogobox/frogo-kick-start-android/actions/workflows/android-ci.yml)

Modern, Google-standard, AI-ready **dual-purpose** Android starter template built with **Jetpack Compose Material 3**, **Clean Architecture**, **Unidirectional Data Flow (UDF)**, **Hilt**, **Room**, **DataStore**, and **Retrofit** — supporting both **standard Android apps** and **2D casual / puzzle / board games** (including native Kotlin rewrites of Unity 2D games).

- **Architecture Deep Dive:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **AI Agent Guidelines:** [AGENTS.md](AGENTS.md)
- **Privacy Policy:** [PRIVACY-POLICY.md](PRIVACY-POLICY.md)
- **License:** [LICENSE](LICENSE)

---

## Architecture Summary

The `:app` module (`io.github.amirisback.androidapp`) is organized into four clean layers aligned with Google's Official Guide to App Architecture:

- **`build-logic/convention/`** — Included Gradle composite build housing shared convention plugins (`androidapp.android.application`, `androidapp.android.application.compose`, `androidapp.android.hilt`, `androidapp.android.room`) and centralized app metadata (`ProjectSetting.kt`, `ProjectAds.kt`) backed by `gradle/libs.versions.toml`.
- **`core/`** — Shared utilities (`core/common/`), Hilt DI modules (`core/di/`), Material 3 theme & reusable Compose components (`core/ui/`), and an opt-in, decoupled **2D game engine core** (`core/game/`: fixed-timestep loop, `GameCanvas` + `GameSurfaceView` renderers, `SoundPool` + BGM audio manager, `ImmersiveModeEffect`, DataStore save store, and AdMob Rewarded controller).
- **`data/`** — Retrofit API service & DTOs (`data/remote/`), Room `AppDatabase` / DAO / entities (`data/local/`), bidirectional mappers (`data/mapper/`), and `MealRepositoryImpl` (`data/repository/`).
- **`domain/`** — 100% pure Kotlin immutable domain models (`domain/model/`), repository contracts (`domain/repository/`), and use cases (`domain/usecase/`) with zero Android or network framework imports.
- **`feature/`** — Standard app features (`main`, `favorite`, `detail`, `about`) plus a minimal opt-in game starter reference (`game`), all following UDF (`*UiState` + `ViewModel` + `<Feature>Route` + stateless `<Feature>Screen` with `@Preview`).

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full package map, import boundary table, and step-by-step feature checklist.

---

## Dual-Purpose Starter Modes

Standard app features and the 2D game track are strictly decoupled and share only `ProjectApplication.kt`, `core/common/`, `core/di/DispatcherModule.kt`, `core/ui/theme/`, and `build-logic/convention/`:

- **Default / Hybrid Mode:** `MainActivity` remains the default launcher with the Meal sample intact. `core/game/` and `feature/game/` stay opt-in with zero startup overhead (`MobileAdsInitProvider` is removed via `tools:node="remove"` so non-game apps never initialize AdMob at launch).
- **Standard App Only (Prune Game Track):** Delete `core/game/`, `feature/game/`, `core/di/GameModule.kt`, their unit tests, and the `.feature.game.GameActivity` manifest entry (optionally drop `google.play.services.ads` / `androidx.datastore.preferences`).
- **2D Casual / Puzzle / Board Game Only (Prune Meal Track):** Delete `data/`, `domain/`, `feature/{main,detail,favorite,about}/`, `core/ui/component/MealCard.kt`, `core/di/{Database,Network,Repository,UseCase}Module.kt`, and promote `.feature.game.GameActivity` to `MAIN` / `LAUNCHER` in `AndroidManifest.xml`.

### Unity 2D Rewrite Mapping (`core/game/`)

| Unity 2D Concept | Native Kotlin Equivalent |
|---|---|
| `FixedUpdate(dt)` | `FixedTimestepClock` + `GameCanvas` / `GameSurfaceHost` |
| `Time.timeScale = 0` / pause | `GameLoopController` (`IDLE` / `RUNNING` / `PAUSED`) |
| `PlayerPrefs` / local save | `GameSaveStore` (`DataStoreGameSaveStore` with JSON slot) |
| `AudioSource` (SFX + BGM) | `GameAudioController` (`SoundPool` + `MediaPlayer` + `GameAudioLifecycleEffect`) |
| AdMob Rewarded Ads | `RewardedAdController` (`AdMobRewardedAdController`, IDs in `ProjectAds.kt`) |
| Fullscreen + `Screen.sleepTimeout` | `ImmersiveModeEffect(enabled = true, keepScreenOn = true)` |

See [AGENTS.md](AGENTS.md) and [ARCHITECTURE.md](ARCHITECTURE.md) for the exact file-by-file pruning checklists.

---

## Tech Stack

| Category | Technology & Version |
|---|---|
| **SDK & JVM** | `minSdk 24` · `compileSdk 37` · `targetSdk 37` · JVM `17` |
| **Build & Compiler** | AGP `9.5.1` · Kotlin `2.4.21` · KSP `2.3.14` · Gradle `9.9.1` · Convention Plugins (`build-logic/convention`) |
| **UI & Design** | Jetpack Compose (BOM `2026.09.00`) · Material 3 · Edge-to-Edge |
| **Dependency Injection** | Hilt `2.60.1` |
| **Persistence** | Room `2.8.5` (KSP schema export) · DataStore Preferences `1.3.0` |
| **Networking** | Retrofit · OkHttp · Gson · Chucker `4.4.0` |
| **Image Loading** | Coil `3.6.3` (`coil-compose`, `coil-network-okhttp`) |
| **2D Game Core (Opt-In)** | Fixed-Timestep Loop · Compose `Canvas` / `SurfaceView` · `SoundPool` + `MediaPlayer` · `WindowInsetsControllerCompat` · Google Mobile Ads `25.5.0` |
| **Async & State** | Kotlin Coroutines · `Flow` · `StateFlow` (`collectAsStateWithLifecycle`) |
| **Android Utilities** | Frogo SDK `4.0.0` |
| **Testing (JVM)** | JUnit `4.13.2` · `kotlinx-coroutines-test` `1.10.2` |

---

## Getting Started (Template Customization)

1. Update application metadata (`NAME_APP`, `APP_DOMAIN`, `APP_PLAY_CONSOLE`, `ADMOB_APP_ID`, version code/name, database & preference names) in `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt`.
2. Update AdMob debug and release unit IDs (`BANNER_ID`, `INTERSTITIAL_ID`, `REWARDED_ID`) in `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectAds.kt`.
3. Update `namespace` in `app/build.gradle.kts` and package directories if renaming the root package.
4. Pick your starter mode (Hybrid, Standard App Only, or 2D Game Only) and follow the pruning guide in [ARCHITECTURE.md](ARCHITECTURE.md) and [AGENTS.md](AGENTS.md).

---

## Build & Verification Commands

```bash
# Assemble debug APK & export Room schema via KSP
./gradlew assembleDebug

# Run JVM unit tests
./gradlew testDebugUnitTest

# Run Android Lint verification
./gradlew lintDebug
```

---

## Screenshots

| UI 1 | UI 2 | UI 3 |
|:---:|:---:|:---:|
| <img width="200px" height="360px" src="docs/image/ss_1.png"> | <img width="200px" height="360px" src="docs/image/ss_2.png"> | <img width="200px" height="360px" src="docs/image/ss_3.png"> |

---

## Contributors

- **Muhammad Faisal Amir** (`faisalamircs@gmail.com`)

![MAD Score](docs/image/mad_score.png?raw=true)
