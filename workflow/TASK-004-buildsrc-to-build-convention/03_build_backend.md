# TASK-004: Build Logic Infrastructure Implementation (`build-logic/convention`)

> **Author:** Bryan (`bryan_backend_devops`)  
> **Phase:** Phase 3 — Track B (Build Infrastructure)  
> **Date:** 2026-11-18  
> **Status:** COMPLETE (Verified)

---

## 1. Summary of Changes

Implemented the `build-logic` Gradle composite build and `:convention` subproject per the frozen Phase 2 contract in `01_prd_faisal.md`, migrated `ProjectSetting` and `ProjectAds` with 100% value parity, registered all 4 convention plugins in `gradle/libs.versions.toml`, wired `includeBuild("build-logic")` in root `settings.gradle.kts`, and added `.kotlin` to root `.gitignore`.

No changes were made to `app/build.gradle.kts`, `buildSrc/`, or project documentation (owned by Hudio in Track C).

---

## 2. Files Created

| File | Purpose |
|---|---|
| `build-logic/settings.gradle.kts` | Configures `pluginManagement`, `dependencyResolutionManagement` (`FAIL_ON_PROJECT_REPOS`), imports `../gradle/libs.versions.toml` as `libs`, sets `rootProject.name = "build-logic"`, and includes `:convention`. |
| `build-logic/gradle.properties` | Enables `org.gradle.parallel=true` and `org.gradle.caching=true` (omitting `configureondemand`). |
| `build-logic/convention/build.gradle.kts` | Applies `kotlin-dsl`, sets JVM 17 (`JavaVersion.VERSION_17` + `JvmTarget.JVM_17` without `java.toolchain`), declares lean 3 `compileOnly` deps (`android-gradleApiPlugin`, `kotlin-gradlePlugin`, `ksp-gradlePlugin`), enables strict `validatePlugins` (`enableStricterValidation = true`, `failOnWarning = true`), and registers the 4 convention plugins using `libs.plugins.*` IDs. |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt` | Public object preserving all constant and computed values (`PROJECT_MIN_SDK`, `PROJECT_COMPILE_SDK`, `PROJECT_TARGET_SDK`, `PROJECT_APP_ID`, `PROJECT_VERSION_CODE`, `PROJECT_VERSION_NAME`, `NAME_APP`, `NAME_APP_DEBUG`, `NAME_APK`, `DB`, `PREF`, `PLAYSTORE_*`) without stale unrelated headers. |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectAds.kt` | Public object preserving all release and debug `ProjectAds.Admob` IDs cleanly. |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectExtensions.kt` | Internal `Project.libs` extension backed by `VersionCatalogsExtension`. |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/AndroidApplicationConventionPlugin.kt` | Applies `com.android.application` (never `org.jetbrains.kotlin.android`), configures `ApplicationExtension`, `KotlinAndroidProjectExtension` (AGP 9.5.1 built-in Kotlin), and `BasePluginExtension` (`archivesName` with `LocalDateTime` + `DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm")`). |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/AndroidApplicationComposeConventionPlugin.kt` | Applies `org.jetbrains.kotlin.plugin.compose`, sets `buildFeatures.compose = true`, and wires Compose BOM + Compose UI/M3/Foundation/Activity/Lifecycle/Tooling dependencies. |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/AndroidHiltConventionPlugin.kt` | Applies `com.google.devtools.ksp` and `com.google.dagger.hilt.android`, and wires `implementation(hilt)` + `ksp(hilt-compiler)`. |
| `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/AndroidRoomConventionPlugin.kt` | Applies `com.google.devtools.ksp`, sets `KspExtension` `arg("room.schemaLocation", "$projectDir/schemas")`, and wires `ksp(androidx-room-compiler)`. |

---

## 3. Files Modified

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | Added 3 `build-logic` `[libraries]` entries (`android-gradleApiPlugin`, `kotlin-gradlePlugin`, `ksp-gradlePlugin`) and 4 versionless convention plugin entries under `[plugins]` (`androidapp-android-application`, `androidapp-android-application-compose`, `androidapp-android-hilt`, `androidapp-android-room`). Retained existing `android-library` & `android-dynamic-feature` entries untouched. |
| `settings.gradle.kts` | Added `includeBuild("build-logic")` inside `pluginManagement`. |
| `.gitignore` | Added `.kotlin` to keep `build-logic/.kotlin` untracked. |

---

## 4. Implementation Notes

- **Version Catalog Prefix Accessor:** Because `androidapp-android-application` is a prefix of `androidapp-android-application-compose`, `build-logic/convention/build.gradle.kts` resolves the base plugin ID via `libs.plugins.androidapp.android.application.asProvider().get().pluginId`. In module `plugins { ... }` blocks (`app/build.gradle.kts`), `alias(libs.plugins.androidapp.android.application)` works directly via `ProviderConvertible<PluginDependency>`.
- **AGP 9.5.1 Built-in Kotlin:** `AndroidApplicationConventionPlugin` applies only `"com.android.application"` and configures `KotlinAndroidProjectExtension` directly (verified against AGP 9.5.1 bytecode).

---

## 5. Verification Evidence

1. **Gradle Compilation & Strict Plugin Validation:**
   - Command: `.\gradlew.bat -p build-logic check --warning-mode=all`
   - Result: `BUILD SUCCESSFUL` (exit code `0`)
   - Executed tasks: `:convention:checkKotlinCompilerVersion`, `:convention:pluginDescriptors`, `:convention:processResources`, `:convention:compileKotlin`, `:convention:classes`, `:convention:jar`, `:convention:pluginUnderTestMetadata`, `:convention:validatePlugins`, `:convention:check`
   - Warnings: `0` deprecation warnings; `validatePlugins` passed with `enableStricterValidation = true` and `failOnWarning = true`.
2. **Static Suppressions Scan:**
   - Checked `build-logic/**/*.kt` and `build-logic/**/*.kts` for `@Suppress` / `@SuppressWarnings` -> **0 matches**.
3. **Git Ignore Verification:**
   - `git status --short --untracked-files=all build-logic/` reports only the 10 expected source/build files (`build-logic/.gradle`, `build-logic/.kotlin`, and `build-logic/convention/build` are all cleanly ignored).
