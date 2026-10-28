# Arsitektur — Theo Console

Dokumen ini menjelaskan struktur, perilaku, dan keputusan teknis aplikasi Android
Theo Console. Aplikasi berjalan mandiri di perangkat; seluruh alur utama
ditangani di dalam proses Android tanpa runtime atau layanan tambahan.

## 1. Ringkasan

- Package: `aeeL.theoSol`
- minSdk 26, targetSdk 36, compileSdk 37, JDK 17
- Single module `app`
- Kotlin 2.4.21, Jetpack Compose, Material 3, Hilt, Coroutines/Flow,
  Navigation Compose, DataStore, OkHttp, kotlinx.serialization
- MVVM + UDF dengan pemisahan `ui/`, `domain/`, `data/`, dan `core/`
- Hanya meminta izin `INTERNET`; tidak ada storage permission, notifikasi, atau
  proses latar belakang

## 2. Arsitektur aplikasi

### Layer

- `core/`: konstanta wire publik, `CryptoEngine`, error, formatter, dan helper
  protokol.
- `domain/`: model, validasi nominal, matematika top-up, status fitur,
  `FeatureSync`, gateway interface, dan use case.
- `data/`: OkHttp transport, `TheoClient`, `ConsoleRepository`, DataStore,
  penyimpanan preferensi, dan log sesi.
- `ui/`: Compose + Material 3, theme, navigasi, ViewModel, layar, komponen,
  ikon, dan preview.
- `di/`: modul Hilt.

### State dan event

- `StateFlow` menyimpan state layar; event sekali pakai memakai `Channel`/`Flow`.
- Hasil top-up dikirim sebagai `Channel<TopUpOutcome>` agar layar sukses tidak
  terbuka ulang saat recomposition.
- ViewModel memanggil use case; layar tidak mengakses jaringan atau DataStore
  secara langsung.
- Operasi jaringan memakai Coroutines dan OkHttp. Pembatalan mengikuti
  `viewModelScope`.

### Dependency injection dan penyimpanan

- Hilt menyediakan `@HiltAndroidApp`, `@HiltViewModel`, dan binding pada
  `SingletonComponent`.
- Preferences DataStore menyimpan username opsional, tema/palet, dan cap waktu
  penulisan pembelian.
- Pilihan bahasa disimpan di SharedPreferences (`theo_language`/`language`) dan
  diterapkan saat cold start melalui `MainActivity.attachBaseContext()`.
- Room, WorkManager, dan database lokal tidak dipakai.

### Navigasi

- Navigation Compose menangani halaman detail, sedangkan tiga tab utama memakai
  `HorizontalPager` dan bottom bar yang tersinkron dua arah.
- Tab utama: Beranda, Top up, dan Akun.
- Pager tetap dapat digeser di semua tab; swipe-to-confirm mengonsumsi gestur
  horizontal hanya di area kontrolnya.
- Halaman Pengaturan dan detail selalu dapat kembali ke tab pager yang sedang
  aktif. Bottom bar hanya tampil di tab utama; tombol kembali mengembalikan ke
  halaman sebelumnya, dan detail tingkat pertama kembali ke `Routes.MAIN`
  tanpa menumpuk halaman baru.
- Tombol kembali sistem dari tab utama selain Beranda mengembalikan pengguna ke
  Beranda.

## 3. Protokol dan data

- Endpoint: `https://api.theotown.com`
- Path: `/user/verify_fb.php`, `/user/events.php`, `/user/purchases.php`
- Transport: HTTP form-urlencoded melalui OkHttp, timeout 20 detik.
- Enkripsi payload: AES-128-CBC; integritas memakai MD5 + salt; respons dapat
  berupa gzip dan dideteksi dari magic bytes, bukan header.
- Autentikasi: login forum, lalu sesi terenkripsi dipakai untuk event dan
  pembelian.
- Field user pada verifikasi forum mempertahankan urutan JSON yang diharapkan
  server.
