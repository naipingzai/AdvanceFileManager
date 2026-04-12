# 页面-布局文件映射表

> 本文件记录所有 Activity/Fragment 与其对应布局文件的映射关系，供分析 skill 快速定位。

## Activities

| Activity | 布局文件 | 菜单文件 |
|----------|---------|---------|
| AppActivity | (使用 Fragment) | - |
| FileListActivity | (使用 FileListFragment) | - |
| EditFileActivity | - | - |
| OpenFileActivity | - | - |
| FilePickerDialogActivity | file_picker_dialog_activity.xml | - |
| SettingsActivity | settings_fragment.xml (Fragment) | - |
| TextEditorActivity | (使用 TextEditorFragment) | - |
| AudioPlayerActivity | (使用 AudioPlayerFragment) | - |
| VideoViewerActivity | (使用 VideoViewerFragment) | - |
| ImageViewerActivity | (使用 ImageViewerFragment) | - |
| PdfViewerActivity | (使用 PdfViewerFragment) | - |
| HexViewerActivity | (使用 HexViewerFragment) | - |
| CsvViewerActivity | (使用 CsvViewerFragment) | - |
| EbookViewerActivity | (使用 EbookViewerFragment) | - |
| TerminalActivity | (使用 TerminalFragment) | - |
| SaveAsActivity | - | - |
| ToolHostActivity | (使用 Tool Fragments) | - |
| DialogHostActivity | (使用 Dialog Fragments) | - |

## Main Fragments

| Fragment | 布局文件 | 菜单文件 |
|----------|---------|---------|
| FileListFragment | file_list_fragment.xml | file_list.xml, file_list_select.xml, file_list_paste.xml, file_list_pick.xml |
| NavigationFragment | navigation_fragment.xml | - |
| AboutFragment | about_fragment.xml | - |
| SettingsFragment | settings_fragment.xml | - |
| SettingsPreferenceFragment | (Preference XML) | - |
| StandardDirectoryListFragment | standard_directory_list_fragment.xml | - |
| BookmarkDirectoryListFragment | bookmark_directory_list_fragment.xml | - |
| StorageListFragment | storage_list_fragment.xml | - |

## Viewer Fragments

| Fragment | 布局文件 | 菜单文件 |
|----------|---------|---------|
| TextEditorFragment | text_editor_fragment.xml | text_editor.xml |
| LargeTextViewerFragment | large_text_viewer_fragment.xml | large_text_viewer.xml |
| AudioPlayerFragment | audio_player_fragment.xml | audio_player.xml |
| VideoViewerFragment | video_viewer_fragment.xml | video_viewer.xml |
| ImageViewerFragment | image_viewer_fragment.xml | image_viewer.xml |
| PdfViewerFragment | pdf_viewer_fragment.xml | pdf_viewer.xml |
| HexViewerFragment | hex_viewer_fragment.xml | hex_viewer.xml |
| CsvViewerFragment | csv_viewer_fragment.xml | - |
| EbookViewerFragment | ebook_viewer_fragment.xml | ebook_viewer.xml |
| TerminalFragment | terminal_fragment.xml | terminal.xml |

## Tool Fragments

| Fragment | 布局文件 | 菜单文件 |
|----------|---------|---------|
| ToolGroupFragment | tool_group_fragment.xml | - |
| AppManagerFragment | app_manager_fragment.xml | app_manager.xml |
| DuplicateFinderFragment | duplicate_finder_fragment.xml | duplicate_finder.xml |
| EmptySearchFragment | empty_search_fragment.xml | empty_search.xml |
| EncryptionFragment | encryption_fragment.xml | - |
| FileCompareFragment | file_compare_fragment.xml | - |
| FileSearchFragment | file_search_fragment.xml | file_search.xml |
| FormatConvertFragment | format_convert_fragment.xml | format_convert_selection.xml |
| ImageCompressFragment | image_compress_fragment.xml | - |
| MediaToolsFragment | media_tools_fragment.xml | - |
| RecentFilesFragment | recent_files_fragment.xml | - |
| StorageAnalysisFragment | storage_analysis_fragment.xml | - |
| TrashFragment | trash_fragment.xml | trash.xml |

