# TASK-003: UI Architecture, Design System & UDF State Specification

> **Author:** Angel (UI/UX Designer)
> **Status:** FROZEN FOR IMPLEMENTATION (Phase 3 Track A)
> **Date:** 2026-11-18
> **Target Workspace:** `d:\AndroidProject\init-android-apps`
> **Namespace:** `io.github.amirisback.androidapp`
> **Upstream PRD:** [`01_prd_faisal.md`](file:///d:/AndroidProject/init-android-apps/workflow/TASK-003-google-standard-ai-ready-restructure/01_prd_faisal.md)

---

## 1. Frozen UI Package & Dependency Architecture

All shared Compose tokens and reusable UI components live under `core/ui/`, and all screens live under `feature/`.

```text
io.github.amirisback.androidapp/
|-- core/
|   |-- ui/
|       |-- theme/
|       |   |-- Color.kt
|       |   |-- Theme.kt
|       |   |-- Type.kt
|       |-- component/
|           |-- AppTopAppBar.kt
|           |-- CommonState.kt
|           |-- MealCard.kt
|-- feature/
    |-- main/
    |   |-- MainActivity.kt
    |   |-- MainScreen.kt
    |   |-- MainUiState.kt
    |   |-- MainViewModel.kt
    |-- favorite/
    |   |-- FavoriteScreen.kt
    |   |-- FavoriteUiState.kt
    |   |-- FavoriteViewModel.kt
    |-- detail/
    |   |-- DetailActivity.kt
    |   |-- DetailScreen.kt
    |   |-- DetailUiState.kt
    |   |-- DetailViewModel.kt
    |-- about/
        |-- AboutUsActivity.kt
        |-- AboutUsScreen.kt
```

### Layer Rules for UI Files
- `core/ui/theme/` depends only on `androidx.compose.*` and `android.os.Build` (for dynamic color). Zero `domain/` or `data/` imports.
- `core/ui/component/` may depend on `core/ui/theme/`, Compose Material 3, Coil 3, `R`, and `domain/model/` (specifically `Meal` for `MealCard`). Zero `data/` or `feature/` imports.
- `feature/*/` depends only on `domain/`, `core/ui/`, `core/common/`, Compose, Lifecycle, and Hilt. Zero `data/` imports.
- Zero `@Suppress` or `@SuppressWarnings` annotations (AC-09). `@OptIn(ExperimentalMaterial3Api::class)` is permitted where required by Material 3 `TopAppBar`.

---

## 2. `core/ui/` Component & Theme Contract

### 2.1 `core/ui/theme/` (`package io.github.amirisback.androidapp.core.ui.theme`)

| File | Contract & Hygiene Notes |
|---|---|
| `Color.kt` | Retain `Purple80`, `PurpleGrey80`, `Pink80`, `Purple40`, `PurpleGrey40`, `Pink40`, and `BrandGreen = Color(0xFF00C853)` (used on `AboutUsScreen`). No visual redesign. |
| `Theme.kt` | Keep `InitTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content: @Composable () -> Unit)` using Material 3 `dynamicDarkColorScheme` / `dynamicLightColorScheme` on API 31+ (`Build.VERSION_CODES.S`) with `DarkColorScheme` / `LightColorScheme` fallbacks. |
| `Type.kt` | Keep `Typography` using `MaterialTheme.typography` scale. Screens must consume `MaterialTheme.typography.*` instead of raw `.sp` literals. |

### 2.2 `core/ui/component/` (`package io.github.amirisback.androidapp.core.ui.component`)

#### `AppTopAppBar.kt`
Add an optional `actions` slot so `MainActivity` can host the About Us action icon cleanly (restoring reachability for AC-06). Replace hardcoded `"Back"` with string resource `R.string.cd_navigate_back`.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
)
```
- **Previews:** `AppTopAppBarPreview` (with back icon) and `AppTopAppBarWithActionsPreview` (with action icon).

#### `CommonState.kt`
Shared feedback states used across all feature screens (ensuring consistent Loading, Empty, and Error presentation):

```kotlin
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
)

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
)

