# Rencana Porting — Theo Console

Dokumen ini adalah hasil analisis CLI `theotown-console` (Node.js 18+) dan rencana
porting ke aplikasi Android native. Semua perilaku diambil dari source CLI di
`cli_src/` yang disertakan di working directory. Source CLI tidak ikut di-commit
ke repositori Android (lihat `.gitignore`).

## 1. Ringkasan CLI

- Entry point: `cli.js`
- Mode: interaktif (TTY, slash command) dan non-interaktif (argumen atau stdin).
- Transport: HTTP form-urlencoded ke `https://api.theotown.com`.
- Protokol: AES-128-CBC, tanda tangan MD5 + salt, body gzip opsional.
- Autentikasi: login forum (`/user/verify_fb.php`) lalu sesi terenkripsi.
- Data akun: event `earned`/`spend` untuk saldo, baris pembelian untuk fitur.
- Penyimpanan lokal: file JSON (`state.json`) berisi username dan cap waktu
  penulisan pembelian. Sesi dan kata sandi tersegel hanya di memori.

## 2. Pemetaan command

| Command CLI | Padanan Android | Layer / UseCase |
| --- | --- | --- |
| `login [nama]` | `LoginScreen` (form username, password, switch "ingat username") | `LoginUseCase` → `ConsoleRepository.login()` |
| `topup <jumlah>` | `TopUpScreen` (input nominal, kartu tinjauan before/amount/after, kontrol swipe TWRP-style) lalu `TopUpSuccessScreen` | `ReviewTopUpUseCase`, `ApplyTopUpUseCase` |
| `purchases` | `PurchasesScreen` (status Free fast speed & Free tunnels) | `ReadPurchasesUseCase` |
| `refresh` | Aksi "Baca ulang" di Beranda dan Top up | `RefreshBalanceUseCase` |
| `sync` | Aksi "Sinkron fitur" di Beranda, Top up, dan Pembelian | `SyncFeaturesUseCase` → `FeatureSync` |
| `signout` | Tombol di `AccountScreen` dengan dialog konfirmasi | `SignOutUseCase` |
| `account` | `AccountScreen` (identitas + detail sesi) | `ConsoleState.account` |
| `session` | `SessionScreen` (status, fingerprint kunci, endpoint) | `ConsoleState.session` |
| `status` | `HomeScreen` (saldo, progres, sisa ruang) | `ConsoleState.stats` |
| `protocol` | `ProtocolScreen` (tabel konstanta + nilai mentah) | `ProtocolInfo` |
| `developer` | `DeveloperScreen` (nama + tautan Telegram) | `DeveloperView` |
| `help` | `HelpScreen` (peta command dan opsi) | Resource statis |
| `clear` | Tidak di-port: hanya masuk akal di terminal. Android memakai navigasi dan sistem task. | — |
| `exit` | Tidak di-port: siklus hidup aplikasi ditangani sistem (back/task switcher). | — |
| `redeem` | **Sengaja tidak di-port** (lihat bagian 8). | — |

### Opsi CLI → UI

| Opsi / perilaku CLI | Padanan Android |
| --- | --- |
| `--user <nama>` | Username tersimpan menjadi prefill di layar login |
| `--yes` | Switch "Terapkan tanpa tinjauan": satu swipe langsung menulis tanpa kartu tinjauan |
| `--json` | Tombol "Salin JSON" dan "Bagikan" di layar Aktivitas |
| `--no-color` | Tema aplikasi (terang/gelap/sistem), palet aksen, dan tanpa warna ANSI |
| `--debug` | Switch "Detail teknis" di Pengaturan (pesan error teknis) |
| `--help`, `-h` | Layar Bantuan |
| `--version`, `-v` | Baris versi di Pengaturan dan Bantuan |
| `THEOTOWN_PASSWORD` | Tidak berlaku; password diketik di form dan tidak disimpan |
| `NO_COLOR` | Tidak berlaku; warna mengikuti tema Material 3 |
| Perintah dari stdin | Diganti aksi UI; tidak ada pipe/script di Android |
| Riwayat perintah ↑/↓ | Tidak di-port; navigasi memakai bottom bar dan tombol kembali |
| `Ctrl+C` / `Esc` saat proses | Tombol "Hentikan" pada operasi yang berjalan (pembatalan coroutine) |
| `clear` | Tidak di-port (lihat tabel command) |

