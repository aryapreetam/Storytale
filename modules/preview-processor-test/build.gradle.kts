import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    android {
        compileSdk = 35
        namespace = "org.jetbrains.compose.storytale.preview.processor.test"
        minSdk = 24
        withHostTest {}
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(compose.runtime)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.components.uiToolingPreview)
                implementation(project(":modules:preview-processor"))
                implementation(kotlin("test"))

                implementation(compose.runtime)
                implementation(kotlin("compiler-embeddable"))
                implementation(kotlin("compose-compiler-plugin-embeddable"))
                implementation(kotlin("test"))
                implementation(libs.assertj.core)
                implementation(libs.junit)
                implementation(libs.kotlinCompileTesting.ksp)
                implementation(project(":modules:runtime-api"))
            }
        }
        val androidHostTest by getting {
            dependencies {
                implementation("androidx.compose.ui:ui-tooling-preview-android:1.7.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("androidx.compose.ui:ui-tooling-preview-desktop:1.7.0")
            }
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}
