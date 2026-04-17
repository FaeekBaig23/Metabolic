package com.faiqbaig.metabolic.feature.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.faiqbaig.metabolic.core.ui.theme.*

@Composable
fun ProfileSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button — go to previous step or exit
    BackHandler(enabled = uiState.currentStep.index > 0) {
        viewModel.goToPreviousStep()
    }

    // Navigate on save complete
    LaunchedEffect(uiState.isSaveComplete) {
        if (uiState.isSaveComplete) onSetupComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Subtle radial green glow behind the form
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MetabolicGreen.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            ProfileTopBar(
                currentStep = uiState.currentStep,
                onBack = { viewModel.goToPreviousStep() }
            )

            // ── Step progress bar ────────────────────────────────────────────
            StepProgressBar(
                totalSteps = ProfileStep.entries.size,
                currentStep = uiState.currentStep.index,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // ── Step content (animated slide) ────────────────────────────────
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    val forward = targetState.index > initialState.index
                    slideInHorizontally(
                        animationSpec = tween(320, easing = EaseInOutCubic),
                        initialOffsetX = { if (forward) it else -it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(320, easing = EaseInOutCubic),
                        targetOffsetX = { if (forward) -it else it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "StepContent"
            ) { step ->
                when (step) {
                    ProfileStep.BASIC_INFO -> BasicInfoStep(
                        name      = uiState.name,
                        gender    = uiState.gender,
                        age       = uiState.age,
                        weightKg  = uiState.weightKg,
                        heightCm  = uiState.heightCm,
                        onNameChange   = viewModel::onNameChange,
                        onGenderChange = viewModel::onGenderChange,
                        onAgeChange    = viewModel::onAgeChange,
                        onWeightChange = viewModel::onWeightChange,
                        onHeightChange = viewModel::onHeightChange
                    )
                    ProfileStep.HEALTH_GOAL -> HealthGoalStep(
                        selectedGoal = uiState.goal,
                        onGoalSelected = viewModel::onGoalChange
                    )
                    ProfileStep.ACTIVITY -> ActivityStep(
                        activityLevel   = uiState.activityLevel,
                        activityTypes   = uiState.activityTypes,
                        onLevelChange   = viewModel::onActivityLevelChange,
                        onTypeToggle    = viewModel::onActivityTypeToggle
                    )
                    ProfileStep.DIET -> DietStep(
                        dietType       = uiState.dietType,
                        allergies      = uiState.allergies,
                        onDietChange   = viewModel::onDietTypeChange,
                        onAllergyToggle = viewModel::onAllergyToggle
                    )
                    ProfileStep.HEALTH_BACKGROUND -> HealthBackgroundStep(
                        medicalConditions = uiState.medicalConditions,
                        healthRisks       = uiState.healthRisks,
                        additionalInfo    = uiState.additionalInfo,
                        onMedicalChange        = viewModel::onMedicalConditionsChange,
                        onRisksChange          = viewModel::onHealthRisksChange,
                        onAdditionalInfoChange = viewModel::onAdditionalInfoChange
                    )
                }
            }

            // ── Bottom CTA ────────────────────────────────────────────────────
            ProfileBottomBar(
                step       = uiState.currentStep,
                isValid    = viewModel.isCurrentStepValid(),
                isSaving   = uiState.isSaving,
                onNext     = {
                    if (uiState.currentStep == ProfileStep.HEALTH_BACKGROUND) {
                        viewModel.saveProfile()
                    } else {
                        viewModel.goToNextStep()
                    }
                }
            )
        }

        // ── Error snackbar ────────────────────────────────────────────────────
        uiState.saveError?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss", color = MetabolicGreen)
                    }
                },
                containerColor = DarkSurfaceVariant,
                contentColor = DarkTextPrimary
            ) {
                Text(error)
            }
        }
    }
}