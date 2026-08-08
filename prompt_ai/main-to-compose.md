# ROLE & SKILL

Anda adalah seorang AI Agent Android Developer Senior yang memiliki spesialisasi (skill) dalam memigrasikan sistem UI lama (XML/View-based) ke Jetpack Compose modern. Anda menguasai best practices Android, arsitektur MVVM/MVI, State Management, Kotlin Coroutines, dan optimasi performa UI.

# TUJUAN

Saya ingin memigrasikan komponen UI Android dari View System (XML) ke Jetpack Compose menggunakan skill/tools yang tersedia di sistem ini, dengan mengikuti standar arsitektur modern (MVI/MVVM), Material Design 3, serta memastikan state management terpisah dari UI.

# INPUT DATA

Skill yang wajib di gunakan adalah sebagai berikut:

- .agents\skills\jetpack-compose-m3
- .agents\skills\migrate-xml-views-to-jetpack-compose
- .agents\skills\styles
- .agents\skills\edge-to-edge

Gunakan skill pembaca file / workspace untuk mengambil source code berikut:

1. File Layout XML:

- app\src\main\res\layout\activity_about_us.xml

1. File Activity/Fragment Terkait:

- app\src\main\java\io\github\amirisback\androidapp\ui\about\AboutUsActivity.kt

# INSTRUKSI MIGRASI

Mohon proses file di atas dan buatkan kode Jetpack Compose dengan ketentuan sebagai berikut:

1. Komponen UI & Layouting:
   - Konversikan ViewGroup (ConstraintLayout, LinearLayout, RelativeLayout) ke Composable yang setara (Box, Column, Row, LazyColumn, atau ConstraintLayout Compose jika sangat kompleks).
   - Gunakan komponen Material Design 3 (Button, OutlinedTextField, Card, Text, dll.).
   - ConstraintLayout XML -> ConstraintLayout Compose (hanya jika kompleks) atau optimalkan menggunakan Row/Column/Box standar.
   - RecyclerView -> LazyColumn / LazyRow.
   - ImageView -> AsyncImage (Coil) jika memuat gambar dari URL.

2. Styling & Resources:
   - Gunakan `stringResource()`, `painterResource()`, dan `dimensionResource()` untuk menjaga modularitas resource.
   - Sesuaikan warna dan tipografi menggunakan objek `MaterialTheme`.
   - Styling & Themes: Gunakan token dari `MaterialTheme` (color, typography, shapes) alih-alih hardcoded colors/dimens dari XML, kecuali jika saya sebutkan lain.

3. State & Event Handling:
   - UI harus bersifat Stateless. Pisahkan State dan Event.
   - Buat parameter lambda untuk event handling (misal: `onButtonClicked: () -> Unit`).
   - Integrasikan dengan StateFlow/LiveData dari ViewModel yang ada di file Activity/Fragment asal (gunakan `collectAsStateWithLifecycle()`).
   - State Management: Ubah semua state UI yang sebelumnya diatur manual (misal: setText(), setVisibility()) menggunakan `MutableState`, `remember`, atau `collectAsStateWithLifecycle()` dari ViewModel jika ada.
   - Unidirectional Data Flow (UDF): Pastikan Composable bersifat stateless (menggunakan State Hoisting) di mana event dikirim ke atas (callbacks) dan data mengalir ke bawah.

4. Preview:
   - Sediakan `@Preview` fungsi Composable, lengkap dengan `ShowBackground = true` dan tema default-nya.

5. Performa: Hindari recomposition yang tidak perlu. Gunakan `remember` untuk objek yang berat dan `derivedStateOf` jika ada kalkulasi state turunan.

# OUTPUT YANG DIHARAPKAN

Berikan output berupa full code untuk file Composable baru (`.kt`), serta berikan panduan singkat jika ada dependensi Gradle baru yang perlu ditambahkan atau perubahan minor yang harus saya lakukan di sisi ViewModel/Activity.

---

# STRUCTURE FOLDER

com.example.myapp/
│
├── data/                         # Data Layer (Agnostik terhadap UI/Compose)
│   ├── model/                    # Data models (DTO, Entity)
│   ├── repository/               # Implementasi Repository
│   └── source/                   # Local (Room) & Remote (Retrofit/Ktor) data sources
│
├── domain/                       # Domain Layer (Opsional, untuk business logic kompleks)
│   ├── model/                    # Domain/Business models
│   └── usecase/                  # Use Cases / Interactors
│
├── ui/                           # UI Layer (Tempat Jetpack Compose berada)
│   ├── components/               # Global/Shared Composables (Reusable UI)
│   │   ├── CustomButton.kt
│   │   └── LoadingScreen.kt
│   │
│   ├── theme/                    # Design System Tokens (Aksesibilitas global)
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   │
│   ├── features/                 # Fitur Utama Aplikasi (Feature-by-Package)
│   │   ├── home/
│   │   │   ├── HomeScreen.kt     # Composable utama untuk fitur Home
│   │   │   ├── HomeViewModel.kt  # State holder untuk Home
│   │   │   ├── HomeUiState.kt    # Data class / Sealed interface penampung State
│   │   │   └── components/       # Composable lokal yang hanya dipakai di Home
│   │   │       └── HomeHeader.kt
│   │   │
│   │   └── detail/
│   │       ├── DetailScreen.kt
│   │       └── DetailViewModel.kt
│   │
│   └── navigation/               # Navigasi Aplikasi (Compose Navigation)
│       ├── NavGraph.kt           # Setup NavHost dan composable destinations
│       └── Destinations.kt       # Definisi rute/screen (bisa menggunakan Type-Safe Navigation)
│
└── MainActivity.kt               # Entry point aplikasi (Entry Point untuk Scaffold/NavHost)

# ANDROID APP ARCHITECTURE

1. File State, ViewModel, dan Screen Berdampingan
Di Jetpack Compose, UI digerakkan oleh State (UiState). Menyatukan HomeScreen.kt, HomeViewModel.kt, dan HomeUiState.kt di dalam folder fitur yang sama (ui/features/home/) membuat kode jauh lebih mudah dirawat (highly cohesive). Saat Anda mengerjakan fitur Home, Anda tidak perlu melompat-lompat folder dari ujung atas ke ujung bawah proyek.

2. Pemisahan Komponen Global vs Lokal
ui/components/: Berisi komponen UI generik yang digunakan di banyak layar, seperti custom button, loading spinner, atau error dialog.

ui/features/[nama_fitur]/components/: Berisi komponen yang sangat spesifik dan hanya masuk akal jika berada di layar tersebut. Ini mencegah folder global menjadi terlalu penuh.

1. Paket theme/ yang Sentralized
Saat Anda membuat proyek baru di Android Studio dengan template Compose, folder ui/theme otomatis dibuat. Tetap pertahankan folder ini karena ia menyimpan konfigurasi MaterialTheme (Warna, Tipografi, dan Bentuk) yang membungkus seluruh aplikasi Anda di MainActivity.

2. Layer Data dan Domain Tetap Bersih dari Compose
Perlu diingat bahwa Jetpack Compose hanyalah toolkit UI. Folder data/ dan domain/ Anda sama sekali tidak boleh mengimpor library Compose (androidx.compose.*). Mereka murni berisi Kotlin standard/coroutine agar kode backend aplikasi Anda tetap bisa diuji (testable) secara independen.
