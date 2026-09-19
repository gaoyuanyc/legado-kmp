package io.legado.bridge

import kotlinx.coroutines.runBlocking

/**
 * Minimal observable bridge entry point for the HarmonyOS .so.
 *
 * v1 purpose: prove the toolchain + KNOI linkage works end-to-end
 * before porting the full shared logic. Each exported function is
 * a simple, pure-Kotlin implementation that ArkTS can call via KNOI.
 */
object LegadoBridge {

    /**
     * Ping the native lib from ArkTS: returns the library version.
     */
    fun version(): String = "0.0.1"

    /**
     * Evaluate a pure-Kotlin MD5 hex digest.
     */
    fun md5Hex(input: String): String {
        val bytes = input.encodeToByteArray()
        return runBlocking { Md5Util.md5Hex(bytes) }
    }

    /**
     * Parse a Legado book source rule string and report the split modes.
     */
    fun ruleSummary(ruleStr: String): String {
        return RuleSplitter.summarize(ruleStr)
    }
}