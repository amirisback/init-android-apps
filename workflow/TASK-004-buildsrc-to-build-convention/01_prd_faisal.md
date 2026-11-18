# TASK-004: Migrate `buildSrc` to Gradle Convention Plugins (`build-logic/convention`) - Product Requirement Document (PRD)

> **Author:** Faisal (Product Manager)  
> **Status:** COMPLETED  
> **Date:** 2026-11-18  
> **Workspace:** `d:\AndroidProject\init-android-apps`  
> **Namespace:** `io.github.amirisback.androidapp`

---

## 1. Business Objective & Context

### Problem Statement
`init-android-apps` currently holds build constants in a legacy `buildSrc/` module (`ProjectSetting.kt`, `ProjectAds.kt`) while duplicating plugin wiring, Android SDK/JVM configuration, Compose setup, Hilt setup, and Room KSP schema export boilerplate directly inside `app/build.gradle.kts` (158 lines). This setup creates four concrete drawbacks:
1. **Build Cache Invalidation:** Any change to a file inside `buildSrc/` invalidates Gradle build caches and task up-to-date checks across the entire build classpath.
2. **Version Catalog Isolation:** `buildSrc/` does not cleanly share the root `gradle/libs.versions.toml` catalog without workarounds, encouraging split version sources.
3. **Monolithic Module Build Script:** `app/build.gradle.kts` mixes application identity, AdMob resource injection, signing config, JVM 17 compiler options, Compose BOM dependencies, Hilt wiring, and Room KSP processor configuration in one imperative script.
4. **Misalignment with Google *Now in Android* Standards:** Modern Google-standard Android templates use an included composite build (`build-logic/convention`) with declarative convention plugins registered in `gradle/libs.versions.toml`.

### User Stories
1. **As an Android developer cloning this starter template**, I want project configuration (`ProjectSetting`, `ProjectAds`) and reusable Gradle convention plugins organized under `build-logic/convention` so I can customize app identity cleanly and keep `app/build.gradle.kts` short, declarative, and cache-friendly.
2. **As a maintainer or AI coding agent extending this repository**, I want standardized convention plugins (`androidapp.android.application`, `androidapp.android.application.compose`, `androidapp.android.hilt`, `androidapp.android.room`) backed by the single root `gradle/libs.versions.toml` so future modules or build updates inherit consistent AGP 9.5.1, Kotlin 2.4.21, JVM 17, Compose, Hilt, and Room settings without copy-pasting boilerplate.

### Success Metrics
- `buildSrc/` directory completely removed from the repository with 0 remaining references in active project files.
- `build-logic/convention` wired via `includeBuild("build-logic")` and reading dependency coordinates exclusively from root `gradle/libs.versions.toml`.
- `app/build.gradle.kts` reduced to declarative convention plugin application plus app-specific metadata and feature dependencies, with 100% functional build parity and zero direct imports of `ProjectSetting`/`ProjectAds`.
- 0 `@Suppress` or `@SuppressWarnings` annotations across `app/src/` and `build-logic/`, strict Gradle plugin validation passing (`enableStricterValidation = true`, `failOnWarning = true`), and 0 deprecated Gradle/AGP/Kotlin DSL APIs introduced.
- `./gradlew clean assembleDebug testDebugUnitTest lintDebug` and `./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig` exit with code `0`, all 24 existing JVM unit tests pass, Room schema JSON remains intact, and Android Lint reports `0` errors.

---

## 2. Scope & Boundaries

### In Scope
- Create composite build `build-logic/` with `build-logic/settings.gradle.kts` (importing root `../gradle/libs.versions.toml` into `versionCatalogs`), `build-logic/gradle.properties` (`org.gradle.parallel=true`, `org.gradle.caching=true`), and `:convention` subproject (`build-logic/convention/build.gradle.kts`).
- Register `includeBuild("build-logic")` inside `pluginManagement` in root `settings.gradle.kts`.
- Migrate `ProjectSetting` and `ProjectAds` out of `buildSrc/` into `build-logic/convention` as public objects in package `io.github.amirisback.androidapp.convention` (removing the stale `FrogoRecyclerView` header while preserving every constant value and derived property).
- Implement 4 focused Gradle Convention Plugins in `build-logic/convention` and register them in `build-logic/convention/build.gradle.kts` and `gradle/libs.versions.toml`:
  - `androidapp.android.application` -> `AndroidApplicationConventionPlugin` (targets `ApplicationExtension`: SDK 24/37, JVM 17, Kotlin compiler options via AGP 9.5.1 built-in Kotlin without applying `org.jetbrains.kotlin.android`, `BuildConfig` & `resValues`, default/release `ProjectSetting` and `ProjectAds` wiring, signing config, `base.archivesName` via `java.time.LocalDateTime` + `DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm")`, proguard config).
  - `androidapp.android.application.compose` -> `AndroidApplicationComposeConventionPlugin` (applies `kotlin.compose` by ID, `buildFeatures.compose = true`, Compose BOM, core Compose UI/M3/Foundation/Activity/Lifecycle/Tooling dependencies).
  - `androidapp.android.hilt` -> `AndroidHiltConventionPlugin` (applies KSP & Hilt by ID, `hilt-android` implementation and `hilt-compiler` KSP wiring).
  - `androidapp.android.room` -> `AndroidRoomConventionPlugin` (applies KSP by ID, `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`, `androidx-room-compiler` KSP wiring).
