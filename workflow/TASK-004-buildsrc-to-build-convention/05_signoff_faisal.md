# TASK-004: Migrate `buildSrc` to Gradle Convention Plugins (`build-logic/convention`) - PM Delivery Signoff

> **Author:** Faisal (Product Manager)  
> **Status:** APPROVED & DELIVERED  
> **Signoff Date:** 2026-11-18  
> **QA Gate Status:** Sandra QA Approved (`8/8` ACs PASS, `0` Blocking Issues, `1` Non-Blocking MEDIUM Security Advisory)  
> **Workspace:** `d:\AndroidProject\init-android-apps`

---

## 1. Acceptance Criteria Verification Audit

| AC ID | Scenario & Criteria | Proof Artifact File | Audit Result |
|---|---|---|---|
| **AC-01** | Composite build `build-logic` included via `includeBuild("build-logic")` inside `pluginManagement`, `:convention` imports root `../gradle/libs.versions.toml` as `libs` with 0 hardcoded versions, `build-logic/gradle.properties` enables `parallel=true` & `caching=true` (no `configureondemand`), and root `.gitignore` adds `.kotlin` | `proof/ac01_settings_and_catalog.txt` | [x] PASSED |
| **AC-02** | `ProjectSetting` & `ProjectAds` cleanly migrated to public objects in `io.github.amirisback.androidapp.convention` preserving 100% of constant and computed values (`minSdk 24`, `compile/targetSdk 37`, `PROJECT_APP_ID`, version `100` / `1.0.0`, DB/Pref names, signing config, release & debug AdMob IDs) with stale `FrogoRecyclerView` headers removed | `proof/ac07_static_security_checks.txt`, `proof/ac04_buildconfig_debug_diff.txt` | [x] PASSED |
| **AC-03** | Exactly 4 focused convention plugins registered (`androidapp.android.application`, `androidapp.android.application.compose`, `androidapp.android.hilt`, `androidapp.android.room`) in `build-logic/convention/build.gradle.kts` and `[plugins]` in `gradle/libs.versions.toml` using lean 3 `compileOnly` deps (`android-gradleApiPlugin`, `kotlin-gradlePlugin`, `ksp-gradlePlugin`); `validatePlugins` passes with `enableStricterValidation = true` and `failOnWarning = true`; 0 speculative plugins created | `proof/ac03_build_logic_check.log` | [x] PASSED |
| **AC-04** | `app/build.gradle.kts` refactored declaratively (158 -> 37 lines) with explicit `namespace = "io.github.amirisback.androidapp"`, 0 `ProjectSetting`/`ProjectAds` imports, and 100% debug & release parity (exact SHA256 match on debug `BuildConfig.java` & `gradleResValues.xml`; release `BuildConfig` & `gradleResValues.xml` verified; debug APK named `kick-start-100-18-11-2026_02-31-debug.apk`; all app deps & Chucker excludes preserved) | `proof/ac04_buildconfig_debug_diff.txt`, `proof/ac04_resvalues_debug_diff.txt`, `proof/ac04_release_parity.txt`, `proof/ac04_apk_output.txt`, `proof/ac04_release_gradle.log` | [x] PASSED |
| **AC-05** | `buildSrc/` directory completely removed after `./gradlew --stop` (`Test-Path buildSrc` = `False`) with 0 remaining `buildSrc` references across active project files outside `workflow/` and zero untracked `.kotlin/` or build-cache artifacts | `proof/ac05_buildsrc_removal_check.txt` | [x] PASSED |
| **AC-06** | `README.md`, `AGENTS.md`, and `ARCHITECTURE.md` updated with exact migrated `ProjectSetting.kt` & `ProjectAds.kt` paths, `build-logic/convention` composite build architecture, 4 convention plugin inventory, and AGP 9.5.1 built-in Kotlin guardrails | Direct audit of `README.md`, `AGENTS.md`, `ARCHITECTURE.md` | [x] PASSED |
| **AC-07** | Zero `@Suppress` / `@SuppressWarnings` annotations across `app/src/` and `build-logic/`; modern AGP 9.5.1 / Gradle 9.9.1 / Kotlin 2.4.21 APIs (`ApplicationExtension`, never applying `org.jetbrains.kotlin.android`, `JavaVersion.VERSION_17` + `JvmTarget.JVM_17` without `java.toolchain`, `LocalDateTime` + `DateTimeFormatter`); 0 deprecation warnings across all `--warning-mode=all` runs | `proof/ac07_static_security_checks.txt`, `proof/ac07_deprecation_check.txt` | [x] PASSED |
| **AC-08** | Clean full verification (`./gradlew clean assembleDebug testDebugUnitTest lintDebug` and `./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig`) exited with code `0`; `24/24` JVM unit tests passed (`0` failures, `0` errors, `0` skipped); Room schema `AppDatabase/1.json` SHA256 byte-for-byte identical; Android Lint reported `0` errors | `proof/ac08_gradle_build.log`, `proof/ac08_junit_summary.txt`, `proof/ac08_room_schema_sha256.txt`, `proof/ac08_lint_summary.txt` | [x] PASSED |

