# TASK-003: Architecture, DI, Build Config & AI-Readability Blueprint

> **Author:** Bryan (Backend & DevOps / Architecture Mapper)  
> **Phase:** Phase 3 — Track B (Architecture, DI & DevOps Spec)  
> **Status:** FROZEN CONTRACT SPEC (Ready for Phase 4 Implementation by Hudio)  
> **Workspace:** `d:\AndroidProject\init-android-apps`  
> **Namespace:** `io.github.amirisback.androidapp`

---

## 1. Gradle, Build Config, Room Schema & Security Specifications

### 1.1 Version Catalog (`gradle/libs.versions.toml`)
Add minimal JVM unit test dependencies under `# Test Library` (no MockK/Mockito/Turbine bloat unless needed; pure JUnit 4 + `kotlinx-coroutines-test` with fakes covers AC-08 cleanly):

```toml
[versions]
# Existing versions unchanged...
junit = "4.13.2"
coroutinesTest = "1.10.2"

[libraries]
# --------------------------------------------------------------------------------------------------
# Test Library
junit = { group = "junit", name = "junit", version.ref = "junit" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutinesTest" }
```

### 1.2 App Module Gradle (`app/build.gradle.kts`)
1. **`buildFeatures` cleanup**: Remove unused `viewBinding = true` (`app/src/main/res/layout/` does not exist; all screens are Jetpack Compose):
   ```kotlin
   buildFeatures {
       buildConfig = true
       resValues = true
       compose = true
   }
   ```
2. **Unit test dependencies**: Append to `dependencies { ... }`:
   ```kotlin
   // Unit Testing (JVM)
   testImplementation(libs.junit)
   testImplementation(libs.kotlinx.coroutines.test)
   ```
3. **KSP Room schema config (keep as-is)**:
   ```kotlin
   ksp {
       arg("room.schemaLocation", "$projectDir/schemas")
   }
   ```
4. **Unchanged / Verified Safe**:
   - `namespace = "io.github.amirisback.androidapp"` — matches root package.
   - `buildSrc/src/main/kotlin/ProjectSetting.kt` & `ProjectAds.kt` — zero references to `app/src/main/java` internal packages.
   - `.github/workflows/android-ci.yml` — zero package/class path references; untouched per PRD Out-of-Scope. *(Backlog note: CI workflow still uses `actions/checkout@v1`, `actions/setup-java@v1` with Java 11, and `actions/upload-artifact@v2`, whereas AGP 9.5.1 requires JDK 17 — track as separate DevOps maintenance task.)*

### 1.3 Room Schema Hygiene (`app/schemas/`)
- **Delete stale directories completely**:
  - `app/schemas/com.frogobox.kickstart.domain.db.ProjectDatabase/`
  - `app/schemas/io.github.amirisback.androidapp.domain.db.ProjectDatabase/`
- **Verify KSP output after build**:
  - `app/schemas/io.github.amirisback.androidapp.data.local.database.AppDatabase/1.json`
  - Must be generated cleanly by `./gradlew assembleDebug` (KSP) and retained in VCS.

### 1.4 ProGuard / R8 Rules (`app/proguard-rules.pro`)
Keep rules minimal and explicit so release builds with `isMinifyEnabled = true` never strip Gson DTO field names:

```proguard
# Preserve Retrofit/Gson DTO fields and annotations under R8 shrinking
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault
-keep class io.github.amirisback.androidapp.data.remote.dto.** { *; }
```

