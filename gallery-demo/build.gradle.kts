import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.jetbrainsCompose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.storytale)
  alias(libs.plugins.serialization)
}

configurations.all {
  resolutionStrategy.dependencySubstitution {
    substitute(module("io.github.aryapreetam.storytale:compiler-plugin"))
      .using(project(":modules:compiler-plugin"))
    substitute(module("io.github.aryapreetam.storytale:runtime-api"))
      .using(project(":modules:runtime-api"))
    substitute(module("io.github.aryapreetam.storytale:gallery"))
      .using(project(":modules:gallery"))
  }
}

kotlin {
  val cmpProfile = providers.gradleProperty("cmpProfile").orNull ?: "1.10"

  js {
    browser()
    binaries.executable()
  }
  wasmJs {
    outputModuleName.set("gallery-demo")
    browser {
      commonWebpackConfig {
        outputFileName = "composeApp.js"
      }
    }
    binaries.executable()
  }

  jvm("desktop")

  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  if (cmpProfile != "1.12") {
    iosX64()
  }
  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)
        implementation(libs.navigation.compose)
        implementation(libs.compose.highlights)
        implementation(libs.kotlinx.serialization.json)
        implementation(projects.modules.runtimeApi)
        implementation(projects.modules.gallery)
        implementation(libs.material3.adaptive)
        implementation(libs.material3.icons.core)
      }
    }

    val desktopMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.androidx.activity.compose)
      }
    }
  }

  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  compilerOptions {
    freeCompilerArgs = listOf(
      "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
      "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
      "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
      "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
      "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
      "-opt-in=kotlinx.coroutines.FlowPreview",
      "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
      "-opt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi",
      "-Xexpect-actual-classes",
    )
  }
}

compose.resources {
  packageOfResClass = "storytale.gallery.demo.generated.resources"
}

android {
  namespace = "storytale.gallery.demo"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    applicationId = "storytale.gallery.demo"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
  buildTypes {
    maybeCreate("Stories").apply {
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("debug", "release")
    }
  }
}

composeCompiler {
  featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}
