# AGENTS.md

Panduan kerja untuk AI coding agent di repositori **Theo Console**.

## Identitas proyek

Theo Console adalah aplikasi Android native untuk membaca saldo dan melakukan
top-up akun TheoTown. Aplikasi harus tetap mandiri: seluruh fitur berjalan di
dalam proses Android tanpa runtime, layanan, atau proses tambahan di perangkat.

- Package: `com.aeeL.theokntl`
- Single module: `app`
- Kotlin 2.2.20, Jetpack Compose, Material 3, Hilt, Coroutines/Flow,
  Navigation Compose, DataStore, OkHttp, kotlinx.serialization
- minSdk 26, targetSdk 36, compileSdk 36, JDK 17
- Dokumentasi: Indonesia; komentar kode mengikuti bahasa di sekitarnya; nama API
  dan tipe tetap Inggris

## Aturan wajib

1. **Jangan menjalankan Gradle secara lokal.** Repositori tidak memiliki Gradle
   wrapper. Jangan menjalankan `gradle`, `./gradlew`, atau task Gradle apa pun.
   Build, lint, dan unit test diverifikasi oleh GitHub Actions setelah push.
2. **Jangan menambahkan Gradle wrapper** atau mengubah workflow menjadi build
   debug. Workflow sengaja hanya membangun varian release.
3. **Jangan commit secret.** Jangan menambahkan keystore, password, token,
   `local.properties`, log build, atau APK hasil build.
4. **Jangan menambahkan runtime atau proses eksternal** yang membuat aplikasi
   bergantung pada layanan di luar proses Android.
5. **Jangan mengubah konstanta wire** di `core/ConsoleConstants.kt` tanpa alasan
   kompatibilitas protokol yang jelas. Nilai tersebut adalah konstanta publik
   klien TheoTown, bukan secret akun.
6. **Jangan menghapus atau menimpa pekerjaan pengguna** yang belum terkait tugas.
   Periksa `git status` dan diff terlebih dahulu.
7. **Jangan menambahkan fitur yang menulis ke akun** tanpa konfirmasi eksplisit
   di UI. Top-up dan sinkron fitur adalah aksi nyata.

## Arsitektur

- `core/`: konstanta wire, crypto, error, formatter.
- `domain/`: model, validasi, top-up math, `FeatureSync`, gateway interface,
  dan use case.
- `data/`: OkHttp transport, `TheoClient`, repository, DataStore, dan log sesi.
- `di/`: modul Hilt.
- `ui/`: theme, ViewModel, navigasi, layar, komponen, ikon, dan preview.

Ikuti pola yang sudah ada:

- MVVM + UDF. State memakai `StateFlow`; event sekali pakai memakai
  `Channel`/`Flow`.
- Layar tidak mengakses jaringan atau DataStore langsung; gunakan use case dan
  ViewModel.
- Dependency injection memakai Hilt. Jangan membuat service locator manual.
- Operasi jaringan memakai Coroutines dan OkHttp; jangan memblokir main thread.
- Penyimpanan preferensi memakai DataStore, kecuali pilihan bahasa yang sudah
  memakai SharedPreferences (`theo_language`/`language`).
- Jangan menambahkan Room, WorkManager, atau database lokal tanpa kebutuhan
  yang jelas.

## Kontrak UI

Jaga perilaku berikut saat mengubah UI:

- Tipografi memakai **Roboto Flex** dengan hierarki bobot Thin–Black. Font
  variable diatur melalui `FontVariation.Settings`; jangan mengganti ke font
  sistem tanpa alasan.
- Ikon tab Top up adalah diamond, dengan varian filled dan outlined.
- Perubahan bahasa harus langsung terlihat tanpa `Activity.recreate()`.
  Gunakan `LocalConfiguration`/`LocalResources`; jangan memakai `LocalContext`
  untuk string yang harus mengikuti bahasa aplikasi.
- Top bar memenuhi area notch/cutout dengan sudut bawah membulat. Bottom bar
  memiliki sudut atas membulat.
- Swipe-to-confirm berubah dari merah ke hijau seiring progres ke kanan.
  Label dan thumb harus tetap kontras dan fill di-clip ke bentuk pill.