- Form body memakai encoder bergaya `URLEncoder` Java: spasi menjadi `+`, `~`
  menjadi `%7E`, dan karakter `.-*_` tidak di-encode.
- Respons terenkripsi diverifikasi dengan MD5 + response salt secara
  timing-safe. Kegagalan dipetakan menjadi error signature atau sesi
  kedaluwarsa.
- Konstanta wire di `core/ConsoleConstants.kt` adalah nilai publik klien
  TheoTown 1.12.95a, bukan secret akun. Jangan mengubahnya tanpa alasan
  kompatibilitas protokol.

### Top up

- Target dihitung dari `max(earned, spend) + amount - spend` untuk
  menormalisasi penghitung event.
- Nominal ditolak jika melebihi `INT32_MAX`; saldo dibatasi 100.000.
- Setelah event ditulis, snapshot dibaca ulang. Status `capped` muncul jika
  saldo akhir lebih rendah dari target.
- Nilai target tetap dipakai untuk validasi dan protokol, tetapi tidak
  ditampilkan sebagai baris UI. Ringkasan dan layar sukses hanya menampilkan
  before/amount/after.
- Penulisan adalah aksi nyata ke akun. Tidak ada undo otomatis.

### Pembelian dan sinkron fitur

- Baca pembelian terlebih dahulu; `add purchase` hanya ditulis untuk baris yang
  belum terlihat.
- Cap waktu per akun membuat sync berulang idempoten selama 12 jam.
- Settle 1,4 detik, lalu watch pada 4, 11, 30, dan 60 detik.
- Jika jalur gagal, sesi di-refresh sekali, kemudian fallback ke `max purchase`.
- Probe baris uji membedakan filter server dari akun yang belum mengembalikan
  baris.
- Kegagalan non-pembatalan dikembalikan sebagai laporan `error`, bukan crash.

## 4. UI

### Tema dan tipografi

- Material 3 stabil dengan ekspresi visual melalui skala tipografi, shape
  membulat, palet aksen, dan motion kartu/hero.
- **Roboto Flex** variable font dibundel di
  `app/src/main/res/font/roboto_flex.ttf` dan dipakai untuk seluruh skala
  Material 3 dengan bobot Thin–Black.
- Lima palet aksen: Teal, Tangerine, Grape, Ocean, dan Rose. Masing-masing
  memiliki varian terang/gelap.
- Semua role `surfaceContainer*`, `surfaceDim`, `surfaceBright`,
  `outlineVariant`, `inverse*`, dan `scrim` diisi konsisten agar kartu, bar, dan
  kontrol tidak memakai warna permukaan campuran.
- Dynamic color hanya tersedia pada Android 12+ dan default mati. Memilih palet
  otomatis mematikan dynamic color; kontrolnya disembunyikan pada Android < 12.

### Edge-to-edge dan inset

