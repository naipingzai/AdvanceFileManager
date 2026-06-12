# UI 设计规范文档

> **适用范围**: Android 15+ (API 35+)  
> 本项目已移除所有 Android 14 及以下的兼容设计，仅保留一套 Android 15 及以上的设计规范。

## 一、页面类型分类

### 1. Activity 页面（4个）
| 页面 | 布局模式 | 说明 |
|------|----------|------|
| activity_ebook_viewer | LinearLayout + Toolbar + WebView | 电子书查看 |
| activity_file_tools | LinearLayout + Toolbar + FrameLayout | 文件工具 |
| activity_ffmpeg_tools | LinearLayout + Toolbar + FrameLayout | FFmpeg工具 |
| file_picker_dialog_activity | FrameLayout + MaterialCardView | 文件选择对话框 |

### 2. Fragment 页面（28个）

#### 2.1 CoordinatorLayout 模式（带AppBar）
- settings_fragment / feature_settings_fragment / ui_settings_fragment
- about_fragment / text_editor_fragment
- bookmark_directory_list_fragment / storage_list_fragment
- trash_fragment / large_text_viewer_fragment
- plugin_settings_fragment / standard_directory_list_fragment

#### 2.2 FrameLayout 模式（媒体查看器）
- image_viewer_fragment / video_viewer_fragment
- audio_player_fragment / ebook_viewer_fragment
- csv_viewer_fragment / pdf_viewer_fragment

#### 2.3 LinearLayout 模式（工具页）
- fragment_recent_files / fragment_trash
- fragment_hex_viewer_tool / fragment_empty_search
- fragment_encryption / fragment_file_search
- fragment_file_compare / fragment_duplicate_finder
- fragment_ffmpeg_feature

### 3. Dialog 对话框（13个）
- name_dialog / batch_rename_dialog / create_archive_dialog
- archive_password_dialog / edit_bookmark_directory_dialog
- edit_device_storage_dialog / edit_document_tree_dialog
- edit_external_storage_shortcut_dialog / file_properties_dialog
- set_mode_dialog / set_principal_dialog
- set_selinux_context_dialog / permission_list_dialog

### 4. 列表项 Item（21个）
- file_item_list / file_item_grid / tool_file_item
- storage_item / bookmark_directory_item / navigation_item
- principal_item / permission_item / mode_bit_item
- breadcrumb_item / text_line_item / pdf_page_item
- item_file_tool / item_media_tool_card / item_plugin

---

## 二、布局模式规范

### 模式A：CoordinatorLayout + AppBar + 内容区
```
CoordinatorLayout (fitsSystemWindows=true)
  ├─ CoordinatorAppBarLayout (background=?colorAppBarSurface)
  │    └─ MaterialToolbar (?actionBarSize)
  └─ CoordinatorScrollingFrameLayout
       └─ RecyclerView / NestedScrollView / PreferenceFragment
```

### 模式B：LinearLayout + 双按钮 + ProgressBar + RecyclerView
```
LinearLayout (vertical)
  ├─ LinearLayout (horizontal, padding=8dp)
  │    ├─ MaterialButton [weight=1, marginEnd=4dp]
  │    └─ MaterialButton [weight=1, marginStart=4dp]
  ├─ ProgressBar (horizontal, gone)
  └─ RecyclerView (clipToPadding=false, paddingBottom=8dp)
```

### 模式C：FrameLayout 叠加层（媒体查看器）
```
FrameLayout (match_parent)
  ├─ 内容层 (PlayerView / WebView / ImageView)
  ├─ ProgressBar (居中)
  ├─ 错误文本 (居中)
  └─ AppBar 浮层 (FrameLayout, gradient_top)
       └─ MaterialToolbar
```

### 模式D：Dialog 表单
```
FrameLayout (paddingTop=@dimen/dialog_title_divider_padding)
  └─ NestedScrollView (scrollIndicators=top|bottom)
       └─ LinearLayout (vertical)
            └─ paddingStart/End=?dialogPreferredPadding
            └─ paddingTop/Bottom=8dp
```

---

## 三、间距规范

### 间距层级
| 层级 | 值 | 使用场景 |
|------|-----|----------|
| 页面级 | @dimen/screen_edge_margin (16dp) | 页面边缘留白 |
| 对话框级 | @dimen/dialogPreferredPadding (16dp/20dp) | 对话框内水平padding |
| 组件级 | 16dp | 卡片内padding、标准间距 |
| 元素级 | 8dp | 按钮间距、小分组间距 |
| 细节级 | 4dp | 紧密关联元素间 |
| 极小级 | 2dp | 标题与副标题间 |