## 3. Arsitektur

- Single module `app`, package `com.aeeL.theokntl`.
- Clean Architecture ringan:
  - `domain/`: model, validasi nominal, matematika top-up, status fitur,
    `FeatureSync` (orchestrasi pembelian dengan dependency interface), use case.
  - `data/`: OkHttp transport, klien protokol terenkripsi, DataStore,
    `ConsoleRepository`, log sesi, implementasi gateway/state store.
  - `ui/`: Compose + Material 3 dengan tipografi, shape, dan motion ekspresif,
  lima palet aksen, bahasa aplikasi, navigasi, ViewModel, theme.
- MVVM + UDF: `StateFlow` untuk state, `Channel`/`Flow` untuk event sekali pakai.
  Event sukses top-up memakai `Channel<TopUpOutcome>` agar layar sukses tidak
  dibuka ulang saat recomposition.
- DI: Hilt (`@HiltAndroidApp`, `@HiltViewModel`, modul `SingletonComponent`).
- Async: Coroutines + Flow. Semua I/O jaringan di `Dispatchers.IO`/OkHttp.
- Penyimpanan: Preferences DataStore untuk username, tema/palet, dan cap waktu
  penulisan pembelian. Bahasa disimpan di SharedPreferences
  (`theo_language`/`language`) lalu diterapkan lewat
  `MainActivity.attachBaseContext()`. Room tidak diperlukan.
- Navigasi: Navigation Compose untuk halaman detail, bottom bar, dan
  `HorizontalPager` untuk empat tab utama. Halaman Pengaturan/detail selalu
  kembali ke tab pager yang sedang dipilih.
- Tidak ada dependency npm yang di-port; tidak ada proses anak.

### Pemetaan library

| CLI (Node) | Android/Kotlin |
| --- | --- |
| `fetch`, `node:http` | OkHttp 4.12.0 |
| `node:crypto` (MD5, AES, RSA) | `MessageDigest`, `Cipher` (`AES/CBC/PKCS5Padding`), `BigInteger.modPow`, `SecureRandom` |
| `node:zlib` | `GZIPInputStream` |
| `readline` / TTY | Compose TextField, Switch, dialog, tombol |
| file `state.json` | Preferences DataStore |
| ANSI reporter/spinner | Kartu status + `LazyColumn` log bergaya terminal |
| `Intl.NumberFormat` | `String.format(Locale.US, "%,d")` agar pemisah ribuan sama |
| Font sistem | **Roboto Flex** variable font yang dibundel di `res/font/`, diterapkan pada seluruh skala tipografi Material 3 |

## 4. Perilaku protokol yang dipertahankan

- Urutan field JSON user saat verifikasi forum dipertahankan.
- Konstanta wire, salt, IV, client id/build, model, dan endpoint tidak diubah.
- Form body memakai encoder ala `URLEncoder` Java (spasi `+`, `~` menjadi `%7E`,
  huruf/angka/`.-*_` tidak di-encode).
- Respons terenkripsi diverifikasi dengan MD5 + response salt secara
  timing-safe; kegagalan → error signature/sesi kedaluwarsa.
- Body gzip dideteksi dari magic bytes, bukan dari header.
- Timeout permintaan 20 detik.
- Top-up:
  - target = `max(earned, spend) + amount - spend` (normalisasi penghitung),
  - ditolak bila melebihi `INT32_MAX`,
  - batas saldo 100.000,
  - setelah tulis event, snapshot dibaca ulang; `capped` bila saldo < target.
  - Nilai `target` tetap dipakai untuk validasi/protokol, tetapi tidak lagi
    ditampilkan sebagai baris UI. Ringkasan Top up, riwayat top-up terakhir, dan
    layar sukses hanya menampilkan before/amount/after agar tidak membingungkan.
