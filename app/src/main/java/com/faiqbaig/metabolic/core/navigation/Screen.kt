package com.faiqbaig.metabolic.core.navigation

sealed class Screen(val route: String) {

    // ── Auth flow ────────────────────────────────────────────
    data object Splash      : Screen("splash")
    data object Onboarding  : Screen("onboarding")
    data object Login       : Screen("login")
    data object Register    : Screen("register")

    // ── Main app flow ────────────────────────────────────────
    data object Dashboard   : Screen("dashboard")
    data object Tracker     : Screen("tracker")
    data object Camera      : Screen("camera")
    data object Plans       : Screen("plans")
    data object Chatbot     : Screen("chatbot")
    data object Map         : Screen("map")
    data object Bmi         : Screen("bmi")
    data object Profile     : Screen("profile")

    // ── Routes with arguments ────────────────────────────────
    data object FoodDetail  : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: String) = "food_detail/$foodId"
    }
    data object PlanDetail  : Screen("plan_detail/{planId}") {
        fun createRoute(planId: String) = "plan_detail/$planId"
    }
}