# 超越 Android 正式版

这是一个不连接服务器、不包含账号体系的 Android 自适应成绩分析应用。学生资料、学科、目标和新增成绩均保存在设备本地；首次启动提供示例数据。

## 使用 Android Studio 打开

1. 使用 Android Studio 打开 `android-demo` 目录。
2. 等待 Gradle 依据 `settings.gradle.kts` 同步依赖。
3. 使用 API 26 或以上的 Android 模拟器或真机运行 `app`，横竖屏均可使用。

## 已验证的设计范围

- 首页、成绩录入、综合/单科分析、我的/目标管理
- 学生资料与默认/自定义学科设置，本地持久化
- 自适应横竖屏布局、启动页、应用图标与帮助中心
- GitHub Releases 更新检查；下载和安装确认遵循 Android 系统权限机制

发布前请执行 `gradlew.bat assembleRelease`，使用签名 APK 并在目标设备上完成安装验证。
