# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.7] - 2026-10-08

### Added
- **Discrete & Enum Parameter Controls**: Added interactive selection chips (`InputChip` in `LazyRow`) for `List<T>` discrete options and Kotlin enum types (`enumEntries`), with overflow modal sheet selection support.
- **Side-by-Side Theme Wrappers**: Added responsive CSS grid comparison layout for evaluating light and dark theme decorators side by side.

### Fixed & Resolved Issues
- **Decoupled Gradle Dependencies (`compileOnly`)**: Converted `android.gradle.plugin`, `kotlin.gradle.plugin`, and `compose.gradle.plugin` from `implementation` to `compileOnly` in the Gradle plugin, eliminating transitive AGP 9.1 and Gradle 9.3+ build-tool leakage into consumer build classpaths.
- **Root Publication Task Resolution**: Fixed root `publishAndReleaseToMavenCentral` and `publishToMavenLocal` tasks in `build.gradle.kts` by migrating from lazy `tasks.matching` to explicit `tasks.named`, guaranteeing all subprojects (`:modules:compiler-plugin`, `:modules:runtime-api`, `:modules:gallery`) and the Gradle plugin are automatically discovered and published in a single command.
- **Theme-Aware Documentation Assets**: Fixed universal theme toggling across Material for MkDocs color schemes (`default` and `slate`), eliminating duplicate stacked screenshots.

## [0.0.6] - 2026-10-06

### Added
- **Dynamic Web Script Resolution**: Replaced hardcoded `composeApp.js` references with dynamic script filename resolution based on project and compilation naming.
- **Interactive Documentation Site**: Added comprehensive Material for MkDocs documentation, step-by-step onboarding guide, and embedded WebAssembly live gallery showcase with offline fallback.
- **Automated Publishing Workflows**: Added GitHub Actions workflows for automated Sonatype Maven Central releases and Gradle Plugin Portal publication under `io.github.aryapreetam.storytale`.
- **Dokka API Documentation**: Configured automated HTML API documentation generation for `:modules:runtime-api` via Dokka 2.1.

### Fixed & Resolved Issues
- **K2 Compiler Plugin Migration**: Updated compiler plugin to Kotlin 2.3+ K2 frontend (FIR declaration generation, IR lowering extension, and reflection-based component registration).
- **AGP 9.1 & Gradle 9 Compatibility**: Modernized Gradle plugin with reflection-based Android variant resolution (`AndroidComponentsHelper`), ProcessBuilder execution, and activity manifest expansion.
- **iOS Simulator Runner Modernization**: Fixed simulator discovery on Xcode 16+, added booted-device fallback selection, and supplied explicit `ARCHS` (`arm64` / `x86_64`) flags to `xcodebuild`.
- **Multiplatform Runner Verification**: Verified runner tasks across Desktop (`:shared:jvmStoriesRun`), Web (`:shared:wasmJsBrowserStoriesDevelopmentRun` and production distributions), Android (`:shared:androidStoriesRun`), and iOS Simulator (`:shared:iosSimulatorArm64StoriesRun` / `:shared:iosX64StoriesRun`).
