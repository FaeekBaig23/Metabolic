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
import com.faiqbaig.metabolic.feature.bmi.BmiScreen
import com.faiqbaig.metabolic.feature.camera.GeminiScreen
import com.faiqbaig.metabolic.feature.plans.PlansScreen
import com.faiqbaig.metabolic.feature.profile_view.EditProfileScreen
import com.faiqbaig.metabolic.feature.profile_view.ProfileScreen
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
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // ── Register ─────────────────────────────────────────
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
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
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ────────────────────────────────────────
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTracker = {
                    navController.navigate(Screen.Tracker.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToChatbot = {
                    navController.navigate(Screen.Chatbot.route) {
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
                onNavigateToBmi = { navController.navigate(Screen.Bmi.route) }
            )
        }

        // ── Tracker ──────────────────────────────────────────
        composable(Screen.Tracker.route) {
            TrackerScreen(
                onNavigateToGemini = { navController.navigate("gemini_screen") }
            )
        }

        // ── Gemini (Formerly Camera) ─────────────────────────
        composable("gemini_screen") {
            val trackerViewModel: com.faiqbaig.metabolic.feature.tracker.TrackerViewModel = hiltViewModel()

            GeminiScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogMeal = { analysis, mealType ->
                    trackerViewModel.logMeal(
                        foodName = analysis.foodName,
                        calories = analysis.calories,
                        protein = analysis.protein.toInt(),
                        carbs = analysis.carbs.toInt(),
                        fat = analysis.fat.toInt(),
                        servingQty = analysis.estimatedWeightG.toFloat(),
                        servingUnit = "g",
                        mealType = mealType
                    )
                    navController.popBackStack()
                }
            )
        }

        // ── Plans ────────────────────────────────────────────
        composable(route = Screen.Plans.route) {
            PlansScreen()
        }

        // ── Chatbot ──────────────────────────────────────────
        composable(Screen.Chatbot.route) {
            com.faiqbaig.metabolic.feature.chatbot.ChatbotScreen()
        }

        // ── BMI ──────────────────────────────────────────────
        composable(route = Screen.Bmi.route) {
            BmiScreen()
        }

        // ── Profile (FIXED: Single block with both parameters) ──
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToEditProfile = {
                    navController.navigate("edit_profile")
                }
            )
        }

        // ── Edit Profile ────────────────────────────────────
        composable(route = "edit_profile") {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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