package com.enterprise.expensetracker.feature.expenses

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enterprise.expensetracker.core.designsystem.components.EmptyStateView
import com.enterprise.expensetracker.core.designsystem.components.ExpenseListItem
import com.enterprise.expensetracker.core.designsystem.components.ExpenseTopBar
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark800
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.dispatchEvent(ExpensesEvent.SetAddDialogVisible(true)) },
                containerColor = BrandBlue,
                contentColor = TextPrimaryDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ExpenseTopBar(
                title = if (state.isHeadOfHousehold) "Household Ledger" else "My Personal Ledger",
                subtitle = if (state.isHeadOfHousehold) "All family members' expenses • Full Access" else "Showing expenses recorded by ${state.currentUser?.name ?: "You"}",
                aiProviderName = null,
                onRefreshClick = { viewModel.dispatchEvent(ExpensesEvent.Refresh) }
            )

            // Family Member Personal Banner (for Member role)
            if (!state.isHeadOfHousehold && state.currentUser != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandBlue.copy(alpha = 0.15f))
                        .border(1.dp, BrandBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "👤 Member View: You can add and manage your own household expenses.",
                        fontSize = 11.sp,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Head of Household: Filter by Family Member
            if (state.isHeadOfHousehold && state.familyMembers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedDark
                    )

                    val isAllSelected = state.selectedMemberId == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isAllSelected) PurpleAi else SlateDark800)
                            .clickable { viewModel.dispatchEvent(ExpensesEvent.MemberFilterSelected(null)) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "👨‍👩‍👧‍👦 All Family",
                            fontSize = 11.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimaryDark
                        )
                    }

                    state.familyMembers.forEach { member ->
                        val isSelected = state.selectedMemberId == member.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PurpleAi else SlateDark800)
                                .clickable { viewModel.dispatchEvent(ExpensesEvent.MemberFilterSelected(member.id)) }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${member.name} (${member.relationship.displayName})",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimaryDark
                            )
                        }
                    }
                }
            }

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.dispatchEvent(ExpensesEvent.SearchQueryChanged(it)) },
                    placeholder = { Text("Search groceries, bills, milk, pharmacy...", color = TextMutedDark, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedContainerColor = CardBackgroundDark,
                        unfocusedContainerColor = CardBackgroundDark,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = CardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" filter chip
                val isAllSelected = state.selectedCategory == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAllSelected) BrandBlue else SlateDark700)
                        .clickable { viewModel.dispatchEvent(ExpensesEvent.CategoryFilterSelected(null)) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "All",
                        fontSize = 12.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                        color = TextPrimaryDark
                    )
                }

                Category.entries.forEach { category ->
                    val isSelected = state.selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) BrandBlue else SlateDark700)
                            .clickable { viewModel.dispatchEvent(ExpensesEvent.CategoryFilterSelected(category)) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = category.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimaryDark
                        )
                    }
                }
            }

            // Transactions List
            if (state.filteredExpenses.isEmpty()) {
                EmptyStateView(
                    title = "No Expenses Found",
                    description = if (state.searchQuery.isNotBlank()) "No records match '${state.searchQuery}'" else "Tap + below to record a household expense.",
                    actionButtonText = "Add Expense",
                    onActionClick = { viewModel.dispatchEvent(ExpensesEvent.SetAddDialogVisible(true)) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredExpenses, key = { it.id }) { expense ->
                        ExpenseListItem(
                            expense = expense,
                            canDelete = state.isHeadOfHousehold,
                            onDelete = if (state.isHeadOfHousehold) {
                                { expenseToDelete = expense }
                            } else null
                        )
                    }
                }
            }
        }

        if (state.isAddDialogOpen) {
            AddExpenseDialog(
                onDismiss = { viewModel.dispatchEvent(ExpensesEvent.SetAddDialogVisible(false)) },
                onConfirm = { title, amount, category, notes ->
                    viewModel.dispatchEvent(ExpensesEvent.AddExpenseSubmitted(title, amount, category, notes))
                }
            )
        }

        if (expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                containerColor = CardBackgroundDark,
                title = {
                    Text(
                        text = "Delete Expense?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete '${expenseToDelete?.title}' (₹${"%,.2f".format(expenseToDelete?.amount ?: 0.0)}) logged by ${expenseToDelete?.createdByUserName}?",
                        fontSize = 14.sp,
                        color = TextMutedDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = expenseToDelete?.id
                            expenseToDelete = null
                            if (id != null) {
                                viewModel.dispatchEvent(ExpensesEvent.DeleteExpense(id))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseAlert),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
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
}
