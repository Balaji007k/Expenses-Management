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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Wifi
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
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.CardBorderDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.Category
import com.enterprise.expensetracker.core.model.Expense
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

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${expense.currency.symbol}${String.format(Locale.US, "%.2f", expense.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                if (!expense.notes.isNullOrBlank()) {
                    Text(
                        text = expense.notes!!,
                        fontSize = 11.sp,
                        color = TextMutedDark,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.GROCERIES_AND_FOOD -> Icons.Default.ShoppingBasket
        Category.RENT_AND_HOUSING -> Icons.Default.Home
        Category.UTILITIES_AND_BILLS -> Icons.Default.Bolt
        Category.INTERNET_AND_MOBILE -> Icons.Default.Wifi
        Category.KIDS_AND_EDUCATION -> Icons.Default.School
        Category.HEALTH_AND_MEDICINE -> Icons.Default.LocalHospital
        Category.HOME_MAINTENANCE -> Icons.Default.Build
        Category.SHOPPING_AND_CLOTHING -> Icons.Default.ShoppingBag
        Category.TRAVEL_AND_FUEL -> Icons.Default.DirectionsCar
        Category.DINING_AND_OUTING -> Icons.Default.Restaurant
        Category.MISCELLANEOUS -> Icons.Default.Category
    }
}
