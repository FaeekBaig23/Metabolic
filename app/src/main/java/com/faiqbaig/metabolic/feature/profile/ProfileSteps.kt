package com.faiqbaig.metabolic.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faiqbaig.metabolic.core.ui.theme.*

// ─── Shared Section Label ─────────────────────────────────────────────────────

@Composable
fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label.uppercase(),
        color = DarkTextSecondary,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp,
        modifier = modifier
    )
}

// ─── Step 2: Health Goal ──────────────────────────────────────────────────────

private val goals = listOf(
    Triple("Lose Weight",    "🔥", "Burn fat, maintain muscle"),
    Triple("Gain Weight",    "📈", "Healthy caloric surplus"),
    Triple("Build Muscle",   "💪", "Strength & hypertrophy"),
    Triple("Athletics",      "⚡", "Optimize performance"),
    Triple("Maintenance",    "⚖️",  "Stay at current weight")
)

@Composable
fun HealthGoalStep(
    selectedGoal: String,
    onGoalSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        StepHeader(
            headline = "What's your goal?",
            subtext   = "This shapes your calorie targets and recommendations."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 2 x 2 grid + one full-width card at bottom
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                goals.take(2).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label,
                        emoji = emoji,
                        subtitle = subtitle,
                        isSelected = selectedGoal == label,
                        onClick = { onGoalSelected(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                goals.drop(2).take(2).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label,
                        emoji = emoji,
                        subtitle = subtitle,
                        isSelected = selectedGoal == label,
                        onClick = { onGoalSelected(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            goals.last().let { (label, emoji, subtitle) ->
                SelectionCard(
                    label = label,
                    emoji = emoji,
                    subtitle = subtitle,
                    isSelected = selectedGoal == label,
                    onClick = { onGoalSelected(label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Step 3: Activity ─────────────────────────────────────────────────────────

private val activityLevels = listOf(
    Triple("Sedentary",         "🛋️", "Little or no exercise"),
    Triple("Lightly Active",    "🚶", "Light exercise 1–3 days/wk"),
    Triple("Moderately Active", "🏃", "Moderate exercise 3–5 days/wk"),
    Triple("Very Active",       "🔥", "Hard exercise 6–7 days/wk")
)

private val activityTypesList = listOf(
    "🏋️ Gym", "🧘 Yoga", "⚽ Sports", "🏃 Cardio", "🏠 Home Workouts", "🤸 Other"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityStep(
    activityLevel: String,
    activityTypes: Set<String>,
    onLevelChange: (String) -> Unit,
    onTypeToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        StepHeader(
            headline = "How active are you?",
            subtext   = "Be honest — this directly affects your calorie calculation."
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel(label = "Activity Level", modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                activityLevels.take(2).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label, emoji = emoji, subtitle = subtitle,
                        isSelected = activityLevel == label,
                        onClick = { onLevelChange(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                activityLevels.drop(2).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label, emoji = emoji, subtitle = subtitle,
                        isSelected = activityLevel == label,
                        onClick = { onLevelChange(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel(label = "What do you do? (optional)", modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activityTypesList.forEach { type ->
                // Strip emoji prefix for storage key
                val key = type.substringAfter(" ")
                SelectionChip(
                    label = type,
                    isSelected = key in activityTypes,
                    onClick = { onTypeToggle(key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Step 4: Diet ─────────────────────────────────────────────────────────────

private val dietTypes = listOf(
    Triple("No Preference",    "🍽️", "Eat everything"),
    Triple("Vegetarian",       "🥗", "No meat"),
    Triple("Vegan",            "🌱", "No animal products"),
    Triple("Keto",             "🥑", "High-fat, low-carb"),
    Triple("Paleo",            "🥩", "Whole, unprocessed"),
    Triple("Mediterranean",    "🫒", "Balanced & heart-healthy")
)

private val allergyList = listOf(
    "🌾 Gluten", "🥛 Dairy", "🥜 Nuts", "🥚 Eggs", "🫘 Soy", "🦐 Shellfish"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietStep(
    dietType: String,
    allergies: Set<String>,
    onDietChange: (String) -> Unit,
    onAllergyToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        StepHeader(
            headline = "Your diet preferences",
            subtext   = "We'll tailor meal suggestions and plans to fit your lifestyle."
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel(label = "Diet Type", modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                dietTypes.take(2).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label, emoji = emoji, subtitle = subtitle,
                        isSelected = dietType == label,
                        onClick = { onDietChange(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                dietTypes.drop(2).take(2).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label, emoji = emoji, subtitle = subtitle,
                        isSelected = dietType == label,
                        onClick = { onDietChange(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                dietTypes.drop(4).forEach { (label, emoji, subtitle) ->
                    SelectionCard(
                        label = label, emoji = emoji, subtitle = subtitle,
                        isSelected = dietType == label,
                        onClick = { onDietChange(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel(label = "Allergies / Intolerances (optional)", modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allergyList.forEach { item ->
                val key = item.substringAfter(" ")
                SelectionChip(
                    label = item,
                    isSelected = key in allergies,
                    onClick = { onAllergyToggle(key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Step 5: Health Background ────────────────────────────────────────────────

@Composable
fun HealthBackgroundStep(
    medicalConditions: String,
    healthRisks: String,
    additionalInfo: String,
    onMedicalChange: (String) -> Unit,
    onRisksChange: (String) -> Unit,
    onAdditionalInfoChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        StepHeader(
            headline = "Health background",
            subtext   = "All optional — helps us keep your plan safe and personal."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetabolicField(
                value = medicalConditions,
                onValueChange = onMedicalChange,
                label = "Medical Conditions",
                placeholder = "e.g. Diabetes Type 2, hypertension…",
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )

            MetabolicField(
                value = healthRisks,
                onValueChange = onRisksChange,
                label = "Health Risks or Concerns",
                placeholder = "e.g. High cholesterol, joint pain…",
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )

            MetabolicField(
                value = additionalInfo,
                onValueChange = onAdditionalInfoChange,
                label = "Anything else we should know?",
                placeholder = "e.g. recovering from injury, stress eating…",
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}