package com.faiqbaig.metabolic.feature.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.faiqbaig.metabolic.core.data.local.MealLogEntity

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

// Imports for CalorieRing and MacroBar
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import java.text.NumberFormat
import androidx.compose.ui.graphics.drawscope.rotate

// TODO: Ensure these imports match your actual theme package
import com.faiqbaig.metabolic.core.ui.theme.DarkTextPrimary
import com.faiqbaig.metabolic.core.ui.theme.DarkTextSecondary
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreenDark
import com.faiqbaig.metabolic.core.ui.theme.MacroProtein
import com.faiqbaig.metabolic.core.ui.theme.MacroCarbs
import com.faiqbaig.metabolic.core.ui.theme.MacroFat
import com.faiqbaig.metabolic.core.ui.theme.DarkSurface
import com.faiqbaig.metabolic.core.ui.theme.DarkSurfaceVariant
import com.faiqbaig.metabolic.core.ui.theme.DarkBorder
import com.faiqbaig.metabolic.core.ui.theme.MetabolicCyan
import com.faiqbaig.metabolic.core.ui.theme.MacroCalories

import com.faiqbaig.metabolic.core.ui.theme.SemanticWarning
import com.faiqbaig.metabolic.core.ui.theme.SemanticError

// Section A: Header

@Composable
fun DashboardHeader(
    greeting: String,
    goal: String,
    userName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamically format today's date (e.g., "Saturday, April 18")
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    // Extract the first letter for the avatar, default to "U" if empty
    val initial = userName.firstOrNull()?.uppercase() ?: "U"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Date, Greeting, and Goal Badge
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentDate,
                color = DarkTextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = greeting,
                color = DarkTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Goal Badge
            Surface(
                color = MetabolicGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "🎯 $goal",
                    color = MetabolicGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Right Side: User Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MetabolicGreenDark)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// Section B: Calorie Ring Card

@Composable
fun CalorieRingCard(
    caloriesConsumed: Int,
    caloriesRemaining: Int,
    dailyCalorieTarget: Int,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    // Animate the ring fill from 0 to the actual progress
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progressFraction) {
        animatedProgress.animateTo(
            targetValue = progressFraction,
            animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
        )
    }

    // Number formatter for commas (e.g., "1,240")
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Center: Circular Progress Ring
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 14.dp.toPx()
                    // Adjust size so the stroke doesn't bleed outside the canvas bounds
                    val diameter = size.minDimension - strokeWidthPx
                    val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                    val arcSize = Size(diameter, diameter)

                    // 1. Draw the empty background track
                    drawArc(
                        color = DarkSurfaceVariant,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx)
                    )

                    // 2. Draw the animated gradient progress arc
                    // Starting at -90f places the start of the arc exactly at the top center
                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(MetabolicGreen, MetabolicCyan)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }

                // Text inside the ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = numberFormat.format(caloriesConsumed),
                        color = DarkTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "kcal consumed",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Row: Stats Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    icon = "🔥",
                    label = "Burned",
                    value = "0 kcal" // Stubbed for now as per spec
                )
                StatColumn(
                    icon = "🍽️",
                    label = "Remaining",
                    value = "${numberFormat.format(caloriesRemaining)} kcal"
                )
                StatColumn(
                    icon = "🎯",
                    label = "Target",
                    value = "${numberFormat.format(dailyCalorieTarget)} kcal"
                )
            }
        }
    }
}

// Helper composable for the three stats at the bottom of the card
@Composable
private fun StatColumn(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$icon $value",
            color = DarkTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = DarkTextSecondary,
            fontSize = 11.sp
        )
    }
}

// Section C: Macro Breakdown Bar

