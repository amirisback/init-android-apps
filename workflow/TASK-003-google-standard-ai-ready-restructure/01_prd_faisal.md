# TASK-003: Google-Standard & AI-Ready Android Project Restructure - Product Requirement Document (PRD)

> **Author:** Faisal (Product Manager)  
> **Status:** COMPLETED  
> **Date:** 2026-11-18  
> **Workspace:** `d:\AndroidProject\init-android-apps`  
> **Namespace:** `io.github.amirisback.androidapp`

---

## 1. Business Objective & Context

### Problem Statement
`init-android-apps` is intended as a reusable Android starter template, but its current package layout, architecture boundaries, and documentation actively mislead both human developers and AI coding agents:
1. **Broken Layer Isolation:** `domain/` mixes Room `@Database`/`@Entity`/`@Dao`, Retrofit `@GET` services, Gson `@SerializedName` DTOs, and repository implementations inside the domain package.
2. **Package-to-Folder Mismatch:** `ui/features/detail/DetailActivity.kt` and `DetailViewModel.kt` declare `package io.github.amirisback.androidapp.ui.detail`, contradicting their physical path, while `AndroidManifest.xml` registers `.ui.detail.DetailActivity`.
3. **Latent Runtime Bugs:** `MealApiService.filterByCategory` and `MealApiService.filterByArea` are missing `@GET(URL_FILTER)` annotations, causing Retrofit runtime exceptions if called. Stale Room schema `app/schemas/com.frogobox.kickstart.domain.db.ProjectDatabase/1.json` remains in VCS.
4. **Inconsistent UI Architecture:** `MainViewModel` exposes a dedicated `MainUiState`, whereas `FavoriteViewModel` and `DetailViewModel` leak raw `Resource<List<MealModel>>` streams and push state orchestration into Activity lifecycle collectors.
5. **Legacy Clutter & Wrong Metadata:** Dead XML/Fragment artifacts (`BaseFragment`, `IBaseFragment`, `IBaseActivity`, `OnItemClickCallback`, `Constant.kt` containing `BaseMusicPlayer`/`SPEECH_` constants) and 15-line copy-pasted headers referencing unrelated projects add token noise for AI tools.
6. **Misleading Documentation & Zero Tests:** `AGENTS.md` refers to a `piano-tiles` project, `README.md` still advertises Glide, LiveData, ViewBinding, and `minSdk 21`, no `ARCHITECTURE.md` exists, and `app/src/test/` contains zero unit tests.

### User Stories
1. **As an Android developer starting a new app from this template**, I want a predictable single-module package structure following Google's Official Guide to App Architecture (`core/`, `data/`, `domain/`, `feature/`) with clean UDF screens and working unit tests so I can clone the repo and ship features without refactoring boilerplate first.
2. **As an AI coding agent navigating or extending this repository**, I want strict 1:1 folder-to-package mapping, clean layer boundaries, accurate `AGENTS.md` and `ARCHITECTURE.md` guides, and zero dead legacy files so I can locate symbols, follow conventions, and scaffold new features without hallucinating or violating architecture rules.

### Success Metrics
- 100% of Kotlin source files under `app/src/main/java` align with Google Clean Architecture layers (`core/`, `data/`, `domain/`, `feature/`).
- 0 Android, Room, Retrofit, or Gson imports inside `domain/`.
- 0 package-to-directory mismatches across all Kotlin files.
- 0 `@Suppress` or `@SuppressWarnings` annotations introduced.
- Passing unit test baseline covering mappers, repository, and all ViewModels, verified with clean Gradle `assembleDebug`, `testDebugUnitTest`, and `lintDebug` builds.

---

## 2. Scope & Boundaries

### In Scope
- Single-module package restructure inside `:app` (`io.github.amirisback.androidapp`) into `core/`, `data/`, `domain/`, and `feature/`.
- Separation of Network DTOs, Room Entities, and pure Kotlin Domain Models with explicit mapper functions in `data/mapper/`.
- Relocation of Room database, DAOs, Retrofit services, remote/local data sources, and repository implementation into `data/`.
- Relocation of shared DI modules, design system theme/components, result wrapper, dispatchers/utilities, and base wrappers into `core/`.
- Standardization of all feature packages (`home` or `main`, `favorite`, `detail`, `about`) around Unidirectional Data Flow (`UiState` + `ViewModel` + stateless `Screen` composable).
- Fixing confirmed defects: missing `@GET(URL_FILTER)` in `MealApiService`, `DetailActivity` package/manifest alignment, and Room schema export cleanup.
- Removal of unused XML/Fragment legacy files, dead constants, and noisy misleading copyright headers while preserving all working user flows (meal browsing, detail view, favorite add/remove, about screen, AdMob/Frogo SDK/Chucker setup).
- Updating `AGENTS.md` and `README.md`, and adding `ARCHITECTURE.md` with package map, layer dependency rules, and a step-by-step "How to Add a New Feature" recipe.
- Adding a JVM unit test baseline under `app/src/test/` for mappers, `MealRepositoryImpl`, and ViewModels (`MainViewModel`/`HomeViewModel`, `FavoriteViewModel`, `DetailViewModel`), including minimal required test dependencies in `gradle/libs.versions.toml` and `app/build.gradle.kts`.