- Pembelian fitur:
  - baca dulu, tulis `add purchase` hanya untuk baris yang belum terlihat,
  - cap waktu per akun agar sync berulang idempoten (12 jam),
  - settle 1,4 detik; watch 4/11/30/60 detik,
  - refresh sesi sekali pada jalur gagal, lalu fallback `max purchase`,
  - probe baris uji untuk membedakan filter server vs akun yang belum
    mengembalikan baris,
  - kegagalan non-pembatalan dikembalikan sebagai laporan `error`, bukan crash.
- Sesi dan password tersegel hanya di memori; sign out menghapusnya.
- Username dan cap waktu pembelian boleh persisten; password tidak pernah.

## 5. Batasan Android dan alternatif

| Keterbatasan | Alternatif yang dipakai |
| --- | --- |
| TTY interaktif, raw mode, riwayat perintah | Form Compose, bottom bar, tombol kembali, dialog konfirmasi |
| `stdin`/`stdout` pipe dan `--json` | Aksi UI; log dapat disalin/dibagikan sebagai JSON |
| Env var `THEOTOWN_PASSWORD` | Password diketik di form; tidak disimpan |
| Akses filesystem bebas | DataStore aplikasi (scoped storage), tanpa izin storage |
| Proses anak / shell | Tidak ada di CLI; tidak diperlukan |
| Operasi sync bisa >1 menit | Dijalankan di `viewModelScope` dengan progres dan tombol hentikan. Jika proses aplikasi dibunuh, server tetap konsisten dan sync berikutnya idempoten. WorkManager tidak dipakai karena operasi butuh konfirmasi/feedback langsung. |
| Clipboard/share | `ClipboardManager` dan `ACTION_SEND` (tanpa FileProvider) |
| Notifikasi | Tidak ada notifikasi; tidak ada izin notifikasi |
| Cleartext | Tidak diaktifkan; semua trafik HTTPS |
| Back | Predictive back didukung (`enableOnBackInvokedCallback`) |

## 6. UI

- Gaya Material 3 ekspresif di atas Material 3 stabil: skala tipografi display
  yang besar, shape lebih membulat, palet aksen, dan motion kartu/hero. API
  `MaterialExpressiveTheme` baru tersedia di artifact beta, jadi proyek tetap
  memakai `MaterialTheme` stabil dan membangun ekspresi lewat typography,
  shapes, warna, serta animasi sendiri.
- Tipografi memakai **Roboto Flex** (variable font, dibundel di
  `app/src/main/res/font/roboto_flex.ttf`) untuk seluruh skala Material 3,
  dengan bobot Thin–Black agar hierarki display/headline/title/body/label lebih
  ekspresif. Lisensi OFL tersedia di `app/src/main/assets/licenses/` dan
  `docs/licenses/`.
- Palet aksen: Teal, Tangerine, Grape, Ocean, Rose. Masing-masing punya varian
  terang/gelap. Semua role `surfaceContainer*`, `surfaceDim`, `surfaceBright`,
  `outlineVariant`, `inverse*`, dan `scrim` diisi konsisten agar kartu, bar,
  dan kontrol tidak memakai warna permukaan campuran. Dynamic color hanya jika
  `Build.VERSION.SDK_INT >= 31`, default mati; memilih palet otomatis mematikan
  dynamic color. Di Android < 12 kontrol dynamic color tidak ditampilkan sama
  sekali.
- Tema terang/gelap/sistem dengan edge-to-edge penuh. Window menggambar di
  belakang status bar, navigation bar, dan display cutout
  (`layoutInDisplayCutoutMode=ALWAYS` pada API 30+ / `SHORT_EDGES` pada API
  28–29). Top bar, bottom bar, dan layar sukses memakai `safeDrawing`/safe
  insets agar konten tidak tertutup notch, sementara background tetap
  memenuhi seluruh layar.
- Bahasa: Ikuti sistem, Inggris, Indonesia. Pilihan disimpan manual di
  SharedPreferences, diterapkan di `attachBaseContext()`, lalu activity
  dibuat ulang. `locales_config.xml` dan AppCompat per-app locale service tidak
  dipakai agar sumber kebenaran bahasa hanya satu. Semua label membaca
  `values-en/`/`values-id/`.
- Pengaturan memakai selector dropdown untuk bahasa dan tema, bukan radio
  button; palet memakai selector swatch. Perubahan bahasa langsung memuat ulang
  UI.