- Refactor `app/build.gradle.kts` to apply the 4 convention plugins declaratively while keeping `namespace = "io.github.amirisback.androidapp"` explicit and preserving full parity for `applicationId`, `BuildConfig.APPLICATION_ID`, `BuildConfig.DATABASE_NAME`, `BuildConfig.PREF_NAME`, debug & release `resValue`s (`app_name`, `admob_app_id`, `admob_banner`, `admob_interstitial_id`), `signingConfigs`, `proguard-rules.pro`, `base.archivesName`, Room KSP schema export, JVM 17, and remaining app dependencies (Chucker, Frogo SDK with Chucker excludes, Coil 3, `ksp(libs.androidx.lifecycle.compiler)`, `kotlinParcelize`, JVM test dependencies).
- Stop Gradle daemon (`./gradlew --stop`) on Windows before deleting `buildSrc/` entirely so no locked `.gradle/` directories remain.
- Add `.kotlin` to root `.gitignore` so `build-logic/.kotlin` stays untracked.
- Update `README.md`, `AGENTS.md`, and `ARCHITECTURE.md` to document `build-logic/convention`, updated customization paths for `ProjectSetting` and `ProjectAds`, and build architecture guidance.

### Out of Scope (YAGNI / Ponytail)
- Splitting `:app` into multiple Gradle modules (`:core:*`, `:data`, `:domain`, `:feature:*`).
- Creating speculative convention plugins for unused module types (`android.library`, `android.dynamic.feature`, `android.test`, `jvm.library`, `android.flavor`, Jacoco, Detekt, Ktlint).
- Modifying or removing existing `android-library` and `android-dynamic-feature` plugin aliases in `gradle/libs.versions.toml` and root `build.gradle.kts` (retained untouched to avoid scope creep and preserve starter-template readiness).
- Changing any app runtime behavior, UI screens (UI/UX Design `02_design_angel.md` is explicitly **N/A**), domain/data code, Room schema version, package namespace (`io.github.amirisback.androidapp`), signing credentials, or AdMob IDs.
- Upgrading Gradle (`9.9.1`), AGP (`9.5.1`), Kotlin (`2.4.21`), KSP (`2.3.14`), Hilt (`2.60.1`), Room (`2.8.5`), or library versions.
- Rewriting `.github/workflows/android-ci.yml` beyond ensuring existing Gradle tasks remain compatible.

---

## 3. Acceptance Criteria (AC Matrix)

- [x] **AC-01: Composite Build `build-logic` Setup & Shared Version Catalog**
  ```gherkin
  Scenario: Composite build `build-logic` integrates with root settings and shared `libs.versions.toml`
    Given the root `settings.gradle.kts`, root `.gitignore`, and `gradle/libs.versions.toml`
    When inspecting `settings.gradle.kts`, `build-logic/settings.gradle.kts`, and `build-logic/gradle.properties`
    Then root `settings.gradle.kts` includes `includeBuild("build-logic")` inside `pluginManagement`
    And `build-logic/settings.gradle.kts` configures `dependencyResolutionManagement` to import `../gradle/libs.versions.toml` into `versionCatalogs` named `libs`
    And `build-logic` includes `:convention` as a Gradle subproject without duplicating version numbers in `build-logic`
    And `build-logic/gradle.properties` sets `org.gradle.parallel=true` and `org.gradle.caching=true` (without `configureondemand`)
    And root `.gitignore` includes `.kotlin` so `build-logic/.kotlin` stays untracked
  ```

