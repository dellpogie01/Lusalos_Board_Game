# Lusalos ? Offline Android Game

A playable Android implementation of the Filipino strategy board game inspired by Nine Men's Morris. All game logic and AI run on-device. Profiles, statistics, and completed results use private local SQLite storage. There is no Firebase, account, or internet permission.

## Play

Choose an AI difficulty and tap **Play Against AI**. Place nine pieces each, then tap a piece and an adjacent empty point to move. Form a line of three to capture an opponent piece; with only three pieces, you can fly to any empty point. Undo is available in AI games before the match ends.

AI wins award 20 points to the local profile. Edit the player name under **Progress**. Completed results persist in `lusalos.db`; unfinished games remain in memory while the app process is alive.

## Build

Open the root folder in Android Studio and use JDK 17 or 21. Install Android SDK 36. Set the SDK location in your own `local.properties` if Android Studio does not create it automatically.

```powershell
$env:JAVA_HOME = 'C:\Users\Admin\.jdks\jbr-21.0.11'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`. The project includes the Gradle wrapper, dependency catalog, launcher icon, and Android theme. The debug signing configuration uses `debug.keystore` in the root; if absent, generate a standard Android debug key there using alias `androiddebugkey` and password `android`. Release signing uses the existing AI Studio signing configuration and environment variables.

## Verification

- Unit tests cover game rules, SQLite persistence, import of legacy guest statistics, rank updates, and duplicate result prevention.
- Device tests tap the rendered board and verify mill formation/capture, all 18 placements followed by movement, AI replies, and undo.
- The runtime APK has no internet permission.

Existing guest preferences are imported once when the SQLite database is first created. App backup is disabled; clearing app storage deletes local progress.
