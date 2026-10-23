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
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.ExpenseScope

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
                title = if (state.isHeadOfHousehold) "Household Ledger" else "Family & Personal Ledger",
                subtitle = if (state.isHeadOfHousehold) "All family expenses • Full Admin Access" else "Edit enabled for all • Delete reserved for Head",
                aiProviderName = null,
                onRefreshClick = { viewModel.dispatchEvent(ExpensesEvent.Refresh) }
            )

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.dispatchEvent(ExpensesEvent.SearchQueryChanged(it)) },
                    placeholder = { Text("Search by item name, notes, or member...", color = TextMutedDark, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMutedDark, modifier = Modifier.size(20.dp))
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

            // Scope Filter (All vs Family Level vs Personal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAllScope = state.selectedScope == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAllScope) BrandBlue else SlateDark700.copy(alpha = 0.6f))
                        .clickable { viewModel.dispatchEvent(ExpensesEvent.ScopeFilterSelected(null)) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("All Scopes", fontSize = 11.sp, fontWeight = if (isAllScope) FontWeight.Bold else FontWeight.Normal, color = TextPrimaryDark)
                }

                val isFamilyScope = state.selectedScope == ExpenseScope.FAMILY
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFamilyScope) BrandBlue else SlateDark700.copy(alpha = 0.6f))
                        .clickable { viewModel.dispatchEvent(ExpensesEvent.ScopeFilterSelected(ExpenseScope.FAMILY)) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Family Shared", fontSize = 11.sp, fontWeight = if (isFamilyScope) FontWeight.Bold else FontWeight.Normal, color = TextPrimaryDark)
                }

                val isPersonalScope = state.selectedScope == ExpenseScope.INDIVIDUAL
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPersonalScope) PurpleAi else SlateDark700.copy(alpha = 0.6f))
                        .clickable { viewModel.dispatchEvent(ExpensesEvent.ScopeFilterSelected(ExpenseScope.INDIVIDUAL)) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Personal Only", fontSize = 11.sp, fontWeight = if (isPersonalScope) FontWeight.Bold else FontWeight.Normal, color = TextPrimaryDark)
                }
            }

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAllSelected = state.selectedCategory == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAllSelected) BrandBlue else SlateDark700)
                        .clickable { viewModel.dispatchEvent(ExpensesEvent.CategoryFilterSelected(null)) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "All Categories",
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
                            onEdit = {
                                viewModel.dispatchEvent(ExpensesEvent.StartEditingExpense(expense))
                            },
                            onStockEnded = {
                                viewModel.dispatchEvent(ExpensesEvent.StockEndedSubmitted(expense))
                            },
                            onRestock = {
                                viewModel.dispatchEvent(ExpensesEvent.StartRestockingExpense(expense))
                            },
                            onDelete = if (state.isHeadOfHousehold) {
                                { expenseToDelete = expense }
                            } else null
                        )
                    }
                }
            }
        }
    }

    // Add or Edit Expense Dialog
    if (state.isAddDialogOpen) {
        AddExpenseDialog(
            initialExpense = state.editingExpense,
            catalogSuggestions = state.catalogSuggestions,
            onDismiss = { viewModel.dispatchEvent(ExpensesEvent.SetAddDialogVisible(false)) },
            onConfirm = { title, amount, category, quantity, unit, scope, notes, imageUrl ->
                if (state.editingExpense != null) {
                    viewModel.dispatchEvent(
                        ExpensesEvent.EditExpenseSubmitted(
                            id = state.editingExpense!!.id,
                            title = title,
                            amount = amount,
                            category = category,
                            quantity = quantity,
                            unit = unit,
                            scope = scope,
                            notes = notes,
                            imageUrl = imageUrl
                        )
                    )
                } else {
                    viewModel.dispatchEvent(
                        ExpensesEvent.AddExpenseSubmitted(
                            title = title,
                            amount = amount,
                            category = category,
                            quantity = quantity,
                            unit = unit,
                            scope = scope,
                            notes = notes,
                            imageUrl = imageUrl
                        )
                    )
                }
            }
        )
    }

    // Restock Dialog (with Emptied vs In-Between and Quantity Left input)
    state.restockingExpense?.let { expense ->
        RestockDialog(
            expense = expense,
            onDismiss = { viewModel.dispatchEvent(ExpensesEvent.StartRestockingExpense(null)) },
            onConfirm = { reason, quantityRemaining ->
                viewModel.dispatchEvent(
                    ExpensesEvent.RestockSubmitted(
                        expense = expense,
                        reason = reason,
                        quantityRemaining = quantityRemaining
                    )
                )
            }
        )
    }

    // Delete Confirmation Dialog (Head Only)
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            containerColor = CardBackgroundDark,
            title = {
                Text(
                    text = "Delete Expense?",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete '${expense.title}' (${expense.quantity} ${expense.unit}) for ₹${"%,.2f".format(expense.amount)}? This action is exclusive to Head of Household.",
                    color = TextMutedDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatchEvent(ExpensesEvent.DeleteExpense(expense.id))
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
