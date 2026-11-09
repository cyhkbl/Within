package io.github.cyhkbl.within

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.cyhkbl.within.ui.LedgerRoot
import io.github.cyhkbl.within.ui.theme.LedgerTheme
import io.github.cyhkbl.within.ui.theme.ThemePrefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemePrefs.init(this)
        val repo = (application as LedgerApp).repo
        setContent {
            val mode by ThemePrefs.mode.collectAsState()
            val dynamic by ThemePrefs.dynamicColor.collectAsState()
            val darkTheme = when (mode) {
                ThemePrefs.MODE_LIGHT -> false
                ThemePrefs.MODE_DARK -> true
                else -> isSystemInDarkTheme()
            }
            LedgerTheme(darkTheme = darkTheme, dynamicColor = dynamic) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LedgerRoot(repo)
                }
            }
        }
    }
}
