<h2 align="center"><b>LearnTube</b></h2>
<h4 align="center">A learning-focused YouTube front-end for Android, built on NewPipe.</h4>

<p align="center">
<a href="https://www.gnu.org/licenses/gpl-3.0" alt="License: GPLv3"><img src="https://img.shields.io/badge/License-GPL%20v3-blue.svg"></a>
</p>

<hr>

## What is LearnTube?

LearnTube is a privacy-first YouTube client for Android — no account required, no Google Play Services, no tracking. It is a fork of [NewPipe](https://github.com/TeamNewPipe/NewPipe), the libre lightweight streaming front-end, extended with structured learning tools designed to help you turn YouTube playlists into real learning paths.

LearnTube keeps everything that makes NewPipe great and adds a layer of learning accountability on top.

---

## Learning Features

### Learning Paths Dashboard
Your bookmarked playlists become learning paths. The dashboard shows a per-path progress bar (watched / total videos), a summary header with the number of active and completed paths, and your weekly study hours — all at a glance before you open a single video.

### Watch Progress Tracking
Long-press any video inside a playlist to mark it as **Watched** or **Unwatched**. The playlist menu also provides bulk actions — **Mark all watched** and **Reset all progress** — so you can manage an entire course in one tap.

### Timestamped Notes
Tap the note button in the player controls to save a note anchored to the current playback position. Notes are listed in the video detail screen in timestamp order. Tap any note to instantly seek back to that moment in the video.

### Learning Streak & Study Time
A daily streak counter tracks consecutive days on which you watched at least one video. The stats header shows your study time for today, this week, and all time, plus a calendar view highlighting the days you were active.

---

## Inherited NewPipe Features

LearnTube inherits the full NewPipe feature set, including:

- Watch videos at resolutions up to 4K
- Background audio playback (audio-only stream to save data)
- Picture-in-Picture (popup) player
- Watch live streams
- Subtitles / closed captions
- Subscribe to channels without an account
- New-video notifications for subscribed channels
- Channel groups and feed browsing
- Watch history
- Local and remote playlist support
- Video / audio / subtitle downloads
- Support for YouTube, PeerTube, Bandcamp, SoundCloud, and media.ccc.de

---

## Acknowledgements

LearnTube is built on top of **[NewPipe](https://github.com/TeamNewPipe/NewPipe)**, the libre, lightweight streaming front-end for Android created and maintained by [TeamNewPipe](https://github.com/TeamNewPipe). Without their years of work building a privacy-respecting, account-free YouTube client, LearnTube would not exist.

We are deeply grateful to every contributor to the NewPipe project and to the [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) library that powers it.

> LearnTube is an independent fork and is **not** affiliated with or endorsed by TeamNewPipe.

---

## Building

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew build            # Full build (all variants)
./gradlew testDebugUnitTest  # Run unit tests
```

---

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)

LearnTube is Free Software: you can use, study, share, and improve it at will. It is distributed under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl.html) or later — the same license as NewPipe.
