package com.enterprise.expensetracker.feature.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.RestockReason

@Composable
fun RestockDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (reason: RestockReason, quantityRemaining: Double?) -> Unit
) {
    var selectedReason by remember { mutableStateOf(RestockReason.EMPTIED) }
    var qtyLeftText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(
                text = "Record Restock for '${expense.title}'",
                fontSize = 17.sp,
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
                    text = "Did the stock finish completely, or are you restocking in-between?",
                    fontSize = 13.sp,
                    color = TextMutedDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedReason == RestockReason.EMPTIED) EmeraldSuccess else SlateDark700)
                            .clickable { selectedReason = RestockReason.EMPTIED }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Completely Emptied",
                            fontSize = 12.sp,
                            fontWeight = if (selectedReason == RestockReason.EMPTIED) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedReason == RestockReason.IN_BETWEEN) BrandBlue else SlateDark700)
                            .clickable { selectedReason = RestockReason.IN_BETWEEN }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "In-Between",
                            fontSize = 12.sp,
                            fontWeight = if (selectedReason == RestockReason.IN_BETWEEN) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimaryDark
                        )
                    }
                }

                if (selectedReason == RestockReason.IN_BETWEEN) {
                    OutlinedTextField(
                        value = qtyLeftText,
                        onValueChange = { qtyLeftText = it },
                        label = { Text("Quantity remaining before restock (${expense.unit})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtyLeft = if (selectedReason == RestockReason.IN_BETWEEN) qtyLeftText.toDoubleOrNull() ?: 0.0 else null
                    onConfirm(selectedReason, qtyLeft)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Confirm Restock", color = TextPrimaryDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}
