package io.legado.shared.analyzeRule

/**
 * Platform-agnostic JS engine interface.
 * 
 * Android implementation: Mozilla Rhino (preserves existing book source compatibility)
 * HarmonyOS implementation: QuickJS via quickjs-kt (compiled to native)
 */
interface JsEngine {
    /**
     * Evaluate a JavaScript script and return the result.
     */
    fun evaluate(script: String): JsValue

    /**
     * Evaluate a JavaScript script asynchronously.
     */
    suspend fun evaluateAsync(script: String): JsValue

    /**
     * Bind a Kotlin object to the JS context with the given name.
     */
    fun bindObject(name: String, obj: Any?)

    /**
     * Call a JS function by name with the given arguments.
     */
    fun callFunction(name: String, vararg args: Any?): JsValue

    /**
     * Inject the Rhino compatibility shim (only needed for QuickJS on HarmonyOS).
     */
    fun injectCompatShim() {
        evaluate(RhinoCompatShim.SHIM_CODE)
    }

    /**
     * Release resources.
     */
    fun close()
}

/**
 * Represents a value returned from JS evaluation.
 */
sealed class JsValue {
    data class String(val value: kotlin.String) : JsValue()
    data class Number(val value: Double) : JsValue()
    data class Boolean(val value: kotlin.Boolean) : JsValue()
    data class Object(val value: Map<kotlin.String, JsValue?>) : JsValue()
    data class Array(val value: List<JsValue?>) : JsValue()
    object Null : JsValue()
    object Undefined : JsValue()

    fun asString(): kotlin.String = when (this) {
        is JsValue.String -> value
        is JsValue.Number -> if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
        is JsValue.Boolean -> value.toString()
        is JsValue.Object -> value.toString()
        is JsValue.Array -> value.toString()
        JsValue.Null -> "null"
        JsValue.Undefined -> "undefined"
    }

    fun asNumber(): kotlin.Double = when (this) {
        is JsValue.Number -> value
        is JsValue.String -> value.toDoubleOrNull() ?: 0.0
        is JsValue.Boolean -> if (value) 1.0 else 0.0
        else -> 0.0
    }

    fun asBoolean(): kotlin.Boolean = when (this) {
        is JsValue.Boolean -> value
        is JsValue.Number -> value != 0.0
        is JsValue.String -> value.isNotEmpty() && value != "false"
        JsValue.Null, JsValue.Undefined -> false
        else -> true
    }
}

/**
 * Factory to create platform-specific JS engine instances.
 */
expect class PlatformJsEngine() : JsEngine

/**
 * Book source rule executor — the core domain logic shared across platforms.
 * 
 * This class is platform-agnostic and delegates JS execution to the platform-specific engine.
 */
class RuleExecutor(private val engine: PlatformJsEngine) {

    /**
     * Execute a book source rule script with the given context.
     * 
     * @param ruleScript The JS rule script from a book source
     * @param context The execution context (baseUrl, html, result, etc.)
     * @return The evaluation result
     */
    fun executeRule(ruleScript: String, context: JsContext): JsValue {
        // Inject Rhino compat shim if needed
        engine.injectCompatShim()

        // Bind context objects
        engine.bindObject("result", context.result)
        engine.bindObject("baseUrl", context.baseUrl)
        engine.bindObject("html", context.html)
        engine.bindObject("baseUrl", context.baseUrl)
        engine.bindObject("src", context.html)
        engine.bindObject("title", context.title)
        engine.bindObject("chapterName", context.chapterName)

        return engine.evaluate(ruleScript)
    }

    fun close() {
        engine.close()
    }
}

/**
 * Execution context for rule evaluation.
 */
data class JsContext(
    val baseUrl: String = "",
    val html: String = "",
    val result: Map<String, Any?> = emptyMap(),
    val title: String = "",
    val chapterName: String = ""
)
