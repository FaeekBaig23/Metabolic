package com.faiqbaig.metabolic.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen
import androidx.compose.ui.Alignment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.faiqbaig.metabolic.feature.auth.SplashScreen
import com.faiqbaig.metabolic.feature.onboarding.OnboardingScreen

@Composable
fun MetabolicNavGraph(
    navController : NavHostController,
    modifier      : Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        modifier         = modifier
    ) {

        // ── Splash ───────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = { isFirstTime ->
                    val destination = if (isFirstTime)
                        Screen.Onboarding.route
                    else
                        Screen.Login.route

                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ───────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ────────────────────────────────────────────
        composable(Screen.Login.route) {
            // Placeholder — replaced in Step 3
            PlaceholderScreen(name = "Login")
        }

        // ── Register ─────────────────────────────────────────
        composable(Screen.Register.route) {
            PlaceholderScreen(name = "Register")
        }

        // ── Dashboard ────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            PlaceholderScreen(name = "Dashboard")
        }

        // ── Tracker ──────────────────────────────────────────
        composable(Screen.Tracker.route) {
            PlaceholderScreen(name = "Tracker")
        }

        // ── Camera ───────────────────────────────────────────
        composable(Screen.Camera.route) {
            PlaceholderScreen(name = "Camera")
        }

        // ── Plans ────────────────────────────────────────────
        composable(Screen.Plans.route) {
            PlaceholderScreen(name = "Plans")
        }

        // ── Chatbot ──────────────────────────────────────────
        composable(Screen.Chatbot.route) {
            PlaceholderScreen(name = "Chatbot")
        }

        // ── Map ──────────────────────────────────────────────
        composable(Screen.Map.route) {
            PlaceholderScreen(name = "Map")
        }

        // ── BMI ──────────────────────────────────────────────
        composable(Screen.Bmi.route) {
            PlaceholderScreen(name = "BMI")
        }

        // ── Profile ──────────────────────────────────────────
        composable(Screen.Profile.route) {
            PlaceholderScreen(name = "Profile")
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "$name — coming soon",
            color     = MetabolicGreen,
            fontSize  = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}