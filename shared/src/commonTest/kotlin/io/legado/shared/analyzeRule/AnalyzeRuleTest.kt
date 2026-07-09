package io.legado.shared.analyzeRule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import io.legado.shared.constant.AppPattern

class AnalyzeRuleTest {

    @Test
    fun splitSourceRule_css() {
        val ruleStr = ".title@text##regex"
        val rule = AnalyzeRule("<div>test</div>", "https://example.com")
        val rules = rule.splitSourceRule(ruleStr)
        assertTrue(rules.isNotEmpty())
        assertEquals(Mode.Default, rules[0].mode)
    }

    @Test
    fun splitSourceRule_js() {
        val ruleStr = "<js>var x = 1;</js>"
        val rule = AnalyzeRule("<div>test</div>", "https://example.com")
        val rules = rule.splitSourceRule(ruleStr)
        assertTrue(rules.isNotEmpty())
    }
}

class AnalyzeUrlTest {

    @Test
    fun initUrl_simple() {
        val url = AnalyzeUrl(
            mUrl = "https://example.com/search?q=test",
            key = "test",
            baseUrl = "https://example.com"
        )
        assertTrue(url.url.contains("test"))
    }
}

class JsonPathReaderTest {

    @Test
    fun readRoot() {
        val json = "{\"name\":\"Test\",\"value\":42}"
        val result = JsonPathReader.read(json, "$.name")
        assertEquals("Test", result)
    }
}

class AppPatternTest {

    @Test
    fun jsPattern_matchesJsTag() {
        assertTrue(AppPattern.JS_PATTERN.containsMatchIn("<js>var x=1;</js>"))
    }

    @Test
    fun splitGroupPattern() {
        val input = "a,b;c"
        val parts = input.split(AppPattern.splitGroupRegex)
        assertTrue(parts.size >= 2)
    }
}

class SourceRuleTest {

    @Test
    fun create() {
        val rule = SourceRule("test", Mode.Regex)
        assertEquals("test", rule.rule)
        assertEquals(Mode.Regex, rule.mode)
    }

    @Test
    fun modeCount() {
        assertTrue(Mode.values().size >= 4)
    }
}