@Composable
fun MacroBreakdownCard(
    proteinConsumed: Int, proteinTarget: Int,
    carbsConsumed: Int, carbsTarget: Int,
    fatConsumed: Int, fatTarget: Int,
    modifier: Modifier = Modifier
) {
    // Single animation driver for the main segmented bar
    val animationProgress = remember { Animatable(0f) }

    // Boolean trigger for the individual sub-bars in the legend
    var startAnimation by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 1. The Main Segmented Bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                // Draw the empty background track
                drawRect(color = DarkSurfaceVariant, size = size)

                val totalConsumed = proteinConsumed + carbsConsumed + fatConsumed
                if (totalConsumed > 0) {
                    val pRatio = proteinConsumed.toFloat() / totalConsumed
                    val cRatio = carbsConsumed.toFloat() / totalConsumed
                    val fRatio = fatConsumed.toFloat() / totalConsumed

                    // Calculate widths based on the current animation frame
                    val animatedTotalWidth = size.width * animationProgress.value
                    val pWidth = animatedTotalWidth * pRatio
                    val cWidth = animatedTotalWidth * cRatio
                    val fWidth = animatedTotalWidth * fRatio

                    var startX = 0f

                    if (pWidth > 0) {
                        drawRect(color = MacroProtein, topLeft = Offset(startX, 0f), size = Size(pWidth, size.height))
                        startX += pWidth
                    }
                    if (cWidth > 0) {
                        drawRect(color = MacroCarbs, topLeft = Offset(startX, 0f), size = Size(cWidth, size.height))
                        startX += cWidth
                    }
                    if (fWidth > 0) {
                        drawRect(color = MacroFat, topLeft = Offset(startX, 0f), size = Size(fWidth, size.height))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. The Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroLegendItem(
                    name = "Protein",
                    consumed = proteinConsumed,
                    target = proteinTarget,
                    color = MacroProtein,
                    startAnimation = startAnimation,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                MacroLegendItem(
                    name = "Carbs",
                    consumed = carbsConsumed,
                    target = carbsTarget,
                    color = MacroCarbs,
                    startAnimation = startAnimation,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                MacroLegendItem(
                    name = "Fat",
                    consumed = fatConsumed,
                    target = fatTarget,
                    color = MacroFat,
                    startAnimation = startAnimation,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroLegendItem(
    name: String,
    consumed: Int,
    target: Int,
    color: Color,
    startAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    // Calculate individual macro progress
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f

    // Animate the sub-bar filling up
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnimation) progress else 0f,
        animationSpec = tween(durationMillis = 900, easing = EaseOutCubic),
        label = "${name}ProgressAnimation"
    )

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                color = DarkTextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${consumed}g / ${target}g",
            color = DarkTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4dp height Sub-progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DarkSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedProgress.coerceAtLeast(0f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

// Section D: Quick Action Row --- REMOVED --- !!!

// Section E: Today's Meals Section

// ── NEW SMART MEALS SECTION ──
@Composable
fun TodaysMealsSection(
    meals: List<MealLogEntity>,
    onSeeAllClick: () -> Unit,
    onLogMealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine which meals the user has logged today (case-insensitive)
    val loggedTypes = meals.map { it.mealType.lowercase() }.toSet()
    val hasBreakfast = "breakfast" in loggedTypes
    val hasLunch = "lunch" in loggedTypes
    val hasDinner = "dinner" in loggedTypes
    val hasSnack = "snack" in loggedTypes

    // Generate the smart conversational message based on what's missing
    val (icon, message) = when {
        meals.isEmpty() ->
            "🍽️" to "No meals logged yet.\nLet's get started!"
        hasBreakfast && hasLunch && hasDinner && hasSnack ->
            "🏆" to "All meals logged for the day.\nGreat job!"
        hasBreakfast && hasLunch && hasDinner ->
            "🍎" to "Main meals logged.\nDon't forget to log your snacks!"
        hasBreakfast && hasLunch ->
            "🌙" to "Breakfast and lunch logged.\nYou have not logged your dinner yet."
        hasBreakfast ->
            "☀️" to "Breakfast logged.\nDon't forget to log your lunch!"
        hasLunch ->
            "⚠️" to "Lunch logged, but you missed breakfast!"
        else ->
            "👍" to "You've logged ${meals.size} items today.\nKeep it up!"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Meals",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All →",
                color = MetabolicGreen,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // The Smart Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = icon, fontSize = 36.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = DarkTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // The Persistent Log Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onLogMealClick() },
                    color = MetabolicGreen
                ) {
                    Text(
                        text = "➕ Log a meal",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

// Section F: Water Intake Tracker

@Composable
fun WaterTrackerCard(
    waterConsumedMl: Int,
    waterTargetMl: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state to remember which glass size the user has selected
    var selectedVolume by remember { mutableStateOf(250) }
    val volumeOptions = listOf(100, 250, 500)

    // Calculate fill percentage and animate it
    val fillFraction = if (waterTargetMl > 0) {
        (waterConsumedMl.toFloat() / waterTargetMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedFill by animateFloatAsState(
        targetValue = fillFraction,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "WaterTankAnimation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "💧 Hydration",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // LEFT: The Vertical Water Tank
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(2.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // The animated water fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction = animatedFill)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MetabolicCyan, Color(0xFF0277BD)) // Cyan to deeper blue
                                )
                            )
                    )

                    // Percentage text overlay
                    Text(
                        text = "${(fillFraction * 100).toInt()}%",
                        color = if (fillFraction > 0.15f) Color.White else DarkTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // RIGHT: The Controls
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Glass Size",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Glass Size Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        volumeOptions.forEach { volume ->
                            val isSelected = selectedVolume == volume
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedVolume = volume },
                                color = if (isSelected) MetabolicCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) MetabolicCyan else DarkBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${volume}ml",
                                        color = if (isSelected) MetabolicCyan else DarkTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Add Water Button
                    Button(
                        onClick = { onAddWater(selectedVolume) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MetabolicCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "➕ Add ${selectedVolume}ml",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BOTTOM: Textual Message
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$waterConsumedMl ml / $waterTargetMl ml consumed today",
                    color = DarkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// Section G: BMI Snapshot Card (Redesigned with Semicircle Gauge)

@Composable
fun BmiSnapshotCard(
    bmi: Double,
    onTrackWeightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (categoryText, categoryColor) = when {
        bmi == 0.0 -> "No data" to DarkSurfaceVariant
        bmi < 18.5 -> "Underweight" to MetabolicCyan
        bmi < 25.0 -> "Normal weight" to MetabolicGreen
        bmi < 30.0 -> "Overweight" to SemanticWarning
        else -> "Obese" to SemanticError
    }

    val formattedBmi = if (bmi > 0) String.format(Locale.US, "%.1f", bmi) else "--"

    // Calculate the target angle for the needle (180 degrees total sweep)
    // 15 BMI = 180° (far left), 35 BMI = 360°/0° (far right)
    val targetAngle = if (bmi > 0) {
        val fraction = ((bmi - 15.0) / 20.0).coerceIn(0.0, 1.0).toFloat()
        180f + (fraction * 180f)
    } else {
        180f // Rest at the far left if no data
    }

    // Animate the needle
    val animatedAngle = remember { Animatable(180f) }
    LaunchedEffect(bmi) {
        animatedAngle.animateTo(
            targetValue = targetAngle,
            animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚖️ Body Mass Index",
                    color = DarkTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Update →",
                    color = DarkTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onTrackWeightClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // The Semicircle Gauge
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(110.dp), // Height is exactly half the width for a perfect semicircle
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 16.dp.toPx()
                    val arcSize = Size(size.width, size.width) // Use width for both to ensure a perfect circle

                    // The gauge arcs
                    // 18.5 BMI is ~31.5 degrees sweep
                    drawArc(color = MetabolicCyan, startAngle = 180f, sweepAngle = 31.5f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))
                    // 25.0 BMI is ~58.5 degrees sweep
                    drawArc(color = MetabolicGreen, startAngle = 211.5f, sweepAngle = 58.5f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))
                    // 30.0 BMI is 45 degrees sweep
                    drawArc(color = SemanticWarning, startAngle = 270f, sweepAngle = 45f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))
                    // 35.0+ BMI is 45 degrees sweep
                    drawArc(color = SemanticError, startAngle = 315f, sweepAngle = 45f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))

                    // The Animated Needle
                    val pivotCenter = Offset(size.width / 2, size.width / 2) // Base of the needle
                    val needleLength = size.width / 2 - strokeWidthPx + 10f

                    if (bmi > 0) {
                        rotate(degrees = animatedAngle.value, pivot = pivotCenter) {
                            // Draw the line pointing to the right (0 degrees).
                            // The rotate transform will bend it over to the correct position on the arc.
                            drawLine(
                                color = Color.White,
                                start = pivotCenter,
                                end = Offset(pivotCenter.x + needleLength, pivotCenter.y),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Draw the central circle dot over the needle base
                        drawCircle(color = Color.White, radius = 18f, center = pivotCenter)
                        drawCircle(color = DarkSurface, radius = 8f, center = pivotCenter)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Centered Text
            Text(
                text = formattedBmi,
                color = DarkTextPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = categoryText,
                color = categoryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- SECTION H: AI Chatbot Promo Card ---
@Composable
fun AiPromoCard(
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onChatClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MetabolicGreen.copy(alpha = 0.3f)), // Subtle green glowing border
        color = Color.Transparent // Let the Box gradient show through
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MetabolicGreen.copy(alpha = 0.15f),
                            MetabolicCyan.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon + Text
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask your AI coach",
                            color = DarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Get personalized diet and workout advice",
                        color = DarkTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right: Call to Action Button
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, DarkTextPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Chat now",
                        color = DarkTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// --- SECTION I: Gym Map Teaser Card ---
@Composable
fun GymTeaserCard(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🗺️ Gyms Near You",
                        color = DarkTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Find fitness centers in your area",
                        color = DarkTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Explore →",
                    color = MetabolicGreen,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onExploreClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder for the future Google Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { onExploreClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📍", fontSize = 32.sp) // Map pin emoji
            }
        }
    }
}

