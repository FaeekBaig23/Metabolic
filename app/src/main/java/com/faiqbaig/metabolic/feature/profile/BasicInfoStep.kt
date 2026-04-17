package com.faiqbaig.metabolic.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.faiqbaig.metabolic.core.ui.theme.*

private val genders = listOf(
    Triple("Male",   "♂",  null),
    Triple("Female", "♀",  null),
    Triple("Other",  "⚧",  null)
)

@Composable
fun BasicInfoStep(
    name: String,
    gender: String,
    age: String,
    weightKg: String,
    heightCm: String,
    onNameChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        StepHeader(
            headline = "Tell us about yourself",
            subtext   = "We'll use this to calculate your personal targets."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Name
        MetabolicField(
            value = name,
            onValueChange = onNameChange,
            label = "Full Name",
            placeholder = "e.g. Alex Johnson",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gender
        SectionLabel(label = "Gender", modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            genders.forEach { (label, emoji, _) ->
                SelectionCard(
                    label = label,
                    emoji = emoji,
                    isSelected = gender == label,
                    onClick = { onGenderChange(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Age
        MetabolicField(
            value = age,
            onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) onAgeChange(it) },
            label = "Age",
            placeholder = "e.g. 25",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weight + Height side by side
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetabolicField(
                value = weightKg,
                onValueChange = { if (it.length <= 5) onWeightChange(it) },
                label = "Weight (kg)",
                placeholder = "e.g. 72",
                modifier = Modifier.weight(1f)
            )
            MetabolicField(
                value = heightCm,
                onValueChange = { if (it.length <= 5) onHeightChange(it) },
                label = "Height (cm)",
                placeholder = "e.g. 175",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}