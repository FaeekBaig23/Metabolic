package com.faiqbaig.metabolic.feature.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector // ── CHANGED: Now uses crisp vector icons instead of text emojis ──
)

val onboardingPages = listOf(
    OnboardingPage(
        title       = "Track Your Calories",
        description = "Log every meal effortlessly. Search from millions of foods or scan your plate with your camera for instant macro breakdown.",
        icon        = Icons.Rounded.Restaurant
    ),
    OnboardingPage(
        title       = "AI-Powered Plans",
        description = "Get personalized diet plans tailored to your goals — whether you want to lose weight, build muscle, or maintain a healthy lifestyle.",
        icon        = Icons.Rounded.AutoAwesome
    ),
    OnboardingPage(
        title       = "Monitor Your Health",
        description = "Track your BMI, weight progress, and daily nutrition trends. Visualize your journey with beautiful charts and insights.",
        icon        = Icons.Rounded.Insights
    ),
    OnboardingPage(
        title       = "Track. Plan. Transform.",
        description = "Take control of your metabolism today. Build sustainable habits, crush your fitness goals, and unlock your best self.",
        icon        = Icons.Rounded.RocketLaunch
    )
)