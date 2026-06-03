package com.faiqbaig.metabolic.feature.plans

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity
import kotlinx.coroutines.delay

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
fun AnimatedPlanLoadingBar(modifier: Modifier = Modifier, isComplete: Boolean = false) {
    val messages = listOf(
        "Analyzing profile...",
        "Checking goals and preferences...",
        "Evaluating macros...",
        "Finalizing meal options...",
        "Organizing final plan..."
    )

    var messageIndex by remember { mutableIntStateOf(0) }

    // ── THE LOGIC FIX: 20% chunks ──
    // Index 0 = 0%, Index 1 = 20%, Index 4 = 80%.
    // When isComplete fires, it overrides to 100% (1f).
    val targetProgress = messageIndex * 0.2f
    val currentProgress = if (isComplete) 1f else targetProgress
    val displayMessage = if (isComplete) "Meal Plan Ready!" else messages[messageIndex]

    // ── THE ANIMATION FIX: Snappier 2-second chunks instead of a 2.5s crawl ──
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        val totalSteps = messages.size
        for (i in 0 until totalSteps) {
            messageIndex = i
            delay(6000L)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(
            targetState = displayMessage,
            animationSpec = tween(500),
            label = "textTransition"
        ) { currentMessage ->
            Text(
                text = currentMessage,
                color = DarkTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.CenterStart
        ) {
            // Wait to draw the gradient until we actually have progress > 0
            if (animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(MetabolicGreen, MetabolicCyan)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun EmptyPlanState(
    isGenerating: Boolean,
    isComplete: Boolean = false,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "Calendar",
            tint = MetabolicGreen,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(64.dp)
        )

        Text("No plan yet", color = DarkTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Text("Generate a personalized daily meal plan based on your goals and preferences.", color = DarkTextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 32.dp))

        if (isGenerating) {
            AnimatedPlanLoadingBar(modifier = Modifier.padding(top = 16.dp), isComplete = isComplete)
        } else {
            Button(
                onClick = onGenerateClick,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("✦ Generate My Plan", color = DarkSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(MetabolicGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = meal.mealType, color = MetabolicGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = "${meal.calories} kcal", color = MacroCalories, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = meal.foodName, color = DarkTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "~${meal.estimatedWeightG} g", color = DarkTextSecondary, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroText("Protein", "${meal.protein}g", MacroProtein)
                MacroText("Carbs", "${meal.carbs}g", MacroCarbs)
                MacroText("Fat", "${meal.fat}g", MacroFat)
            }

            Spacer(modifier = Modifier.height(16.dp))

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
        text = { Text("This will permanently replace your current daily meal plan. Do you want to continue?", color = DarkTextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Yes, Regenerate", color = MetabolicGreen, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = DarkTextPrimary) }
        }
    )
}

@Composable
fun GeneratingOverlay(isComplete: Boolean = false) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(DarkSurface).padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            AnimatedPlanLoadingBar(isComplete = isComplete)
        }
    }
}