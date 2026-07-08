# 鸿蒙 .so 编译技术阻塞报告

**日期**: 2026-07-08
**状态**: ⛔ 阻塞 — 需要 macOS ARM64 物理/云主机

---

## 阻塞原因

| 尝试 | 结果 | 根因 |
|------|------|------|
| GitHub Actions `macos-26` + Kotlin-OHOS | ❌ 工具链下载失败 | URL 返回 HTML (404/重定向) |
| GitHub Actions + KuiklyBase LLVM | ❌ 同上 | 同上 |
| 标准 Kotlin/Native 2.3.21 | ❌ target 不存在 | `harmonyOSArm64` 非内建 target |

**核心事实**: `harmonyOSArm64` target 需要自定义 Kotlin/Native 编译器，此编译器**仅支持 macOS ARM64** 且需要：
- 修改过 LLVM 后端的 Kotlin/Native 编译器
- HarmonyOS SDK sysroot
- 平台特定的 toolchain 链接器

## 可行路径

| 方案 | 成本 | 可行性 | 说明 |
|------|------|--------|------|
| **MacStadium 云 Mac** | ~$75/月 | ✅ | Apple M4 Mac Mini，按需编译 |
| **AWS EC2 Mac** | ~$1.2/小时 | ✅ | 用完即销毁，成本最低 |
| **GitHub Actions self-hosted** | Mac 硬件 | ✅ | 需自备 Mac 做 runner |
| **购买 Mac Mini** | ~$500 | ✅ | 一次性投入 |

## 当前产出

尽管 .so 编译受阻，已完成的核心资产:

```
✅ KMP shared 模块 (21 Kotlin 文件)
   ├─ SQLDelight 21 表 + CRUD 查询
   ├─ HttpHandler Ktor 抽象 (Android/JVM/Native)
   ├─ AnalyzeUrl URL 模板解析器
   ├─ JsEngine expect/actual 抽象
   ├─ Book/BookSource/ReplaceRule 模型
   └─ MD5/StringUtils/NetworkUtils/CookieManager

✅ Android Compose (8 文件)
   ├─ BookshelfPage / SourcePage / SettingsPage
   ├─ LegadoNavigation (NavHost + BottomBar)
   └─ ComposeActivity + Theme

✅ HarmonyOS ArkUI (3 文件)
   ├─ Index.ets (书籍列表 + Tab 导航)
   └─ Reader.ets (阅读页)

✅ GitHub Actions CI (JVM/Android 编译 + 53 测试)

✅ 详细开发计划 (Phase 3-7)
```

## 推荐行动

1. **接受 .so 编译需要 Mac 的现实** → 租赁云 Mac 或购买 Mac Mini
2. **继续推进 KMP shared 业务逻辑** → 不依赖 .so 编译的工作:
   - AnalyzeRule 完整版迁移
   - WebBook 搜索/内容获取
   - ReadBook 阅读引擎
   - BookParser (TXT/EPUB/MOB)
3. **.so 编译准备好后** → 一次性集成到 HAP
