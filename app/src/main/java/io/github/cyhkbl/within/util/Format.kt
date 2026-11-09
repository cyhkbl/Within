package io.github.cyhkbl.within.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    val zone: ZoneId = ZoneId.systemDefault()

    fun monthRange(ym: YearMonth): Pair<Long, Long> {
        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = ym.atEndOfMonth().atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun yearRange(year: Int): Pair<Long, Long> {
        val start = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.of(year, 12, 31).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun dayRange(date: LocalDate): Pair<Long, Long> {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun toLocalDate(ts: Long): LocalDate =
        Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()

    fun toLocalDateTime(ts: Long): LocalDateTime =
        Instant.ofEpochMilli(ts).atZone(zone).toLocalDateTime()

    fun nowLocalDateTime(): LocalDateTime = LocalDateTime.now(zone)

    fun fromLocalDateTime(dt: LocalDateTime): Long = dt.atZone(zone).toInstant().toEpochMilli()

    fun dayKey(ts: Long): String = toLocalDate(ts).toString()

    fun formatDayHeader(ts: Long): String {
        val date = toLocalDate(ts)
        val today = LocalDate.now()
        return when (date) {
            today -> "${date.monthValue}月${date.dayOfMonth}日 今天"
            today.minusDays(1) -> "${date.monthValue}月${date.dayOfMonth}日 昨天"
            else -> {
                val weekday = when (date.dayOfWeek.value) {
                    1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
                    5 -> "周五"; 6 -> "周六"; else -> "周日"
                }
                "${date.monthValue}月${date.dayOfMonth}日 $weekday"
            }
        }
    }

    fun formatTime(ts: Long): String =
        toLocalDateTime(ts).format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

    fun formatDateTime(ts: Long): String =
        toLocalDateTime(ts).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()))

    fun formatMonthLabel(ym: YearMonth): String = "${ym.year}年${ym.monthValue}月"
}

object MoneyUtils {

    fun format(cents: Long, withSign: Boolean = false, symbol: String = "¥"): String {
        val negative = cents < 0
        val abs = kotlin.math.abs(cents)
        val yuan = abs / 100
        val fen = (abs % 100).toInt()
        val body = String.format(Locale.getDefault(), "%,d.%02d", yuan, fen)
        val sign = when {
            negative -> "-"
            withSign -> "+"
            else -> ""
        }
        return "$sign$symbol$body"
    }

    fun formatPlain(cents: Long): String {
        val abs = kotlin.math.abs(cents)
        val yuan = abs / 100
        val fen = (abs % 100).toInt()
        return String.format(Locale.getDefault(), "%d.%02d", yuan, fen)
    }

    /** 把 "12.50" 这类输入转成分；非法返回 null。 */
    fun parseToCents(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val normalized = trimmed.replace(",", "")
        val value = normalized.toDoubleOrNull() ?: return null
        if (value < 0) return null
        return Math.round(value * 100)
    }

    fun parseExpression(input: String): Long? {
        if (input.isBlank()) return null
        val tokens = input.trim().split(Regex("\\s+"))
        if (tokens.size == 1) return parseToCents(tokens[0])
        var total = parseToCents(tokens[0]) ?: return null
        var i = 1
        while (i + 1 < tokens.size + 1 && i + 1 <= tokens.size) {
            val op = tokens[i]
            val operand = parseToCents(tokens.getOrNull(i + 1) ?: return total) ?: return total
            total = when (op) {
                "+" -> total + operand
                "-" -> total - operand
                else -> return total
            }
            i += 2
        }
        return total
    }
}