- [x] **AC-02: Clean Migration of `ProjectSetting` & `ProjectAds`**
  ```gherkin
  Scenario: Project configuration constants move from `buildSrc` to `build-logic/convention` with full value parity
    Given the existing `ProjectSetting` and `ProjectAds` definitions in `buildSrc/`
    When migrating both configuration objects into `build-logic/convention` under public package `io.github.amirisback.androidapp.convention`
    Then all constant values and computed outputs remain identical:
      | Key | Expected Value |
      | `PROJECT_MIN_SDK` / `PROJECT_COMPILE_SDK` / `PROJECT_TARGET_SDK` | `24` / `37` / `37` |
      | `PROJECT_APP_ID` | `"io.github.amirisback.kickstart"` |
      | `PROJECT_VERSION_CODE` / `PROJECT_VERSION_NAME` | `100` / `"1.0.0"` |
      | `NAME_APP` / `NAME_APP_DEBUG` | `"Kick Start"` / `"Kick Start DEV"` |
      | `NAME_APK` | `"kick-start"` |
      | `DB` / `PREF` | `"\"kick_start.db\""` / `"\"pref_kick_start\""` |
      | `PLAYSTORE_*` signing fields | Identical to existing `ProjectSetting` values |
      | `ProjectAds.Admob` release & `Debug` IDs | Identical to existing `ProjectAds` values |
    And stale unrelated project copyright headers (such as `FrogoRecyclerView`) are removed
  ```

- [x] **AC-03: Focused Convention Plugins Registered & Declared in Version Catalog**
  ```gherkin
  Scenario: Convention plugins are implemented in `build-logic/convention` and exposed via `gradle/libs.versions.toml`
    Given `build-logic/convention/build.gradle.kts` and `gradle/libs.versions.toml`
    When inspecting registered Gradle plugins and compile dependencies
    Then `build-logic/convention/build.gradle.kts` registers exactly 4 convention plugins:
      - `androidapp.android.application` -> `io.github.amirisback.androidapp.convention.AndroidApplicationConventionPlugin`
      - `androidapp.android.application.compose` -> `io.github.amirisback.androidapp.convention.AndroidApplicationComposeConventionPlugin`
      - `androidapp.android.hilt` -> `io.github.amirisback.androidapp.convention.AndroidHiltConventionPlugin`
      - `androidapp.android.room` -> `io.github.amirisback.androidapp.convention.AndroidRoomConventionPlugin`
    And `build-logic/convention/build.gradle.kts` uses only the lean 3 `compileOnly` dependencies (`android-gradleApiPlugin`, `kotlin-gradlePlugin`, `ksp-gradlePlugin`) and enables `tasks.validatePlugins { enableStricterValidation = true; failOnWarning = true }`
    And each convention plugin is declared under `[plugins]` in `gradle/libs.versions.toml`
    And no speculative unused convention plugins (such as library or dynamic-feature plugins) are created
  ```

- [x] **AC-04: Declarative `app/build.gradle.kts` Refactor with 100% Build Parity**
  ```gherkin
  Scenario: `:app` build script consumes convention plugins while preserving all generated debug and release build artifacts and configs
    Given the refactored `app/build.gradle.kts`
    When inspecting `:app` configuration and running debug assembly plus release resource/BuildConfig generation (`./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig`)
    Then `app/build.gradle.kts` applies the 4 convention plugins via `alias(libs.plugins.*)` with zero imports of `ProjectSetting` or `ProjectAds`
    And `app/build.gradle.kts` keeps `namespace = "io.github.amirisback.androidapp"` explicit
    And `:app` preserves `applicationId`, `BuildConfig.APPLICATION_ID`, `BuildConfig.DATABASE_NAME`, `BuildConfig.PREF_NAME`, debug and release `resValue` entries (`app_name`, `admob_app_id`, `admob_banner`, `admob_interstitial_id`) verified in `app/build/generated/res/resValues/<variant>/values/gradleResValues.xml`, `signingConfigs` (`release`), `proguard-rules.pro`, `base.archivesName` (using `java.time.LocalDateTime` + `DateTimeFormatter`), Room KSP schema location (`$projectDir/schemas`), JVM 17 target, and all required dependencies (Chucker, Frogo SDK with Chucker excludes, Coil 3, `ksp(libs.androidx.lifecycle.compiler)`, `kotlinParcelize`, JUnit, `kotlinx-coroutines-test`)
  ```

