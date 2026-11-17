# SUMITX — Chiron

User-controlled Android music companion with a floating island overlay.

## Current feature foundation
- Notification-listener permission onboarding.
- Explicit overlay permission and user-started foreground service.
- Floating island with expandable controls.
- Media-session metadata and previous, play/pause, and next controls when supported by the active player.
- Animated rainbow spectrum-style visualizer.
- Local LRC import and timestamp-based lyric preview.
- Neon accent palette and auto-collapse setting.
- No account, analytics SDK, hidden network calls, or automatic lyric scraping.

## Compatibility
YouTube Music support depends on Android exposing its active media session and notification-listener access. Device battery settings can affect overlays. Lyrics are imported by the user from an LRC file.

## Privacy and Play Protect
SUMITX uses documented Android notification-listener, overlay, and foreground-service APIs. Sensitive permissions must be granted by the user in Android Settings. The app does not hide itself, bypass Play Protect, download executable code, or disable device security. A clean implementation reduces risk but cannot guarantee Play Protect will never show a warning.

## Build
Use JDK 17 and Gradle 8.11.1, then run gradle assembleDebug.
