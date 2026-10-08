import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.mavenPublish) apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.asProvider().get()))
    }
}

subprojects {
    group = findProperty("storytale.deploy.group") ?: findProperty("libGroup") ?: "io.github.aryapreetam.storytale"
    version = findProperty("storytale.deploy.version")
        ?: findProperty("libVersion")
        ?: error("'storytale.deploy.version' was not set")

    plugins.withId("com.vanniktech.maven.publish") {
        configureIfExists<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral()
            if (project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey") || project.hasProperty("signing.gnupg.keyName") || project.findProperty("signAllPublications")?.toString()?.toBoolean() == true) {
                signAllPublications()
            }
        }
    }
    plugins.apply(rootProject.libs.plugins.spotless.get().pluginId)
    extensions.configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("src/test/resources/**")
            ktlint(libs.ktlint.get().version)
                .editorConfigOverride(
                    mapOf(
                        "indent_size" to "4",
                        "ktlint_compose_modifier-missing-check" to "disabled",
                        "ktlint_compose_compositionlocal-allowlist" to "disabled",
                    ),
                )
                .customRuleSets(listOf(libs.composeRules.get().toString()))
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(libs.ktlint.get().version)
                .editorConfigOverride(
                    mapOf("indent_size" to "4"),
                )
        }
    }
}

inline fun <reified T : Any> Project.configureIfExists(fn: T.() -> Unit) {
    extensions.findByType(T::class.java)?.fn()
}

gradle.projectsEvaluated {
    tasks.register("publishToMavenCentral") {
        group = "publishing"
        description = "Publish all subprojects and included gradle-plugin to Maven Central"

        subprojects.forEach { subproject ->
            if (subproject.plugins.hasPlugin("com.vanniktech.maven.publish")) {
                dependsOn(subproject.tasks.named("publishToMavenCentral"))
            }
        }
        dependsOn(gradle.includedBuild("gradle-plugin").task(":publishToMavenCentral"))
    }

    tasks.register("publishAndReleaseToMavenCentral") {
        group = "publishing"
        description = "Publish and automatically release all subprojects and included gradle-plugin to Maven Central"

        subprojects.forEach { subproject ->
            if (subproject.plugins.hasPlugin("com.vanniktech.maven.publish")) {
                dependsOn(subproject.tasks.named("publishAndReleaseToMavenCentral"))
            }
        }
        dependsOn(gradle.includedBuild("gradle-plugin").task(":publishAndReleaseToMavenCentral"))
    }

    tasks.register("publishToMavenLocal") {
        group = "publishing"
        description = "Publish all subprojects and included gradle-plugin to Maven Local"

        subprojects.forEach { subproject ->
            if (subproject.plugins.hasPlugin("com.vanniktech.maven.publish")) {
                dependsOn(subproject.tasks.named("publishToMavenLocal"))
            }
        }
        dependsOn(gradle.includedBuild("gradle-plugin").task(":publishToMavenLocal"))
    }
}