- Layar: Beranda, Login, Top up, Sukses top up, Pembelian, Akun, Sesi, Protokol,
  Bantuan, Pengembang, Pengaturan, Aktivitas.
- Beranda memakai sapaan otomatis berdasarkan jam: pagi 04.00–10.59, siang
  11.00–14.59, sore 15.00–17.59, malam 18.00–03.59.
- Navigasi tab utama memakai `HorizontalPager` interaktif dengan bottom bar
  yang tersinkron dua arah, sehingga swipe terasa natural dan perubahan tab
  tidak memicu loop animasi. Top up menonaktifkan swipe pager karena gestur
  horizontal di layar itu dicadangkan untuk konfirmasi. Membuka Pengaturan
  atau halaman detail dari tab mana pun lalu menekan bottom bar akan kembali
  ke `Routes.MAIN` dan memilih tab yang diminta, bukan menumpuk halaman.
- Top up memakai kontrol swipe-to-confirm bergaya TWRP; tidak ada tombol Apply
  atau Cancel. Offset thumb diperbarui langsung saat drag agar responsif;
  animasi hanya dipakai untuk snap-back/penyelesaian. Swipe pertama membuka
  tinjauan, kecuali switch "tanpa tinjauan" aktif; swipe berikutnya menulis.
  Mengubah nominal membatalkan tinjauan. Baris target internal dihapus dari UI
  tinjauan.
- Setelah tulis berhasil, event `TopUpOutcome` membuka layar sukses terpisah
  dengan orb animasi, confetti, ringkasan before/amount/after, status capped,
  dan laporan fitur. Layar ini menyembunyikan top bar/bottom bar, memakai safe
  insets, dan mendukung back.
- Transisi navigasi memakai slide horizontal + fade, bukan crossfade.
- Layout responsif: `BoxWithConstraints` dengan breakpoint 720dp untuk dua
  kolom di tablet; bukan `WindowSizeClass` eksperimental agar API stabil.
- Loading, error, empty state; aksi yang menulis memakai swipe atau dialog
  konfirmasi (sign out).
- Log gaya terminal: `LazyColumn`, font monospace, timestamp, level, salin,
  bagikan, bersihkan.
- Semua string UI di `values/strings.xml` (Inggris default), `values-en/`, dan
  `values-id/`; jumlah key ketiganya dijaga sama. Log protokol bersifat data
  teknis (tidak diterjemahkan).
- Preview Compose untuk layar utama, termasuk preview layar sukses top-up.

## 7. Keamanan dan privasi

- Tidak ada hardcoded secret baru; konstanta protokol berasal dari CLI dan
  memang public client constants.
