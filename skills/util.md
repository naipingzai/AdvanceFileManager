# Skill: util — 工具函数与扩展函数

## 概述
`naipingzai.materialfile.util` 包含 80+ 个工具类/扩展函数文件，提供纯 Kotlin 工具函数和 Android 扩展函数。

## 纯 Kotlin 函数（无 Android 依赖，可 JVM 单元测试）

| 文件 | 函数 | 测试文件 |
|------|------|----------|
| AnyExtensions.kt | `Any.hash(vararg values)` | AnyExtensionsTest |
| ByteArrayExtensions.kt | `ByteArray.sha1Digest()`, `toHexString()` | ByteArrayExtensionsTest |
| CharSequenceExtensions.kt | `takeIfNotBlank()`, `takeIfNotEmpty()` | CharSequenceExtensionsTest |
| CollectionExtensions.kt | `enumSetOf()`, `toLinkedSet()`, `toEnumSet()`, `takeIfNotEmpty()`, `removeFirst()` | CollectionExtensionsTest |
| IntExtensions.kt | `Int.hasBits()`, `andInv` | IntExtensionsTest |
| LongExtensions.kt | `Long.hasBits()`, `andInv` | LongExtensionsTest |
| ListExtensions.kt | `startsWith()`, `endsWith()` | ListExtensionsTest |
| ThrowableExtensions.kt | `findCauseByClass<T>()` | ThrowableExtensionsTest |
| PathName.kt | `PathName`, `FileName`, `asPathName()`, `asFileName()`, `baseName`, `extensions` | PathNameTest |
| Base64.kt | `String.asBase64()`, `toByteArray()`, `ByteArray.toBase64()` | Base64Test |
| Color.kt | `Int.asColor()`, `alpha`, `red`, `green`, `blue`, `withAlpha()`, `withModulatedAlpha()`, `compositeOver()` | ColorTest |
| MapSet.kt | `MapSet<K,V>`, `LinkedMapSet<K,V>` — add/remove/contains/iterator | MapSetTest |
| AutoCloseableExtensions.kt | `closeSafe()` | AutoCloseableExtensionsTest |
| ActionState.kt | `isReady`, `isRunning`, `isFinished` | ActionStateTest |
| DataState.kt | `Loading`, `Success`, `Error`, `toLoading()`, `toError()` | DataStateTest |
| Stateful.kt | `Loading`, `Failure`, `Success` | StatefulTest |
| MediaInfo.kt | `hasAudio`, `hasVideo`, `formatDuration()`, `summary()` | MediaInfoTest |

## Android 扩展函数（需要 Robolectric 或 Instrumented Test）

| 文件 | 函数 |
|------|------|
| BundleExtensions.kt | `getParcelableSafe()` |
| ContextExtensions.kt | 上下文工具 |
| FragmentExtensions.kt | Fragment 工具 |
| ViewExtensions.kt | View 动画/布局工具 |
| IntentPathExtensions.kt | `Intent.extraPath` |
| ParcelableArgs.kt | Bundle args 序列化 |

## 测试策略
- 纯 Kotlin 函数: JUnit 4 + Google Truth 断言
- Android 函数: Robolectric (sdk=34) 或 Instrumented Test