@Composable
fun EmptyState(
    message: String = stringResource(id = R.string.message_empty_data),
    modifier: Modifier = Modifier,
)
```
- `ErrorMessage` and `EmptyState` wrap `Text` inside a centered `Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center)` or keep `Text` centered with `textAlign = TextAlign.Center` so multi-line messages never clip or left-align awkwardly in narrow viewports.
- `ErrorMessage` uses `color = MaterialTheme.colorScheme.error`, `style = MaterialTheme.typography.bodyLarge`.
- `EmptyState` uses `color = MaterialTheme.colorScheme.onSurfaceVariant`, `style = MaterialTheme.typography.bodyLarge`.
- **Previews:** `LoadingIndicatorPreview`, `ErrorMessagePreview`, `EmptyStatePreview`.

#### `MealCard.kt`
Update from legacy `MealModel` to pure domain `Meal` (`io.github.amirisback.androidapp.domain.model.Meal`) and apply Material 3 card & typography hygiene without redesigning the card layout:

```kotlin
@Composable
fun MealCard(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```
- **M3 Hygiene Requirements:**
  1. Use clickable M3 `Card(onClick = onClick, ...)` instead of `Modifier.clickable` on a non-clickable `Card` so ripple clips to the card's rounded corners and exposes proper clickable semantics.
  2. Set `colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)` (replaces `surface`) so cards have clean tonal contrast in both Light and Dark themes.
  3. Keep `elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)` and padding (`Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)`).
  4. `AsyncImage` uses fixed height `128.dp`, `ContentScale.Crop`, `crossfade(true)`, and `contentDescription = meal.name` (non-blank meal title, or fallback `stringResource(R.string.cd_meal_image)` if blank).
  5. Replace raw `fontSize` calls:
     - Meal name: `style = MaterialTheme.typography.titleMedium`, `fontWeight = FontWeight.Bold`, `color = MaterialTheme.colorScheme.primary`, `maxLines = 1`, `overflow = TextOverflow.Ellipsis`.
     - Meal area: `style = MaterialTheme.typography.bodyMedium`, `color = MaterialTheme.colorScheme.onSurfaceVariant`, `maxLines = 1`, `overflow = TextOverflow.Ellipsis`.
     - Meal category: `style = MaterialTheme.typography.bodyMedium`, `color = MaterialTheme.colorScheme.onSurfaceVariant`, `maxLines = 3`, `overflow = TextOverflow.Ellipsis`.
  6. Note on domain property mapping: Hudio maps domain `Meal` fields (`id`, `name`, `thumbnailUrl`, `category`, `area` or agreed property names in `domain/model/Meal.kt`) directly in `MealCard` and `DetailScreen` with non-null `String` defaults (no `?: ""` null cascades in UI).
- **Previews:** `MealCardLightPreview` and `MealCardDarkPreview` (`uiMode = Configuration.UI_MODE_NIGHT_YES`).

---

## 3. Feature UDF Contracts (`UiState`, `Route`, `Screen`, `@Preview`)

### Standard Pattern (Applies to All Stateful Features)
1. **ViewModel** exposes a single `val uiState: StateFlow<*UiState>` (zero raw `Resource<T>` exposed to UI).
2. **`<Feature>Route`** collects state via `viewModel.uiState.collectAsStateWithLifecycle()` (never `collectAsState()`), handles lifecycle side effects (`LaunchedEffect`), and delegates rendering to `<Feature>Screen`.
3. **`<Feature>Screen`** is 100% stateless: accepts only immutable `uiState`, event lambdas, and `modifier: Modifier = Modifier`.
4. **`LazyColumn` item keys** must use a guaranteed non-blank unique key (`key = { it.id }` on domain `Meal`) to avoid duplicate-key crashes.

---

### 3.1 `feature/main/` (`package io.github.amirisback.androidapp.feature.main`)

#### `MainUiState.kt`
```kotlin
package io.github.amirisback.androidapp.feature.main

import io.github.amirisback.androidapp.domain.model.Meal

