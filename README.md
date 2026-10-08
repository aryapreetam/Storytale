![Hero Image](https://github.com/user-attachments/assets/b90b5776-f2f4-4385-8b7d-94eb912eacdf)

# Storytale

Component-driven UI development and story gallery for Compose Multiplatform.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.aryapreetam.storytale/gradle-plugin?label=Maven%20Central&color=6366F1)](https://search.maven.org/artifact/io.github.aryapreetam.storytale/gradle-plugin/0.0.7/jar)
[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-2.3+-7F52FF?logo=kotlin&logoColor=white)](https://github.com/aryapreetam/storytale)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10+-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> [!NOTE]
> **Fork Notice**: This is a maintained fork of [Kotlin/Storytale](https://github.com/Kotlin/Storytale) supporting Kotlin 2.3+, Compose Multiplatform 1.10+, AGP 9.1+, and Maven Central distribution. It will be archived once upstream is officially released.

Storytale isolates your `@Composable` components and compiles a standalone, interactive component gallery app running across **Android, iOS, Desktop (JVM), and Web (Wasm)**.

[Your first story ↗](https://aryapreetam.github.io/storytale/getting-started/first-story) | [Read the Documentation ↗](https://aryapreetam.github.io/storytale/) | [Live Web Gallery Demo ↗](https://aryapreetam.github.io/storytale/gallery/)

<img width="1604" alt="All platforms" src="https://github.com/user-attachments/assets/b9a3d08f-7ff5-4a55-a0fc-904b4279e116">

---

## ⚙️ Getting Started

### 1. Add the Plugin

Storytale is published to **Maven Central** and the **Gradle Plugin Portal**.

#### Using Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
storytale = "0.0.7"

[plugins]
storytale = { id = "io.github.aryapreetam.storytale", version.ref = "storytale" }
```

In your shared UI module (`composeApp/build.gradle.kts` or `shared/build.gradle.kts`):

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.storytale)
}
```

Ensure `mavenCentral()` and `gradlePluginPortal()` are declared in your `settings.gradle.kts`.

---

### 2. Create a Stories Source Set

Storytale isolates gallery and test code in dedicated story source sets alongside production code:

```text
shared/
└── src/
    ├── commonMain/kotlin/…        # Production components
    └── commonStories/kotlin/     # Story definitions
```

Use `commonStories` for stories shared across all platforms, or target-specific sets like `androidStories`, `iosStories`, `desktopStories`, or `wasmStories`.

---

### 3. Write a Story

In `src/commonStories/kotlin/PrimaryButton.story.kt`:

```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.storytale.story

@Composable
fun PrimaryButton(onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled) {
        Text("Click me!")
    }
}

val `Primary Button default state` by story {
    val enabled by parameter(true)
    PrimaryButton(onClick = {}, enabled = enabled)
}
```

---

### 4. Run the Story Gallery

Execute the runner task for your target platform:

```bash
# Desktop (JVM)
./gradlew desktopStoriesRun  # OR ./gradlew jvmStoriesRun 

# Web (Wasm)
./gradlew wasmJsBrowserStoriesDevelopmentRun

# Android (device or emulator)
./gradlew androidStoriesRun

# iOS Simulator
./gradlew iosSimulatorArm64StoriesRun
```

---

## Toolchain Compatibility

| Component | Supported Version | Notes |
| :--- | :--- | :--- |
| **Kotlin** | `2.3.0`+ | Compatible with FIR K2 compiler plugin ABI. |
| **Compose Multiplatform** | `1.8.0` – `1.12.x` | Verified on `1.10.1` and `1.12.0`. |
| **Android Gradle Plugin** | `8.8.0` – `9.1.0` | Compatible with AGP 9.1 and Kotlin Multiplatform Android libraries. |
| **Gradle** | `8.10` – `9.3+` | Configuration-cache compatible tasks. |
| **JDK Host** | `17` or `21` | Required for Gradle daemon and Kotlin compiler execution. |

---

## Documentation

Full guides and references are hosted at **[aryapreetam.github.io/storytale](https://aryapreetam.github.io/storytale/)**:
- [Installation Guide](https://aryapreetam.github.io/storytale/getting-started/installation/)
- [Writing Your First Story](https://aryapreetam.github.io/storytale/getting-started/first-story/)
- [Parameters Guide](https://aryapreetam.github.io/storytale/guides/parameters/)
- [Decorators Guide](https://aryapreetam.github.io/storytale/guides/decorators/)
- [Multiplatform Workflows](https://aryapreetam.github.io/storytale/guides/multiplatform/)

---

## Building Locally

```bash
# Publish artifacts to local Maven repository
./gradlew publishToMavenLocal

# Run checks across all modules
./gradlew check
```

---

## License & Upstream

Storytale is distributed under the [Apache 2.0 License](LICENSE).  
Upstream repository: [Kotlin/Storytale](https://github.com/Kotlin/Storytale).
