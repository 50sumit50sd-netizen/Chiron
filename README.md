# SUMITX — Chiron Android Starter

A Kotlin Android starter for a customizable floating music island.

## What is implemented in this starter
- Native Android project using Kotlin.
- Notification-listener service that reads basic media notification metadata when exposed by a media app.
- User-enabled foreground service for a floating overlay.
- Automatic show/hide based on detected media notification state, after permissions and auto mode are enabled.
- Basic compact island with song title and artist.
- Basic island width setting.
- GitHub Actions workflow to build a debug APK.

## Important: automatic background behaviour
The island cannot silently grant itself Android permissions. On first use:
1. Open SUMITX.
2. Grant Notification Access.
3. Grant Display over other apps.
4. Tap Enable automatic floating island.
After this one-time setup, the foreground service can keep watching media notification updates and show the island when compatible media is detected. Android may still restrict background services or battery behaviour depending on device software.

## Planned / not yet implemented
The following requested features are part of the product roadmap, not complete in this starter:
- Full YouTube Music media-session transport controls and per-app profile switching.
- Synchronized lyrics provider/search engine and LRC import UI.
- Real audio waveform, frequency spectrum and beat detection.
- Dual rainbow sonic waves.
- Full drag-to-position, swipe gestures and expanded island.
- Full RGB/HEX customization studio, gradients, glow controls and animation editor.
- App-specific profiles and backup/export.
- Device-specific testing and production release signing.

Do not treat this starter as a finished production app. Build and test each feature incrementally.

## Build
Requires JDK 17 and Gradle 8.11.1:
```bash
gradle assembleDebug
```
APK output:
`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds the debug APK on pushes to `main` and manual workflow dispatch.
