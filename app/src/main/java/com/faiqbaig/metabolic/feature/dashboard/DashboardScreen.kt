package com.faiqbaig.metabolic.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
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
import com.faiqbaig.metabolic.core.ui.theme.DarkSurfaceVariant
import com.faiqbaig.metabolic.core.ui.theme.DarkTextSecondary
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        weightKg = uiState.weightKg,
                        heightCm = uiState.heightCm,
                        onTrackWeightClick = onNavigateToBmi
                    )
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
    onChatClick: () -> Unit,
    onPlansClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        // ── CHANGED: Using DarkSurfaceVariant for a lighter contrast against the background ──
        containerColor = DarkSurfaceVariant,
        tonalElevation = 8.dp
    ) {
        val selectedColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MetabolicGreen,
            unselectedIconColor = DarkTextSecondary,
            selectedTextColor = MetabolicGreen,
            unselectedTextColor = DarkTextSecondary,
            indicatorColor = MetabolicGreen.copy(alpha = 0.15f)
        )

        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = onHomeClick,
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = selectedColors
        )
        NavigationBarItem(
            selected = currentRoute == "tracker",
            onClick = onTrackerClick,
            icon = { Icon(Icons.Rounded.Restaurant, contentDescription = "Tracker", modifier = Modifier.size(24.dp)) },
            label = { Text("Meals", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = selectedColors
        )
        NavigationBarItem(
            selected = currentRoute == "chatbot",
            onClick = onChatClick,
            icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = "Chat", modifier = Modifier.size(24.dp)) },
            label = { Text("Chat", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = selectedColors
        )
        NavigationBarItem(
            selected = currentRoute == "plans",
            onClick = onPlansClick,
            icon = { Icon(Icons.Rounded.DateRange, contentDescription = "Plans", modifier = Modifier.size(24.dp)) },
            label = { Text("Plans", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = selectedColors
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = { Icon(Icons.Rounded.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
            label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
            colors = selectedColors
        )
    }
}