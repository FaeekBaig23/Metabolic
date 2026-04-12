package com.faiqbaig.metabolic.feature.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreenDark
import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: (isFirstTime: Boolean) -> Unit
) {
    // ── Animation states ─────────────────────────────────────
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue    = if (startAnimation) 1f else 0f,
        animationSpec  = tween(durationMillis = 1000, easing = EaseInOut),
        label          = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue    = if (startAnimation) 1f else 0.7f,
        animationSpec  = tween(durationMillis = 1000, easing = EaseOutBack),
        label          = "scale"
    )

    val taglineAlpha = animateFloatAsState(
        targetValue    = if (startAnimation) 1f else 0f,
        animationSpec  = tween(
            durationMillis = 800,
            delayMillis    = 600,
            easing         = EaseIn
        ),
        label          = "tagline"
    )

    // ── Trigger animation + navigate after delay ─────────────
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2800)
        // TODO: Replace with real first-launch check from DataStore
        onSplashFinished(true)
    }

    // ── UI ───────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF0D2A20)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // App name with scale + fade animation
            Text(
                text       = "Metabolic",
                fontSize   = 52.sp,
                fontWeight = FontWeight.Bold,
                color      = MetabolicGreen,
                textAlign  = TextAlign.Center,
                modifier   = Modifier
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline fades in slightly after the title
            Text(
                text      = "Track. Plan. Transform.",
                fontSize  = 15.sp,
                fontWeight = FontWeight.Medium,
                color     = MetabolicGreen.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.alpha(taglineAlpha.value)
            )
        }

        // Version tag at bottom
        Text(
            text     = "v1.0.0",
            fontSize = 12.sp,
            color    = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha.value)
        )
    }
}