- [x] **AC-05: Complete Removal of `buildSrc/` & Zero Stale References**
  ```gherkin
  Scenario: Legacy `buildSrc` directory and all active references to it are eliminated cleanly on Windows
    Given `./gradlew --stop` is executed before removing `buildSrc/`
    When checking the repository filesystem and searching active project files outside `workflow/`
    Then the `buildSrc/` directory (including any `.gradle` or `build` subdirectories) does not exist
    And zero occurrences of `buildSrc` appear in `README.md`, `AGENTS.md`, `ARCHITECTURE.md`, `settings.gradle.kts`, `build.gradle.kts`, or `app/build.gradle.kts`
  ```

- [x] **AC-06: Documentation Updated for `build-logic/convention`**
  ```gherkin
  Scenario: Project documentation accurately describes the new build convention setup
    Given `README.md`, `AGENTS.md`, and `ARCHITECTURE.md`
    When a developer or AI agent reads template customization and build architecture instructions
    Then `README.md` points template customization steps to `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/ProjectSetting.kt` and `ProjectAds.kt`
    And `AGENTS.md` and `ARCHITECTURE.md` document the `build-logic/convention` composite build, the 4 convention plugins, and build guardrail rules (including AGP 9.5.1 built-in Kotlin constraint)
  ```

- [x] **AC-07: Zero Warning Suppressions & Modern Gradle/AGP 9.x API Compliance**
  ```gherkin
  Scenario: Build logic and app sources contain no suppressions and no newly introduced deprecated APIs
    Given all Kotlin source files under `app/src/` and `build-logic/`
    When scanning for `@Suppress` or `@SuppressWarnings` and compiling `build-logic` and `:app`
    Then zero `@Suppress` or `@SuppressWarnings` annotations exist in `app/src/` or `build-logic/`
    And `build-logic/convention` targets `ApplicationExtension` directly, never applies `org.jetbrains.kotlin.android`, uses `JavaVersion.VERSION_17` + `JvmTarget.JVM_17` (without `java.toolchain` provisioning), uses `java.time.LocalDateTime`/`DateTimeFormatter` instead of `SimpleDateFormat`, and passes `validatePlugins` with `failOnWarning = true`
  ```

- [x] **AC-08: Clean Full Gradle Verification & Existing Test Suite Parity**
  ```gherkin
  Scenario: Full clean build, release config parity, JVM unit test suite, Room schema export, and Android Lint pass cleanly
    Given the migrated repository with `build-logic/convention` active and `buildSrc/` removed
    When running `./gradlew clean assembleDebug testDebugUnitTest lintDebug` and `./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig`
    Then both Gradle commands complete with exit code `0`
    And all 24 existing JVM unit tests under `app/src/test/` pass with 0 failures and 0 skipped tests
    And `app/schemas/io.github.amirisback.androidapp.data.local.database.AppDatabase/1.json` remains valid and intact
    And Android Lint (`lintDebug`) passes with 0 errors
  ```

---

## 4. Contract Freeze Specifications (FROZEN - Phase 2 Approved)

- **Composite Build Path:** `build-logic/` with single Gradle subproject `:convention` (`build-logic/convention/`), included via `includeBuild("build-logic")` inside `pluginManagement` in root `settings.gradle.kts`.
- **Gradle Properties (`build-logic/gradle.properties`):**
  - `org.gradle.parallel=true`
  - `org.gradle.caching=true`
  - Explicitly omit `org.gradle.configureondemand`.
- **JDK / Kotlin Compilation Config (`build-logic/convention/build.gradle.kts`):**
  - Use `java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }` and `kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }` (do NOT use `java.toolchain` to avoid JDK 21 host provisioning failures).
  - Enable `tasks.validatePlugins { enableStricterValidation = true; failOnWarning = true }`.
- **Canonical Package:** `io.github.amirisback.androidapp.convention` (1:1 physical directory path under `build-logic/convention/src/main/kotlin/io/github/amirisback/androidapp/convention/`).
- **Convention Plugin Registry (4 plugins, frozen):**
  | Plugin ID | Version Catalog Alias (`[plugins]`) | Implementation Class |
  |---|---|---|
  | `androidapp.android.application` | `androidapp-android-application` | `io.github.amirisback.androidapp.convention.AndroidApplicationConventionPlugin` |
  | `androidapp.android.application.compose` | `androidapp-android-application-compose` | `io.github.amirisback.androidapp.convention.AndroidApplicationComposeConventionPlugin` |
  | `androidapp.android.hilt` | `androidapp-android-hilt` | `io.github.amirisback.androidapp.convention.AndroidHiltConventionPlugin` |
  | `androidapp.android.room` | `androidapp-android-room` | `io.github.amirisback.androidapp.convention.AndroidRoomConventionPlugin` |
