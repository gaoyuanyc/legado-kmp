package io.legado.shared.analyzeRule

actual class PlatformJsEngine actual constructor() : JsEngine {
    override fun evaluate(script: String): JsValue {
        return JsValue.String("JVM stub")
    }

    override suspend fun evaluateAsync(script: String): JsValue {
        return evaluate(script)
    }

    override fun bindObject(name: String, obj: Any?) {}

    override fun callFunction(name: String, vararg args: Any?): JsValue {
        return JsValue.Null
    }

    override fun close() {}
}
