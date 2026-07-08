# Legado KMP → HarmonyOS 下一阶段开发计划

**日期**: 2026-07-08
**当前版本**: Phase 0-2 + A/B/C 已完成
**代码统计**: 21 Kotlin 文件 (shared) + 8 Compose 文件 + 3 ArkTS 文件

---

## 当前状态

### 目标架构（回顾）

```
┌─────────────────────┐          ┌──────────────────────────┐
│   Android App        │          │   HarmonyOS HAP           │
│   (Kotlin + Compose) │          │   (ArkTS + ArkUI)         │
│                      │          │                          │
│  ┌────────────────┐  │          │  ┌────────────────────┐  │
│  │ Compose UI     │  │          │  │ ArkUI Pages        │  │
│  │ (骨架 + 3页面)  │  │          │  │ (骨架 + 2页面)     │  │
│  └───────┬────────┘  │          │  └─────────┬──────────┘  │
│          │ ViewModel   │          │            │ N-API      │
│  ┌───────▼────────┐  │          │  ┌─────────▼──────────┐  │
│  │ Platform       │  │          │  │ 桥接层 (KNOI/C++)  │  │
│  │ Adapters       │  │          │  └─────────┬──────────┘  │
│  └───────┬────────┘  │          │            │             │
└──────────┼───────────┘          └────────────┼─────────────┘
           │                                   │
           ▼                                   ▼
┌──────────────────────────────────────────────────────────────┐
│                    shared (Kotlin Multiplatform)              │
│                                                              │
│  已完成:                                                      │
│  ├─ data/ (DatabaseDriverFactory, SQLDelight 21表)           │
│  ├─ analyzeRule/ (JsEngine expect/actual, AnalyzeUrl, RuleExecutor)│
│  ├─ model/ (Book, BookSource, ReplaceRule)                   │
│  ├─ network/ (HttpHandler Ktor抽象)                           │
│  ├─ utils/ (MD5, StringUtils, NetworkUtils, JsEncodeUtils, CookieManager)│
│  └─ constant/ (BookType, AppPattern)                         │
│                                                              │
│  待完成:                                                      │
│  ├─ analyzeRule/ (AnalyzeRule完整版, WebBook, RuleAnalyzer)   │
│  ├─ data/ (DAO查询补充, DatabaseMigration)                    │
│  ├─ readbook/ (阅读引擎: 分页/进度/TTS)                       │
│  ├─ web/ (本地HTTP服务器 Ktor)                                │
│  ├─ book/ (TXT/EPUB/MOBI 解析器)                             │
│  ├─ search/ (搜索聚合引擎)                                    │
│  └─ rss/ (RSS解析器)                                         │
└──────────────────────────────────────────────────────────────┘
```

---

## Phase 3: KNOI 桥接层 & 互操作 (3-4 周)

**目标**: 打通鸿蒙 ↔ KMP 的双向通信

### Task 3.1: KNOI 服务接口定义

**Files:**
- Create: `shared/src/commonMain/.../knoi/BookSourceService.kt`
- Create: `shared/src/commonMain/.../knoi/BookService.kt`
- Create: `shared/src/commonMain/.../knoi/ConfigService.kt`

**设计:**
```kotlin
// 在 shared 模块中定义 KNOI 服务接口
@ServiceProvider("BookSourceService")
class BookSourceServiceImpl : BookSourceService {
    @ExternalFun
    override suspend fun search(url: String, query: String, page: Int): List<Book> { }
    
    @ExternalFun
    override suspend fun getBookInfo(bookUrl: String): BookInfo { }
    
    @ExternalFun
    override suspend fun getChapterList(bookUrl: String): List<Chapter> { }
    
    @ExternalFun
    override suspend fun getChapterContent(chapterUrl: String): String { }
}

@ServiceProvider("BookService")  
class BookServiceImpl : BookService {
    @ExternalFun
    override suspend fun getAllBooks(): List<Book> { }
    
    @ExternalFun
    override suspend fun insertBook(book: Book) { }
    
    @ExternalFun
    override suspend fun deleteBook(bookUrl: String) { }
}
```

