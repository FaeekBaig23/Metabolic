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
import kotlinx.coroutines.delay

val DarkBackground = Color(0xFF0A1612)

@Composable
fun PlansScreen(
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showErrorDialog by remember { mutableStateOf(false) }
    var displayErrorMessage by remember { mutableStateOf("") }

    // ── NEW: State controls for the 2-second 100% delay ──
    var forceLoadingState by remember { mutableStateOf(false) }
    var isRegeneratingFlag by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }

    val handleGenerate = {
        forceLoadingState = true
        isRegeneratingFlag = false
        isComplete = false
        viewModel.onGeneratePlan()
    }

    val handleRegenerate = {
        forceLoadingState = true
        isRegeneratingFlag = true
        isComplete = false
        viewModel.onRegenerateConfirmed()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { rawError ->
            val isTrafficError = rawError.contains("503") ||
                    rawError.contains("timeout", ignoreCase = true) ||
                    rawError.contains("demand", ignoreCase = true)

            displayErrorMessage = if (isTrafficError) {
                "Metabolic is currently handling a high volume of requests. Please wait a few moments and try generating your plan again!"
            } else {
                "We encountered a hiccup while crafting your plan. Please check your connection and try again."
            }

            showErrorDialog = true
            forceLoadingState = false // Immediately drop loading screen on error
            isComplete = false
            viewModel.clearError()
        }
    }

    // ── NEW: Intercept the end of generation to hold at 100% for 2 seconds ──
    LaunchedEffect(uiState.isGenerating) {
        if (!uiState.isGenerating && forceLoadingState) {
            delay(100) // Brief pause to see if an error caused it to stop
            if (!showErrorDialog) {
                isComplete = true // Push the bar to 100%
                delay(2000L) // Hold the screen for 2 seconds
                viewModel.onDaySelected(0)
                forceLoadingState = false // Now drop the loading screen
                isComplete = false
            }
        }
    }

    LaunchedEffect(uiState.hasPlan) {
        // Ensures Day 0 is selected if the user opens the app with a saved plan
        if (uiState.hasPlan && !forceLoadingState) {
            viewModel.onDaySelected(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {

        // Use our local force loading state to decide what to show
        val showEmpty = (!uiState.hasPlan && !forceLoadingState) || (forceLoadingState && !isRegeneratingFlag)
        val showOverlay = forceLoadingState && isRegeneratingFlag

        if (showEmpty) {
            EmptyPlanState(
                isGenerating = forceLoadingState,
                isComplete = isComplete,
                onGenerateClick = handleGenerate
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
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

        if (uiState.isRegenerateDialogVisible) {
            RegenerateDialog(
                onConfirm = handleRegenerate, // Pass the hijacked UI trigger
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

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                containerColor = DarkSurface,
                title = { Text("Taking a quick breather", color = DarkTextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text(displayErrorMessage, color = DarkTextSecondary) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("Got it", color = MetabolicGreen, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showOverlay) {
            GeneratingOverlay(isComplete = isComplete)
        }
    }
}