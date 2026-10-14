# Growth Score Android DEMO

这是一个不连接服务器、不包含账号体系的 Android 横屏交互 DEMO。数据完全在内存中，重启应用会恢复默认模拟数据。

## 使用 Android Studio 打开

1. 使用 Android Studio 打开 `android-demo` 目录。
2. 等待 Gradle 依据 `settings.gradle.kts` 同步依赖。
3. 使用 API 26 或以上的横屏模拟器运行 `app`。

## 已验证的设计范围

- 首页 Dashboard、成绩录入、综合/单科分析、我的/目标管理
- 8 次综合考试与 10 次单科测试模拟数据
- 得分率、等级、趋势、稳定性和目标差距的本地计算

当前设备未安装 JDK、Android SDK、Gradle 或模拟器，因此尚不能在本机完成 APK 构建与安装验证。
