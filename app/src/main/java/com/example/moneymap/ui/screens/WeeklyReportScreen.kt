package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.ui.components.CustomBarChart
import com.example.moneymap.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Weekly Report", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                        }
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "This Week's Spending",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "₹642.30",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    val weeklyData = listOf(
                        Pair("M", 45f),
                        Pair("T", 120f),
                        Pair("W", 30f),
                        Pair("T", 0f),
                        Pair("F", 250f),
                        Pair("S", 110f),
                        Pair("S", 87f)
                    )
                    
                    CustomBarChart(
                        data = weeklyData,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        barColor = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Category Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryReportItem("Food", "₹210", 32, Color(0xFFFF7A00))
                CategoryReportItem("Shopping", "₹180", 28, Color(0xFFEC4899))
                CategoryReportItem("Bills", "₹90", 14, Color(0xFFEAB308))
            }
        }
    }
}
