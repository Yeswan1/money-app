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
import com.example.moneymap.data.model.BudgetSummaryResponse
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyBudgetScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var summary by remember { mutableStateOf<BudgetSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val calendar = remember { Calendar.getInstance() }
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    LaunchedEffect(Unit) {
        repository.getBudgetSummary(month, year).onSuccess {
            summary = it
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Monthly Setup", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                    IconButton(onClick = { /* Edit Budget */ }) {
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
                val totalBudgeted = summary?.totalBudgeted ?: 0.0
                val totalSpent = summary?.totalSpent ?: 0.0
                val remainingBudget = summary?.remainingBudget ?: 0.0
                val breakdown = summary?.breakdown ?: emptyList()

                // Total Monthly Budget
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Monthly Budget",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("₹%.2f", totalBudgeted),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("₹%.2f Used • ₹%.2f Remaining", totalSpent, remainingBudget),
                        fontSize = 14.sp,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Category Allocations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (breakdown.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No category budgets set for this month.", color = Color(0xFF64748B), fontSize = 15.sp)
                    }
                } else {
                    breakdown.forEach { item ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(item.color))
                        } catch (e: Exception) {
                            Color(0xFF3B82F6)
                        }
                        
                        BudgetAllocationItem(
                            allocation = BudgetAllocation(
                                name = item.categoryName,
                                spent = String.format("₹%.2f", item.spent),
                                total = String.format("₹%.2f", item.limit),
                                progress = item.utilizationPercentage.toFloat() / 100f,
                                color = color
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun BudgetAllocationItem(allocation: BudgetAllocation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = allocation.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "${allocation.spent} / ${allocation.total}",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
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
            trackColor = allocation.color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

data class BudgetAllocation(
    val name: String,
    val spent: String,
    val total: String,
    val progress: Float,
    val color: Color
)
