package io.legado.shared.model

data class Book(
    val bookUrl: String = "",
    val tocUrl: String = "",
    val origin: String = "loc_book",
    val originName: String = "",
    val name: String = "",
    val author: String = "",
    val kind: String? = null,
    val customTag: String? = null,
    val coverUrl: String? = null,
    val customCoverUrl: String? = null,
    val intro: String? = null,
    val customIntro: String? = null,
    val charset: String? = null,
    val type: Int = 0,
    val group: Long = 0,
    val latestChapterTitle: String? = null,
    val latestChapterTime: Long = 0,
    val lastCheckTime: Long = 0,
    val lastCheckCount: Int = 0,
    val totalChapterNum: Int = 0,
    val durChapterTitle: String? = null,
    val durChapterIndex: Int = 0,
    val durVolumeIndex: Int = 0,
    val chapterInVolumeIndex: Int = 0,
    val durChapterPos: Int = 0,
    val durChapterTime: Long = 0,
    val wordCount: String? = null,
    val canUpdate: Boolean = true,
    val order: Int = 0,
    val originOrder: Int = 0,
    val variable: String? = null,
    val readConfig: String? = null,
    val syncTime: Long = 0
)

data class BookSource(
    val bookSourceUrl: String = "",
    val bookSourceName: String = "",
    val bookSourceGroup: String? = null,
    val bookSourceType: Int = 0,
    val loginUrl: String? = null,
    val loginUi: String? = null,
    val loginCheckJs: String? = null,
    val comment: String? = null,
    val createTime: Long = 0,
    val header: String? = null,
    val searchUrl: String? = null,
    val lastUpdateTime: Long = 0,
    val exploreUrl: String? = null,
    val enabled: Boolean = true,
    val enabledExplore: Boolean = true,
    val weight: Int = 0,
    val isDisplay: Boolean = true,
    val useWebview: Boolean = true,
    val ruleFindUrl: String? = null,
    val bookUrlPattern: String? = null,
    val customOrder: Int = 0,
    val variable: String? = null,
    // ── Book source rule fields (for WebBook engine) ──
    val ruleSearch: String? = null,          // 搜索列表规则
    val ruleSearchName: String? = null,      // 书名规则
    val ruleSearchAuthor: String? = null,    // 作者规则
    val ruleSearchKind: String? = null,      // 分类规则
    val ruleSearchIntro: String? = null,     // 简介规则
    val ruleSearchCoverUrl: String? = null,  // 封面规则
    val ruleSearchLastChapter: String? = null,// 最后章节规则
    val ruleSearchUrl: String? = null,       // 书籍URL规则
    val ruleBookName: String? = null,        // 书籍名规则
    val ruleBookAuthor: String? = null,      // 作者规则
    val ruleCoverUrl: String? = null,        // 封面规则
    val ruleIntro: String? = null,           // 简介规则
    val ruleKind: String? = null,            // 分类规则
    val ruleLastChapter: String? = null,     // 最后更新规则
    val ruleTocUrl: String? = null,          // 目录URL规则
    val ruleToc: String? = null,             // 目录列表规则
    val ruleChapterName: String? = null,     // 章节名规则
    val ruleChapterUrl: String? = null,      // 章节URL规则
    val ruleContentUrl: String? = null,      // 正文URL规则
    val ruleContent: String? = null,         // 正文规则
    val ruleNextTocUrl: String? = null,      // 下一目录URL规则
    val ruleBookContent: String? = null,      // 正文净化规则
    val rulePreUpdateJs: String? = null      // 预处理JS
) {
    /**
     * Get the content rule configuration.
     */
    fun getContentRule(): ContentRule = ContentRule(
        content = ruleContent ?: "",
        sourceRegex = ruleContentUrl,
        webJs = null
    )

    /**
     * Get the book type from source.
     */
    fun getBookType(): String = ""
}

/**
 * Content rule configuration.
 */
data class ContentRule(
    val content: String? = null,
    val sourceRegex: String? = null,
    val webJs: String? = null
)

/**
 * ReplaceRule entity for content replacement.
 * Migrated from Room entity (without Android dependencies).
 */
data class ReplaceRule(
    val id: Long = 0L,
    val name: String = "",
    val group: String? = null,
    val pattern: String = "",
    val replacement: String = "",
    val scope: String? = null,
    val scopeTitle: Boolean = false,
    val scopeContent: Boolean = true,
    val excludeScope: String? = null,
    val isEnabled: Boolean = true,
    val isRegex: Boolean = true,
    val timeoutMillisecond: Long = 3000L,
    val order: Int = 0
)
