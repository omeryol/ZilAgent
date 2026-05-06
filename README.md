# ZilAgent

**ZilAgent** is an open-source Android app for managing school bell schedules, countdown timers, and smart reminders.

## Features

- 🔔 **Bell Schedule Management** — Define custom bell schedules with multiple profiles
- ⏳ **Live Countdown Widget** — Home screen widget with countdown to next bell, progress bar, and color themes
- 📅 **Plan Tab** — Special days, official holidays, and custom holiday management with auto-detection
- 🎓 **Exam Mode** — Suppress bells during exam periods
- 🗓️ **Syllabus View** — Class and subject management with weekly overview
- 🎨 **Widget Themes** — Multiple color palettes (Dawn, Ember, Grove, Lagoon, Mono, Neon, Paper, Slate)
- 🔁 **Auto Holidays** — Automatic fixed-date Turkish national holiday detection

## Requirements

- Android 8.0 (API 26) or higher
- No internet permission required — fully offline

## Building

```bash
./gradlew :app:assembleDebug
```

Install to connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## License

This project is licensed under the **GNU General Public License v3.0**.  
See the [LICENSE](LICENSE) file for details.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
