# F-Droid Submission Notes (ZilAgent)

This project is prepared for F-Droid inclusion with command-line reproducible build inputs:

- FOSS license file exists (`GPL-3.0-or-later`)
- No proprietary SDKs (no Firebase/GMS/analytics/ads)
- Fastlane metadata exists under `fastlane/metadata/android/`
- Offline-first design (no internet permission)
- Portable Gradle config (no machine-specific `org.gradle.java.home`)

## Suggested fdroiddata metadata (starter)

Package name: `com.zilagent.app`

Recommended fields:

- `License: GPL-3.0-or-later`
- `SourceCode: https://github.com/omeryol/ZilAgent`
- `IssueTracker: https://github.com/omeryol/ZilAgent/issues`
- `RepoType: git`
- `Repo: https://github.com/omeryol/ZilAgent`
- `Builds.gradle: yes`
- `Builds.subdir: app` (only if needed by maintainer config)

## Final steps before opening MR to fdroiddata

1. Create a signed Git tag for the exact release source (example: `v2.1.0`).
2. Ensure the public repository includes this latest source and metadata files.
3. Open submission at:
   - https://gitlab.com/fdroid/rfp/issues (simple path)
   - or direct metadata MR at https://gitlab.com/fdroid/fdroiddata
4. In submission note, explicitly state:
   - app is fully offline,
   - no tracking/ads/non-free dependencies,
   - no internet permission requested.

## Local verification command

```powershell
.\gradlew.bat :app:assembleRelease
```

If this command succeeds on clean checkout and F-Droid maintainers can reproduce it, inclusion should be straightforward.
