package com.enterprise.expensetracker.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.AiInsightCard
import com.enterprise.expensetracker.core.designsystem.components.EmptyStateView
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.components.LoadingStateView
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
import com.enterprise.expensetracker.core.model.ReportPeriod
import com.enterprise.expensetracker.domain.usecase.ItemConsumptionReport

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val report = state.analyticsReport

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "Usage Analytics & Reports",
            subtitle = "Consumption cycles & price vs volume trends",
            aiProviderName = state.activeAiProvider,
            onRefreshClick = { viewModel.dispatchEvent(InsightsEvent.RefreshInsights) }
        )

        if (state.isLoading && report == null) {
            LoadingStateView(message = "Aggregating multi-period usage metrics...")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Period Selector (Day, Week, Month, Year)
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
                        ReportPeriod.entries.forEach { period ->
                            val isSelected = state.selectedPeriod == period
                            val label = when (period) {
                                ReportPeriod.DAY -> "Day"
                                ReportPeriod.WEEK -> "Week"
                                ReportPeriod.MONTH -> "Month"
                                ReportPeriod.YEAR -> "Year"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandBlue else Color.Transparent)
                                    .clickable { viewModel.dispatchEvent(InsightsEvent.SelectPeriod(period)) }
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

                // 2. High-Level Summary Card
                if (report != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(CardBackgroundDark, BrandBlue.copy(alpha = 0.1f))
                                    )
                                )
                                .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.periodLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMutedDark
                                    )
                                    // Cost Change Badge
                                    val isCostUp = report.totalCostChangePercent > 0
                                    val costBadgeColor = if (isCostUp) RoseAlert else EmeraldSuccess
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(costBadgeColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isCostUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = costBadgeColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${if (isCostUp) "+" else ""}${report.totalCostChangePercent}% spend",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = costBadgeColor
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(text = "Total Spend", fontSize = 11.sp, color = TextMutedDark)
                                        Text(
                                            text = "₹%,.2f".format(report.totalSpendCurrent),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Prior Period Spend", fontSize = 11.sp, color = TextMutedDark)
                                        Text(
                                            text = "₹%,.2f".format(report.totalSpendPrevious),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextMutedDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Tab Navigation Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportTab.entries.forEach { tab ->
                            val isSelected = state.selectedTab == tab
                            val (title, icon) = when (tab) {
                                ReportTab.ITEM_CONSUMPTION -> "Item Usage & Restock" to Icons.Default.Inventory2
                                ReportTab.PRICE_VARIATION -> "Price vs Volume Trends" to Icons.Default.PriceChange
                                ReportTab.CATEGORY_DISTRIBUTION -> "Category Share" to Icons.Default.PieChart
                                ReportTab.AI_ADVISOR -> "AI Recommendations" to Icons.Default.AutoAwesome
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BrandBlue else SlateDark700.copy(alpha = 0.5f))
                                    .clickable { viewModel.dispatchEvent(InsightsEvent.SelectTab(tab)) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, tint = if (isSelected) TextPrimaryDark else TextMutedDark, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextPrimaryDark else TextMutedDark
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Tab Content
                when (state.selectedTab) {
                    ReportTab.ITEM_CONSUMPTION -> {
                        val items = report?.itemReports.orEmpty()
                        if (items.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No Item Records Yet",
                                    description = "Record expenses with items like Tomato, Milk, or Groceries to see usage duration and restock cycles.",
                                    actionButtonText = "Refresh",
                                    onActionClick = { viewModel.dispatchEvent(InsightsEvent.RefreshInsights) }
                                )
                            }
                        } else {
                            items(items, key = { it.itemName }) { itemReport ->
                                ItemConsumptionCard(itemReport = itemReport)
                            }
                        }
                    }

                    ReportTab.PRICE_VARIATION -> {
                        val items = report?.itemReports.orEmpty()
                        if (items.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No Inflation Data",
                                    description = "Record items across at least two periods to compare unit price fluctuations vs quantity changes.",
                                    actionButtonText = "Refresh",
                                    onActionClick = { viewModel.dispatchEvent(InsightsEvent.RefreshInsights) }
                                )
                            }
                        } else {
                            items(items, key = { "price_${it.itemName}" }) { itemReport ->
                                PriceVarianceCard(itemReport = itemReport)
                            }
                        }
                    }

                    ReportTab.CATEGORY_DISTRIBUTION -> {
                        val cats = report?.categoryBreakdown.orEmpty()
                        if (cats.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No Category Spend",
                                    description = "Record expenses to see category distribution.",
                                    actionButtonText = "Refresh",
                                    onActionClick = { viewModel.dispatchEvent(InsightsEvent.RefreshInsights) }
                                )
                            }
                        } else {
                            val totalSpend = report?.totalSpendCurrent?.coerceAtLeast(1.0) ?: 1.0
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(CardBackgroundDark)
                                        .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Text(
                                            text = "Category Share of Spend",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )

                                        cats.entries.sortedByDescending { it.value }.forEach { (catName, amount) ->
                                            val pct = (amount / totalSpend) * 100.0
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = catName, fontSize = 13.sp, color = TextPrimaryDark)
                                                    Text(
                                                        text = "₹" + "%,.2f".format(amount) + " (${pct.toInt()}%)",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimaryDark
                                                    )
                                                }
                                                LinearProgressIndicator(
                                                    progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = BrandBlue,
                                                    trackColor = SlateDark700
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ReportTab.AI_ADVISOR -> {
                        val insights = state.insights
                        if (insights.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No Anomalies Detected",
                                    description = "Your household spending is currently normal and within budget thresholds.",
                                    actionButtonText = "Refresh",
                                    onActionClick = { viewModel.dispatchEvent(InsightsEvent.RefreshInsights) }
                                )
                            }
                        } else {
                            items(insights) { insight ->
                                AiInsightCard(insight = insight)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemConsumptionCard(itemReport: ItemConsumptionReport) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Name, Category, Total Spend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = itemReport.itemName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = itemReport.categoryName,
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹%,.2f".format(itemReport.currentSpend),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "${itemReport.currentQuantity} ${itemReport.unit} consumed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldSuccess
                    )
                }
            }

            // Usage & Restock Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateDark700.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Restock Frequency", fontSize = 10.sp, color = TextMutedDark)
                    Text(
                        text = "Every ~${itemReport.restockCadenceDays} days",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Avg Usage Rate", fontSize = 10.sp, color = TextMutedDark)
                    Text(
                        text = "~${itemReport.avgUsageDays} days / ${itemReport.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurpleAi
                    )
                }
            }

            // Dual Variation Chips (Spend vs Quantity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cost Variation Chip
                val isCostUp = itemReport.costChangePercent > 0
                val costColor = if (isCostUp) RoseAlert else EmeraldSuccess
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(costColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text("Cost Variation", fontSize = 10.sp, color = TextMutedDark)
                        Text(
                            text = "${if (isCostUp) "+" else ""}${itemReport.costChangePercent}% vs prior",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = costColor
                        )
                    }
                }

                // Quantity Variation Chip
                val isQtyUp = itemReport.quantityChangePercent > 0
                val qtyColor = if (isQtyUp) AmberWarning else TextMutedDark
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(qtyColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text("Volume Variation", fontSize = 10.sp, color = TextMutedDark)
                        Text(
                            text = "${if (isQtyUp) "+" else ""}${itemReport.quantityChangePercent}% quantity",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = qtyColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceVarianceCard(itemReport: ItemConsumptionReport) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemReport.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                val isInflation = itemReport.unitPriceChangePercent > 0
                val inflColor = if (isInflation) RoseAlert else EmeraldSuccess
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(inflColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${if (isInflation) "+" else ""}${itemReport.unitPriceChangePercent}% unit price",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = inflColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Unit Price: ₹%,.1f / %s".format(itemReport.avgPurchasePrice, itemReport.unit),
                    fontSize = 12.sp,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Qty: ${itemReport.currentQuantity} ${itemReport.unit}",
                    fontSize = 12.sp,
                    color = TextMutedDark
                )
            }

            Text(
                text = if (itemReport.unitPriceChangePercent > 0) {
                    "Cost increase is driven by higher unit price (inflation) rather than consumption."
                } else if (itemReport.quantityChangePercent > 0) {
                    "More volume consumed with stable or reduced unit price."
                } else {
                    "Stable price and consumption habit."
                },
                fontSize = 11.sp,
                color = TextMutedDark
            )
        }
    }
}
