package com.faiqbaig.metabolic.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

// ── Theme Imports ──
import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import com.faiqbaig.metabolic.core.ui.theme.DarkTextPrimary
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel : AuthViewModel = hiltViewModel()
) {
    // ── Animation Triggers ──
    var iconVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var versionVisible by remember { mutableStateOf(false) }

    // ── Observe the destination calculated by the ViewModel ──
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        delay(150) // Smooth handoff from native splash screen
        iconVisible = true

        delay(400) // Stagger the word reveal
        textVisible = true

        delay(300) // Stagger the version number
        versionVisible = true

        delay(1500) // Wait for the beautiful animations to finish

        // Navigate directly to whatever the ViewModel decided
        startDestination?.let { route ->
            onNavigate(route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground), // Solid color prevents flashing from native splash
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-24).dp) // Pushes it slightly up for optical balance
        ) {
            // 1. The 'M' Logo Animation
            AnimatedVisibility(
                visible = iconVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 }
            ) {
                Text(
                    text = "M",
                    color = MetabolicGreen,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. The "METABOLIC" Word Animation
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 }
            ) {
                Text(
                    text = "METABOLIC",
                    color = DarkTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp // Wide tracking looks very premium
                )
            }
        }

        // 3. Keep your version number (fades in quietly at the bottom)
        AnimatedVisibility(
            visible = versionVisible,
            enter = fadeIn(tween(1000)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text     = "v1.6.5",
                fontSize = 12.sp,
                color    = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}