**验证标准:**
- KNOI KSP 插件成功生成 TypeScript `.d.ts` 文件
- ArkTS 侧能识别生成的服务接口

### Task 3.2: 鸿蒙 N-API 桥接层

**Files:**
- Create: `harmonyApp/entry/src/main/cpp/napi_bridge.cpp` (更新)
- Create: `harmonyApp/entry/src/main/cpp/CMakeLists.txt` (更新)

**设计:**
```cpp
// napi_bridge.cpp - KNOI 桥接入口
#include <napi/native_api.h>
#include <hilog/log.h>

// KNOI 初始化 - 加载 libshared.so 并注册服务
static napi_value InitKnoi(napi_env env, napi_value exports) {
    // 1. KNOI 自动注册所有 @ServiceProvider 标记的服务
    // 2. 导出ArkTS可调用的模块
    return exports;
}

static napi_module knoiModule = {
    .nm_version = 1,
    .nm_filename = nullptr,
    .nm_register_func = InitKnoi,
    .nm_modname = "entry",
    .nm_flags = 0,
    .reserved = { 0 },
};

extern "C" __attribute__((constructor)) void RegisterKnoiModule() {
    napi_module_register(&knoiModule);
}
```

**验证标准:**
- N-API 模块加载成功
- ArkTS 能调用 Kotlin 服务方法

### Task 3.3: ArkTS 侧服务注册与初始化

**Files:**
- Create: `harmonyApp/entry/src/main/ets/knoi/KnoiInit.ets`
- Create: `harmonyApp/entry/src/main/ets/bridge/BookSourceBridge.ets`
- Create: `harmonyApp/entry/src/main/ets/bridge/BookBridge.ets`

**KnoiInit.ets:**
```typescript
import { setup, init } from "@kuiklybase/knoi"

// 初始化: 加载 libshared.so (编译后由KMP产出)
setup("libshared.so", BuildProfile.DEBUG)
init()

export { setup, init }
```

**BookSourceBridge.ets:**
```typescript
import { getService } from "@kuiklybase/knoi"

export class BookSourceBridge {
  static async search(url: string, query: string, page: number): Promise<Book[]> {
    const service = getService<BookSourceService>("BookSourceService")
    return service.search(url, query, page)
  }
  
  static async getBookInfo(bookUrl: string): Promise<BookInfo> {
    const service = getService<BookSourceService>("BookSourceService")
    return service.getBookInfo(bookUrl)
  }
  
  static async getChapterList(bookUrl: string): Promise<Chapter[]> {
    const service = getService<BookSourceService>("BookSourceService")
    return service.getChapterList(bookUrl)
  }
  
  static async getContent(chapterUrl: string): Promise<string> {
    const service = getService<BookSourceService>("BookSourceService")
    return service.getChapterContent(chapterUrl)
  }
}
```

**验证标准:**
- ArkTS → Kotlin 单向调用成功
- 数据序列化/反序列化正确

---

## Phase 4: 核心共享业务逻辑完善 (6-8 周)

**目标**: 迁移剩余 80% 业务逻辑到 shared 模块

### Task 4.1: 完整 AnalyzeRule 引擎

**Original:** `legado-master/app/.../AnalyzeRule.kt` (978 行)
**Target:** `shared/src/commonMain/.../analyzeRule/AnalyzeRule.kt`

**核心挑战:** AnalyzeRule 重度使用 Rhino 特有 API (`Java.type()`, `importClass()`)

