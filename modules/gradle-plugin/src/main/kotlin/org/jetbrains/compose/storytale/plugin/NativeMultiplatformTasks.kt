@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package org.jetbrains.compose.storytale.plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.task
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.resources.resolve.ResolveResourcesFromDependenciesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.KonanTarget

fun Project.processNativeCompilation(extension: StorytaleExtension, target: KotlinNativeTarget) {
  if (target.konanTarget !in setOf(KonanTarget.IOS_ARM64, KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.IOS_X64)) {
    return
  }
  project.logger.info("Configuring storytale for Kotlin/Native")
  val generatorTask = createNativeStorytaleGenerateSourceTask(extension, target)
  val compilation = createNativeStorytaleCompileTask(extension, target, generatorTask)
  createNativeStorytaleExecTask(compilation, extension, target)
}

private fun Project.createNativeStorytaleCompileTask(
  extension: StorytaleExtension,
  target: KotlinNativeTarget,
  generatorTask: NativeSourceGeneratorTask,
): KotlinNativeCompilation {
  val storytaleBuildDir = extension.getBuildDirectory(target)
  val mainCompilation = target.compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME).get()
  val storytaleCompilation =
    target.compilations.create(StorytaleGradlePlugin.STORYTALE_SOURCESET_SUFFIX) as KotlinNativeCompilation

  storytaleCompilation.associateWith(mainCompilation)
  setupResourceResolvingForTarget(storytaleBuildDir, storytaleCompilation)

  storytaleCompilation.target.apply {
    binaries.framework(StorytaleGradlePlugin.STORYTALE_TASK_GROUP) {
      baseName = StorytaleGradlePlugin.STORYTALE_NATIVE_APP_NAME
      isStatic = true
      compilation = storytaleCompilation
    }
  }

  storytaleCompilation.apply {
    defaultSourceSet.dependsOn(extension.mainStoriesSourceSet)
    defaultSourceSet.kotlin.setSrcDirs(files("$storytaleBuildDir/sources"))

    val resolveDependencyResourcesTask = extension.project.tasks
      .getByName(storytaleCompilation.resolveDependencyResourcesTaskName) as ResolveResourcesFromDependenciesTask

    defaultSourceSet.resources.srcDirs(
      "$storytaleBuildDir/resources",
      resolveDependencyResourcesTask.outputDirectory,
      mainCompilation.defaultSourceSet.resources,
    )

    compileTaskProvider.configure {
      dependsOn(generatorTask)
      dependsOn(resolveDependencyResourcesTask)
    }
  }

  return storytaleCompilation
}

private fun Project.createNativeStorytaleGenerateSourceTask(
  extension: StorytaleExtension,
  target: KotlinNativeTarget,
): NativeSourceGeneratorTask {
  val storytaleBuildDir = extension.getBuildDirectory(target)
  return task<NativeSourceGeneratorTask>("${target.name}${StorytaleGradlePlugin.STORYTALE_GENERATE_SUFFIX}") {
    group = StorytaleGradlePlugin.STORYTALE_TASK_GROUP
    description = "Generate Native source files for '${target.name}'"
    title = target.name
    outputResourcesDir = file("$storytaleBuildDir/resources")
    outputSourcesDir = file("$storytaleBuildDir/sources")
  }
}

private fun Project.createNativeStorytaleExecTask(
  compilation: KotlinNativeCompilation,
  extension: StorytaleExtension,
  target: KotlinNativeTarget,
): Task? {
  if (target.konanTarget !in setOf(KonanTarget.IOS_X64, KonanTarget.IOS_SIMULATOR_ARM64)) return null

  val deviceId = objects.property<String>()
  val targetSuffix = target.name.capitalized()
  val linkTask = tasks.findByPath("link${StorytaleGradlePlugin.STORYTALE_TASK_GROUP.capitalized()}${StorytaleGradlePlugin.LINK_BUILD_VERSION}Framework$targetSuffix") as? KotlinNativeLink
    ?: error("Link task was not created for target ${target.name}")

  val unzipXCodeProjectTask = createUnzipResourceTask(extension)
  val simulatorRegistrationTask = createSimulatorRegistrationTask(unzipXCodeProjectTask, targetSuffix, deviceId)

  val buildTask = createBuildTask(targetSuffix, deviceId, unzipXCodeProjectTask, simulatorRegistrationTask, linkTask, target)
  val platform = if (target.konanTarget in setOf(KonanTarget.IOS_X64, KonanTarget.IOS_SIMULATOR_ARM64)) "iphonesimulator" else "iphoneos"

  val copyResourcesTask = createCopyNativeResourcesTask(platform, target, targetSuffix, compilation, unzipXCodeProjectTask, buildTask)
  val installAppTask = createInstallApplicationToSimulatorTask(deviceId, targetSuffix, platform, unzipXCodeProjectTask, buildTask, copyResourcesTask)

  return task("${target.name}${StorytaleGradlePlugin.STORYTALE_SOURCESET_SUFFIX}Run") {
    group = StorytaleGradlePlugin.STORYTALE_TASK_GROUP
    dependsOn(unzipXCodeProjectTask)
    dependsOn(installAppTask)

    inputs.property("deviceId", deviceId)

    doLast {
      runProcess(
        "/usr/bin/xcrun",
        "simctl",
        "launch",
        deviceId.get(),
        StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_PATH,
        workingDir = unzipXCodeProjectTask.outputDir.get().asFile,
      )
      runProcess("/usr/bin/open", "-a", "Simulator")
    }
  }
}