- Ripple/highlight kartu tools di console harus ter-clip ke bentuk kartu.
- Kartu loading Pembelian memakai warna `secondaryContainer`/
  `onSecondaryContainer`.
- Di tab Top up, swipe horizontal pager dinonaktifkan agar gestur dipakai untuk
  konfirmasi. Tombol kembali dari tab utama selain Beranda kembali ke Beranda.
- Jangan menambahkan tombol Apply/Cancel pada top-up; konfirmasi memakai swipe.
- Semua string UI harus melalui resource. Jangan hardcode teks yang terlihat
  pengguna.

## Lokalisasi

- File string: `app/src/main/res/values/strings.xml` (Inggris default),
  `app/src/main/res/values-en/strings.xml`, dan
  `app/src/main/res/values-id/strings.xml`.
- Jumlah key di ketiga file harus sama. Saat menambah atau mengubah key,
  perbarui ketiganya.
- Gunakan `stringResource` untuk Composable. Untuk Toast, share intent, atau
  pesan error di luar Composable, gunakan `Resources` yang sudah dilokalkan.
- Log protokol adalah data teknis dan tidak perlu diterjemahkan.

## Keamanan

- Password hanya boleh berada di memori dan dikirim tersegel. Jangan pernah
  menyimpannya ke DataStore, SharedPreferences, log, atau file.
- Sesi (id, encryption key, cake) hanya di memori; fingerprint pendek boleh
  ditampilkan.
- Jangan mencatat password, session key, atau isi `cake` ke log.
- Semua trafik harus HTTPS. Jangan mengaktifkan cleartext atau menambahkan
  endpoint kustom.
- Manifest hanya meminta izin `INTERNET`; `allowBackup` harus tetap `false`.

## Alur kerja

1. Baca `README.md`, `docs/ARCHITECTURE.md`, dan file yang relevan sebelum
   mengubah kode.
2. Cari implementasi yang sudah ada dengan `grep`/`glob`; jangan menduplikasi
   helper, model, atau string.
3. Buat perubahan sekecil mungkin dan konsisten dengan gaya sekitarnya.
4. Lakukan verifikasi statis (lihat bawah). Jangan mencoba build lokal.
5. Periksa `git diff --check` dan `git status --short`.
6. Commit dengan pesan konvensional, misalnya `feat(ui): ...`,
   `fix(data): ...`, atau `ci: ...`.
7. Push ke `main` hanya jika diminta atau jika alur kerja tugas memang
   mengharuskannya. CI akan memverifikasi setelah push.

## Verifikasi statis

Karena Gradle tidak boleh dijalankan lokal, lakukan pemeriksaan berikut:

- Baca ulang diff dan pastikan tidak ada perubahan yang tidak disengaja.
- Pastikan import, referensi resource, dan nama simbol konsisten.
- Pastikan key string di `values/`, `values-en/`, dan `values-id/` tetap sama.
- Cari referensi lama atau nama file yang sudah dihapus.
- Periksa sintaks YAML workflow secara manual.
- Jalankan `git diff --check`.
- Pastikan tidak ada APK, keystore, log, atau file build yang ikut ter-stage.

## CI/CD

- Workflow: `.github/workflows/build.yml`
- Trigger: push ke `main` dan `workflow_dispatch`.
- Perintah: `gradle lintRelease testReleaseUnitTest assembleRelease`.
- Hanya satu varian build: release. Jangan menambahkan build debug ke CI.
- ABI native dibatasi ke `armeabi-v7a` dan `arm64-v8a`.
- Artifact: `theo-console`, berisi `theo-console.apk` (retensi 14 hari).
- UI test instrumented tidak dijalankan di CI karena tidak ada emulator.
- Signing release memakai secret `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
  `KEY_ALIAS`, dan `KEY_PASSWORD`; tanpa keystore, build memakai debug signing.

## Dokumentasi

- `README.md`: ringkasan aplikasi, build CI, signing, fitur, dan struktur.
- `docs/ARCHITECTURE.md`: arsitektur, protokol, UI, keamanan, versi, test, dan
  CI.
- `AGENTS.md`: aturan kerja agent ini.
- Jaga dokumentasi tetap sesuai perilaku kode. Jangan menambahkan catatan
  riwayat atau referensi ke proyek lain yang tidak lagi menjadi bagian dari
  aplikasi.
