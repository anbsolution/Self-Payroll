# Self Attendance & Salary Slip — Phase 1

Complete Android source project for exactly four main pages:

**Punch | Attendance | Salary Slip | Profile**

## Technology
- Kotlin source on Android Gradle Plugin 9.4.0
- Gradle 9.6.1
- Android SDK 36 / JDK 17
- Material 3 UI
- Room + Coroutines + WorkManager
- MVVM-style ViewModels + repository layer
- Android `PdfDocument` for offline PDF creation

AGP 9.4.0 requires Gradle 9.6.0 or newer and JDK 17. This project pins Gradle 9.6.1.

## Offline-first behavior
All core records are saved locally first. Attendance, profile and salary records carry synchronization state and timestamps. WorkManager schedules synchronization only when a network is available. A failed remote operation never deletes the local record.

`SyncApi` is the secure backend abstraction. Phase 1 intentionally does not contain credentials or a fake remote server. `NoOpSecureApi` fails safely until a real authenticated HTTPS implementation is supplied in a later phase.

## Build with Termux
Do not execute the project from `/sdcard/Download`, because Android shared storage may reject executable files. Copy it into Termux home first:

```bash
cd /sdcard/Download
cp -r Self-Attendance-Salary-Slip $HOME/
cd $HOME/Self-Attendance-Salary-Slip
bash gradlew :app:assembleDebug
```

The included launcher uses Gradle 9.6.1. If `gradle` is already installed, it uses that command; otherwise it downloads the pinned distribution into the Termux Gradle user directory.

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
`.github/workflows/build-apk.yml` installs JDK 17 and Android SDK 36, provisions Gradle 9.6.1, builds the debug APK, runs unit tests, and uploads the APK as a workflow artifact.

## Project structure
- `app/src/main/java/.../data/local` — Room entities, DAOs and database
- `app/src/main/java/.../repository` — offline repository, API boundary and sync worker
- `app/src/main/java/.../ui` — four main fragments and ViewModels
- `app/src/test` — salary calculation tests
- `app/src/androidTest` — basic Android instrumentation test
- `.github/workflows` — CI build

## Phase boundary
Phase 1 only. No Dashboard, Home, Settings, Reports or Admin main page is included.
