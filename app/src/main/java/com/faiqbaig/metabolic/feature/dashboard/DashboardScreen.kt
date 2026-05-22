package com.faiqbaig.metabolic.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import com.faiqbaig.metabolic.core.ui.theme.DarkSurface
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen

@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToBmi: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profileImageUri by viewModel.profileImageUri.collectAsState()

    val backgroundBrush = remember {
        Brush.radialGradient(
            colors = listOf(
                MetabolicGreen.copy(alpha = 0.07f),
                Color.Transparent
            ),
            center = Offset(x = 500f, y = 0f),
            radius = 800f
        )
    }

    Scaffold(
        bottomBar = {
            MetabolicBottomNav(
                currentRoute = "dashboard",
                onHomeClick = { },
                onTrackerClick = onNavigateToTracker,
                onChatClick = onNavigateToChatbot,
                onPlansClick = onNavigateToPlans,
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = MetabolicGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(paddingValues),
                contentPadding = PaddingValues(top = 24.dp)
            ) {
                // Section A: Header
                item {
                    DashboardHeader(
                        greeting = uiState.greeting,
                        goal = uiState.goal,
                        userName = uiState.userName,
                        profileImageUri = profileImageUri,
                        onProfileClick = onNavigateToProfile
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section B: Calorie Ring
                item {
                    CalorieRingCard(
                        caloriesConsumed = uiState.totalCalories,
                        caloriesRemaining = uiState.caloriesRemaining,
                        dailyCalorieTarget = uiState.dailyCalorieTarget,
                        progressFraction = uiState.calorieProgressFraction
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section C: Macro Breakdown
                item {
                    MacroBreakdownCard(
                        proteinConsumed = uiState.totalProtein, proteinTarget = uiState.proteinTarget,
                        carbsConsumed = uiState.totalCarbs, carbsTarget = uiState.carbsTarget,
                        fatConsumed = uiState.totalFat, fatTarget = uiState.fatTarget
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Section E: Today's Meals
                item {
                    TodaysMealsSection(
                        meals = uiState.todaysMeals,
                        onSeeAllClick = onNavigateToTracker,
                        onLogMealClick = onNavigateToTracker
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section F: Water Tracker
                item {
                    WaterTrackerCard(
                        waterConsumedMl = uiState.waterConsumedMl,
                        waterTargetMl = uiState.dailyWaterTargetMl,
                        onAddWater = viewModel::addWater
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section G: BMI Snapshot (Expanded)
                item {
                    BmiSnapshotCard(
                        bmi = uiState.bmi,
                        weightKg = uiState.weightKg, // Make sure your DashboardUiState exposes this!
                        heightCm = uiState.heightCm, // Make sure your DashboardUiState exposes this!
                        onTrackWeightClick = onNavigateToBmi
                    )
                    Spacer(modifier = Modifier.height(40.dp)) // The padding closes out the screen cleanly
                }
            }
        }
    }
}

@Composable
private fun MetabolicBottomNav(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onTrackerClick: () -> Unit,
    onChatClick: () -> Unit,
    onPlansClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = onHomeClick,
            icon = { Text("🏠", fontSize = 20.sp) },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MetabolicGreen.copy(alpha = 0.2f))
        )
        NavigationBarItem(
            selected = currentRoute == "tracker",
            onClick = onTrackerClick,
            icon = { Text("🍽️", fontSize = 20.sp) },
            label = { Text("Tracker", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MetabolicGreen.copy(alpha = 0.2f))
        )
        NavigationBarItem(
            selected = currentRoute == "chatbot",
            onClick = onChatClick,
            icon = { Text("✨", fontSize = 20.sp) },
            label = { Text("Chat", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MetabolicGreen.copy(alpha = 0.2f))
        )
        NavigationBarItem(
            selected = currentRoute == "plans",
            onClick = onPlansClick,
            icon = { Text("📅", fontSize = 20.sp) },
            label = { Text("Plans", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MetabolicGreen.copy(alpha = 0.2f))
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = { Text("👤", fontSize = 20.sp) },
            label = { Text("Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = MetabolicGreen.copy(alpha = 0.2f))
        )
    }
}