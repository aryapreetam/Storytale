import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

val cmpProfile = providers.gradleProperty("cmpProfile").orNull ?: "1.10"

kotlin {
    wasmJs {
        browser()
    }
    js {
        browser()
    }
    if (cmpProfile != "1.12") {
        iosX64()
    }
    iosArm64()
    iosSimulatorArm64()
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    android {
        namespace = "org.jetbrains.compose.storytale.runtime"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
        }
    }
}

group = "io.github.aryapreetam.storytale"

mavenPublishing {
    coordinates(group.toString(), "runtime-api", version.toString())

    pom {
        name.set("Storytale Runtime API")
        description.set("Runtime API used by Storytale stories and generated code.")
        url.set("https://github.com/aryapreetam/Storytale")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("aryapreetam")
                name.set("Preetam Bhosle")
            }
        }

        scm {
            url.set("https://github.com/aryapreetam/Storytale")
            connection.set("scm:git:https://github.com/aryapreetam/Storytale.git")
            developerConnection.set("scm:git:ssh://git@github.com/aryapreetam/Storytale.git")
            tag.set("HEAD")
        }
    }
}

dokka {
    moduleName.set("Storytale Runtime API")
}
