package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.repository.MoneyMapRepository
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyBudgetScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var isLoading by remember { mutableStateOf(true) }
    
    var totalMonthlyBudget by remember { mutableDoubleStateOf(0.0) }
    var weeklyAllocations by remember { mutableStateOf<List<WeeklyAllocation>>(emptyList()) }
    var activeWeekBudget by remember { mutableDoubleStateOf(0.0) }
    var activeWeekSpent by remember { mutableDoubleStateOf(0.0) }

    val calendar = remember { Calendar.getInstance() }
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    LaunchedEffect(Unit) {
        var budgetSum = 0.0
        repository.getBudgetSummary(month, year).onSuccess {
            budgetSum = it.totalBudgeted
        }

        repository.getRecentTransactions(limit = 1000, type = "EXPENSE").onSuccess { transactions ->
            // Filter transactions for this month/year
            val currentMonthTransactions = transactions.filter { t ->
                try {
                    // Date format: "YYYY-MM-DD..."
                    val parts = t.transactionDate.split("-")
                    val tYear = parts.getOrNull(0)?.toIntOrNull()
                    val tMonth = parts.getOrNull(1)?.toIntOrNull()
                    tYear == year && tMonth == month
                } catch (e: Exception) {
                    false
                }
            }

            // Calculate spent amounts per week
            val week1Spent = currentMonthTransactions.filter { getDayFromDate(it.transactionDate) in 1..7 }.sumOf { it.amount }
            val week2Spent = currentMonthTransactions.filter { getDayFromDate(it.transactionDate) in 8..14 }.sumOf { it.amount }
            val week3Spent = currentMonthTransactions.filter { getDayFromDate(it.transactionDate) in 15..21 }.sumOf { it.amount }
            val week4Spent = currentMonthTransactions.filter { getDayFromDate(it.transactionDate) in 22..28 }.sumOf { it.amount }
            val week5Spent = currentMonthTransactions.filter { getDayFromDate(it.transactionDate) >= 29 }.sumOf { it.amount }

            // Divide monthly budget into weekly limits (proportional by days)
            val week1Limit = budgetSum * (7.0 / maxDay)
            val week2Limit = budgetSum * (7.0 / maxDay)
            val week3Limit = budgetSum * (7.0 / maxDay)
            val week4Limit = budgetSum * (7.0 / maxDay)
            val week5Limit = if (maxDay > 28) budgetSum * ((maxDay - 28.0) / maxDay) else 0.0

            val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) ?: "Month"

            val weeks = mutableListOf(
                WeeklyAllocation("Week 1 ($monthName 1 - $monthName 7)", String.format("₹%.0f", week1Spent), String.format("₹%.0f", week1Limit), (if (week1Limit > 0) week1Spent / week1Limit else 0.0).toFloat(), Color(0xFF10B981), currentDay in 1..7),
                WeeklyAllocation("Week 2 ($monthName 8 - $monthName 14)", String.format("₹%.0f", week2Spent), String.format("₹%.0f", week2Limit), (if (week2Limit > 0) week2Spent / week2Limit else 0.0).toFloat(), Color(0xFF3B82F6), currentDay in 8..14),
                WeeklyAllocation("Week 3 ($monthName 15 - $monthName 21)", String.format("₹%.0f", week3Spent), String.format("₹%.0f", week3Limit), (if (week3Limit > 0) week3Spent / week3Limit else 0.0).toFloat(), Color(0xFF8B5CF6), currentDay in 15..21),
                WeeklyAllocation("Week 4 ($monthName 22 - $monthName 28)", String.format("₹%.0f", week4Spent), String.format("₹%.0f", week4Limit), (if (week4Limit > 0) week4Spent / week4Limit else 0.0).toFloat(), Color(0xFFEC4899), currentDay in 22..28)
            )

            if (maxDay > 28) {
                weeks.add(
                    WeeklyAllocation("Week 5 ($monthName 29 - $monthName $maxDay)", String.format("₹%.0f", week5Spent), String.format("₹%.0f", week5Limit), (if (week5Limit > 0) week5Spent / week5Limit else 0.0).toFloat(), Color(0xFFF59E0B), currentDay >= 29)
                )
            }

            weeklyAllocations = weeks
            totalMonthlyBudget = budgetSum

            // Find active week variables
            val activeWeek = weeks.find { it.isActive } ?: weeks.firstOrNull()
            if (activeWeek != null) {
                activeWeekSpent = when {
                    currentDay in 1..7 -> week1Spent
                    currentDay in 8..14 -> week2Spent
                    currentDay in 15..21 -> week3Spent
                    currentDay in 22..28 -> week4Spent
                    else -> week5Spent
                }
                activeWeekBudget = when {
                    currentDay in 1..7 -> week1Limit
                    currentDay in 8..14 -> week2Limit
                    currentDay in 15..21 -> week3Limit
                    currentDay in 22..28 -> week4Limit
                    else -> week5Limit
                }
            }

            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Weekly Setup", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else {
                // This Week's Budget
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This Week's Budget",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("₹%.2f", activeWeekBudget),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("₹%.2f Used • ₹%.2f Remaining", activeWeekSpent, Math.max(0.0, activeWeekBudget - activeWeekSpent)),
                        fontSize = 14.sp,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Weekly Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                weeklyAllocations.forEach { allocation ->
                    WeeklyAllocationItem(allocation)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun WeeklyAllocationItem(allocation: WeeklyAllocation) {
    val containerBg = if (allocation.isActive) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
    val borderStrokeColor = if (allocation.isActive) Color(0xFFBFDBFE) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(containerBg, RoundedCornerShape(16.dp))
            .let { modifier ->
                if (allocation.isActive) {
                    modifier.clip(RoundedCornerShape(16.dp))
                    // Subtle visual highlight for active week
                } else modifier
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = allocation.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (allocation.isActive) Color(0xFF1E40AF) else Color(0xFF0F172A)
                )
                if (allocation.isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Active", fontSize = 11.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "${allocation.spent} / ${allocation.total}",
                fontSize = 14.sp,
                color = if (allocation.isActive) Color(0xFF1E40AF) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LinearProgressIndicator(
            progress = Math.min(1.0f, allocation.progress),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = allocation.color,
            trackColor = if (allocation.isActive) allocation.color.copy(alpha = 0.2f) else Color(0xFFE2E8F0),
            strokeCap = StrokeCap.Round
        )
    }
}

private fun getDayFromDate(dateString: String): Int {
    return try {
        val datePart = dateString.split("T").firstOrNull() ?: return 1
        val parts = datePart.split("-")
        parts.getOrNull(2)?.toIntOrNull() ?: 1
    } catch (e: Exception) {
        1
    }
}

data class WeeklyAllocation(
    val name: String,
    val spent: String,
    val total: String,
    val progress: Float,
    val color: Color,
    val isActive: Boolean
)
