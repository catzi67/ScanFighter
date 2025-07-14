package com.catto.scanfighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.catto.scanfighter.ui.navigation.ScanFighterNavigation
import com.catto.scanfighter.ui.theme.ScanFighterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ScanFighterTheme {
                ScanFighterNavigation()
            }
            //
        }
    }
}
