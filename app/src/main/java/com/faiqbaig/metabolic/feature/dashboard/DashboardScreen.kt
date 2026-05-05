package com.faiqbaig.metabolic.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
    onNavigateToCamera: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToBmi: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The subtle radial gradient glow behind the header
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
                currentRoute = "dashboard", // Hardcoded for this screen
                onHomeClick = { /* Already here */ },
                onTrackerClick = onNavigateToTracker,
                onPlansClick = onNavigateToPlans,
                onProfileClick = onNavigateToProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                shape = CircleShape,
                containerColor = MetabolicGreen,
                contentColor = Color.Black,
                modifier = Modifier.offset(y = 20.dp) // Sink it slightly into the nav bar
            ) {
                Text(text = "📷", fontSize = 24.sp)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = DarkBackground
    ) { paddingValues ->

        if (uiState.isLoading) {
            // Simple loading state
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
                        onProfileClick = onNavigateToProfile
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section B: Calorie Ring
                item {
                    CalorieRingCard(
                        // ── CHANGED: Now using live totalCalories from Room ──
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
                        // ── CHANGED: Now using live macro totals from Room ──
                        proteinConsumed = uiState.totalProtein, proteinTarget = uiState.proteinTarget,
                        carbsConsumed = uiState.totalCarbs, carbsTarget = uiState.carbsTarget,
                        fatConsumed = uiState.totalFat, fatTarget = uiState.fatTarget
                    )
                    Spacer(modifier = Modifier.height(32.dp))   // 24 to 32 test
                }

                // Section D: Quick Actions --- REMOVED --- !!!

                // Section E: Today's Meals
                item {
                    TodaysMealsSection(
                        // ── CHANGED: Passed emptyList() since we removed DummyMealLogs ──
                        meals = uiState.todaysMeals,
                        onSeeAllClick = onNavigateToTracker,
                        onLogMealClick = onNavigateToTracker
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section F: Water Tracker
                item {
                    WaterTrackerCard(
                        waterGlasses = uiState.waterGlasses,
                        onWaterToggle = viewModel::onWaterToggle
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section G: BMI Snapshot
                item {
                    BmiSnapshotCard(
                        bmi = uiState.bmi,
                        onTrackWeightClick = onNavigateToBmi
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section H: AI Chatbot Promo
                item {
                    AiPromoCard(onChatClick = onNavigateToChatbot)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section I: Gym Teaser
                item {
                    GymTeaserCard(onExploreClick = onNavigateToMap)
                    // Section J: Bottom Padding
                    Spacer(modifier = Modifier.height(40.dp))
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
        // Empty item to create space for the centered FAB
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { },
            enabled = false
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