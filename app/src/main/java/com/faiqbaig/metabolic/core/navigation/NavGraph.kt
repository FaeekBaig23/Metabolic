package com.faiqbaig.metabolic.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen
import androidx.compose.ui.Alignment

import androidx.hilt.navigation.compose.hiltViewModel
import com.faiqbaig.metabolic.feature.auth.AuthViewModel

import androidx.navigation.NavGraph.Companion.findStartDestination

import com.faiqbaig.metabolic.feature.auth.LoginScreen
import com.faiqbaig.metabolic.feature.auth.RegisterScreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.faiqbaig.metabolic.feature.auth.SplashScreen
import com.faiqbaig.metabolic.feature.onboarding.OnboardingScreen
import com.faiqbaig.metabolic.feature.profile.ProfileSetupScreen
import com.faiqbaig.metabolic.feature.dashboard.DashboardScreen

// ── NEW: Import your Tracker Screen ──
import com.faiqbaig.metabolic.feature.tracker.TrackerScreen

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
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Clear the splash screen from the backstack so they can't return to it
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
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                // ── Add this missing callback back in! ──
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // ── Register ─────────────────────────────────────────
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Route to ProfileSetup instead of Dashboard
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ── Profile Setup ───────────────────────────────────
        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        // Clear the backstack so they can't go back to setup
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ────────────────────────────────────────
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                // Bottom Navigation Tabs (with state preservation)
                onNavigateToTracker = {
                    navController.navigate(Screen.Tracker.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToPlans = {
                    navController.navigate(Screen.Plans.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },

                // Standard Screens (FAB and Cards)
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToChatbot = { navController.navigate(Screen.Chatbot.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToBmi = { navController.navigate(Screen.Bmi.route) }
            )
        }

        // ── Tracker ──────────────────────────────────────────
        composable(Screen.Tracker.route) {
            // ── CHANGED: Replaced the Placeholder with the real screen ──
            TrackerScreen()
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