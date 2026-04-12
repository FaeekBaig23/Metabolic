package com.faiqbaig.metabolic.feature.onboarding

data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String
)

val onboardingPages = listOf(
    OnboardingPage(
        title       = "Track Your Calories",
        description = "Log every meal effortlessly. Search from millions of foods or scan your plate with your camera for instant macro breakdown.",
        emoji       = "🍎"
    ),
    OnboardingPage(
        title       = "AI-Powered Plans",
        description = "Get personalized diet plans tailored to your goals — whether you want to lose weight, build muscle, or maintain a healthy lifestyle.",
        emoji       = "🤖"
    ),
    OnboardingPage(
        title       = "Monitor Your Health",
        description = "Track your BMI, weight progress, and daily nutrition trends. Visualize your journey with beautiful charts and insights.",
        emoji       = "📊"
    ),
    OnboardingPage(
        title       = "Find Fitness Centers",
        description = "Discover nearby gyms, yoga studios, and fitness centers on an interactive map. Your next workout is always close by.",
        emoji       = "📍"
    )
)