- **Build-Logic `compileOnly` Dependencies (lean 3-dep set in `gradle/libs.versions.toml` `[libraries]`):**
  - `android-gradleApiPlugin = { group = "com.android.tools.build", name = "gradle-api", version.ref = "agp" }`
  - `kotlin-gradlePlugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }`
  - `ksp-gradlePlugin = { group = "com.google.devtools.ksp", name = "com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }`
  - `compose-gradlePlugin` and `hilt-gradlePlugin` are omitted; Compose and Hilt plugins are applied by ID backed by root `build.gradle.kts` `apply false` runtime classpath.
- **AGP 9.5.1 Built-in Kotlin Rule:**
  - Target `ApplicationExtension` directly (0 type parameters on `CommonExtension`).
  - **NEVER** apply `org.jetbrains.kotlin.android` (AGP 9.5.1 auto-registers `KotlinAndroidProjectExtension` as `"kotlin"` and rejects `org.jetbrains.kotlin.android`).
- **Migrated Configuration Objects:**
  - `ProjectSetting` and `ProjectAds` live as **public** objects in `io.github.amirisback.androidapp.convention`.
  - All current usages are encapsulated inside `AndroidApplicationConventionPlugin`, so `app/build.gradle.kts` has zero direct imports of `ProjectSetting` or `ProjectAds`.
- **App Module Responsibilities (`app/build.gradle.kts`):**
  - Explicit `namespace = "io.github.amirisback.androidapp"`.
  - Applies the 4 convention plugins + `alias(libs.plugins.kotlinParcelize)`.
  - Retains app-specific dependencies: `frogo-android` (with Chucker excludes), `github-chucker` / `github-chucker-no-op`, `coil-compose` / `coil-network-okhttp`, `ksp(libs.androidx.lifecycle.compiler)`, `junit`, and `kotlinx-coroutines-test`.
- **Root Build & Version Catalog Preservation:**
  - Retain existing `apply false` plugin declarations in root `build.gradle.kts` (including `android-library` and `android-dynamic-feature` aliases in `gradle/libs.versions.toml` & root `build.gradle.kts`) untouched.
- **Git & OS Hygiene:**
  - Append `.kotlin` to root `.gitignore`.
  - Run `./gradlew --stop` before deleting `buildSrc/`.
- **UI/UX Design Deliverable (`02_design_angel.md`):** Explicitly marked **N/A** (pure build infrastructure refactor with zero UI changes).

---

## Cross-Role Alignment Meeting Log

