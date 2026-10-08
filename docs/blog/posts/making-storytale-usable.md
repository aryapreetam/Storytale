---
date: 2026-10-07
description: Announcing Storytale 0.0.7 — isolated component-driven UI development and storybook gallery for Compose Multiplatform.
categories:
  - Releases
---

# Storytale 0.0.7: Component Gallery for Compose Multiplatform

Developing UI components inside a full Compose Multiplatform app usually requires building the entire target binary, launching an emulator or desktop window, and navigating through multiple screens to reach a specific state.

JetBrains started exploring this problem with [Kotlin/Storytale](https://github.com/Kotlin/Storytale), an isolated component explorer for Compose Multiplatform. However, the upstream repository stalled, remained unreleased on Maven Central, and broke as Kotlin and Compose Multiplatform evolved.

<!-- more -->

---

## Background: Why This Fork Exists

Back in April 2026, while building design system components for my own Compose Multiplatform projects, I ran into the need for an isolated component gallery. I got the core compiler plugin and runtime working for Kotlin 2.3 and published an initial `0.0.5` release to Maven Central.

<figure>
  <img class="only-light" src="../../../../../assets/am-components-gallery-light.webp#only-light" alt="Component catalog from a Compose Multiplatform project" />
  <img class="only-dark" src="../../../../../assets/am-components-gallery-dark.webp#only-dark" alt="Component catalog from a Compose Multiplatform project" />
  <figcaption>Component catalog from one of the Compose Multiplatform projects I worked on &mdash; isolating 17+ custom UI components</figcaption>
</figure>

I didn't announce it at the time — the documentation was incomplete, several runner tasks still had rough edges across mobile and web, and I had no idea other developers in the community were looking for a solution to this. A few days ago, after joining the [Storytale channel on Kotlinlang Slack](https://app.slack.com/client/T09229ZC6/C091T6HB20Y), I realized many developers were actively running into the exact same blockers: broken compiler plugins on modern Kotlin, missing runners for mobile and web targets, and no published artifacts to pull from.

Storytale `0.0.7` resolves these toolchain blockers and makes the project usable out of the box across modern Kotlin (2.3–2.4) and Compose Multiplatform (1.10–1.12).

---

## Interactive Live Showcase

Here is the Storytale gallery compiled to WebAssembly (`wasmJs`) running directly in your browser. You can navigate the story catalog on the left and adjust parameters in real time on the right:

<div class="wasm-gallery-wrapper" data-gallery-url="https://aryapreetam.github.io/storytale/gallery/">
  <div class="storytale-gallery-fallback">
    <img class="only-light" src="../../../../../assets/wasm-stories-gallery-light.webp#only-light" alt="Storytale Wasm Gallery Preview" />
    <img class="only-dark" src="../../../../../assets/wasm-stories-gallery-dark.webp#only-dark" alt="Storytale Wasm Gallery Preview" />
  </div>
  <div class="storytale-gallery-container" style="display: none;">
    <div class="storytale-gallery-header">
      <div class="dots">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
      <span>Storytale Interactive Showcase &mdash; Web (Wasm)</span>
      <a class="storytale-gallery-link" href="https://aryapreetam.github.io/storytale/gallery/" target="_blank" rel="noopener" style="font-size: 0.8rem;">Open Fullscreen &nearr;</a>
    </div>
    <iframe class="storytale-gallery-iframe" data-src="https://aryapreetam.github.io/storytale/gallery/" loading="lazy" sandbox="allow-scripts allow-same-origin allow-forms allow-popups" title="Storytale Live Showcase"></iframe>
  </div>
</div>

---

## What Was Broken & Fixed in 0.0.7

Making Storytale usable on modern Compose Multiplatform toolchains required solving several fundamental issues:

### 1. Kotlin 2.2+ FIR Compiler Plugin
Kotlin 2.0+ uses the K2 compiler frontend (FIR — Frontend Intermediate Representation). The upstream plugin relied on older, deprecated compiler APIs that broke on modern Kotlin versions. The compiler plugin has been updated to integrate with modern FIR declaration generation and IR transformation, maintaining ABI stability across Kotlin 2.3 and 2.4.

### 2. Multiplatform Target Runners
Upstream runner tasks failed to execute or package on recent Gradle and Compose Multiplatform releases. Each Compose Multiplatform target runner has been restored and verified:
- **Desktop (JVM)**: `./gradlew :shared:jvmStoriesRun` launches a native desktop window running the gallery.
- **Web (Wasm)**: `./gradlew :shared:wasmJsBrowserStoriesDevelopmentRun` starts a local webpack dev server with hot reload, while `:shared:wasmJsBrowserStoriesProductionExecutableDistribution` builds static production assets.
- **Android**: `./gradlew :shared:androidStoriesRun` builds the gallery test APK and launches the activity directly on connected devices or emulators.
- **iOS Simulator**: `./gradlew :shared:iosSimulatorArm64StoriesRun` (Apple Silicon) and `:shared:iosX64StoriesRun` (Intel) compile native iOS binaries and boot them into the simulator.

<figure>
  <img class="only-light" src="../../../../../assets/jvm-stories-gallery-light.webp#only-light" alt="Storytale Desktop Runner" />
  <img class="only-dark" src="../../../../../assets/jvm-stories-gallery-dark.webp#only-dark" alt="Storytale Desktop Runner" />
  <figcaption>Storytale Desktop (JVM) runner launched via <code>./gradlew :shared:jvmStoriesRun</code></figcaption>
</figure>

### 3. Decoupled Gradle Dependencies (`compileOnly`)
The upstream Gradle plugin declared `implementation(libs.android.gradle.plugin)` and `implementation(libs.compose.gradle.plugin)`, which leaked build-tool dependencies directly into consumer build classpaths. We converted these to `compileOnly` so Storytale operates against whatever AGP and Compose versions the consumer project already applies, without forcing transitive AGP or Gradle version upgrades onto existing builds.

### 4. Public Maven Central & Plugin Portal Artifacts
All artifacts (`compiler-plugin`, `runtime-api`, `gallery`, and `gradle-plugin`) are published to **Maven Central** and the **Gradle Plugin Portal** under `io.github.aryapreetam.storytale`.

---

## Getting Started

Add the plugin to your project:

### 1. Version Catalog & Build Script

In `gradle/libs.versions.toml`:

```toml
[versions]
storytale = "0.0.7"

[plugins]
storytale = { id = "io.github.aryapreetam.storytale", version.ref = "storytale" }
```

In your shared UI module `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.storytale)
}
```

### 2. Run Your Stories

Stories live strictly in `src/commonStories/kotlin/`, separated from production binaries:

```bash
# Desktop
./gradlew :shared:jvmStoriesRun

# Web
./gradlew :shared:wasmJsBrowserStoriesDevelopmentRun

# Android
./gradlew :shared:androidStoriesRun

# iOS Simulator
./gradlew :shared:iosSimulatorArm64StoriesRun
```

For a full step-by-step tutorial covering project setup, story registration, interactive parameters, and platform troubleshooting, see the **[Writing Your First Story](../../getting-started/first-story.md)** guide.

---

## Resources

- **Tutorial**: [Writing Your First Story](../../getting-started/first-story.md)
- **Documentation**: [aryapreetam.github.io/storytale](https://aryapreetam.github.io/storytale/)
- **Starter Template**: [github.com/aryapreetam/storytale-sample](https://github.com/aryapreetam/storytale-sample)
- **GitHub Repository**: [github.com/aryapreetam/storytale](https://github.com/aryapreetam/storytale)
- **Discussion**: [Kotlinlang Slack `#storytale`](https://app.slack.com/client/T09229ZC6/C091T6HB20Y)
