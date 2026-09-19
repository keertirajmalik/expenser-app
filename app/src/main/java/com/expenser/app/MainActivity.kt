package com.expenser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.expenser.app.ui.nav.ExpenserNavHost
import com.expenser.app.ui.theme.ExpenserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ExpenserTheme {
                ExpenserNavHost()
            }
        }
    }
}
