---
name: analyze-ui
description: '分析Android工程所有页面的UI元素。扫描每个Activity/Fragment的布局文件，提取所有按钮、文字、行间距、字体大小和按钮逻辑，汇总成表格供用户审查。USE FOR: UI审查、按钮清理、页面元素盘点、代码逻辑审查。Use when: 分析页面、审查UI、按钮盘点、页面元素汇总。'
argument-hint: '可选：指定要分析的页面名称，留空则分析全部页面'
---

# Android 页面 UI 元素分析

## 目标

扫描当前工程所有的 Activity、Fragment 页面，逐一分析每个页面上的：
- **按钮**（Button / ImageButton / FloatingActionButton / MaterialButton / SpeedDial / MenuItem 等）
- **文字**（TextView / EditText 中的 text / hint）
- **行间距**（lineSpacingExtra / lineSpacingMultiplier / lineHeight）
- **字体大小**（textSize / textAppearance）
- **按钮逻辑**（setOnClickListener / onClick 绑定的处理方法及具体逻辑）

最终汇总成 **Markdown 表格** 输出给用户，让用户逐条判断是否保留。

## 执行步骤

### 第一步：定位所有页面文件

1. 搜索所有 `*Activity.kt` 和 `*Fragment.kt` 文件（路径 `app/src/main/java/`）
2. 在每个 Activity/Fragment 中，找到它引用的布局文件：
   - `setContentView(R.layout.xxx)` 
   - `inflater.inflate(R.layout.xxx, ...)`
   - `binding = XxxBinding.inflate(...)` → 对应 `layout/xxx.xml`
3. 记录 **页面名称 → 布局文件** 的映射关系

### 第二步：分析布局 XML 文件

对每个布局 XML 文件（`app/src/main/res/layout/*.xml`），提取以下信息：

#### 2.1 按钮元素
扫描以下标签：
- `<Button>`, `<com.google.android.material.button.MaterialButton>`
- `<ImageButton>`, `<FloatingActionButton>`
- `<com.leinardi.android.speeddial.SpeedDialView>`
- 任何带有 `android:onClick` 属性的 View
- `<include>` 引用的子布局中的按钮

记录每个按钮的：
- `android:id` → 按钮ID
- `android:text` / `app:text` → 按钮文字（可能引用 `@string/xxx`，需要查 strings.xml 解析实际文字）
- `android:contentDescription` → 辅助描述
- `style` → 按钮样式

#### 2.2 文字元素
扫描 `<TextView>`, `<EditText>`, `<TextInputEditText>`, `<TextInputLayout>` 等：
- `android:text` → 显示文字
- `android:hint` → 提示文字
- `android:textSize` → 字体大小（如 `14sp`）
- `android:textAppearance` → 文字样式引用（如 `?textAppearanceBodyMedium`）
- `android:lineSpacingExtra` → 行间距额外值
- `android:lineSpacingMultiplier` → 行间距倍数
- `android:lineHeight` → 行高

#### 2.3 菜单文件
检查对应的 Activity/Fragment 是否引用了菜单文件：
- `onCreateOptionsMenu` → `menuInflater.inflate(R.menu.xxx, menu)`
- `toolbar.inflateMenu(R.menu.xxx)`

扫描对应菜单 XML（`app/src/main/res/menu/*.xml`）中的 `<item>` 标签：
- `android:id` → 菜单项ID
- `android:title` → 菜单标题
- `android:icon` → 图标
- `app:showAsAction` → 显示方式

### 第三步：分析按钮逻辑

在对应的 Activity/Fragment Kotlin 文件中，搜索每个按钮的点击处理逻辑：

1. 搜索 `binding.buttonId.setOnClickListener` 或 `findViewById(R.id.buttonId).setOnClickListener`
2. 搜索 `when (item.itemId)` 中的菜单处理
3. 搜索 `onOptionsItemSelected` 中的菜单项处理
4. 搜索 `SpeedDialView` 的 `setOnActionSelectedListener`
5. 提取点击后的具体操作逻辑（简要描述，如"打开文件"、"删除确认"、"跳转设置页"等）

### 第四步：解析字符串资源

查询 `app/src/main/res/values/strings.xml` 和 `strings_npzlib.xml`，将 `@string/xxx` 引用解析为实际文字。

### 第五步：输出汇总表格

按照以下格式，**逐页面** 输出 Markdown 表格：

