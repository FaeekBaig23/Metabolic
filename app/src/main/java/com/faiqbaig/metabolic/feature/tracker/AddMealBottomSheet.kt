package com.faiqbaig.metabolic.feature.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.faiqbaig.metabolic.core.data.remote.UsdaFood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealBottomSheet(
    food: UsdaFood,
    onDismiss: () -> Unit,
    onLogMeal: (foodName: String, calories: Int, protein: Int, carbs: Int, fat: Int, servingQty: Float, servingUnit: String, mealType: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // USDA nutrient IDs: 1008=Calories, 1003=Protein, 1005=Carbs, 1004=Fat
    val baseCalories = food.foodNutrients.find { it.nutrientId == 1008 }?.value ?: 0f
    val baseProtein = food.foodNutrients.find { it.nutrientId == 1003 }?.value ?: 0f
    val baseCarbs = food.foodNutrients.find { it.nutrientId == 1005 }?.value ?: 0f
    val baseFat = food.foodNutrients.find { it.nutrientId == 1004 }?.value ?: 0f

    var servingQtyStr by remember { mutableStateOf("1.0") }
    val servingQty = servingQtyStr.toFloatOrNull() ?: 0f

    var selectedMealType by remember { mutableStateOf("Breakfast") }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp) // Extra padding for system nav bars
        ) {
            Text(text = "Log ${food.description}", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // Live Macro Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cal: ${(baseCalories * servingQty).toInt()}", style = MaterialTheme.typography.bodyMedium)
                Text("Pro: ${(baseProtein * servingQty).toInt()}g", style = MaterialTheme.typography.bodyMedium)
                Text("Carbs: ${(baseCarbs * servingQty).toInt()}g", style = MaterialTheme.typography.bodyMedium)
                Text("Fat: ${(baseFat * servingQty).toInt()}g", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = servingQtyStr,
                onValueChange = { servingQtyStr = it },
                label = { Text("Number of Servings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Meal Type", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            // Meal Type Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                mealTypes.forEach { type ->
                    FilterChip(
                        selected = selectedMealType == type,
                        onClick = { selectedMealType = type },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onLogMeal(
                        food.description,
                        (baseCalories * servingQty).toInt(),
                        (baseProtein * servingQty).toInt(),
                        (baseCarbs * servingQty).toInt(),
                        (baseFat * servingQty).toInt(),
                        servingQty,
                        "serving",
                        selectedMealType
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = servingQty > 0f // Prevent saving 0 servings
            ) {
                Text("Save to Diary")
            }
        }
    }
}