### 1.5 Network Security & Manifest (`network_security_config.xml` & `AndroidManifest.xml`)
1. **`app/src/main/res/xml/network_security_config.xml`**:
   - Remove stale cleartext exception for `newsapi.org` (leftover from unrelated template).
   - Enforce HTTPS-only (`themealdb.com` is already `https://www.themealdb.com/`):
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config>
       <base-config cleartextTrafficPermitted="false" />
   </network-security-config>
   ```
2. **`app/src/main/AndroidManifest.xml`**:
   - Remove `android:usesCleartextTraffic="true"` from `<application>`.
   - Keep `android:networkSecurityConfig="@xml/network_security_config"`.
   - Keep `android:name=".ProjectApplication"` at root package.
   - Update Activity class names to match restructured `feature/` packages (1:1 alignment with AC-03):
     ```xml
     <activity android:name=".feature.detail.DetailActivity" />
     <activity android:name=".feature.about.AboutUsActivity" />
     <activity
         android:name=".feature.main.MainActivity"
         android:exported="true">
         <intent-filter>
             <action android:name="android.intent.action.MAIN" />
             <category android:name="android.intent.category.LAUNCHER" />
         </intent-filter>
     </activity>
     ```
     *(If Phase 2/Angel frozen feature package is `feature/home/` for `MainActivity`, align `.feature.home.MainActivity` accordingly; ensure 100% match between `AndroidManifest.xml` and the actual `package` statement in `MainActivity.kt`.)*

---

## 2. Hilt Dependency Injection Contracts (`core/di/`)

All DI modules live exclusively under `io.github.amirisback.androidapp.core.di`.
**Rule:** `core/di/` is the **only** package allowed to reference both `data/` implementations and `domain/` contracts simultaneously. `core/di/` MUST NOT import `feature/`.

### 2.1 `core/di/DispatcherModule.kt`
Eliminates 17 hardcoded `Dispatchers.IO` calls inside data sources so `MealRemoteDataSource` and `MealLocalDataSource` run deterministically under `kotlinx-coroutines-test`.

```kotlin
package io.github.amirisback.androidapp.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
```

**Data-source constructor contract:**
- `MealRemoteDataSource @Inject constructor(private val apiService: MealApiService, @IoDispatcher private val ioDispatcher: CoroutineDispatcher)`
- `MealLocalDataSource @Inject constructor(private val mealDao: MealDao, @IoDispatcher private val ioDispatcher: CoroutineDispatcher)`

### 2.2 `core/di/DatabaseModule.kt`
- Uses `object` (no module instantiation overhead).
- Hilt owns the `@Singleton` lifecycle — **delete** manual `companion object { @Volatile INSTANCE; fun getInstance(...) }` from `AppDatabase`.
- `AppDatabase` stays a pure Room `@Database(entities = [MealEntity::class], version = 1, exportSchema = true)` abstract class in `data/local/database/AppDatabase.kt`.

```kotlin
package io.github.amirisback.androidapp.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.amirisback.androidapp.core.common.appDatabaseName
import io.github.amirisback.androidapp.core.common.appIsDebug
import io.github.amirisback.androidapp.data.local.dao.MealDao
import io.github.amirisback.androidapp.data.local.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            appDatabaseName,
        )
        if (appIsDebug) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }
        return builder.build()
    }

    @Provides
    fun provideMealDao(database: AppDatabase): MealDao = database.mealDao()
}
```

### 2.3 `core/di/NetworkModule.kt`
Fixes existing unscoped `OkHttpClient.Builder` / `Retrofit` / `Cache` leak (which previously allocated new clients & thread pools per injection) and removes dependency on dead `util/Constant.kt`.

```kotlin
package io.github.amirisback.androidapp.core.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.amirisback.androidapp.core.common.appIsDebug
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig
import io.github.amirisback.androidapp.data.remote.api.MealApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE_BYTES = 10L * 1024L * 1024L
    private const val TIMEOUT_SECONDS = 60L

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache =
        Cache(context.cacheDir, CACHE_SIZE_BYTES)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (appIsDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        cache: Cache,
        @ApplicationContext context: Context,
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(ChuckerInterceptor.Builder(context).build())
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val nullOnEmptyConverterFactory = object : Converter.Factory() {
            override fun responseBodyConverter(
                type: Type,
                annotations: Array<out Annotation>,
                retrofit: Retrofit,
            ): Converter<ResponseBody, Any?> {
                val delegate = retrofit.nextResponseBodyConverter<Any?>(this, type, annotations)
                return Converter { body ->
                    if (body.contentLength() != 0L) delegate.convert(body) else null
                }
            }
        }

        return Retrofit.Builder()
            .baseUrl(MealApiConfig.BASE_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideMealApiService(retrofit: Retrofit): MealApiService =
        retrofit.create(MealApiService::class.java)
}
```

### 2.4 `core/di/RepositoryModule.kt`
- Removes redundant `includes = [NetworkModule::class, DatabaseModule::class]` (already installed in `SingletonComponent`).
- Binds `MealRepositoryImpl` as `@Singleton`.

```kotlin
package io.github.amirisback.androidapp.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.amirisback.androidapp.data.repository.MealRepositoryImpl
import io.github.amirisback.androidapp.domain.repository.MealRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMealRepository(impl: MealRepositoryImpl): MealRepository
}
```

### 2.5 `core/di/UseCaseModule.kt`
- Keeps `ViewModelComponent` + `@ViewModelScoped`.
- **Critical Domain-Isolation Note for Hudio**: Current `MealInteractor` imports `MealUrl.API_KEY`. Moving `MealUrl` into `data/remote/api/MealApiConfig.kt` would create a forbidden `domain/ -> data/` import if `MealInteractor` still referenced it. Instead, inject/supply `MealApiConfig.API_KEY` inside `MealRemoteDataSource` or `MealRepositoryImpl` (so `MealRepository` methods do not expose `apiKey` to `domain/`, or pass `apiKey` inside `data/`), keeping `domain/` 100% free of `data/` imports.

```kotlin
package io.github.amirisback.androidapp.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.github.amirisback.androidapp.domain.usecase.MealInteractor
import io.github.amirisback.androidapp.domain.usecase.MealUseCase

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

    @Binds
    @ViewModelScoped
    abstract fun bindMealUseCase(impl: MealInteractor): MealUseCase
}
```

---

## 3. AI-Readability Documentation Specifications

Hudio shall author/update all three root docs (`AGENTS.md`, `ARCHITECTURE.md`, `README.md`) in Phase 4 following these exact contracts. Keep prose concise, zero filler, zero legacy project names (`piano-tiles`, `BaseMusicPlayer`, `newsapi.org`, `Glide`, `LiveData`, `ViewBinding`, `minSdk 21`).

### 3.1 `AGENTS.md` Specification (Hard Cap: <120 Lines)
Purpose: Fast, low-token binding instruction file loaded automatically by AI coding agents on every session.

Required sections (in exact order):
1. **Header & Project Identity** (5–8 lines):
   - Project: `init-android-apps` (Google-standard single-module Android starter template).
   - Namespace: `io.github.amirisback.androidapp` | Module: `:app`.
   - Stack: AGP `9.5.1`, Kotlin `2.4.21`, KSP `2.3.14`, Jetpack Compose M3 (BOM `2026.09.00`), Hilt `2.60.1`, Room `2.8.5`, Retrofit + Gson, Coil `3.6.3`, Chucker `4.4.0`, Frogo SDK `4.0.0`, `minSdk 24` / `compileSdk 37` / `targetSdk 37`, JVM 17.
   - Explicit pointer: **"Read `ARCHITECTURE.md` before creating, moving, or refactoring any source file."**
2. **Layer Dependency & Import Guardrails** (compact Markdown table):
   - `domain/` -> Allowed: Kotlin stdlib, `kotlinx.coroutines`, `javax.inject`, `core/common/Resource`. Forbidden: `android.*`, `androidx.*`, `retrofit2.*`, `com.google.gson.*`, `okhttp3.*`, `data/`, `feature/`.
   - `data/` -> Allowed: `domain/`, `core/common/`, `core/di/` qualifiers, Room, Retrofit, Gson, OkHttp, Hilt. Forbidden: `feature/`, Compose UI.
   - `feature/` -> Allowed: `domain/`, `core/ui/`, `core/common/`, Compose M3, Lifecycle, Hilt. Forbidden: `data/` (no DAO, Entity, DTO, API service, or `*RepositoryImpl`).
   - `core/di/` -> Allowed: wires `data/` -> `domain/` via Hilt. Forbidden: `feature/`.
   - `core/ui/` -> Allowed: Compose M3, Coil, `domain/model/` (for shared display components like `MealCard`). Forbidden: `data/`, feature ViewModels.
3. **Non-Negotiable Coding Rules** (bulleted checklist):
   - Strict 1:1 physical directory to Kotlin `package` declaration.
   - All stateful features follow UDF: `ViewModel` exposes `StateFlow<*UiState>` (never raw `Resource<T>`) to stateless, previewable `*Screen(uiState, onEvent)` Composables.
   - Zero `@Suppress` or `@SuppressWarnings`; always migrate deprecated APIs to official modern replacements.
   - Never hardcode `Dispatchers.IO` inside data sources; inject `@IoDispatcher`.
   - Never commit wrong-project copyright banners or dead XML/Fragment abstractions.
   - Every new mapper, repository, or ViewModel requires JVM unit tests in `app/src/test/`.
4. **Verification Commands** (exact copy-pasteable shell commands):
   ```bash
   ./gradlew assembleDebug testDebugUnitTest lintDebug
   ```
   Plus static sanity reminders: verify zero forbidden imports in `domain/` and zero `@Suppress` in `app/src/`.
5. **Relevant Android Skills Enforcement** (cleaned-up 5–6 line list):
   - `frogo-sdk`, `edge-to-edge`, `jetpack-compose-m3`, `android-cli`, `kotlin-coroutines-expert`, `r8-analyzer` (remove `piano-tiles` reference and irrelevant XML migration/billing items unless needed).

### 3.2 `ARCHITECTURE.md` Specification (Deep Reference & Feature Scaffolding)
Purpose: Single source of truth for human developers and AI agents navigating or extending the codebase.

Required sections (in exact order):
1. **Overview & Architecture Principles**:
   - Single-module Clean Architecture (`core/`, `data/`, `domain/`, `feature/`) aligned with Google's Guide to App Architecture and *Now in Android*.
   - Why single-module: fast starter clone & low ceremony; strict package boundaries enable painless future Gradle modularization (`:core:*`, `:feature:*`).
2. **Canonical Package Tree (ASCII)**:
   - Exact 1:1 tree matching the final restructured `app/src/main/java/io/github/amirisback/androidapp/` (including `ProjectApplication.kt`, `core/{common,di,ui/{component,theme}}`, `data/{local/{database,dao,entity},remote/{api,dto},mapper,repository}`, `domain/{model,repository,usecase}`, `feature/{main,favorite,detail,about}`) and `app/src/test/java/io/github/amirisback/androidapp/`.
3. **Layer Dependency Diagram & Import Boundary Table**:
   - Mermaid `flowchart TD` (or ASCII diagram) showing `feature -> domain <- data`, `core/di -> data & domain`, `feature -> core/ui & core/common`, `data -> core/common`.
   - Full Allowed vs Forbidden imports matrix.
4. **Naming Conventions Table**:
   | Artifact | Suffix / Pattern | Package |
   |---|---|---|
   | Pure domain entity | Noun (`Meal`, `Category`) | `domain/model/` |
   | Repository contract | `*Repository` | `domain/repository/` |
   | UseCase contract / impl | `*UseCase` / `*Interactor` | `domain/usecase/` |
   | Retrofit API interface | `*ApiService` | `data/remote/api/` |
   | API URLs & query constants | `*ApiConfig` | `data/remote/api/` |
   | Network JSON payload | `*Dto`, `*ResponseDto` | `data/remote/dto/` |
   | Room table | `*Entity` | `data/local/entity/` |
   | Room DAO | `*Dao` | `data/local/dao/` |
   | Room DB | `AppDatabase` | `data/local/database/` |
   | Data sources | `*RemoteDataSource`, `*LocalDataSource` | `data/remote/`, `data/local/` |
   | Model mappers | `*Mapper.kt` (`toDomain()`, `toEntity()`) | `data/mapper/` |
   | Repository implementation | `*RepositoryImpl` | `data/repository/` |
   | Feature UI state | `*UiState` | `feature/<name>/` |
   | Feature ViewModel | `*ViewModel` | `feature/<name>/` |
   | Feature Compose screen | `*Screen.kt` (stateful route + stateless content + `@Preview`) | `feature/<name>/` |
   | Activity host | `*Activity` | `feature/<name>/` |
   | Hilt DI module | `*Module` | `core/di/` |
5. **"Where to Put X" Quick Decision Table**:
   - At least 12 direct mappings (e.g., new REST endpoint, new Room table, JSON-to-domain conversion, shared Compose button/card, DI provider, coroutine dispatcher, screen state data class, unit test).
6. **Unidirectional Data Flow (UDF) Pattern**:
   - Diagram + short pattern rules: Activity/Route collects `viewModel.uiState.collectAsStateWithLifecycle()` -> passes immutable `*UiState` down and lambda callbacks up to stateless `*Screen` -> ViewModel mutates private `MutableStateFlow` via UseCase/Repository.
7. **"How to Add a New Feature" Step-by-Step Checklist**:
   1. Define pure Kotlin model in `domain/model/<Name>.kt` and contract in `domain/repository/<Name>Repository.kt` (+ optional `domain/usecase/`).
   2. Add DTO (`data/remote/dto/`), API endpoint (`data/remote/api/`), Room Entity/DAO (`data/local/` if cached), and mappers (`data/mapper/`).
   3. Implement `<Name>RemoteDataSource` / `<Name>LocalDataSource` (injecting `@IoDispatcher`) and `<Name>RepositoryImpl` (`data/repository/`).
   4. Wire bindings in `core/di/` (`NetworkModule`, `DatabaseModule`, `RepositoryModule`, `UseCaseModule`).
   5. Create `feature/<name>/` with `<Name>UiState.kt`, `<Name>ViewModel.kt`, `<Name>Screen.kt` (stateless + `@Preview`), and `<Name>Activity.kt` (if standalone entry).
   6. Register any new Activity in `app/src/main/AndroidManifest.xml` (only if not hosted in existing shell).
   7. Add JVM unit tests in `app/src/test/java/io/github/amirisback/androidapp/` covering mappers, repository, and ViewModel state transitions.
   8. Run `./gradlew assembleDebug testDebugUnitTest lintDebug` and confirm zero lint/warning suppressions and updated Room schema JSON (if DB changed).
8. **Testing & Verification Commands**:
   - Test structure mirroring main source tree (`data/mapper/`, `data/repository/`, `feature/<name>/`).
   - Prefer lightweight fake repositories/DAOs + `kotlinx-coroutines-test` (`runTest`, `UnconfinedTestDispatcher`/`StandardTestDispatcher`).
   - Commands: `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, `./gradlew lintDebug`.

