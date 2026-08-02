# Agent Project Guidelines & Android Skills Enforcement

## Instruksi Penggunaan Skill Android
Setiap kali mengembangkan, memodifikasi, merombak (*refactoring*), mengoptimalkan, atau menguji proyek Android **piano-tiles** ini, Agent WAJIB secara aktif membaca dan menerapkan petunjuk dari **Skills Agent Android** yang relevan sebelum melakukan perubahan kode:

### 1. Pengembangan & Arsitektur UI
- **`frogo-sdk`**: Gunakan panduan dan komponen Frogo SDK untuk pengembangan UI (Compose/XML), integrasi iklan (AdMob), helper RecyclerView, dan utilitas Android core.
- **`edge-to-edge`**: Terapkan panduan adaptif edge-to-edge untuk memastikan komponen UI tidak tertutup status bar, navigation bar, atau IME/keyboard inset.
- **`migrate-xml-views-to-jetpack-compose`**: Ikuti alur kerja terstruktur saat melakukan migrasi layout dari legacy XML View ke Jetpack Compose.
- **`jetpack-compose-m3`** & **`android-jetpack-compose-expert`**: Gunakan praktik terbaik Material Design 3 dan pengoptimalan performa Jetpack Compose.

### 2. Navigasi & Monetisasi
- **`navigation-3`**: Gunakan panduan Jetpack Navigation 3 untuk manajemen navigasi halaman, deep link, multiple backstack, dan scene transitions.
- **`play-billing-library-version-upgrade`**: Ikuti panduan pembaruan Google Play Billing Library (PBL) saat menangani in-app purchase atau langganan.

### 3. Build Optimization & CLI Operations
- **`android-cli`**: Gunakan skill `android-cli` untuk otomasi perintah Android CLI, pengujian, deployment, emulator, dan diagnosa lingkungan pengembangan.
- **`agp-9-upgrade`**: Ikuti aturan & panduan migrasi saat memperbarui Android Gradle Plugin (AGP) ke versi 9.
- **`r8-analyzer`**: Analisis file build dan Proguard/R8 keep rules untuk mengeliminasi aturan redundan dan meminimalkan ukuran APK.

### 4. Performa & Asinkron
- **`kotlin-coroutines-expert`**: Gunakan praktik terbaik penanganan Coroutines, Flow, dan operasi asinkron tanpa memblokir Main UI Thread.
- **`diagnose-android-overheating`**: Periksa dan hindari kebocoran memori (*memory leak*) atau pemakaian CPU tinggi yang dapat menyebabkan perangkat cepat panas.

---

## Prosedur Eksekusi Agent
1. **Periksa Skill**: Sebelum mulai menulis atau merombak kode, identifikasi skill Android di atas yang relevan dengan tugas.
2. **Baca `SKILL.md`**: Gunakan `view_file` pada `SKILL.md` dari skill yang relevan untuk membaca dokumentasi & petunjuk teknisnya.
3. **Patuhi Panduan**: Ikuti standar coding, arsitektur, dan keamanan yang tertera pada skill tersebut secara konsisten.
