package com.faiqbaig.metabolic.feature.profile_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity

val MetabolicGreen = Color(0xFF00C896)
val MetabolicRed = Color(0xFFFF4B4B)
val DarkSurface = Color(0xFF121F1B)
val DarkSurfaceVariant = Color(0xFF1C2E28)
val DarkTextPrimary = Color(0xFFE8F5F0)
val DarkTextSecondary = Color(0xFF8FBFB0)


@Composable
fun ProfileHeader(
    profile: UserProfileEntity?,
    profileImageUri: String?,
    latestWeightKg: Float?,
    latestBmi: Float?,
    onAvatarClick: () -> Unit,
    onEditClick: () -> Unit
) {
    if (profile == null) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MetabolicGreen)
        }
        return
    }

    val displayWeight = latestWeightKg ?: profile.weightKg
    val displayBmi = latestBmi ?: profile.bmi

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Avatar Circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(DarkSurfaceVariant)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri.isNullOrEmpty()) {
                Text(
                    text = profile.name.take(1).uppercase(),
                    color = MetabolicGreen,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Username & Goal
        Text(text = profile.name, color = DarkTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Goal: ${profile.goal}", color = MetabolicGreen, fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Stats Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(24.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem("Weight", "$displayWeight kg")
                    StatItem("Height", "${profile.heightCm} cm")
                    StatItem("BMI", String.format("%.1f", displayBmi))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Daily Targets", color = DarkTextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroItem("Cals", "${profile.dailyCalorieTarget}")
                    MacroItem("Pro", "${profile.dailyProteinTarget}g")
                    MacroItem("Carbs", "${profile.dailyCarbsTarget}g")
                    MacroItem("Fat", "${profile.dailyFatTarget}g")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Edit Button
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MetabolicGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MetabolicGreen)
        ) {
            Text("Edit Profile Details", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProfileHeaderCard(profile: UserProfileEntity?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(24.dp)
    ) {
        if (profile == null) {
            CircularProgressIndicator(color = MetabolicGreen, modifier = Modifier.align(Alignment.Center))
        } else {
            Column {
                Text(
                    text = profile.name,
                    color = DarkTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: ${profile.goal}",
                    color = MetabolicGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Weight", "${profile.weightKg} kg")
                    StatItem("Height", "${profile.heightCm} cm")
                    StatItem("BMI", String.format("%.1f", profile.bmi))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = DarkSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Daily Targets", color = DarkTextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroItem("Cals", "${profile.dailyCalorieTarget}")
                    MacroItem("Pro", "${profile.dailyProteinTarget}g")
                    MacroItem("Carbs", "${profile.dailyCarbsTarget}g")
                    MacroItem("Fat", "${profile.dailyFatTarget}g")
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(text = value, color = DarkTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = DarkTextSecondary, fontSize = 14.sp)
    }
}

@Composable
fun MacroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = DarkTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(text = label, color = DarkTextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = DarkTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable { onToggle(!isChecked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = DarkTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = DarkTextSecondary, fontSize = 14.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkSurface,
                checkedTrackColor = MetabolicGreen,
                uncheckedThumbColor = DarkTextSecondary,
                uncheckedTrackColor = DarkSurface
            )
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SettingActionRow(
    title: String,
    titleColor: Color = DarkTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(text = "›", color = DarkTextSecondary, fontSize = 24.sp, modifier = Modifier.padding(bottom = 4.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Bulletproof pure-box dialog to avoid Material 3 parameter conflicts
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .clickable(onClick = {}) // Intercept taps so they don't dismiss
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Delete Account?",
                    color = MetabolicRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This action is permanent. All your data, meal plans, and logs will be permanently erased. Do you want to proceed?",
                    color = DarkTextSecondary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = MetabolicRed)
                    ) {
                        Text("Delete Everything", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}