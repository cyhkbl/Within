package io.github.cyhkbl.within.io

/** 最小可用的 CSV 读写：处理引号、逗号、换行。 */
object CsvUtils {

    fun escape(value: String): String {
        val needsQuote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuote) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    fun row(vararg cells: String): String = cells.joinToString(",") { escape(it) }

    /** 解析一整段文本为行列表，正确处理带引号的字段。 */
    fun parse(text: String): List<List<String>> {
        val rows = ArrayList<List<String>>()
        val current = ArrayList<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        val s = text.removePrefix("\uFEFF")
        while (i < s.length) {
            val c = s[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < s.length && s[i + 1] == '"') {
                            field.append('"')
                            i++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        field.append(c)
                    }
                }

                c == '"' -> inQuotes = true
                c == ',' -> {
                    current.add(field.toString())
                    field.clear()
                }

                c == '\r' -> Unit
                c == '\n' -> {
                    current.add(field.toString())
                    field.clear()
                    if (current.any { it.isNotBlank() }) rows.add(ArrayList(current))
                    current.clear()
                }

                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || current.isNotEmpty()) {
            current.add(field.toString())
            if (current.any { it.isNotBlank() }) rows.add(ArrayList(current))
        }
        return rows
    }
}
