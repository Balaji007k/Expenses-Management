package com.enterprise.expensetracker.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.AiInsightCard
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.components.LoadingStateView
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "AI Predictions & Insights",
            subtitle = "Forecasting powered by ${state.activeAiProvider}",
            aiProviderName = state.activeAiProvider
        )

        if (state.isLoading) {
            LoadingStateView(message = "Executing AI predictive models...")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Predictive Forecast Banner
                if (state.forecast != null) {
                    val forecast = state.forecast!!
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(CardBackgroundDark, PurpleAi.copy(alpha = 0.12f))
                                    )
                                )
                                .border(1.dp, PurpleAi.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = PurpleAi,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "SPENDING FORECAST",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleAi,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                    Text(
                                        text = "${(forecast.confidenceRate * 100).toInt()}% Model Confidence",
                                        fontSize = 11.sp,
                                        color = TextMutedDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "₹${String.format(Locale.US, "%.2f", forecast.predictedEndOfMonthSpend)}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )

                                Text(
                                    text = "Projected End-of-Period Total",
                                    fontSize = 13.sp,
                                    color = TextMutedDark
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Current Actual", fontSize = 11.sp, color = TextMutedDark)
                                        Text(
                                            text = "₹${String.format(Locale.US, "%.2f", forecast.currentMonthActual)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimaryDark
                                        )
                                    }
                                    Column {
                                        Text(text = "Burn Rate / Day", fontSize = 11.sp, color = TextMutedDark)
                                        Text(
                                            text = "₹${String.format(Locale.US, "%.2f", forecast.predictedBurnRatePerDay)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldSuccess
                                        )
                                    }
                                    Column {
                                        Text(text = "Period Remaining", fontSize = 11.sp, color = TextMutedDark)
                                        Text(
                                            text = "${forecast.daysRemainingInPeriod} Days",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimaryDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = forecast.summaryNarrative,
                                    fontSize = 13.sp,
                                    color = TextMutedDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // AI Anomaly & Optimization Recommendations
                item {
                    Text(
                        text = "Budget Alerts & Savings Opportunities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                if (state.insights.isEmpty()) {
                    item {
                        Text(
                            text = "No anomalies detected in your home budget. All household spending is well within limits.",
                            fontSize = 13.sp,
                            color = TextMutedDark,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(state.insights, key = { it.id }) { insight ->
                        AiInsightCard(insight = insight)
                    }
                }

                // Refresh Action
                item {
                    Button(
                        onClick = { viewModel.dispatchEvent(InsightsEvent.RefreshInsights) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Re-calculate Home Forecast", color = TextPrimaryDark)
                    }
                }
            }
        }
    }
}