**迁移策略:**
```kotlin
// commonMain — 平台无关核心
class AnalyzeRule(
    private val html: String,
    private val baseUrl: String = "",
    private var rule: String = "",
    private val source: BookSource? = null
) {
    private val jsEngine: PlatformJsEngine = PlatformJsEngine()
    
    // 规则解析器 (纯 Kotlin, 可跨平台)
    private val ruleAnalyzer = RuleAnalyzer()
    
    fun getString(rule: String): String { ... }
    fun getStringList(rule: String): List<String> { ... }
    fun getElement(rule: String): AnalyzeElements { ... }
    fun getElements(rule: String): List<AnalyzeElements> { ... }
    
    // JS 执行委托给平台引擎
    private fun evalJS(jsCode: String, result: Any? = null): Any? {
        // 注入书源规则专用 JS 垫片
        jsEngine.injectBookSourceShim(source)
        return jsEngine.evaluate(jsCode)
    }
}

// androidMain — Rhino 实现保留完整 Java 互操作
actual class PlatformJsEngine : JsEngine {
    private val rhinoContext = RhinoContext.enter().apply {
        optimizationLevel = -1
        languageVersion = VERSION_ES6
    }
    
    override fun evaluate(script: String): JsValue {
        // Rhino 特有的 Java.type() 和 importClass() 在 Android 端完整支持
        return mapRhinoResult(rhinoContext.evaluateString(scope, script, "rule", 1, null))
    }
}

// jsMain / nativeMain — QuickJS + 自定义垫片
actual class PlatformJsEngine : JsEngine {
    private val quickJS = QuickJS.create()
    private val context = quickJS.createContext()
    
    override fun evaluate(script: String): JsValue {
        // QuickJS 端使用 RhinoCompatShim 模拟 Java.type() 等 API
        injectRhinoCompatShim()
        return mapQuickJSResult(context.evaluate(script))
    }
}
```

**验证标准:**
- 90% 书源规则无需修改即可在 QuickJS 端运行
- Android 端 100% 兼容 (Rhino 完整互操作)

### Task 4.2: WebBook 搜索与内容获取引擎

**Original:** `WebBook.kt` + `AnalyzeUrl.kt` (搜索 → 详情 → 目录 → 内容)
**Target:** `shared/src/commonMain/.../webbook/WebBook.kt`

**核心接口:**
```kotlin
class WebBook(
    private val httpHandler: HttpHandler,
    private val analyzeRule: AnalyzeRule,
    private val source: BookSource
) {
    // 搜索书籍
    suspend fun searchBook(key: String, page: Int = 1): List<SearchResult> { ... }
    
    // 获取书籍详情
    suspend fun getBookInfo(bookUrl: String): BookInfo { ... }
    
    // 获取章节列表
    suspend fun getChapterList(bookUrl: String): List<BookChapter> { ... }
    
    // 获取章节内容
    suspend fun getChapterContent(chapterUrl: String): String { ... }
}
```

**依赖:**
- HttpHandler (Phase 1 已完成)
- AnalyzeRule (Task 4.1)
- 规则解析器 (已有 RuleExecutor)

### Task 4.3: 阅读引擎 (ReadBook)

**Original:** `ReadBook.kt` (1064 行)
**Target:** `shared/src/commonMain/.../readbook/ReadBook.kt`

**核心功能:**
```kotlin
class ReadBook {
    // 章节管理
    fun getChapter(index: Int): BookChapter
    fun getChapterCount(): Int
    
    // 阅读进度
    fun getProgress(): ReadingProgress
    fun saveProgress(chapterIndex: Int, posInChapter: Int)
    
    // 分页引擎
    fun getPageCount(chapterIndex: Int): Int
    fun getPageContent(chapterIndex: Int, page: Int): String
    
    // 搜索高亮
    fun searchInChapter(chapterIndex: Int, keyword: String): List<SearchMatch>
}
```

**平台差异:**
- Android: 使用 `android.text.StaticLayout` 进行文字排版
- HarmonyOS: 使用 Native Canvas API

### Task 4.4: 书籍格式解析器

**Original:** `TextFile.kt`, `EpubFile.kt`, `MobiFile.kt`, `PdfFile.kt`, `LocalBook.kt`
**Target:** `shared/src/commonMain/.../book/`

