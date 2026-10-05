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

    Apply it in your shared UI module's `build.gradle.kts` (e.g. `composeApp` or `shared`):

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
    google()
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

Storytale `0.0.6` is compiled and verified against the following toolchains:

| Component | Minimum Supported | Recommended | Notes |
| :--- | :--- | :--- | :--- |
| **Kotlin** | `2.3.0` | `2.3.20` | Required for FIR K2 compiler plugin ABI compatibility. |
| **Compose Multiplatform** | `1.8.0` | `1.10.1` / `1.12.x` | Verified with dynamic Compose Multiplatform profiles. |
| **Android Gradle Plugin (AGP)** | `8.8.0` | `9.1.0` | Supports both `com.android.library` and `com.android.kotlin.multiplatform.library`. |
| **Gradle** | `8.10` | `9.3.1` | Compatible with Gradle 9 lifecycle and process execution APIs. |
| **Android Min SDK** | `24` | `30+` | API 30+ is required if using spaces in story identifiers (D8 DEX 040 format). |
| **JDK Host** | `17` | `21` | Required for Kotlin compiler execution and Gradle daemons. |

!!! note "Story Identifiers & Android DEX 040"
    When defining story identifiers with spaces (e.g. `val \`Confirm Dialog Box\` by story`), Kotlin emits synthetic delegate fields containing spaces. Android D8 DEX format 040 (introduced in API level 30) natively supports arbitrary UTF-8 identifiers. If your app targets `minSdk < 30`, use identifiers without spaces (e.g. `val ConfirmDialogBox by story`) or set `minSdk = 30` in your test/story module.

---

## Next Step

Proceed to **[Writing Your First Story](first-story.md)** to configure your first component story using a project generated from [kmp.new](https://kmp.new).
