---
date: 2026-10-01
description: Storytale 0.0.6 release — component gallery and runner tasks for Compose Multiplatform.
categories:
  - Releases
---

# Storytale 0.0.6: Component Gallery for Compose Multiplatform

Storytale 0.0.6 is released. It provides an isolated component gallery and multiplatform runner tasks for Compose Multiplatform on Desktop (JVM), Web (Wasm), Android, and iOS.

<!-- more -->

Developing UI components inside a full application requires building the whole app and navigating through multiple screens to reach a specific state. Storytale isolates composables into standalone stories with interactive parameters and platform-specific runner tasks.

---

## What's in 0.0.6

### 1. Kotlin 2.2 K2 FIR Compiler Plugin
The story registrar and preview processor integrate with Kotlin 2.2's Frontend Intermediate Representation (FIR):
- Discovers `@Preview` annotations and `by story` delegates during compilation.
- Generates multiplatform entry points at compile time.
- Requires no runtime reflection.

### 2. Multiplatform Runners
Storytale 0.0.6 includes Gradle tasks for each Compose Multiplatform target:
- **Desktop**: Run directly with `./gradlew :desktopStoriesRun`.
- **Web (Wasm)**: Static distribution bundle ready for hosting (`:wasmJsBrowserStoriesProductionExecutableDistribution`).
- **Android**: Launch on emulator or connected device via `./gradlew :androidStoriesRun` (supports both Android applications and multiplatform libraries).
- **iOS**: Simulator discovery and launch via `:iosSimulatorArm64StoriesRun` and `:iosX64StoriesRun`.

### 3. Interactive Parameters
Stories can declare parameters using `parameter(...)`:
```kotlin
val `Primary Action Button` by story(group = "Buttons") {
    val title by parameter("Confirm")
    val enabled by parameter(true)

    Button(onClick = {}, enabled = enabled) {
        Text(title)
    }
}
```
The gallery sidebar generates inputs for strings, booleans, numbers, and enum selections that update the component state in real time.

---

## Setup

Storytale 0.0.6 is published to **Maven Central** and the **Gradle Plugin Portal** under `io.github.aryapreetam.storytale`.

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.aryapreetam.storytale") version "0.0.6"
}
```

See the [Installation Guide](../../getting-started/installation.md) and [Multiplatform Workflows](../../guides/multiplatform.md) for details.
