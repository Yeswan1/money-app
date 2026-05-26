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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.ui.components.CustomAreaChart
import com.example.moneymap.ui.components.CustomBarChart
import com.example.moneymap.ui.components.CustomPieChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Weekly", "Monthly")

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
                WeeklyReportView()
            } else {
                MonthlyReportView()
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun WeeklyReportView() {
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
                    text = "$642.30 spent this week",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val weeklyData = listOf(
                    Pair("Mon", 45f),
                    Pair("Tue", 120f),
                    Pair("Wed", 30f),
                    Pair("Thu", 0f),
                    Pair("Fri", 250f),
                    Pair("Sat", 110f),
                    Pair("Sun", 87f)
                )
                
                CustomBarChart(
                    data = weeklyData,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    barColor = Color(0xFF3B82F6)
                )
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
                CategoryReportItem("Food & Drink", "$210", 32, Color(0xFFFF7A00))
                CategoryReportItem("Shopping", "$180", 28, Color(0xFFEC4899))
                CategoryReportItem("Transport", "$120", 18, Color(0xFF3B82F6))
                CategoryReportItem("Bills", "$90", 14, Color(0xFFEAB308))
                CategoryReportItem("Others", "$42", 8, Color(0xFF64748B))
            }
        }
    }
}

@Composable
fun MonthlyReportView() {
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
                
                val trendData = listOf(
                    Pair("Jan", 2100f),
                    Pair("Feb", 1800f),
                    Pair("Mar", 2400f),
                    Pair("Apr", 2200f),
                    Pair("May", 2876f)
                )
                
                CustomAreaChart(
                    data = trendData,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    lineColor = Color(0xFF8B5CF6)
                )
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
                val pieData = listOf(
                    Pair("Housing", 1200f),
                    Pair("Food", 450f),
                    Pair("Transport", 340f),
                    Pair("Shopping", 210f),
                    Pair("Others", 180f)
                )
                
                val colors = listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFFFF7A00),
                    Color(0xFF3B82F6),
                    Color(0xFFEC4899),
                    Color(0xFF14B8A6)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomPieChart(
                        data = pieData,
                        colors = colors,
                        modifier = Modifier.size(160.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        pieData.forEachIndexed { index, pair ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors[index]))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = pair.first, fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                    Text(text = "$${pair.second.toInt()}", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
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
