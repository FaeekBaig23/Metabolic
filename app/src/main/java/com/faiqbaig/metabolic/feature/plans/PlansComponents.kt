package com.faiqbaig.metabolic.feature.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity

val MetabolicGreen = Color(0xFF00C896)
val MetabolicCyan = Color(0xFF00A0C8)
val DarkSurface = Color(0xFF121F1B)
val DarkSurfaceVariant = Color(0xFF1C2E28)
val DarkTextPrimary = Color(0xFFE8F5F0)
val DarkTextSecondary = Color(0xFF8FBFB0)
val MacroProtein = Color(0xFF00C896)
val MacroCarbs = Color(0xFF00A0C8)
val MacroFat = Color(0xFFFFBA49)
val MacroCalories = Color(0xFFFF6B6B)

@Composable
fun EmptyPlanState(
    isGenerating: Boolean,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📅", fontSize = 64.sp, modifier = Modifier.padding(bottom = 16.dp))
        Text("No plan yet", color = DarkTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text("Generate a personalized 7-day meal plan based on your goals and preferences.", color = DarkTextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 32.dp))

        Button(
            onClick = onGenerateClick,
            enabled = !isGenerating,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen, disabledContainerColor = MetabolicGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = DarkSurface, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Generating your plan...", color = DarkSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            } else {
                Text("✦ Generate My Plan", color = DarkSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DaySelector(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(days) { index, day ->
            val isSelected = index == selectedDayIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MetabolicGreen else DarkSurfaceVariant)
                    .clickable { onDaySelected(index) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = day,
                    color = if (isSelected) DarkSurface else DarkTextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun PlanMealCard(
    meal: DietPlanMealEntity,
    onLogMeal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(MetabolicGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = meal.mealType, color = MetabolicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = "${meal.calories} kcal", color = MacroCalories, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food Name
            Text(text = meal.foodName, color = DarkTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "~${meal.estimatedWeightG} g", color = DarkTextSecondary, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Macros
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroText("Protein", "${meal.protein}g", MacroProtein)
                MacroText("Carbs", "${meal.carbs}g", MacroCarbs)
                MacroText("Fat", "${meal.fat}g", MacroFat)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THE FIX: Stripped down, simple, error-free button.
            Button(
                onClick = onLogMeal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("Log this Meal", color = DarkSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MacroText(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = DarkTextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun RegenerateDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceVariant,
        title = { Text("Regenerate Plan?", color = DarkTextPrimary) },
        text = { Text("This will permanently replace your current 7-day meal plan. Do you want to continue?", color = DarkTextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Yes, Regenerate", color = MetabolicGreen, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = DarkTextPrimary) }
        }
    )
}

@Composable
fun GeneratingOverlay() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(DarkSurface).padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MetabolicGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cooking up your plan...", color = DarkTextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}