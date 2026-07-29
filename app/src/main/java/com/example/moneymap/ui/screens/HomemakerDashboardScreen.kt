package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
fun HomemakerDashboardScreen(
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
                HomemakerHeader()
                BalanceCard(
                    title = "Household Budget",
                    amount = "₹2,500.00",
                    sub1Title = "Spent",
                    sub1Amount = "₹1,150.00",
                    sub2Title = "Remaining",
                    sub2Amount = "₹1,350.00"
                )
            }

            item {
                HomemakerQuickActionsSection(onAddExpense = onAddTransaction)
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
                        text = "Recent Household Expenses",
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

            items(homemakerTransactions) { transaction ->
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

@Composable
fun HomemakerHeader() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Good Evening", fontSize = 14.sp, color = Color.Gray)
        Text("Priya", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@Composable
fun HomemakerQuickActionsSection(onAddExpense: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Household Tools",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(
                title = "Add Expense",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                iconBg = Color(0xFFDBEAFE),
                iconTint = Color(0xFF3B82F6),
                onClick = onAddExpense,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Grocery List",
                icon = Icons.Default.ShoppingCart,
                iconBg = Color(0xFFDCFCE7),
                iconTint = Color(0xFF16A34A),
                onClick = { },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Family Allowances",
                icon = Icons.Default.FamilyRestroom,
                iconBg = Color(0xFFFCE7F3),
                iconTint = Color(0xFFDB2777),
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

val homemakerTransactions = listOf(
    DashboardTransaction("Weekly Groceries", "Food", "Today", "145.50", Icons.Default.ShoppingCart, Color(0xFF10B981)),
    DashboardTransaction("Electricity Bill", "Bills", "Yesterday", "85.00", Icons.Default.Receipt, Color(0xFFF59E0B)),
    DashboardTransaction("Kids School Supplies", "Education", "May 8", "120.00", Icons.Default.AccountBalanceWallet, Color(0xFF8B5CF6)),
    DashboardTransaction("Household Budget", "Income", "May 1", "2,500.00", Icons.Default.AccountBalanceWallet, Color(0xFF10B981), true)
)
