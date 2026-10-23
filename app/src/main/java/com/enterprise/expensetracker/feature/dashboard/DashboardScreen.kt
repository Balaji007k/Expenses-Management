package com.enterprise.expensetracker.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.AiInsightCard
import com.enterprise.expensetracker.core.designsystem.components.EmptyStateView
import com.enterprise.expensetracker.core.designsystem.components.ExpenseListItem
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.components.SummaryMetricCard
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.feature.expenses.AddExpenseDialog

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isAddDialogOpen by remember { mutableStateOf(false) }

    val userTitle = state.currentUser?.name ?: "Household"
    val userSubtitle = if (state.currentUser?.isHeadOfHousehold == true) {
        "👑 Head of Household • Full Access"
    } else if (state.currentUser != null) {
        "👤 ${state.currentUser?.relationship?.displayName ?: "Member"} • My Expenses"
    } else {
        "Home Budget & Family Expenses"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "$userTitle's Home",
            subtitle = userSubtitle,
            aiProviderName = state.activeAiEngine
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-Level Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMetricCard(
                        title = "Spend This Month",
                        value = "₹${"%,.2f".format(state.totalSpendThisMonth)}",
                        subtitle = "Budget: ₹${"%,.0f".format(state.monthlyBudget)}",
                        icon = Icons.Default.CreditCard,
                        iconTint = BrandBlue,
                        modifier = Modifier.weight(1f)
                    )

                    SummaryMetricCard(
                        title = "Forecasted Spend",
                        value = "₹${"%,.2f".format(state.predictedEndOfMonthSpend)}",
                        subtitle = "Estimated Month-End",
                        icon = Icons.Default.TrendingUp,
                        iconTint = PurpleAi,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { isAddDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Expense", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.dispatchEvent(DashboardEvent.SyncData) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Cloud", fontSize = 13.sp, color = TextPrimaryDark)
                    }
                }
            }

            // Featured Home AI Insight
            if (state.featuredInsight != null) {
                item {
                    Text(
                        text = "Household Budget Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AiInsightCard(insight = state.featuredInsight!!)
                }
            }

            // Recent Home Expenses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Home Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
            }

            if (state.recentExpenses.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Home Expenses Yet",
                        description = "Start recording your household groceries, utilities, and bills to track family spend.",
                        actionButtonText = "Add First Expense",
                        onActionClick = { isAddDialogOpen = true }
                    )
                }
            } else {
                items(state.recentExpenses, key = { it.id }) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AddExpenseDialog(
            onDismiss = { isAddDialogOpen = false },
            onConfirm = { title, amount, category, notes ->
                viewModel.dispatchEvent(DashboardEvent.AddExpenseSubmitted(title, amount, category, notes))
                isAddDialogOpen = false
            }
        )
    }
}
