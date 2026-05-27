package com.faiqbaig.metabolic.feature.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    onNavigateToGemini: () -> Unit,
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 1. The Summary Bar
            DailySummaryBar(
                calories = state.totalCalories,
                protein = state.totalProtein,
                carbs = state.totalCarbs,
                fat = state.totalFat,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 2. The AI Logging Button (Navigates to the Gemini Scanner)
            Button(
                onClick = onNavigateToGemini,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Scanner"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log a meal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // 3. Dynamic Content List (Logged Meals)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // padding for Bottom Nav
            ) {
                val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

                mealTypes.forEach { type ->
                    val mealsForType = state.mealsByType[type] ?: emptyList()

                    item {
                        Text(
                            text = type,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                        )
                    }

                    if (mealsForType.isEmpty()) {
                        item {
                            Text(
                                text = "No meals logged yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    } else {
                        items(mealsForType) { meal ->
                            MealLogRow(
                                meal = meal,
                                onDeleteClick = { viewModel.deleteMeal(meal.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}