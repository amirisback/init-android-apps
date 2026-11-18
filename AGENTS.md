# Agent Guidelines — `init-android-apps`

- **Project:** `init-android-apps` (Google-standard single-module Android starter template)
- **Module / Namespace:** `:app` (`io.github.amirisback.androidapp`) + included composite build `build-logic/convention` (`io.github.amirisback.androidapp.convention`)
- **Toolchain & Stack:** AGP `9.5.1` (built-in Kotlin), Kotlin `2.4.21`, KSP `2.3.14`, Gradle `9.9.1`, JVM `17`, `minSdk 24` / `compileSdk 37` / `targetSdk 37`
- **Core Libraries:** Jetpack Compose M3 (BOM `2026.09.00`), Hilt `2.60.1`, Room `2.8.5`, DataStore Preferences `1.3.0`, Google Mobile Ads `25.5.0`, Retrofit + OkHttp + Gson, Coil `3.6.3`, Chucker `4.4.0`, Frogo SDK `4.0.0`, JUnit `4.13.2`, `kotlinx-coroutines-test` `1.10.2`

> **Mandatory First Step:** Read [`ARCHITECTURE.md`](ARCHITECTURE.md) before creating, moving, or refactoring any source file.

## Layer Dependency & Import Guardrails

| Layer | Allowed Dependencies | Forbidden Dependencies |
|---|---|---|
| `domain/` | Kotlin stdlib, `kotlinx.coroutines`, `javax.inject`, `core/common/Resource` | `android.*`, `androidx.*`, `retrofit2.*`, `com.google.gson.*`, `okhttp3.*`, `data/`, `feature/`, `core/game/` |
| `data/` | `domain/`, `core/common/`, `core/di/` qualifiers (`@IoDispatcher`), Room, Retrofit, Gson, OkHttp, Hilt | `feature/`, Compose UI |
| `feature/` | `domain/`, `core/ui/`, `core/game/`, `core/common/`, Compose M3, Lifecycle, Hilt | `data/` (no DAO, Entity, DTO, API service, or `*RepositoryImpl`) |
| `core/game/` | Kotlin stdlib, `kotlinx.coroutines`, `core/di/` (`@IoDispatcher`), Compose, Lifecycle, DataStore, Google Mobile Ads, Android media/window APIs, Hilt | `data/`, `feature/` |
| `core/di/` | Wires `data/` & `core/game/` implementations to `domain/` & `core/game/` interfaces via Hilt | `feature/` |
| `core/ui/` | Compose M3, Coil 3, `domain/model/` (shared display components such as `MealCard`) | `data/`, feature ViewModels |

## Dual-Purpose Starter (Standard App + Opt-In 2D Game Core)

Both tracks are strictly decoupled and share only `ProjectApplication.kt`, `core/common/`, `core/di/DispatcherModule.kt`, `core/ui/theme/`, and `build-logic/convention/`. Choose one of three modes when cloning this template:

- **Default / Hybrid Mode:** `feature/main/MainActivity` is the default launcher (Meal sample intact); `core/game/` and `feature/game/` stay opt-in with zero startup overhead (`MobileAdsInitProvider` is removed via `tools:node="remove"`).
- **Mode A — Standard App Only (Prune Game Track):**
  - Delete `app/src/main/java/.../core/game/`, `feature/game/`, and `core/di/GameModule.kt`.
  - Delete `app/src/test/java/.../core/game/`, `feature/game/`, `testutil/FakeGame*.kt`, and `testutil/FakeRewardedAdController.kt`.
  - Remove `.feature.game.GameActivity` (and `MobileAdsInitProvider` removal if dropping AdMob) from `AndroidManifest.xml`, plus `game_*` entries in `res/values/strings.xml`.
  - Optionally remove `google.play.services.ads` (and `androidx.datastore.preferences` if unused) from `app/build.gradle.kts`.
