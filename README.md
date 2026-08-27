# WarehouseScanRecorder

**WarehouseScanRecorder** 是一款运行在 **Urovo DT50X PDA** 上的仓库扫描记录工具。

它可以在后台接收 PDA 扫描器产生的条码数据，并将扫描记录保存到本地数据库。扫描记录可以手动导出，也可以通过后台定时任务自动导出为 CSV 文件。

---

## 第一次使用 PDA 前

### 1. 开启开发者选项

在 PDA 上进入：

```text
设置
→ 关于手机
```

找到：

```text
版本号
Build Number
```

连续点击 **7 次左右**。

系统会提示开发者选项已经启用。

---

### 2. 打开 USB 调试

进入：

```text
设置
→ 系统
→ 开发者选项
```

找到：

```text
USB 调试
```

打开。

如果 PDA 弹出：

```text
是否允许 USB 调试？
```

选择：

```text
允许
```

如果有：

```text
始终允许使用这台计算机
```

可以勾选。

---

# 🔌 连接 PDA

使用 USB 数据线将 PDA 连接到 Windows 电脑。然后打开 Windows 命令提示符，进入项目目录：

```text
C:\AndroidProjects\WarehouseScanRecorder
```

运行：

```bat
adb devices
```

如果看到类似：

```text
List of devices attached
01182525999999    device
```

说明 PDA 已经成功连接。

---

# 🚀 使用 `update_app.bat`

进入项目目录：

```text
C:\AndroidProjects\WarehouseScanRecorder
```

直接运行：

```text
update_app.bat
```

脚本会自动执行安装/更新流程。

完成后，PDA 上应该可以看到：

```text
WarehouseScanRecorder
```
---

# 📱 首次启动

安装完成后，在 PDA 上打开：

```text
WarehouseScanRecorder
```

进入扫描界面。

首先设置：

```text
Operator Name
```

例如：

```text
LiHua
```

保存后，程序会记住操作员名称。

之后导出的文件会包含操作员名称。

例如：

```text
scan_records_LiHua_20260827_101735.csv
```

---

# 🔫 配置 PDA 扫描器

WarehouseScanRecorder 使用 PDA 的系统扫描器广播接收扫描结果。

在 Urovo DT50X 的扫描器设置中，需要更改扫描器输出方式为**按键和Intent同时输出**。

---

# 📡 后台扫描

> **App 不需要一直显示在屏幕前，也可以接收 PDA 扫描器产生的扫描数据。**

扫描流程：

```text
PDA 扫描器
     ↓
系统扫描广播
     ↓
ScanReceiver
     ↓
ScanRecordRepository
     ↓
Room Database
     ↓
保存扫描记录
```


---

# 💾 扫描记录

每次扫描会保存：

```text
条码
扫描时间
条码类型
```

其中：

* 条码类型保存在数据库中
* 当前 CSV 导出文件不再包含条码类型

CSV 当前格式为：

```text
序号,条码,扫描时间
```

例如：

```text
序号,条码,扫描时间
1,"E119151141101","2026-08-27 10:17:20"
2,"E119151141102","2026-08-27 10:17:35"
```

CSV 使用 UTF-8 BOM，以便 Windows Excel 正确识别中文。

---

# 📁 CSV 文件位置

扫描记录导出到：

```text
Download/WarehousePutaway/
```

例如：

```text
PDA
└── Download
    └── WarehousePutaway
        ├── scan_records_LiHua_20260827_101735.csv
        ├── scan_records_LiHua_20260827_131307.csv
        └── scan_records_LiHua_20260827_150012.csv
```

---

# 🔄 同步扫描记录

项目提供：

```text
sync_scan_records.bat
```

用于将 PDA 中的 CSV 扫描记录同步到电脑。

运行：

```bat
sync_scan_records.bat
```

脚本会：

1. 检查 ADB
2. 检查 PDA 是否连接
3. 检查扫描记录目录
4. 查找 CSV 文件
5. 根据文件名识别 Operator Name
6. 创建对应的操作员文件夹
7. 将 CSV 文件复制到电脑
8. 跳过已经同步的文件

---

# 🧹 清除 PDA 上的扫描记录

项目还提供：

```text
clear_scan_records.bat
```

用于删除 PDA：

```text
Download/WarehousePutaway/
```

中的扫描记录 CSV。

⚠️ **注意：**

执行这个脚本会删除 PDA 上现有的扫描记录文件。

建议在执行前先使用：

```text
sync_scan_records.bat
```

将记录同步到电脑。

---

# ⏰ 自动导出

程序包含后台定时导出机制。

使用 Android WorkManager 执行后台任务。

程序可以在后台定期检查扫描记录，并将记录导出为 CSV。

后台任务还负责清理符合保留期限条件的旧导出文件。

因此，即使员工没有手动点击“导出”，后台任务仍然可以完成定时导出。

---

# 🛠️ 项目结构

主要目录：

```text
WarehouseScanRecorder/
│
├── app/
│   └── src/
│       └── main/
│           └── java/
│               └── com/anmei/warehouseputaway/
│
├── gradle/
│
├── update_app.bat
├── sync_scan_records.bat
├── clear_scan_records.bat
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

主要功能模块：

```text
scanner/
    扫描器广播接收

data/
    数据保存、导出

data/local/database/
    Room 数据库

data/preferences/
    Operator Name 等设置

ui/
    用户界面

work/
    后台定时任务

receiver/
    系统启动相关广播
```

---

# 🔧 开发环境

如果需要修改源代码，需要安装 Android 开发环境。

项目目前使用 Android：

```text
compileSdk = 36
targetSdk = 36
minSdk = 30
```

目标设备：

```text
Urovo DT50X
Android 11
API 30
```

项目使用：

```text
Kotlin
Jetpack Compose
Hilt
Room
WorkManager
KSP
```

---

# ❗ 常见问题

## PDA 无法连接

运行：

```bat
adb devices
```

如果没有显示 PDA：

1. 检查 USB 数据线
2. 检查 PDA USB 模式
3. 确认 USB 调试已经开启
4. 重新连接 USB
5. 查看 PDA 是否出现“允许 USB 调试”提示

---

## `adb devices` 显示 `unauthorized`

例如：

```text
01182525040358    unauthorized
```

解锁 PDA 屏幕。

如果出现：

```text
Allow USB debugging?
```

点击：

```text
Allow
```

然后重新运行：

```bat
adb devices
```

---

## 扫描了条码，但是程序没有记录

首先确认：

```text
Scanner Settings
→ Intent Output
```

已经启用。

确认 Action：

```text
android.intent.ACTION_DECODE_DATA
```

然后确认扫描器输出的字段包含：

```text
barcode_string
```

如果仍然没有记录，可以检查：

```text
adb logcat
```

查看 ScanReceiver 相关日志。

---

## App 在后台时还能记录吗？

可以。

只要 PDA 扫描器仍然正常发送广播，WarehouseScanRecorder 的扫描接收组件可以在 App UI 没有显示时接收扫描结果。

因此：

```text
App UI 没打开
```

不等于：

```text
扫描记录功能停止
```

---

# ⚠️ 重要提示

### 不要直接删除数据库文件

扫描记录使用 Room 数据库保存。

不要手动删除：

```text
/data/data/...
```

中的数据库文件。

如需清理导出的 CSV，请使用：

```text
clear_scan_records.bat
```

---

# 📄 License

本项目的具体使用和分发权限以仓库中的 License 文件为准。

如果仓库没有单独提供 License，则默认不授予第三方复制、修改或重新分发本项目源代码的权利。
