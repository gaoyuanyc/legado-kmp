package io.legado.bridge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BridgeTest {

    @Test
    fun version_returnsNonEmpty() {
        assertTrue(LegadoBridge.version().isNotEmpty())
    }

    @Test
    fun md5Hex_matchesKnownVector() {
        // MD5("") = d41d8cd98f00b204e9800998ecf8427e
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", LegadoBridge.md5Hex(""))
        // MD5("abc") = 900150983cd24fb0d6963f7d28e17f72
        assertEquals("900150983cd24fb0d6963f7d28e17f72", LegadoBridge.md5Hex("abc"))
    }

    @Test
    fun ruleSummary_classifiesCss() {
        assertEquals("CSS: .title@text", RuleSplitter.summarize(".title@text"))
    }

    @Test
    fun ruleSummary_classifiesJs() {
        assertTrue(RuleSplitter.summarize("<js>var x=1;</js>").startsWith("JS"))
    }
}