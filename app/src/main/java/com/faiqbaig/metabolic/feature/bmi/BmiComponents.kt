package com.faiqbaig.metabolic.feature.bmi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faiqbaig.metabolic.core.data.local.WeightLogEntity

// Design System Colors from Handoff
val MetabolicGreen = Color(0xFF00C896)
val MetabolicCyan = Color(0xFF00A0C8)
val DarkBackground = Color(0xFF0A1612)
val SemanticWarning = Color(0xFFFFBA49)
val SemanticError = Color(0xFFFF6B6B)
val DarkSurface = Color(0xFF121F1B)
val DarkSurfaceVariant = Color(0xFF1C2E28)
val DarkTextPrimary = Color(0xFFE8F5F0)
val DarkTextSecondary = Color(0xFF8FBFB0)

@Composable
fun BmiDialCard(bmi: Double, category: String, weight: Double, height: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current BMI",
                color = DarkTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(160.dp, 80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val strokeWidthPx = 40f
                        val arcSize = Size(size.width, size.width) // Make it a full circle size for drawArc

                        // ── CHANGED: Dashboard-synced segmented arcs ──
                        drawArc(color = MetabolicCyan, startAngle = 180f, sweepAngle = 31.5f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))
                        drawArc(color = MetabolicGreen, startAngle = 211.5f, sweepAngle = 58.5f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))
                        drawArc(color = SemanticWarning, startAngle = 270f, sweepAngle = 45f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))
                        drawArc(color = SemanticError, startAngle = 315f, sweepAngle = 45f, useCenter = false, size = arcSize, style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt))

                        val fraction = ((bmi - 15.0) / 20.0).coerceIn(0.0, 1.0).toFloat()
                        val angleDegrees = 180f + (fraction * 180f)
                        val angleRadians = Math.toRadians(angleDegrees.toDouble())

                        val needleLength = size.width / 2 - 10f
                        val centerX = size.width / 2
                        val centerY = size.height

                        val endX = centerX + (needleLength * kotlin.math.cos(angleRadians)).toFloat()
                        val endY = centerY + (needleLength * kotlin.math.sin(angleRadians)).toFloat()

                        drawLine(
                            color = Color.White,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 12f,
                            center = Offset(centerX, centerY)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Text("Weight", color = MetabolicGreen, fontSize = 14.sp)
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", weight)} kg",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Height", color = MetabolicGreen, fontSize = 14.sp)
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", height)} cm",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = String.format(java.util.Locale.US, "%.1f", bmi),
                color = DarkTextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = category,
                color = when (category) {
                    "Underweight" -> MetabolicCyan
                    "Normal weight" -> MetabolicGreen
                    "Overweight" -> SemanticWarning
                    else -> SemanticError
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogForm(
    weight: String,
    note: String,
    isSaving: Boolean,
    onWeightChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Log New Entry", color = DarkTextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetabolicGreen,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.weight(1.5f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetabolicGreen,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen)
            ) {
                Text(if (isSaving) "Saving..." else "Log Weight", color = DarkBackground)
            }
        }
    }
}

@Composable
fun HistoryFilterToggle(
    currentFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HistoryFilter.values().forEach { filter ->
            val isSelected = filter == currentFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) MetabolicGreen.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) MetabolicGreen else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(filter) {
                        HistoryFilter.WEEK -> "7 Days"
                        HistoryFilter.MONTH -> "30 Days"
                        HistoryFilter.ALL -> "All Time"
                    },
                    color = if (isSelected) MetabolicGreen else DarkTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WeightHistoryChart(dataPoints: List<Pair<String, Double>>) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(DarkSurface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Not enough data to generate chart.", color = DarkTextSecondary)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .padding(top = 32.dp, bottom = 32.dp, start = 40.dp, end = 16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            if (width <= 0f || height <= 0f) return@Canvas

            // ── REVERTED: Original 4-Segment Dynamic Scaling (+2/-2 Padding) ──
            val maxWeight = dataPoints.maxOf { it.second }.toFloat() + 2f
            val minWeight = dataPoints.minOf { it.second }.toFloat() - 2f
            val weightRange = (maxWeight - minWeight).coerceAtLeast(1f) // Prevent division by zero

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#8FBFB0")
                textSize = 30f
                isAntiAlias = true
            }

            // Draw Y-Axis Gridlines & Labels
            val stepY = height / 4
            for (i in 0..4) {
                val y = height - (i * stepY)
                val value = minWeight + (weightRange * (i / 4f))

                drawLine(
                    color = DarkSurfaceVariant,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2f
                )

                textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                drawContext.canvas.nativeCanvas.drawText(
                    String.format(java.util.Locale.US, "%.1f", value),
                    -10f,
                    y + 10f,
                    textPaint
                )
            }

            if (dataPoints.size > 1) {
                val stepX = width / (dataPoints.size - 1).toFloat()
                val linePath = Path()
                val fillPath = Path()
                val points = mutableListOf<Offset>()

                dataPoints.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = height - ((point.second.toFloat() - minWeight) / weightRange) * height
                    val safeY = y.coerceIn(0f, height)

                    points.add(Offset(x, safeY))

                    if (index == 0) {
                        linePath.moveTo(x, safeY)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, safeY)
                    } else {
                        linePath.lineTo(x, safeY)
                        fillPath.lineTo(x, safeY)
                    }

                    if (index == 0 || index == dataPoints.size - 1 || dataPoints.size <= 5) {
                        textPaint.textAlign = android.graphics.Paint.Align.CENTER
                        drawContext.canvas.nativeCanvas.drawText(
                            point.first,
                            x,
                            height + 40f,
                            textPaint
                        )
                    }
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(MetabolicGreen.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height.coerceAtLeast(1f)
                    )
                )

                drawPath(
                    path = linePath,
                    color = MetabolicGreen,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                points.forEach { pointOffset ->
                    drawCircle(color = DarkSurface, radius = 10f, center = pointOffset)
                    drawCircle(color = MetabolicGreen, radius = 6f, center = pointOffset)
                }
            } else if (dataPoints.size == 1) {
                val point = dataPoints.first()
                val y = height - ((point.second.toFloat() - minWeight) / weightRange) * height
                val safeY = y.coerceIn(0f, height)

                drawCircle(color = MetabolicGreen, radius = 8f, center = Offset(width / 2, safeY))

                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawContext.canvas.nativeCanvas.drawText(
                    point.first,
                    width / 2,
                    height + 40f,
                    textPaint
                )
            }
        }
    }
}

@Composable
fun WeightLogRow(entry: WeightLogEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${entry.weightKg} kg", color = DarkTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(entry.date + (if (!entry.note.isNullOrBlank()) " • ${entry.note}" else ""),
                color = DarkTextSecondary, fontSize = 12.sp)
        }

        Text("BMI: ${String.format("%.1f", entry.bmi)}", color = MetabolicCyan, modifier = Modifier.padding(end = 16.dp))

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SemanticError)
        }
    }
}