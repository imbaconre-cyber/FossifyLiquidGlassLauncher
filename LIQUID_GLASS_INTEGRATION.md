# Fossify Launcher — Liquid Glass Integration

This fork applies a comprehensive Liquid Glass overhaul to the real Fossify Launcher
(`org.fossify.home`) using Kyant0's AndroidLiquidGlass library as the low-level effect engine.

## 1. Dependency choice and justification

`gradle/libs.versions.toml`:

```toml
compose = "2026.02.00"          # Compose BOM -> Compose 1.10.3
backdrop = "1.0.6"              # io.github.kyant0:backdrop
```

`app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.compose)
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.backdrop)
}
```

Why `backdrop:1.0.6` instead of `2.0.1`:

* `1.0.6` is published against Kotlin `2.3.10` and Compose `1.10.3`, which matches this
  Fossify Launcher checkout exactly (Kotlin 2.3.10 / KSP 2.3.12).
* `2.0.1` targets Compose Multiplatform 1.12 / Kotlin 2.4.10; KSP 2.4.x is not published,
  so upgrading would break Room/KSP. Compose BOM `2026.02.00` resolves to 1.10.3.

The library ships no high-level widgets, so every visible glass component below is custom code
built on `Modifier.drawBackdrop` + `BackdropEffectScope`.

## 2. New components

| File | Purpose |
| --- | --- |
| `glass/GlassSurfaces.kt` | `LiquidGlassPanel`, `FossifyLiquidDock`, `LiquidGlassDrawer`, `LiquidGlassFolder`, `LiquidGlassWidget` |
| `glass/FossifyLiquidSlider.kt` | `FossifyLiquidSlider` + `LiquidSliderController` with drag, tap-to-set, `onValueChange`, `onValueChangeFinished`, step quantization |
| `glass/GlassComposeHosts.kt` | `ComposeView` bridge helpers: `bindLiquidDock`, `bindLiquidDrawerBackground`, `bindLiquidSearchBar`, `bindLiquidSlider` |
| `helpers/IconPackManager.kt` | Standard intent discovery (`org.adw.launcher.THEMES` etc.), `appfilter.xml` parsing, themed drawable lookup |
| `helpers/IconShape.kt` | Circle / rounded-square / squircle masking for home, dock, drawer and folder icons |

## 3. Features added

* **Liquid dock** — glass bar rendered behind Fossify's existing dock row; Fossify's drag/drop,
  tap, long-press and item placement code is untouched.
* **Liquid drawer + search bar** — glass backdrop in `all_apps_fragment.xml` plus a dedicated
  glass capsule behind the drawer search field.
* **Icon packs** — Settings → Liquid glass → Icon pack lists all installed packs declaring a
  standard theme action. Selecting one parses its `appfilter.xml` (meta-data resource or
  `assets/appfilter.xml`) and applies icons on home, dock and drawer.
* **Grid rows × columns** — Fossify's existing row/column settings are retained and now feed
  the glass dock height and recalculated cell geometry.
* **Icon size slider** — liquid slider, 60–140% in 5% steps; scales `iconSize` in
  `HomeScreenGrid.fillCellSizes()`.
* **Dock icon count slider** — liquid slider, 3–7 icons; filters dock drawing and hit-testing.
* **Icon shape** — default / circle / rounded square / squircle.
* **Glass folders and widgets** — translucent rim/highlight on folder icons, rounded glass
  tint behind widget host views.

Fossify Launcher itself contains no brightness/volume sliders (those live in system UI), so the
new `FossifyLiquidSlider` is the only slider implementation in the app and is used for all new
adjustable settings. Its controller preserves the Android slider callback contract.

## 4. File-by-file modification guide

### Gradle

* `gradle/libs.versions.toml` — Compose BOM, backdrop, Compose artifacts, Compose compiler plugin.
* `build.gradle.kts` — declare Compose plugin `apply false`.
* `app/build.gradle.kts` — apply Compose plugin, enable `compose`, add dependencies.

### New Kotlin files

* `app/src/main/kotlin/org/fossify/home/glass/GlassSurfaces.kt`
* `app/src/main/kotlin/org/fossify/home/glass/FossifyLiquidSlider.kt`
* `app/src/main/kotlin/org/fossify/home/glass/GlassComposeHosts.kt`
* `app/src/main/kotlin/org/fossify/home/helpers/IconPackManager.kt`
* `app/src/main/kotlin/org/fossify/home/helpers/IconShape.kt`

### Modified Kotlin files

* `activities/MainActivity.kt` — bind dock/drawer glass hosts, reload selected icon pack on
  create, refresh grid metrics/dock height on resume, apply pack icon + icon shape in
  `getAllAppLaunchers()`.
* `activities/SettingsActivity.kt` — bind two liquid sliders, add icon pack and icon shape
  pickers, refresh labels, invalidate `IconCache` on visual changes.
* `fragments/AllAppsFragment.kt` — bind the glass search-bar host.
* `views/HomeScreenGrid.kt` — scaled `iconSize` in `fillCellSizes()`, `refreshGridMetrics()`,
  dock-count filtering in draw and hit-testing, shaped icon drawing, glass folder rim,
  glass widget background, pack-aware `getDrawableForPackageName` calls.
* `extensions/Context.kt` — `getDrawableForPackageName(packageName, activityName)` now consults
  `IconPackManager` first.
* `helpers/Config.kt`, `helpers/Constants.kt` — new preference keys and defaults.

### Modified resources

* `res/layout/activity_main.xml` — `liquid_dock_background` ComposeView behind the home grid.
* `res/layout/all_apps_fragment.xml` — `liquid_drawer_background` and `liquid_search_background`.
* `res/layout/activity_settings.xml` — glass settings section with sliders and pickers.
* `res/values/strings.xml` — new labels.

## 5. API-level compatibility

The backdrop effects come from `backdrop:1.0.6` and degrade automatically:

* **API 33+ (Android 13+)** — full refraction/lens, vibrancy and blur.
* **API 31–32 (Android 12–12L)** — `lens()` returns early, `blur()` uses `RenderEffect`;
  glass panels still show blur + tint.
* **API ≤30** — `lens()` and `blur()` are no-ops; the tinted rounded surfaces and icon
  masking remain, so the launcher stays fully usable.

Minimum SDK stays at Fossify's original 26.

## 6. Build

GitHub Actions workflow `Build Launcher` builds `:app:assembleDebug` and uploads the artifact
`fossify-launcher-debug`.
