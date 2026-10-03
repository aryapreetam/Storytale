---
title: Storytale — Component Gallery for Compose Multiplatform
---

# Storytale

<p align="center">
  <strong>Component-driven UI development and story gallery for Compose Multiplatform.</strong>
</p>

<p align="center">
  <a href="https://search.maven.org/artifact/io.github.aryapreetam.storytale/gradle-plugin/0.0.6/jar"><img src="https://img.shields.io/maven-central/v/io.github.aryapreetam.storytale/gradle-plugin?label=Maven%20Central&color=6366F1" alt="Maven Central" /></a>
  <a href="https://github.com/aryapreetam/storytale/actions"><img src="https://img.shields.io/badge/Kotlin%20Multiplatform-2.2+-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin Multiplatform" /></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.8+-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform" /></a>
  <a href="https://github.com/aryapreetam/storytale/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" /></a>
</p>

---

Storytale lets you render and inspect Compose UI components in isolation across **Desktop (JVM)**, **Web (Wasm)**, **Android**, and **iOS**. Components are discovered at compile time using a Kotlin K2 compiler plugin, with no manual registry configuration or runtime reflection.

<div class="feature-grid">
  <div class="feature-card">
    <h3>Kotlin DSL</h3>
    <p>Declare stories with <code>by story</code>. Supports category grouping, descriptions, and interactive parameters.</p>
  </div>
  <div class="feature-card">
    <h3>Multiplatform Runners</h3>
    <p>Run your component gallery directly on Desktop (JVM), Web (Wasm), Android Emulators, and iOS Simulators using Gradle tasks.</p>
  </div>
  <div class="feature-card">
    <h3>Interactive Parameters</h3>
    <p>Adjust component arguments—text, booleans, enums, numbers—in real time using <code>parameter()</code> without recompiling.</p>
  </div>
  <div class="feature-card">
    <h3>Compile-Time Registration</h3>
    <p>Story registration happens at compile time via a Kotlin FIR compiler plugin, avoiding runtime reflection.</p>
  </div>
</div>

---

## Interactive Live Showcase

Here is the gallery running in the browser, built with Compose Multiplatform for WebAssembly (`wasmJs`):

<div class="storytale-gallery-container">
  <div class="storytale-gallery-header">
    <div class="dots">
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
    </div>
    <span>Storytale Wasm Gallery</span>
    <a href="gallery/" target="_blank" style="font-size: 0.8rem; text-decoration: none;">Open Fullscreen ↗</a>
  </div>
  <iframe class="storytale-gallery-iframe" src="gallery/index.html" loading="lazy"></iframe>
</div>

---

## Quick Example

Declare a story in `src/commonStories/kotlin/`:

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import storytale.story
import storytale.parameter

val `Primary Action Button` by story(
    group = "Controls/Buttons",
    description = "Primary button with customizable label"
) {
    val label by parameter("label", defaultValue = "Confirm Purchase")
    val isEnabled by parameter("enabled", defaultValue = true)

    Button(
        onClick = { },
        enabled = isEnabled
    ) {
        Text(label)
    }
}
```

Run the gallery on Desktop:

```bash
./gradlew :desktopStoriesRun
```

---

## Documentation

- **[Installation](getting-started/installation.md)**: Add the Gradle plugin to your project.
- **[Writing Your First Story](getting-started/first-story.md)**: Structure and group component stories.
- **[Interactive Parameters](guides/parameters.md)**: Add dynamic controls to stories.
- **[Decorators & Theming](guides/decorators.md)**: Apply themes and layout wrappers.
- **[Multiplatform Workflows](guides/multiplatform.md)**: Run stories on Desktop, Wasm, Android, and iOS.
- **[API Reference](https://aryapreetam.github.io/storytale/api/)**: Dokka API reference.
