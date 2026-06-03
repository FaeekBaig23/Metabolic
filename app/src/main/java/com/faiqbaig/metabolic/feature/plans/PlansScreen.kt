package com.faiqbaig.metabolic.feature.plans

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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

val DarkBackground = Color(0xFF0A1612)

@Composable
fun PlansScreen(
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.hasPlan) {
        if (uiState.hasPlan) {
            // Force the ViewModel to look at Day 0, where all our newly generated meals are!
            viewModel.onDaySelected(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        if (!uiState.hasPlan) {
            EmptyPlanState(
                isGenerating = uiState.isGenerating,
                onGenerateClick = viewModel::onGeneratePlan
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Daily Plan",
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↻ Regenerate",
                            color = MetabolicGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.onRegenerateClick() }
                                .padding(8.dp)
                        )
                        IconButton(onClick = viewModel::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Plan",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // The DaySelector was removed here!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
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

        if (uiState.isDeleteDialogVisible) {
            AlertDialog(
                onDismissRequest = viewModel::onDeleteDismissed,
                containerColor = DarkSurface,
                title = { Text("Delete Diet Plan", color = DarkTextPrimary) },
                text = { Text("Are you sure you want to delete your daily meal plan?", color = DarkTextSecondary) },
                confirmButton = {
                    TextButton(onClick = viewModel::onDeleteConfirmed) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onDeleteDismissed) {
                        Text("Cancel", color = MetabolicGreen)
                    }
                }
            )
        }

        if (uiState.isGenerating && uiState.hasPlan) {
            GeneratingOverlay()
        }
    }
}