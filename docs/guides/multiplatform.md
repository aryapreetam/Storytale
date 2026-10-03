---
title: Multiplatform Workflows & Tooling — Storytale
---

# Multiplatform Workflows

Storytale provides dedicated runner tasks for Desktop, Web (Wasm), Android, and iOS.

---

## 1. Desktop (JVM)

The Desktop runner launches a window directly from Gradle.

```bash
./gradlew :desktopStoriesRun
```

### Details
- Launches a desktop window directly from Gradle.
- Supports macOS, Linux, and Windows.

---

## 2. Web (WebAssembly / Wasm)

Storytale compiles directly to WebAssembly (`wasmJs`), allowing you to publish interactive storybook galleries to any static website, CDN, or GitHub Pages.

### Development Server
```bash
./gradlew :wasmJsBrowserStoriesRun
```
Starts a local development server with live reload.

### Production Static Distribution
```bash
./gradlew :wasmJsBrowserStoriesProductionExecutableDistribution
```

The compiled output is emitted to:
```
build/dist/wasmJs/productionExecutable/
├── index.html
├── skiko.wasm
└── <module-name>.wasm
```

You can deploy these static files to GitHub Pages, Cloudflare Pages, Vercel, or AWS S3.

---

## 3. Android (Emulator & Physical Device)

Storytale supports both **Android Applications** (`com.android.application`) and **Android Multiplatform Libraries** (`com.android.library`).

```bash
./gradlew :androidStoriesRun
```

### Automatic Workflow
1. Detects connected physical devices or running emulators via `adb devices`.
2. Assembles the Storytale gallery APK (`<module>-Stories.apk`).
3. Installs the APK onto the active device via `adb install -r`.
4. Launches the synthesized `StorytaleAppActivity` immediately.

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

> [!NOTE]
> Android DEX format 040 (introduced in API 30) fully supports spaces and arbitrary UTF-8 characters in simple identifier names.

---

## 4. iOS (Simulator)

Storytale includes built-in iOS simulator synthesis and runner tasks:

=== "Apple Silicon (M1 / M2 / M3 / M4)"

    ```bash
    ./gradlew :iosSimulatorArm64StoriesRun
    ```

=== "Intel Mac (x86_64)"

    ```bash
    ./gradlew :iosX64StoriesRun
    ```

### Resilient Simulator Resolution
The Storytale Gradle plugin automatically queries `xcrun simctl` to discover currently booted or available simulators (such as an iPhone 16 or iPad Pro). If an active simulator is already open, Storytale reuses it directly without creating fragile duplicate devices.

### What the Task Does
1. Resolves the active simulator UUID and architecture.
2. Compiles the Kotlin/Native framework (`StorytaleFramework.framework`).
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
| **Android** | `:androidStoriesRun` | Device / Emulator via ADB | Standalone APK |
| **iOS** | `:iosSimulatorArm64StoriesRun` / `:iosX64StoriesRun` | iOS Simulator via `simctl` | iOS `.app` bundle |
