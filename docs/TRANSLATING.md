# 翻译指南

界面上的每一句文案都外置在 Android 标准的字符串资源里，所以添加一种语言不需要改任何 Kotlin 代码。

## 现状

| 语言 | 文件 | 状态 |
|---|---|---|
| 简体中文（默认） | `app/src/main/res/values/strings.xml` | 完整 |
| English | `app/src/main/res/values-en/strings.xml` | 完整 |

## 添加一种新语言

以日语（`ja`）为例：

```bash
cp app/src/main/res/values/strings.xml app/src/main/res/values-ja/strings.xml
```

然后逐条翻译。语言代码用 Android 认可的形式：`ja`、`ko`、`fr`、`de`、`zh-rTW`（台湾正体）、`pt-rBR`（巴西葡语）等。

翻译完直接提 PR 即可，不需要改代码。

## 必须遵守的规则

### 1. 占位符原样保留

```xml
<string name="io_status_exported">已导出 %1$d 条账单到「下载/%2$s」</string>
```

`%1$d` 是整数，`%2$s` 是字符串。翻译时**只能调整它们的位置和前后文字，不能删、不能改名、不能改数量**。如果语序需要调整，可以换位：

```xml
<!-- 英文 -->
<string name="io_status_exported">Exported %1$d entries to “Downloads/%2$s”</string>
```

参数按编号匹配，所以换位是安全的。

### 2. XML 转义

文本里的 `<`、`&` 要写成 `&lt;`、`&amp;`。单双引号不用转义，但如果字符串以引号开头或结尾，需要用反斜杠或者外层引号包裹。

中文里的「」在其它语言里建议换成该语言惯用的引号，不要保留。

### 3. 不要翻译 string 的 name

`name` 是代码引用的 key，翻译时保持不变。

### 4. 长度

移动端空间有限。按钮、导航标签这类短文案尽量和原文长度接近；说明性长句不受限制。

### 5. 复数

目前还没有用到 `<plurals>`。如果某个语言对「1 条」和「2 条」有词形变化（俄语、波兰语等），提 issue 说明，我们可以把那几条改成 `<plurals>` 形式。

## 如何检查自己的翻译

```bash
# 编译，缺漏或格式错误会直接报错
./gradlew :app:assembleDebug
```

Android 在编译时会检查：

- 是否有未翻译的 key（不会报错，但会显示成中文）
- 占位符数量是否与默认语言一致（会报错）

运行后把系统语言切到目标语言，逐屏检查即可。

## 关于 Weblate / Crowdin

本项目使用标准的 Android 资源文件，可以直接接入 Weblate、Crowdin、Crowdin 的 GitHub 集成或 Transifex，不需要改代码。

如果你维护翻译平台并且想让本仓库自动同步，提 issue 说明平台和接入方式即可。

## 不要动的东西

- `values/strings.xml` 里的 `app_name` 是默认语言的应用名（量入为出），各语言可以有自己的译名（英文是 Within）。
- 数据层的字符串不算界面文案，不在翻译范围内：分类名（存在数据库里）、CSV 表头、支付通知的匹配关键词。
