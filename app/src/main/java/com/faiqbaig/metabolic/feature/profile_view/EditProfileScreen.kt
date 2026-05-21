package com.faiqbaig.metabolic.feature.profile_view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// ── ADDED MISSING THEME IMPORTS ──
import com.faiqbaig.metabolic.core.ui.theme.DarkBackground
import com.faiqbaig.metabolic.core.ui.theme.DarkSurface
import com.faiqbaig.metabolic.core.ui.theme.DarkSurfaceVariant
import com.faiqbaig.metabolic.core.ui.theme.DarkTextPrimary
import com.faiqbaig.metabolic.core.ui.theme.DarkTextSecondary
import com.faiqbaig.metabolic.core.ui.theme.MetabolicGreen

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profileState.collectAsState()

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = MetabolicGreen)
        }
        return
    }

    // Unpack current profile safely
    val p = profile!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Edit Profile Details", color = DarkTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // ── Personal Info ──
        Text("Personal", color = MetabolicGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        EditTextField("Name", p.name) { v -> viewModel.updateField { it.copy(name = v) } }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EditTextField("Age", p.age.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(age = v.toIntOrNull() ?: it.age) }
            }
            EditTextField("Gender", p.gender, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(gender = v) }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EditTextField("Height (cm)", p.heightCm.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(heightCm = v.toFloatOrNull() ?: it.heightCm) }
            }
            EditTextField("Base Weight (kg)", p.weightKg.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(weightKg = v.toFloatOrNull() ?: it.weightKg) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Goals & Activity ──
        Text("Lifestyle & Diet", color = MetabolicGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        EditTextField("Primary Goal", p.goal) { v -> viewModel.updateField { it.copy(goal = v) } }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EditTextField("Activity Level", p.activityLevel, modifier = Modifier.weight(1f)) { v -> viewModel.updateField { it.copy(activityLevel = v) } }
            EditTextField("Diet Type", p.dietType, modifier = Modifier.weight(1f)) { v -> viewModel.updateField { it.copy(dietType = v) } }
        }

        EditTextField("Activity Types (e.g., Gym, Running)", p.activityTypes) { v -> viewModel.updateField { it.copy(activityTypes = v) } }
        EditTextField("Fitness Background", p.background) { v -> viewModel.updateField { it.copy(background = v) } }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Health & Medical ──
        Text("Health & Medical", color = MetabolicGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        EditTextField("Allergies", p.allergies) { v -> viewModel.updateField { it.copy(allergies = v) } }
        EditTextField("Medical Conditions", p.medicalConditions) { v -> viewModel.updateField { it.copy(medicalConditions = v) } }
        EditTextField("Physical Risks / Limitations", p.risks) { v -> viewModel.updateField { it.copy(risks = v) } }
        Spacer(modifier = Modifier.height(16.dp))

        // ── Macro Targets ──
        Text("Daily Targets", color = MetabolicGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EditTextField("Calories", p.dailyCalorieTarget.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(dailyCalorieTarget = v.toIntOrNull() ?: it.dailyCalorieTarget) }
            }
            EditTextField("Protein (g)", p.dailyProteinTarget.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(dailyProteinTarget = v.toIntOrNull() ?: it.dailyProteinTarget) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EditTextField("Carbs (g)", p.dailyCarbsTarget.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(dailyCarbsTarget = v.toIntOrNull() ?: it.dailyCarbsTarget) }
            }
            EditTextField("Fat (g)", p.dailyFatTarget.toString(), isNumber = true, modifier = Modifier.weight(1f)) { v ->
                viewModel.updateField { it.copy(dailyFatTarget = v.toIntOrNull() ?: it.dailyFatTarget) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Action Buttons ──
        Button(
            onClick = { viewModel.saveProfile(onSuccess = onNavigateBack) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MetabolicGreen)
        ) {
            Text("Save Changes", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp)
        ) {
            Text("Cancel", color = DarkTextSecondary)
        }
    }
}

@Composable
fun EditTextField(
    label: String,
    value: String,
    isNumber: Boolean = false,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = DarkTextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text),
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MetabolicGreen,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = DarkTextPrimary,
            unfocusedTextColor = DarkTextPrimary,
            cursorColor = MetabolicGreen
        ),
        shape = RoundedCornerShape(12.dp)
    )
}