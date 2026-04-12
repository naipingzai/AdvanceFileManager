# Skill: filejob — 文件操作任务

## 概述
`naipingzai.materialfile.filejob` 实现后台文件操作，通过前台服务运行。

## FileJob 子类

| 类 | 操作 |
|----|------|
| CopyFileJob | 复制文件 |
| MoveFileJob | 移动文件 |
| DeleteFileJob | 删除文件 |
| ArchiveFileJob | 创建归档 |
| CreateFileJob | 创建文件/目录 |
| ExtractFileJob | 解压归档 |
| RestoreFileJob | 从回收站恢复 |
| InstallApkJob | 安装 APK |

## FileJobService

| 方法 | 说明 |
|------|------|
| `onStartCommand()` | 启动文件任务 |
| `archive/copy/move/delete/create/...()` | 各操作工厂方法 |

## 测试策略
- 文件操作: Instrumented Test (需要真实文件系统)
- 测试创建/删除/复制/移动基本流程
