# Changelog

All notable changes to this project will be documented in this file.

## [v2.2.0] - 2026-03-07

### Added
- F-Droid metadata template added: `metadata/com.zilagent.app.yml`.
- Security hardening notes and submission guidance added: `FDROID_SUBMISSION.md`.

### Changed
- Version bumped to `2.2.0` (`versionCode 3`).
- README expanded with product overview, build instructions, and F-Droid readiness details.
- Fastlane store metadata (TR/EN) refreshed and corrected.

### Security
- Android auto-backup disabled (`allowBackup=false`).
- Backup restore flow redesigned with validation and atomic transaction behavior.
- QR import/export payload limits and schema validation added.

### Repository Cleanup
- Removed obsolete root-level screenshots and temporary dump files.
- Removed tracked release binary artifacts from source tree (`app/release/*`).
- `.gitignore` strengthened to prevent local/temporary files from re-entering the repository.

## [v2.1.0] - 2026-01-17

### Added
- Offline QR profile sharing and import.
- Lesson notes support.
- Day-based profile schedule selection.

### Fixed
- Default profile creation and timing logic.
- Weekend/empty-day widget status behavior.
- Settings reactivity and restart-related issues.

### Changed
- Source code links updated to `https://github.com/omeryol/ZilAgent`.
- Confirmed offline-first behavior with no internet permission.
