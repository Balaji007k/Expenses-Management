package com.enterprise.expensetracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark

@Composable
fun BudgetPreferenceDialog(
    initialLimit: Double,
    isSmartInsightsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (limit: Double, smartInsights: Boolean) -> Unit
) {
    var limitText by remember { mutableStateOf(initialLimit.toInt().toString()) }
    var smartInsights by remember { mutableStateOf(isSmartInsightsEnabled) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(
                text = "Edit Household Budget Preferences",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Configure your family's overall monthly ceiling and AI recommendations.",
                    fontSize = 13.sp,
                    color = TextMutedDark
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        errorMessage = null
                    },
                    label = { Text("Monthly Family Limit (₹)") },
                    leadingIcon = {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = CardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = RoseAlert, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.padding(4.dp))
                        Column {
                            Text("Smart AI Insights", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            Text("Anomaly alerts & spend forecasts", fontSize = 11.sp, color = TextMutedDark)
                        }
                    }

                    Switch(
                        checked = smartInsights,
                        onCheckedChange = { smartInsights = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimaryDark,
                            checkedTrackColor = BrandBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = limitText.toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        errorMessage = "Please enter a valid monthly limit (e.g. 50000)"
                        return@Button
                    }
                    onSave(parsed, smartInsights)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Save Changes", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}
