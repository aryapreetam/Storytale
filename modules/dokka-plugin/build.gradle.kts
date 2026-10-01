/*
 * Copyright 2014-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
}

group = "io.github.aryapreetam.storytale"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.dokka.core)
    implementation(libs.dokka.base)
    implementation(libs.kotlinx.html)

    testImplementation(libs.jsoup)
    testImplementation(libs.dokka.test.api)
    testImplementation(libs.dokka.base.test.utils)
    testImplementation(libs.dokka.base)
    testImplementation(libs.junit.api)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.dokka.analysisKotlinSymbols)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "storytale-dokka-plugin"
            from(components["kotlin"])
        }
        withType<MavenPublication> {
            artifact(emptyJavadocJar)

            pom {
                name.set("Storytale Dokka Plugin")
                description.set("Dokka plugin for Storytale.")
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
    }
}

signing {
    useGpgCmd()
    isRequired = true
    sign(publishing.publications)
}
