# Theo Console

Aplikasi Android native untuk TheoTown Console: port dari CLI Node.js
`theotown-console` ke Kotlin + Jetpack Compose + Material 3.

- Package: `com.aeeL.theokntl`
- minSdk 26, targetSdk 36, compileSdk 36
- Single module `app`, Clean Architecture ringan (`ui/`, `domain/`, `data/`)
- MVVM + UDF, Hilt, Coroutines/Flow, Navigation Compose, DataStore
- HTTP memakai OkHttp dengan protokol terenkripsi yang sama seperti CLI

## Penting: build hanya di CI

Repositori ini **tidak memiliki Gradle wrapper** dan tidak dimaksudkan untuk
di-build di perangkat ini. Semua build, lint, dan unit test dijalankan di GitHub
Actions setelah push. Jangan menjalankan `gradle`, `./gradlew`, atau task Gradle
apa pun secara lokal.

Workflow: `.github/workflows/build.yml`

- Trigger: push ke `main` dan `workflow_dispatch`
- JDK 17 (Temurin), Gradle 8.13 lewat `gradle/actions/setup-gradle`
- Perintah: `gradle lint testDebugUnitTest assembleDebug assembleRelease`
- Instrumented test hanya dikompilasi (`assembleDebugAndroidTest`), tidak
  dijalankan karena tidak ada emulator
- APK debug dan release di-upload sebagai artifact dengan nama yang memuat
  commit SHA

## Mengunduh APK dari Actions

1. Buka tab **Actions** di repositori GitHub.
2. Pilih workflow **Android CI** dan run terbaru yang hijau.
3. Di bagian **Artifacts**, unduh:
   - `theo-console-debug-<sha>` untuk APK debug,
   - `theo-console-release-<sha>` untuk APK release.
4. Ekstrak ZIP artifact; APK berada di dalamnya. Artifact disimpan 14 hari.

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
- Beranda: saldo, earned/spend, sisa ruang, progres batas 100.000, status fitur.
- Top up: validasi nominal, tinjauan target, konfirmasi tulis, opsi terapkan
  tanpa tinjauan, dan pemeriksaan fitur setelah tulis.
- Pembelian: baca status Free fast speed dan Free tunnels, sinkron fitur dengan
  watch, refresh sesi, fallback max purchase, dan probe.
- Akun, sesi (fingerprint kunci, endpoint), protokol, pengembang, bantuan,
  pengaturan tema, dan log aktivitas gaya terminal dengan salin/bagikan JSON.
- Edge-to-edge, predictive back, dynamic color hanya Android 12+, tema
  terang/gelap/sistem, layout dua kolom pada layar ≥720dp.
- String UI tersedia dalam bahasa Inggris (`values/`, `values-en/`) dan
  Indonesia (`values-id/`).

Command terminal `clear` dan `exit` tidak di-port karena siklus hidup aplikasi
ditangani sistem. Fitur **redeem gift sengaja tidak di-port** sama sekali; lihat
`docs/PORTING_PLAN.md` bagian "Sengaja tidak di-port".

## Struktur singkat

```
app/src/main/java/com/aeeL/theokntl/
  core/       konstanta wire, crypto, error, formatter
  domain/     model, validasi, top-up math, FeatureSync, gateway, use case
  data/       OkHttp transport, TheoClient, repository, DataStore, log
  di/         modul Hilt
  ui/         theme, ViewModel, navigation, screen, komponen
app/src/main/res/values/       string default (Inggris), tema, warna
app/src/main/res/values-en/    string Inggris
app/src/main/res/values-id/    string Indonesia
app/src/test/                  unit test domain dan crypto
app/src/androidTest/           satu UI test dasar (hanya dikompilasi di CI)
docs/PORTING_PLAN.md           pemetaan CLI, asumsi, batasan, exclusions
```

## Verifikasi

Verifikasi lokal pada tugas ini hanya statis: pembacaan kode, import, referensi
resource, manifest, versi dependency, dan sintaks YAML. Kompilasi, lint, unit
test, dan instrumented test pertama akan diverifikasi oleh GitHub Actions.
