# Reaction Camera - 轻量级 Android 反应视频录制 App

一个简单、独立的 Reaction Camera 应用。

## 核心设计原则

- **上下分屏**：上半部分播放参考视频，下半部分摄像头预览
- **完全独立**：参考视频播放与摄像头录制互不影响
- **只保存摄像头视频**：最终文件只包含你的反应画面 + 声音，不合成参考视频
- **无网络、无账号、无服务器**

## 技术栈

- **Kotlin**
- **Jetpack Compose**（UI）
- **CameraX**（摄像头预览 + 录制）
- **Media3 ExoPlayer**（参考视频播放）
- **Accompanist Permissions**（权限处理）

## 功能

1. 选择本地参考视频
2. 独立播放 / 暂停参考视频
3. 前置 / 后置摄像头切换
4. 开始 / 停止录制（只录摄像头画面 + 麦克风）
5. 录制计时显示
6. 视频自动保存到相册 `Movies/ReactionCamera/` 目录

## 如何运行

1. 用 **Android Studio**（推荐最新稳定版）打开本项目根目录 `ReactionCamera`
2. 等待 Gradle 同步完成
3. 连接真机（推荐，模拟器摄像头支持有限）或使用模拟器
4. 点击 Run

### 最低要求

- minSdk 26（Android 8.0）
- 推荐真机测试摄像头和录音功能

## 项目结构

```
ReactionCamera/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/reactioncamera/
│   │   │   ├── MainActivity.kt
│   │   │   └── ui/
│   │   │       ├── ReactionScreen.kt          # 主界面（上下分屏）
│   │   │       ├── ReferenceVideoPlayer.kt    # 上半部分参考视频
│   │   │       ├── CameraRecorder.kt          # 下半部分摄像头录制
│   │   │       └── theme/Theme.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 使用流程

1. 打开 App → 授予「摄像头」和「麦克风」权限
2. 点击上半部分「选择视频」→ 从相册选择参考视频
3. 点击播放按钮 → **仅播放参考视频**
4. 点击下半部分红色录制按钮 → **开始录制摄像头**
5. 录制过程中可随时暂停/继续参考视频，不影响录制
6. 再次点击停止录制 → 视频自动保存到相册

## 后续可扩展功能

- 调整上下分屏比例（50/50、40/60 等）
- 左右分屏模式
- 录制倒计时（3/5/10 秒）
- 参考视频音量独立控制
- 录制完成后直接分享到 TikTok / Instagram / YouTube
- 可选「合成 Reaction 视频」功能（后期）

## 注意事项

- 第一次运行请在真机上测试
- 录制的视频保存在系统相册的 `Movies/ReactionCamera` 文件夹
- 参考视频完全不会进入最终录制文件

---

基于原始需求设计，第一版专注核心功能：独立参考视频播放 + 独立摄像头录制 + 只保存摄像头视频。