sealed interface MainUiState {
    data object Loading : MainUiState
    data object Empty : MainUiState
    data class Success(val meals: List<Meal>) : MainUiState
    data class Error(val message: String) : MainUiState
}
```
- **Transition Rule in `MainViewModel`:**
  - Trigger initial `searchMeal("Cream")` in `MainViewModel.init` (remove `mainViewModel.searchMeal("Cream")` from `MainActivity.onCreateExt` so screen rotation does not re-fetch network data).
  - `Resource.Loading` -> `MainUiState.Loading`
  - `Resource.Success` with empty list -> `MainUiState.Empty`
  - `Resource.Success` with non-empty list -> `MainUiState.Success(meals)`
  - `Resource.Error` -> `MainUiState.Error(resource.message ?: "Unknown error")`

#### `MainActivity.kt` (App Shell)
- Hosts `MainScaffold` (or `MainActivityScreen`) inside `InitTheme`:
  - `Scaffold` `topBar`: `AppTopAppBar(title = stringResource(id = currentTab.titleRes), actions = { IconButton(onClick = onAboutClick) { Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(id = R.string.title_about_us)) } })`
  - `Scaffold` `bottomBar`: `NavigationBar` with `MainTab.MAIN` (`R.string.title_main`, `R.drawable.ic_tv`) and `MainTab.FAVORITE` (`R.string.title_fav`, `R.drawable.ic_favorite`).
  - `Scaffold` content applies `Modifier.fillMaxSize().padding(innerPadding)` and switches between `MainRoute` and `FavoriteRoute`.
  - Clicking About action launches `AboutUsActivity.launch(this)`.
  - Clicking a meal launches `DetailActivity.createIntent(this, meal)` via `startActivityResultExt(...)` and refreshes `favoriteViewModel.loadFavorites()` on return.
  - Remove `setupToolbar()` / `supportActionBar?.hide()` once XML theme uses `NoActionBar`.

#### `MainScreen.kt`
```kotlin
@Composable
fun MainRoute(
    viewModel: MainViewModel,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreen(
        uiState = uiState,
        onMealClick = onMealClick,
        modifier = modifier,
    )
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
)
```
- Handles all 4 branches exhaustively (`Loading` -> `LoadingIndicator()`, `Empty` -> `EmptyState()`, `Error` -> `ErrorMessage(uiState.message)`, `Success` -> `LazyColumn(contentPadding = PaddingValues(vertical = 8.dp))` with `key = { it.id }`).
- **Required `@Preview`s:**
  - `MainScreenSuccessPreview`
  - `MainScreenEmptyPreview`
  - `MainScreenErrorPreview`
  - `MainScreenLoadingPreview`

---

### 3.2 `feature/favorite/` (`package io.github.amirisback.androidapp.feature.favorite`)

#### `FavoriteUiState.kt`
```kotlin
package io.github.amirisback.androidapp.feature.favorite

import io.github.amirisback.androidapp.domain.model.Meal

sealed interface FavoriteUiState {
    data object Loading : FavoriteUiState
    data object Empty : FavoriteUiState
    data class Success(val meals: List<Meal>) : FavoriteUiState
    data class Error(val message: String) : FavoriteUiState
}
```
- **Transition Rule in `FavoriteViewModel`:**
  - Replaces raw `_mealsState: MutableStateFlow<Resource<List<MealModel>>>` with `_uiState: MutableStateFlow<FavoriteUiState>(FavoriteUiState.Loading)`.
  - Maps `Resource.Loading` -> `FavoriteUiState.Loading`, `Resource.Success(empty)` -> `FavoriteUiState.Empty`, `Resource.Success(nonEmpty)` -> `FavoriteUiState.Success(meals)`, `Resource.Error` -> `FavoriteUiState.Error(message)`.

#### `FavoriteScreen.kt`
```kotlin
@Composable
fun FavoriteRoute(
    viewModel: FavoriteViewModel,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoriteScreen(
        uiState = uiState,
        onMealClick = onMealClick,
        modifier = modifier,
    )
}

