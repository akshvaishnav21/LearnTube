# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NewPipe is an Android video streaming app — a libre, lightweight front-end for YouTube, PeerTube, Bandcamp, SoundCloud, and other media services. The project is written primarily in Kotlin with some legacy Java.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew build                  # Full build (all variants)
./gradlew testDebugUnitTest      # Run unit tests
./gradlew connectedCheck         # Run instrumented tests (requires device/emulator)
./gradlew lintDebug              # Lint analysis

# Code style
./gradlew runCheckstyle          # Java/Kotlin style check
./gradlew runKtlint              # Kotlin lint check
./gradlew formatKtlint           # Auto-format Kotlin code
./gradlew checkDependenciesOrder # Verify dependency ordering in build.gradle.kts
```

The pre-debug build automatically runs ktlint formatting, Checkstyle, and dependency order validation. Skip formatting with `-DskipFormatKtlint`.

To run a single test class:
```bash
./gradlew testDebugUnitTest --tests "org.schabi.newpipe.SomeTestClass"
```

## Architecture

### Key Abstractions

- **NewPipeExtractor** (external library) — abstracts all streaming service APIs (YouTube, PeerTube, etc.) behind a unified interface. Service-specific logic lives there, not in this repo.
- `App.kt` — Application class; initializes NewPipe extractor, crash reporting (ACRA), and global state.
- `MainActivity.java` — Single-activity architecture; hosts all fragments via a `FragmentManager`.
- `RouterActivity.java` — Handles deep links and shared URLs, routing them to the appropriate fragment.

### Package Structure (`app/src/main/java/org/schabi/newpipe/`)

- `player/` — ExoPlayer integration with full media session, notification, gesture, and playback queue management. This is the largest and most complex package.
- `local/` — Local data management: bookmarks, feed, history, playlists, subscriptions.
- `database/` — Room DAO and entity classes for feed, history, playlists, streams, and subscriptions.
- `fragments/` — UI fragments for video detail and list views.
- `settings/` — Settings screens and preference management.
- `util/` — Utilities and helpers for external app communication, navigation, theming, etc.
- `info_list/` — RecyclerView item holders and dialogs for stream/channel/playlist items.

### Data Layer

Room database (`NewPipeDatabase.kt`) with schemas stored in `app/schemas/`. When modifying Room entities, increment the database version and provide a migration. Schema JSON files are auto-exported and committed.

### Reactive Programming

RxJava 3 is used heavily for async operations (especially feed loading, subscription management, database queries). Some newer code uses Kotlin Coroutines. Both coexist — prefer coroutines for new Kotlin code.

## Configuration & Versions

All dependency versions are centralized in `gradle/libs.versions.toml`. Always use version catalog references (`libs.xxx`) rather than hardcoded version strings in `build.gradle.kts`.

Key SDK targets: `minSdk 21`, `targetSdk 35`, `compileSdk 36`. Java toolchain: 17 (compilation), 21 (Checkstyle).

## Code Style

- Kotlin files: enforced by ktlint (config in root `.editorconfig`)
- Java files: enforced by Checkstyle (rules in `checkstyle/`)
- Gradle dependency declarations must follow a specific ordering — validated by the custom `checkDependenciesOrder` task in `buildSrc/`
