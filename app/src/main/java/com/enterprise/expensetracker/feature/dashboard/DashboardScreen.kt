package com.enterprise.expensetracker.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.AiInsightCard
import com.enterprise.expensetracker.core.designsystem.components.EmptyStateView
import com.enterprise.expensetracker.core.designsystem.components.ExpenseListItem
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.theme.AmberWarning
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Expense

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val isHead = state.isHeadOfHousehold
    val userTitle = state.currentUser?.name ?: "Household"
    val userSubtitle = if (isHead) {
        "👑 Head of Household • Family Budget Admin"
    } else if (state.currentUser != null) {
        "👤 ${state.currentUser?.relationship?.displayName ?: "Member"} • Personal & Family Overview"
    } else {
        "Family Budget & Expenses"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "$userTitle's Home",
            subtitle = userSubtitle,
            aiProviderName = state.activeAiEngine,
            onRefreshClick = { viewModel.dispatchEvent(DashboardEvent.RefreshData) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Multi-Period Projection Selector Pills (Weekly, Monthly, Yearly)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackgroundDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BudgetPeriodView.entries.forEach { period ->
                        val isSelected = state.selectedPeriodView == period
                        val label = when (period) {
                            BudgetPeriodView.WEEKLY -> "Weekly View"
                            BudgetPeriodView.MONTHLY -> "Monthly View"
                            BudgetPeriodView.YEARLY -> "Yearly View"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BrandBlue else Color.Transparent)
                                .clickable { viewModel.dispatchEvent(DashboardEvent.SelectPeriodView(period)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimaryDark else TextMutedDark
                            )
                        }
                    }
                }
            }

            // 2. Family Household Budget Card
            item {
                BudgetProgressCard(
                    title = "Family Household Budget",
                    subtitle = "Shared across all members • ${state.selectedPeriodView.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    budgetAmount = state.effectiveFamilyBudgetAmount,
                    spendAmount = state.familySpendInPeriod,
                    remainingAmount = state.familyRemainingBalance,
                    spendPercent = state.familyBudgetPercent,
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = BrandBlue,
                    onEditClick = { viewModel.dispatchEvent(DashboardEvent.SetBudgetDialogVisible(true)) }
                )
            }

            // 3. Member Personal Allowance Card (for Individual Members)
            if (!isHead || state.memberBudget != null) {
                item {
                    val memberBudgetAmt = state.effectiveMemberBudgetAmount ?: 15000.0
                    val memberRemaining = state.memberRemainingBalance ?: (memberBudgetAmt - state.memberSpendInPeriod)
                    val memberPct = state.memberBudgetPercent ?: (state.memberSpendInPeriod / memberBudgetAmt * 100.0)

                    BudgetProgressCard(
                        title = "My Personal Allowance",
                        subtitle = "Individual spending under Head's budget",
                        budgetAmount = memberBudgetAmt,
                        spendAmount = state.memberSpendInPeriod,
                        remainingAmount = memberRemaining,
                        spendPercent = memberPct,
                        icon = Icons.Default.Person,
                        iconTint = PurpleAi,
                        onEditClick = { viewModel.dispatchEvent(DashboardEvent.SetBudgetDialogVisible(true)) }
                    )
                }
            }

            // 4. Forecast Metric Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackgroundDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Projected End of Month Spend",
                                fontSize = 12.sp,
                                color = TextMutedDark,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹%,.2f".format(state.predictedEndOfMonthSpend),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleAi.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PurpleAi,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 5. Featured AI Insight
            state.featuredInsight?.let { insight ->
                item {
                    AiInsightCard(insight = insight)
                }
            }

            // 6. Action Row (Add Expense & Cloud Sync)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToExpenses,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Expense", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.dispatchEvent(DashboardEvent.SyncData) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = TextPrimaryDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Atlas", color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 7. Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "View All →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue,
                        modifier = Modifier.clickable { onNavigateToExpenses() }
                    )
                }
            }

            // 8. Recent Transactions List
            if (state.recentExpenses.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Expenses Yet",
                        description = "Tap 'Add Expense' above to record your first transaction.",
                        actionButtonText = "Add Expense",
                        onActionClick = onNavigateToExpenses
                    )
                }
            } else {
                items(state.recentExpenses, key = { it.id }) { expense ->
                    ExpenseListItem(
                        expense = expense,
                        canDelete = isHead,
                        onDelete = if (isHead) {
                            { expenseToDelete = expense }
                        } else null
                    )
                }
            }
        }
    }

    // Set Budget Dialog
    if (state.isBudgetDialogOpen) {
        SetBudgetDialog(
            isHeadOfHousehold = isHead,
            currentFamilyBudget = state.effectiveFamilyBudgetAmount,
            currentMemberBudget = state.effectiveMemberBudgetAmount,
            onDismiss = { viewModel.dispatchEvent(DashboardEvent.SetBudgetDialogVisible(false)) },
            onSave = { amount, periodType, isFamilyLevel ->
                viewModel.dispatchEvent(
                    DashboardEvent.SaveBudgetSubmitted(
                        amount = amount,
                        periodType = periodType,
                        isFamilyBudget = isFamilyLevel
                    )
                )
            }
        )
    }

    // Confirm Delete Dialog (Head only)
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = CardBackgroundDark,
            title = {
                Text("Delete Expense?", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete '${expense.title}' for ₹${"%,.2f".format(expense.amount)}? This will remove it from MongoDB Atlas.",
                    color = TextMutedDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatchEvent(DashboardEvent.DeleteExpense(expense.id))
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert)
                ) {
                    Text("Delete", color = TextPrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel", color = TextMutedDark)
                }
            }
        )
    }
}

@Composable
private fun BudgetProgressCard(
    title: String,
    subtitle: String,
    budgetAmount: Double,
    spendAmount: Double,
    remainingAmount: Double,
    spendPercent: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onEditClick: () -> Unit
) {
    val progressColor = when {
        spendPercent >= 100.0 -> RoseAlert
        spendPercent >= 80.0 -> AmberWarning
        else -> EmeraldSuccess
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        Text(text = subtitle, fontSize = 11.sp, color = TextMutedDark)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateDark700.copy(alpha = 0.6f))
                        .clickable { onEditClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = BrandBlue, modifier = Modifier.size(13.dp))
                        Text("Set", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
                    }
                }
            }

            // Budget Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "Spent", fontSize = 11.sp, color = TextMutedDark)
                    Text(
                        text = "₹%,.2f".format(spendAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining", fontSize = 11.sp, color = TextMutedDark)
                    Text(
                        text = if (remainingAmount >= 0) "₹%,.2f".format(remainingAmount) else "-₹%,.2f".format(-remainingAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingAmount >= 0) EmeraldSuccess else RoseAlert
                    )
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (spendPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = SlateDark700
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${spendPercent.toInt()}% of ₹" + "%,d".format(budgetAmount.toInt()) + " limit",
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                    Text(
                        text = if (spendPercent > 100) "Over Budget!" else if (spendPercent >= 80) "Near Limit" else "On Track",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }
        }
    }
}
