package io.github.cyhkbl.within.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 外观偏好：主题模式（跟随系统/浅色/深色）和动态取色。
 * 用 SharedPreferences 而不是 DataStore，因为要在 Activity 创建的第一帧就能读到值。
 */
object ThemePrefs {

    const val MODE_SYSTEM = "system"
    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"

    private const val NAME = "ledger_theme"
    private var prefs: SharedPreferences? = null

    private val _mode = MutableStateFlow(MODE_SYSTEM)
    val mode: StateFlow<String> = _mode

    private val _dynamicColor = MutableStateFlow(true)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        prefs = p
        _mode.value = p.getString("mode", MODE_SYSTEM) ?: MODE_SYSTEM
        _dynamicColor.value = p.getBoolean("dynamic", true)
    }

    fun setMode(mode: String) {
        prefs?.edit()?.putString("mode", mode)?.apply()
        _mode.value = mode
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs?.edit()?.putBoolean("dynamic", enabled)?.apply()
        _dynamicColor.value = enabled
    }
}
