# Legado KMP → HarmonyOS 迁移项目

将 Legado (阅读) Android 应用通过 KMP 技术栈迁移到 HarmonyOS。

## 项目结构

| 模块 | 说明 |
|------|------|
| `shared/` | KMP 共享模块 — 业务逻辑（数据库、网络、书源解析、JS引擎） |
| `harmonyApp/` | 鸿蒙 HAP 工程 — ArkUI 端 + KNOI 桥接层 |
| `.github/workflows/` | CI — 在 macOS ARM64 上编译 libshared.so |

## 快速开始

### 本地开发 (Windows)

```bash
# 编译 Android 目标验证
./gradlew :shared:compileDebugKotlinAndroid

# 运行 JVM 单元测试
./gradlew :shared:jvmTest

# 构建鸿蒙 HAP (已有独立工程)
cd harmonyApp && devecocli build
```

### 编译 .so (macOS ARM64 only)

通过 GitHub Actions 自动编译：

1. 推送到 master 分支
2. Actions 自动调度 `macos-26` runner
3. 编译产物自动上传为 Artifacts
4. 下载 `libshared.so` 集成到 HAP

手动触发: GitHub → Actions → Build libshared.so → Run workflow

## 测试

- **53 个 JVM 单元测试** 覆盖: MD5、字符串、JS引擎、SQLDelight CRUD、内容替换、常量
- **编译验证**: Android / JVM / HAP 三目标

## 技术栈

- Kotlin Multiplatform (Kotlin 2.3.21)
- SQLDelight 2.0.2 (数据库)
- Ktor 3.1.0 (网络)
- KNOI (HarmonyOS 桥接)
- ArkTS (鸿蒙 UI)

## License

MIT
