package com.enterprise.expensetracker.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enterprise.expensetracker.core.designsystem.theme.AmberWarning
import com.enterprise.expensetracker.core.designsystem.theme.BrandBlue
import com.enterprise.expensetracker.core.designsystem.theme.CardBackgroundDark
import com.enterprise.expensetracker.core.designsystem.theme.EmeraldSuccess
import com.enterprise.expensetracker.core.designsystem.theme.PurpleAi
import com.enterprise.expensetracker.core.designsystem.theme.RoseAlert
import com.enterprise.expensetracker.core.designsystem.theme.TextMutedDark
import com.enterprise.expensetracker.core.designsystem.theme.TextPrimaryDark
import com.enterprise.expensetracker.core.model.ai.AiInsight
import com.enterprise.expensetracker.core.model.ai.InsightSeverity

@Composable
fun AiInsightCard(
    insight: AiInsight,
    modifier: Modifier = Modifier
) {
    val accentColor = when (insight.severity) {
        InsightSeverity.ALERT -> RoseAlert
        InsightSeverity.WARNING -> AmberWarning
        InsightSeverity.OPPORTUNITY -> EmeraldSuccess
        InsightSeverity.INFO -> BrandBlue
    }

    val icon = when (insight.severity) {
        InsightSeverity.ALERT, InsightSeverity.WARNING -> Icons.Default.Warning
        InsightSeverity.OPPORTUNITY -> Icons.Default.TrendingUp
        InsightSeverity.INFO -> Icons.Default.Info
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CardBackgroundDark,
                        PurpleAi.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                1.dp,
                accentColor.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = PurpleAi,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI INSIGHT • ${insight.generatedByEngine.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAi,
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "${(insight.confidenceScore * 100).toInt()}% Confidence",
                    fontSize = 11.sp,
                    color = TextMutedDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = insight.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insight.message,
                        fontSize = 13.sp,
                        color = TextMutedDark,
                        lineHeight = 18.sp
                    )
                    if (insight.estimatedSavings != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estimated Potential Savings: $${String.format("%.2f", insight.estimatedSavings)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }
        }
    }
}
