package io.github.cyhkbl.within.io

import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 极简 xlsx 读取器。
 *
 * xlsx 就是一个 zip：`xl/sharedStrings.xml` 存字符串池，`xl/worksheets/sheet1.xml` 存单元格，
 * 单元格里 `t="s"` 表示值是字符串池下标。这里只做「按行还原成二维字符串表」这一件事，
 * 不处理公式、样式、合并单元格——记账账单用不到。
 */
object XlsxReader {

    private const val NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"

    fun read(input: InputStream): List<List<String>> {
        val entries = HashMap<String, ByteArray>()
        ZipInputStream(input.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name
                    if (name == "xl/sharedStrings.xml" || name.startsWith("xl/worksheets/sheet")) {
                        entries[name] = zip.readBytes()
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val shared = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) } ?: emptyList()

        // 取第一个 worksheet（sheet1.xml，或任何 sheet*.xml 中名字最小的那个）
        val sheetKey = entries.keys
            .filter { it.startsWith("xl/worksheets/sheet") }
            .minByOrNull { it }

        val sheetBytes = sheetKey?.let { entries[it] } ?: return emptyList()
        return parseSheet(sheetBytes, shared)
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val doc = newBuilder().parse(ByteArrayInputStream(bytes))
        val out = ArrayList<String>()
        val siList = doc.getElementsByTagNameNS(NS, "si")
        for (i in 0 until siList.length) {
            val si = siList.item(i) as? Element ?: continue
            val tList = si.getElementsByTagNameNS(NS, "t")
            val sb = StringBuilder()
            for (j in 0 until tList.length) {
                sb.append(tList.item(j).textContent ?: "")
            }
            out.add(sb.toString())
        }
        return out
    }

    private fun parseSheet(bytes: ByteArray, shared: List<String>): List<List<String>> {
        val doc = newBuilder().parse(ByteArrayInputStream(bytes))
        val rows = doc.getElementsByTagNameNS(NS, "row")
        val result = ArrayList<List<String>>(rows.length)

        for (r in 0 until rows.length) {
            val row = rows.item(r) as? Element ?: continue
            val cells = row.getElementsByTagNameNS(NS, "c")
            val map = HashMap<Int, String>()
            var maxIndex = -1
            for (c in 0 until cells.length) {
                val cell = cells.item(c) as? Element ?: continue
                val ref = cell.getAttribute("r")
                val colIndex = columnIndex(ref)
                if (colIndex < 0) continue
                val value = cellValue(cell, shared)
                map[colIndex] = value
                if (colIndex > maxIndex) maxIndex = colIndex
            }
            val line = ArrayList<String>(maxIndex + 1)
            for (i in 0..maxIndex) line.add(map[i] ?: "")
            result.add(line)
        }
        return result
    }

    private fun cellValue(cell: Element, shared: List<String>): String {
        val type = cell.getAttribute("t")
        return when (type) {
            "s" -> {
                val v = cell.getElementsByTagNameNS(NS, "v").item(0)?.textContent
                val index = v?.toIntOrNull() ?: return ""
                shared.getOrElse(index) { "" }
            }

            "inlineStr" -> {
                val isNode = cell.getElementsByTagNameNS(NS, "is").item(0) as? Element ?: return ""
                val tList = isNode.getElementsByTagNameNS(NS, "t")
                val sb = StringBuilder()
                for (i in 0 until tList.length) sb.append(tList.item(i).textContent ?: "")
                sb.toString()
            }

            else -> cell.getElementsByTagNameNS(NS, "v").item(0)?.textContent ?: ""
        }
    }

    /** "AB12" -> 27（从 0 开始的列号）；解析失败返回 -1。 */
    private fun columnIndex(ref: String): Int {
        var i = 0
        var result = 0
        var found = false
        while (i < ref.length) {
            val ch = ref[i]
            if (ch in 'A'..'Z') {
                result = result * 26 + (ch - 'A' + 1)
                found = true
            } else if (ch in 'a'..'z') {
                result = result * 26 + (ch - 'a' + 1)
                found = true
            } else {
                break
            }
            i++
        }
        return if (found) result - 1 else -1
    }

    private fun newBuilder() =
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
        }.newDocumentBuilder()
}
