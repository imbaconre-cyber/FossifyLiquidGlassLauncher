# Fossify Launcher + AndroidLiquidGlass

Combined source for a custom Android launcher that aims to bring Fossify's
privacy-first launcher together with Kyant0's AndroidLiquidGlass liquid-glass
UI effects.

## Structure

- `/` — Fossify Launcher source (Gradle project, package `org.fossify.home`)
- `/liquidglass` — Kyant0/AndroidLiquidGlass library source (Compose backdrop/liquid glass)

## Integration plan (build on a PC with Android Studio)

1. Add the liquid glass module to `settings.gradle.kts`:
   ```kotlin
   include(":liquidglass")
   // or publish the library and depend on it
   ```
2. In `app/build.gradle.kts`, add:
   ```kotlin
   implementation(project(":liquidglass"))
   // plus Compose BOM / material3 as required by AndroidLiquidGlass
   ```
3. Replace Fossify's home screen / app drawer composables with liquid-glass
   styled surfaces (backdrop blur, glass cards) from the AndroidLiquidGlass API.
4. Adjust theme/colors to an iOS-26-like palette.
5. Build & install:
   ```bash
   ./gradlew assembleRelease
   adb install app/build/outputs/apk/release/*.apk
   ```

## Status

Source combined automatically. Not yet built or integrated. Building requires
Android Studio + JDK 17 + Android SDK.

## Credits

- https://github.com/FossifyOrg/Launcher
- https://github.com/Kyant0/AndroidLiquidGlass