| Role | Question / Constraint | Resolution / Binding Decision |
|---|---|---|
| **Bryan (Build / DevOps)** | Verified AGP 9.5.1 / Gradle 9.9.1 / Kotlin 2.4.21 / KSP 2.3.14 bytecode: `CommonExtension` has 0 type parameters; `ApplicationExtension` extends `CommonExtension`; AGP 9.5.1 built-in Kotlin auto-registers `KotlinAndroidProjectExtension` as `"kotlin"` and rejects `org.jetbrains.kotlin.android`. Host runs JDK 21 so `java.toolchain` risks provisioning failure. Proposed lean 3 `compileOnly` deps (`gradle-api`, `kotlin-gradle-plugin`, `ksp-gradle-plugin`), omitting `compose-gradlePlugin` and `hilt-gradlePlugin`. Proposed public `ProjectSetting`/`ProjectAds` in `io.github.amirisback.androidapp.convention` encapsulated by `AndroidApplicationConventionPlugin`, `.kotlin` in root `.gitignore`, `validatePlugins` strict mode, and `build-logic/gradle.properties` (`parallel=true`, `caching=true`, no `configureondemand`). | **Faisal (PM Decision):** Approved in full. Freeze lean 3-dep `compileOnly` set (`android-gradleApiPlugin`, `kotlin-gradlePlugin`, `ksp-gradlePlugin`) while retaining root `build.gradle.kts` `apply false` runtime classpath. Freeze `ApplicationExtension` direct targeting, strict prohibition against applying `org.jetbrains.kotlin.android`, explicit `JavaVersion.VERSION_17` + `JvmTarget.JVM_17` without `java.toolchain`, `validatePlugins` strict mode, public `ProjectSetting`/`ProjectAds` encapsulated in `AndroidApplicationConventionPlugin`, `build-logic/gradle.properties`, and `.kotlin` entry in root `.gitignore`. |
| **Hudio (Mobile)** | Confirmed `BuildConfig.APPLICATION_ID` is used in `core/common/BuildConfigExt.kt:7`. Proposed `java.time.LocalDateTime` + `DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm")` for `base.archivesName` on JVM 17, keeping `namespace` explicit in `app/build.gradle.kts`, and keeping app-specific plugins/deps (`kotlinParcelize`, `frogo-android`, Chucker, Coil 3, test libs) in `app/build.gradle.kts`. Raised two open questions: (Q1) keep or remove unused `ksp(libs.androidx.lifecycle.compiler)`? (Q2) keep or remove unused `android-library` & `android-dynamic-feature` aliases in `libs.versions.toml` and root `build.gradle.kts`? | **Faisal (PM Decision):** Approved `BuildConfig.APPLICATION_ID` parity addition, `LocalDateTime` + `DateTimeFormatter` for `base.archivesName`, explicit `namespace` in `app/build.gradle.kts`, and keeping app-specific deps in `:app`. **(Q1) Keep `ksp(libs.androidx.lifecycle.compiler)`** in `app/build.gradle.kts` for strict AC-04 dependency parity without altering annotation processing scope. **(Q2) Keep `android-library` and `android-dynamic-feature` aliases untouched** in `gradle/libs.versions.toml` and root `build.gradle.kts` to avoid unrelated diff outside `buildSrc -> build-logic` scope and keep template expansion ready (while creating zero speculative convention plugins for them). |
| **Sandra (QA / Security)** | Verified AGP 9.5.1 generates `app/build/generated/res/resValues/<variant>/values/gradleResValues.xml` (not `generated.xml`). Requested adding `./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig` to AC-04 QA checks, mandating `./gradlew --stop` before deleting `buildSrc/` on Windows, preserving hardcoded `PLAYSTORE_*` template placeholders for parity with a non-blocking MEDIUM security advisory in `04_qa_sandra.md`, and confirming `02_design_angel.md` is N/A. | **Faisal (PM Decision):** Approved all items. Added `BuildConfig.APPLICATION_ID`, `gradleResValues.xml` verification, and `./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig` to AC-04 and AC-08. Mandated `./gradlew --stop` prior to `buildSrc/` deletion in AC-05. Approved preserving hardcoded `PLAYSTORE_*` template placeholders with a non-blocking MEDIUM security advisory logged in `04_qa_sandra.md`. Marked `02_design_angel.md` as **N/A** (zero UI changes). |
| **Faisal (PM)** | Contract freeze & YAGNI gate before Phase 3 execution. | **Faisal (PM Decision):** Contract is **ALIGNED & FROZEN**. Proceed directly to Phase 3 Engineering Implementation (Bryan + Hudio) followed by Phase 4 QA Gate (Sandra) and Phase 5 Signoff (Faisal). |

---

## 5. Execution Tracks (Active)

- **Track A (UI/UX - Angel):** **N/A** (pure build infrastructure migration; zero UI changes).
- **Track B (Build Infrastructure - Bryan):** Create `build-logic/` (`settings.gradle.kts`, `gradle.properties`, `:convention/build.gradle.kts`), migrate `ProjectSetting` & `ProjectAds`, implement the 4 convention plugins, update `settings.gradle.kts`, `gradle/libs.versions.toml`, and root `.gitignore`, and log deliverables in `03_build_backend.md`.
- **Track C (App Module & Docs - Hudio):** Refactor `app/build.gradle.kts` declaratively, run `./gradlew --stop` and remove `buildSrc/`, update `README.md`, `AGENTS.md`, and `ARCHITECTURE.md`, and log deliverables in `03_build_mobile.md`.
- **Phase 4 (QA & Security Gate - Sandra):** Run `./gradlew clean assembleDebug testDebugUnitTest lintDebug` and `./gradlew :app:generateReleaseResValues :app:generateReleaseBuildConfig`, verify `gradleResValues.xml`, `BuildConfig` (`APPLICATION_ID`, `DATABASE_NAME`, `PREF_NAME`), 24 unit tests, Room schema JSON, zero `buildSrc` refs, zero `@Suppress`, and log report (plus non-blocking MEDIUM advisory on `PLAYSTORE_*`) in `04_qa_sandra.md`.
- **Phase 5 (PM Signoff - Faisal):** Audit `AC-01` through `AC-08`, write `05_signoff_faisal.md`, and mark `TASK-004` `COMPLETED` in `workflow/INDEX.md`.
