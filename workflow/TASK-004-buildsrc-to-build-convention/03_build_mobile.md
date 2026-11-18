# TASK-004: `:app` Module Migration, `buildSrc/` Removal & Documentation Update

> **Author:** Hudio (`hudio_mobile`)  
> **Phase:** Phase 3/4 — Track C (App Module Migration, `buildSrc/` Removal & Docs)  
> **Date:** 2026-11-18  
> **Status:** COMPLETE (Verified)

---

## 1. Summary of Changes

Migrated `:app` (`app/build.gradle.kts`) from monolithic build configuration + legacy `buildSrc/` constants to declarative convention plugins provided by `build-logic/convention`, cleanly stopped Gradle daemons and deleted `buildSrc/`, updated `README.md`, `AGENTS.md`, and `ARCHITECTURE.md`, and verified full debug + release build parity, JVM unit tests, Android Lint, and Room schema integrity.

---

## 2. Files Modified & Removed

### Modified
| File | Change |
|---|---|
| `app/build.gradle.kts` | Reduced from 158 lines to 37 lines. Replaced boilerplate plugins & Android/Compose/Hilt/Room/Kotlin/signing/resValue/BuildConfig/archivesName config with the 4 convention plugins (`androidapp-android-application`, `androidapp-android-application-compose`, `androidapp-android-hilt`, `androidapp-android-room`) + `kotlinParcelize`. Kept `android { namespace = "io.github.amirisback.androidapp" }` explicit and preserved all app-specific dependencies (`github-chucker` / `github-chucker-no-op`, `frogo-android` with Chucker excludes, `ksp(libs.androidx.lifecycle.compiler)`, `coil-compose`, `coil-network-okhttp`, `junit`, `kotlinx-coroutines-test`) with zero imports of `ProjectSetting` or `ProjectAds`. |
| `README.md` | Documented `build-logic/convention/` in `Architecture Summary` and `Tech Stack`; updated `Getting Started (Template Customization)` paths from `buildSrc/src/main/kotlin/ProjectSetting.kt` & `ProjectAds.kt` to `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt` & `ProjectAds.kt`. |
| `AGENTS.md` | Documented `build-logic/convention` composite build, AGP 9.5.1 built-in Kotlin constraint (never apply `org.jetbrains.kotlin.android`), single-source `gradle/libs.versions.toml` guardrail, and extended zero `@Suppress` / `@SuppressWarnings` checks to cover both `app/src/` and `build-logic/`. |
| `ARCHITECTURE.md` | Added **Build Logic (`build-logic/convention`)** overview in Section 1 listing all 4 convention plugins + `ProjectSetting.kt`/`ProjectAds.kt`, and added corresponding rows in Section 5 ("Where to Put X"). |

### Removed
| Path | Method | Verification |
|---|---|---|
| `buildSrc/` (all files & hidden `.gradle`/`.kotlin`/`build` dirs) | Ran `.\gradlew.bat --stop` first, then `Remove-Item -Recurse -Force buildSrc` | `Test-Path buildSrc` -> `False` |

---

## 3. Verification Evidence

### A. Full Debug Build, Unit Tests & Android Lint
- **Command:** `.\gradlew.bat clean assembleDebug testDebugUnitTest lintDebug --warning-mode=all`
- **Result:** `BUILD SUCCESSFUL` (exit code `0`, `0` deprecation warnings)
- **JVM Unit Tests:** `24` executed, `24` passed, `0` failed, `0` errors, `0` skipped:
  - `JsonExtTest`: 2/2 passed
  - `MealMapperTest`: 3/3 passed
  - `MealRepositoryImplTest`: 6/6 passed
  - `DetailViewModelTest`: 5/5 passed
  - `FavoriteViewModelTest`: 4/4 passed
  - `MainViewModelTest`: 4/4 passed
- **Android Lint (`lint-results-debug.sarif`):** `0` errors, `0` warnings.
- **Debug APK Output:** `app/build/outputs/apk/debug/kick-start-100-18-11-2026_02-19-debug.apk` (matches `kick-start-100-<dd-MM-yyyy_hh-mm>-debug.apk`).

### B. Release `BuildConfig` & `resValues` Parity
- **Command:** `.\gradlew.bat :app:generateReleaseResValues :app:generateReleaseBuildConfig --warning-mode=all`
- **Result:** `BUILD SUCCESSFUL` (exit code `0`, `0` deprecation warnings)

| Artifact | Variant | Verified Generated Values |
|---|---|---|
| `BuildConfig.java` | `debug` | `DEBUG = true`, `APPLICATION_ID = "io.github.amirisback.kickstart"`, `VERSION_CODE = 100`, `VERSION_NAME = "1.0.0"`, `DATABASE_NAME = "kick_start.db"`, `PREF_NAME = "pref_kick_start"` |
| `BuildConfig.java` | `release` | `DEBUG = false`, `APPLICATION_ID = "io.github.amirisback.kickstart"`, `VERSION_CODE = 100`, `VERSION_NAME = "1.0.0"`, `DATABASE_NAME = "kick_start.db"`, `PREF_NAME = "pref_kick_start"` |
| `gradleResValues.xml` | `debug` | `app_name = "Kick Start DEV"`, `admob_app_id = "ca-app-pub-3940256099942544~3347511713"`, `admob_banner = "ca-app-pub-3940256099942544/6300978111"`, `admob_interstitial_id = "ca-app-pub-3940256099942544/1033173712"` |
| `gradleResValues.xml` | `release` | `app_name = "Kick Start"`, `admob_app_id = ""`, `admob_banner = ""`, `admob_interstitial_id = ""` |

### C. Static & Structural Checks
- `Test-Path buildSrc` -> `False`
- Active repo references to `buildSrc` (excluding `workflow/`, `.git/`, `.idea/`, `.gradle/`, `build/`) -> **`0` matches**
- `@Suppress` / `@SuppressWarnings` scan across `app/src/` and `build-logic/` -> **`0` matches**
- Room schema JSON (`app/schemas/io.github.amirisback.androidapp.data.local.database.AppDatabase/1.json`) -> exists and unmodified (`git status --short app/schemas` clean).
