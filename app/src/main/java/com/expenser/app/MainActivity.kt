package com.expenser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expenser.app.data.model.ThemeMode
import com.expenser.app.ui.nav.ExpenserNavHost
import com.expenser.app.ui.theme.ExpenserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as ExpenserApp).container
        setContent {
            val saved by container.themeMode.collectAsStateWithLifecycle()
            val preview by container.previewTheme.collectAsStateWithLifecycle()
            val mode = preview ?: saved
            val darkTheme = when (mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            ExpenserTheme(darkTheme = darkTheme) {
                ExpenserNavHost()
            }
        }
    }
}
