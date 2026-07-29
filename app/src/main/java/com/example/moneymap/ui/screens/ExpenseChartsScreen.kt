package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.ui.components.CustomPieChart
import com.example.moneymap.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseChartsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Expense Analytics", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Expense Distribution",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
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
                    
                    CustomPieChart(
                        data = pieData,
                        colors = colors,
                        modifier = Modifier.size(240.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        pieData.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors[index]))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = pair.first, fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                                }
                                Text(text = "₹${pair.second.toInt()}", fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
