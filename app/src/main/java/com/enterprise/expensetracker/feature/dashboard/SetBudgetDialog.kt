package com.enterprise.expensetracker.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.enterprise.expensetracker.core.model.BudgetPeriod

@Composable
fun SetBudgetDialog(
    isHeadOfHousehold: Boolean,
    currentFamilyBudget: Double,
    currentMemberBudget: Double?,
    onDismiss: () -> Unit,
    onSave: (amount: Double, period: BudgetPeriod, isFamilyLevel: Boolean) -> Unit
) {
    var isFamilyLevel by remember { mutableStateOf(isHeadOfHousehold) }
    var periodType by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var amountText by remember {
        mutableStateOf(
            if (isHeadOfHousehold) currentFamilyBudget.toInt().toString()
            else (currentMemberBudget?.toInt() ?: 15000).toString()
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toDoubleOrNull() ?: 0.0

    // Calculations for preview
    val weeklyEquiv = when (periodType) {
        BudgetPeriod.WEEKLY -> amount
        BudgetPeriod.MONTHLY -> amount / 4.333
    }
    val monthlyEquiv = when (periodType) {
        BudgetPeriod.MONTHLY -> amount
        BudgetPeriod.WEEKLY -> amount * 4.333
    }
    val yearlyEquiv = when (periodType) {
        BudgetPeriod.MONTHLY -> amount * 12.0
        BudgetPeriod.WEEKLY -> amount * 52.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(
                text = if (isFamilyLevel) "Configure Family Budget" else "Configure My Personal Budget",
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
                // If user is Head, allow toggling between setting Family Budget vs Member Budget
                if (isHeadOfHousehold) {
                    Text(
                        text = "Budget Target Scope",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
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
                                .background(if (isFamilyLevel) BrandBlue else SlateDark700)
                                .clickable {
                                    isFamilyLevel = true
                                    amountText = currentFamilyBudget.toInt().toString()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Family Head Budget",
                                fontSize = 12.sp,
                                fontWeight = if (isFamilyLevel) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimaryDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isFamilyLevel) BrandBlue else SlateDark700)
                                .clickable {
                                    isFamilyLevel = false
                                    amountText = (currentMemberBudget?.toInt() ?: 15000).toString()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Personal Member",
                                fontSize = 12.sp,
                                fontWeight = if (!isFamilyLevel) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }

                // Period Type Toggle: Weekly vs Monthly
                Text(
                    text = "Base Definition Period",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMutedDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetPeriod.entries.forEach { period ->
                        val isSelected = periodType == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandBlue else SlateDark700)
                                .clickable { periodType = period }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (period == BudgetPeriod.MONTHLY) "Monthly Budget" else "Weekly Budget",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Budget Amount (₹)") },
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

                // Multi-functional projection preview card
                if (amount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateDark700.copy(alpha = 0.5f))
                            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Multi-Period Conversion Preview",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weekly:", fontSize = 12.sp, color = TextMutedDark)
                                Text("₹%,d".format(weeklyEquiv.toInt()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monthly:", fontSize = 12.sp, color = TextMutedDark)
                                Text("₹%,d".format(monthlyEquiv.toInt()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Yearly:", fontSize = 12.sp, color = TextMutedDark)
                                Text("₹%,d".format(yearlyEquiv.toInt()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = androidx.compose.ui.graphics.Color(0xFFF43F5E),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount <= 0) {
                        errorMessage = "Please enter an amount greater than ₹0"
                        return@Button
                    }
                    onSave(amount, periodType, isFamilyLevel)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Save Budget", color = TextPrimaryDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedDark)
            }
        }
    )
}
