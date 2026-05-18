package com.faiqbaig.metabolic.feature.plans

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Assume DarkBackground is defined in your theme
val DarkBackground = Color(0xFF0A1612)

@Composable
fun PlansScreen(
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Error handling (Toast)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // THE FIX: Removed "&& !uiState.isGenerating" so the empty state stays visible to show its loading button!
        if (!uiState.hasPlan) {
            EmptyPlanState(
                isGenerating = uiState.isGenerating,
                onGenerateClick = viewModel::onGeneratePlan
            )
        } else {
            // Changed to a simple 'else' since if they DO have a plan, we show the list
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your 7-Day Plan",
                            color = DarkTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.generatedOnLabel,
                            color = DarkTextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "↻ Regenerate",
                        color = MetabolicGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.onRegenerateClick() }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day Selector
                DaySelector(
                    selectedDayIndex = uiState.selectedDayIndex,
                    onDaySelected = viewModel::onDaySelected
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Meals List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Padding for bottom nav bar
                ) {
                    items(uiState.selectedDayMeals) { meal ->
                        PlanMealCard(
                            meal = meal,
                            onLogMeal = {
                                viewModel.onLogMeal(meal)
                                Toast.makeText(context, "Logged to ${meal.mealType}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Overlays & Dialogs
        if (uiState.isRegenerateDialogVisible) {
            RegenerateDialog(
                onConfirm = viewModel::onRegenerateConfirmed,
                onDismiss = viewModel::onRegenerateDismissed
            )
        }

        if (uiState.isGenerating && uiState.hasPlan) {
            GeneratingOverlay()
        }
    }
}