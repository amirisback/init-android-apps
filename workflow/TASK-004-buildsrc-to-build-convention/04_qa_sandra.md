# TASK-004: QA & Security Gate Report (`buildsrc-to-build-convention`)

> **Author:** Sandra (`sandra_qa`) — QA & Security Gatekeeper  
> **Phase:** Phase 5 — Independent QA Verification & Security Gate  
> **Date:** 2026-11-18  
> **Workspace:** `d:\AndroidProject\init-android-apps`  
> **Quality Gate Verdict:** **APPROVED** (8/8 ACs PASS · 0 Blocking Issues · 1 Pre-Existing Non-Blocking MEDIUM Security Advisory)

---

## 1. Executive Summary

Independent end-to-end verification confirms that migrating `buildSrc/` to the Gradle composite build `build-logic/convention` meets every frozen acceptance criterion (`AC-01` through `AC-08`) with **100% byte-for-byte build artifact parity**, **0 deprecation or compiler warnings** under `--warning-mode=all`, **strict `validatePlugins` passing**, **24/24 JVM unit tests passing**, **0 Android Lint errors**, **Room schema SHA256 parity**, and **complete removal of `buildSrc/`**.

No source files in `app/src/`, `app/src/main/AndroidManifest.xml`, or `app/proguard-rules.pro` were altered.

---

## 2. Acceptance Criteria Verification Matrix (`AC-01`–`AC-08`)

| AC | Requirement Summary | Status | Key Verified Evidence | Proof Artifact |
|---|---|---|---|---|
| **AC-01** | Composite build `build-logic` setup, shared `libs.versions.toml`, `gradle.properties`, `.gitignore` `.kotlin` | **PASS** | `settings.gradle.kts:2` declares `includeBuild("build-logic")` inside `pluginManagement`; `build-logic/settings.gradle.kts` imports `../gradle/libs.versions.toml` as `libs` and includes `:convention`; `build-logic/gradle.properties` sets `org.gradle.parallel=true` & `org.gradle.caching=true` (no `configureondemand`); `.gitignore:4` adds `.kotlin`; **0** hardcoded version literals across all 10 `build-logic/` source files. | [ac01_settings_and_catalog.txt](proof/ac01_settings_and_catalog.txt) |
| **AC-02** | `ProjectSetting` & `ProjectAds` migration with 100% value parity & clean headers | **PASS** | Public objects under `io.github.amirisback.androidapp.convention` match every frozen constant (`minSdk=24`, `compile/targetSdk=37`, `PROJECT_APP_ID="io.github.amirisback.kickstart"`, `VERSION_CODE=100`, `VERSION_NAME="1.0.0"`, `NAME_APP="Kick Start"`, `NAME_APP_DEBUG="Kick Start DEV"`, `NAME_APK="kick-start"`, `DB="\"kick_start.db\""`, `PREF="\"pref_kick_start\""`, `PLAYSTORE_*`, `ProjectAds.Admob.*` release & `Debug`). **0** `FrogoRecyclerView` stale headers in `build-logic/`. | [ac07_static_security_checks.txt](proof/ac07_static_security_checks.txt), [ac04_buildconfig_debug_diff.txt](proof/ac04_buildconfig_debug_diff.txt) |
| **AC-03** | Exactly 4 convention plugins registered + lean 3 `compileOnly` deps + strict `validatePlugins` | **PASS** | Registered in `build-logic/convention/build.gradle.kts` and `[plugins]` in `gradle/libs.versions.toml`: `androidapp.android.application`, `androidapp.android.application.compose`, `androidapp.android.hilt`, `androidapp.android.room`. Exact 3 `compileOnly` deps (`android-gradleApiPlugin`, `kotlin-gradlePlugin`, `ksp-gradlePlugin`); 0 speculative plugins; `.\gradlew.bat -p build-logic check --rerun-tasks --warning-mode=all` passed (`validatePlugins` with `enableStricterValidation = true`, `failOnWarning = true`). | [ac03_build_logic_check.log](proof/ac03_build_logic_check.log) |
| **AC-04** | Declarative `app/build.gradle.kts` with 100% debug & release build parity | **PASS** | `app/build.gradle.kts` reduced to 37 lines, explicit `namespace = "io.github.amirisback.androidapp"`, **0** `ProjectSetting`/`ProjectAds` imports, all app deps & Chucker excludes preserved. Debug `BuildConfig.java` and `gradleResValues.xml` have **exact SHA256 byte-for-byte parity** vs pre-migration baselines. Release `BuildConfig.java` (`DEBUG = false`, `APPLICATION_ID = "io.github.amirisback.kickstart"`) and release `gradleResValues.xml` (`app_name = "Kick Start"`, release AdMob IDs `""`) verified. Debug APK named `kick-start-100-18-11-2026_02-31-debug.apk`. | [ac04_buildconfig_debug_diff.txt](proof/ac04_buildconfig_debug_diff.txt), [ac04_resvalues_debug_diff.txt](proof/ac04_resvalues_debug_diff.txt), [ac04_release_parity.txt](proof/ac04_release_parity.txt), [ac04_apk_output.txt](proof/ac04_apk_output.txt), [ac04_release_gradle.log](proof/ac04_release_gradle.log) |
| **AC-05** | Complete `buildSrc/` removal & 0 stale active references | **PASS** | `Test-Path buildSrc` -> `False` (no residual `.gradle/`, `.kotlin/`, or `build/` dirs). Repo-wide search (excluding `workflow/`, `.git/`, `.idea/`, `.gradle/`, `.kotlin/`, `build/`) returns **0** `buildSrc` matches. `git status --short --untracked-files=all` confirms zero untracked `.kotlin/` or build-cache pollution. | [ac05_buildsrc_removal_check.txt](proof/ac05_buildsrc_removal_check.txt) |
| **AC-06** | Documentation (`README.md`, `AGENTS.md`, `ARCHITECTURE.md`) updated | **PASS** | `README.md:49-50` points template customization directly to `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt` & `ProjectAds.kt`. `AGENTS.md` documents `build-logic/convention`, AGP 9.5.1 built-in Kotlin constraint (never apply `org.jetbrains.kotlin.android`), single-source `gradle/libs.versions.toml`, and `@Suppress` check across `app/src/` & `build-logic/`. `ARCHITECTURE.md` documents all 4 convention plugins in Sections 1 & 5. | Direct audit of `README.md`, `AGENTS.md`, `ARCHITECTURE.md` |
| **AC-07** | Zero `@Suppress` / `@SuppressWarnings` & modern Gradle 9.9.1 / AGP 9.5.1 / Kotlin 2.4.21 APIs | **PASS** | **0** `@Suppress` / `@SuppressWarnings` across `app/src/` and `build-logic/`. `build-logic/convention` targets `ApplicationExtension` directly, never applies `org.jetbrains.kotlin.android`, uses `JavaVersion.VERSION_17` + `JvmTarget.JVM_17` (no `java.toolchain`), uses `java.time.LocalDateTime` + `DateTimeFormatter` instead of `SimpleDateFormat`. **0** deprecation or compiler warnings across all 3 `--warning-mode=all` Gradle logs. | [ac07_static_security_checks.txt](proof/ac07_static_security_checks.txt), [ac07_deprecation_check.txt](proof/ac07_deprecation_check.txt) |
| **AC-08** | Clean full build, 24/24 JVM unit tests, Room schema parity, Android Lint `0` errors | **PASS** | `.\gradlew.bat clean assembleDebug testDebugUnitTest lintDebug --warning-mode=all` exit code `0`; `.\gradlew.bat :app:generateReleaseResValues :app:generateReleaseBuildConfig --warning-mode=all` exit code `0`; **24/24** JUnit tests pass (`0` fail, `0` error, `0` skip); Room schema `AppDatabase/1.json` SHA256 matches baseline identically; Android Lint reports **0 errors** (11 pre-existing template warnings, 0 new); `domain/` has **0** forbidden imports. | [ac08_gradle_build.log](proof/ac08_gradle_build.log), [ac08_junit_summary.txt](proof/ac08_junit_summary.txt), [ac08_room_schema_sha256.txt](proof/ac08_room_schema_sha256.txt), [ac08_lint_summary.txt](proof/ac08_lint_summary.txt) |