@Composable
fun FavoriteScreen(
    uiState: FavoriteUiState,
    onMealClick: (Meal) -> Unit,
    modifier: Modifier = Modifier,
)
```
- Handles all 4 branches exhaustively (`Loading`, `Empty`, `Error`, `Success` with `LazyColumn`, `contentPadding = PaddingValues(vertical = 8.dp)`, and `key = { it.id }`).
- **Required `@Preview`s:**
  - `FavoriteScreenSuccessPreview`
  - `FavoriteScreenEmptyPreview`
  - `FavoriteScreenErrorPreview`

---

### 3.3 `feature/detail/` (`package io.github.amirisback.androidapp.feature.detail`)

#### `DetailUiState.kt`
```kotlin
package io.github.amirisback.androidapp.feature.detail

import io.github.amirisback.androidapp.domain.model.Meal

data class DetailUiState(
    val meal: Meal? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val isDeleted: Boolean = false,
)
```
- **Why `data class`:** On Detail, the meal hero image and metadata stay rendered while `isLoading` overlays during favorite insert/delete operations, and transient UI events (`userMessage`, `errorMessage`, `isDeleted`) are state-driven per Google's Android UI Events guidance (preventing dropped events across configuration changes).
- **ViewModel Contract (`DetailViewModel`):**
  - Replaces the 3 separate `Resource` StateFlows (`mealsState`, `insertState`, `deleteState`) and Activity-held `mutableStateOf` flags with a single `val uiState: StateFlow<DetailUiState>`.
  - Methods: `setMeal(meal: Meal)` (sets `meal` and checks favorite status via `useCase.searchById(meal.id)`), `addToFavorites()`, `removeFromFavorites()`, `consumeUserMessage()`, `consumeErrorMessage()`.

#### `DetailScreen.kt` & `DetailActivity.kt`
```kotlin
@Composable
fun DetailRoute(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            onShowMessage(message)
            viewModel.consumeUserMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            onShowMessage(message)
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
        }
    }

    DetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddFavoriteClick = viewModel::addToFavorites,
        onRemoveFavoriteClick = viewModel::removeFromFavorites,
        modifier = modifier,
    )
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBackClick: () -> Unit,
    onAddFavoriteClick: () -> Unit,
    onRemoveFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```
- **Screen Composition & Hygiene Rules:**
  - `Scaffold` `topBar`: `AppTopAppBar(title = stringResource(id = R.string.title_detail), onBackClick = onBackClick)`.
  - Body `Box(Modifier.fillMaxSize().padding(innerPadding))`:
    - When `uiState.meal != null`: renders `DetailContent` (scrollable `Column` with `AsyncImage` height `350.dp`, meal title in `MaterialTheme.typography.titleLarge` bold, area right-aligned in `MaterialTheme.typography.bodyMedium`, category in `MaterialTheme.typography.bodyMedium`, pinned bottom action button).
    - When `uiState.meal == null && !uiState.isLoading`: renders `EmptyState(message = stringResource(id = R.string.message_meal_not_found))`.
    - When `uiState.isLoading`: renders `LoadingIndicator(modifier = Modifier.fillMaxSize())` overlay.
  - Action Buttons:
    - Favorite state (`uiState.isFavorite == true`): `OutlinedButton(onClick = onRemoveFavoriteClick, enabled = !uiState.isLoading, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(text = stringResource(id = R.string.action_remove_favorite)) }`
    - Non-favorite state (`uiState.isFavorite == false`): `Button(onClick = onAddFavoriteClick, enabled = !uiState.isLoading, modifier = Modifier.fillMaxWidth()) { Text(text = stringResource(id = R.string.action_add_favorite)) }`
- **Required `@Preview`s:**
  - `DetailScreenNotFavoritePreview`
  - `DetailScreenFavoritePreview`
  - `DetailScreenLoadingPreview`

---

### 3.4 `feature/about/` (`package io.github.amirisback.androidapp.feature.about`)

- Pure static screen: no `ViewModel`, `Route`, or `UiState` required (YAGNI).
- `AboutUsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier)`:
  - `Scaffold` `topBar`: `AppTopAppBar(title = stringResource(id = R.string.title_about_us), onBackClick = onBackClick)`.
  - `Image` logo: `contentDescription = stringResource(id = R.string.cd_app_logo)`, `Modifier.size(150.dp)`.
  - Brand title: `style = MaterialTheme.typography.titleLarge`, `fontWeight = FontWeight.Bold`, `color = BrandGreen`.
  - Copyright footer: `style = MaterialTheme.typography.bodySmall`, `color = MaterialTheme.colorScheme.onSurfaceVariant`, aligned `Alignment.BottomCenter` with `padding(bottom = 16.dp)`.
- **Required `@Preview`s:**
  - `AboutUsScreenLightPreview`
  - `AboutUsScreenDarkPreview` (`uiMode = Configuration.UI_MODE_NIGHT_YES`)

---

## 4. `res/` Hygiene Specification

### 4.1 `app/src/main/res/values/strings.xml` (Exact Target Contents)

Fixes the broken `title_about_us` value (`toolbar_about_us` -> `About Us`), extracts all hardcoded UI/a11y/toast strings, and removes unused XML template placeholder strings (`dummy`, `dummyLong`, `hello_blank_fragment`, `dialog_*`, `title_consumable`, `title_copyright`, `ads_size_*`).

```xml
<resources>
    <!-- Screen & Navigation Titles -->
    <string name="title_main">Main</string>
    <string name="title_fav">Favorite</string>
    <string name="title_detail">Detail Meals</string>
    <string name="title_about_us">About Us</string>

    <!-- Actions -->
    <string name="action_add_favorite">Insert to Database</string>
    <string name="action_remove_favorite">Remove From Database</string>

    <!-- Feedback & State Messages -->
    <string name="message_empty_data">Data is empty</string>
    <string name="message_meal_not_found">Meal data not found</string>
    <string name="message_added_to_favorite">Added %1$s to favorites</string>

    <!-- Content Descriptions (Accessibility) -->
    <string name="cd_navigate_back">Back</string>
    <string name="cd_app_logo">Frogobox Logo</string>
    <string name="cd_meal_image">Meal thumbnail</string>

    <!-- About Screen -->
    <string name="about_frogobox">Frogobox Software Industry</string>
    <string name="about_copyright">© Copyright 2019</string>