- **Mode B — 2D Casual / Puzzle / Board Game Only (Prune Meal App Track):**
  - Delete `app/src/main/java/.../data/`, `domain/`, `feature/main/`, `feature/detail/`, `feature/favorite/`, `feature/about/`, `core/ui/component/MealCard.kt`, and `core/di/{Database,Network,Repository,UseCase}Module.kt` (keep `DispatcherModule.kt` and `GameModule.kt`).
  - Delete `app/src/test/java/.../data/`, `feature/{main,detail,favorite}/`, and `testutil/FakeMeal*.kt`.
  - In `AndroidManifest.xml`, remove `MainActivity`, `DetailActivity`, `AboutUsActivity`, and promote `.feature.game.GameActivity` to `android:exported="true"` with `MAIN` / `LAUNCHER` intent-filter.
  - If offline-only, optionally remove `androidapp.android.room`, `chucker`, `coil`, and `app/schemas/` from `app/build.gradle.kts`.
  - Unity 2D REA rewrite mapping: `FixedUpdate` -> `FixedTimestepClock`/`onFixedUpdate`, `Time.timeScale = 0` -> `GameLoopController`, `PlayerPrefs` -> `GameSaveStore`, `AudioSource` -> `GameAudioController`, AdMob Rewarded -> `RewardedAdController`, fullscreen/wake -> `ImmersiveModeEffect`.

## Non-Negotiable Coding Rules

- Maintain a strict 1:1 match between physical directories and Kotlin `package` declarations in `app/src/main/java` and `app/src/test/java`.
- Follow Unidirectional Data Flow (UDF) in all stateful features: `ViewModel` exposes a single `StateFlow<*UiState>` (never raw `Resource<T>`) collected via `collectAsStateWithLifecycle()` in `<Feature>Route`, delegating to a stateless, previewable `<Feature>Screen(uiState, onEvent)`.
- **Zero warning suppression:** Never introduce `@Suppress` or `@SuppressWarnings` in `app/src/` or `build-logic/`. Always migrate deprecated APIs to official modern replacements.
- **Build logic & version catalog:** Keep reusable Gradle build configuration and app metadata (`ProjectSetting.kt`, `ProjectAds.kt`) inside the `build-logic/convention` composite build, sourcing all versions strictly from root `gradle/libs.versions.toml`. Never apply `org.jetbrains.kotlin.android` (AGP 9.5.1 uses built-in Kotlin).
- Never hardcode `Dispatchers.IO` inside data sources; inject `@IoDispatcher` from `core/di/DispatcherModule.kt` and always rethrow `CancellationException`.
- Keep Activities inheriting directly from `FrogoComposeActivity` (with `enableEdgeToEdge()`) and ViewModels from `androidx.lifecycle.ViewModel`. Do not reintroduce empty base wrappers, XML layouts, or wrong-project copyright banners.
- Every new mapper, repository, or ViewModel must include JVM unit tests under `app/src/test/` using hand-written fakes and `kotlinx-coroutines-test`.

## Verification Commands

Run all three verification tasks before completing any code change:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Static checks to confirm on every change:
- Zero forbidden imports (`android.*`, `androidx.*`, `retrofit2.*`, `com.google.gson.*`, `okhttp3.*`, `data.*`, `feature.*`, `core.game.*`) inside `domain/`.
- Zero `@Suppress` or `@SuppressWarnings` annotations under `app/src/` and `build-logic/`.
- Exported Room schema JSON matches `app/schemas/io.github.amirisback.androidapp.data.local.database.AppDatabase/<version>.json`.

## Relevant Android Skills

Consult the matching Android skill (`SKILL.md`) before working in its domain:
- **`frogo-sdk`**: `FrogoComposeActivity`, Frogo SDK v4.0.0 utilities and conventions.
- **`edge-to-edge`**: `enableEdgeToEdge()`, `Scaffold` `innerPadding`, and window inset handling.
- **`jetpack-compose-m3`**: Material Design 3 components, typography, color roles, and `@Preview` hygiene.
- **`kotlin-coroutines-expert`**: Structured concurrency, `StateFlow`, `CancellationException`, and dispatcher injection.
- **`android-cli`**: Device deployment, emulator control, and environment diagnostics.
- **`r8-analyzer`**: ProGuard/R8 shrinking rules (e.g., Gson DTO preservation).