### Out of Scope (YAGNI / Ponytail)
- Multi-module Gradle split (`:core:*`, `:feature:*`): explicitly deferred to keep starter template setup fast and low-ceremony.
- Rewriting multi-Activity navigation (`MainActivity`, `DetailActivity`, `AboutUsActivity`) into a single-Activity Navigation 3 graph unless required to fix a defect.
- Visual redesign of existing screens, new API endpoints, new user-facing features, or UI/instrumentation tests (`androidTest`).
- Migrating Gson to Kotlinx Serialization or replacing Frogo SDK / Hilt / Coil 3 / Chucker dependencies.
- Modifying release signing configuration or CI pipeline logic beyond ensuring build compatibility.

---

## 3. Acceptance Criteria (AC Matrix)

- [x] **AC-01: Google-Standard Single-Module Package Hierarchy**
  ```gherkin
  Scenario: Source tree matches Google Guide to App Architecture and Now in Android conventions
    Given the `:app` source directory at `app/src/main/java/io/github/amirisback/androidapp`
    When a developer or AI agent inspects the top-level packages
    Then only clean top-level packages `core/`, `data/`, `domain/`, `feature/` and the root Application class exist
    And no legacy `common/`, `di/`, `ui/`, or `util/` root packages remain
  ```

- [x] **AC-02: Strict Domain Layer Isolation & Data Model Separation**
  ```gherkin
  Scenario: Domain layer contains pure Kotlin models, repository contracts, and use cases
    Given all Kotlin files under `domain/`
    When static import inspection is run on `domain/`
    Then `domain/` has zero imports from `android.*`, `androidx.*`, `retrofit2.*`, `com.google.gson.*`, or `okhttp3.*`
    And Retrofit DTOs with `@SerializedName` live exclusively in `data/remote/dto/`
    And Room `@Entity` classes, `@Dao` interfaces, and `@Database` live exclusively in `data/local/`
    And pure mapper functions converting DTO <-> Domain and Entity <-> Domain live in `data/mapper/`
    And `MealRepositoryImpl` and remote/local data sources live in `data/`
  ```

- [x] **AC-03: 100% Package-to-Folder Alignment & Valid Manifest**
  ```gherkin
  Scenario: Every Kotlin file package declaration matches its physical folder path
    Given all `.kt` files under `app/src/main/java` and `app/src/test/java`
    When comparing each file's `package` statement against its directory path
    Then 100% of files match their directory path with zero discrepancies
    And `app/src/main/AndroidManifest.xml` resolves `ProjectApplication`, `MainActivity`, `DetailActivity`, and `AboutUsActivity` to their exact restructured package names
  ```

- [x] **AC-04: Consistent UDF State Pattern Across All Features**
  ```gherkin
  Scenario: Every stateful feature exposes UiState from ViewModel to a stateless Compose Screen
    Given the feature packages `main`, `favorite`, `detail`, and `about`
    When inspecting feature ViewModels and Composables
    Then `MainViewModel`, `FavoriteViewModel`, and `DetailViewModel` each expose a single `StateFlow<*UiState>` (not raw `Resource<T>`)
    And each feature uses a two-layer `<Feature>Route` (collecting state via `collectAsStateWithLifecycle()`) + stateless, previewable `<Feature>Screen` Composable receiving immutable UI state and event lambdas
    And `MainViewModel.init` triggers the initial `searchMeal("Cream")` load, `DetailScreen` disables favorite action buttons while loading, `AboutUsActivity` is reachable from `MainActivity` top bar action, and `title_about_us` displays `"About Us"` instead of `"toolbar_about_us"`
  ```

