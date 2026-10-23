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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import com.enterprise.expensetracker.core.designsystem.components.UserAvatarView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExpenseTopBar(
            title = "Settings",
            subtitle = "Household preferences & account session",
            aiProviderName = null,
            onRefreshClick = { viewModel.dispatchEvent(SettingsEvent.SyncMongoAtlas) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prominent User Account & Sign Out Card
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
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Account & Session",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            }
                            state.currentUser?.let { user ->
                                val isHead = user.isHeadOfHousehold
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isHead) PurpleAi.copy(alpha = 0.2f) else BrandBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isHead) "👑 Head" else "👤 Member",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHead) PurpleAi else BrandBlue
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val user = state.currentUser
                        if (user != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.dispatchEvent(SettingsEvent.SetAvatarDialogVisible(true))
                                        }
                                ) {
                                    UserAvatarView(
                                        name = user.name,
                                        avatarUrl = user.avatarUrl,
                                        isHeadOfHousehold = user.isHeadOfHousehold,
                                        size = 54.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(BrandBlue)
                                            .border(1.5.dp, CardBackgroundDark, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoCamera,
                                            contentDescription = "Change photo",
                                            tint = TextPrimaryDark,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = user.email,
                                        fontSize = 12.sp,
                                        color = TextMutedDark
                                    )
                                    Text(
                                        text = "Role: ${user.role.displayName} • ${user.relationship.displayName}",
                                        fontSize = 11.sp,
                                        color = TextMutedDark
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.dispatchEvent(SettingsEvent.SetAvatarDialogVisible(true)) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = { showSignOutConfirm = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAlert),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(RoseAlert.copy(alpha = 0.6f), RoseAlert))
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoseAlert)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign Out of Account",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseAlert
                                )
                            }
                        } else {
                            Text(
                                text = "No active session logged in.",
                                fontSize = 13.sp,
                                color = TextMutedDark
                            )
                        }
                    }
                }
            }

            // Household Budget Preferences
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
                                Icon(Icons.Default.Settings, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Household Budget Preferences",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.dispatchEvent(SettingsEvent.SetBudgetPrefDialogVisible(true)) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingRow(label = "Currency", value = "Rupees (${state.config.defaultCurrencyCode} - ₹)")
                        SettingRow(label = "Monthly Family Limit", value = "₹%,d".format(state.currentFamilyBudget.toInt()))
                        SettingRow(label = "Smart Insights", value = if (state.isSmartInsightsEnabled) "Enabled" else "Disabled")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
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

                            val isKeySet = when (state.activeAiProvider.uppercase()) {
                                "GEMINI" -> state.geminiApiKey.isNotBlank()
                                "OPENAI" -> state.openAiApiKey.isNotBlank()
                                else -> true
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isKeySet) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isKeySet) "● Live Key Active" else "○ Key Required",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isKeySet) EmeraldSuccess else AmberWarning
                                )
                            }
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

                        Spacer(modifier = Modifier.height(12.dp))

                        when (state.activeAiProvider.uppercase()) {
                            "GEMINI" -> {
                                var geminiInput by remember(state.geminiApiKey) { mutableStateOf(state.geminiApiKey) }
                                var showGeminiKey by remember { mutableStateOf(false) }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Google Gemini API Key",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMutedDark
                                    )

                                    OutlinedTextField(
                                        value = geminiInput,
                                        onValueChange = { geminiInput = it },
                                        placeholder = { Text("Paste AIzaSy... key from Google AI Studio") },
                                        leadingIcon = {
                                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = PurpleAi, modifier = Modifier.size(16.dp))
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                                Icon(
                                                    if (showGeminiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle visibility",
                                                    tint = TextMutedDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark,
                                            focusedBorderColor = PurpleAi,
                                            unfocusedBorderColor = CardBorderDark
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.dispatchEvent(SettingsEvent.SaveAiKey("GEMINI", geminiInput)) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAi),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Save Gemini Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.dispatchEvent(SettingsEvent.TestAiConnection("GEMINI")) },
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isTestingAi,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (state.isTestingAi) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = PurpleAi)
                                            } else {
                                                Text("Test Live Call", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PurpleAi)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Tip: Get a 100% free Gemini API key at aistudio.google.com without adding a credit card.",
                                        fontSize = 11.sp,
                                        color = TextMutedDark
                                    )
                                }
                            }
                            "OPENAI" -> {
                                var openAiInput by remember(state.openAiApiKey) { mutableStateOf(state.openAiApiKey) }
                                var showOpenAiKey by remember { mutableStateOf(false) }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "OpenAI API Key",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMutedDark
                                    )

                                    OutlinedTextField(
                                        value = openAiInput,
                                        onValueChange = { openAiInput = it },
                                        placeholder = { Text("Paste sk-... key from platform.openai.com") },
                                        leadingIcon = {
                                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { showOpenAiKey = !showOpenAiKey }) {
                                                Icon(
                                                    if (showOpenAiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle visibility",
                                                    tint = TextMutedDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        visualTransformation = if (showOpenAiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark,
                                            focusedBorderColor = BrandBlue,
                                            unfocusedBorderColor = CardBorderDark
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.dispatchEvent(SettingsEvent.SaveAiKey("OPENAI", openAiInput)) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Save OpenAI Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.dispatchEvent(SettingsEvent.TestAiConnection("OPENAI")) },
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !state.isTestingAi,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (state.isTestingAi) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrandBlue)
                                            } else {
                                                Text("Test Live Call", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Uses gpt-4o-mini for family spend forecasts and anomaly detection.",
                                        fontSize = 11.sp,
                                        color = TextMutedDark
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "Offline Simulation Mode: Generates mathematical financial estimates without external API keys or network requests.",
                                    fontSize = 12.sp,
                                    color = TextMutedDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // AI Test Connection Result Box
                        state.aiTestResult?.let { resultText ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (resultText.startsWith("Error")) RoseAlert.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f))
                                    .border(1.dp, if (resultText.startsWith("Error")) RoseAlert else EmeraldSuccess, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = resultText,
                                        fontSize = 11.sp,
                                        color = TextPrimaryDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.dispatchEvent(SettingsEvent.DismissAiTestResult) }) {
                                        Text("Dismiss", fontSize = 11.sp, color = TextMutedDark)
                                    }
                                }
                            }
                        }
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

    // Sign Out Confirmation Dialog
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            containerColor = CardBackgroundDark,
            title = {
                Text(
                    text = "Sign Out of Account?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out from this device? You can log back in at any time.",
                    fontSize = 14.sp,
                    color = TextMutedDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        viewModel.dispatchEvent(SettingsEvent.SignOut)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAlert),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = TextMutedDark)
                }
            }
        )
    }

    if (state.isAvatarDialogOpen && state.currentUser != null) {
        val user = state.currentUser!!
        AvatarPickerDialog(
            currentName = user.name,
            currentAvatarUrl = user.avatarUrl,
            isHeadOfHousehold = user.isHeadOfHousehold,
            userId = user.id,
            onDismiss = { viewModel.dispatchEvent(SettingsEvent.SetAvatarDialogVisible(false)) },
            onSaveAvatar = { newAvatar ->
                viewModel.dispatchEvent(SettingsEvent.UpdateAvatar(newAvatar))
            }
        )
    }

    if (state.isBudgetPrefDialogOpen) {
        BudgetPreferenceDialog(
            initialLimit = state.currentFamilyBudget,
            isSmartInsightsEnabled = state.isSmartInsightsEnabled,
            onDismiss = { viewModel.dispatchEvent(SettingsEvent.SetBudgetPrefDialogVisible(false)) },
            onSave = { limit, smartInsights ->
                viewModel.dispatchEvent(SettingsEvent.SaveBudgetPreference(limit, smartInsights))
            }
        )
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