---

## 2. Non-Functional, Security & Quality Gate Check
- [x] **Zero Warning / Zero Suppression:** `0` `@Suppress` / `@SuppressWarnings` annotations across `app/src/` and `build-logic/`; `0` deprecation warnings under `--warning-mode=all`; `validatePlugins` strict mode (`failOnWarning = true`) passed; `0` Android Lint errors.
- [x] **Byte-for-Byte Build Parity:** Debug `BuildConfig.java`, debug `gradleResValues.xml`, and Room schema `AppDatabase/1.json` all verified with exact SHA256 match against pre-migration baselines. Zero changes to `app/src/`, `AndroidManifest.xml`, or `app/proguard-rules.pro`.
- [x] **Scope Integrity (YAGNI / Ponytail):** Only the 4 plugins serving `:app` were created; lean 3 `compileOnly` build-logic dependencies; existing `android-library` and `android-dynamic-feature` catalog aliases preserved untouched without speculative convention plugins; UI/UX design (`02_design_angel.md`) cleanly marked N/A.
- [x] **Workflow Deliverables Complete:** `01_prd_faisal.md`, `03_build_backend.md`, `03_build_mobile.md`, `04_qa_sandra.md`, `05_signoff_faisal.md`, and 17 proof/baseline files in `proof/` are present, consistent, and audited.
- [x] **Rollback / Safe Revert Plan:** All migration edits are isolated to build configuration, `build-logic/`, `buildSrc/` removal, `.gitignore`, and docs; reverting via `git revert` restores prior `buildSrc/` setup cleanly if needed.

---

## 3. Non-Blocking Follow-Up Advisories (Post-Task Backlog)
Captured from Sandra's QA report (`04_qa_sandra.md`) for future template hardening (none block delivery):
1. **Pre-Existing MEDIUM Security Advisory (Release Signing Credentials):** Hardcoded template placeholder signing fields remain in `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt` (`PLAYSTORE_STORE_FILE`, `PLAYSTORE_STORE_PASSWORD`, `PLAYSTORE_KEY_ALIAS`, `PLAYSTORE_KEY_PASSWORD`) alongside the pre-existing `app/frogoboxmedia.jks` keystore binary committed in `5b87fb9` (preserved verbatim per frozen AC-02 parity requirement). Recommended future task: externalize release signing configuration to `local.properties` / CI environment variables (`providers.environmentVariable(...)` / `providers.gradleProperty(...)`) and ignore `*.jks` / `*.keystore` in `.gitignore`.
2. **Pre-Existing Starter-Template Lint Warnings:** 11 legacy non-blocking warnings (`1 NewerVersionAvailable`, `1 ObsoleteSdkInt`, `6 UnusedResources`, `2 MonochromeLauncherIcon`, `1 IconLocation`; `0` errors, `0` introduced by `build-logic/convention`) remain tracked in `proof/ac08_lint_summary.txt`.

---

## 4. Final PM Decision
- **Verdict:** **APPROVED & DELIVERED**
- **Release Summary:** `init-android-apps` has successfully migrated from legacy `buildSrc/` to a Google *Now in Android* style Gradle composite build (`build-logic/convention`) sharing root `gradle/libs.versions.toml` as the single source of truth. `ProjectSetting` and `ProjectAds` now live cleanly under `io.github.amirisback.androidapp.convention`, four focused convention plugins (`androidapp.android.application`, `androidapp.android.application.compose`, `androidapp.android.hilt`, `androidapp.android.room`) encapsulate AGP 9.5.1 / Kotlin 2.4.21 / JVM 17 / Compose / Hilt / Room setup, `app/build.gradle.kts` shrank from 158 lines to 37 declarative lines with byte-for-byte artifact parity, `buildSrc/` is fully removed, and documentation is synchronized. All 8 Acceptance Criteria are verified with independent proof artifacts.
- **Next Actions:** `01_prd_faisal.md` status and AC checkboxes updated to `COMPLETED`, and `workflow/INDEX.md` updated to `COMPLETED`.
