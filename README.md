# Anime Arena Legends

Game fighting arena 3D native Android bertema anime dengan grafis 3D real-time, animasi skeletal GLB, sistem combo & cinematic ultimate, lawan AI taktis, serta sistem audio & voice lengkap yang berjalan 100% offline.

---

## 1. Deskripsi Game

**Anime Arena Legends** mempertemukan dua pendekar elemen orisinal di arena **Kagerou Twilight Village**:

- **Kurogane Raiden** (*Stormblade of Lightning*) — Pendekar pedang kilat dengan mobilitas cepat, serangan jarak dekat beruntun, tebasan kilat jarak menengah (*Raijin Flash Surge*), dan jurus pamungkas sinematik *Heavenly Thunder Judgment*.
- **Enma Homura** (*Crimson Infernal Blade*) — Pendekar api dengan serangan kombinasi bertenaga besar, ledakan area (*Crimson Nova Burst*), dan jurus pamungkas sinematik *Infernal Solar Cataclysm*.

---

## 2. Fitur Permainan

- **Rendering 3D Native (Google Filament + glTF/GLB)**:
  - Model karakter 3D anatomis berkerangka (*rigged 21-bone skeletal GLB*) dengan tekstur cel-PBR dan emissive glow.
  - 20 klip animasi skeletal per karakter (*Idle, Walk, Run, Jump, Fall, Light1–3, Heavy1–2, Launcher, AirCombo, HitReaction, Guard, Dodge, SpecialAttack, UltimatePrep, UltimateStrike, Victory, Defeat*) dengan *crossfade animation blending*.
  - Pencahayaan real-time (directional sun + shadow mapping, lampu lentera desa hangat, serta point light elemen dinamis).
  - Sistem VFX 3D menggunakan *Object Pooling* (tebasan energi, shockwave, pecahan kilat/api).
- **Sistem Pertarungan & Ultimate Combo**:
  - Rantai combo terintegrasi: `Light → Light → Light`, `Light → Heavy → Heavy`, `Heavy → Launcher → Air Combo → Special → Ultimate`.
  - Input buffering, cancel window, hitstop impact, knockback, guard gauge, block chip damage, guard break, dodge i-frames, dan penegakan cooldown/energy meter.
  - **Cinematic Ultimate System**: Transisi kamera close-up wajah, pengumpulan energi, voice shout, slow-motion impact, camera shake terukur, serta opsi tombol **SKIP** setelah aktivasi.
- **Tiga Mode Permainan**:
  1. **Versus Mode** (1v1 melawan AI dengan pilihan tingkat kesulitan *Novice*, *Warrior*, *Master*).
  2. **Arcade Mode** (Gauntlet 3 stage dengan peningkatan kesulitan bertahap).
  3. **Training Dojo** (Latihan combo bebas dengan regenerasi HP lawan & energi).
- **Audio & Voice System Lengkap**:
  - Musik latar adaptif (*Menu, Battle Normal, Battle Intense, Ultimate Cinematic, Victory, Defeat*).
  - Sound effects pertempuran & antarmuka serta voice lines orisinal untuk kedua karakter.
  - Pengaturan volume terpisah (*Master, Music, SFX, Voice*) dan toggle *Mute*.
- **Optimasi Perangkat Menengah (RAM 8 GB)**:
  - Pilihan target **30 FPS** / **60 FPS**, preset kualitas (*Low, Medium, High*), *render resolution scaling* (60%–100%), kontrol bayangan, serta penyimpanan lokal **Room** + **DataStore**.

---

## 3. Struktur Repository

```text
.
├── .github/workflows/
│   └── android-apk.yml          # Workflow GitHub Actions build APK otomatis
├── tools/
│   ├── generate_glb_assets.py   # Generator model GLB rigged & arena 3D
│   └── generate_audio_assets.py # Generator aset audio WAV (BGM, SFX, Voice)
├── app/src/main/
│   ├── assets/
│   │   ├── models/              # fighter_raiden.glb, fighter_homura.glb, arena_village.glb, VFX
│   │   └── audio/               # BGM, SFX, dan Voice lines (.wav)
│   └── java/com/example/
│       ├── app/                 # AnimeArenaApp.kt
│       ├── core/                # Math3D, ObjectPool, GameModels, BattleEngine
│       ├── characters/          # FighterSpec, FighterState
│       ├── animation/           # AnimationState, AnimationStateMachine
│       ├── combat/              # CombatSystem, CombatEvents, UltimateCinematicController
│       ├── physics/             # ArenaPhysics (ground, collider, boundary)
│       ├── camera/              # ArenaCameraController (lock-on & cinematic camera)
│       ├── ai/                  # AiOpponentController
│       ├── arena/               # VillageArenaData
│       ├── graphics/            # FilamentArenaRenderer, VfxParticleSystem
│       ├── audio/               # AudioEngine, VoiceSystem
│       ├── settings/            # GameSettings, GraphicsQuality
│       ├── storage/             # SettingsDataStore, MatchRecordDatabase (Room)
│       ├── ui/                  # Theme, ArenaViewModel, ArenaNavGraph, Screens & Multitouch HUD
│       └── MainActivity.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
└── gradlew / gradlew.bat
```

