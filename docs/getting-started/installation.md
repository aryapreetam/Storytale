---
title: Installation — Storytale
---

# Installation

Storytale is distributed via **Maven Central** and the **Gradle Plugin Portal**.

---

## 1. Apply the Gradle Plugin

Add the Storytale Gradle plugin to your project:

=== "Version Catalog (`gradle/libs.versions.toml`)"

    ```toml
    [versions]
    storytale = "0.0.6"

    [plugins]
    storytale = { id = "io.github.aryapreetam.storytale", version.ref = "storytale" }
    ```

    Apply it in your module's `build.gradle.kts`:

    ```kotlin
    plugins {
        alias(libs.plugins.kotlinMultiplatform)
        alias(libs.plugins.composeMultiplatform)
        alias(libs.plugins.storytale)
    }
    ```

=== "Kotlin DSL (`build.gradle.kts`)"

    ```kotlin
    plugins {
        id("io.github.aryapreetam.storytale") version "0.0.6"
    }
    ```

---

## 2. Verify Repositories

Ensure `mavenCentral()` is declared in your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

---

## 3. Platform Compatibility

Storytale `0.0.6` is compiled and verified against:

| Component | Minimum Supported Version | Recommended Version |
| :--- | :--- | :--- |
| **Kotlin** | 2.1.0 | 2.2.0 |
| **Compose Multiplatform** | 1.7.0 | 1.8.0+ / 1.10.1 |
| **Gradle** | 8.5 | 8.10+ |
| **Android Min SDK** | 26 (30 for backticks with spaces) | 30+ |
| **JDK Host** | 17 | 21+ |

> [!NOTE]
> When defining story identifiers with spaces (for example `val \`My Button Story\` by story`), set Android `minSdk = 30` or higher to satisfy Android D8 DEX format 040 requirements.

---

## Next Step

Proceed to **[Writing Your First Story](first-story.md)**.
