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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyBudgetScreen(onBack: () -> Unit) {
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
                    text = "$1,000.00",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$642.30 Used • $357.70 Remaining",
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
            
            val weeks = listOf(
                WeeklyAllocation("Week 1 (May 1 - May 7)", "$850", "$1,000", 0.85f, Color(0xFF10B981), true),
                WeeklyAllocation("Week 2 (May 8 - May 14)", "$642", "$1,000", 0.64f, Color(0xFF3B82F6), true),
                WeeklyAllocation("Week 3 (May 15 - May 21)", "$0", "$1,000", 0.0f, Color(0xFFCBD5E1), false),
                WeeklyAllocation("Week 4 (May 22 - May 28)", "$0", "$1,000", 0.0f, Color(0xFFCBD5E1), false),
                WeeklyAllocation("Week 5 (May 29 - May 31)", "$0", "$400", 0.0f, Color(0xFFCBD5E1), false)
            )
            
            weeks.forEach { allocation ->
                WeeklyAllocationItem(allocation)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun WeeklyAllocationItem(allocation: WeeklyAllocation) {
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
                color = if (allocation.isActive) Color(0xFF0F172A) else Color(0xFF94A3B8)
            )
            Text(
                text = "${allocation.spent} / ${allocation.total}",
                fontSize = 14.sp,
                color = if (allocation.isActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LinearProgressIndicator(
            progress = allocation.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = allocation.color,
            trackColor = if (allocation.isActive) allocation.color.copy(alpha = 0.15f) else Color(0xFFE2E8F0),
            strokeCap = StrokeCap.Round
        )
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
