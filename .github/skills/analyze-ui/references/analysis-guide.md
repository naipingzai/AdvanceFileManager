# UI 分析参考指南

## 需要扫描的 XML 标签类型

### 按钮类标签
| 标签 | 说明 |
|------|------|
| `<Button>` | 标准按钮 |
| `<com.google.android.material.button.MaterialButton>` | Material 按钮 |
| `<ImageButton>` | 图片按钮 |
| `<com.google.android.material.floatingactionbutton.FloatingActionButton>` | FAB |
| `<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>` | 扩展 FAB |
| `<com.leinardi.android.speeddial.SpeedDialView>` | SpeedDial |
| `<Chip>` / `<com.google.android.material.chip.Chip>` | Chip（可点击） |
| `<Switch>` / `<SwitchCompat>` | 开关 |
| `<CheckBox>` | 复选框 |
| `<RadioButton>` | 单选按钮 |
| `<ToggleButton>` | 切换按钮 |

### 文字类标签
| 标签 | 说明 |
|------|------|
| `<TextView>` | 文本显示 |
| `<EditText>` | 文本输入 |
| `<com.google.android.material.textfield.TextInputEditText>` | Material 文本输入 |
| `<com.google.android.material.textfield.TextInputLayout>` | 文本输入布局（含 hint） |
| `<AutoCompleteTextView>` | 自动补全文本 |

### 需要提取的属性
| 属性 | 说明 |
|------|------|
| `android:id` | 元素 ID |
| `android:text` | 显示文字 |
| `android:hint` | 提示文字 |
| `android:textSize` | 字体大小 |
| `android:textAppearance` | 文字样式 |
| `android:lineSpacingExtra` | 行间距（额外） |
| `android:lineSpacingMultiplier` | 行间距（倍数） |
| `android:lineHeight` | 行高 |
| `android:textColor` | 文字颜色 |
| `android:textStyle` | 文字风格（bold/italic） |
| `android:onClick` | 点击处理方法名 |
| `style` | 引用的样式 |
| `android:contentDescription` | 内容描述 |

## Kotlin 代码中的点击逻辑关键词

```kotlin
// 直接绑定
binding.xxx.setOnClickListener { ... }
binding.xxx.setOnLongClickListener { ... }
findViewById<View>(R.id.xxx).setOnClickListener { ... }

// 菜单处理
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
        R.id.action_xxx -> { ... }
    }
}

// SpeedDial
binding.speedDial.setOnActionSelectedListener { actionItem ->
    when (actionItem.id) {
        R.id.xxx -> { ... }
    }
}

// Toolbar 导航
toolbar.setNavigationOnClickListener { ... }

// BottomNavigationView
bottomNav.setOnItemSelectedListener { item -> ... }

// TabLayout
tabLayout.addOnTabSelectedListener(...)

// RecyclerView item click
holder.itemView.setOnClickListener { ... }

// Dialog buttons
AlertDialog.Builder(context)
    .setPositiveButton(...) { ... }
    .setNegativeButton(...) { ... }
    .setNeutralButton(...) { ... }
```

## ViewBinding 命名规则

布局文件名 → Binding 类名的映射：
- `file_list_fragment.xml` → `FileListFragmentBinding`
- `text_editor_fragment.xml` → `TextEditorFragmentBinding`
- `name_dialog.xml` → `NameDialogBinding`

布局中的 ID → Binding 属性名的映射（下划线转驼峰）：
- `android:id="@+id/btn_save"` → `binding.btnSave`
- `android:id="@+id/recycler_view"` → `binding.recyclerView`
- `android:id="@+id/speed_dial"` → `binding.speedDial`

## 页面与布局文件的映射关系

在以下位置查找页面对应的布局文件：

### Activity
```kotlin
// 方式1：直接设置
setContentView(R.layout.xxx)

// 方式2：ViewBinding
val binding = XxxBinding.inflate(layoutInflater)
setContentView(binding.root)
```

### Fragment
```kotlin
// 方式1：构造函数
class XxxFragment : Fragment(R.layout.xxx)

// 方式2：onCreateView
override fun onCreateView(...): View {
    val binding = XxxBinding.inflate(inflater, container, false)
    return binding.root
}

// 方式3：inflate
inflater.inflate(R.layout.xxx, container, false)
```

### DialogFragment
```kotlin
// 方式1：onCreateView
override fun onCreateView(...): View {
    return inflater.inflate(R.layout.xxx, container, false)
}

// 方式2：onCreateDialog
override fun onCreateDialog(...): Dialog {
    return AlertDialog.Builder(context)
        .setView(R.layout.xxx)
        .create()
}
```
