package io.legado.shared.analyzeRule

import org.mozilla.javascript.Context as RhinoContext
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ImporterTopLevel

/**
 * Android JS engine implementation using Mozilla Rhino.
 * 
 * This preserves 100% compatibility with existing book source rules.
 */
actual class PlatformJsEngine actual constructor() : JsEngine {
    
    private val rhinoContext: RhinoContext = RhinoContext.enter().apply {
        optimizationLevel = -1 // Interpreter mode for Android compatibility
        languageVersion = RhinoContext.VERSION_ES6
    }
    
    private val scope: Scriptable = ImporterTopLevel(rhinoContext)

    override fun evaluate(script: String): JsValue {
        val result = rhinoContext.evaluateString(scope, script, "rule", 1, null)
        return mapRhinoToJsValue(result)
    }

    override suspend fun evaluateAsync(script: String): JsValue {
        // Rhino is synchronous on Android; wrap in coroutine-friendly manner
        return evaluate(script)
    }

    override fun bindObject(name: String, obj: Any?) {
        val jsObject = RhinoContext.javaToJS(obj, scope)
        scope.put(name, scope, jsObject)
    }

    override fun callFunction(name: String, vararg args: Any?): JsValue {
        val fn = scope.get(name, scope) as? org.mozilla.javascript.Function
        if (fn != null) {
            val jsArgs = args.map { RhinoContext.javaToJS(it, scope) }.toTypedArray()
            val result = fn.call(rhinoContext, scope, scope, jsArgs)
            return mapRhinoToJsValue(result)
        }
        return JsValue.Null
    }

    override fun close() {
        RhinoContext.exit()
    }

    private fun mapRhinoToJsValue(obj: Any?): JsValue {
        return when (obj) {
            null -> JsValue.Null
            is String -> JsValue.String(obj)
            is Number -> JsValue.Number(obj.toDouble())
            is Boolean -> JsValue.Boolean(obj)
            is Scriptable -> mapScriptableToJsValue(obj)
            else -> JsValue.String(obj.toString())
        }
    }

    private fun mapScriptableToJsValue(scriptable: Scriptable): JsValue {
        val keys = scriptable.getIds()
        val isArray = keys.all { it is Int } && keys.isNotEmpty()
        
        return if (isArray) {
            JsValue.Array(keys.filterIsInstance<Int>().map { idx -> mapRhinoToJsValue(scriptable.get(idx, scriptable)) })
        } else {
            val map = mutableMapOf<String, JsValue?>()
            for (key in keys) {
                val keyStr = key.toString()
                val value: Any? = when (key) {
                    is Int -> scriptable.get(key, scriptable)
                    is String -> scriptable.get(key, scriptable)
                    else -> null
                }
                if (value != null) {
                    map[keyStr] = mapRhinoToJsValue(value)
                }
            }
            JsValue.Object(map)
        }
    }

    private fun Scriptable.undefinedValue(): Any? {
        return try {
            this.javaClass.getDeclaredField("NOT_FOUND").let { field ->
                field.isAccessible = true
                field.get(null)
            }
        } catch (e: Exception) {
            null
        }
    }
}
