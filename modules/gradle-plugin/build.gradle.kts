import java.util.Properties
import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.buildTimeConfig)
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint(libs.ktlint.get().version)
            .editorConfigOverride(
                mapOf("indent_size" to "4"),
            )
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.ktlint.get().version)
            .editorConfigOverride(
                mapOf("indent_size" to "4"),
            )
    }
}

gradlePlugin {
    website.set("https://github.com/aryapreetam/Storytale")
    vcsUrl.set("https://github.com/aryapreetam/Storytale")
    plugins {
        create("storytale") {
            id = "io.github.aryapreetam.storytale"
            implementationClass = "org.jetbrains.compose.storytale.plugin.StorytaleGradlePlugin"
            displayName = "Storytale Gradle Plugin"
            description = "Interactive component explorer and catalog tool for Compose Multiplatform"
            tags.set(listOf("compose", "multiplatform", "storytale", "gallery", "ui"))
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    implementation(libs.kotlin.poet)
}

fun resolveSharedGradleProperty(name: String): String? {
    providers.gradleProperty(name).orNull?.let { return it }

    val sharedGradleProperties = rootDir.resolve("../../gradle.properties")
    if (sharedGradleProperties.isFile) {
        val properties = Properties()
        sharedGradleProperties.inputStream().use(properties::load)
        properties.getProperty(name)?.let { return it }
    }

    return null
}

fun resolveStorytalePluginVersion(): String = resolveSharedGradleProperty("storytale.plugin.version")
    ?: libs.versions.storytalePluginPublication.get()

fun resolveStorytaleRuntimeVersion(): String = resolveSharedGradleProperty("storytale.runtime.version")
    ?: resolveSharedGradleProperty("storytale.deploy.version")
    ?: error("'storytale.runtime.version' or 'storytale.deploy.version' was not set")

val storytaleRuntimeVersion = resolveStorytaleRuntimeVersion()

group = "io.github.aryapreetam.storytale"
version = resolveStorytalePluginVersion()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

mavenPublishing {
    publishToMavenCentral()
    if (project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey") || project.hasProperty("signing.gnupg.keyName") || project.findProperty("signAllPublications")?.toString()?.toBoolean() == true) {
        signAllPublications()
    }
    coordinates(group.toString(), "gradle-plugin", version.toString())

    pom {
        name.set("Storytale Gradle Plugin")
        description.set("Gradle plugin for Storytale.")
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

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    friendPaths.setFrom(libraries)
}

buildTimeConfig {
    config {
        packageName.set("org.jetbrains.compose.storytale.plugin")
        objectName.set("BuildTimeConfig")
        destination.set(project.layout.buildDirectory.get().asFile)

        configProperties {
            val projectVersion: String by string(storytaleRuntimeVersion)
        }
    }
}
