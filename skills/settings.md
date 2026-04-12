# Skill: settings — 设置与偏好

## 概述
`naipingzai.materialfile.settings` 包含 23 个文件，管理应用偏好设置。

## 可测试类

| 类 | 可测试方法 |
|----|-----------|
| Settings | 各种 SettingLiveData 的默认值和类型 |
| PathSettings | `getFileListViewType(path)`, `getFileListSortOptions(path)` |
| SettingLiveData | `putValue()`, SharedPreference 监听 |
| NonNegativeIntegerPreference | `integer` (get/set), 非负验证 |
| PasswordPreference | SimpleSummaryProvider |
| DefaultIfEmptyEditTextPreference | `setText()` 空值处理 |

## 测试策略
- SettingLiveData 逻辑: Robolectric Unit Test
- Preference 子类: Robolectric Unit Test
