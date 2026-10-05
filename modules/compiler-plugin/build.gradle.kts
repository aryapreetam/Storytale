import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.mavenPublish)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}
dependencies {
    compileOnly(compose.runtime)
    compileOnly(kotlin("compiler-embeddable"))
    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(compose.foundation)
    testImplementation(compose.material3)
    testImplementation(compose.runtime)
    testImplementation(compose.ui)
    testImplementation(kotlin("compose-compiler-plugin-embeddable"))
    testImplementation(kotlin("test"))
    testImplementation(libs.assertj.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinCompileTesting.core)
    testImplementation(project(":modules:runtime-api"))
}

sourceSets {
    val main by getting {
        kotlin.srcDirs("src/kotlin")
        resources.srcDir("src/resources")
    }
    val test by getting {
        kotlin.srcDirs("src/test/kotlin")
        resources.srcDir("src/test/resources")
    }
}

group = "io.github.aryapreetam.storytale"

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Empty(),
        ),
    )
    coordinates(group.toString(), "compiler-plugin", version.toString())

    pom {
        name.set("Storytale Compiler Plugin")
        description.set("Kotlin compiler plugin for Storytale.")
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

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}
