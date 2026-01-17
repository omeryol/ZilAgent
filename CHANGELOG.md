# Changelog

All notable changes to this project will be documented in this file.

## [v2.0.0] - 2026-01-17

### Added (Yeni Özellikler)
- **Offline Profile Sharing (QR):** Added ability to share and import profiles via QR code without internet connection.
- **Lesson Notes:** Users can now add notes to specific lessons by long-pressing them in the Syllabus tab.
- **Day Selection for Profiles:** Added day selection (Mon-Sun) support when creating/editing profiles. Weekends can now be active school days if selected.
- **F-Droid Compliance:** 
    - Verified full open-source compliance.
    - Added SHA-256 checksum to Gradle Wrapper for security.
    - Ensured Fastlane metadata (en-US) is complete.
- **Reactivity:** Implemented Flow-based architecture in `SyllabusViewModel` and `DashboardViewModel` for instant UI updates when settings change (no restart required).

### Fixed (Düzeltmeler)
- **Default Profile Logic:** Fixed issues where default profiles were duplicated or had incorrect time settings (Morning Assembly duration fix).
- **Widget Logic:** 
    - Fixed "Weekend" and "Empty Day" display logic. 
    - Widgets now correctly show "Holiday" or "Empty" labels instead of blank or incorrect schedule headers.
    - Improved next-day transition logic for widgets.
- **App Restart Issue:** Resolved the bug requiring app restart for settings to apply.

### Changed (Değişiklikler)
- **About Section:** Updated source code repository links to `https://github.com/omeryol/ZilAgent`.
- **Permissions:** Confirmed removal of `INTERNET` permission for strict privacy.

### Technical
- **Room Database:** Added `LessonNote` entity and DAOs.
- **Build System:** Cleaned up build logs and verified Gradle 8.13 compatibility.