**解析器架构:**
```kotlin
interface BookParser {
    fun parse(file: OkioSource): ParsedBook
    fun getChapter(chapterIndex: Int): ChapterContent
    fun getCoverImage(): ByteArray?
}

// TXT 解析 (纯 Kotlin, 完全跨平台)
class TxtParser(private val encoding: String = "UTF-8") : BookParser { ... }

// EPUB 解析 (使用 ksoup 解析 HTML)
class EpubParser : BookParser { ... }

// 平台特定解析器 (通过 expect/actual)
expect class PlatformPdfParser() : BookParser
expect class PlatformMobiParser() : BookParser
```

**依赖:**
- Okio (多平台 I/O)
- ksoup (KMP HTML 解析库, 替代 jsoup)

### Task 4.5: 内容替换规则引擎

**Original:** `ReplaceAnalyzer.kt` + `ReplaceRuleDao.kt`
**Target:** 已在 Phase 1 迁移基础版，需补充完整的规则链和正则超时检测

**增强功能:**
```kotlin
class ReplaceAnalyzer {
    fun applyReplacements(
        text: String,
        rules: List<ReplaceRule>,
        timeoutMs: Long = 3000,
        onTimeout: (ReplaceRule) -> Unit = {}
    ): String
    
    // 支持作用域过滤
    fun applyForContent(text: String, source: BookSource?): String
    fun applyForTitle(text: String, source: BookSource?): String
    
    // 排除范围
    fun applyWithExclusions(text: String, rules: List<ReplaceRule>): String
}
```

### Task 4.6: RSS 解析器

**Original:** `Rss.kt`, `RssParserByRule.kt`, `RssParserDefault.kt`
**Target:** `shared/src/commonMain/.../rss/RssParser.kt`

```kotlin
class RssParser(private val httpHandler: HttpHandler) {
    suspend fun parseDefault(feedUrl: String): RssFeed { ... }
    suspend fun parseByRule(feedUrl: String, source: BookSource): RssFeed { ... }
}
```

### Task 4.7: 本地 HTTP 服务器 (Ktor Server)

**Original:** `NanoHTTPD` 实现
**Target:** `shared/src/commonMain/.../web/WebServer.kt`

```kotlin
class WebServer(private val port: Int = 1234) {
    private val server = embeddedServer(Netty, port) {
        routing {
            get("/") { ... }
            post("/api/importSource") { ... }
            webSocket("/ws") { ... }
        }
    }
    
    fun start() { server.start() }
    fun stop() { server.stop() }
}
```

---

## Phase 5: Android UI 完善 (6-8 周)

**Goal**: 从现有 3 页面扩展到完整功能 (20+ Activity/Compose 页面)

### Task 5.1: 引入 Jetpack ViewModel + StateFlow

**Files:**
- Create: `androidApp/.../viewmodel/BookshelfViewModel.kt`
- Create: `androidApp/.../viewmodel/ReaderViewModel.kt`
- Create: `androidApp/.../viewmodel/SourceViewModel.kt`

### Task 5.2: 阅读界面迁移

**Original:** `ReadBookActivity.kt` (~2000 行 XML + Kotlin)
**Target:** Compose 版阅读界面

**核心组件:**
```kotlin
@Composable
fun ReaderScreen(
    bookUrl: String,
    viewModel: ReaderViewModel = viewModel()
) {
    // 阅读区域 (支持点击中心唤出菜单)
    // 底部进度条
    // 章节跳转
    // 字体/主题/翻页模式设置
}
```

### Task 5.3: 书源管理迁移

**Original:** `SourceManagementActivity.kt` + 书源编辑 + 调试
**Target:** Compose 版书源管理系统

### Task 5.4: 搜索/发现页面迁移

**Original:** `SearchActivity.kt` + `ExploreActivity.kt`
**Target:** 聚合搜索结果 + 发现页

### Task 5.5: 设置系统迁移

**Original:** 30+ PreferenceFragment
**Target**: Compose 设置页面 (分类: 阅读/书源/同步/关于)

---

## Phase 6: 鸿蒙 ArkUI UI 完善 (6-8 周)

**Goal**: 从 2 页面扩展到完整功能，对齐 Android 端

