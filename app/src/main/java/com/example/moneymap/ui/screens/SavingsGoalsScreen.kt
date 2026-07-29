package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.data.model.SavingsGoalDto
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    onBack: () -> Unit,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var goals by remember { mutableStateOf<List<SavingsGoalDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getSavingsGoals()
            .onSuccess { loaded ->
                goals = loaded
            }
            .onFailure { errorMessage = it.message ?: "Could not load savings goals." }
        isLoading = false
    }

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

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No savings goals created yet.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(goals) { goal ->
                        val percentVal = if (goal.targetAmount > 0.0) (goal.currentAmount / goal.targetAmount) else 0.0
                        val percentText = String.format("%.0f%%", percentVal * 100)
                        val color = try {
                            if (goal.color != null) {
                                Color(android.graphics.Color.parseColor(goal.color))
                            } else {
                                Color(0xFF3B82F6)
                            }
                        } catch (e: Exception) {
                            Color(0xFF3B82F6)
                        }

                        Box(modifier = Modifier.clickable { onGoalClick(goal.name) }) {
                            SavingsGoalCard(
                                title = goal.name,
                                progressText = String.format("₹%.0f / ₹%.0f", goal.currentAmount, goal.targetAmount),
                                percentage = percentText,
                                progress = percentVal.toFloat(),
                                icon = iconForGoal(goal.name),
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    title: String,
    progressText: String,
    percentage: String,
    progress: Float,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                }
                Text(percentage, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Saved", fontSize = 13.sp, color = Color(0xFF64748B))
                Text(progressText, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = Math.min(1.0f, progress),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}

private fun iconForGoal(name: String): ImageVector {
    return when (name.lowercase(Locale.US)) {
        "vacation", "flight", "trip" -> Icons.Default.Flight
        "car", "vehicle" -> Icons.Default.DirectionsCar
        "home", "house" -> Icons.Default.Home
        else -> Icons.Default.Security
    }
}
