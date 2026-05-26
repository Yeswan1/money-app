package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StudentDashboardScreen(
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onChatClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                StudentHeader()
                BalanceCard(
                    title = "Student Allowance",
                    amount = "$450.00",
                    sub1Title = "Spent",
                    sub1Amount = "$125.50",
                    sub2Title = "Saved",
                    sub2Amount = "$324.50"
                )
            }

            item {
                QuickActionsSection(onAddExpense = onAddTransaction)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "View All →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.clickable { onSeeAllTransactions() }
                    )
                }
            }

            items(studentTransactions) { transaction ->
                DashboardTransactionItem(transaction)
            }
        }

        // Floating Action Button (Chat)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(60.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                    shape = CircleShape
                )
                .clickable { onChatClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "AI Assistant",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
