package com.enterprise.expensetracker.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.theme.AmberWarning
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
import com.enterprise.expensetracker.core.model.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseListItem(
    expense: Expense,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(expense.timestamp))

    val categoryColor = runCatching {
        Color(android.graphics.Color.parseColor(expense.category.defaultColorHex))
    }.getOrDefault(Color.Gray)

    val categoryIcon = getCategoryIcon(expense.category)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackgroundDark)
            .border(1.dp, CardBorderDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = expense.category.displayName,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = expense.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expense.category.displayName,
                            fontSize = 12.sp,
                            color = TextMutedDark
                        )
                        Text(
                            text = " • $formattedDate",
                            fontSize = 12.sp,
                            color = TextMutedDark
                        )
                    }
                }
            }

            // Amount and Sync Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${expense.currency.symbol}${String.format(Locale.US, "%.2f", expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isSynced = expense.syncStatus == SyncStatus.SYNCED
                    Icon(
                        imageVector = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Sync,
                        contentDescription = "Sync Status",
                        tint = if (isSynced) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = if (isSynced) "MongoDB Synced" else "Pending Sync",
                        fontSize = 10.sp,
                        color = if (isSynced) EmeraldSuccess else AmberWarning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.FOOD_AND_DINING -> Icons.Default.Restaurant
        Category.TRAVEL_AND_TRANSPORT -> Icons.Default.DirectionsCar
        Category.SOFTWARE_AND_TECH -> Icons.Default.Computer
        Category.OFFICE_AND_EQUIPMENT -> Icons.Default.BusinessCenter
        Category.HEALTH_AND_WELLNESS -> Icons.Default.LocalHospital
        Category.ENTERTAINMENT -> Icons.Default.Movie
        Category.UTILITIES_AND_BILLS -> Icons.Default.ReceiptLong
        Category.MISCELLANEOUS -> Icons.Default.Category
    }
}
