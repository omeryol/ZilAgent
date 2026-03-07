# ZilAgent

ZilAgent is an offline-first Android app for school bell schedules, lesson flow tracking, and classroom countdowns.
It is designed for teachers and schools that need reliable timing without internet dependency.

## Highlights

- Offline by default: no internet permission, local-only data
- Profile-based schedule engine (normal day, exam day, weekend course)
- Two focused home screen widgets:
  - Live Countdown
  - Lesson Flow
- Fullscreen Exam Mode timer for classroom visibility
- Turkish and English UI
- Local backup/restore and QR profile transfer

## Why ZilAgent

- Fast in-class workflow: current lesson, break, and remaining time at a glance
- Privacy-friendly architecture: data stays on device
- Built for practical school usage, not generic timer scenarios

## Screenshots

Store screenshots are available in:

- `fastlane/metadata/android/en-US/images/phoneScreenshots/`
- `fastlane/metadata/android/tr-TR/images/phoneScreenshots/`

## Tech Stack

- Kotlin + Jetpack Compose
- Room (local database)
- App Widgets
- CameraX + ZXing (QR transfer)

## Build From Source

### Requirements

- JDK 17+ (Android Studio JBR works)
- Android SDK (API 34)

### Commands

```powershell
# Debug APK
.\gradlew.bat :app:assembleDebug

# Release APK
$env:ANDROID_HOME="C:\Users\<you>\AppData\Local\Android\Sdk"
.\gradlew.bat :app:assembleRelease
```

## Project Structure

- `app/src/main/java/com/zilagent/app/` : application source code
- `app/src/main/res/` : Android resources
- `fastlane/metadata/android/` : localized store metadata
- `metadata/com.zilagent.app.yml` : F-Droid metadata template

## F-Droid Readiness

This project is prepared for F-Droid submission:

- FOSS license: `GPL-3.0-or-later`
- No tracking SDKs / ads / proprietary analytics
- Offline-first behavior
- Fastlane metadata included
- F-Droid metadata template included

See also: `FDROID_SUBMISSION.md`

## License

GNU GPL v3. See [LICENSE](LICENSE).