### 间距变量定义
```xml
<dimen name="spacing_xxs">2dp</dimen>
<dimen name="spacing_xs">4dp</dimen>
<dimen name="spacing_sm">8dp</dimen>
<dimen name="spacing_md">12dp</dimen>
<dimen name="spacing_lg">16dp</dimen>
<dimen name="spacing_xl">24dp</dimen>
<dimen name="spacing_xxl">32dp</dimen>
```

---

## 四、字体大小规范

### textAppearance 使用规范
| 用途 | textAppearance | 约sp值 |
|------|----------------|--------|
| 大标题 | textAppearanceTitleLarge | ~22sp |
| 中标题 | textAppearanceTitleMedium | ~16sp |
| 小标题 | textAppearanceTitleSmall | ~14sp |
| 正文大 | textAppearanceBodyLarge | ~16sp |
| 正文中 | textAppearanceBodyMedium | ~14sp |
| 正文小 | textAppearanceBodySmall | ~12sp |
| 列表项标题 | textAppearanceListItem | ~16sp |
| 列表项副标题 | textAppearanceListItemSecondary | ~14sp |
| 标签大 | textAppearanceLabelLarge | ~14sp |
| 标签中 | textAppearanceLabelMedium | ~12sp |
| 标签小 | textAppearanceLabelSmall | ~11sp |

### 引用格式规范
统一使用 `?textAppearance*` 短格式，不使用 `?attr/textAppearance*` 或 `?android:attr/textAppearance*`。

---

## 五、卡片样式规范

### 卡片圆角
所有内容卡片统一使用 `cardCornerRadius="12dp"`。

### 卡片阴影
| 类型 | cardElevation | strokeWidth | 使用场景 |
|------|---------------|-------------|----------|
| 填充卡片 | 2dp | 0dp | 设置项、关于页面、功能卡片 |
| 描边卡片 | 0dp | 1dp | 特殊强调区域 |

### 卡片样式定义
```xml
<!-- 填充卡片 -->
<style name="Widget.App.CardView.Filled">
    <item name="cardElevation">2dp</item>
    <item name="strokeWidth">0dp</item>
    <item name="cardCornerRadius">12dp</item>
</style>

<!-- 描边卡片 -->
<style name="Widget.App.CardView.Outlined">
    <item name="cardElevation">0dp</item>
    <item name="strokeWidth">1dp</item>
    <item name="cardCornerRadius">12dp</item>
</style>
```

---

## 六、组件规范

### 开关组件
统一使用 `MaterialSwitch` 或 `MaterialCheckBox`，不使用原生 `CheckBox`。

### 列表项高度
| 类型 | 高度 | 使用场景 |
|------|------|----------|
| 单行列表项 | ?listPreferredItemHeightSmall (56dp) | 导航项、设置项 |
| 双行列表项 | 80dp | 功能设置项 |
| 对话框项 | ?listPreferredItemHeight (72dp) | 对话框选项 |

### 图标尺寸
| 类型 | 尺寸 | 使用场景 |
|------|------|----------|
| 列表图标 | @dimen/icon_size (24dp) | 列表项左侧图标 |
| 大图标 | @dimen/large_icon_size (48dp) | 关于页面应用图标 |
| 徽章图标 | @dimen/badge_size (18dp) | 文件状态徽章 |

---

## 七、用户可调整设置

通过 **设置 → 显示设置** 页面，用户可调整：

1. **字体大小**：缩放比例 50%~200%
2. **界面间距**：缩放比例 50%~200%
3. **列表项高度**：缩放比例 50%~200%
4. **图标大小**：缩放比例 50%~200%
5. **页面边距**：缩放比例 50%~200%
6. **对话框内边距**：缩放比例 50%~200%
7. **按钮间距**：缩放比例 50%~200%

### 预设模式
- **紧凑**：适合小屏幕，信息密度高
- **默认**：平衡显示效果
- **宽松**：适合大屏幕或视力不佳用户
- **自定义**：手动调节各参数

---

## 八、入口设置页面规范

### 页面结构
入口设置页面包含三个可折叠分组：
1. **常规工具** - 文件操作入口（打开方式、压缩、分享、复制路径、添加书签、属性）
2. **文件工具** - 文件搜索、重复查找等
3. **媒体工具** - 格式转换、视频压缩等

### 折叠行为
- 所有分组默认折叠
- 点击分组标题行展开/折叠
- 分组标题行无右侧开关，仅显示展开/折叠图标
- 子项显示开关用于启用/禁用

### 实时生效
入口设置的更改在返回文件列表时立即生效，无需重启应用。