## Dialog Fragments

| Fragment | 布局文件 |
|----------|---------|
| CreateDirectoryDialogFragment | name_dialog.xml |
| CreateFileDialogFragment | name_dialog.xml |
| RenameFileDialogFragment | name_dialog.xml |
| BatchRenameDialogFragment | batch_rename_dialog.xml |
| CreateArchiveDialogFragment | create_archive_dialog.xml |
| ArchivePasswordDialogFragment | archive_password_dialog.xml |
| OpenFileAsDialogFragment | (AlertDialog) |
| PathDialogFragment | (AlertDialog) |
| NavigateToPathDialogFragment | name_dialog.xml |
| FilePropertiesDialogFragment | file_properties_dialog.xml |
| SetModeDialogFragment | set_mode_dialog.xml |
| SetPrincipalDialogFragment | set_principal_dialog.xml |
| SetSeLinuxContextDialogFragment | set_selinux_context_dialog.xml |
| EditBookmarkDirectoryDialogFragment | edit_bookmark_directory_dialog.xml |
| EditDeviceStorageDialogFragment | edit_device_storage_dialog.xml |
| EditDocumentTreeDialogFragment | edit_document_tree_dialog.xml |
| EditExternalStorageShortcutDialogFragment | edit_external_storage_shortcut_dialog.xml |
| AddStorageDialogFragment | (AlertDialog) |
| ColorPreferenceDialogFragment | color_picker_dialog.xml |
| FileJobErrorDialogFragment | file_job_error_dialog_view.xml |
| FileJobConflictDialogFragment | file_job_conflict_dialog_view.xml |
| ConfirmDeleteFilesDialogFragment | (AlertDialog) |
| ConfirmReplaceFileDialogFragment | (AlertDialog) |
| ConfirmReloadDialogFragment | (AlertDialog) |
| ConfirmCloseDialogFragment | (AlertDialog) |
| ConfirmDeleteAudioDialogFragment | (AlertDialog) |
| ConfirmDeleteDialogFragment | (AlertDialog) |
| ConfirmDeleteVideoDialogFragment | (AlertDialog) |
| PermissionListDialogFragment | permission_list_dialog.xml |
| LicensesDialogFragment | (AlertDialog) |

## Item 布局（RecyclerView/List 条目）

| 布局文件 | 用途 |
|---------|------|
| file_item_list.xml | 文件列表项（列表模式） |
| file_item_grid.xml | 文件列表项（网格模式） |
| navigation_item.xml | 导航抽屉项 |
| navigation_divider_item.xml | 导航分隔线 |
| breadcrumb_item.xml | 面包屑路径项 |
| storage_item.xml | 存储设备项 |
| storage_category_item.xml | 存储分析分类项 |
| bookmark_directory_item.xml | 书签目录项 |
| permission_item.xml | 权限列表项 |
| principal_item.xml | 用户/组项 |
| mode_bit_item.xml | 权限位项 |
| tool_card_item.xml | 工具卡片项 |
| tool_file_item.xml | 工具文件项 |
| media_tool_card_item.xml | 媒体工具卡片 |
| format_convert_item.xml | 格式转换项 |
| duplicate_group_header.xml | 重复文件组头 |
| hex_row_item.xml | 十六进制行 |
| text_line_item.xml | 文本行 |
| pdf_page_item.xml | PDF 页 |
| image_viewer_item.xml | 图片查看器项 |
| chip_filter.xml | 筛选 Chip |
| chip_input_closeable.xml | 可关闭的输入 Chip |
| file_properties_tab_item.xml | 文件属性标签页项 |
| file_properties_checksum_compare_item.xml | 校验和对比项 |