### Task 6.1: 完整 Tab 导航 + 状态管理

**更新架构:**
- 使用 `AppStorageV2` 全局状态管理
- `@ObservedV2` + `@Trace` 替代 V1 装饰器
- 实现 `StateStore` 模式的状态集中管理

### Task 6.2: 阅读页完善

**增强:**
- 系统级光感视效 (Liquid Glass)
- 手势翻页 (左右滑动/仿真翻页)
- 字体加载/自定义字体
- TTS 朗读 (Core Speech Kit)

### Task 6.3: 书源管理 + 调试页

- 书源导入 (JSON/URL/二维码)
- 书源规则编辑器 (高亮语法)
- 书源调试 (实时执行规则)

### Task 6.4: 设置页 + 同步

- WebDAV 同步设置
- 阅读偏好设置
- 主题切换 (亮色/暗色/跟随系统)

---

## Phase 7: 集成测试与优化 (3-4 周)

### Task 7.1: 双端功能对齐测试

**测试矩阵:**
| 功能 | Android | HarmonyOS | 对齐度目标 |
|------|---------|-----------|-----------|
| 书籍搜索 | ✅ | ✅ | 100% |
| 目录解析 | ✅ | ✅ | 100% |
| 内容获取 | ✅ | ✅ | 100% |
| 阅读翻页 | 🔄 | 🔄 | 95% |
| TTS朗读 | 🔄 | 🔄 | 90% |
| 书源管理 | 🔄 | 🔄 | 100% |
| WebDAV同步 | ⏳ | ⏳ | 90% |

### Task 7.2: 性能优化

- KNOI 调用开销评估 (目标: <1ms/调用)
- .so 包体积优化 (当前 ~12MB, 目标: <8MB)
- 大列表滚动性能 (List/LazyForEach)
- 内存泄漏检测

### Task 7.3: 书源规则兼容性测试

**测试 100+ 真实书源规则，确保:**
- Android 端 100% 兼容 (Rhino)
- 鸿蒙端 >=90% 兼容 (QuickJS + 垫片)

---

## 工时估算

| 阶段 | 工期 | 人员 | 关键交付 |
|------|------|------|---------|
| Phase 3: KNOI桥接 | 3-4 周 | 2 人 | libshared.so 加载+双向通信 |
| Phase 4: 核心逻辑 | 6-8 周 | 2-3 人 | AnalyzeRule/WebBook/ReadBook |
| Phase 5: Android UI | 6-8 周 | 2 人 | Compose 20+ 页面 |
| Phase 6: HarmonyOS UI | 6-8 周 | 2 人 | ArkUI 20+ 页面 |
| Phase 7: 集成测试 | 3-4 周 | 全员 | 发布就绪 |
| **总计** | **24-32 周** | **核心 4-5 人** | |

---

## 技术风险与缓解

| 风险 | 概率 | 影响 | 缓解 |
|------|------|------|------|
| QuickJS 书源规则兼容性 <85% | 中 | 高 | Rhino 兼容垫片 + 规则分析工具 |
| KNOI 停止维护 | 低 | 高 | 保持桥接层最小化; 必要时手写 N-API |
| Kotlin/Native 协程 freeze 问题 | 中 | 中 | 使用新版 Native 协程 (2.3+ 已改善) |
| 鸿蒙特有 API 无 KNOI 封装 | 中 | 中 | 自定义 C++ 桥接 |
| SQLDelight 复杂查询性能 | 低 | 中 | 索引优化 + 查询基准测试 |

---

## 立即行动项 (下一周)

1. **在 macOS 环境编译 `libshared.so`** (或配置 GitHub Actions macOS runner)
2. **定义 KNOI `@ServiceProvider` 接口** → 自动生成 `.d.ts`
3. **实现 AnalyzeRule 完整版** (最核心的迁移工作)
4. **补充 WebBook 搜索/内容获取** (依赖 AnalyzeRule + HttpHandler)
5. **Android 端接入 shared 模块** → 验证端到端调用链
