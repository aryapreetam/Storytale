---
title: Storytale — Component Gallery for Compose Multiplatform
---

<h1 align="center" style="border-bottom: none; margin-top: 0;">
  <img src="assets/logo.svg" width="44" height="44" alt="Storytale Logo" align="absmiddle"/>
  <span style="vertical-align: middle; margin-left: 8px;">Storytale</span>
</h1>

<p align="center">Component-driven UI development and story gallery for Compose Multiplatform</p>

<p align="center">
  <a href="https://search.maven.org/artifact/io.github.aryapreetam.storytale/gradle-plugin/0.0.7/jar"><img src="https://img.shields.io/maven-central/v/io.github.aryapreetam.storytale/gradle-plugin?label=Maven%20Central&color=6366F1" alt="Maven Central" /></a>
  <a href="https://github.com/aryapreetam/storytale/actions"><img src="https://img.shields.io/badge/Kotlin%20Multiplatform-2.3+-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin Multiplatform" /></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.10+-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" /></a>
  <a href="https://github.com/aryapreetam/storytale/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" /></a>
</p>

<p align="center" style="font-size: 0.67rem; color: var(--md-default-fg-color--light); width: 100%; margin: 0.5rem 0 1.5rem 0;">
This is a fork of <a href="https://github.com/Kotlin/Storytale" target="_blank" rel="noopener">Kotlin/Storytale</a> supporting Kotlin 2.3+, Compose Multiplatform 1.10+, AGP 9.1, and Maven Central. It will be   archived once upstream is officially released.
</p> 

<p align="center">
  <img width="1604" alt="Storytale running across Desktop, Web, Android, and iOS" src="https://github.com/user-attachments/assets/b9a3d08f-7ff5-4a55-a0fc-904b4279e116" style="border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.12);" />
</p>

### Features

- **Declarative Kotlin DSL**: Declare isolated component previews with `by story`, supporting hierarchical grouping and contextual descriptions.
- **Interactive Parameters**: Adjust text, booleans, numbers, and enum parameters in real time with `parameter()` without recompiling.
- **Compile-Time Discovery**: Zero reflection; stories are synthesized at compile time via a Kotlin FIR compiler plugin.
- **Multiplatform Runners**: Launch your gallery natively on Android, iOS, Desktop (JVM), and Web (Wasm) with dedicated Gradle tasks.

<p style="margin: 1.5rem 0;">
  <a href="getting-started/installation/"><strong>Get Started</strong></a> | <a href="getting-started/first-story/"><strong>Your First Story</strong></a> | <a href="https://aryapreetam.github.io/storytale/api/"><strong>API Reference</strong> ↗</a>
</p>

---

## Quick Start

### 1. Apply the Plugin

In your shared UI module (e.g. `:shared` OR `:app:shared` OR `:composeApp`) `build.gradle.kts`:

```kotlin
plugins {
  id("io.github.aryapreetam.storytale") version "0.0.7"
}
```

### 2. Write a Story

Declare a story in `src/commonStories/kotlin/`:

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import org.jetbrains.compose.storytale.story

val `Primary Action Button` by story(group = "Controls/Buttons") {
  val label by parameter("Confirm Purchase")
  val isEnabled by parameter(true)

  Button(
    onClick = {},
    enabled = isEnabled
  ) {
    Text(label)
  }
}
```

### 3. Run the Gallery

Run the gallery on Desktop, Web, Android, or iOS:

=== "Desktop (JVM)"

    ```bash
    ./gradlew :composeApp:jvmStoriesRun
    ```

=== "Web (Wasm)"

    ```bash
    ./gradlew :composeApp:wasmJsBrowserStoriesDevelopmentRun
    ```

=== "Android"

    ```bash
    ./gradlew :composeApp:androidStoriesRun
    ```

=== "iOS Simulator"

    ```bash
    # Apple Silicon
    ./gradlew :composeApp:iosSimulatorArm64StoriesRun

    # Intel Mac
    ./gradlew :composeApp:iosX64StoriesRun
    ```

---

## Interactive Live Showcase

The gallery running in the browser, built with Compose Multiplatform for WebAssembly (`wasmJs`):

<div class="wasm-gallery-wrapper" data-gallery-url="https://aryapreetam.github.io/storytale/gallery/">
  <div class="storytale-gallery-fallback">
    <img class="only-light" src="assets/wasm-stories-gallery-light.webp#only-light" alt="Storytale Wasm Gallery Preview" />
    <img class="only-dark" src="assets/wasm-stories-gallery-dark.webp#only-dark" alt="Storytale Wasm Gallery Preview" />
  </div>
  <div class="storytale-gallery-container" style="display: none;">
    <div class="storytale-gallery-header">
      <div class="dots">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
      <span>Storytale Wasm Gallery</span>
      <a class="storytale-gallery-link" href="https://aryapreetam.github.io/storytale/gallery/" target="_blank" rel="noopener" style="font-size: 0.8rem; text-decoration: none;">Open Fullscreen &nearr;</a>
    </div>
    <iframe class="storytale-gallery-iframe" data-src="https://aryapreetam.github.io/storytale/gallery/" loading="lazy" sandbox="allow-scripts allow-same-origin allow-forms allow-popups" title="Storytale Wasm Gallery"></iframe>
  </div>
</div>

---

## Documentation

- **[Installation](getting-started/installation.md)**: Repository setup, Gradle configuration, and platform compatibility.
- **[Writing Your First Story](getting-started/first-story.md)**: Step-by-step setup using a standard project from kmp.new.
- **[Parameters & State](guides/parameters.md)**: Expose dynamic controls for text, booleans, and lists.
- **[Decorators & Theming](guides/decorators.md)**: Wrap stories with custom themes, padding, and composition locals.
- **[Multiplatform Workflows](guides/multiplatform.md)**: Dedicated commands and packaging details for each platform.