---

## 3. Gradle Execution Runs

| Command | Exit Code | Deprecation Warnings (`--warning-mode=all`) | Log Artifact |
|---|---|---|---|
| `.\gradlew.bat -p build-logic check --rerun-tasks --warning-mode=all` | `0` | `0` | [ac03_build_logic_check.log](proof/ac03_build_logic_check.log) |
| `.\gradlew.bat clean assembleDebug testDebugUnitTest lintDebug --warning-mode=all` | `0` | `0` | [ac08_gradle_build.log](proof/ac08_gradle_build.log) |
| `.\gradlew.bat :app:generateReleaseResValues :app:generateReleaseBuildConfig --warning-mode=all` | `0` | `0` | [ac04_release_gradle.log](proof/ac04_release_gradle.log) |

---

## 4. Artifact Parity, Test & Lint Details

### A. Byte-for-Byte Parity Checksums
| Artifact | Baseline SHA256 | Post-Migration SHA256 | Parity |
|---|---|---|---|
| Debug `BuildConfig.java` | `7B3D48AC64B505234A682B7E9B8B722901E6D69C83D36C75A487A52E868DFD1C` | `7B3D48AC64B505234A682B7E9B8B722901E6D69C83D36C75A487A52E868DFD1C` | **EXACT MATCH** |
| Debug `gradleResValues.xml` | `0DB32B8CCB32FFD76A2F4DA48CF7F8DA96052C460083AF381A045EF518A90F84` | `0DB32B8CCB32FFD76A2F4DA48CF7F8DA96052C460083AF381A045EF518A90F84` | **EXACT MATCH** |
| Room Schema `AppDatabase/1.json` | `8F3D12ACACBAAD3FB503603BA1BEF4630FDBC6CB2FF011C231369A70FC7F1BAE` | `8F3D12ACACBAAD3FB503603BA1BEF4630FDBC6CB2FF011C231369A70FC7F1BAE` | **EXACT MATCH** |

