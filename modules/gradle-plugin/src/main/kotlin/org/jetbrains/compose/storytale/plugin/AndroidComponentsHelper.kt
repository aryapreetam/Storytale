package org.jetbrains.compose.storytale.plugin

import java.io.File
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal object AndroidComponentsHelper {

    fun onVariant(
        project: Project,
        variantName: String,
        onResolved: (variantName: String, applicationId: Provider<String>) -> Unit,
    ) {
        // 1. Try modern AndroidComponentsExtension (AGP 8.x / 9.x)
        val components = project.extensions.findByName("androidComponents")
        if (components != null) {
            try {
                val onVariantsMethod = components.javaClass.methods.firstOrNull {
                    it.name == "onVariants" && it.parameterCount == 2 && it.parameterTypes[1] == Action::class.java
                } ?: components.javaClass.methods.firstOrNull {
                    it.name == "onVariants" && it.parameterCount == 1 && it.parameterTypes[0] == Action::class.java
                }

                if (onVariantsMethod != null) {
                    val action = object : Action<Any> {
                        override fun execute(variant: Any) {
                            try {
                                val getNameMethod = variant.javaClass.methods.firstOrNull { it.name == "getName" && it.parameterCount == 0 }
                                val name = getNameMethod?.invoke(variant) as? String
                                if (name == variantName) {
                                    val getAppIdMethod = variant.javaClass.methods.firstOrNull { it.name == "getApplicationId" && it.parameterCount == 0 }

                                    @Suppress("UNCHECKED_CAST")
                                    val appIdProvider = (getAppIdMethod?.invoke(variant) as? Provider<String>)
                                        ?: project.provider { resolveFallbackApplicationId(project, variantName) }
                                    onResolved(name, appIdProvider)
                                }
                            } catch (e: Throwable) {
                                project.logger.debug("Storytale AndroidComponentsHelper: Error in onVariants callback", e)
                            }
                        }
                    }

                    if (onVariantsMethod.parameterCount == 2) {
                        val selectorMethod = components.javaClass.getMethod("selector")
                        val selector = selectorMethod.invoke(components)
                        val withBuildTypeMethod = selector.javaClass.methods.firstOrNull { it.name == "withBuildType" && it.parameterCount == 1 }
                        val targetedSelector = withBuildTypeMethod?.invoke(selector, variantName) ?: selector.javaClass.getMethod("all").invoke(selector)
                        onVariantsMethod.invoke(components, targetedSelector, action)
                        return
                    } else {
                        onVariantsMethod.invoke(components, action)
                        return
                    }
                }
            } catch (e: Throwable) {
                project.logger.debug("Storytale AndroidComponentsHelper: Failed to register onVariants via modern API", e)
            }
        }

        // 2. Legacy fallback for AGP 8.x
        val android = project.extensions.findByName("android")
        if (android != null) {
            try {
                val getApplicationVariants = android.javaClass.methods.firstOrNull { it.name == "getApplicationVariants" }
                if (getApplicationVariants != null) {
                    val variants = getApplicationVariants.invoke(android)
                    val configureEachMethod = variants?.javaClass?.methods?.firstOrNull { it.name == "configureEach" && it.parameterCount == 1 }
                    val action = object : Action<Any> {
                        override fun execute(variant: Any) {
                            try {
                                val name = variant.javaClass.getMethod("getName").invoke(variant) as? String
                                if (name == variantName) {
                                    val appId = (variant.javaClass.getMethod("getApplicationId").invoke(variant) as? String)
                                        ?: resolveFallbackApplicationId(project, variantName)
                                    onResolved(name, project.provider { appId })
                                }
                            } catch (e: Throwable) {
                                project.logger.debug("Storytale AndroidComponentsHelper: Error in legacy variant callback", e)
                            }
                        }
                    }
                    if (configureEachMethod != null) {
                        configureEachMethod.invoke(variants, action)
                        return
                    }
                }
            } catch (e: Throwable) {
                project.logger.debug("Storytale AndroidComponentsHelper: Legacy applicationVariants fallback failed", e)
            }
        }

        // 3. Fallback: project evaluation callback
        project.afterEvaluate {
            val appId = resolveFallbackApplicationId(project, variantName)
            onResolved(variantName, project.provider { appId })
        }
    }

    fun onDeviceTestVariant(
        project: Project,
        manifestFile: File? = null,
        sourcesDir: File? = null,
        onResolved: (variantName: String, applicationId: Provider<String>) -> Unit,
    ) {
        val components = project.extensions.findByName("androidComponents")
        if (components != null) {
            try {
                val onVariantsMethod = components.javaClass.methods.firstOrNull {
                    it.name == "onVariants" && it.parameterCount == 2 && it.parameterTypes[1] == Action::class.java
                } ?: components.javaClass.methods.firstOrNull {
                    it.name == "onVariants" && it.parameterCount == 1 && it.parameterTypes[0] == Action::class.java
                }

                if (onVariantsMethod != null) {
                    val action = object : Action<Any> {
                        override fun execute(variant: Any) {
                            try {
                                // 1. Check if variant has deviceTests (HasDeviceTests / HasAndroidTest)
                                val getDeviceTests = variant.javaClass.methods.firstOrNull { it.name == "getDeviceTests" }
                                val deviceTests = getDeviceTests?.invoke(variant) as? Map<*, *>
                                val deviceTest = deviceTests?.values?.firstOrNull() ?: run {
                                    val getAndroidTest = variant.javaClass.methods.firstOrNull { it.name == "getAndroidTest" }
                                    getAndroidTest?.invoke(variant)
                                }

                                if (deviceTest != null) {
                                    val getNameMethod = deviceTest.javaClass.methods.firstOrNull { it.name == "getName" && it.parameterCount == 0 }
                                    val name = getNameMethod?.invoke(deviceTest) as? String ?: "androidDeviceTest"
                                    val getAppIdMethod = deviceTest.javaClass.methods.firstOrNull { it.name == "getApplicationId" && it.parameterCount == 0 }

                                    @Suppress("UNCHECKED_CAST")
                                    val appIdProvider = (getAppIdMethod?.invoke(deviceTest) as? Provider<String>)
                                        ?: project.provider { resolveFallbackApplicationId(project, "test") }

                                    if (manifestFile != null || sourcesDir != null) {
                                        try {
                                            val getSourcesMethod = deviceTest.javaClass.methods.firstOrNull { it.name == "getSources" }
                                            val sources = getSourcesMethod?.invoke(deviceTest)
                                            if (sources != null) {
                                                if (manifestFile != null) {
                                                    val getManifestsMethod = sources.javaClass.methods.firstOrNull { it.name == "getManifests" }
                                                    val manifests = getManifestsMethod?.invoke(sources)
                                                    val addStaticManifestMethod = manifests?.javaClass?.methods?.firstOrNull {
                                                        it.name == "addStaticManifestFile" && it.parameterCount == 1
                                                    }
                                                    addStaticManifestMethod?.invoke(manifests, manifestFile.absolutePath)
                                                }
                                                if (sourcesDir != null) {
                                                    val getKotlinMethod = sources.javaClass.methods.firstOrNull { it.name == "getKotlin" }
                                                    val kotlinSources = getKotlinMethod?.invoke(sources)
                                                    val addStaticSourceMethod = kotlinSources?.javaClass?.methods?.firstOrNull {
                                                        it.name == "addStaticSourceDirectory" && it.parameterCount == 1
                                                    }
                                                    addStaticSourceMethod?.invoke(kotlinSources, sourcesDir.absolutePath)
                                                }
                                            }
                                        } catch (e: Throwable) {
                                            project.logger.debug("Storytale: could not register sources via AGP Sources API", e)
                                        }
                                    }

                                    onResolved(name, appIdProvider)
                                    return
                                }

                                // 2. Check if variant itself is AndroidTest / DeviceTest
                                val getNameMethod = variant.javaClass.methods.firstOrNull { it.name == "getName" && it.parameterCount == 0 }
                                val variantName = getNameMethod?.invoke(variant) as? String
                                if (variantName?.contains("deviceTest", ignoreCase = true) == true ||
                                    variantName?.contains("androidTest", ignoreCase = true) == true
                                ) {
                                    val getAppIdMethod = variant.javaClass.methods.firstOrNull { it.name == "getApplicationId" && it.parameterCount == 0 }

                                    @Suppress("UNCHECKED_CAST")
                                    val appIdProvider = (getAppIdMethod?.invoke(variant) as? Provider<String>)
                                        ?: project.provider { resolveFallbackApplicationId(project, "test") }
                                    onResolved(variantName, appIdProvider)
                                }
                            } catch (e: Throwable) {
                                project.logger.debug("Storytale AndroidComponentsHelper: Error in onDeviceTestVariant callback", e)
                            }
                        }
                    }

                    val selectorMethod = components.javaClass.getMethod("selector")
                    val selector = selectorMethod.invoke(components)
                    val allMethod = selector.javaClass.getMethod("all")
                    val allSelector = allMethod.invoke(selector)
                    if (onVariantsMethod.parameterCount == 2) {
                        onVariantsMethod.invoke(components, allSelector, action)
                    } else {
                        onVariantsMethod.invoke(components, action)
                    }
                    return
                }
            } catch (e: Throwable) {
                project.logger.debug("Storytale AndroidComponentsHelper: Failed to register onDeviceTestVariant", e)
            }
        }

        project.afterEvaluate {
            val fallbackAppId = resolveFallbackApplicationId(project, "test")
            onResolved("androidDeviceTest", project.provider { fallbackAppId })
        }
    }

    private fun resolveFallbackApplicationId(project: Project, variantName: String): String {
        val android = project.extensions.findByName("android")
        if (android != null) {
            try {
                val getDefaultConfig = android.javaClass.methods.firstOrNull { it.name == "getDefaultConfig" }
                val defaultConfig = getDefaultConfig?.invoke(android)
                val getApplicationId = defaultConfig?.javaClass?.methods?.firstOrNull { it.name == "getApplicationId" }
                val baseAppId = getApplicationId?.invoke(defaultConfig) as? String
                if (!baseAppId.isNullOrBlank()) {
                    return "$baseAppId.$variantName"
                }
                val getNamespace = android.javaClass.methods.firstOrNull { it.name == "getNamespace" }
                val namespace = getNamespace?.invoke(android) as? String
                if (!namespace.isNullOrBlank()) {
                    return "$namespace.$variantName"
                }
            } catch (_: Throwable) {}
        }
        return "storytale.$variantName"
    }

    fun resolveAdbPath(project: Project): String {
        val androidHome = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
        if (!androidHome.isNullOrBlank()) {
            val adb = File(androidHome, "platform-tools/adb")
            if (adb.isFile && adb.canExecute()) {
                return adb.absolutePath
            }
        }
        val android = project.extensions.findByName("android")
        if (android != null) {
            try {
                val getAdbExecutable = android.javaClass.methods.firstOrNull { it.name == "getAdbExecutable" }
                val adbFile = getAdbExecutable?.invoke(android) as? File
                if (adbFile != null && adbFile.isFile) {
                    return adbFile.absolutePath
                }
            } catch (_: Throwable) {}
        }
        return "adb"
    }

    fun resolveActiveDeviceSerial(adbPath: String): String? {
        return try {
            val output = runProcessIgnoreExit(adbPath, "devices")
            val devices = output.trim().lines()
                .drop(1)
                .filter { it.isNotBlank() && it.contains("\tdevice") }
                .map { it.split("\t")[0].trim() }

            devices.firstOrNull { !it.startsWith("emulator-") } ?: devices.firstOrNull()
        } catch (_: Throwable) {
            null
        }
    }
}
