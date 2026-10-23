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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "Executive Overview",
            subtitle = "Enterprise Expense Intelligence",
            aiProviderName = state.activeAiEngine,
            mongoSyncStatus = state.mongoSyncState
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMetricCard(
                        title = "TOTAL SPEND",
                        value = "${state.currencySymbol}${String.format(Locale.US, "%.2f", state.totalSpendThisMonth)}",
                        subtitle = "Current Period",
                        icon = Icons.Default.CreditCard,
                        iconTint = BrandBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "AI FORECAST",
                        value = "${state.currencySymbol}${String.format(Locale.US, "%.2f", state.predictedEndOfMonthSpend)}",
                        subtitle = "Projected End-Month",
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
                        onClick = { viewModel.dispatchEvent(DashboardEvent.AddDemoExpense) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Demo", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.dispatchEvent(DashboardEvent.TriggerMongoSync) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Mongo", fontSize = 13.sp, color = TextPrimaryDark)
                    }
                }
            }

            // Featured AI Insight
            if (state.featuredInsight != null) {
                item {
                    Text(
                        text = "Real-time AI Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AiInsightCard(insight = state.featuredInsight!!)
                }
            }

            // Recent Expenses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    if (state.recentExpenses.isNotEmpty()) {
                        Text(
                            text = "View All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlue,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            if (state.recentExpenses.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Transactions Recorded",
                        description = "Add sample transactions or synchronize with MongoDB to observe live tracking.",
                        actionButtonText = "Generate Sample Transaction",
                        onActionClick = { viewModel.dispatchEvent(DashboardEvent.AddDemoExpense) }
                    )
                }
            } else {
                items(state.recentExpenses, key = { it.id }) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }
}