private fun Project.createUnzipResourceTask(extension: StorytaleExtension): UnzipResourceTask {
  val taskName = "${StorytaleGradlePlugin.STORYTALE_TASK_GROUP}UnzipXCodeProject"
  return (tasks.findByName(taskName) as? UnzipResourceTask) ?: task<UnzipResourceTask>(taskName) {
    resourcePath.set("${StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME}.zip")
    outputDir.set(
      file(
        layout.buildDirectory.get().asFile
          .resolve(extension.buildDir)
          .resolve(StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME),
      ),
    )
  }
}

private fun Project.createSimulatorRegistrationTask(unzipResourceTask: UnzipResourceTask, targetSuffix: String, deviceIdProperty: Property<String>): Task {
  return task("${StorytaleGradlePlugin.STORYTALE_TASK_GROUP}Register$targetSuffix") {
    group = StorytaleGradlePlugin.STORYTALE_TASK_GROUP
    dependsOn(unzipResourceTask)

    doLast {
      val availableDevices = IosSimulatorResolver.listAvailableIosDevices()
      val activeSdkVersion = IosSimulatorResolver.getActiveIosSdkVersion()
      val selectedDevice = IosSimulatorResolver.selectBestDevice(availableDevices, activeSdkVersion)

      project.logger.info("Using iOS simulator: ${selectedDevice.name} (${selectedDevice.udid}) [runtime: ${selectedDevice.runtimeName}, booted: ${selectedDevice.isBooted}]")

      if (!selectedDevice.isBooted) {
        runProcess("/usr/bin/xcrun", "simctl", "boot", selectedDevice.udid)
      }

      deviceIdProperty.set(selectedDevice.udid)
    }
  }
}

internal data class DiscoveredIosDevice(
  val name: String,
  val udid: String,
  val isBooted: Boolean,
  val runtimeName: String,
  val runtimeVersion: String,
)

internal object IosSimulatorResolver {
  fun getActiveIosSdkVersion(): String {
    return runCatching {
      val process = ProcessBuilder("xcrun", "xcodebuild", "-version", "-sdk", "iphonesimulator", "SDKVersion").start()
      val out = process.inputStream.bufferedReader().readText().trim()
      process.waitFor()
      out
    }.getOrDefault("")
  }

  fun listAvailableIosDevices(): List<DiscoveredIosDevice> {
    val process = ProcessBuilder("xcrun", "simctl", "list", "devices", "available").start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()

    val devices = mutableListOf<DiscoveredIosDevice>()
    var currentRuntime = ""
    var currentRuntimeVersion = ""

    output.lineSequence().forEach { line ->
      val trimmed = line.trim()
      if (trimmed.startsWith("-- ") && trimmed.endsWith(" --")) {
        val header = trimmed.removeSurrounding("-- ", " --").trim()
        if (header.startsWith("iOS", ignoreCase = true)) {
          currentRuntime = header
          currentRuntimeVersion = header.substringAfter("iOS", "").trim()
        } else {
          currentRuntime = ""
          currentRuntimeVersion = ""
        }
      } else if (currentRuntime.isNotEmpty() && trimmed.contains("(")) {
        val name = trimmed.substringBefore("(").trim()
        val udid = trimmed.substringAfter("(").substringBefore(")")
        val state = trimmed.substringAfterLast("(").substringBefore(")")
        if (name.isNotEmpty() && udid.contains("-")) {
          devices += DiscoveredIosDevice(
            name = name,
            udid = udid,
            isBooted = state.contains("Booted", ignoreCase = true),
            runtimeName = currentRuntime,
            runtimeVersion = currentRuntimeVersion,
          )
        }
      }
    }
    return devices
  }

  fun selectBestDevice(availableDevices: List<DiscoveredIosDevice>, activeSdkVersion: String): DiscoveredIosDevice {
    if (availableDevices.isEmpty()) {
      throw GradleException("No available iOS Simulators detected. Please install an iOS simulator via Xcode.")
    }

    val booted = availableDevices.firstOrNull { it.isBooted && it.name.startsWith("iPhone", ignoreCase = true) }
      ?: availableDevices.firstOrNull { it.isBooted }
    if (booted != null) return booted

    val activeDevices = if (activeSdkVersion.isNotBlank()) {
      availableDevices.filter { it.runtimeVersion.startsWith(activeSdkVersion) || activeSdkVersion.startsWith(it.runtimeVersion) }
    } else {
      emptyList()
    }.ifEmpty { availableDevices }

    val preferredModels = listOf("iPhone 17", "iPhone 17 Pro", "iPhone 16", "iPhone 16 Pro", "iPhone 15", "iPhone 15 Pro", "iPhone 14")
    for (model in preferredModels) {
      val found = activeDevices.firstOrNull { it.name.equals(model, ignoreCase = true) }
      if (found != null) return found
    }

    return activeDevices.firstOrNull { it.name.startsWith("iPhone", ignoreCase = true) }
      ?: activeDevices.first()
  }
}

