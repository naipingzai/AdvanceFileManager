# Skill: provider — 文件系统 Provider

## 概述
`naipingzai.materialfile.provider` 实现 Java NIO2 FileSystemProvider 接口。

## Provider 实现

| Provider | 包 | 用途 |
|----------|------|------|
| LinuxFileSystemProvider | linux/ | 直接 syscall 操作本地文件 |
| ArchiveFileSystemProvider | archive/ | 通过 libarchive 读写归档 |
| ContentFileSystemProvider | content/ | ContentResolver 访问 |
| DocumentFileSystemProvider | document/ | SAF 访问 |
| RootFileSystemProvider | root/ | Root/Shizuku 远程 IPC |

## common/ 公共接口

| 类/接口 | 方法 |
|---------|------|
| PosixFileAttributeView | `readAttributes()`, `setPermissions()`, `setOwner()`, `setGroup()` |
| PosixFileStore | `getTotalSpace()`, `getUsableSpace()`, `isReadOnly()` |
| PathObservable | `observe()` — inotify 文件监听 |

## 测试策略
- Linux Provider: Instrumented Test (需要真实文件系统)
- Archive Provider: Instrumented Test (创建测试归档)
- Content/Document: Mock 或跳过
