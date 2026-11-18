# Agent Guidelines — `init-android-apps`

- **Project:** `init-android-apps` (Google-standard single-module Android starter template)
- **Module / Namespace:** `:app` (`io.github.amirisback.androidapp`)
- **Toolchain & Stack:** AGP `9.5.1`, Kotlin `2.4.21`, KSP `2.3.14`, JVM `17`, `minSdk 24` / `compileSdk 37` / `targetSdk 37`
- **Core Libraries:** Jetpack Compose M3 (BOM `2026.09.00`), Hilt `2.60.1`, Room `2.8.5`, Retrofit + OkHttp + Gson, Coil `3.6.3`, Chucker `4.4.0`, Frogo SDK `4.0.0`, JUnit `4.13.2`, `kotlinx-coroutines-test` `1.10.2`

> **Mandatory First Step:** Read [`ARCHITECTURE.md`](ARCHITECTURE.md) before creating, moving, or refactoring any source file.

## Layer Dependency & Import Guardrails

| Layer | Allowed Dependencies | Forbidden Dependencies |
|---|---|---|
| `domain/` | Kotlin stdlib, `kotlinx.coroutines`, `javax.inject`, `core/common/Resource` | `android.*`, `androidx.*`, `retrofit2.*`, `com.google.gson.*`, `okhttp3.*`, `data/`, `feature/` |
| `data/` | `domain/`, `core/common/`, `core/di/` qualifiers (`@IoDispatcher`), Room, Retrofit, Gson, OkHttp, Hilt | `feature/`, Compose UI |
| `feature/` | `domain/`, `core/ui/`, `core/common/`, Compose M3, Lifecycle, Hilt | `data/` (no DAO, Entity, DTO, API service, or `*RepositoryImpl`) |
| `core/di/` | Wires `data/` implementations to `domain/` interfaces via Hilt | `feature/` |
| `core/ui/` | Compose M3, Coil 3, `domain/model/` (shared display components such as `MealCard`) | `data/`, feature ViewModels |

## Non-Negotiable Coding Rules

- Maintain a strict 1:1 match between physical directories and Kotlin `package` declarations in `app/src/main/java` and `app/src/test/java`.
- Follow Unidirectional Data Flow (UDF) in all stateful features: `ViewModel` exposes a single `StateFlow<*UiState>` (never raw `Resource<T>`) collected via `collectAsStateWithLifecycle()` in `<Feature>Route`, delegating to a stateless, previewable `<Feature>Screen(uiState, onEvent)`.
- **Zero warning suppression:** Never introduce `@Suppress` or `@SuppressWarnings`. Always migrate deprecated APIs to official modern replacements.
- Never hardcode `Dispatchers.IO` inside data sources; inject `@IoDispatcher` from `core/di/DispatcherModule.kt` and always rethrow `CancellationException`.
- Keep Activities inheriting directly from `FrogoComposeActivity` (with `enableEdgeToEdge()`) and ViewModels from `androidx.lifecycle.ViewModel`. Do not reintroduce empty base wrappers, XML layouts, or wrong-project copyright banners.
- Every new mapper, repository, or ViewModel must include JVM unit tests under `app/src/test/` using hand-written fakes and `kotlinx-coroutines-test`.

## Verification Commands

Run all three verification tasks before completing any code change:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Static checks to confirm on every change:
- Zero forbidden imports (`android.*`, `androidx.*`, `retrofit2.*`, `com.google.gson.*`, `okhttp3.*`, `data.*`, `feature.*`) inside `domain/`.
- Zero `@Suppress` or `@SuppressWarnings` annotations under `app/src/`.
- Exported Room schema JSON matches `app/schemas/io.github.amirisback.androidapp.data.local.database.AppDatabase/<version>.json`.

## Relevant Android Skills

Consult the matching Android skill (`SKILL.md`) before working in its domain:
- **`frogo-sdk`**: `FrogoComposeActivity`, Frogo SDK v4.0.0 utilities and conventions.
- **`edge-to-edge`**: `enableEdgeToEdge()`, `Scaffold` `innerPadding`, and window inset handling.
- **`jetpack-compose-m3`**: Material Design 3 components, typography, color roles, and `@Preview` hygiene.
- **`kotlin-coroutines-expert`**: Structured concurrency, `StateFlow`, `CancellationException`, and dispatcher injection.
- **`android-cli`**: Device deployment, emulator control, and environment diagnostics.
- **`r8-analyzer`**: ProGuard/R8 shrinking rules (e.g., Gson DTO preservation).
