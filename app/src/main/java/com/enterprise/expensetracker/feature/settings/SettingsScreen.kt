package com.enterprise.expensetracker.feature.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "Settings",
            subtitle = "Household preferences & cloud sync",
            aiProviderName = null
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Household Preferences
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardBackgroundDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Household Budget Preferences",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingRow(label = "Currency", value = state.config.defaultCurrencyCode)
                        SettingRow(label = "Monthly Family Limit", value = "$${state.config.maxSingleExpenseThreshold}")
                        SettingRow(label = "Smart Insights", value = if (state.config.isAiInsightsEnabled) "Enabled" else "Disabled")
                    }
                }
            }

            // AI Advisor Engine
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardBackgroundDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurpleAi, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Financial Advisor Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val providers = listOf("GEMINI", "OPENAI", "MOCK")
                            providers.forEach { provider ->
                                val isSelected = state.activeAiProvider.equals(provider, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) PurpleAi else SlateDark700)
                                        .clickable { viewModel.dispatchEvent(SettingsEvent.SwitchAiProvider(provider)) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = provider,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = TextPrimaryDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Powers household budget predictions, grocery spend forecasts, and savings recommendations.",
                            fontSize = 12.sp,
                            color = TextMutedDark,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Cloud Backup & Sync (No technical DB information displayed)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardBackgroundDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cloud Backup & Sync",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            }
                            Text(
                                text = "Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingRow(label = "Recorded Home Expenses", value = "${state.totalLocalExpensesCount} items")
                        SettingRow(label = "Automatic Backup", value = "Enabled")

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.dispatchEvent(SettingsEvent.SyncMongoAtlas) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync All Expenses Now", fontSize = 13.sp, color = TextPrimaryDark)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.dispatchEvent(SettingsEvent.ClearLocalDatabase) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RoseAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Expense Ledger", fontSize = 13.sp, color = RoseAlert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMutedDark)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
    }
}
