package com.faiqbaig.metabolic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.faiqbaig.metabolic.core.navigation.MetabolicNavGraph
import com.faiqbaig.metabolic.core.ui.theme.MetabolicTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.Box

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install native splash screen first
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MetabolicTheme {
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    MetabolicNavGraph(navController = navController)
                }
            }
        }
    }
}