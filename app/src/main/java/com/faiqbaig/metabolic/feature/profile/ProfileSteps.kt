package com.faiqbaig.metabolic.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    Triple("Lose Weight",    Icons.Default.LocalFireDepartment, "Burn fat, maintain muscle"),
    Triple("Gain Weight",    Icons.Default.TrendingUp, "Healthy caloric surplus"),
    Triple("Build Muscle",   Icons.Default.FitnessCenter, "Strength & hypertrophy"),
    Triple("Athletics",      Icons.Default.Bolt, "Optimize performance"),
    Triple("Maintenance",    Icons.Default.MonitorWeight,  "Stay at current weight")
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
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                goals.take(2).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label,
                        icon = icon,
                        subtitle = subtitle,
                        isSelected = selectedGoal == label,
                        onClick = { onGoalSelected(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                goals.drop(2).take(2).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label,
                        icon = icon,
                        subtitle = subtitle,
                        isSelected = selectedGoal == label,
                        onClick = { onGoalSelected(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
            goals.last().let { (label, icon, subtitle) ->
                SelectionCard(
                    label = label,
                    icon = icon,
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
    Triple("Sedentary",         Icons.Default.Weekend, "Little or no exercise"),
    Triple("Lightly Active",    Icons.Default.DirectionsWalk, "Light exercise 1–3 days/wk"),
    Triple("Moderately Active", Icons.Default.DirectionsRun, "Moderate exercise 3–5 days/wk"),
    Triple("Very Active",       Icons.Default.LocalFireDepartment, "Hard exercise 6–7 days/wk")
)

private val activityTypesList = listOf(
    Pair("Gym", Icons.Default.FitnessCenter),
    Pair("Yoga", Icons.Default.SelfImprovement),
    Pair("Sports", Icons.Default.SportsSoccer),
    Pair("Cardio", Icons.Default.DirectionsRun),
    Pair("Home Workouts", Icons.Default.Home),
    Pair("Other", Icons.Default.MoreHoriz)
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
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activityLevels.take(2).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label, icon = icon, subtitle = subtitle,
                        isSelected = activityLevel == label,
                        onClick = { onLevelChange(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activityLevels.drop(2).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label, icon = icon, subtitle = subtitle,
                        isSelected = activityLevel == label,
                        onClick = { onLevelChange(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
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
            activityTypesList.forEach { (label, icon) ->
                SelectionChip(
                    label = label,
                    icon = icon,
                    isSelected = label in activityTypes,
                    onClick = { onTypeToggle(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Step 4: Diet ─────────────────────────────────────────────────────────────

private val dietTypes = listOf(
    Triple("No Preference",    Icons.Default.Restaurant, "Eat everything"),
    Triple("Vegetarian",       Icons.Default.Eco, "No meat"),
    Triple("Vegan",            Icons.Default.Spa, "No animal products"),
    Triple("Keto",             Icons.Default.Egg, "High-fat, low-carb"),
    Triple("Paleo",            Icons.Default.SetMeal, "Whole, unprocessed"),
    Triple("Mediterranean",    Icons.Default.LocalDining, "Balanced & heart-healthy")
)

private val allergyList = listOf(
    Pair("Gluten", Icons.Default.Grass),
    Pair("Dairy", Icons.Default.LocalDrink),
    Pair("Nuts", Icons.Default.Eco),
    Pair("Eggs", Icons.Default.Egg),
    Pair("Soy", Icons.Default.Spa),
    Pair("Shellfish", Icons.Default.SetMeal)
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
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dietTypes.take(2).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label, icon = icon, subtitle = subtitle,
                        isSelected = dietType == label,
                        onClick = { onDietChange(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dietTypes.drop(2).take(2).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label, icon = icon, subtitle = subtitle,
                        isSelected = dietType == label,
                        onClick = { onDietChange(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dietTypes.drop(4).forEach { (label, icon, subtitle) ->
                    SelectionCard(
                        label = label, icon = icon, subtitle = subtitle,
                        isSelected = dietType == label,
                        onClick = { onDietChange(label) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
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
            allergyList.forEach { (label, icon) ->
                SelectionChip(
                    label = label,
                    icon = icon,
                    isSelected = label in allergies,
                    onClick = { onAllergyToggle(label) }
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