---

## 4. Teknologi yang Digunakan

- **Bahasa**: Kotlin 2.2
- **UI**: Jetpack Compose + Material 3 + Navigation Compose (`@Serializable` type-safe routes)
- **3D Engine**: Google Filament (`filament-android`, `gltfio-android`, `filament-utils-android` v1.80.1)
- **Penyimpanan Lokal**: Room Database + Jetpack DataStore Preferences
- **Audio**: Android `SoundPool` + `MediaPlayer`
- **Build System**: Gradle 9.3.1 (Kotlin DSL) + Android Gradle Plugin 9.1.1

---

## 5. Cara Mengunggah Source Code ke GitHub

1. Buat repository baru di GitHub (misalnya `anime-arena-legends`).
2. Jalankan perintah berikut di direktori proyek:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Anime Arena Legends 3D Android game"
   git branch -M main
   git remote add origin https://github.com/<username>/anime-arena-legends.git
   git push -u origin main
   ```

---

## 6. Cara Menjalankan GitHub Actions

Workflow otomatis berjalan setiap kali Anda melakukan `push` ke branch `main` atau `master`, atau membuat `pull_request`.

Untuk menjalankan build secara manual:
1. Buka repository di GitHub.
2. Klik tab **Actions**.
3. Pilih workflow **Android APK Build**.
4. Klik tombol **Run workflow** → pilih branch `main` → klik **Run workflow**.

---

## 7. Cara Memeriksa Status Build

1. Buka tab **Actions** pada repository GitHub Anda.
2. Klik pada workflow run terbaru.
3. Pantau setiap tahapan (*Checkout*, *Install JDK 21*, *testDebugUnitTest*, *assembleDebug*, *Verify Debug APK*, *Upload Artifact*) hingga berstatus hijau (**Success**).

---

## 8. Cara Mengunduh APK

Setelah workflow selesai dengan status berhasil, unduh APK melalui jalur berikut:

**GitHub Repository → Actions → Pilih workflow run yang berhasil → Artifacts → `anime-arena-legends-debug-apk`**

Ekstrak file `.zip` hasil unduhan untuk mendapatkan `app-debug.apk`.

---

## 9. Cara Memasang APK di Android

1. Pindahkan file `app-debug.apk` ke perangkat Android Anda (Android 7.0 / API 24 ke atas).
2. Buka file APK dari File Manager.
3. Jika diminta, aktifkan izin **Install unknown apps** untuk File Manager tersebut.
4. Tekan **Install** lalu buka **Anime Arena Legends** dalam orientasi landscape.

---

## 10. Cara Mengatur Signed Release APK

Demi keamanan, keystore dan password **tidak boleh** disimpan di repository. Workflow mendukung penandatanganan rilis opsional melalui **GitHub Repository Secrets**:

1. Siapkan file keystore `.jks` dengan key alias `upload` (misalnya via `keytool -genkey -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`).
2. Encode file keystore ke Base64:
   ```bash
   base64 -w 0 my-upload-key.jks
   ```
3. Buka **GitHub Repository → Settings → Secrets and variables → Actions → New repository secret**, lalu tambahkan 4 secrets berikut:
   - `ANDROID_KEYSTORE_BASE64`: Isi string Base64 dari file `.jks`.
   - `ANDROID_KEYSTORE_PASSWORD`: Password keystore Anda.
   - `ANDROID_KEY_ALIAS`: Alias key (`upload`).
   - `ANDROID_KEY_PASSWORD`: Password key alias Anda.
4. Jika keempat secrets tersebut belum diisi, workflow tetap berjalan sukses dan menghasilkan Debug APK (`anime-arena-legends-debug-apk`). Jika sudah diisi, workflow juga akan menghasilkan artifact `anime-arena-legends-release-apk`.
