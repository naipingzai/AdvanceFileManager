# AdvanceFileManager UI 设计文档

> 更新于 2026-06-12，基于项目源码分析。minSdk=35 (Android 15)。

---

## 目录

1. [字体（Typography）](#一字体typography)
2. [间距（Spacing）](#二间距spacing)
3. [图标尺寸（Icon Sizes）](#三图标尺寸icon-sizes)
4. [列表项高度（List Item Heights）](#四列表项高度list-item-heights)
5. [卡片与形状（Shape & Corner Radius）](#五卡片与形状shape--corner-radius)
6. [颜色体系（Colors）](#六颜色体系colors)
7. [触摸目标与最小尺寸](#七触摸目标与最小尺寸)
8. [Toolbar & BottomBar](#八toolbar--bottombar)
9. [FastScroll 相关](#九fastscroll-相关)
10. [SimpleMenu 偏好组件](#十simplemenu-偏好组件)
11. [各页面间距详情](#十各页面间距详情)
12. [变量控制链总结](#十二变量控制链总结)

---

## 一、字体（Typography）

### 1.1 全局字体族

| 属性 | 值 | 定义位置 |
|------|------|----------|
| `app_typeface_plain_regular` | `sans-serif` | `res/values/strings.xml` |

**整个应用统一使用 `sans-serif` (regular) 字体族**。所有 `TextAppearance` 样式都通过 `fontFamily` 和 `android:fontFamily` 引用 `@string/app_typeface_plain_regular`。

### 1.2 M3 标准文字样式（全局 Typography 映射）

在 `themes_material3.xml` 中通过主题属性映射，所有页面继承：

| 主题属性 | App 样式 | 父样式（继承字号） | 覆盖字体 |
|----------|---------|--------|----------|
| `textAppearanceDisplayLarge` | `TextAppearance.App.M3.DisplayLarge` | `Material3.DisplayLarge` (57sp) | `sans-serif` |
| `textAppearanceDisplayMedium` | `TextAppearance.App.M3.DisplayMedium` | `Material3.DisplayMedium` (45sp) | `sans-serif` |
| `textAppearanceDisplaySmall` | `TextAppearance.App.M3.DisplaySmall` | `Material3.DisplaySmall` (36sp) | `sans-serif` |
| `textAppearanceHeadlineLarge` | `TextAppearance.App.M3.HeadlineLarge` | `Material3.HeadlineLarge` (32sp) | `sans-serif` |
| `textAppearanceHeadlineMedium` | `TextAppearance.App.M3.HeadlineMedium` | `Material3.HeadlineMedium` (28sp) | `sans-serif` |
| `textAppearanceHeadlineSmall` | `TextAppearance.App.M3.HeadlineSmall` | `Material3.HeadlineSmall` (24sp) | `sans-serif` |
| `textAppearanceTitleLarge` | `TextAppearance.App.M3.TitleLarge` | `Material3.TitleLarge` (22sp) | `sans-serif` |
| `textAppearanceTitleMedium` | `TextAppearance.App.M3.TitleMedium` | `Material3.TitleMedium` (16sp) | `sans-serif` |
| `textAppearanceTitleSmall` | `TextAppearance.App.M3.TitleSmall` | `Material3.TitleSmall` (14sp) | `sans-serif` |
| `textAppearanceBodyLarge` | `TextAppearance.App.M3.BodyLarge` | `Material3.BodyLarge` (16sp) | `sans-serif` |
| `textAppearanceBodyMedium` | `TextAppearance.App.M3.BodyMedium` | `Material3.BodyMedium` (14sp) | `sans-serif` |
| `textAppearanceBodySmall` | `TextAppearance.App.M3.BodySmall` | `Material3.BodySmall` (12sp) | `sans-serif` |
| `textAppearanceLabelLarge` | `TextAppearance.App.M3.LabelLarge` | `Material3.LabelLarge` (14sp) | `sans-serif` |
| `textAppearanceLabelMedium` | `TextAppearance.App.M3.LabelMedium` | `Material3.LabelMedium` (12sp) | `sans-serif` |
| `textAppearanceLabelSmall` | `TextAppearance.App.M3.LabelSmall` | `Material3.LabelSmall` (11sp) | `sans-serif` |

> 字号由 Material3 父样式继承，App 层只覆盖字体族。

### 1.3 应用自定义文字样式

定义于 `res/values/styles.xml`：

| 样式名 | 父样式 | 覆盖属性 | 用途 |
|--------|--------|----------|------|
| `TextAppearance.App.ToolbarTitle` | `TitleLarge` | `textSize=16sp` | Toolbar 标题 |
| `TextAppearance.App.AboutAppName` | `TitleLarge` | `textSize=18sp` | 关于页应用名 |
| `TextAppearance.App.ListItemTitle` | `BodyLarge` | — | 列表项标题 |
| `TextAppearance.App.ListItemSubtitle` | `BodyMedium` | — | 列表项副标题 |
| `TextAppearance.App.ListItemTitle.Compact` | `TitleMedium` | `textSize=14sp` | 紧凑列表项标题 |
| `TextAppearance.App.ListItemSubtitle.Compact` | `BodyMedium` | `textSize=12sp` | 紧凑列表项副标题 |
| `TextAppearance.App.DialogTitle` | `TitleMedium` | `textStyle=bold` | 对话框标题 |
| `TextAppearance.App.DialogBody` | `BodyMedium` | — | 对话框正文 |
| `TextAppearance.App.AboutListItem` | `BodyLarge` | — | 关于页列表项 |
| `TextAppearance.App.CardTitle` | `TitleSmall` | — | 卡片标题 |
| `TextAppearance.App.CardBody` | `BodySmall` | `textColor=?colorOnSurfaceVariant` | 卡片正文 |
| `TextAppearance.App.SectionTitle` | `TitleSmall` | `textStyle=bold`, `textColor=?colorOnSurface` | 区块标题 |
| `TextAppearance.App.InfoLabel` | `BodyMedium` | `textColor=?colorOnSurfaceVariant` | 信息标签 |
| `TextAppearance.App.StatValue` | `TitleSmall` | `textStyle=bold`, `textColor=?colorOnSurface` | 统计数值 |

### 1.4 特殊字体

| 页面 | 控件 | 字体 | 字号 | 控制方式 |
|------|------|------|------|----------|
| Hex 查看器 | `hexContent` TextView | `monospace` | `12sp` | 布局 XML 硬编码 `android:fontFamily="monospace"` |

### 1.5 文字颜色语义化属性

通过 `attrs.xml` 定义，`themes_material3.xml` 绑定默认值：

| 自定义属性 | 默认值 | 用途 |
|------------|--------|------|
| `appCardTitleColor` | `?colorOnSurface` | 卡片标题颜色 |
| `appCardBodyColor` | `?colorOnSurfaceVariant` | 卡片正文颜色 |
| `appCardIconTint` | `?colorOnSurfaceVariant` | 卡片图标着色 |
| `appCardArrowTint` | `?colorOnSurfaceVariant` | 卡片箭头着色 |

---

## 二、间距（Spacing）

### 2.1 间距系统

定义于 `res/values/dimens.xml`：

| 变量名 | 值 | 用途 |
|--------|------|------|
| `spacing_xxs` | `2dp` | 极小间距（如标题与副标题间距） |
| `spacing_xs` | `4dp` | 小间距 |
| `spacing_sm` | `8dp` | 中小间距（列表垂直内边距、分隔间距） |
| `spacing_md` | `12dp` | 中等间距 |
| `spacing_lg` | `16dp` | 大间距（页面边缘边距） |
| `spacing_xl` | `24dp` | 超大间距（对话框内边距） |
| `spacing_xxl` | `32dp` | 特大间距 |

### 2.2 屏幕边缘边距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `screen_edge_margin` | `16dp` | 标准页面边距 |
| `screen_edge_margin_minus_4dp` | `12dp` | 文件列表图标起始边距 |
| `screen_edge_margin_minus_8dp` | `8dp` | 较小边距 |
| `screen_edge_margin_minus_12dp` | `4dp` | 最小边距 |
| `screen_edge_margin_minus_12dp_at_least_8dp` | `8dp` | 最小触摸边距（≥8dp） |
| `screen_edge_margin_minus_16dp` | `0dp` | 零边距 |
| `app_screen_edge_margin` | `16dp` | 应用级页面边距，通过 `?attr/appScreenEdgeMargin` 引用 |

### 2.3 内容起始边距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `content_start_margin` | `72dp` | 内容起始边距 |
| `content_start_margin_minus_12dp` | `60dp` | 内容起始边距 -12dp |
| `content_start_from_screen_edge_margin_minus_24dp` | `32dp` | 图标-文字间距（about 页等） |
| `content_start_from_screen_edge_margin_minus_36dp_considering_at_least_8dp` | `16dp` | 文件列表菜单按钮边距 |
| `content_start_from_screen_edge_margin_minus_40dp` | `16dp` | About 页面应用名左边距 |
| `content_start_from_screen_edge_margin_minus_44dp` | `12dp` | 文件列表图标右边距 |

### 2.4 对话框间距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `dialog_padding` | `24dp` | 对话框标准内边距 |
| `dialog_padding_minus_4dp` | `20dp` | 对话框内边距 -4dp |
| `dialog_padding_minus_6dp` | `18dp` | 对话框内边距 -6dp |
| `dialog_padding_minus_7dp` | `17dp` | 对话框内边距 -7dp |
| `dialog_padding_minus_16dp` | `8dp` | 对话框内边距 -16dp |
| `dialog_title_divider_padding` | `8dp` | 对话框标题分割线间距 |

### 2.5 列表间距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `list_vertical_padding` | `8dp` | 列表垂直内边距（about 卡片内 padding） |
| `list_bottom_padding_with_fab` | `88dp` | 有 FAB 时列表底部内边距 |

### 2.6 导航抽屉间距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `navigation_max_width` | `320dp` | 导航抽屉最大宽度 |
| `navigation_header_height` | `172dp` | 导航头部高度 |
| `navigation_item_horizontal_padding` | `24dp` | 导航项水平内边距 |
| `navigation_item_icon_padding` | `24dp` | 导航项图标内边距 |
| `navigation_separator_vertical_padding` | `8dp` | 导航分隔线垂直间距 |

> 注：`navigation_item_horizontal_padding` 和 `navigation_item_icon_padding` 在 `dimens_material3.xml` 中桥接到 M3 库变量 `m3_navigation_item_horizontal_padding` 和 `m3_navigation_item_icon_padding`。

### 2.7 其他间距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `tool_fragment_bottom_action_padding` | `72dp` | 工具页底部操作区间距 |
| `toolbar_content_inset_with_nav` | `16dp` | Toolbar 有导航按钮时内容缩进 |

---

## 三、图标尺寸（Icon Sizes）

定义于 `res/values/dimens.xml`：

| 变量名 | 值 | 用途 |
|--------|------|------|
| `icon_size` | `24dp` | 标准图标（文件图标、about 图标等） |
| `large_icon_size` | `40dp` | 大图标（缩略图、应用图标） |
| `badge_size` | `18dp` | 徽章尺寸 |
| `badge_size_plus_1dp` | `19dp` | 应用徽章尺寸 |
| `app_icon_size_large` | `40dp` | 应用大图标 |
| `app_icon_size_default` | `24dp` | 应用默认图标 |
| `app_icon_size_small` | `18dp` | 应用小图标 |
| `app_card_icon_size` | `24dp` | 卡片图标大小，通过 `?attr/appCardIconSize` 引用 |
| `app_card_icon_container_size` | `44dp` | 卡片图标容器大小，通过 `?attr/appCardIconContainerSize` 引用 |
| `app_card_arrow_size` | `20dp` | 卡片箭头大小 |
| `profile_badge_size` | `24dp` | Profile 徽章尺寸（iconloaderlib） |

---

## 四、列表项高度（List Item Heights）

定义于 `res/values/dimens.xml`：

| 变量名 | 值 | 用途 |
|--------|------|------|
| `dense_single_line_list_item_height` | `40dp` | 紧凑单行列表项 |
| `single_line_list_item_height` | `56dp` | 标准单行列表项 |
| `two_line_list_item_height` | `48dp` | 双行列表项（文件列表 `file_item_list.xml` 使用） |

---

## 五、卡片与形状（Shape & Corner Radius）

### 5.1 ShapeAppearance 样式

定义于 `res/values/styles.xml`，通过 `themes_material3.xml` 绑定到全局主题属性：

| 样式名 | 父样式 | 圆角值 | 主题属性 |
|--------|--------|--------|----------|
| `ShapeAppearance.App.LargeComponent` | `ShapeAppearance.Material3.LargeComponent` | `16dp` | `?attr/shapeAppearanceLargeComponent` |
| `ShapeAppearance.App.MediumComponent` | `ShapeAppearance.Material3.MediumComponent` | `12dp` | `?attr/shapeAppearanceMediumComponent` |
| `ShapeAppearance.App.SmallComponent` | `ShapeAppearance.Material3.SmallComponent` | `8dp` | `?attr/shapeAppearanceSmallComponent` |

### 5.2 卡片样式

| 样式名 | 关键属性 | 控制变量 |
|--------|----------|----------|
| `Widget.App.CardView.Filled` | `cardElevation=0dp`, `strokeWidth=0dp`, `cardBackgroundColor=?colorSurfaceContainerLow`, `cardCornerRadius=16dp` | `?attr/appFilledCardStyle` |
| `Widget.App.CardView.Outlined` | `cardElevation=0dp`, `strokeColor=?colorOutlineVariant`, `strokeWidth=1dp`, `cardCornerRadius=16dp` | `?attr/appOutlinedCardStyle` |
| `Widget.MaterialFiles.CardView` | `cardElevation=0dp`, `strokeColor` / `strokeWidth` from M3 | `?attr/materialCardViewStyle` |

### 5.3 其他圆角

| 组件 | 圆角 | 控制方式 |
|------|------|----------|
| 应用卡片圆角 | `16dp` | `dimens.xml` 中 `app_card_corner_radius` → `?attr/appCardCornerRadius` |
| SimpleMenu 弹窗圆角 | `4dp` | `values.xml` 中 `popupBackgroundRadius` |
| ListView.DropDown | 上下 `8dp` 内边距 | `styles.xml` 中 `Widget.MaterialFiles.ListView.DropDown` |

---

## 六、颜色体系（Colors）

### 6.1 M3 动态颜色

- **主题基类**：`Theme.Material3.DynamicColors.DayNight.NoActionBar`
- 使用 Material3 DynamicColor 系统，颜色跟随系统壁纸动态生成
- **minSdk=35 (Android 15)**，Dynamic Color 始终可用，系统自动生成 primary、secondary、tertiary 等色板
- 静态颜色文件仅作为兜底参考，实际不被使用

### 6.2 颜色文件结构说明

项目 values 目录下存在多个颜色文件，**看起来很多，但各有分工**：

| 文件 | 行数 | 用途 | 说明 |
|------|------|------|------|
| `colors.xml` | 68 行 | **应用自定义颜色** | 项目实际使用的颜色：primary/surface 兜底色、12 种文件图标颜色、媒体播放器颜色、IO 监控颜色等。这是最核心的颜色文件。 |
| `colors_google.xml` | 13 行 | **Google Chromium 品牌色** | 仅 3 个颜色：`google_blue_600`(#1A73E8)、`google_blue_300`(#8AB4F8)、`google_grey_900`(#202124)。用作 `color_primary` 和 `color_surface` 的兜底值（当 Dynamic Color 不可用时）。 |
| `colors_google_material3.xml` | 89 行 | **M3 参考色板** | 从 Chromium 项目提取的 M3 静态色板（neutral、primary、secondary、tertiary 各 10-20 个色阶）。当 Dynamic Color 不可用时，作为 M3 主题的静态回退色板。 |
| `colors_material.xml` | 282 行 | **Material Design 1 完整色板** | 标准 MD1 19 色系完整色板（每个色系 10-14 个色阶）。这是 Android 传统标准资源，为项目中可能用到的传统 Material 颜色提供引用。 |
| `colors_custom.xml` | 23 行 | **MD1 色板快捷别名** | 将每个色系的 500 色阶提取为简短别名，如 `material_red` = `material_red_500`。方便代码中引用。 |
| `colors_material3.xml` | 13 行 | **M3 窗口遮罩色** | 仅 3 个颜色（M3 风格的系统窗口遮罩色），用于 v29+ 未使用的硬编码兜底。 |

**总结**：看起来有 6 个颜色文件，但实际上：
- **应用实际使用的**主要是 `colors.xml`（~68 行自定义颜色）
- `colors_google.xml`（3 个品牌色）作为 primary/surface 的回退
- `colors_material.xml`（282 行）是标准 Material 1 调色板，属于**参考性资源**，大部分色阶不会被直接引用
- `colors_google_material3.xml`（89 行）是 M3 静态回退，Dynamic Color 可用时完全不使用
- `colors_custom.xml`（23 行）是上面的快捷别名
- `colors_material3.xml`（13 行）基本可以忽略

由于 minSdk=35 (Android 15)，Dynamic Color 始终可用，静态颜色文件仅作为兜底参考。

### 6.3 应用自定义颜色

定义于 `res/values/colors.xml`（**实际核心颜色**）：

| 颜色名 | 值 | 用途 |
|--------|------|------|
| `color_primary_light` | `#4285F4` (Google Blue 600) | 浅色主题 Primary |
| `color_primary_dark` | `#7BAAF7` (Google Blue 300) | 深色主题 Primary |
| `color_surface_light` | `#FFFFFF` | 浅色主题 Surface |
| `color_surface_dark` | `#1F1F1F` (Google Grey 900) | 深色主题 Surface |
| `activity_icon_tint` | `#49454F` | Activity 图标着色 |
| `shortcut_icon_background` | `#F5F5F5` | 快捷方式图标背景 |
| `media_background` | `@android:color/black` | 媒体查看器背景 |
| `io_read_color` | `#4CAF50` | IO 监控读颜色 |
| `io_write_color` | `#FF9800` | IO 监控写颜色 |

**文件图标颜色系列**（12 种）：

| 颜色名 | 值 |
|--------|------|
| `file_icon_light_blue` | `#4B86F0` |
| `file_icon_blue` | `#4285F4` |
| `file_icon_cyan` | `#24C1E0` |
| `file_icon_light_green` | `#34A853` |
| `file_icon_green` | `#22A667` |
| `file_icon_deep_green` | `#0F9D58` |
| `file_icon_grey` | `#5F6368` |
| `file_icon_orange` | `#FD7541` |
| `file_icon_purple` | `#A142F4` |
| `file_icon_red` | `#EA4335` |
| `file_icon_deep_red` | `#DB4437` |
| `file_icon_yellow` | `#F4B400` |

### 6.3 语义化颜色属性

通过 `attrs.xml` 定义，`themes_material3.xml` 绑定：

| 属性 | 来源 | 用途 |
|------|------|------|
| `?colorOnSurface` | M3 | 主要文字/图标颜色 |
| `?colorOnSurfaceVariant` | M3 | 次要文字/图标颜色 |
| `?colorSurface` | M3 | 背景色 |
| `?colorSurfaceContainerLow` | M3 | 填充卡片背景 |
| `?colorOutlineVariant` | M3 | 描边卡片边框色 |
| `?colorAppBarSurface` | 自定义 → `?colorSurface` | AppBar 背景 |

---

## 七、触摸目标与最小尺寸

定义于 `res/values/dimens.xml` 和 `themes_material3.xml`：

| 变量名 | 值 | 用途 |
|--------|------|------|
| `touch_target_size` | `48dp` | 所有可交互元素最小触摸区域 |
| `afs_min_touch_target_size` | `24dp` | FastScroll 缩小的触摸目标（因旁边有弹出菜单） |
| `minTouchTargetSize` | `0dp` | 主题级全局设置（`themes_material3.xml` 中设置为 0） |
| `tab_layout_height` | `48dp` | TabLayout 高度 |

---

## 八、Toolbar & BottomBar

定义于 `res/values/dimens.xml`：

| 变量名 | 值 | 用途 |
|--------|------|------|
| `file_list_toolbar_padding_start` | `8dp` | 文件列表 Toolbar 左内边距 |
| `file_list_toolbar_padding_end_no_overflow` | `14dp` | 无溢出时 Toolbar 右内边距 |
| `file_list_toolbar_padding_end_with_overflow` | `10dp` | 有溢出时 Toolbar 右内边距 |
| `toolbar_content_inset_with_nav` | `16dp` | Toolbar 有导航按钮时内容缩进 |
| `bottom_bar_elevation` | `4dp` | 底部栏阴影高度 |

---

## 九、FastScroll 相关

定义于 `res/values/dimens.xml`：

| 变量名 | 值 | 用途 |
|--------|------|------|
| `afs_popup_min_size` | `88dp` | 弹窗最小尺寸 |
| `afs_popup_margin_end` | `16dp` | 弹窗右边距 |
| `afs_popup_text_size` | `45dp` | 弹窗文字大小 |
| `afs_md2_popup_min_width` | `78dp` | MD2 弹窗最小宽度 |
| `afs_md2_popup_min_height` | `64dp` | MD2 弹窗最小高度 |
| `afs_md2_popup_margin_end` | `14dp` | MD2 弹窗右边距 |
| `afs_md2_popup_padding_start` | `16dp` | MD2 弹窗左内边距 |
| `afs_md2_popup_padding_end` | `29dp` | MD2 弹窗右内边距 |
| `afs_md2_popup_elevation` | `3dp` | MD2 弹窗阴影 |
| `afs_md2_popup_text_size` | `34dp` | MD2 弹窗文字大小 |
| `afs_track_color` | `#39FFFFFF` | 滚动条轨道颜色 |

---

## 十、SimpleMenu 偏好组件

SimpleMenu 是一个自定义的下拉选择偏好组件（非独立页面），用于**设置页（SettingsFragment）**中的 ListPreference 替代品。它在 `settings.xml` 中以 `rikka.preference.SimpleMenuPreference` 形式使用，例如"文件名省略方式"和"APK 默认打开方式"等设置项。点击时弹出一个 Material 风格的弹出菜单或对话框供用户选择。

定义于 `res/values/values.xml`：

### 弹窗尺寸与间距

| 变量名 | 值 | 用途 |
|--------|------|------|
| `simple_menu_dialog_max_width` | `600dp` | SimpleMenu 对话框模式最大宽度 |
| `simple_menu_margin` | `15dp` | 列表模式水平边距 |
| `simple_menu_unit` | `56dp` | 列表项单位高度 |
| `simple_menu_max_units` | `5` | 最多显示5项 |

### 列表模式间距

| 属性 | 值 | 用途 |
|------|------|------|
| `listMarginHorizontal` | `15dp` | 列表模式水平边距 |
| `listMarginVertical` | `8dp` | 列表模式垂直边距 |
| `listItemPadding` | `16dp` | 列表项内边距 |
| `listElevation` | `8dp` | 列表阴影高度 |

### 对话框模式间距

| 属性 | 值 | 用途 |
|------|------|------|
| `dialogMarginHorizontal` | `16dp` | 对话框模式水平边距 |
| `dialogMarginVertical` | `24dp` | 对话框模式垂直边距 |
| `dialogItemPadding` | `24dp` | 对话框项内边距 |
| `dialogElevation` | `24dp` | 对话框阴影高度 |
| `popupBackgroundRadius` | `4dp` | 弹窗背景圆角 |

---

## 十一、各页面间距详情

### 11.1 文件列表页（FileListFragment）

**布局文件**：`file_list_fragment.xml` → `file_list_fragment_include.xml`

| 元素 | 变量/硬编码值 | 用途 |
|------|--------------|------|
| 整体 | `DrawerLayout` + `CoordinatorLayout` + `PersistentBarLayout` | 架构容器 |
| 列表项行高 | `@dimen/two_line_list_item_height` = `48dp` | 双行文件列表项高度 |
| 图标容器 | `width/height=@dimen/touch_target_size` = `48dp` | 图标触摸区域 |
| 图标容器左边距 | `@dimen/screen_edge_margin_minus_4dp` = `12dp` | 图标到左边距 |
| 图标容器右边距 | `@dimen/content_start_from_screen_edge_margin_minus_44dp` = `12dp` | 图标到文字间距 |
| 图标内边距 | `@dimen/touch_target_large_icon_padding` = `4dp` | 图标容器内边距 |
| 图标尺寸 | `@dimen/icon_size` = `24dp` | 文件图标大小 |
| 缩略图尺寸 | `@dimen/large_icon_size` = `40dp` | 缩略图大小 |
| 标题文字样式 | `?textAppearanceListItem` → `TextAppearance.App.ListItemTitle` | 文件名 |
| 副标题文字样式 | `?textAppearanceListItemSecondary` → `TextAppearance.App.ListItemSubtitle` | 文件描述 |
| 副标题颜色 | `?colorOnSurfaceVariant` | 副标题灰色 |
| 菜单按钮 | `width/height=@dimen/touch_target_size` = `48dp` | 右侧菜单按钮 |
| 菜单按钮左边距 | `@dimen/content_start_from_screen_edge_margin_minus_36dp_considering_at_least_8dp` = `16dp` | 菜单左边距 |
| 菜单按钮右边距 | `@dimen/screen_edge_margin_minus_12dp_at_least_8dp` = `8dp` | 菜单右边距 |
| 菜单按钮内边距 | `@dimen/touch_target_icon_padding` = `12dp` | 菜单图标内边距 |
| 菜单按钮图标颜色 | `?colorOnSurfaceVariant` | 灰色三点菜单 |

### 11.2 关于页（AboutFragment）

**布局文件**：`about_fragment.xml`

| 元素 | 变量/硬编码值 | 用途 |
|------|--------------|------|
| 页面整体内边距 | `@dimen/screen_edge_margin` = `16dp` | 滚动容器 padding |
| 卡片分隔线 | `@drawable/transparent_divider_vertical_16dp` | 卡片间 16dp 间距 |
| 卡片内边距 | `paddingTop/Bottom=@dimen/list_vertical_padding` = `8dp` | 卡片内部上下间距 |
| 应用名行高 | `?listPreferredItemHeight` | 使用系统推荐行高 |
| 应用名图标尺寸 | `@dimen/large_icon_size` = `40dp` | 启动器图标 |
| 应用名左边距 | `@dimen/content_start_from_screen_edge_margin_minus_40dp` = `16dp` | 图标到文字 |
| 应用名文字样式 | `TextAppearance.MaterialFiles.Material3.AboutAppName` (18sp) | 应用名 |
| 列表行高 | `?listPreferredItemHeightSmall` | 单行列表行高 |
| 列表项左/右内边距 | `?android:listPreferredItemPaddingStart/End` | 系统推荐内边距 |
| 列表项图标尺寸 | `@dimen/icon_size` = `24dp` | 功能图标 |
| 图标-文字间距 | `@dimen/content_start_from_screen_edge_margin_minus_24dp` = `32dp` | 图标到文字 |
| 列表项文字样式 | `TextAppearance.MaterialFiles.Material3.AboutListItem` (BodyLarge) | 列表项文字 |
| 版本号样式 | `?textAppearanceLabelSmall` | 小号标签文字 |
| 版本信息行内边距 | `paddingTop/Bottom=8dp` | 行内间距 |
| 区块标题样式 | `?textAppearanceBodySmall` + `textColor=?colorOnSurfaceVariant` | "作者"等区块标题 |

### 11.3 设置页（SettingsFragment）

**布局文件**：`settings_fragment.xml`

| 元素 | 变量/硬编码值 | 用途 |
|------|--------------|------|
| AppBar 背景 | `?colorAppBarSurface` | 顶栏背景色 |
| Toolbar 高度 | `?actionBarSize` | 系统 ActionBar 高度 |
| 内容区 | `SettingsPreferenceFragment` (PreferenceFragment) | 使用系统 Preference 布局 |

### 11.4 Feature 设置页（FeatureSettingsFragment）

**布局文件**：`feature_settings_fragment.xml` + `feature_settings_item.xml`

| 元素 | 变量/硬编码值 | 用途 |
|------|--------------|------|
| 列表项最小高度 | `72dp`（硬编码） | 每行最小高度 |
| 列表项左内边距 | `@dimen/navigation_item_horizontal_padding` = `24dp` | 左侧留白 |
| 列表项上内边距 | `16dp`（硬编码） | 上方留白 |
| 列表项下内边距 | `16dp`（硬编码） | 下方留白 |
| 列表项右内边距 | `@dimen/screen_edge_margin_minus_12dp_at_least_8dp` = `8dp` | 右侧留白 |
| 标题文字样式 | `?textAppearanceListItem` → `TextAppearance.App.ListItemTitle` | Feature 标题 |
| 描述文字样式 | `?textAppearanceListItemSmall` → `TextAppearance.App.ListItemSubtitle` | Feature 描述 |
| 标题-描述间距 | `2dp`（硬编码 `layout_marginTop`） | 极小间距 |
| 描述颜色 | `?colorOnSurfaceVariant` | 灰色 |
| Switch 左边距 | `16dp`（硬编码 `layout_marginStart`） | 开关左边距 |

### 11.5 Hex 查看器（HexViewerToolFragment）

**布局文件**：`fragment_hex_viewer_tool.xml`

| 元素 | 变量/硬编码值 | 用途 |
|------|--------------|------|
| 文件信息文字 | `marginHorizontal=16dp`, `marginTop=8dp` | 信息行边距 |
| 文件信息文字样式 | `?attr/textAppearanceBodySmall` | 小号文字 |
| Hex 内容字体 | `monospace` | 等宽字体（非 sans-serif） |
| Hex 内容字号 | `12sp` | 小号等宽字体 |
| Hex 内容内边距 | `16dp` | 内容区域留白 |
| HorizontalScrollView 上边距 | `4dp` | 信息与内容间距 |
| 加载更多按钮 | `Widget.Material3.Button.TextButton` 样式 | 文字按钮 |

### 11.6 对话框

**通用对话框间距**（通过 `dimens.xml` 控制）：

| 变量 | 值 | 用途 |
|------|------|------|
| `dialog_padding` | `24dp` | 对话框标准内边距 |
| `dialog_title_divider_padding` | `8dp` | 标题与内容分割间距 |
| `dialog_padding_minus_16dp` | `8dp` | 紧凑对话框内边距 |

**名称对话框**（`name_dialog.xml`）：

| 元素 | 值 | 用途 |
|------|------|------|
| paddingTop | `@dimen/dialog_title_divider_padding` = `8dp` | 标题分割间距 |
| 内部 paddingBottom | `8dp`（硬编码） | 内容底部间距 |

### 11.7 导航抽屉（NavigationFragment）

**布局文件**：`navigation_item.xml` + `navigation_fragment.xml`

| 元素 | 变量/硬编码值 | 用途 |
|------|--------------|------|
| 抽屉最大宽度 | `@dimen/navigation_max_width` = `320dp` | 限制宽度 |
| 抽屉背景 | `?colorSurface` | 表面色背景 |
| 行高 | `?listPreferredItemHeightSmall` | 系统推荐行高 |
| 水平内边距 | `@dimen/navigation_item_horizontal_padding` = `24dp` | 在 M3 NavigationView 中使用 |
| 图标内边距 | `@dimen/navigation_item_icon_padding` = `24dp` | 在 M3 NavigationView 中使用 |
| 分隔线间距 | `@dimen/navigation_separator_vertical_padding` = `8dp` | 分隔线上下间距 |

---

## 十二、变量控制链总结

### 12.1 文字样式控制链

```
themes_material3.xml (全局 textAppearanceXxx 映射)
  └─ styles.xml (TextAppearance.App.M3.* 定义字体 = sans-serif)
       └─ styles.xml (TextAppearance.App.* 覆盖字号/粗细/颜色)
            └─ attrs.xml (appXxxTextAppearance 语义化属性声明)
                 └─ themes_material3.xml (属性默认值绑定到具体样式)
                      └─ 布局 XML 通过 ?attr/appXxx 或 ?textAppearanceXxx 引用
```

**示例**：文件列表标题
```
主题 textAppearanceListItem
  → styles.xml: TextAppearance.App.ListItemTitle (parent=BodyLarge, fontFamily=sans-serif)
    → file_item_list.xml: android:textAppearance="?textAppearanceListItem"
```

### 12.2 间距控制链

```
dimens.xml (所有间距值定义)
  └─ 布局 XML 通过 @dimen/xxx 直接引用
  └─ attrs.xml (appScreenEdgeMargin 等自定义属性声明)
       └─ themes_material3.xml (属性默认值绑定到 @dimen/xxx)
            └─ 布局 XML 通过 ?attr/xxx 引用
```

**示例**：页面边缘边距
```
dimens.xml: <dimen name="screen_edge_margin">16dp</dimen>
  → attrs.xml: <attr name="appScreenEdgeMargin" format="dimension" />
    → themes_material3.xml: <item name="appScreenEdgeMargin">@dimen/app_screen_edge_margin</item>
      → 布局: android:padding="?attr/appScreenEdgeMargin"
```

### 12.3 颜色控制链

```
M3 DynamicColors (系统动态色生成)
  └─ themes_material3.xml (colorAppBarSurface 等自定义属性默认值)
       └─ attrs.xml (appCardTitleColor 等语义化属性声明)
            └─ themes_material3.xml (属性默认值绑定到 M3 颜色引用)
                 └─ 布局 XML 通过 ?attr/xxx 或 ?colorXxx 引用
```

**示例**：卡片标题颜色
```
M3: ?colorOnSurface
  → attrs.xml: <attr name="appCardTitleColor" format="color" />
    → themes_material3.xml: <item name="appCardTitleColor">?colorOnSurface</item>
      → 布局: android:textColor="?attr/appCardTitleColor"
```

---

## 附录：资源文件索引

| 文件 | 内容 |
|------|------|
| `res/values/dimens.xml` | 所有间距、尺寸定义 |
| `res/values/dimens_material3.xml` | M3 库间距桥接 |
| `res/values/styles.xml` | TextAppearance、ShapeAppearance、CardView 等样式 |
| `res/values/styles_material3.xml` | M3 兼容别名样式 |
| `res/values/themes_material3.xml` | 全局主题定义与属性映射 |
| `res/values/attrs.xml` | 自定义属性声明 |
| `res/values/colors.xml` | 自定义颜色值 |
| `res/values/colors_material3.xml` | M3 材料颜色 |
| `res/values/colors_custom.xml` | 自定义颜色选择器 |
| `res/values/colors_google.xml` | Google 品牌颜色 |
| `res/values/colors_material.xml` | 材料颜色 |
| `res/values/values.xml` | SimpleMenu 组件属性与样式 |
| `res/values/strings.xml` | 字符串资源（含字体族定义） |
| `res/values/integers.xml` | 整数值（动画时长等） |
| `res/layout/*.xml` | 所有布局文件（约 80 个） |