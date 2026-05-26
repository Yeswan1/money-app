package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    onBack: () -> Unit,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit
) {
    val goals = listOf(
        GoalSummary("Emergency Fund", "$1,200", "$5,000", "24%", 0.24f, Icons.Default.Security, Color(0xFF10B981)),
        GoalSummary("Vacation to Japan", "$750", "$2,000", "37%", 0.37f, Icons.Default.Flight, Color(0xFF3B82F6)),
        GoalSummary("New Car Downpayment", "$4,500", "$10,000", "45%", 0.45f, Icons.Default.DirectionsCar, Color(0xFFF59E0B)),
        GoalSummary("House Deposit", "$15,000", "$50,000", "30%", 0.3f, Icons.Default.Home, Color(0xFF8B5CF6))
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onAddGoalClick) {
                            Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color(0xFF2563EB))
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
                .background(Color.White)
        ) {
            HorizontalDivider(color = Color(0xFFF1F5F9))

            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(goals) { goal ->
                    Box(modifier = Modifier.clickable { onGoalClick(goal.title) }) {
                        GoalCard(
                            title = goal.title,
                            progressText = "${goal.current} / ${goal.target}",
                            percentage = goal.percentage,
                            progress = goal.progress,
                            icon = goal.icon,
                            color = goal.color
                        )
                    }
                }
            }
        }
    }
}

data class GoalSummary(
    val title: String,
    val current: String,
    val target: String,
    val percentage: String,
    val progress: Float,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
