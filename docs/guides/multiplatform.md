---
title: Multiplatform Workflows & Tooling — Storytale
---

# Multiplatform Workflows

Storytale provides dedicated runner tasks for Desktop, Web (Wasm), Android, and iOS.

---

## 1. Desktop (JVM)

The Desktop runner launches a native desktop window directly from Gradle.

```bash
./gradlew :composeApp:desktopStoriesRun
```

### Highlights
- Runs directly from Gradle without creating intermediate emulator devices.
- Supports macOS, Linux, and Windows hosts.
- Re-executes instantaneously for quick UI iteration.

---

## 2. Web (WebAssembly / Wasm)

Storytale compiles directly to WebAssembly (`wasmJs`), allowing you to publish interactive storybook galleries to any static website, CDN, or GitHub Pages.

### Development Server
```bash
./gradlew :composeApp:wasmJsBrowserStoriesRun
```
Starts a local development server with live reload.

### Production Static Distribution
```bash
./gradlew :composeApp:wasmJsBrowserStoriesProductionExecutableDistribution
```

The compiled output is emitted to:
```text
build/dist/wasmJs/productionExecutable/
├── index.html
├── skiko.wasm
└── <module-name>.wasm
```

You can deploy these static files to GitHub Pages, Cloudflare Pages, Vercel, or AWS S3.

---

## 3. Android (Device & Emulator)

Storytale supports both **Android Applications** (`com.android.application`) and modern **Android Multiplatform Libraries** (`com.android.kotlin.multiplatform.library` / `com.android.library`).

```bash
./gradlew :composeApp:androidStoriesRun
```

<p align="center">
  <img alt="Storytale running on Android Device" src="../../assets/android_screenshot.webp" style="max-width: 480px; border-radius: 8px; border: 1px solid #CBD5E1; box-shadow: 0 4px 12px rgba(0,0,0,0.12);" />
</p>

### Self-Contained Library Runner
When applied to a library module (such as `:composeApp` or `:shared`):
1. **Device Test Wiring**: Automatically configures the Android `deviceTest` target and generates a synthetic `AndroidManifest.xml` registering `StorytaleAppActivity`.
2. **Deterministic Installation**: Packages the test APK (`<module>-androidTest.apk`) and installs it onto the active device via `adb install -r`.
3. **Activity Launch**: Immediately invokes `am start -n <package>.test/<package>.test.StorytaleAppActivity`.
4. **App Coexistence**: Because the stories APK runs under the test package suffix (`.test`), it **coexists concurrently** with your main host application (`androidApp`) on the same physical phone or emulator without namespace collision.

### Android SDK Configuration

When using Kotlin backtick identifiers with spaces (e.g. ``val `Primary Button State` by story``), Kotlin generates synthetic delegate fields with spaces in their names. 

To ensure the Android D8 desugarer and dexer can package these identifiers, configure your `minSdk` to `30` (Android 11) or higher:

```kotlin
android {
  defaultConfig {
    minSdk = 30
  }
}
```

!!! note "Android DEX Format 040"
    Android DEX format 040 (introduced in API 30) fully supports spaces and arbitrary UTF-8 characters in simple identifier names. If your app must target `minSdk < 30`, use simple identifiers without spaces (e.g. `val PrimaryButtonState by story`).

---

## 4. iOS (Simulator)

Storytale includes built-in iOS simulator synthesis and runner tasks:

=== "Apple Silicon (M1 / M2 / M3 / M4)"

    ```bash
    ./gradlew :composeApp:iosSimulatorArm64StoriesRun
    ```

=== "Intel Mac (x86_64)"

    ```bash
    ./gradlew :composeApp:iosX64StoriesRun
    ```

<p align="center">
  <img alt="Storytale running on iOS Simulator" src="../../assets/ios_screenshot.webp" style="max-width: 420px; border-radius: 8px; border: 1px solid #CBD5E1; box-shadow: 0 4px 12px rgba(0,0,0,0.12);" />
</p>

### Resilient Simulator Resolution
The Storytale Gradle plugin automatically queries `xcrun simctl` to discover currently booted or available simulators (such as an iPhone 16 or iPad Pro). If an active simulator is already open, Storytale reuses it directly without creating duplicate devices.

You can also specify a specific simulator device ID via Gradle property:
```bash
./gradlew :composeApp:iosX64StoriesRun -Pstorytale.ios.simulator.id=<device-udid>
```

### What the Task Does
1. Resolves the active simulator UUID and target architecture (`arm64` or `x86_64`).
2. Compiles the Kotlin/Native debug framework (`StorytaleFramework.framework`).
3. Generates the wrapper Xcode project with proper `Info.plist` bundle identifiers.
4. Builds the `.app` bundle using `xcodebuild` targeting `iphonesimulator`.
5. Installs the app via `xcrun simctl install`.
6. Launches the gallery app in the simulator via `xcrun simctl launch`.

---

## Target Matrix Summary

| Platform | Gradle Task | Runtime Environment | Output Artifact |
| :--- | :--- | :--- | :--- |
| **Desktop (JVM)** | `:desktopStoriesRun` | JVM Window | JVM Process |
| **Web (Wasm)** | `:wasmJsBrowserStoriesRun` | Browser (Wasm GC) | Static `.wasm` & `.html` |
| **Android** | `:androidStoriesRun` | Device / Emulator via ADB | `<module>-androidTest.apk` |
| **iOS** | `:iosSimulatorArm64StoriesRun` / `:iosX64StoriesRun` | iOS Simulator via `simctl` | iOS `.app` bundle |