### 3.3 `README.md` Specification (Human Quickstart)
Purpose: Clean, accurate landing page for developers cloning the starter template.

Required sections:
1. **Header & Overview**: Concise description of `init-android-apps` as a modern, Google-standard, AI-ready Android starter template (Jetpack Compose + Material 3 + Clean Architecture + Hilt + Room + Retrofit).
2. **Architecture Summary**: Brief 4-layer overview (`core/`, `data/`, `domain/`, `feature/`) with direct links to [ARCHITECTURE.md](ARCHITECTURE.md) and [AGENTS.md](AGENTS.md).
3. **Tech Stack Table** (exact current versions — zero legacy Glide/LiveData/ViewBinding/minSdk 21):
   - Min SDK `24` | Compile & Target SDK `37` | JVM `17`
   - AGP `9.5.1` | Kotlin `2.4.21` | KSP `2.3.14`
   - UI: Jetpack Compose (BOM `2026.09.00`) + Material 3 + Edge-to-Edge
   - DI: Hilt `2.60.1`
   - Persistence: Room `2.8.5` (with KSP schema export)
   - Networking: Retrofit + OkHttp + Gson + Chucker `4.4.0`
   - Images: Coil `3.6.3` (`coil-compose`, `coil-network-okhttp`)
   - Utilities/Monetization: Frogo SDK `4.0.0`
   - Testing: JUnit `4.13.2` + `kotlinx-coroutines-test` `1.10.2`
