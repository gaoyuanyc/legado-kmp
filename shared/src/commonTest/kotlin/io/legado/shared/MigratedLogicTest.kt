package io.legado.shared.utils

import io.legado.shared.constant.AppPattern
import io.legado.shared.constant.BookType
import io.legado.shared.model.ReplaceRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MD5UtilsTest {

    @Test
    fun md5Encode_producesCorrectHash() {
        val hash = MD5Utils.md5Encode("hello")
        assertEquals("5d41402abc4b2a76b9719d911017c592", hash)
    }

    @Test
    fun md5Encode_emptyString() {
        val hash = MD5Utils.md5Encode("")
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", hash)
    }

    @Test
    fun md5Encode_null() {
        val hash = MD5Utils.md5Encode(null)
        assertEquals("", hash)
    }

    @Test
    fun md5Encode_produces32Chars() {
        val hash = MD5Utils.md5Encode("test")
        assertEquals(32, hash.length)
    }

    @Test
    fun md5Encode16_returns16Chars() {
        val hash = MD5Utils.md5Encode16("hello")
        assertEquals(16, hash.length)
    }

    @Test
    fun md5Encode_deterministic() {
        val hash1 = MD5Utils.md5Encode("deterministic")
        val hash2 = MD5Utils.md5Encode("deterministic")
        assertEquals(hash1, hash2)
    }

    @Test
    fun md5Encode_differentInputs() {
        val hash1 = MD5Utils.md5Encode("input1")
        val hash2 = MD5Utils.md5Encode("input2")
        assertTrue(hash1 != hash2)
    }
}

class StringUtilsTest {

    @Test
    fun trim_basic() {
        assertEquals("hello", StringUtils.trim("  hello  "))
    }

    @Test
    fun trim_null() {
        assertEquals("", StringUtils.trim(null))
    }

    @Test
    fun formatFileSize_bytes() {
        assertEquals("100.0B", StringUtils.formatFileSize(100))
    }

    @Test
    fun formatFileSize_kilobytes() {
        assertEquals("1.0KB", StringUtils.formatFileSize(1024))
    }

    @Test
    fun formatFileSize_megabytes() {
        assertEquals("1.0MB", StringUtils.formatFileSize(1024 * 1024))
    }

    @Test
    fun stripHtmlTags_removesTags() {
        assertEquals("Hello World", StringUtils.stripHtmlTags("<b>Hello</b> <i>World</i>"))
    }

    @Test
    fun escapeHtml_basic() {
        assertEquals("&lt;script&gt;alert(&quot;test&quot;)&lt;/script&gt;",
            StringUtils.escapeHtml("<script>alert(\"test\")</script>"))
    }

    @Test
    fun getDomain_validUrl() {
        assertEquals("example.com", StringUtils.getDomain("https://example.com/path"))
    }

    @Test
    fun getDomain_nullForInvalid() {
        assertEquals(null, StringUtils.getDomain("not-a-url"))
    }

    @Test
    fun splitNotBlank_basic() {
        val result = StringUtils.splitNotBlank("a,b,c", ",".toRegex())
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun splitNotBlank_filtersEmpty() {
        val result = StringUtils.splitNotBlank("a,,c", ",".toRegex())
        assertEquals(listOf("a", "c"), result)
    }

    @Test
    fun base64_roundTrip() {
        val original = "Hello, World!"
        val encoded = StringUtils.encodeBase64(original.toByteArray())
        val decoded = String(StringUtils.decodeBase64(encoded))
        assertEquals(original, decoded)
    }
}

class JsEncodeUtilsTest {

    @Test
    fun containsJs_withJsTag() {
        assertTrue(JsEncodeUtils.containsJs("<js>var x = 1;</js>"))
    }

    @Test
    fun containsJs_withJsPrefix() {
        assertTrue(JsEncodeUtils.containsJs("@js:var x = 1;"))
    }

    @Test
    fun containsJs_noJs() {
        assertFalse(JsEncodeUtils.containsJs("just plain text"))
    }

    @Test
    fun extractJs_extractsJsTag() {
        val result = JsEncodeUtils.extractJs("before <js>alert(1)</js> after")
        assertTrue(result.contains("alert(1)"))
    }

    @Test
    fun quoteJs_escapesQuotes() {
        assertEquals("'it\\'s a test'", JsEncodeUtils.quoteJs("it's a test"))
    }

    @Test
    fun wrapJs_wrapsCorrectly() {
        assertEquals("<js>alert(1)</js>", JsEncodeUtils.wrapJs("alert(1)"))
    }
}

class ReplaceAnalyzerTest {

    @Test
    fun applyRule_simpleReplacement() {
        val rule = ReplaceRule(pattern = "old", replacement = "new", isRegex = false, isEnabled = true)
        assertEquals("new text", ReplaceAnalyzer.applyRule("old text", rule))
    }

    @Test
    fun applyRule_regexReplacement() {
        val rule = ReplaceRule(pattern = "[0-9]+", replacement = "N", isRegex = true, isEnabled = true)
        assertEquals("abcNdefNf", ReplaceAnalyzer.applyRule("abc12def34f", rule))
    }

    @Test
    fun applyRule_disabled() {
        val rule = ReplaceRule(pattern = "a", replacement = "B", isRegex = false, isEnabled = false)
        assertEquals("abc", ReplaceAnalyzer.applyRule("abc", rule))
    }

    @Test
    fun applyReplacements_multipleRules() {
        val rules = listOf(
            ReplaceRule(pattern = "a", replacement = "A", isRegex = false, isEnabled = true),
            ReplaceRule(pattern = "b", replacement = "B", isRegex = false, isEnabled = true)
        )
        assertEquals("ABc", ReplaceAnalyzer.applyReplacements("abc", rules))
    }

    @Test
    fun isValidRule_valid() {
        val rule = ReplaceRule(pattern = "test", isEnabled = true)
        assertTrue(ReplaceAnalyzer.isValidRule(rule))
    }

    @Test
    fun isValidRule_emptyPattern() {
        val rule = ReplaceRule(pattern = "", isEnabled = true)
        assertFalse(ReplaceAnalyzer.isValidRule(rule))
    }

    @Test
    fun isValidRule_invalidRegex() {
        val rule = ReplaceRule(pattern = "[invalid", isRegex = true, isEnabled = true)
        assertFalse(ReplaceAnalyzer.isValidRule(rule))
    }
}

class ConstantsTest {

    @Test
    fun bookType_flags() {
        assertEquals(4, BookType.video)
        assertEquals(8, BookType.text)
        assertEquals(32, BookType.audio)
        assertEquals(256, BookType.local)
        assertEquals(1024, BookType.notShelf)
    }

    @Test
    fun bookType_tags() {
        assertEquals("loc_book", BookType.localTag)
        assertEquals("webDav::", BookType.webDavTag)
    }

    @Test
    fun appPattern_jsPattern() {
        assertTrue(AppPattern.JS_PATTERN.containsMatchIn("<js>var x=1;</js>"))
    }

    @Test
    fun appPattern_domain() {
        val match = AppPattern.domainRegex.find("https://www.example.com/path")
        assertEquals("www.example.com", match?.groupValues?.getOrNull(1))
    }

    @Test
    fun appPattern_chnMap() {
        assertEquals(1, AppPattern.chnMap['一'])
        assertEquals(10, AppPattern.chnMap['十'])
    }
}
