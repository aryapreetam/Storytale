rootProject.name = "Storytale"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    val cmpProfile = providers.gradleProperty("cmpProfile").orNull ?: "1.10"
    versionCatalogs {
        create("libs") {
            when (cmpProfile) {
                "1.10" -> {
                    version("kotlin", "2.3.20")
                    version("compose-plugin", "1.10.1")
                }
                "1.12" -> {
                    version("compose-plugin", "1.12.1")
                }
                else -> error("Invalid cmpProfile '$cmpProfile'. Supported values are: '1.10', '1.12'.")
            }

            val kotlinOverride = providers.gradleProperty("kotlinVersion").orNull
            if (!kotlinOverride.isNullOrBlank()) {
                version("kotlin", kotlinOverride)
            }
            val composeOverride = providers.gradleProperty("composeVersion").orNull
            if (!composeOverride.isNullOrBlank()) {
                version("compose-plugin", composeOverride)
            }
        }
    }
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        mavenLocal()
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
                }
            }
            filter {
                includeGroupByRegex("org\\.jetbrains\\.intellij\\.deps.*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://www.jetbrains.com/intellij-repository/releases/")
                }
            }
            filter {
                includeGroup("com.jetbrains.intellij.platform")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

include(":gallery-demo:composeApp")
include(":gallery-demo:androidApp")
include(":modules:gallery")
includeBuild("modules/gradle-plugin")
include(":modules:compiler-plugin")
include(":modules:dokka-plugin")
include(":modules:runtime-api")
include(":modules:preview-processor")
include(":modules:preview-processor-test")