- Password hanya dikirim tersegel (RSA PKCS#1 v1.5 + MD5 mask) dan tidak
  disimpan di disk.
- Sesi (id, enc key, cake) hanya di memori; fingerprint MD5 8 karakter boleh
  ditampilkan.
- `allowBackup=false`, tanpa cleartext, tanpa izin selain INTERNET.
- Log tidak memuat password, session key, atau isi `cake`.
- DataStore menyimpan username (opsional) dan cap waktu pembelian.

## 8. Sengaja tidak di-port

### Redeem gift

Fitur gift code dari CLI **tidak di-port sama sekali**. Tidak ada screen,
use case, endpoint, model, konstanta, string, maupun test untuk fitur ini.
`API_PATHS` hanya memuat `verify`, `events`, dan `purchases`. Source CLI
referensi (`cli_src/`) tidak di-commit ke repositori Android.

### Command terminal

- `clear`: hanya operasi terminal; Android memakai navigasi.
- `exit`: siklus hidup aplikasi ditangani sistem.

## 9. Asumsi

- Kontrak API TheoTown yang dipakai CLI (`1.12.95a` / build `11295`) masih
  berlaku. Bila server berubah, error protokol akan tampil di layar Aktivitas.
- Endpoint tetap `https://api.theotown.com`; tidak ada konfigurasi endpoint
  kustom agar tidak menambah permukaan penyalahgunaan.
- Angka `earned`/`spend` diperlakukan sebagai `Long` saat ditampilkan agar
  pengurangan tidak overflow, tetapi nilai tulis event tetap dibatasi
  `INT32_MAX` seperti CLI.
- Penulisan top-up/pembelian adalah aksi nyata ke akun. Top-up memakai swipe
  untuk konfirmasi (tanpa tombol Apply/Cancel); switch "tanpa tinjauan"
  melewati kartu tinjauan tetapi tetap memerlukan satu swipe.
- Preferensi tema/palet disimpan di DataStore. Pilihan bahasa disimpan di
  SharedPreferences terpisah dan diterapkan lewat `attachBaseContext()`, bukan
  AppCompat per-app locale, supaya tidak ada dua sumber kebenaran. Pilihan
  bahasa lama dari AppCompat tidak dimigrasikan; pengguna cukup memilih ulang.
- Build, lint, dan test hanya dijalankan di GitHub Actions. Tidak ada Gradle
  wrapper karena wrapper tidak bisa digenerate tanpa menjalankan Gradle;
  CI memakai Gradle 8.13 terpasang.

## 10. Versi dan kompatibilitas

| Komponen | Versi | Alasan |
| --- | --- | --- |
| AGP | 8.11.1 | Stabil, mendukung compileSdk 36 dan kompatibel dengan KGP 2.2.20 |
| Gradle | 8.13 | Minimum untuk AGP 8.11.x |
| JDK | 17 (Temurin) | Didukung AGP 8.11 |
| Kotlin | 2.2.20 | Stabil, mendukung Compose BOM 2025.09.01 dan KSP |
| KSP | 2.2.20-2.0.3 | Rilis resmi untuk Kotlin 2.2.20 |
| Hilt | 2.56.2 | Stabil, mendukung KSP2 |
| Compose BOM | 2025.09.01 | Compose UI 1.9.2 + Material3 1.4.0 yang kompatibel |
| Material3 | 1.4.0 (via BOM) | Stabil; ekspresi visual dibangun di atas API stabil |
| Roboto Flex | Variable font bundled | Tipografi ekspresif, lisensi OFL disertakan |
| AppCompat | 1.7.1 | Host theme `Theme.AppCompat.DayNight.NoActionBar`; bukan lagi untuk per-app locale |
| Navigation Compose | 2.8.9 | Stabil |
| OkHttp | 4.12.0 | Stabil, mendukung Android 8+ |
| kotlinx.serialization | 1.8.1 | JSON + state pembelian |
| DataStore | 1.1.4 | Stabil |
| minSdk / targetSdk / compileSdk | 26 / 36 / 36 | Sesuai AGENTS.md |

## 11. Rencana pengujian

- Unit test JVM:
  - validasi nominal (kosong, bukan bilangan, nol, melebihi ruang, pemisah),
  - matematika tinjauan top-up dan overflow,
  - status fitur dan verifikasi,
  - crypto: MD5 vektor uji, AES-CBC vektor NIST, segel password (panjang/format),
  - `FeatureSync` dengan gateway palsu: idempoten, watch, refresh, fallback,
    dan probe,
  - batas periode sapaan Beranda (0, 3, 4, 10, 11, 14, 15, 17, 18, 23).
- UI test dasar (Compose) hanya dikompilasi di CI, tidak dijalankan tanpa
  emulator.
- Lint + R8/shrinkResources saat release.
- Verifikasi lokal hanya statis (tanpa Gradle): import, resource, manifest,
  versi dependency, dan sintaks.

## 12. CI/CD

- `.github/workflows/build.yml`
- Trigger: push ke `main` dan `workflow_dispatch`.
- Runner Ubuntu, JDK 17, Gradle 8.13 via `gradle/actions/setup-gradle`,
  cache aktif, `concurrency` membatalkan build lama.
- Langkah: checkout → JDK → Gradle → SDK platform 36 → signing opsional →
  `gradle lint testDebugUnitTest assembleDebug assembleRelease` → kompilasi
  instrumented test → upload APK debug/release dengan nama berisi SHA.
- Signing release dari secret `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
  `KEY_ALIAS`, `KEY_PASSWORD`; fallback debug signing bila kosong.
- `permissions: contents: read`.
