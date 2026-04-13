package com.faiqbaig.metabolic.feature.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: (isFirstTime: Boolean) -> Unit,
    viewModel       : AuthViewModel = hiltViewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue   = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, easing = EaseInOut),
        label         = "alpha"
    )
    val scaleAnim = animateFloatAsState(
        targetValue   = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(1000, easing = EaseOutBack),
        label         = "scale"
    )
    val taglineAlpha = animateFloatAsState(
        targetValue   = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, delayMillis = 600, easing = EaseIn),
        label         = "tagline"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2800)

        // Check login state + onboarding state
        val isLoggedIn     = viewModel.isLoggedIn
        val isOnboarded    = viewModel.isOnboardingCompleted()

        when {
            isLoggedIn  -> onSplashFinished(false)  // go to Dashboard
            isOnboarded -> onSplashFinished(false)  // go to Login
            else        -> onSplashFinished(true)   // go to Onboarding
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF0D2A20))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "Track. Plan. Transform.",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = MetabolicGreen.copy(alpha = 0.7f),
                textAlign  = TextAlign.Center,
                modifier   = Modifier.alpha(taglineAlpha.value)
            )
        }
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