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

// Section D: Quick Action Row

@Composable
fun QuickActionRow(
    onLogMealClick: () -> Unit,
    onScanMealClick: () -> Unit,
    onWaterClick: () -> Unit,
    onLogWeightClick: () -> Unit,
    onAskAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A horizontally scrollable row
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            QuickActionChip(icon = "➕", label = "Log Meal", onClick = onLogMealClick)
        }
        item {
            QuickActionChip(icon = "📷", label = "Scan Meal", onClick = onScanMealClick)
        }
        item {
            QuickActionChip(icon = "💧", label = "Water", onClick = onWaterClick)
        }
        item {
            QuickActionChip(icon = "⚖️", label = "Log Weight", onClick = onLogWeightClick)
        }
        item {
            QuickActionChip(icon = "🤖", label = "Ask AI", onClick = onAskAiClick)
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = DarkTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Section E: Today's Meals Section

@Composable
fun TodaysMealsSection(
    meals: List<MealLogEntity>,
    onSeeAllClick: () -> Unit,
    onLogMealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Content
        if (meals.isEmpty()) {
            EmptyMealState(onLogMealClick = onLogMealClick)
        } else {
            // Placeholder rendering for when Step 6 populates the list
            meals.forEach { _ ->
                MealRow(
                    icon = "🍳",
                    name = "Scrambled Eggs & Toast", // Stub text
                    time = "08:30 AM",               // Stub text
                    calories = 340,                  // Stub text
                    hasProtein = true,
                    hasCarbs = true,
                    hasFat = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EmptyMealState(onLogMealClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🍽️", fontSize = 40.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No meals logged yet",
                color = DarkTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prominent Call to Action Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onLogMealClick() },
                color = MetabolicGreen
            ) {
                Text(
                    text = "➕ Log your first meal",
                    color = Color.Black, // High contrast on the green button
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun MealRow(
    icon: String,
    name: String,
    time: String,
    calories: Int,
    hasProtein: Boolean,
    hasCarbs: Boolean,
    hasFat: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Meal Icon
            Text(text = icon, fontSize = 24.sp)

            Spacer(modifier = Modifier.width(12.dp))

            // Center: Name and Time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = DarkTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Right: Calories and Macro Dots
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$calories kcal",
                    color = MacroCalories,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasProtein) MacroDot(color = MacroProtein)
                    if (hasCarbs) MacroDot(color = MacroCarbs)
                    if (hasFat) MacroDot(color = MacroFat)
                }
            }
        }
    }
}

@Composable
private fun MacroDot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// Section F: Water Intake Tracker

@Composable
fun WaterTrackerCard(
    waterGlasses: Int,
    onWaterToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalGlasses = 8
    val mlPerGlass = 250 // Standard glass size

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💧 Hydration",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row of glasses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..totalGlasses) {
                    val isFilled = i <= waterGlasses
                    Box(
                        modifier = Modifier
                            // Scaled down slightly from 40dp to 36dp to ensure
                            // it doesn't overflow on smaller Android screens
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) MetabolicCyan else DarkSurfaceVariant)
                            .clickable { onWaterToggle(i) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFilled) {
                            // A subtle visual cue that the glass is full
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer text calculating the total ml
            Text(
                text = "$waterGlasses / $totalGlasses glasses — ${waterGlasses * mlPerGlass} ml",
                color = DarkTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Section G: BMI Snapshot Card

@Composable
fun BmiSnapshotCard(
    bmi: Double,
    onTrackWeightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine the category and color based on standard BMI ranges
    val (categoryText, categoryColor) = when {
        bmi == 0.0 -> "No data" to DarkSurfaceVariant // Fallback for empty state
        bmi < 18.5 -> "Underweight" to MetabolicCyan
        bmi < 25.0 -> "Normal weight" to MetabolicGreen
        bmi < 30.0 -> "Overweight" to SemanticWarning
        else -> "Obese" to SemanticError
    }

    // Format BMI to one decimal place
    val formattedBmi = if (bmi > 0) String.format(Locale.US, "%.1f", bmi) else "--"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Large BMI Number
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.3f)
            ) {
                Text(
                    text = formattedBmi,
                    color = DarkTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BMI",
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Side: Gauge Bar and Labels
            Column(
                modifier = Modifier.weight(0.7f)
            ) {
                // The Visual Gauge
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    val trackHeight = 6.dp.toPx()
                    val centerY = size.height / 2
                    val segmentWidth = size.width / 4

                    // Draw the 4 colored segments
                    drawLine(MetabolicCyan, Offset(0f, centerY), Offset(segmentWidth, centerY), strokeWidth = trackHeight, cap = StrokeCap.Round)
                    drawLine(MetabolicGreen, Offset(segmentWidth, centerY), Offset(segmentWidth * 2, centerY), strokeWidth = trackHeight)
                    drawLine(SemanticWarning, Offset(segmentWidth * 2, centerY), Offset(segmentWidth * 3, centerY), strokeWidth = trackHeight)
                    drawLine(SemanticError, Offset(segmentWidth * 3, centerY), Offset(size.width, centerY), strokeWidth = trackHeight, cap = StrokeCap.Round)

                    // Calculate dot position only if we have a valid BMI
                    if (bmi > 0) {
                        // Clamp BMI between 15 and 35 for visual gauge bounding
                        val clampedBmi = bmi.coerceIn(15.0, 35.0)
                        val fraction = ((clampedBmi - 15.0) / 20.0).toFloat()
                        val dotX = size.width * fraction

                        // Draw white indicator dot
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = Offset(dotX, centerY)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Labels below the gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = categoryText,
                        color = categoryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Track weight →",
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onTrackWeightClick() }
                    )
                }
            }
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

