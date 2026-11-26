# VEYRA Auto — Phase 1 — 0.1.10

Android Auto-oriented Kotlin/Compose project prototype.

## Fixed roadmap
1. Core app and Android Auto foundation
2. Media playback
3. YouTube module
4. Turkish voice commands (offline recognition evaluation/integration)
5. Internet radio
6. Settings, testing and APK release

## Phase 1 scope
- MediaLibraryService implementation file added with a placeholder browsable catalog
- Cloud build workflow uses Gradle 8.9 provisioning via Gradle Build Action
- Compose phone UI and module navigation
- Media3 MediaLibraryService / MediaLibrarySession foundation
- Browsable root catalog (Media, Radio, Voice Command)
- Android Auto descriptor and manifest metadata
- GitHub Actions cloud build, lint report, and APK artifact

## Validation status
The source has not yet been compiled in this environment or tested on Android Auto hardware.

## Cloud build
Upload this project to a GitHub repository on `main`, then use Actions > VEYRA Android Build > Run workflow. A successful build provides the `VEYRA-debug-apk` artifact. The workflow runs `lintDebug` and `assembleDebug` and stores the report artifact when available.
