# Skill: filelist — 文件列表浏览

## 概述
`naipingzai.materialfile.filelist` 是应用核心模块，包含 45+ 个文件。

## 关键类

| 类 | 职责 |
|----|------|
| FileListActivity | 主 Activity |
| FileListFragment | 文件列表 Fragment |
| FileListViewModel | 导航/搜索/排序/视图类型 |
| FileListAdapter | RecyclerView 适配器 |
| FileListLiveData | 异步加载文件列表 |
| SearchFileListLiveData | 搜索结果 LiveData |
| BreadcrumbLayout | 面包屑路径导航 |
| FileSortOptions | 排序选项 |

## 可测试接口 (FileListViewModel)

| 方法 | 说明 |
|------|------|
| `navigateTo(state, path)` | 导航到路径 |
| `resetTo(path)` | 重置到路径 |
| `navigateUp()` | 向上导航 |
| `search(query)` | 搜索 |
| `stopSearching()` | 停止搜索 |
| `reload()` | 重新加载 |

## 可测试扩展 (FileItemExtensions)

| 属性/方法 | 说明 |
|-----------|------|
| `name` | 文件名 |
| `baseName` | 文件基名 |
| `extension` | 扩展名 |
| `isArchiveFile` | 是否归档 |
| `isListable` | 是否可列举 |
| `supportsThumbnail` | 是否支持缩略图 |

## 测试策略
- Activity: Instrumented Test
- ViewModel: Robolectric Unit Test
- FileSortOptions: Unit Test
- FileItemExtensions: Robolectric