- [x] **AC-05: Confirmed Defect Fixes, Security & Room Schema Hygiene**
  ```gherkin
  Scenario: Latent API, manifest, security, and Room schema defects are resolved
    Given the restructured `MealApiService`, `AppDatabase`, `AndroidManifest.xml`, and `network_security_config.xml`
    When inspecting these files
    Then `MealApiService.filterByCategory` and `MealApiService.filterByArea` both declare `@GET(MealApiConfig.URL_FILTER)` annotations
    And both stale Room schema directories (`com.frogobox.kickstart.domain.db.ProjectDatabase` and `io.github.amirisback.androidapp.domain.db.ProjectDatabase`) are deleted, and KSP exports a valid schema JSON under `io.github.amirisback.androidapp.data.local.database.AppDatabase/1.json`
    And `DetailActivity` and `AboutUsActivity` explicitly declare `android:exported="false"`, `network_security_config.xml` removes stale `newsapi.org`, `AndroidManifest.xml` sets `android:usesCleartextTraffic="false"`, and `app/proguard-rules.pro` adds Gson DTO keep rules
  ```

- [x] **AC-06: Dead Legacy Code & Misleading Boilerplate Cleanup**
  ```gherkin
  Scenario: Unused XML-era files, wrappers, dead resources, and wrong-project headers are removed
    Given the restructured repository
    When scanning source and resource files for legacy artifacts
    Then `BaseActivity.kt`, `BaseViewModel.kt`, `BaseFragment.kt`, `IBaseFragment.kt`, `IBaseActivity.kt`, `OnItemClickCallback.kt`, and `util/Constant.kt` are deleted (Activities extend `FrogoComposeActivity`, ViewModels extend `androidx.lifecycle.ViewModel`)
    And `viewBinding = true` is disabled in `app/build.gradle.kts`, XML themes switch to `NoActionBar` (removing `setupToolbar()`), and unused XML menus (`res/menu/*`), drawables, dimens, attrs, font, and dead strings are removed
    And copy-pasted multi-line headers referencing wrong projects (`BaseMusicPlayer`, `ImplementationAdmob`, `consumable-code-the-meal-db-api`) are removed from restructured Kotlin files while preserving all working app flows
  ```

- [x] **AC-07: AI-First Documentation Suite**
  ```gherkin
  Scenario: Documentation accurately reflects the template architecture and guides AI agents
    Given the repository root documentation files
    When an AI agent or developer reads `AGENTS.md`, `ARCHITECTURE.md`, and `README.md`
    Then `AGENTS.md` accurately targets `init-android-apps` (with zero references to `piano-tiles`), points to `ARCHITECTURE.md`, and defines binding layer and coding rules
    And `ARCHITECTURE.md` documents the exact package tree, unidirectional dependency rules (`feature -> domain <- data`, `core` shared), UDF pattern, and a step-by-step "How to Add a New Feature" recipe
    And `README.md` accurately lists the current stack (AGP 9.5.1, Kotlin 2.4.21, Jetpack Compose Material 3, Hilt, Room, Retrofit, Coil 3, Chucker, minSdk 24, compileSdk/targetSdk 37) with zero obsolete references to Glide, LiveData, ViewBinding, or minSdk 21
  ```

- [x] **AC-08: Unit Test Baseline**
  ```gherkin
  Scenario: Core data, utility, and presentation logic is verified by fast JVM unit tests
    Given the test source set under `app/src/test/java/io/github/amirisback/androidapp/` using `junit = "4.13.2"` and `kotlinx-coroutines-test = "1.10.2"` with hand-written fakes
    When running `./gradlew testDebugUnitTest`
    Then all 6 unit test suites exist and pass:
      - `MealMapperTest` (lossless DTO <-> Domain <-> Entity mapping)
      - `MealRepositoryImplTest` (remote success/error and local favorite operations)
      - `MainViewModelTest` (initial load and search `MainUiState` transitions)
      - `FavoriteViewModelTest` (`FavoriteUiState` loading/empty/content/error transitions)
      - `DetailViewModelTest` (`DetailUiState` favorite status, insert, delete, and loading guards)
      - `JsonExtTest` (`toJson`/`fromJson` valid and malformed JSON handling via `runCatching`)
  ```

- [x] **AC-09: Zero Warning Suppression & Clean Migration Policy**
  ```gherkin
  Scenario: Codebase adheres to strict no-suppression quality policy
    Given all added and modified Kotlin files in `app/src/main/java` and `app/src/test/java`
    When searching for `@Suppress` and `@SuppressWarnings`
    Then zero suppression annotations exist in modified or newly created files
    And any encountered deprecated API usage is migrated to its official modern replacement
  ```

- [x] **AC-10: Clean Gradle Build, Test, and Lint Verification**
  ```gherkin
  Scenario: Restructured project builds, tests, and passes lint cleanly
    Given the completed restructure in workspace `d:\AndroidProject\init-android-apps`
    When executing `./gradlew assembleDebug testDebugUnitTest lintDebug`
    Then all three Gradle tasks exit with code 0
    And verification logs/evidence are stored under `workflow/TASK-003-google-standard-ai-ready-restructure/proof/`
  ```

---

## 4. Contract Freeze Specifications (FROZEN)

### 4.1 Frozen Package Structure (`io.github.amirisback.androidapp`)
```text
io.github.amirisback.androidapp/
|-- ProjectApplication.kt
|-- core/
|   |-- common/
|   |   |-- Resource.kt                    (sealed interface Resource<out T>: Loading, Success, Error)
|   |   |-- BuildConfigExt.kt
|   |   |-- JsonExt.kt                     (toJson / fromJson wrapped safely with runCatching)
|   |-- di/
|   |   |-- DatabaseModule.kt              (object)
|   |   |-- DispatcherModule.kt            (object with @IoDispatcher)
|   |   |-- NetworkModule.kt               (object; @Singleton Cache, OkHttpClient, HttpLoggingInterceptor, Retrofit, MealApiService)
|   |   |-- RepositoryModule.kt            (@Binds @Singleton)
|   |   |-- UseCaseModule.kt               (@Binds @ViewModelScoped)
|   |-- ui/
|       |-- component/
|       |   |-- AppTopAppBar.kt            (supports optional actions slot)
|       |   |-- CommonState.kt
|       |   |-- MealCard.kt                (renders pure domain Meal)
|       |-- theme/
|           |-- Color.kt
|           |-- Theme.kt
|           |-- Type.kt
|-- data/
|   |-- local/
|   |   |-- database/
|   |   |   |-- AppDatabase.kt             (replaces ProjectDatabase)
|   |   |   |-- DatabaseConfig.kt
|   |   |-- dao/
|   |   |   |-- MealDao.kt                 (suspend fun queries/mutations)
|   |   |-- entity/
|   |   |   |-- MealEntity.kt
|   |   |-- MealLocalDataSource.kt         (injected @IoDispatcher; rethrows CancellationException)
|   |-- remote/
|   |   |-- api/
|   |   |   |-- MealApiConfig.kt           (URLs, query constants, API_KEY)
|   |   |   |-- MealApiService.kt          (@GET(MealApiConfig.URL_FILTER) fixed on filterByCategory & filterByArea)
|   |   |-- dto/
|   |   |   |-- MealDto.kt
|   |   |   |-- CategoryDto.kt
|   |   |   |-- AreaDto.kt
|   |   |   |-- IngredientDto.kt
|   |   |   |-- MealFilterDto.kt
|   |   |   |-- MealResponseDto.kt
|   |   |   |-- CategoryResponseDto.kt
|   |   |-- MealRemoteDataSource.kt        (supplies MealApiConfig.API_KEY; injected @IoDispatcher; rethrows CancellationException)
|   |-- mapper/
|   |   |-- MealMapper.kt                  (lossless bidirectional DTO <-> Domain <-> Entity)
|   |-- repository/
|       |-- MealRepositoryImpl.kt
|-- domain/
|   |-- model/
|   |   |-- Meal.kt                        (immutable val properties + ingredients/measures lists)
|   |   |-- Category.kt
|   |   |-- Area.kt
|   |   |-- Ingredient.kt
|   |   |-- MealFilter.kt
|   |-- repository/
|   |   |-- MealRepository.kt              (apiKey removed from repository signatures)
|   |-- usecase/
|       |-- MealUseCase.kt
|       |-- MealInteractor.kt
|-- feature/
    |-- main/
    |   |-- MainActivity.kt                (extends FrogoComposeActivity; top bar action navigates to AboutUsActivity)
    |   |-- MainScreen.kt                  (MainRoute with collectAsStateWithLifecycle() + stateless MainScreen + @Preview)
    |   |-- MainUiState.kt
    |   |-- MainViewModel.kt               (extends ViewModel; init triggers searchMeal("Cream"))
    |-- favorite/
    |   |-- FavoriteScreen.kt              (FavoriteRoute + stateless FavoriteScreen + @Preview)
    |   |-- FavoriteUiState.kt
    |   |-- FavoriteViewModel.kt           (extends ViewModel)
    |-- detail/
    |   |-- DetailActivity.kt              (extends FrogoComposeActivity; android:exported="false")
    |   |-- DetailScreen.kt                (DetailRoute + stateless DetailScreen; disables buttons while loading + @Preview)
    |   |-- DetailUiState.kt
    |   |-- DetailViewModel.kt             (extends ViewModel)
    |-- about/
        |-- AboutUsActivity.kt             (extends FrogoComposeActivity; android:exported="false")
        |-- AboutUsScreen.kt               (stateless AboutUsScreen + @Preview)
```

### 4.2 Layer Dependency Rules (Binding)
| Layer | Allowed Dependencies | Forbidden Dependencies |
|---|---|---|
| `domain/` | Kotlin stdlib, `kotlinx.coroutines`, `core/common/Resource` (pure Kotlin only) | `android.*`, `androidx.*`, `retrofit2.*`, `gson`, `okhttp3`, `data/`, `feature/` |
| `data/` | `domain/`, `core/common/`, `core/di/@IoDispatcher`, Room, Retrofit, Gson, OkHttp, Hilt | `feature/`, Compose UI |
| `feature/` | `domain/`, `core/ui/`, `core/common/`, Compose, Lifecycle, Hilt | `data/` (no direct DAO/DTO/API/RepositoryImpl imports) |
| `core/di/` | Wires `data/` implementations to `domain/` interfaces via Hilt | `feature/` |
| `core/ui/` | Compose Material 3, Coil 3, `domain/model/` (`MealCard` renders pure domain `Meal`) | `data/`, feature ViewModels |

### 4.3 Frozen Technical Contracts
- **Design System Packaging:** `core/ui/` (`theme/` + `component/`) is used instead of `core/designsystem/` per Google Now in Android domain-isolation rule (`MealCard` accepts domain `Meal`) and YAGNI.
- **YAGNI & Legacy Cleanup:** Delete `BaseActivity`, `BaseViewModel`, `BaseFragment`, `IBaseFragment`, `IBaseActivity`, `OnItemClickCallback`, and `util/Constant.kt`. Activities extend `FrogoComposeActivity` directly; ViewModels extend `androidx.lifecycle.ViewModel` directly. Disable `viewBinding = true` in `app/build.gradle.kts`. Remove dead XML `res/menu/*`, unused drawables, dimens, attrs, font, and strings. Switch XML themes to `NoActionBar` and remove `setupToolbar()`.
- **Security & Build Hygiene:** Declare `android:exported="false"` on `DetailActivity` and `AboutUsActivity`; remove stale `newsapi.org` entry from `network_security_config.xml`; set `android:usesCleartextTraffic="false"` in `AndroidManifest.xml`; add Gson DTO keep rules in `app/proguard-rules.pro`.
- **UI & UDF Hygiene:** Standardize all features on two-layer `<Feature>Route` (`collectAsStateWithLifecycle()`) + stateless `<Feature>Screen` with `@Preview` functions. Fix `title_about_us` string resource (`"toolbar_about_us"` -> `"About Us"`), extract hardcoded UI strings to `strings.xml`, wire `AboutUsActivity` from `MainActivity` top bar action via `AppTopAppBar`'s optional `actions` slot, move initial `searchMeal("Cream")` into `MainViewModel.init`, and disable favorite insert/delete buttons while `isLoading` is true.
- **Unit Test Contract:** Add `junit = "4.13.2"` and `kotlinx-coroutines-test = "1.10.2"` in `gradle/libs.versions.toml` and `app/build.gradle.kts`. Use hand-written fakes (no mocking framework bloat) for `MealMapperTest`, `MealRepositoryImplTest`, `MainViewModelTest`, `FavoriteViewModelTest`, `DetailViewModelTest`, and `JsonExtTest`.

---

### Cross-Role Alignment Meeting Log

> **Status:** FROZEN (Unanimous Cross-Role Alignment Complete)  
> **Date:** 2026-11-18  
> **Moderator:** Faisal (PM)

| Role | Question / Constraint | Resolution / Binding Decision |
|---|---|---|
| **Angel (UI/UX)** | `core/ui/` vs `core/designsystem/` naming; consistent UDF Route/Screen split; `AboutUsActivity` unreachable; `title_about_us` bug (`"toolbar_about_us"`); hardcoded strings; double-tap on favorite buttons during loading | Use `core/ui/` (`theme/` + `component/`) so `MealCard` can render domain `Meal` without violating NiA designsystem isolation; enforce `<Feature>Route` (`collectAsStateWithLifecycle()`) + stateless `<Feature>Screen` + `@Preview` across all features; add optional `actions` slot to `AppTopAppBar` to navigate to `AboutUsActivity`; fix `title_about_us` to `"About Us"`, extract hardcoded strings, move `searchMeal("Cream")` into `MainViewModel.init`, and disable favorite buttons while loading. |
| **Hudio (Mobile)** | YAGNI cleanup of empty base wrappers and unused XML resources; pure Kotlin `domain/` models; `AppDatabase` rename & schema cleanup; `MealApiService` bugs; safe JSON helper | Delete `BaseActivity`, `BaseViewModel`, `BaseFragment`, `IBaseFragment`, `IBaseActivity`, `OnItemClickCallback`, `util/Constant.kt`; inherit `FrogoComposeActivity` and `androidx.lifecycle.ViewModel` directly; disable `viewBinding = true`; switch themes to `NoActionBar` and remove `setupToolbar()` + dead XML menus/drawables/dimens/attrs/font/strings; rename `ProjectDatabase` -> `AppDatabase` and remove both stale schema folders; wrap `JsonExt` with `runCatching`; make `domain/` models immutable `val`s with `Meal.ingredients`/`measures` lists and remove `apiKey` from `MealRepository`. |
| **Bryan (Backend / Data & DI)** | Hilt scoping and dispatcher injection; coroutine cancellation safety; DataSource/DAO suspend contract; API key encapsulation; R8/ProGuard Gson safety | Convert `DatabaseModule`, `DispatcherModule` (`@IoDispatcher`), and `NetworkModule` to `object` modules with `@Singleton` on `Cache`, `OkHttpClient`, `HttpLoggingInterceptor`, `Retrofit`, `MealApiService`; use `@Binds @Singleton` in `RepositoryModule` and `@Binds @ViewModelScoped` in `UseCaseModule`; make `MealDao` methods `suspend fun`; inject `@IoDispatcher` in `MealLocalDataSource` & `MealRemoteDataSource` and rethrow `CancellationException`; encapsulate `MealApiConfig.API_KEY` in `MealRemoteDataSource`; fix `@GET(MealApiConfig.URL_FILTER)` on `filterByCategory` & `filterByArea`; add lossless bidirectional `MealMapper`; add Gson DTO keep rule in `app/proguard-rules.pro`. |
| **Sandra (QA & Security)** | Manifest component exposure; cleartext traffic & stale `newsapi.org` network config; deterministic fast unit tests without heavy mocking frameworks; static architecture & zero-suppression gates | Enforce `android:exported="false"` on `DetailActivity` & `AboutUsActivity`, remove stale `newsapi.org` from `network_security_config.xml`, set `usesCleartextTraffic="false"`; standardize unit tests on `junit = "4.13.2"` + `kotlinx-coroutines-test = "1.10.2"` with hand-written fakes covering 6 suites (`MealMapperTest`, `MealRepositoryImplTest`, `MainViewModelTest`, `FavoriteViewModelTest`, `DetailViewModelTest`, `JsonExtTest`); enforce static checks (0 forbidden `domain/` imports, 100% package-folder match, 0 `@Suppress`/`@SuppressWarnings`) plus `./gradlew assembleDebug testDebugUnitTest lintDebug` proof gate. |

---

## 5. Execution Tracks

- **Track A (UI/UX Architecture & State Spec - Angel):** Deliver `02_design_angel.md` defining `core/ui/` component structure, UDF `UiState` contracts for all feature screens, and edge-to-edge / Material 3 compliance notes.
- **Track B (Mobile Architecture Refactor & Unit Tests - Hudio):** Execute restructure across `core/`, `data/`, `domain/`, `feature/`, fix defects, clean legacy code, update `AGENTS.md`/`ARCHITECTURE.md`/`README.md`, add unit test baseline, and log in `03_build_mobile.md`.
- **Track C (QA Verification & Architecture Audit - Sandra):** Run static layer/package/suppression checks, execute `assembleDebug`, `testDebugUnitTest`, `lintDebug`, save proof artifacts under `proof/`, and deliver `04_qa_sandra.md`.
- **Signoff Gate (Faisal):** Verify all `AC-01` through `AC-10` against proof artifacts and finalize `05_signoff_faisal.md`.