- Status bar disembunyikan dengan `WindowInsetsControllerCompat.hide()` dan
  `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. Window tetap menggambar di belakang
  navigation bar dan display cutout (`layoutInDisplayCutoutMode=ALWAYS` pada
  API 30+, `SHORT_EDGES` pada API 28–29).
- Top bar setinggi 60dp hanya memakai inset cutout horizontal; inset cutout atas
  tidak ditambahkan agar bar tidak turun dan area notch tetap terisi. Status bar
  tidak menambah padding kedua. Bottom bar dan layar sukses memakai inset
  sistem masing-masing satu kali; background tetap memenuhi layar.
- Top bar menggunakan surface penuh hingga area cutout dengan sudut bawah
  membulat; bottom bar memiliki sudut atas membulat.

### Bahasa

- Pilihan bahasa: ikuti sistem, Inggris, dan Indonesia. Opsi sistem membaca
  locale perangkat dari `Resources.getSystem()`, bukan konfigurasi activity
  yang sudah di-override.
- Perubahan bahasa langsung memperbarui `LocalConfiguration`, `LocalResources`,
  dan `LocalLayoutDirection` tanpa recreate activity.
- Pilihan tersimpan dan diterapkan saat cold start melalui
  `attachBaseContext()`.
- `locales_config.xml` dan AppCompat per-app locale tidak dipakai agar sumber
  kebenaran bahasa hanya satu.
- String Inggris tersedia di `values/` dan `values-en/`; string Indonesia di
  `values-id/` dan `values-in/`. `values-in/` adalah salinan kompatibilitas
  untuk API < 35, yang masih memakai kode locale lama `in`; `values-id/` dipakai
  pada API 35 ke atas. Jumlah key keempat file dijaga sama.
- Nominal, saldo, dan batas di UI mengikuti locale aktif. Log protokol dan
  nilai mesin tetap memakai format stabil.
- Log protokol adalah data teknis dan tidak diterjemahkan.

### Layar

- Beranda, Login, Top up, Sukses top up, Akun, Protokol, Bantuan, Pengembang,
  Pengaturan, dan Aktivitas.
- Akun menggabungkan identitas dan detail sesi: status, waktu masuk, aksi
  terakhir, fingerprint kunci, endpoint, dan catatan keamanan. Tidak ada layar
  sesi terpisah.
- Pembelian tidak memiliki tab terpisah. Setelah top-up, fitur Free fast speed
  dan Free tunnels dibaca dan disinkronkan otomatis; Beranda menyediakan aksi
  sinkron manual.
- Sapaan Beranda mengikuti jam: pagi 04.00–10.59, siang 11.00–14.59, sore
  15.00–17.59, malam 18.00–03.59.
- Top up memakai kontrol swipe-to-confirm; tidak ada tombol Apply atau Cancel.
  Offset thumb mengikuti drag secara langsung, animasi hanya untuk snap-back
  atau penyelesaian.
- Swipe pertama selalu membuka tinjauan. Swipe berikutnya menulis. Mengubah
  nominal membatalkan tinjauan.
- Setelah tulis berhasil, layar sukses terpisah menampilkan check orb dengan
  ring pulse/rotasi halus di sekeliling ikon, reveal sekali, ringkasan
  before/amount/after, status capped, dan laporan fitur. Tidak ada gradient atau
  confetti dekoratif. Layar ini menyembunyikan top bar/bottom bar dan mendukung
  tombol kembali.
- Transisi navigasi memakai slide horizontal + fade sekitar 260ms
  (`FastOutSlowInEasing`); layar sukses memakai fade + scale halus.
- Layout responsif memakai `BoxWithConstraints` dengan breakpoint 720dp untuk
  dua kolom; `WindowSizeClass` eksperimental tidak dipakai.
- Loading, error, dan empty state disediakan. Aksi yang menulis memakai swipe
  atau dialog konfirmasi.
- Log aktivitas memakai `LazyColumn`, timestamp, level, salin JSON, bagikan, dan
  bersihkan.
- Preview Compose tersedia untuk layar utama, termasuk layar sukses top-up.

## 5. Keamanan dan privasi

- Password hanya dikirim tersegel (RSA PKCS#1 v1.5 + mask MD5) dan tidak pernah
  disimpan di disk.
- Sesi (id, encryption key, cake) hanya berada di memori; fingerprint MD5 8
  karakter boleh ditampilkan.
- `allowBackup=false`, cleartext dimatikan, dan hanya izin `INTERNET` yang
  diminta.
- Log tidak memuat password, session key, atau isi `cake`.
- DataStore hanya menyimpan username opsional dan cap waktu pembelian.
- Tidak ada endpoint kustom; semua trafik menuju host TheoTown melalui HTTPS.

## 6. Batasan dan keputusan

- Kontrak API mengikuti klien TheoTown 1.12.95a/build 11295. Jika server
  berubah, error protokol tampil di layar Aktivitas.
- Angka `earned`/`spend` ditampilkan sebagai `Long` agar pengurangan tidak
  overflow, tetapi nilai tulis event tetap dibatasi `INT32_MAX`.
- Jika proses aplikasi dihentikan saat sync, server tetap konsisten dan sync
  berikutnya idempoten.
- Tidak ada notifikasi atau sinkronisasi otomatis di latar belakang.
- Build, lint, dan unit test hanya dijalankan di GitHub Actions. Repositori
  tidak memiliki Gradle wrapper dan tidak di-build di perangkat.
- Semua akses jaringan, penulisan event, dan pembelian berjalan di dalam
  aplikasi.

## 7. Versi dan kompatibilitas

| Komponen | Versi | Alasan |
| --- | --- | --- |
| AGP | 9.4.1 | Stabil, mendukung compileSdk 37 dan Gradle 9.8.x |
| Gradle | 9.8.2 | Minimum AGP 9.4 adalah 9.6.0 |
| JDK | 17 (Temurin) | Didukung AGP 9.4 |
| Kotlin | 2.4.21 | KGP bawaan AGP ditingkatkan lewat buildscript |
| KSP | 2.3.12 | Versi terpisah dari Kotlin, mendukung AGP 9 |
| Hilt | 2.60.1 | Stabil, mendukung KSP dan AGP 9 |
| Compose BOM | 2026.09.00 | Compose UI 1.12.1 + Material3 1.4.0 yang kompatibel |
| Material3 | 1.4.0 (via BOM) | Stabil; ekspresi visual dibangun di atas API stabil |
| Roboto Flex | Variable font bundled | Tipografi ekspresif, lisensi OFL disertakan |
| AppCompat | 1.8.0 | Host theme `Theme.AppCompat.DayNight.NoActionBar` |
| Navigation Compose | 2.10.2 | Stabil |
| OkHttp | 5.5.1 | Stabil, mendukung Android 8+ |
| kotlinx.serialization | 1.12.0 | JSON dan state pembelian |
| DataStore | 1.3.0 | Stabil |
| minSdk / targetSdk / compileSdk | 26 / 36 / 37 | Kebijakan proyek |

## 8. Pengujian

- Unit test JVM di `app/src/test/`:
  - validasi nominal (kosong, bukan bilangan, nol, melebihi ruang, pemisah),
  - matematika tinjauan top-up dan overflow,
  - status fitur dan verifikasi,
  - crypto: MD5 vector, AES-CBC vector NIST, segel password,
  - `FeatureSync` dengan gateway palsu: idempoten, watch, refresh, fallback,
    dan probe,
  - batas periode sapaan Beranda.
- UI test Compose di `app/src/androidTest/` tersedia untuk verifikasi manual di
  emulator; CI release-only tidak menjalankannya.
- Lint dan R8/shrinkResources berjalan pada varian release.
- Verifikasi lokal hanya statis: pembacaan kode, import, referensi resource,
  manifest, versi dependency, dan sintaks YAML. Jangan menjalankan Gradle lokal.

## 9. CI/CD

- Workflow: `.github/workflows/build.yml`
- Trigger: push ke `main` dan `workflow_dispatch`.
- Runner Ubuntu, JDK 17, Gradle 9.8.2 melalui `gradle/actions/setup-gradle`,
  cache aktif, dan `concurrency` membatalkan build lama.
- Langkah: checkout → JDK → Gradle → SDK platform 37.0 → signing opsional →
  `gradle lintRelease testReleaseUnitTest assembleRelease` → upload
  `theo-console.apk` sebagai artifact `theo-console`.
- Hanya varian release yang dibangun; ABI native dibatasi ke `armeabi-v7a` dan
  `arm64-v8a`.
- Signing release memakai secret `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
  `KEY_ALIAS`, dan `KEY_PASSWORD`; jika keystore tidak ada, build memakai debug
  signing.
- `permissions: contents: read`.
