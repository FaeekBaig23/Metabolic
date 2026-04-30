package com.faiqbaig.metabolic.feature.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // A state to remember which food we clicked on to show in the Bottom Sheet later
    var selectedFoodForLogging by remember { mutableStateOf<com.faiqbaig.metabolic.core.data.remote.UsdaFood?>(null) }

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

            // 2. The Search Bar
            FoodSearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClearClick = viewModel::clearSearch,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 3. Dynamic Content List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // padding for Bottom Nav
            ) {
                // If they are searching, show loading or results
                if (state.searchQuery.isNotEmpty()) {
                    if (state.isSearching) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (state.searchError != null) {
                        item {
                            Text(text = "Error: ${state.searchError}", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        items(state.searchResults) { food ->
                            SearchResultItem(
                                food = food,
                                onAddClick = {
                                    // We will open the Add Meal Bottom Sheet here next!
                                    selectedFoodForLogging = food
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
                // If they aren't searching, show their logged meals for today
                else {
                    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

                    mealTypes.forEach { type ->
                        val mealsForType = state.mealsByType[type] ?: emptyList()

                        item {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
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
    // ─── BOTTOM SHEET TRIGGER ───────────────────────────────────────────────
    selectedFoodForLogging?.let { food ->
        AddMealBottomSheet(
            food = food,
            onDismiss = { selectedFoodForLogging = null },
            onLogMeal = { name, cal, pro, carbs, fat, qty, unit, type ->
                viewModel.logMeal(name, cal, pro, carbs, fat, qty, unit, type)
            }
        )
    }
}