```markdown
## 页面：[Activity/Fragment名称]
**布局文件**: `layout/xxx.xml`  
**菜单文件**: `menu/xxx.xml`（如有）

### 按钮列表
| 序号 | 按钮ID | 按钮类型 | 显示文字 | 点击逻辑 | 是否保留？ |
|------|--------|----------|----------|----------|-----------|
| 1 | btn_save | MaterialButton | 保存 | 调用saveFile()保存当前文件 | ✅/❌ |
| 2 | fab_add | FAB | + | 弹出SpeedDial菜单 | ✅/❌ |
| 3 | menu_delete | MenuItem | 删除 | 弹出确认对话框后删除选中文件 | ✅/❌ |

### 文字元素
| 序号 | 元素ID | 元素类型 | 显示文字/提示 | 字体大小 | 行间距 | 文字样式 |
|------|--------|----------|--------------|----------|--------|---------|
| 1 | tv_title | TextView | 文件名 | 16sp | - | textAppearanceTitleMedium |
| 2 | et_search | EditText | 搜索文件... (hint) | 14sp | 1.2x | textAppearanceBodyMedium |

### 代码逻辑概要
| 逻辑点 | 描述 | 是否保留？ |
|--------|------|-----------|
| onCreate初始化 | 初始化ViewModel，绑定RecyclerView | ✅/❌ |
| 文件排序逻辑 | 按名称/大小/日期排序 | ✅/❌ |
```

## 分析范围

如果用户指定了页面名称（通过参数传入），只分析该页面。否则按以下分组 **逐组分析全部页面**：

### 分组1：主界面
- AppActivity → FileListFragment → NavigationFragment

### 分组2：文件操作对话框
- CreateDirectoryDialogFragment, CreateFileDialogFragment, RenameFileDialogFragment
- ConfirmDeleteFilesDialogFragment, BatchRenameDialogFragment
- CreateArchiveDialogFragment, ArchivePasswordDialogFragment
- OpenFileAsDialogFragment, PathDialogFragment, NavigateToPathDialogFragment

### 分组3：文件查看器
- TextEditorActivity/Fragment, ImageViewerActivity/Fragment
- AudioPlayerActivity/Fragment, VideoViewerActivity/Fragment
- PdfViewerActivity/Fragment, HexViewerActivity/Fragment
- CsvViewerActivity/Fragment, EbookViewerActivity/Fragment
- LargeTextViewerFragment

### 分组4：工具页面
- ToolGroupFragment, AppManagerFragment, DuplicateFinderFragment
- EmptySearchFragment, EncryptionFragment, FileCompareFragment
- FileSearchFragment, FormatConvertFragment, ImageCompressFragment
- MediaToolsFragment, RecentFilesFragment, StorageAnalysisFragment
- TrashFragment

### 分组5：设置与存储
- SettingsActivity/Fragment, SettingsPreferenceFragment
- StandardDirectoryListFragment, BookmarkDirectoryListFragment
- StorageListFragment, AddDocumentTreeFragment
- AddExternalStorageShortcutFragment

### 分组6：文件属性
- FilePropertiesDialogFragment 及所有 Tab Fragment
- SetModeDialogFragment, SetOwnerDialogFragment 等权限对话框

### 分组7：系统对话框
- FileJobErrorDialogFragment, FileJobConflictDialogFragment
- 各种权限申请 Rationale 对话框

### 分组8：终端
- TerminalActivity/Fragment

### 分组9：关于页面
- AboutFragment

## 输出要求

1. **每次只分析一个分组**，输出该分组所有页面的表格后，询问用户是否继续分析下一组
2. 表格中的「是否保留？」列留空，等待用户填写
3. 如果某个布局文件通过 `<include>` 引用了其他布局，也要展开分析
4. 解析 `@string/xxx` 为实际文字显示
5. 字体大小如果来自 `textAppearance`，注明样式名即可
6. 按钮逻辑简要描述（一句话），不需要粘贴源代码

## 注意事项

- 本工程使用 ViewBinding（`XxxBinding.inflate`），注意通过 binding 名称反推布局文件名
- SpeedDial 的 action 定义在菜单 XML 中，需要一并分析
- `<include>` 标签引用的子布局也要扫描
- Toolbar 的菜单需要同时检查 XML 定义和代码中的动态添加
- Preference 页面的 XML 在 `res/xml/` 目录下，不是 `res/layout/`
