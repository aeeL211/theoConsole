# Theo Console

Aplikasi Android native untuk TheoTown Console, dibangun dengan Kotlin +
Jetpack Compose + Material 3 ekspresif.

- Package: `aeeL.theoSol`
- minSdk 26, targetSdk 36, compileSdk 37
- Single module `app`, Clean Architecture ringan (`ui/`, `domain/`, `data/`)
- MVVM + UDF, Hilt, Coroutines/Flow, Navigation Compose, DataStore
- HTTP memakai OkHttp dengan protokol terenkripsi milik klien TheoTown

## Penting: build hanya di CI

Repositori ini **tidak memiliki Gradle wrapper** dan tidak dimaksudkan untuk
di-build di perangkat ini. Semua build, lint, dan unit test dijalankan di GitHub
Actions setelah push. Jangan menjalankan `gradle`, `./gradlew`, atau task Gradle
apa pun secara lokal.

Workflow: `.github/workflows/build.yml`

- Trigger: push ke `main` dan `workflow_dispatch`
- JDK 17 (Temurin), Gradle 9.8.2 lewat `gradle/actions/setup-gradle`
- Perintah: `gradle lintRelease testReleaseUnitTest assembleRelease`
- Hanya varian release yang dibangun; ABI native dibatasi ke `armeabi-v7a`
  dan `arm64-v8a`
- Satu artifact bernama `theo-console` berisi `theo-console.apk`

## Mengunduh APK dari Actions

1. Buka tab **Actions** di repositori GitHub.
2. Pilih workflow **Android CI** dan run terbaru yang hijau.
3. Di bagian **Artifacts**, unduh `theo-console`.
4. Ekstrak ZIP artifact; APK bernama `theo-console.apk` berada di dalamnya.
   Artifact disimpan 14 hari.

Untuk memicu build manual, buka **Actions → Android CI → Run workflow** pada
branch `main`.

## Signing release

Jika empat secret signing tidak diisi, build release memakai debug signing agar
CI tetap menghasilkan APK. Untuk signing release yang sebenarnya, tambahkan
repository secrets di **Settings → Secrets and variables → Actions**:

| Secret | Isi |
| --- | --- |
| `KEYSTORE_BASE64` | Isi file keystore yang di-encode Base64 |
| `KEYSTORE_PASSWORD` | Password keystore |
| `KEY_ALIAS` | Alias key release |
| `KEY_PASSWORD` | Password key |

Membuat nilai Base64 (Linux):

```bash
base64 -w0 release.jks > release.jks.base64
```

Salin seluruh isi `release.jks.base64` ke secret `KEYSTORE_BASE64`. Jangan commit
file keystore, `.base64`, atau password ke repositori. CI men-decode keystore ke
`$RUNNER_TEMP/release.jks`, lalu Gradle membaca `KEYSTORE_FILE` dari environment.

Jika `KEYSTORE_BASE64` kosong, langkah decode dilewati dan release memakai debug
signing. Jika keystore diisi tetapi password/alias salah, build signing akan
gagal; isi keempat secret secara bersamaan.

## Fitur aplikasi

- Login forum dengan password tersegel (RSA PKCS#1 v1.5 + mask MD5); password
  tidak disimpan.
- Beranda: sapaan otomatis sesuai jam, saldo, earned/spend, sisa ruang, progres
  batas 100.000, status fitur, dan ringkasan top-up terakhir.
- Top up: validasi nominal, ringkasan before/amount/after (tanpa baris target
  internal), tinjauan wajib, konfirmasi swipe-to-confirm tanpa tombol
  Apply/Cancel, dan layar sukses animasi terpisah.
- Verifikasi fitur otomatis setelah top-up: baca status Free fast speed dan Free
  tunnels, sinkron fitur dengan watch, refresh sesi, fallback max purchase, dan
  probe. Tidak ada tab Pembelian terpisah.
- Akun dengan identitas dan detail sesi (status, fingerprint kunci, endpoint),
  protokol, pengembang, bantuan, pengaturan tema, dan log aktivitas dengan
  salin/bagikan JSON.
- Tema ekspresif dengan lima palet aksen (Teal, Tangerine, Grape, Ocean, Rose),
  **Roboto Flex** untuk seluruh tipografi, shape membulat, dan motion
  slide/fade. Lisensi font ada di `app/src/main/assets/licenses/`.
- Pengaturan bahasa Indonesia/Inggris/ikuti sistem, tema terang/gelap/sistem,
  dynamic color Android 12+ (default mati, kontrol disembunyikan di Android
  < 12), dan detail teknis. Bahasa dan tema memakai selector dropdown.
- Navigasi tab utama memakai `HorizontalPager` + bottom bar yang sinkron;
  swipe horizontal berpindah tab di semua tab, termasuk Top up. Kontrol
  swipe-to-confirm mengonsumsi gestur horizontal hanya di area kontrolnya.
  Bottom bar hanya tampil di tab utama. Halaman detail memakai tombol kembali
  di top bar dan kembali ke tab pager yang sedang aktif; login yang berhasil
  kembali ke Beranda.
- Status bar disembunyikan; top bar 60dp menempati area cutout tanpa inset atas
  tambahan, safe inset hanya diterapkan sekali pada konten, predictive back, dan
  layout dua kolom pada layar ≥720dp.
- String UI tersedia dalam bahasa Inggris (`values/`, `values-en/`) dan
  Indonesia (`values-id/`).

Seluruh alur berjalan di dalam aplikasi Android; tidak ada runtime, layanan,
atau proses tambahan yang diperlukan di perangkat.

## Lisensi font

Roboto Flex dibundel dari Google Fonts di `app/src/main/res/font/roboto_flex.ttf`
di bawah SIL Open Font License 1.1. Teks lisensi lengkap tersedia di
`app/src/main/assets/licenses/roboto_flex_ofl.txt` dan
`docs/licenses/roboto_flex_ofl.txt`.

## Struktur singkat

```
app/src/main/java/aeeL/theoSol/
  core/       konstanta wire, crypto, error, formatter
  domain/     model, validasi, top-up math, FeatureSync, gateway, use case
  data/       OkHttp transport, TheoClient, repository, DataStore, log
  di/         modul Hilt
  ui/         theme, ViewModel, navigation, screen, komponen
app/src/main/res/values/       string default (Inggris) dan tema
app/src/main/res/values-en/    string Inggris
app/src/main/res/values-id/    string Indonesia
app/src/main/res/font/         Roboto Flex variable font
app/src/main/assets/licenses/  lisensi font OFL
app/src/test/                  unit test domain dan crypto
app/src/androidTest/           UI test dasar (butuh emulator; tidak dijalankan CI)
docs/ARCHITECTURE.md           arsitektur, protokol, UI, keamanan, dan CI
AGENTS.md                      panduan kerja untuk agent di repositori ini
```

## Verifikasi

Verifikasi lokal hanya statis: pembacaan kode, import, referensi resource,
manifest, versi dependency, dan sintaks YAML. Kompilasi, lint, dan unit test
diverifikasi oleh GitHub Actions; UI test instrumented dijalankan manual di
emulator.
