package io.legado.shared.analyzeRule

/**
 * HarmonyOS JS engine implementation using QuickJS (via quickjs-kt).
 *
 * STUB: quickjs-kt is not available on Maven Central yet.
 * This is a placeholder that will be replaced with the full implementation
 * once the dependency is resolved.
 *
 * QuickJS is a lightweight, embeddable JS engine that compiles to native code.
 * This implementation runs on HarmonyOS via Kotlin/Native.
 */
actual class PlatformJsEngine actual constructor() : JsEngine {

    override fun evaluate(script: String): JsValue {
        // TODO: Replace with QuickJS implementation
        return JsValue.String("QuickJS not yet available - stub implementation")
    }

    override suspend fun evaluateAsync(script: String): JsValue {
        return evaluate(script)
    }

    override fun bindObject(name: String, obj: Any?) {
        // TODO: Implement QuickJS binding
    }

    override fun callFunction(name: String, vararg args: Any?): JsValue {
        // TODO: Implement QuickJS function call
        return JsValue.Null
    }

    override fun close() {
        // TODO: Release QuickJS resources
    }
}
