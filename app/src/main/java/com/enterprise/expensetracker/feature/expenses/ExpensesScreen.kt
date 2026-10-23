package com.enterprise.expensetracker.feature.expenses

import androidx.compose.foundation.background
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.enterprise.expensetracker.core.designsystem.theme.SlateDark700
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = "Expense Ledger",
                subtitle = "Manage & audit business transactions"
            )

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.dispatchEvent(ExpensesEvent.SearchQueryChanged(it)) },
                    placeholder = { Text("Search by title or vendor...", color = TextMutedDark, fontSize = 14.sp) },
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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
                    title = "No Matching Transactions",
                    description = if (state.searchQuery.isNotBlank()) "No records match '${state.searchQuery}'" else "Tap + below to record a business expense.",
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
                        ExpenseListItem(expense = expense)
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
    }
}