4. **Getting Started (Template Customization)**:
   - Update app name, `APP_DOMAIN`, `APP_PLAY_CONSOLE`, version, and AdMob IDs in `buildSrc/src/main/kotlin/ProjectSetting.kt` and `ProjectAds.kt`.
   - Update `namespace` in `app/build.gradle.kts` if changing root package.
5. **Build & Verification Commands**:
   ```bash
   ./gradlew assembleDebug
   ./gradlew testDebugUnitTest
   ./gradlew lintDebug
   ```
6. **Screenshots / License / Privacy Policy**: Preserve existing valid links/assets (`docs/image/...`, `LICENSE`, `PRIVACY-POLICY.md`).

---

## 4. Handoff Verification Checklist for Phase 4 (Hudio) & Phase 5 (Sandra)
- [ ] `gradle/libs.versions.toml` has `junit = "4.13.2"` and `kotlinx-coroutines-test = "1.10.2"`.
- [ ] `app/build.gradle.kts` removes `viewBinding = true` and adds `testImplementation` deps.
- [ ] Stale schemas (`com.frogobox...ProjectDatabase`, `io.github...domain.db.ProjectDatabase`) deleted; new `app/schemas/io.github.amirisback.androidapp.data.local.database.AppDatabase/1.json` generated by KSP.
- [ ] `app/proguard-rules.pro` includes `data.remote.dto.**` keep rule.
- [ ] `network_security_config.xml` sets `cleartextTrafficPermitted="false"` with zero `newsapi.org` reference; `AndroidManifest.xml` removes `usesCleartextTraffic="true"` and updates all Activity class paths.
- [ ] `core/di/` contains all 5 modules (`DatabaseModule`, `DispatcherModule`, `NetworkModule`, `RepositoryModule`, `UseCaseModule`) with `@Singleton` scopes fixed and zero `domain/ -> data/` leak in `MealInteractor`.
- [ ] `AGENTS.md` (<120 lines, zero `piano-tiles`), `ARCHITECTURE.md` (full tree + tables + feature checklist), and `README.md` (accurate stack) authored cleanly.
