package io.legado.shared.analyzeRule

/**
 * Rhino compatibility shim for QuickJS engine.
 * 
 * This provides JavaScript code that simulates Rhino-specific APIs
 * (importClass, importPackage, Java.type, etc.) when running
 * under QuickJS on HarmonyOS.
 */
object RhinoCompatShim {
    
    /**
     * Shim code that must be evaluated before any book source rule.
     * Provides compatibility for the most commonly used Rhino APIs.
     */
    const val SHIM_CODE: String = """
    (function() {
        // Rhino's importClass - maps to pre-bound Java type bridges
        globalThis.importClass = function(className) {
            var type = __kotlinGetClass(className);
            if (type) {
                var simpleName = className.substring(className.lastIndexOf('.') + 1);
                globalThis[simpleName] = type;
            }
        };
        
        // Rhino's importPackage - bulk import
        globalThis.importPackage = function(packageName) {
            var package = __kotlinGetPackage(packageName);
            if (package) {
                for (var name in package) {
                    if (package.hasOwnProperty(name) && typeof package[name] === 'function') {
                        globalThis[name] = package[name];
                    }
                }
            }
        };
        
        // Java.type replacement
        globalThis.Java = {
            type: function(className) {
                return __kotlinGetClass(className);
            },
            adapter: function(javaObj, jsObj) {
                return __kotlinCreateAdapter(javaObj, jsObj);
            }
        };
        
        // Ensure console exists
        if (typeof console === 'undefined') {
            console = {
                log: function(msg) { __kotlinLog(String(msg)); },
                error: function(msg) { __kotlinError(String(msg)); },
                warn: function(msg) { __kotlinWarn(String(msg)); }
            };
        }
        
        // JSON helpers
        if (typeof JSON === 'undefined') {
            JSON = {
                parse: function(str) { return __kotlinJsonParse(str); },
                stringify: function(obj) { return __kotlinJsonStringify(obj); }
            };
        }
    })();
    """
}