</resources>
```
*(Note: Keep `app_name`, `admob_app_id`, `admob_banner`, and `admob_interstitial_id` injected via `resValue` in `app/build.gradle.kts` untouched.)*

### 4.2 `themes.xml` Cleanup (`NoActionBar` for Compose Edge-to-Edge)

Replace `app/src/main/res/values/themes.xml` with a minimal Compose-ready `NoActionBar` host theme so `enableEdgeToEdge()` owns system bar rendering cleanly on API 24 through API 37 without ActionBar flash or deprecated `android:statusBarColor`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.KickStart" parent="Theme.MaterialComponents.DayNight.NoActionBar" />
</resources>
```
- Because `Theme.MaterialComponents.DayNight.NoActionBar` already adapts light/dark window chrome natively and Compose `InitTheme` handles 100% of surface/content theming, `app/src/main/res/values-night/themes.xml` becomes redundant and can be deleted along with legacy XML color/attr overrides.
- All Activities (`MainActivity`, `DetailActivity`, `AboutUsActivity`) can then remove the redundant `setupToolbar()` / `supportActionBar?.hide()` boilerplate.

### 4.3 Verified Dead `res/` Files Safe to Delete (0 Active References)

Verified by recursive scan across `app/src/` and `buildSrc/`:

| Category | Files Safe to Delete | Reason |
|---|---|---|
| `res/menu/` | `main_bottom_navigation.xml`, `menu_toolbar_detail.xml`, `menu_toolbar_main.xml` | Legacy XML menus; Compose `NavigationBar` and `AppTopAppBar` replaced them. |
| `res/drawable/` | `background_button_primary.xml`, `background_card.xml`, `background_card_toolbar.xml`, `background_splash_screen.xml`, `ic_movie.xml`, `ic_person.xml`, `ic_toolbar_about_us.xml`, `ic_toolbar_back_home.xml`, `ic_toolbar_back_home_white.xml`, `ic_toolbar_copyright.xml` | Zero Kotlin or active XML references. |
| `res/font/` | `montserrat_regular.ttf` | Referenced only by legacy `themes.xml`; never wired in Compose `Type.kt`. |
| `res/values/` | `attrs.xml`, `dimens.xml`, `base_colors.xml`, `colors.xml` | Referenced only by deleted legacy drawables and legacy `themes.xml`. |
| `res/values-night/` | `colors.xml`, `themes.xml` | Redundant once `values/themes.xml` uses `Theme.MaterialComponents.DayNight.NoActionBar`. |

