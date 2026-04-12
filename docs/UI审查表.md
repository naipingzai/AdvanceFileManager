# Android 工程 UI 元素审查表

> **用法**：
> 
> - 在「是否保留？」列填入 ✅（保留）/ ❌（删除）/ ❓（待定）。
> - 如果选择 ❌，请在「删除后修改方案」列描述删除该元素/逻辑后的处理方式。
>   例如：去掉"升序按钮"后 → "默认始终升序排序，移除 setSortOrder 逻辑"。
>   不清楚可填 `?` 等待讨论。
> - 全部分组完成后，会基于此表生成删除/保留清单和迁移方案。

## 目录

- [分组 1：主界面](#分组-1主界面)
- [分组 2：文件操作对话框](#分组-2文件操作对话框)
- [分组 3：文件查看器](#分组-3文件查看器)
- [分组 4：工具页面](#分组-4工具页面)
- [分组 5：设置与存储](#分组-5设置与存储)
- [分组 6：文件属性与权限对话框](#分组-6文件属性与权限对话框)
- [分组 7：系统对话框](#分组-7系统对话框)
- [分组 8：终端](#分组-8终端)
- [分组 9：关于页面](#分组-9关于页面)

---

## 分组 1：主界面

### 页面：AppActivity

**布局**：无（抽象基类，仅承载 Fragment）  **菜单**：无

#### 核心代码逻辑

| 序号  | 模块    | 描述                                                | 涉及方法                        | 是否保留？ | 删除后修改方案         |
| --- | ----- | ------------------------------------------------- | --------------------------- | ----- | --------------- |
| 1   | 字体缩放  | 根据 `Settings.FONT_SCALE` 调整全局字号；FOLLOW_SYSTEM 不覆盖 | `applyFontScale()`          | N     | 去除字体缩放功能        |
| 2   | 夜间模式  | 应用日/夜主题                                           | `NightModeHelper.apply()`   | N     | 去除夜间模式功能，只要日间模式 |
| 3   | 自定义主题 | 应用自定义主题色                                          | `CustomThemeHelper.apply()` | N     | 去掉自定义主题         |
| 4   | 导航返回  | 处理 ActionBar 上箭头返回                                | `onSupportNavigateUp()`     | Y     |                 |

---

### 页面：FileListFragment（文件列表主页面）

**布局**：`file_list_fragment.xml` → include `file_list_fragment_include.xml` + 底部栏  
**菜单**：`file_list.xml`、`file_list_select.xml`、`file_list_paste.xml`、`file_list_pick.xml`、`file_list_pick_bottom.xml`、`file_list_speed_dial.xml`、`file_list_breadcrumb.xml`

#### 按钮列表（普通模式 file_list.xml）

| 序号  | 按钮ID                             | 类型         | 显示文字      | 点击逻辑                               | 是否保留？ | 删除后修改方案                                                                      |
| --- | -------------------------------- | ---------- | --------- | ---------------------------------- | ----- | ---------------------------------------------------------------------------- |
| 1   | `action_search`                  | SearchView | 搜索        | 展开搜索框，1s 防抖触发 `viewModel.search()` | N     | 删除顶部的搜索功能和图标                                                                 |
| 2   | `action_view_sort`               | 子菜单        | 视图与排序     | 弹出视图模式与排序子菜单                       | Y     |                                                                              |
| 3   | `action_view_list`               | MenuItem   | 列表视图      | `FileViewType.LIST`                | Y     |                                                                              |
| 4   | `action_view_grid`               | MenuItem   | 网格视图      | `FileViewType.GRID`                | Y     | 网格视图增加多列配置，支持2-5列调节，调节控制使用手势控制，放大手势减少列数，捏合手势增加列数，列数进行记忆，切换网格视图的时候默认调节到这个列数配置 |
| 5   | `action_sort_by_name`            | MenuItem   | 按名称       | `setSortBy(NAME)`                  | Y     |                                                                              |
| 6   | `action_sort_by_type`            | MenuItem   | 按类型       | `setSortBy(TYPE)`                  | Y     |                                                                              |
| 7   | `action_sort_by_size`            | MenuItem   | 按大小       | `setSortBy(SIZE)`                  | N     | 删除按大小排序                                                                      |
| 8   | `action_sort_by_last_modified`   | MenuItem   | 按最后修改     | `setSortBy(LAST_MODIFIED)`         | Y     |                                                                              |
| 9   | `action_sort_order_ascending`    | Checkable  | 升序        | `setSortOrder(ASC/DESC)`           | N     | 删除升序排序设置，默认升序                                                                |
| 10  | `action_sort_directories_first`  | Checkable  | 文件夹优先     | `setSortDirectoriesFirst()`        | Y     |                                                                              |
| 11  | `action_view_sort_path_specific` | Checkable  | 仅当前文件夹    | 路径专属排序设置                           | N     | 删除该功能                                                                        |
| 12  | `action_new_task`                | MenuItem   | 在新窗口打开    | 启动新 `FileListActivity`             | Y     |                                                                              |
| 13  | `action_refresh`                 | MenuItem   | 刷新（R）     | `refresh()`                        | Y     |                                                                              |
| 14  | `action_select_all`              | MenuItem   | 全选（A）     | 选中所有文件                             | Y     |                                                                              |
| 15  | `action_show_hidden_files`       | Checkable  | 显示隐藏文件（H） | `setShowHiddenFiles()`             | Y     |                                                                              |
| 16  | `action_share`                   | MenuItem   | 分享        | 分享当前路径                             | N     | 删除分享当前路径                                                                     |
| 17  | `action_copy_path`               | MenuItem   | 复制路径      | 复制路径到剪贴板                           | N     | 删除复制路径功能                                                                     |
| 18  | `action_add_bookmark`            | MenuItem   | 添加书签      | 添加当前目录为书签                          | N     | 删除添加书签功能                                                                     |
| 19  | `action_storage_analysis`        | MenuItem   | 存储分析      | 跳转存储分析工具                           | N     | 删除存储分析功能                                                                     |
| 20  | `action_file_search`             | MenuItem   | 文件搜索      | 跳转文件搜索工具                           | N     | 整合文件搜索 重复文件查找 空文件查找 最近文件 到一个功能按钮里面                                           |
| 21  | `action_duplicate_finder`        | MenuItem   | 重复文件查找    | 跳转重复查找工具                           | N     | 同上                                                                           |
| 22  | `action_empty_search`            | MenuItem   | 空文件查找     | 跳转空文件搜索工具                          | N     | 同上                                                                           |
| 23  | `action_recent_files`            | MenuItem   | 最近文件      | 跳转最近文件                             | N     | 同上                                                                           |

#### 按钮列表（选择/粘贴/Pick/面包屑/SpeedDial）

| 序号  | 按钮ID                            | 类型              | 显示文字       | 点击逻辑                     | 是否保留？ | 删除后修改方案                                            |
| --- | ------------------------------- | --------------- | ---------- | ------------------------ | ----- | -------------------------------------------------- |
| 24  | `action_cut`                    | MenuItem        | 剪切（X）      | `cutFiles()`             | Y     |                                                    |
| 25  | `action_copy`                   | MenuItem        | 复制/提取（C）   | `copyFiles()`，档案文件时变"提取" | Y     |                                                    |
| 26  | `action_delete`                 | MenuItem        | 删除         | 弹确认后删除                   | Y     |                                                    |
| 27  | `action_extract`                | MenuItem        | 提取         | 解压档案文件                   | Y     |                                                    |
| 28  | `action_archive`                | MenuItem        | 压缩         | 弹"创建档案"对话框               | Y     |                                                    |
| 29  | `action_batch_rename`           | MenuItem        | 批量重命名      | 弹批量重命名对话框                | Y     |                                                    |
| 30  | `action_format_convert`         | MenuItem        | 格式转换       | 选中均为媒体时显示，跳转格式转换         | Y     | 把按钮默认显示，不用判断是否是媒体文件，也不是格式转换，改成媒体工具，把所有媒体工具整合到一个页面来 |
| 31  | `action_share` (select)         | MenuItem        | 分享         | 分享选中文件                   | Y     |                                                    |
| 32  | `action_paste`                  | MenuItem        | 粘贴（V）      | 执行粘贴任务                   | Y     |                                                    |
| 33  | `action_open`                   | MenuItem        | 选择（Pick）   | 返回所选文件给调用方               | Y     |                                                    |
| 34  | `action_create`                 | MenuItem        | 保存（SaveAs） | 创建新文件返回给调用方              | Y     |                                                    |
| 35  | `action_copy_path` (breadcrumb) | MenuItem        | 复制路径       | 复制该面包屑路径                 | N     | 删除复制路径功能                                           |
| 36  | `action_open_in_new_task`       | MenuItem        | 在新窗口打开     | 启动新窗口到该路径                | Y     |                                                    |
| 37  | `speedDial`                     | FAB             | +          | 展开 SpeedDial             | Y     |                                                    |
| 38  | `action_create_directory`       | SpeedDialAction | 新建文件夹      | 弹新建目录对话框                 | Y     |                                                    |
| 39  | `action_create_file`            | SpeedDialAction | 新建文件       | 弹新建文件对话框                 | Y     |                                                    |

#### 文字元素

| 序号  | 元素ID                       | 类型                       | 显示内容        | 字体大小 | 行间距 | 样式                                   | 是否保留？ | 删除后修改方案 |
| --- | -------------------------- | ------------------------ | ----------- | ---- | --- | ------------------------------------ | ----- | ------- |
| 1   | `toolbar`                  | CrossfadeSubtitleToolbar | "文件" / 当前路径 | —    | —   | Material Toolbar                     | Y     |         |
| 2   | `breadcrumbLayout`         | BreadcrumbLayout         | 路径段动态渲染     | —    | —   | —                                    | Y     |         |
| 3   | `text` (breadcrumb_item)   | TextView                 | 文件夹名        | —    | —   | textAppearanceTitleSmall, maxLines=1 | Y     |         |
| 4   | `errorText`                | TextView                 | 错误提示（动态）    | —    | —   | textAppearanceListItem               | Y     |         |
| 5   | `emptyView`                | TextView                 | "无文件"       | —    | —   | textAppearanceListItem               | Y     |         |
| 6   | `progress`                 | ProgressBar              | 加载中         | —    | —   | —                                    | Y     |         |
| 7   | `bottomCreateFileNameEdit` | TextInputEditText        | hint："文件名"  | —    | —   | maxLines=1                           | Y     |         |

#### 核心代码逻辑

| 序号  | 模块        | 描述                                           | 是否保留？ | 删除后修改方案 |
| --- | --------- | -------------------------------------------- | ----- | ------- |
| 1   | 列表初始化     | 创建 ViewModel/Adapter，配置 GridLayoutManager    | Y     |         |
| 2   | 菜单分发      | `onMenuItemSelected()` 处理 20+ 菜单项            | Y     |         |
| 3   | 排序        | 4 种排序 + 升降序 + 文件夹优先                          | Y     |         |
| 4   | 搜索        | SearchView + 1s 防抖触发 search                  | N     | 删除      |
| 5   | 选择状态      | `onSelectedFilesChanged()` 动态显示选择菜单          | Y     |         |
| 6   | 文件操作      | 剪/复/删/解压/压缩                                  | Y     |         |
| 7   | 粘贴态       | `addToPasteState()` + 粘贴菜单                   | Y     |         |
| 8   | 面包屑导航     | `BreadcrumbLayout.Listener` → `navigateTo()` | Y     |         |
| 9   | 存储权限      | `requestStoragePermissionLauncher`           | Y     |         |
| 10  | SpeedDial | 新建文件/文件夹                                     | Y     |         |
| 11  | 返回键       | 返回上级 / 关搜索 / 关 SpeedDial                     | Y     |         |
| 12  | 抽屉        | 打开导航抽屉                                       | Y     |         |
| 13  | 视图切换      | LIST / GRID                                  | Y     |         |
| 14  | 隐藏文件      | 过滤显示                                         | Y     |         |
| 15  | 下拉刷新      | SwipeRefreshLayout                           | Y     |         |

---

### 页面：NavigationFragment（导航抽屉）

**布局**：`navigation_fragment.xml`（仅一个 RecyclerView）+ `navigation_item.xml`  **菜单**：无

#### 按钮 / 文字 / 逻辑

| 序号  | 元素             | 类型                              | 显示内容 / 描述                                          | 样式               | 是否保留？ | 删除后修改方案 |
| --- | -------------- | ------------------------------- | -------------------------------------------------- | ---------------- | ----- | ------- |
| 1   | `itemLayout`   | CheckableForegroundLinearLayout | 整行点击导航 / 长按菜单                                      | —                | Y     |         |
| 2   | `iconImage`    | ImageView                       | 导航项图标                                              | —                | Y     |         |
| 3   | `titleText`    | TextView                        | 标题（书签/存储/工具名）                                      | maxLines=1, 省略号  | Y     |         |
| 4   | `subtitleText` | AutoGoneTextView                | 副标题（路径/容量等）                                        | maxLines=1, 自动隐藏 | Y     |         |
| 5   | 列表初始化          | —                               | LinearLayoutManager + NavigationListAdapter        | —                | Y     |         |
| 6   | 项数据观察          | —                               | `NavigationItemListLiveData` 动态更新                  | —                | Y     |         |
| 7   | 当前路径监听         | —                               | 高亮当前导航项                                            | —                | Y     |         |
| 8   | 导航 / Intent 启动 | —                               | `navigateTo()` / `navigateToRoot()` / 设置/关于 Intent | —                | Y     |         |
| 9   | 抽屉关闭           | —                               | `closeNavigationDrawer()`                          | —                | Y     |         |

---

## 分组 2：文件操作对话框

### 通用元素

所有名称类对话框（CreateDirectory / CreateFile / Rename / Archive / Path / NavigateToPath）均使用：

- 布局：`name_dialog.xml`（`nameLayout` + `nameEdit`）
- 按钮：Positive=`OK`、Negative=`Cancel`
- 校验：`isNameValid()`，错误：空 / 非法字符 / 已存在 / 路径错误

### 页面：CreateDirectoryDialogFragment / CreateFileDialogFragment / RenameFileDialogFragment / NavigateToPathDialogFragment

| 序号  | 项                | 类型                | 内容 / 描述                                                                              | 是否保留？ | 删除后修改方案 |
| --- | ---------------- | ----------------- | ------------------------------------------------------------------------------------ | ----- | ------- |
| 1   | 标题 (Create Dir)  | TextView          | "New folder"                                                                         | Y     |         |
| 2   | 标题 (Create File) | TextView          | "New file"                                                                           | Y     |         |
| 3   | 标题 (Rename)      | TextView          | "Rename"，初始光标选中 baseName                                                             | Y     |         |
| 4   | 标题 (NavigateTo)  | TextView          | "Go to"，初值=`path.toUserFriendlyString()`                                             | Y     |         |
| 5   | `nameEdit`       | TextInputEditText | 名称 / 路径输入                                                                            | Y     |         |
| 6   | OK               | Button            | 触发 `onOk(name/path)` 回调 → `FileJobService.create/rename` 或 `viewModel.resetTo(path)` | Y     |         |
| 7   | Cancel           | Button            | 关闭                                                                                   | Y     |         |
| 8   | 校验逻辑             | —                 | `FileNameDialogFragment.isNameValid()` / `PathDialogFragment` URI 解析                 | Y     |         |
| 9   | Toast 提示         | —                 | "Rename task started…"                                                               | Y     |         |

### 页面：BatchRenameDialogFragment（`batch_rename_dialog.xml`）

| 序号  | 项              | 类型                | 内容 / 描述                                                                     | 样式                                                             | 是否保留？ | 删除后修改方案 |
| --- | -------------- | ----------------- | --------------------------------------------------------------------------- | -------------------------------------------------------------- | ----- | ------- |
| 1   | 标题             | TextView          | "Batch rename"                                                              | —                                                              | Y     |         |
| 2   | `nameEdit`     | TextInputEditText | hint="Base name"，初值"File"                                                   | —                                                              | Y     |         |
| 3   | `previewLabel` | TextView          | "Preview"                                                                   | textAppearanceTitleMedium                                      | Y     |         |
| 4   | `previewText`  | TextView          | 动态预览 "File-001.ext"                                                         | textAppearanceBodyMedium, monospace, lineSpacingMultiplier=1.3 | Y     |         |
| 5   | OK             | Button            | `listener.batchRenameFiles(files, baseName)` → `FileJobService.batchRename` | —                                                              | Y     |         |
| 6   | 预览生成           | —                 | `updatePreview()` 计算数字位数+保留扩展名+最多 5 行                                       | —                                                              | Y     |         |

### 页面：CreateArchiveDialogFragment（`create_archive_dialog.xml`）

| 序号  | 元素ID           | 类型                | 显示文字 / 描述                                          | 是否保留？ | 删除后修改方案 |
| --- | -------------- | ----------------- | -------------------------------------------------- | ----- | ------- |
| 1   | 标题             | TextView          | "Create archive"                                   | Y     |         |
| 2   | `nameEdit`     | TextInputEditText | 名称（自动追加扩展名）                                        | Y     |         |
| 3   | `zipRadio`     | RadioButton       | ".zip"                                             | Y     |         |
| 4   | `tarXzRadio`   | RadioButton       | ".tar.xz"                                          | Y     |         |
| 5   | `sevenZRadio`  | RadioButton       | ".7z"                                              | Y     |         |
| 6   | `passwordEdit` | TextInputEditText | hint="Password (optional)"，仅 ZIP 显示                | Y     |         |
| 7   | OK             | Button            | `listener.archive(...)` → `FileJobService.archive` | Y     |         |
| 8   | 格式切换逻辑         | —                 | `updatePasswordLayoutVisibility()`                 | Y     |         |

### 页面：ArchivePasswordDialogFragment（`archive_password_dialog.xml`）

| 序号  | 元素             | 类型                | 内容 / 描述                                         | 是否保留？ | 删除后修改方案 |
| --- | -------------- | ----------------- | ----------------------------------------------- | ----- | ------- |
| 1   | 标题             | TextView          | "Password required"                             | Y     |         |
| 2   | 消息             | TextView          | `"%1$s" is password-protected.`                 | Y     |         |
| 3   | `passwordEdit` | TextInputEditText | inputType=textPassword，回车确认                     | Y     |         |
| 4   | OK             | Button            | 校验非空 → `path.archiveAddPassword(password)` → 回调 | Y     |         |
| 5   | Cancel         | Button            | 回调 false 并 finish                               | Y     |         |
| 6   | 错误提示           | —                 | "Password cannot be empty"                      | Y     |         |
| 7   | 不可外部取消         | —                 | `setCanceledOnTouchOutside(false)`              | Y     |         |

### 页面：OpenFileAsDialogFragment（AlertDialog setItems）

| 序号  | 项     | 类型   | 显示文字             | 点击逻辑                   | 是否保留？ | 删除后修改方案 |
| --- | ----- | ---- | ---------------- | ---------------------- | ----- | ------- |
| 1   | 标题    | —    | `Open "%1$s" as` |                        | Y     |         |
| 2   | item1 | List | Text             | `openAs(text/plain)`   | Y     |         |
| 3   | item2 | List | Image            | `openAs(image/*)`      | Y     |         |
| 4   | item3 | List | Audio            | `openAs(audio/*)`      | Y     |         |
| 5   | item4 | List | Video            | `openAs(video/*)`      | Y     |         |
| 6   | item5 | List | Folder           | `openAs(directory)`    | Y     |         |
| 7   | item6 | List | Other            | `openAs(any)`          | Y     |         |
| 8   | item7 | List | Hex              | 启动 `HexViewerActivity` | Y     |         |

### 页面：ConfirmDeleteFilesDialogFragment（无自定义布局）

| 序号  | 项       | 内容 / 描述                                                 | 是否保留？ | 删除后修改方案 |
| --- | ------- | ------------------------------------------------------- | ----- | ------- |
| 1   | 消息（单文件） | `Delete "xxx"?`                                         | Y     |         |
| 2   | 消息（单目录） | `Delete folder "xxx" and its contents?`                 | Y     |         |
| 3   | 消息（多文件） | Plural: `Delete %d files?`                              | Y     |         |
| 4   | 消息（多目录） | Plural: `Delete %d folders and their contents?`         | Y     |         |
| 5   | 消息（混合）  | Plural: `Delete %d items?`                              | Y     |         |
| 6   | OK      | `listener.deleteFiles(files)` → `FileJobService.delete` | Y     |         |
| 7   | Cancel  | 关闭                                                      | Y     |         |

---

## 分组 3：文件查看器

### 页面：TextEditorFragment（`text_editor_fragment.xml` / `text_editor.xml`）

| 序号  | 元素                | 类型              | 内容 / 描述                                    | 是否保留？ | 删除后修改方案 |
| --- | ----------------- | --------------- | ------------------------------------------ | ----- | ------- |
| 1   | `action_save`     | MenuItem        | Save，调用 `save()` → ViewModel.writeFile     | Y     |         |
| 2   | `action_reload`   | MenuItem        | Reload，弹确认后重载                              | Y     |         |
| 3   | `action_encoding` | SubMenu         | Encoding，从 COMMON_CHARSETS 动态生成            | Y     |         |
| 4   | `toolbar`         | MaterialToolbar | 文件名                                        | Y     |         |
| 5   | `progress`        | ProgressBar     | 加载中                                        | Y     |         |
| 6   | `errorText`       | TextView        | 错误提示 (textAppearanceListItem)              | Y     |         |
| 7   | `textEdit`        | EditText        | 文件内容 (textAppearanceListItemSecondary)     | Y     |         |
| 8   | 文本变化跟踪            | —               | `doAfterTextChanged` 设置 isTextChanged      | Y     |         |
| 9   | 返回确认              | —               | OnBackPressedCallback 弹确认                  | Y     |         |
| 10  | EditText 状态恢复     | —               | savedState 防止 TransactionTooLargeException | Y     |         |

### 页面：LargeTextViewerFragment（`large_text_viewer_fragment.xml` / `large_text_viewer.xml`）

| 序号  | 元素                  | 类型                      | 内容 / 描述                         | 是否保留？ | 删除后修改方案 |
| --- | ------------------- | ----------------------- | ------------------------------- | ----- | ------- |
| 1   | `action_go_to_line` | MenuItem                | 输入行号跳转                          | Y     |         |
| 2   | `action_encoding`   | SubMenu                 | 切换编码重载                          | Y     |         |
| 3   | `infoBar`           | TextView                | "500 lines                      | Y     |         |
| 4   | `loadingMore`       | LinearProgressIndicator | 加载更多                            | Y     |         |
| 5   | 分块加载                | —                       | 512KB/块，滚动至 lines.size-200 自动加载 |       |         |
| 6   | 行分割                 | —                       | LINE_SPLIT_REGEX `\r\n          | Y     |         |
| 7   | RecyclerView 渲染     | —                       | 替代 WebView 防 OOM                | Y     |         |

### 页面：ImageViewerFragment（`image_viewer_fragment.xml` / `image_viewer.xml`）

| 序号  | 元素              | 类型              | 内容 / 描述                        | 是否保留？ | 删除后修改方案 |
| --- | --------------- | --------------- | ------------------------------ | ----- | ------- |
| 1   | `action_delete` | MenuItem        | Delete，弹确认后删除                  | Y     |         |
| 2   | `action_share`  | MenuItem        | Share，调用 createSendImageIntent | Y     |         |
| 3   | `toolbar`       | MaterialToolbar | "filename                      | Y     |         |
| 4   | ViewPager2      | —               | 多图浏览，DepthPageTransformer 动画   | Y     |         |
| 5   | 系统UI隐藏          | —               | SystemUiHelper toggle          | Y     |         |
| 6   | 页码动态            | —               | registerOnPageChangeCallback   | Y     |         |

### 页面：AudioPlayerFragment（`audio_player_fragment.xml` / `audio_player.xml`）

| 序号  | 元素                    | 类型        | 内容 / 描述                                    | 是否保留？ | 删除后修改方案 |
| --- | --------------------- | --------- | ------------------------------------------ | ----- | ------- |
| 1   | `action_delete`       | MenuItem  | Delete                                     | Y     |         |
| 2   | `action_share`        | MenuItem  | Share                                      | Y     |         |
| 3   | `seekBar`             | Slider    | valueFrom=0, valueTo=1000                  | Y     |         |
| 4   | `albumArt`            | ImageView | 封面，长按调速 / 短按播放暂停                           | Y     |         |
| 5   | `speedIndicator`      | TextView  | "1.0x/1.5x/2.0x"，textAppearanceTitleMedium | Y     |         |
| 6   | `seekCurrentTimeText` | TextView  | "00:00"                                    | Y     |         |
| 7   | `seekTotalTimeText`   | TextView  | "03:45"                                    | Y     |         |
| 8   | ExoPlayer             | —         | MediaItem 文件 URI                           | Y     |         |
| 9   | 手势                    | —         | 长按调速 / 横向滑动 100ms/像素 快进退                   | Y     |         |
| 10  | 进度同步                  | —         | 500ms updateRunnable                       | Y     |         |
| 11  | 专辑封面                  | —         | MediaMetadataRetriever                     | Y     |         |
| 12  | 播放列表                  | —         | paths 前后曲目切换                               | Y     |         |

### 页面：VideoViewerFragment（`video_viewer_fragment.xml` / `video_viewer.xml`）

| 序号  | 元素               | 类型         | 内容 / 描述                  | 是否保留？ | 删除后修改方案                                                                                |
| --- | ---------------- | ---------- | ------------------------ | ----- | -------------------------------------------------------------------------------------- |
| 1   | `action_delete`  | MenuItem   | Delete                   | Y     |                                                                                        |
| 2   | `action_share`   | MenuItem   | Share                    | Y     |                                                                                        |
| 3   | `seekBar`        | Slider     | 进度条                      | Y     |                                                                                        |
| 4   | `playerView`     | PlayerView | use_controller=false 自定义 | Y     |                                                                                        |
| 5   | `speedIndicator` | TextView   | 倍速显示                     | Y     |                                                                                        |
| 6   | 双击快进退            | —          | 左半屏退10s/右半屏进30s          | Y     |                                                                                        |
| 7   | 长按调速             | —          | 1.0x/1.5x/2.0x 循环        | N     | 长按进入倍速，长按并向左滑动减慢倍速，先提示到0.75x，继续就是0.5x，最低0.25x，右滑就是2.0x 3.0x 4.0x 5.0x，范围就是0.25x - 5.0x |
| 8   | 系统UI             | —          | SystemUiHelper           | Y     |                                                                                        |
| 9   | 时间格式             | —          | formatTime() HH:mm:ss    | Y     |                                                                                        |

### 页面：PdfViewerFragment（`pdf_viewer_fragment.xml` / `pdf_viewer.xml`）

| 序号  | 元素              | 类型       | 内容 / 描述                                      | 是否保留？ | 删除后修改方案 |
| --- | --------------- | -------- | -------------------------------------------- | ----- | ------- |
| 1   | `action_share`  | MenuItem | Share                                        | Y     |         |
| 2   | `pageIndicator` | TextView | "45 / 120"，textAppearanceLabelMedium         | Y     |         |
| 3   | `fileSizeText`  | TextView | "File size: 2.5 MB"，textAppearanceLabelSmall | Y     |         |
| 4   | PdfRenderer     | —        | ParcelFileDescriptor 打开                      | Y     |         |
| 5   | 临时文件            | —        | content:// 转临时文件                             | Y     |         |
| 6   | 滚动监听            | —        | OnScrollListener 更新页码                        | Y     |         |
| 7   | 资源清理            | —        | onDestroyView 关闭 PdfRenderer                 | Y     |         |

### 页面：HexViewerFragment（`hex_viewer_fragment.xml` / `hex_viewer.xml` / `hex_row_item.xml`）

| 序号  | 元素                    | 类型       | 内容 / 描述                       | 是否保留？ | 删除后修改方案 |
| --- | --------------------- | -------- | ----------------------------- | ----- | ------- |
| 1   | `action_go_to_offset` | MenuItem | 跳转字节偏移                        | Y     |         |
| 2   | `fileSizeText`        | TextView | "File size: 1024 KB"          | Y     |         |
| 3   | 列头 Offset/Hex/ASCII   | TextView | textAppearanceLabelSmall bold | Y     |         |
| 4   | 分块读取                  | —        | 16B/行，CHUNK 16KB，预加载 1MB      | Y     |         |
| 5   | 加载更多按钮                | —        | "Load remaining (xMB)"        | Y     |         |
| 6   | Go to offset          | —        | offset/16 → 行号                | Y     |         |

### 页面：CsvViewerFragment（`csv_viewer_fragment.xml`）

| 序号  | 元素          | 类型          | 内容 / 描述              | 是否保留？ | 删除后修改方案 |
| --- | ----------- | ----------- | -------------------- | ----- | ------- |
| 1   | `webView`   | WebView     | HTML 表格，缩放支持         | Y     |         |
| 2   | `progress`  | ProgressBar | 加载中                  | Y     |         |
| 3   | `errorText` | TextView    | "Failed to load CSV" | Y     |         |
| 4   | CSV 解析      | —           | 支持引号转义               | Y     |         |
| 5   | 异步加载        | —           | Dispatchers.Default  | Y     |         |

### 页面：EbookViewerFragment（`ebook_viewer_fragment.xml` / `ebook_viewer.xml`）

| 序号  | 元素             | 类型       | 内容 / 描述                                | 是否保留？ | 删除后修改方案 |
| --- | -------------- | -------- | -------------------------------------- | ----- | ------- |
| 1   | `action_share` | MenuItem | Share                                  | Y     |         |
| 2   | `webView`      | WebView  | EPUB/MOBI 渲染                           | Y     |         |
| 3   | `errorText`    | TextView | "Failed to open ebook: Invalid format" | Y     |         |
| 4   | EPUB 解析        | —        | EpubParser 解 ZIP                       | Y     |         |
| 5   | MOBI 解析        | —        | MobiParser 二进制                         | Y     |         |
| 6   | 图片缓存           | —        | 临时目录 imageDir，onDestroy 清理             | Y     |         |

---

## 分组 4：工具页面

### 页面：ToolGroupFragment（`tool_group_fragment.xml` / `tool_card_item.xml`）

| 序号  | 元素                   | 类型               | 内容 / 描述                             | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ---------------- | ----------------------------------- | ----- | ------- |
| 1   | `cardView`           | MaterialCardView | 工具卡片，点击启动对应 Activity                | Y     |         |
| 2   | `featureTitle`       | TextView         | 工具名称（动态），npzCardTitleTextAppearance | Y     |         |
| 3   | `featureDescription` | TextView         | 工具描述，npzCardBodyTextAppearance      | Y     |         |
| 4   | 工具项加载                | —                | `page.createToolItems()`            | Y     |         |

### 页面：AppManagerFragment（`app_manager_fragment.xml` / `app_manager.xml`）

| 序号  | 元素                    | 类型                   | 内容 / 描述                                 | 是否保留？ | 删除后修改方案    |
| --- | --------------------- | -------------------- | --------------------------------------- | ----- | ---------- |
| 1   | `action_sort_name`    | MenuItem             | 按名称排序                                   | N     | 删除APP管理的功能 |
| 2   | `action_sort_size`    | MenuItem             | 按大小排序                                   | N     |            |
| 3   | `action_sort_package` | MenuItem             | 按包名排序                                   | N     |            |
| 4   | `action_show_system`  | MenuItem (Checkable) | 显示系统应用                                  | N     |            |
| 5   | `action_show_user`    | MenuItem (Checkable) | 显示用户应用                                  | N     |            |
| 6   | `appCountText`        | TextView             | "应用数: X"，textAppearanceBodyMedium       | N     |            |
| 7   | 加载逻辑                  | —                    | PackageManager.getInstalledApplications | N     |            |
| 8   | 长按菜单                  | —                    | 打开 / 卸载 / 清除数据                          | N     |            |

### 页面：DuplicateFinderFragment（`duplicate_finder_fragment.xml` / `duplicate_finder.xml` / `duplicate_group_header.xml`）

| 序号  | 元素                              | 类型             | 内容 / 描述                                     | 是否保留？ | 删除后修改方案 |
| --- | ------------------------------- | -------------- | ------------------------------------------- | ----- | ------- |
| 1   | `addPathButton`                 | MaterialButton | 添加路径                                        | Y     |         |
| 2   | `chipTypeAll/Image/Video/Audio` | Chip           | 类型过滤                                        | Y     |         |
| 3   | `action_keep_rule`              | MenuItem       | 保留规则                                        | Y     |         |
| 4   | `action_delete_selected`        | MenuItem       | 删除选中                                        | Y     |         |
| 5   | `headerTitle`                   | TextView       | "组 X (Y 个文件)"，textAppearanceTitleSmall      | Y     |         |
| 6   | `headerDescription`             | TextView       | "大小:xxx · Hash:xxx"，textAppearanceBodySmall | Y     |         |
| 7   | Hash 算法                         | —              | MD5/SHA-1/SHA-256/CRC32                     | Y     |         |
| 8   | 保留规则                            | —              | 最长/最短路径/最新/最旧                               | Y     |         |

### 页面：EmptySearchFragment（`empty_search_fragment.xml` / `empty_search.xml`）

| 序号  | 元素                         | 类型             | 内容 / 描述            | 是否保留？ | 删除后修改方案 |
| --- | -------------------------- | -------------- | ------------------ | ----- | ------- |
| 1   | `addPathButton`            | MaterialButton | 添加路径               | Y     |         |
| 2   | `chipEmptyFiles`           | Chip           | 包含空文件              | Y     |         |
| 3   | `chipEmptyFolders`         | Chip           | 包含空文件夹             | Y     |         |
| 4   | `includeHiddenDirsSwitch`  | MaterialSwitch | 包含隐藏目录             | Y     |         |
| 5   | `includeHiddenFilesSwitch` | MaterialSwitch | 包含隐藏文件             | Y     |         |
| 6   | `followSymlinksSwitch`     | MaterialSwitch | 跟随符号链接             | Y     |         |
| 7   | `searchButton`             | MaterialButton | 开始搜索               | Y     |         |
| 8   | 递归遍历                       | —              | newDirectoryStream | Y     |         |

### 页面：EncryptionFragment（`encryption_fragment.xml`）

| 序号  | 元素                     | 类型                | 内容 / 描述            | 是否保留？ | 删除后修改方案 |
| --- | ---------------------- | ----------------- | ------------------ | ----- | ------- |
| 1   | `addFileButton`        | MaterialButton    | 添加文件               | Y     |         |
| 2   | `encryptButton`        | MaterialButton    | 加密                 | Y     |         |
| 3   | `decryptButton`        | MaterialButton    | 解密                 | Y     |         |
| 4   | `deleteOriginalSwitch` | MaterialSwitch    | 加密后删除原文件           | Y     |         |
| 5   | `passwordInput`        | TextInputEditText | hint="输入密码"        | Y     |         |
| 6   | `fileCountText`        | TextView          | "已添加 X 个文件"        | Y     |         |
| 7   | 加密执行                   | —                 | AES-256，生成 .enc 后缀 | Y     |         |

### 页面：FileCompareFragment（`file_compare_fragment.xml`）

| 序号  | 元素                    | 类型             | 内容 / 描述                       | 是否保留？ | 删除后修改方案 |
| --- | --------------------- | -------------- | ----------------------------- | ----- | ------- |
| 1   | `selectFile1Button`   | MaterialButton | 选择文件1                         | Y     |         |
| 2   | `selectFile2Button`   | MaterialButton | 选择文件2                         | Y     |         |
| 3   | `compareButton`       | MaterialButton | 比较                            | Y     |         |
| 4   | `file1Text/file2Text` | TextView       | 已选路径，textAppearanceBodyMedium | Y     |         |
| 5   | `resultText`          | TextView       | 对比结果（Hex/文本）                  | Y     |         |
| 6   | 字节对比                  | —              | 后台线程读取                        | Y     |         |

### 页面：FileSearchFragment（`file_search_fragment.xml` / `file_search.xml`）

| 序号  | 元素                                       | 类型                | 内容 / 描述          | 是否保留？ | 删除后修改方案 |
| --- | ---------------------------------------- | ----------------- | ---------------- | ----- | ------- |
| 1   | `browseButton`                           | MaterialButton    | 浏览               | Y     |         |
| 2   | `nameInput`                              | TextInputEditText | hint="文件名（包含）"   | Y     |         |
| 3   | `extensionInput`                         | TextInputEditText | hint="扩展名（逗号分隔）" | Y     |         |
| 4   | `chipTypeAll/Image/Video/Audio/Document` | Chip              | 类型过滤             | Y     |         |
| 5   | `searchButton`                           | MaterialButton    | 搜索               | Y     |         |
| 6   | `includeHiddenDirsSwitch`                | MaterialSwitch    | 包含隐藏目录           | Y     |         |
| 7   | `includeHiddenFilesSwitch`               | MaterialSwitch    | 包含隐藏文件           | Y     |         |
| 8   | 多条件过滤                                    | —                 | 文件名/扩展名/大小/类型    | Y     |         |

### 页面：FormatConvertFragment（`format_convert_fragment.xml` / `format_convert_selection.xml` / `format_convert_item.xml`）

| 序号  | 元素                                 | 类型             | 内容 / 描述                               | 是否保留？ | 删除后修改方案 |
| --- | ---------------------------------- | -------------- | ------------------------------------- | ----- | ------- |
| 1   | `addFileButton`                    | MaterialButton | 添加文件                                  | Y     |         |
| 2   | `convertButton`                    | MaterialButton | 转换                                    | Y     |         |
| 3   | `batchFormatButton`                | MaterialButton | 批量设置格式                                | Y     |         |
| 4   | `removeSelectedButton`             | MaterialButton | 移除选中                                  | Y     |         |
| 5   | `nameText/descriptionText`         | TextView       | 文件项 textAppearanceListItem(Secondary) | Y     |         |
| 6   | `inputFormatChip/outputFormatChip` | TextView       | "[MP4]"/"[AVI]"                       | Y     |         |
| 7   | FFmpeg 转换                          | —              | FFmpegJni.getMediaInfo()              | Y     |         |

### 页面：ImageCompressFragment（`image_compress_fragment.xml`）

| 序号  | 元素                                       | 类型             | 内容 / 描述         | 是否保留？ | 删除后修改方案 |
| --- | ---------------------------------------- | -------------- | --------------- | ----- | ------- |
| 1   | `qualitySlider`                          | Slider         | 质量 10-100%      | Y     |         |
| 2   | `maxDimensionSlider`                     | Slider         | 最大尺寸 320-4000px | Y     |         |
| 3   | `chipJpeg/chipPng/chipWebp`              | Chip           | 输出格式            | Y     |         |
| 4   | `addFileButton`                          | MaterialButton | 添加文件            | Y     |         |
| 5   | `compressButton`                         | MaterialButton | 压缩              | Y     |         |
| 6   | `qualityValueText/maxDimensionValueText` | TextView       | "80%"/"1920px"  | Y     |         |

### 页面：MediaToolsFragment（`media_tools_fragment.xml` / `media_tool_card_item.xml`）

| 序号  | 元素                    | 类型               | 内容 / 描述                 | 是否保留？ | 删除后修改方案 |
| --- | --------------------- | ---------------- | ----------------------- | ----- | ------- |
| 1   | `cardView`            | MaterialCardView | 工具卡片（提取音频/剪切/压缩等）       | Y     |         |
| 2   | `ffmpegStatusDot`     | View             | FFmpeg 可用状态             | Y     |         |
| 3   | `ffmpegVersionText`   | TextView         | "FFmpeg vX.X.X" 或 "不可用" | Y     |         |
| 4   | `operationStatusText` | TextView         | "处理中: 50%"              | Y     |         |
| 5   | 提取音频 / 剪切 / 压缩 / GIF  | —                | FFmpeg 调用               | Y     |         |

### 页面：RecentFilesFragment（`recent_files_fragment.xml`）

| 序号  | 元素                                | 类型             | 内容 / 描述         | 是否保留？ | 删除后修改方案 |
| --- | --------------------------------- | -------------- | --------------- | ----- | ------- |
| 1   | `addPathButton`                   | MaterialButton | 添加路径            | Y     |         |
| 2   | `chip_1hour/24hours/7days/30days` | Chip           | 时间范围            | Y     |         |
| 3   | `includeHiddenSwitch`             | MaterialSwitch | 包含隐藏文件          | Y     |         |
| 4   | `searchButton`                    | MaterialButton | 搜索              | Y     |         |
| 5   | `resultCountText`                 | TextView       | "找到 X 个文件"      | Y     |         |
| 6   | 按修改时间过滤+降序                        | —              | lastModified 比较 | Y     |         |

### 页面：StorageAnalysisFragment（`storage_analysis_fragment.xml` / `storage_category_item.xml`）

| 序号  | 元素                                                 | 类型                      | 内容 / 描述    | 是否保留？ | 删除后修改方案  |
| --- | -------------------------------------------------- | ----------------------- | ---------- | ----- | -------- |
| 1   | `addPathButton`                                    | MaterialButton          | 添加路径       | N     | 删除存储分析功能 |
| 2   | `chipStorageOverview/FileType/LargeFiles/OldFiles` | Chip                    | 分析模式       | N     |          |
| 3   | `analyzeButton`                                    | MaterialButton          | 开始分析       | N     |          |
| 4   | `categoryName/SizeText/InfoText`                   | TextView                | 类别名/大小/数量  | N     |          |
| 5   | `categoryProgress`                                 | LinearProgressIndicator | 进度条        | N     |          |
| 6   | 分类统计                                               | —                       | 按扩展名分类，可视化 | N     |          |

### 页面：TrashFragment（`trash_fragment.xml` / `trash.xml`）

| 序号  | 元素                        | 类型       | 内容 / 描述            | 是否保留？ | 删除后修改方案 |
| --- | ------------------------- | -------- | ------------------ | ----- | ------- |
| 1   | `emptyTrashFab`           | FAB      | 永久清空回收站            | Y     |         |
| 2   | `action_select_all`       | MenuItem | 全选/取消              | Y     |         |
| 3   | `action_restore_selected` | MenuItem | 恢复选中               | Y     |         |
| 4   | `action_delete_selected`  | MenuItem | 永久删除选中             | Y     |         |
| 5   | 空提示                       | TextView | "回收站为空"            | Y     |         |
| 6   | 项目副标题                     | TextView | "文件·2.5MB·删除于:..." | Y     |         |
| 7   | 恢复操作                      | —        | originalPath 移回    | Y     |         |

---

## 分组 5：设置与存储

### 页面：SettingsActivity / SettingsFragment（`settings_fragment.xml`）

| 序号  | 元素           | 类型              | 内容 / 描述                       | 是否保留？ | 删除后修改方案 |
| --- | ------------ | --------------- | ----------------------------- | ----- | ------- |
| 1   | `toolbar`    | MaterialToolbar | 返回                            | Y     |         |
| 2   | onCreate 初始化 | —               | 添加 SettingsFragment           | Y     |         |
| 3   | 语言变更         | —               | setApplicationLocalesPre33 重启 | Y     |         |
| 4   | 夜间/字体监听      | —               | 重建 Activity                   | Y     |         |

### 页面：SettingsPreferenceFragment（`res/xml/settings.xml`）

#### Interface 分组

| 序号  | Preference Key                 | 类型                     | 标题                     | 是否保留？ | 删除后修改方案 |
| --- | ------------------------------ | ---------------------- | ---------------------- | ----- | ------- |
| 1   | `pref_key_locale`              | LocalePreference       | Language               | Y     |         |
| 2   | `pref_key_night_mode`          | SimpleMenuPreference   | Night mode             | N     |         |
| 3   | `pref_key_black_night_mode`    | SwitchPreferenceCompat | Black night mode       | N     |         |
| 4   | `pref_key_font_scale`          | SimpleMenuPreference   | Font size              | Y     |         |
| 5   | `pref_key_file_list_animation` | SwitchPreferenceCompat | File list animation    | Y     |         |
| 6   | `pref_key_file_name_ellipsize` | SimpleMenuPreference   | Display long file name | Y     |         |

#### Behavior 分组

| 序号  | Preference Key                         | 类型                            | 标题                                  | 是否保留？ | 删除后修改方案 |
| --- | -------------------------------------- | ----------------------------- | ----------------------------------- | ----- | ------- |
| 7   | `pref_key_file_list_default_directory` | DefaultDirectoryPreference    | Default folder                      | Y     |         |
| 8   | (无 key)                                | StoragesPreference            | Storage                             | Y     |         |
| 9   | (无 key)                                | StandardDirectoriesPreference | Standard folders                    | Y     |         |
| 10  | (无 key)                                | BookmarkDirectoriesPreference | Bookmark folders                    | Y     |         |
| 11  | `pref_key_root_strategy`               | RootStrategyPreference        | Root access mode                    | Y     |         |
| 12  | `pref_key_archive_file_name_encoding`  | CharsetPreference             | Archive file name encoding          | Y     |         |
| 13  | `pref_key_open_apk_default_action`     | SimpleMenuPreference          | Open Android package                | Y     |         |
| 14  | `pref_key_show_pdf_thumbnail_pre_28`   | SwitchPreferenceCompat        | Show thumbnail for PDF (Android<28) | Y     |         |

### 页面：StandardDirectoryListFragment（`standard_directory_list_fragment.xml`）

仅 Toolbar 容器，内容由 StandardDirectoryListPreferenceFragment 提供。
| 序号 | 元素 | 类型 | 内容 | 是否保留？ | 删除后修改方案 |
|---|---|---|---|---|---|
| 1 | `toolbar` | MaterialToolbar | "Standard folders" |  |  |

### 页面：BookmarkDirectoryListFragment（`bookmark_directory_list_fragment.xml` / `bookmark_directory_item.xml`）

| 序号  | 元素               | 类型              | 内容 / 描述                                      | 是否保留？ | 删除后修改方案 |
| --- | ---------------- | --------------- | -------------------------------------------- | ----- | ------- |
| 1   | `toolbar`        | MaterialToolbar | Bookmark folders                             | Y     |         |
| 2   | `fab`            | FAB             | 添加书签目录（文件选择器）                                | Y     |         |
| 3   | `dragHandleView` | ImageView       | 长按拖拽重排                                       | Y     |         |
| 4   | `emptyView`      | TextView        | "No bookmark folders"，textAppearanceListItem | Y     |         |
| 5   | `nameText`       | TextView        | 名称，textAppearanceListItem                    | Y     |         |
| 6   | `pathText`       | TextView        | 路径，textAppearanceListItemSecondary           | Y     |         |
| 7   | Item 点击          | —               | EditBookmarkDirectoryDialogFragment          | Y     |         |

### 页面：StorageListFragment（`storage_list_fragment.xml` / `storage_item.xml`）

| 序号  | 元素                | 类型              | 内容 / 描述                            | 是否保留？ | 删除后修改方案 |
| --- | ----------------- | --------------- | ---------------------------------- | ----- | ------- |
| 1   | `toolbar`         | MaterialToolbar | Storage                            | Y     |         |
| 2   | `fab`             | FAB             | AddStorageDialogFragment           | Y     |         |
| 3   | `dragHandleView`  | ImageView       | 拖拽重排                               | Y     |         |
| 4   | `emptyView`       | TextView        | "No storage"                       | Y     |         |
| 5   | `nameText`        | TextView        | 存储名 (@color/storage_name)          | Y     |         |
| 6   | `descriptionText` | TextView        | 描述 textAppearanceListItemSecondary | Y     |         |

### 页面：AddStorageDialogFragment（菜单对话框）

| 序号  | 项     | 显示文字             | 点击逻辑                                    | 是否保留？ | 删除后修改方案 |
| --- | ----- | ---------------- | --------------------------------------- | ----- | ------- |
| 1   | 标题    | Add storage      |                                         | Y     |         |
| 2   | item1 | Android/data     | AddExternalStorageShortcutFragment（11+） | N     |         |
| 3   | item2 | Android/obb      | AddExternalStorageShortcutFragment（11+） | N     |         |
| 4   | item3 | External storage | AddDocumentTreeActivity                 | Y     |         |

### 页面：EditBookmarkDirectoryDialogFragment（`edit_bookmark_directory_dialog.xml`）

| 序号  | 元素         | 类型                | 内容                                   | 是否保留？ | 删除后修改方案 |
| --- | ---------- | ----------------- | ------------------------------------ | ----- | ------- |
| 1   | 标题         | —                 | "Bookmark folder"                    | Y     |         |
| 2   | `nameEdit` | TextInputEditText | hint=Name                            | Y     |         |
| 3   | `pathText` | ReadOnly EditText | hint=Path，点击改路径                      | Y     |         |
| 4   | OK         | Button            | save() → BookmarkDirectories.replace | Y     |         |
| 5   | Remove     | Button            | BookmarkDirectories.remove           | Y     |         |
| 6   | Cancel     | Button            | 取消                                   | Y     |         |

### 页面：EditDeviceStorageDialogFragment（`edit_device_storage_dialog.xml`）

| 序号  | 元素             | 类型                | 内容                  | 是否保留？ | 删除后修改方案 |
| --- | -------------- | ----------------- | ------------------- | ----- | ------- |
| 1   | 标题             | —                 | Edit device storage | Y     |         |
| 2   | `nameEdit`     | TextInputEditText | hint=Name           | Y     |         |
| 3   | `pathText`     | ReadOnly EditText | hint=Path，可复制       | Y     |         |
| 4   | OK             | Button            | save()              | Y     |         |
| 5   | Show/Hide (中性) | Button            | toggleVisibility    | Y     |         |
| 6   | Cancel         | Button            | 取消                  | Y     |         |

### 页面：EditDocumentTreeDialogFragment（`edit_document_tree_dialog.xml`）

| 序号  | 元素                   | 类型                | 内容                    | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ----------------- | --------------------- | ----- | ------- |
| 1   | 标题                   | —                 | Edit external storage | Y     |         |
| 2   | `nameEdit`           | TextInputEditText | hint=Name             | Y     |         |
| 3   | `uriText`            | ReadOnly EditText | hint=URI              | Y     |         |
| 4   | `pathText`           | ReadOnly EditText | hint=Path（可选）         | Y     |         |
| 5   | OK / Remove / Cancel | Button            | 保存 / 删除 / 取消          | Y     |         |

### 页面：EditExternalStorageShortcutDialogFragment（`edit_external_storage_shortcut_dialog.xml`）

| 序号  | 元素                   | 类型                | 内容                             | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ----------------- | ------------------------------ | ----- | ------- |
| 1   | 标题                   | —                 | Edit external storage shortcut | Y     |         |
| 2   | `nameEdit`           | TextInputEditText | hint=Name                      | Y     |         |
| 3   | `rootIdEdit`         | TextInputEditText | hint=Storage volume（必填）        | Y     |         |
| 4   | `pathEdit`           | TextInputEditText | hint=Path（可选）                  | Y     |         |
| 5   | OK / Remove / Cancel | Button            | 校验保存 / 删除 / 取消                 | Y     |         |

### 页面：AddDocumentTreeFragment（无布局）

| 序号  | 模块                  | 描述                                                | 是否保留？ | 删除后修改方案 |
| --- | ------------------- | ------------------------------------------------- | ----- | ------- |
| 1   | OpenDocumentTree 启动 | onActivityCreated 启动选择器                           | Y     |         |
| 2   | 处理回调                | takePersistablePermission + Storages.addOrReplace | Y     |         |

### 页面：AddExternalStorageShortcutFragment（无布局）

| 序号  | 模块              | 描述                            | 是否保留？ | 删除后修改方案 |
| --- | --------------- | ----------------------------- | ----- | ------- |
| 1   | Documents UI 检查 | 不可用则 toast activity_not_found | Y     |         |
| 2   | 添加快捷方式          | Storages.addOrReplace         | Y     |         |

### 页面：ColorPreferenceDialogFragment（`color_picker_dialog.xml`）

| 序号  | 元素           | 类型       | 内容 / 描述           | 是否保留？ | 删除后修改方案 |
| --- | ------------ | -------- | ----------------- | ----- | ------- |
| 1   | `palette`    | GridView | 颜色网格              | N     |         |
| 2   | OK / Cancel  | Button   | 保存 / 取消           | N     |         |
| 3   | Default (中性) | Button   | 重置为默认色（仅默认色在调色板内） | N     |         |

---

## 分组 6：文件属性与权限对话框

### 页面：FilePropertiesDialogFragment（`file_properties_dialog.xml`）

| 序号  | 元素              | 类型        | 内容 / 描述                     | 是否保留？ | 删除后修改方案 |
| --- | --------------- | --------- | --------------------------- | ----- | ------- |
| 1   | `tabLayout`     | TabLayout | scrollable 模式               | Y     |         |
| 2   | `viewPager`     | ViewPager | 7 个 Tab                     | Y     |         |
| 3   | OK              | Button    | 关闭                          | Y     |         |
| 4   | 动态 Tab 加载       | —         | `isAvailable()` 决定显示        | Y     |         |
| 5   | SharedViewModel | —         | FilePropertiesFileViewModel | Y     |         |

### Tab：Basic（基本信息）

| 序号  | 字段                   | 显示文字             | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ---------------- | ----- | ------- |
| 1   | Name                 | 文件名（EditText 只读） | Y     |         |
| 2   | Type                 | 类型               | Y     |         |
| 3   | Archive File / Entry | 档案文件 / 条目（条件）    | Y     |         |
| 4   | Parent Folder        | 父目录              | Y     |         |
| 5   | Link Target          | 软链接目标（条件）        | Y     |         |
| 6   | Size                 | 大小               | Y     |         |
| 7   | Contents             | 内容（目录）           | Y     |         |
| 8   | Last Modified        | 最后修改时间           | Y     |         |
| 9   | Free Space           | 剩余空间（条件）         | Y     |         |
| 10  | 目录递归遍历               | 500ms 定时更新       | Y     |         |

### Tab：Permission（权限）

| 序号  | 字段 / 元素         | 显示文字                   | 点击逻辑                       | 是否保留？ | 删除后修改方案 |
| --- | --------------- | ---------------------- | -------------------------- | ----- | ------- |
| 1   | Owner           | 所有者                    | 打开 SetOwnerDialog          | Y     |         |
| 2   | Group           | 组                      | 打开 SetGroupDialog          | Y     |         |
| 3   | Mode            | 权限位 "rwxr-xr-x (0755)" | 打开 SetModeDialog           | Y     |         |
| 4   | SELinux Context | SELinux 上下文            | 打开 SetSeLinuxContextDialog | Y     |         |

### Tab：Image / Audio / Video / APK / Checksum

| 序号  | Tab      | 关键能力                                      | 是否保留？ | 删除后修改方案 |
| --- | -------- | ----------------------------------------- | ----- | ------- |
| 1   | Image    | EXIF 解析、Geocoder 反向地理编码                   | Y     |         |
| 2   | Audio    | MediaMetadataRetriever：标题/艺术家/专辑/时长/比特率   | Y     |         |
| 3   | Video    | 分辨率/时长/拍摄时间/GPS                           | Y     |         |
| 4   | APK      | PackageParser：包名/版本/SDK/权限/签名，权限对话框       | Y     |         |
| 5   | Checksum | CRC32/MD5/SHA-1/SHA-256/SHA-512 + Compare | Y     |         |

### 页面：SetModeDialogFragment（`set_mode_dialog.xml` / `mode_bit_item.xml`）

| 序号  | 元素                     | 类型                | 内容 / 描述                                      | 是否保留？ | 删除后修改方案 |
| --- | ---------------------- | ----------------- | -------------------------------------------- | ----- | ------- |
| 1   | `ownerText/Dropdown`   | EditText+DropDown | Owner 权限位                                    | Y     |         |
| 2   | `groupText/Dropdown`   | EditText+DropDown | Group 权限位                                    | Y     |         |
| 3   | `othersText/Dropdown`  | EditText+DropDown | Others 权限位                                   | Y     |         |
| 4   | `specialText/Dropdown` | EditText+DropDown | Special (SUID/SGID/Sticky)                   | Y     |         |
| 5   | `recursiveCheck`       | MaterialCheckBox  | "Apply to enclosed files"（仅目录）               | Y     |         |
| 6   | `uppercaseXCheck`      | MaterialCheckBox  | "Don't add Execute for enclosed files"（递归启用） | Y     |         |
| 7   | OK / Cancel            | Button            | FileJobService.setMode / 取消                  | Y     |         |

### 页面：SetPrincipalDialogFragment（`set_principal_dialog.xml` / `principal_item.xml`）

SetOwnerDialogFragment / SetGroupDialogFragment 共用。
| 序号 | 元素 | 类型 | 内容 / 描述 | 是否保留？ | 删除后修改方案 |
|---|---|---|---|---|---|
| 1 | `filterEdit` | TextInputEditText | hint="Enter a name or ID" | Y |  |
| 2 | `progress` | ProgressBar | Loading |Y  |  |
| 3 | `errorText` | TextView | 错误 | Y |  |
| 4 | `emptyView` | TextView | "Empty" | Y |  |
| 5 | `recyclerView` | RecyclerView | 用户/组列表 | Y |  |
| 6 | `recursiveCheck` | CheckBox | "Apply to enclosed files"（仅目录） |Y  |  |
| 7 | OK | Button | FileJobService.setOwner/setGroup | Y |  |
| 8 | Cancel | Button | 取消 | Y |  |
| 9 | item `principalText` | TextView | "name (id)" textAppearanceListItemSecondary |Y  |  |
| 10 | item `labelText` | AutoGoneTextView | 系统标签 |Y  |  |
| 11 | item `radio` | MaterialRadioButton | 单选指示 |Y  |  |

### 页面：SetSeLinuxContextDialogFragment（`set_selinux_context_dialog.xml`）

| 序号  | 元素                   | 类型                | 内容 / 描述               | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ----------------- | --------------------- | ----- | ------- |
| 1   | `seLinuxContextEdit` | TextInputEditText | 上下文输入                 | Y     |         |
| 2   | `recursiveCheck`     | CheckBox          | 递归（仅目录）               | Y     |         |
| 3   | OK                   | Button            | setSeLinuxContext     | Y     |         |
| 4   | Restore (中性)         | Button            | restoreSeLinuxContext | Y     |         |
| 5   | Cancel               | Button            | 取消                    | Y     |         |

### 页面：PermissionListDialogFragment（`permission_list_dialog.xml` / `permission_item.xml`）

| 序号  | 元素                     | 类型               | 内容 / 描述                              | 是否保留？ | 删除后修改方案 |
| --- | ---------------------- | ---------------- | ------------------------------------ | ----- | ------- |
| 1   | `recyclerView`         | RecyclerView     | APK 权限列表                             | Y     |         |
| 2   | item `labelText`       | TextView         | 分类标签 textAppearanceListItemSecondary | Y     |         |
| 3   | item `nameText`        | TextView         | 权限名                                  | Y     |         |
| 4   | item `descriptionText` | AutoGoneTextView | 描述（条件）                               | Y     |         |

---

## 分组 7：系统对话框

### 页面：FileJobErrorDialogFragment（`file_job_error_dialog_view.xml`）

| 序号  | 元素                        | 类型       | 内容 / 描述                                         | 是否保留？ | 删除后修改方案 |
| --- | ------------------------- | -------- | ----------------------------------------------- | ----- | ------- |
| 1   | Positive/Negative/Neutral | Button   | 文字由 args 动态指定，回调 `FileJobErrorAction`           | Y     |         |
| 2   | `remountButton`           | Button   | "Remount X as read-write" / Remounting / 已完成 三态 | Y     |         |
| 3   | `allCheck`                | CheckBox | "Apply this action to all files"                | Y     |         |
| 4   | Title / Message           | TextView | args.title / args.message                       | Y     |         |
| 5   | 挂载状态监听                    | —        | viewModel.remountState                          | Y     |         |

### 页面：FileJobConflictDialogFragment（`file_job_conflict_dialog_view.xml`）

| 序号  | 元素                               | 类型                | 内容 / 描述                  | 是否保留？ | 删除后修改方案 |
| --- | -------------------------------- | ----------------- | ------------------------ | ----- | ------- |
| 1   | Positive                         | Button            | Merge/Replace/Rename（动态） | Y     |         |
| 2   | Negative                         | Button            | Skip                     | Y     |         |
| 3   | Neutral                          | Button            | Cancel                   | Y     |         |
| 4   | `targetNameText/DescriptionText` | TextView          | 原文件信息（修改时间｜大小）           | Y     |         |
| 5   | `sourceNameText/DescriptionText` | TextView          | 新文件信息                    | Y     |         |
| 6   | `showNameLayout`                 | Button            | 展开"Select a new name…"区域 | Y     |         |
| 7   | `nameEdit`                       | TextInputEditText | hint="New name"          | Y     |         |
| 8   | `nameLayout`.endIcon             | —                 | 重置为原名                    | Y     |         |
| 9   | `allCheck`                       | CheckBox          | 应用所有；输入新名时禁用             | Y     |         |

### 页面：ConfirmReplaceFileDialogFragment（无布局）

| 序号  | 元素      | 内容 / 描述                      | 是否保留？ | 删除后修改方案 |
| --- | ------- | ---------------------------- | ----- | ------- |
| 1   | Message | `Replace "%s"?`              | Y     |         |
| 2   | OK      | `listener.replaceFile(file)` | Y     |         |
| 3   | Cancel  | 关闭                           | Y     |         |

### 页面：ConfirmReloadDialogFragment（无布局）

| 序号  | 元素       | 内容 / 描述                                             | 是否保留？ | 删除后修改方案 |
| --- | -------- | --------------------------------------------------- | ----- | ------- |
| 1   | Message  | "Are you sure you want to reload? Unsaved changes…" | Y     |         |
| 2   | Positive | "Keep editing"                                      | Y     |         |
| 3   | Negative | "Reload" → `listener.reload()`                      | Y     |         |

### 页面：ConfirmCloseDialogFragment（无布局）

| 序号  | 元素       | 内容 / 描述                                             | 是否保留？ | 删除后修改方案 |
| --- | -------- | --------------------------------------------------- | ----- | ------- |
| 1   | Message  | "Are you sure you want to discard unsaved changes…" | Y     |         |
| 2   | Positive | "Keep editing"                                      | Y     |         |
| 3   | Negative | "Discard" → `listener.finish()`                     | Y     |         |

### 页面：ConfirmDeleteDialogFragment（图像）/ ConfirmDeleteAudioDialogFragment / ConfirmDeleteVideoDialogFragment

| 序号  | 元素      | 内容 / 描述                      | 是否保留？ | 删除后修改方案 |
| --- | ------- | ---------------------------- | ----- | ------- |
| 1   | Message | `Delete "%s"?`               | Y     |         |
| 2   | OK      | `listener.delete(args.path)` | Y     |         |
| 3   | Cancel  | 关闭                           | Y     |         |

### 页面：LicensesDialogFragment

| 序号  | 元素    | 内容 / 描述                                      | 是否保留？ | 删除后修改方案 |
| --- | ----- | -------------------------------------------- | ----- | ------- |
| 1   | Title | "Licenses"                                   | Y     |         |
| 2   | 内容    | `R.raw.licenses` XML + LicensesDialog NOTICE | Y     |         |
| 3   | Close | 关闭                                           | Y     |         |

### 权限 Rationale 对话框（5 个）

| 序号  | 对话框                                                  | Message                                                          | 按钮                     | 是否保留？ | 删除后修改方案 |
| --- | ---------------------------------------------------- | ---------------------------------------------------------------- | ---------------------- | ----- | ------- |
| 1   | ShowRequestStoragePermissionRationale                | "App needs permission to access files. Please click ALLOW…"      | OK / Cancel            | Y     |         |
| 2   | ShowRequestStoragePermissionInSettingsRationale      | "App needs permission… Please grant Storage in system settings." | Open settings / Cancel | Y     |         |
| 3   | ShowRequestNotificationPermissionRationale           | "App needs permission to post notifications…"                    | OK / Cancel            | Y     |         |
| 4   | ShowRequestNotificationPermissionInSettingsRationale | "Please grant Notification in system settings."                  | Open settings / Cancel | Y     |         |
| 5   | ShowRequestAllFilesAccessRationale                   | "App needs access to manage all files…"                          | OK / Cancel            | Y     |         |

---

## 分组 8：终端

### 页面：TerminalActivity / TerminalFragment（`terminal_fragment.xml` / `terminal.xml`）

| 序号  | 元素                   | 类型          | 内容 / 描述                                      | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ----------- | -------------------------------------------- | ----- | ------- |
| 1   | `sendButton`         | ImageButton | 发送命令，executeCurrentInput                     | N     | 删除终端功能  |
| 2   | `action_interrupt`   | MenuItem    | "Interrupt (Ctrl+C)"，写入 ETX(0x03)            | N     |         |
| 3   | `action_clear`       | MenuItem    | "Clear screen"，clearScreen                   | N     |         |
| 4   | `action_restart`     | MenuItem    | "Restart shell"                              | N     |         |
| 5   | `action_toggle_root` | MenuItem    | "Root shell"（需 su 可用）                        | N     |         |
| 6   | `promptText`         | TextView    | "$" / "#"，14sp                               | N     |         |
| 7   | `commandInput`       | EditText    | 14sp，textNoSuggestions，hint="Enter command…" | N     |         |
| 8   | `outputText`         | TextView    | 13sp，monospace                               | N     |         |
| 9   | Shell 启动             | —           | ProcessBuilder + sh/su，env: TERM/HOME/PS1    | N     |         |
| 10  | 输出读取                 | —           | 后台线程 InputStreamReader                       | N     |         |
| 11  | ANSI 过滤              | —           | stripAnsiCodes() 正则                          | N     |         |
| 12  | 命令历史                 | —           | DPAD_UP/DOWN 导航                              | N     |         |
| 13  | Root 切换              | —           | isSuAvailable + 重启 shell                     | N     |         |

---

## 分组 9：关于页面

### 页面：AboutFragment（`about_fragment.xml`）

#### 卡片 1：应用信息

| 序号  | 元素                    | 类型                     | 显示内容                           | 字体 / 样式                                             | 点击逻辑                                  | 是否保留？ | 删除后修改方案 |
| --- | --------------------- | ---------------------- | ------------------------------ | --------------------------------------------------- | ------------------------------------- | ----- | ------- |
| 1   | (icon)                | ImageView              | launcher_icon                  | —                                                   | —                                     | Y     |         |
| 2   | (app name)            | TextView               | "Material Files"               | TextAppearance.MaterialFiles.Material3.AboutAppName | —                                     | Y     |         |
| 3   | Version 标题            | TextView               | "Version"                      | AboutListItem                                       | —                                     | Y     |         |
| 4   | Version 值             | TextView               | "1.0.0 (1)"                    | textAppearanceLabelSmall                            | —                                     | Y     |         |
| 5   | `gitHubLayout`        | ForegroundLinearLayout | "View on GitHub"               | AboutListItem                                       | （已注释移除）                               | Y     |         |
| 6   | `licensesLayout`      | ForegroundLinearLayout | "Licenses"                     | AboutListItem                                       | LicensesDialogFragment.show()         | Y     |         |
| 7   | `privacyPolicyLayout` | ForegroundLinearLayout | "Privacy policy" (NONFREE 才显示) | AboutListItem                                       | startActivitySafe(PRIVACY_POLICY_URI) | Y     |         |

#### 卡片 2：作者信息

| 序号  | 元素                   | 类型                     | 显示内容               | 字体 / 样式                 | 点击逻辑    | 是否保留？ | 删除后修改方案 |
| --- | -------------------- | ---------------------- | ------------------ | ----------------------- | ------- | ----- | ------- |
| 8   | "Author" 标题          | TextView               | "Author"           | textAppearanceBodySmall | —       | Y     |         |
| 9   | `authorNameLayout`   | ForegroundLinearLayout | "naipingzai"       | AboutListItem           | （已注释移除） | Y     |         |
| 10  | `authorGitHubLayout` | ForegroundLinearLayout | "Follow on GitHub" | AboutListItem           | （已注释移除） | Y     |         |

#### 卡片 3：FFmpeg 信息

| 序号  | 元素                       | 类型       | 显示内容                                                                                                                                        | 字体 / 样式                  | 是否保留？ | 删除后修改方案 |
| --- | ------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------ | ----- | ------- |
| 11  | "FFmpeg" 标题              | TextView | "FFmpeg"                                                                                                                                    | textAppearanceBodySmall  | Y     |         |
| 12  | "FFmpeg version" 行       | TextView | "FFmpeg version"                                                                                                                            | AboutListItem            | Y     |         |
| 13  | `ffmpegVersionText`      | TextView | 动态 FFmpegJni.getVersion() / "N/A"                                                                                                           | textAppearanceLabelSmall | Y     |         |
| 14  | "Capabilities" 行         | TextView | "Capabilities"                                                                                                                              | AboutListItem            | Y     |         |
| 15  | `ffmpegCapabilitiesText` | TextView | "Format convert, Extract audio, Trim, Video compress, Video snapshot, GIF maker, Video enhance, Image compress, Image enhance, Merge files" | textAppearanceLabelSmall | Y     |         |

#### 核心代码逻辑

| 序号  | 模块           | 描述                               | 是否保留？ | 删除后修改方案 |
| --- | ------------ | -------------------------------- | ----- | ------- |
| 1   | 绑定初始化        | DataBinding                      | Y     |         |
| 2   | ActionBar 配置 | setSupportActionBar + 返回键        | Y     |         |
| 3   | FFmpeg 版本获取  | try-catch 异常显示 N/A               | Y     |         |
| 4   | NONFREE 条件编译 | privacyPolicyLayout 仅 NONFREE 显示 | Y     |         |

---

<!-- 全部分组采集完毕，等待用户填写后输出汇总统计 -->
