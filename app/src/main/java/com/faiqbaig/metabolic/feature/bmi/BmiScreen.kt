package com.faiqbaig.metabolic.feature.bmi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BmiScreen(
    viewModel: BmiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("BMI & Weight Log", color = DarkTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. Snapshot Dial
            item {
                BmiDialCard(
                    bmi = uiState.currentBmi,
                    category = uiState.bmiCategory
                )
            }

            // 2. Input Form
            item {
                WeightLogForm(
                    weight = uiState.weightInputField,
                    note = uiState.noteField,
                    isSaving = uiState.isSaving,
                    onWeightChange = viewModel::onWeightInputChange,
                    onNoteChange = viewModel::onNoteChange,
                    onSave = viewModel::onLogWeight
                )

                uiState.error?.let { errorMsg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg, color = SemanticError, fontSize = 14.sp)
                }
            }

            // 3. History Section Header & Filters
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Progress History", color = DarkTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                HistoryFilterToggle(
                    currentFilter = uiState.historyFilter,
                    onFilterSelected = viewModel::onFilterChange
                )
            }

            // 4. Line Chart
            item {
                WeightHistoryChart(dataPoints = uiState.chartDataPoints)
            }

            // 5. Scrollable List of Past Entries
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Logs", color = DarkTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (uiState.weightLogs.isEmpty()) {
                item {
                    Text("No logs found for this period.", color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                items(uiState.weightLogs, key = { it.id }) { log ->
                    WeightLogRow(
                        entry = log,
                        onDelete = { viewModel.onDeleteLog(log) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp)) // Bottom padding for nav bar clearance
                }
            }
        }
    }
}