**Active `res/` Assets to KEEP (Do Not Delete):**
- `res/drawable/ic_tv.xml`, `res/drawable/ic_favorite.xml`, `res/drawable/ic_frogobox.png`, `res/drawable/ic_launcher_background.xml`
- `res/drawable-v24/ic_launcher_foreground.xml`
- `res/mipmap-*/*` (launcher icons)
- `res/values/strings.xml`, `res/values/themes.xml`
- `res/xml/network_security_config.xml`

---

## 5. Edge-to-Edge, Material 3 & Accessibility Checklist

Hudio and Sandra should verify every item below before Phase 5 signoff:

- [ ] **Edge-to-Edge Activation:** `enableEdgeToEdge()` is invoked in `MainActivity`, `DetailActivity`, and `AboutUsActivity`, paired with `Theme.MaterialComponents.DayNight.NoActionBar` (no `android:statusBarColor`, no `supportActionBar?.hide()`).
- [ ] **Scaffold Inset Consumption:** Every `Scaffold` passes its `innerPadding` into the root content container (`Modifier.fillMaxSize().padding(innerPadding)`), preventing overlap with status bar, gesture navigation bar, top bar, or bottom bar.
- [ ] **Lifecycle-Safe Collection:** All feature routes collect `StateFlow` with `collectAsStateWithLifecycle()` (zero `collectAsState()` on ViewModel flows).
- [ ] **Complete UI States:** `MainScreen`, `FavoriteScreen`, and `DetailScreen` handle Loading, Content, Empty, and Error states cleanly via `LoadingIndicator`, `EmptyState`, and `ErrorMessage`.
- [ ] **Interactive Guarding:** `DetailScreen` favorite action buttons set `enabled = !uiState.isLoading` to prevent duplicate Room writes during in-flight operations.
- [ ] **Touch Targets & Semantics:** `AppTopAppBar` navigation/action controls use `IconButton` (48dp minimum touch target); `MealCard` uses M3 `Card(onClick = onClick)` for proper ripple clipping and accessibility role; `DetailScreen` buttons are full-width M3 `Button` / `OutlinedButton` (>= 48dp touch target).
- [ ] **Non-Empty Content Descriptions:** Every `Icon` and `Image`/`AsyncImage` provides a meaningful `contentDescription` sourced from `strings.xml` or `meal.name` (no hardcoded `"Back"` or `"Logo"` literals).
- [ ] **Typography & Dark Mode Contrast:** No raw `fontSize = *.sp` overrides in feature screens or `MealCard`; `MealCard` uses `surfaceContainerLow` and `MaterialTheme.colorScheme` roles so both Light and Dark themes maintain WCAG AA readability.
- [ ] **Config-Change Safety:** `MainViewModel` triggers initial meal search in `init` (not in `MainActivity.onCreateExt`), and `MainActivity` tab state uses `rememberSaveable`.
- [ ] **Preview Coverage:** `AppTopAppBar`, `CommonState`, `MealCard`, `MainScreen`, `FavoriteScreen`, `DetailScreen`, and `AboutUsScreen` all declare `@Preview` functions wrapped in `InitTheme`.
