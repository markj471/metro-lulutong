# 武汉地铁路路通

一个基于 Kotlin 和 Jetpack Compose 开发的武汉地铁查询与换乘规划 Android 应用，同时作为 Android 功能测试与自动化测试实践项目。

## 项目亮点

- 武汉地铁线路、站点、英文站名和运营时间查询
- 站点搜索、搜索历史、热门搜索和收藏
- 起终点选择、路线规划、换乘站展示和票价估算
- 本地账号登录、注册、修改密码和退出登录
- 浅色、深色、跟随系统和字体大小设置
- 地铁线路图缩放与边界限制
- 本地 JSON 数据，核心查询和换乘功能无需服务器即可运行

## 测试实践

- 按需求拆分登录、线路、站点、换乘、设置、地图、权限和兼容性测试点
- 设计功能、异常、边界、权限和兼容性测试用例 66 条
- 使用 Kotlin 编写 Jetpack Compose UI 自动化脚本
- Pixel_7a AVD（Android 14）上执行 22 个 UI 自动化测试方法，22 个通过
- JUnit 单元测试 9 套、24 项通过
- 按用例编号去重，66 条设计用例中实际执行 32 条，31 条通过、1 条失败
- 记录并跟踪 1 个一般/P2 问题：首次启动定位权限弹窗早于登录页

## 技术栈

- Kotlin
- Jetpack Compose、Material 3
- Navigation Compose、StateFlow
- JUnit、Compose UI Test、ActivityScenario
- Gradle、Android Studio、ADB
- SharedPreferences
- 本地 JSON 数据
- Dijkstra 路线规划算法

## 运行项目

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 运行 `app` 配置到 Android 模拟器或 Android 手机。
4. 测试账号：`admin`，密码：`123456`。

`local.properties` 由 Android Studio 根据本机 SDK 路径自动生成，不应提交到仓库。

## 构建 APK

Windows:

```text
gradlew.bat assembleDebug
```

运行单元测试：

```text
gradlew.bat testDebugUnitTest
```

连接 Android 模拟器后运行 UI 自动化测试：

```text
gradlew.bat connectedDebugAndroidTest
```

## 数据说明

项目内置武汉地铁基础数据，主要功能可以离线运行。本项目使用本地模拟登录，未接入真实后端服务。
