# 账本（Ledger）

对标「钱迹」的 Android 记账应用，核心差异化是**支付完成后自动弹出预填好的记账卡片**。

## 当前状态

能编译出 debug APK（约 17 MB），**尚未在真机上台过**（开发过程中手机断连）。第一优先级的验证是装上去跑一遍主流程。

## 技术栈

| 项 | 版本 |
|---|---|
| Kotlin | AGP 9 内置（不再单独挂 KGP） |
| AGP | 9.5.1 |
| Gradle | 9.9（wrapper） |
| Compose BOM | 2026.09.00（Compose 1.12） |
| Material3 | 1.5.0-rc01（官方 Expressive API） |
| Room | 2.8.5（KSP 2.3.13） |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |

依赖走阿里云镜像（Maven Central 对 Kotlin 编译器会 301 到 GitHub，而 GitHub 经代理常 504）。

AGP 9 的两处构建方式变化：
- 内置 Kotlin 支持，**不能**再应用 `org.jetbrains.kotlin.android`，否则构建直接失败；
- Compose 编译器插件仍需显式声明 `org.jetbrains.kotlin.plugin.compose`。

## 已实现

**记账核心**
- 首页：月支出/收入/结余/总资产卡片、最近 7 日柱状、按日分组的账单流
- 记一笔：支出/收入/转账切换（分类区随收支类型刷新）、分类网格、自绘金额键盘、备注、账户/时间
- 账单详情：金额、分类、账户、时间、备注、来源、删除
- 退款：对支出账单部分/全额退款，生成关联的收入记录，支持撤销、多笔累加
- 统计：本月总支出、分类排行（条形占比）
- 资产：账户列表与净资产
- 日历视图：月网格每日收支小计，点某天展开当天账单
- 我的：设置入口列表

**导入导出**
- 导出 CSV，列顺序按钱迹习惯：`时间,类型,金额,分类,账户,备注,标签,币种,手续费`
- 导入按表头名字匹配（不依赖列顺序），分类和账户不存在时自动创建
- 走 SAF 选文件，导入前有预览和条数确认

**自动记账**
- `PaymentNotificationService`：读微信/支付宝/淘宝/京东/美团/饿了么/拼多多的通知
- `PaymentAccessibilityService`：在支付结果页读「支付成功」等强信号（兜底）
- `PaymentParser`：金额正则、收支方向判断、商户提取、指纹去重（同来源同金额同商户同分钟只算一次）
- `AutoRecordActivity`：底部确认卡，金额可改、分类可换、账户可换、备注可改，一键确认入账
- `AutoLogSettingsActivity`：三权限检查（通知使用权/无障碍/悬浮窗）+ 跳系统设置 + 测试触发

**数据**
- 6 张表：books / accounts / categories / transactions / budgets / tags
- 金额统一用 `Long` 存「分」，避免浮点误差
- 写账单时在同一事务里维护账户余额
- 首次启动写入默认分类（22 个支出 + 7 个收入）与 4 个账户

## Material 3 Expressive

严格走官方实现：`MaterialExpressiveTheme` + `ExperimentalMaterial3ExpressiveApi`（material3 1.5.0-rc01）。

为了用上它，整条链必须一起升：
- Compose 1.12（BOM 2026.09.00）
- AGP 9.5.1（9.0 起内置 Kotlin）
- Gradle 9.9
- compileSdk / targetSdk 37（平台包实际叫 `platforms;android-37.1` 和 `37.2`）
- build-tools 37.0.0

## 构建

```powershell
$env:JAVA_HOME = "D:\software\Scoop\apps\temurin21-jdk\current"
$env:ANDROID_HOME = "D:\software\Android\Sdk"
$env:GRADLE_USER_HOME = "D:\software\gradle-home"
.\gradlew.bat :app:assembleDebug --console=plain
# 产物 app\build\outputs\apk\debug\app-debug.apk
```

## 待办（按优先级）

1. **真机验证主流程**（装、记一笔、看首页、点详情、删除）
2. 自动记账真机联调：开三权限 → 用测试按钮验证弹窗 → 真实支付验证
3. 分类管理（增删改、二级分类）
4. 预算（月/年总预算 + 分类预算 + 日均）
5. 周期记账（房租、订阅）
6. 导入导出（支付宝/微信账单 CSV）
7. 多账本、标签、报销、退款
8. 桌面小组件、日历视图
9. 可选：工具链升级到官方 Expressive API
