package com.catto.scanfighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.catto.scanfighter.ui.navigation.ScanFighterNavigation
import com.catto.scanfighter.ui.theme.ScanFighterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanFighterTheme {
                ScanFighterNavigation()
            }
        }
    }
}
