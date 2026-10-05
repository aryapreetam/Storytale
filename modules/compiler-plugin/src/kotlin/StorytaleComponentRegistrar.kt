package org.jetbrains.compose.plugin.storytale.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class StorytaleComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean get() = true

    override val pluginId: String = "io.github.aryapreetam.storytale.compiler-plugin"

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        Companion.registerExtensions(this)
    }

    companion object {
        fun registerExtensions(extensionStorage: ExtensionStorage) {
            val extension = StorytaleLoweringExtension()
            val companion = IrGenerationExtension::class.java.getField("Companion").get(null)
            val registerMethod = extensionStorage.javaClass.methods.firstOrNull {
                it.name == "registerExtension" && it.parameterTypes.size == 2
            }
            if (registerMethod != null) {
                registerMethod.invoke(extensionStorage, companion, extension)
            } else {
                with(extensionStorage) {
                    IrGenerationExtension.registerExtension(extension)
                }
            }
        }
    }
}