### B. JVM Unit Test Results (`24/24` Passed)
| Test Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `io.github.amirisback.androidapp.core.common.JsonExtTest` | 2 | 0 | 0 | 0 |
| `io.github.amirisback.androidapp.data.mapper.MealMapperTest` | 3 | 0 | 0 | 0 |
| `io.github.amirisback.androidapp.data.repository.MealRepositoryImplTest` | 6 | 0 | 0 | 0 |
| `io.github.amirisback.androidapp.feature.detail.DetailViewModelTest` | 5 | 0 | 0 | 0 |
| `io.github.amirisback.androidapp.feature.favorite.FavoriteViewModelTest` | 4 | 0 | 0 | 0 |
| `io.github.amirisback.androidapp.feature.main.MainViewModelTest` | 4 | 0 | 0 | 0 |
| **Total** | **24** | **0** | **0** | **0** |

### C. Android Lint Audit (`lint-results-debug.sarif`)
> **QA Audit Clarification:** `03_build_mobile.md` reported `0 warnings` because SARIF findings inherit default severity from `tool.driver.rules[*].defaultConfiguration.level` when `result.level` is omitted. Resolving rule default levels independently confirms:

- **Errors / Fatal:** `0` (**PASS** per AC-08)
- **Warnings:** `11` (all pre-existing starter-template warnings; **0** introduced by `build-logic/convention`):
  - `1` `NewerVersionAvailable` (`kotlinx-coroutines-test` `1.10.2` -> `1.11.0`, out of scope per version-freeze rule)
  - `1` `ObsoleteSdkInt` (`app/src/main/res/drawable-v24`)
  - `6` `UnusedResources` (`R.string.admob_banner` & `R.string.admob_interstitial_id` across debug/release resValues)
  - `2` `MonochromeLauncherIcon` (`ic_launcher.xml`, `ic_launcher_round.xml`)
  - `1` `IconLocation` (`app/src/main/res/drawable/ic_frogobox.png`)

---

## 5. Security & Hygiene Audit

| Check | Result | Details |
|---|---|---|
| `@Suppress` / `@SuppressWarnings` (`app/src/` & `build-logic/`) | **PASS (0)** | Zero warning suppressions across application and build-logic sources. |
| `domain/` Layer Import Guardrail | **PASS (0)** | Zero forbidden imports (`android.*`, `androidx.*`, `retrofit2.*`, `gson`, `okhttp3`, `data.*`, `feature.*`). |
| Manifest / Network / ProGuard Regression | **PASS** | `git diff HEAD -- app/src/main/AndroidManifest.xml app/proguard-rules.pro app/src/` is empty. |
| Version Catalog Single Source of Truth | **PASS** | `build-logic/` contains `0` hardcoded dependency versions; all coordinates resolve from root `gradle/libs.versions.toml`. |
| Git Hygiene (`.kotlin/` & `buildSrc/`) | **PASS** | `.kotlin` added to root `.gitignore`; `buildSrc/` fully removed; no untracked Gradle/Kotlin cache artifacts in `git status`. |

### Security Advisory (Non-Blocking — Pre-Existing Template Debt)
- **Severity:** **MEDIUM (Informational / Follow-up Recommended)**
- **Finding:** Hardcoded release signing credentials remain in `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt` (`PLAYSTORE_STORE_FILE = "frogoboxmedia.jks"`, `PLAYSTORE_STORE_PASSWORD = "amirisback"`, `PLAYSTORE_KEY_ALIAS = "frogoisback"`, `PLAYSTORE_KEY_PASSWORD = "amirisback"`), alongside the pre-existing template keystore binary `app/frogoboxmedia.jks` committed in initial commit `5b87fb9` (untouched by TASK-004).
- **Disposition:** **Accepted as non-blocking for TASK-004** per frozen Phase 2 alignment decision (AC-02 strictly requires 100% constant parity with zero signing changes).
- **Recommendation for Future Task:** Move release keystore path and credentials out of Kotlin source into `local.properties` / CI environment variables (`providers.environmentVariable(...)` / `providers.gradleProperty(...)`) and add `*.jks` / `*.keystore` to `.gitignore` once template adopters are guided via README.

---

## 6. Quality Gate Decision

**VERDICT: APPROVED**

All 8 Acceptance Criteria (`AC-01` through `AC-08`) are independently verified with reproducible proof artifacts stored in `workflow/TASK-004-buildsrc-to-build-convention/proof/`. Ready for **Phase 6: PM Signoff (`05_signoff_faisal.md`)**.
