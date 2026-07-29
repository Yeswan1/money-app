package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.example.moneymap.ui.components.CustomAreaChart
import com.example.moneymap.ui.components.CustomBarChart
import com.example.moneymap.ui.components.CustomPieChart
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.data.model.WeeklyReportResponse
import com.example.moneymap.data.model.MonthlyReportResponse
import com.example.moneymap.data.model.SpendingTrendDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Weekly", "Monthly")

    var weeklyReport by remember { mutableStateOf<WeeklyReportResponse?>(null) }
    var monthlyReport by remember { mutableStateOf<MonthlyReportResponse?>(null) }
    var spendingTrends by remember { mutableStateOf<List<SpendingTrendDto>>(emptyList()) }
    var isLoadingWeekly by remember { mutableStateOf(true) }
    var isLoadingMonthly by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getWeeklyReport().onSuccess {
            weeklyReport = it
            isLoadingWeekly = false
        }.onFailure {
            isLoadingWeekly = false
        }

        repository.getMonthlyReport().onSuccess {
            monthlyReport = it
            isLoadingMonthly = false
        }.onFailure {
            isLoadingMonthly = false
        }

        repository.getSpendingTrends().onSuccess {
            spendingTrends = it
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Reports Dashboard", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Period", tint = Color(0xFF0F172A))
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
            // Tabs
            Surface(color = Color.White, shadowElevation = 2.dp) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFF3B82F6)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) Color(0xFF3B82F6) else Color(0xFF64748B)
                                ) 
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTabIndex == 0) {
                if (isLoadingWeekly) {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                } else {
                    WeeklyReportView(weeklyReport)
                }
            } else {
                if (isLoadingMonthly) {
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                } else {
                    MonthlyReportView(monthlyReport, spendingTrends)
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun WeeklyReportView(report: WeeklyReportResponse?) {
    val totalSpent = report?.totalSpent ?: 0.0
    val dailyTrend = report?.dailyTrend ?: emptyList()
    val breakdown = report?.breakdown ?: emptyList()

    Column(modifier = Modifier.padding(16.dp)) {
        // 7-Day Spending Analysis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "7-Day Spending Analysis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("₹%.2f spent this week", totalSpent),
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val weeklyData = dailyTrend.map { Pair(it.day.substring(0, Math.min(3, it.day.length)), it.amount.toFloat()) }
                
                if (weeklyData.isNotEmpty()) {
                    CustomBarChart(
                        data = weeklyData,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        barColor = Color(0xFF3B82F6)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available", color = Color(0xFF64748B))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Category Analytics",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (breakdown.isEmpty()) {
                    Text("No transactions logged this week.", color = Color(0xFF64748B), modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    breakdown.forEach { item ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(item.color))
                        } catch (e: Exception) {
                            Color(0xFF3B82F6)
                        }
                        CategoryReportItem(item.category, String.format("₹%.2f", item.amount), item.percentage.toInt(), color)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyReportView(report: MonthlyReportResponse?, trends: List<SpendingTrendDto>) {
    val breakdown = report?.breakdown ?: emptyList()

    Column(modifier = Modifier.padding(16.dp)) {
        // 5-Month Spending Trends
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Spending Trends",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Compared to previous months",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val trendData = trends.map { Pair(it.monthName, it.expenses.toFloat()) }
                
                if (trendData.isNotEmpty()) {
                    CustomAreaChart(
                        data = trendData,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        lineColor = Color(0xFF8B5CF6)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No trends data available", color = Color(0xFF64748B))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Expense Charts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val pieData = breakdown.map { Pair(it.category, it.amount.toFloat()) }
                
                val colors = breakdown.map {
                    try {
                        Color(android.graphics.Color.parseColor(it.color))
                    } catch (e: Exception) {
                        Color(0xFF3B82F6)
                    }
                }
                
                if (pieData.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomPieChart(
                            data = pieData,
                            colors = colors,
                            modifier = Modifier.size(140.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            pieData.forEachIndexed { index, pair ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors[index % colors.size]))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = pair.first, fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                        Text(text = String.format("₹%.2f", pair.second), fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No category data available this month.", color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryReportItem(name: String, amount: String, percentage: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(12.dp))
                Text(name, fontSize = 15.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
            }
            Text(amount, fontSize = 15.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "$percentage%",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
