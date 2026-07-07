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
    val ruleSearchUrl: String? = null,
    val bookUrlPattern: String? = null,
    val customOrder: Int = 0,
    val variable: String? = null
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
