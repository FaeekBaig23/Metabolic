package com.faiqbaig.metabolic.feature.profile_view

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

val DarkBackground = Color(0xFF0A1612)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher for the Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                // Keep permission to read this URI even after the app closes
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.updateProfileImage(it.toString())
            }
        }
    )

    // Handle Toasts
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isPasswordResetSent) {
        if (uiState.isPasswordResetSent) {
            Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
            viewModel.dismissPasswordResetMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 100.dp)
        ) {
            // ── Section A: Profile Overview (New Centered Layout) ──
            item {
                ProfileHeader(
                    profile = uiState.userProfile,
                    profileImageUri = uiState.profileImageUri,
                    latestWeightKg = uiState.latestWeightKg,
                    latestBmi = uiState.latestBmi,
                    onAvatarClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onEditClick = onNavigateToEditProfile
                )
            }

            // ── Section B: App Settings ──
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("App Settings")

                SettingToggleRow(
                    title = "Meal Reminders",
                    subtitle = "Get notified when it's time to eat",
                    isChecked = uiState.mealRemindersEnabled,
                    onToggle = viewModel::toggleMealReminders
                )

                SettingToggleRow(
                    title = "Hydration Reminders",
                    subtitle = "Periodic nudges to drink water",
                    isChecked = uiState.hydrationRemindersEnabled,
                    onToggle = viewModel::toggleHydrationReminders
                )
            }

            // ── Section C: Account Management ──
            item {
                SectionTitle("Account")

                SettingActionRow(
                    title = "Change Password",
                    onClick = { viewModel.sendPasswordResetEmail() }
                )

                SettingActionRow(
                    title = "Log Out",
                    onClick = {
                        viewModel.signOut()
                        onNavigateToLogin()
                    }
                )

                SettingActionRow(
                    title = "Delete Account",
                    titleColor = MetabolicRed,
                    onClick = { viewModel.showDeleteConfirmation(true) }
                )
            }
        }

        // ── Overlays ──
        if (uiState.showDeleteConfirmation) {
            DeleteAccountDialog(
                onConfirm = { viewModel.deleteAccount(onSuccess = onNavigateToLogin) },
                onDismiss = { viewModel.showDeleteConfirmation(false) }
            )
        }
    }
}