private fun Project.createBuildTask(
  targetSuffix: String,
  deviceId: Property<String>,
  unzipResourceTask: UnzipResourceTask,
  simulatorRegistrationTask: Task,
  linkTask: KotlinNativeLink,
  target: KotlinNativeTarget,
): Task {
  return task("${StorytaleGradlePlugin.STORYTALE_TASK_GROUP}Build$targetSuffix") {
    group = StorytaleGradlePlugin.STORYTALE_TASK_GROUP
    dependsOn(unzipResourceTask)
    dependsOn(simulatorRegistrationTask)
    dependsOn(linkTask)

    inputs.property("deviceId", deviceId)

    val xcodeProjectPath = unzipResourceTask.outputDir.get().asFile
    inputs.files(linkTask.outputs.files)
    outputs.dir(xcodeProjectPath.resolve(StorytaleGradlePlugin.DERIVED_DATA_DIRECTORY_NAME))

    doLast {
      val frameworkPath = linkTask.destinationDirectory.asFile.get().path
      val arch = if (target.konanTarget === KonanTarget.IOS_SIMULATOR_ARM64) "arm64" else "x86_64"
      runProcess(
        "/usr/bin/xcodebuild",
        "clean",
        "build",
        "-project",
        "${StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME}/${StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME}.xcodeproj",
        "-scheme",
        StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME,
        "-destination",
        "id=${deviceId.get()}",
        "-derivedDataPath",
        StorytaleGradlePlugin.DERIVED_DATA_DIRECTORY_NAME,
        "ARCHS=$arch",
        "ONLY_ACTIVE_ARCH=NO",
        "FRAMEWORK_SEARCH_PATHS=$frameworkPath",
        workingDir = xcodeProjectPath,
      )
    }
  }
}

private fun Project.createCopyNativeResourcesTask(
  platform: String,
  target: KotlinNativeTarget,
  targetSuffix: String,
  compilation: KotlinNativeCompilation,
  unzipResourceTask: UnzipResourceTask,
  buildTask: Task,
): NativeCopyResourcesTask {
  val frameworkResources = files().apply {
    compilation.allKotlinSourceSets.forAll { from(it.resources.sourceDirectories) }
  }

  return task<NativeCopyResourcesTask>("${StorytaleGradlePlugin.STORYTALE_TASK_GROUP}CopyResources$targetSuffix") {
    dependsOn(frameworkResources)
    dependsOn(unzipResourceTask)
    dependsOn(buildTask)

    xcodeTargetPlatform.set(platform)
    xcodeTargetArchs.set(if (target.konanTarget.architecture === Architecture.X64) "x86_64" else "arm64")
    resourceFiles.set(frameworkResources)

    val appPath = unzipResourceTask.outputDir.get().asFile
      .resolve(StorytaleGradlePlugin.DERIVED_DATA_DIRECTORY_NAME)
      .resolve("Build/Products")
      .resolve("${StorytaleGradlePlugin.LINK_BUILD_VERSION}-$platform")
      .resolve("${StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME}.app")

    outputDir.set(appPath.resolve("compose-resources"))
  }
}

private fun Project.createInstallApplicationToSimulatorTask(
  deviceId: Property<String>,
  targetSuffix: String,
  platform: String,
  unzipResourceTask: UnzipResourceTask,
  buildTask: Task,
  copyResourcesTask: Task,
): Task {
  return task("${StorytaleGradlePlugin.STORYTALE_TASK_GROUP}InstallApp$targetSuffix") {
    group = StorytaleGradlePlugin.STORYTALE_TASK_GROUP
    dependsOn(unzipResourceTask)
    dependsOn(buildTask)
    dependsOn(copyResourcesTask)

    inputs.property("deviceId", deviceId)

    doLast {
      val appPath = "${StorytaleGradlePlugin.DERIVED_DATA_DIRECTORY_NAME}/Build/Products/${StorytaleGradlePlugin.LINK_BUILD_VERSION}-$platform/${StorytaleGradlePlugin.STORYTALE_NATIVE_PROJECT_NAME}.app"
      runProcess(
        "/usr/bin/xcrun",
        "simctl",
        "install",
        deviceId.get(),
        appPath,
        workingDir = unzipResourceTask.outputDir.get().asFile,
      )
    }
  }
}
