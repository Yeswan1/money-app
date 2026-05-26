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
import com.example.moneymap.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAnalyticsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = { Text("Category Analytics", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
            Text(
                "Spending by Category",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    CategoryReportItem("Housing & Rent", "$1,200", 42, Color(0xFF8B5CF6))
                    CategoryReportItem("Food & Dining", "$450", 16, Color(0xFFFF7A00))
                    CategoryReportItem("Transport", "$340", 12, Color(0xFF3B82F6))
                    CategoryReportItem("Shopping", "$210", 8, Color(0xFFEC4899))
                    CategoryReportItem("Education", "$150", 6, Color(0xFFF59E0B))
                    CategoryReportItem("Bills & Utilities", "$120", 5, Color(0xFFEAB308))
                    CategoryReportItem("Entertainment", "$90", 4, Color(0xFF14B8A6))
                    CategoryReportItem("Others", "$216", 7, Color(0xFF64748B))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
