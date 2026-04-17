package com.faiqbaig.metabolic.feature.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faiqbaig.metabolic.core.ui.theme.*

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
fun ProfileTopBar(
    currentStep: ProfileStep,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep.index > 0) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkTextPrimary
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentStep.title,
                color = DarkTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
            Text(
                text = "Step ${currentStep.index + 1} of ${ProfileStep.entries.size}",
                color = DarkTextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(40.dp))
    }
}

// ─── Step Progress Bar ────────────────────────────────────────────────────────

@Composable
fun StepProgressBar(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            val isComplete = index < currentStep
            val isCurrent  = index == currentStep

            val animatedWidth by animateFloatAsState(
                targetValue = if (isCurrent) 1f else if (isComplete) 1f else 0f,
                animationSpec = tween(400, easing = EaseOutCubic),
                label = "stepWidth"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkSurfaceVariant)
            ) {
                if (isComplete || isCurrent) {

                    // ── CHANGED: Resolve the background type before applying it ──
                    val activeBackgroundModifier = if (isComplete) {
                        Modifier.background(MetabolicGreen)
                    } else {
                        Modifier.background(
                            Brush.horizontalGradient(
                                listOf(MetabolicGreen, MetabolicCyan)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (isComplete) 1f else animatedWidth)
                            .clip(RoundedCornerShape(2.dp))
                            .then(activeBackgroundModifier) // ── CHANGED: Apply it safely here ──
                    )
                }
            }
        }
    }
}

// ─── Bottom CTA Bar ───────────────────────────────────────────────────────────

@Composable
fun ProfileBottomBar(
    step: ProfileStep,
    isValid: Boolean,
    isSaving: Boolean,
    onNext: () -> Unit
) {
    val isLast = step == ProfileStep.HEALTH_BACKGROUND
    val label  = when {
        isSaving -> "Saving…"
        isLast   -> "Complete Setup"
        else     -> "Continue"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, DarkBackground)
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Button(
            onClick = onNext,
            enabled = isValid && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = DarkSurfaceVariant
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isValid && !isSaving)
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(MetabolicGreen, MetabolicCyan)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = DarkBackground,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isValid) DarkBackground else DarkTextSecondary
                    )
                }
            }
        }
    }
}

// ─── Reusable Selection Components ───────────────────────────────────────────

/**
 * A card-style single-select option (used for goals, activity levels, diet types, gender).
 */
@Composable
fun SelectionCard(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val borderColor = if (isSelected) MetabolicGreen else DarkBorder
    val bgColor = if (isSelected) MetabolicGreen.copy(alpha = 0.12f) else DarkSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = if (isSelected) MetabolicGreen else DarkTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = DarkTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MetabolicGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * A pill-style multi-select chip (used for activity types, allergies).
 */
@Composable
fun SelectionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MetabolicGreen else DarkBorder
    val bgColor = if (isSelected) MetabolicGreen.copy(alpha = 0.15f) else DarkSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) MetabolicGreen else DarkTextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

/**
 * Step header used at the top of each step's content.
 */
@Composable
fun StepHeader(
    headline: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = headline,
            color = DarkTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtext,
            color = DarkTextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

/**
 * Metabolic-styled outlined text field.
 */
@Composable
fun MetabolicField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = if (placeholder.isNotBlank()) {
            { Text(placeholder, color = DarkTextSecondary.copy(alpha = 0.6f), fontSize = 13.sp) }
        } else null,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DarkTextPrimary,
            unfocusedTextColor = DarkTextPrimary,
            focusedBorderColor = MetabolicGreen,
            unfocusedBorderColor = DarkBorder,
            focusedLabelColor = MetabolicGreen,
            unfocusedLabelColor = DarkTextSecondary,
            cursorColor = MetabolicGreen,
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface
        )
    )
}