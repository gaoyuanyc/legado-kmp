package io.legado.shared.data

import io.legado.shared.model.Book
import io.legado.shared.model.BookSource

fun Books.toModel(): Book = Book(
    bookUrl = bookUrl, tocUrl = tocUrl, origin = origin,
    originName = originName, name = name, author = author,
    kind = kind, customTag = customTag, coverUrl = coverUrl,
    customCoverUrl = customCoverUrl, intro = intro, customIntro = customIntro,
    charset = charset, type = type.toInt(), group = group,
    latestChapterTitle = latestChapterTitle, latestChapterTime = latestChapterTime,
    lastCheckTime = lastCheckTime, lastCheckCount = lastCheckCount.toInt(),
    totalChapterNum = totalChapterNum.toInt(), durChapterTitle = durChapterTitle,
    durChapterIndex = durChapterIndex.toInt(), durVolumeIndex = durVolumeIndex.toInt(),
    chapterInVolumeIndex = chapterInVolumeIndex.toInt(), durChapterPos = durChapterPos.toInt(),
    durChapterTime = durChapterTime, wordCount = wordCount,
    canUpdate = canUpdate != 0L, order = order.toInt(),
    originOrder = originOrder.toInt(), variable = variable,
    readConfig = readConfig, syncTime = syncTime
)

fun Book_sources.toModel(): BookSource = BookSource(
    bookSourceUrl = bookSourceUrl, bookSourceName = bookSourceName,
    bookSourceGroup = bookSourceGroup, bookSourceType = bookSourceType.toInt(),
    loginUrl = loginUrl, loginUi = loginUi, loginCheckJs = loginCheckJs,
    comment = bookSourceComment, createTime = lastUpdateTime,
    header = header_, searchUrl = searchUrl, lastUpdateTime = lastUpdateTime,
    enabled = enabled != 0L, enabledExplore = enabledExplore != 0L,
    weight = weight.toInt(), isDisplay = (eventListener != 0L),
    useWebview = (customButton != 0L),
    ruleFindUrl = ruleExplore, ruleSearchUrl = searchUrl,
    bookUrlPattern = bookUrlPattern, customOrder = customOrder.toInt(),
    variable = variableComment
)
