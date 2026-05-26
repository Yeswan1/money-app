package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.DashboardStatsResponse
import com.example.moneymap.data.model.SavingsGoalDto
import com.example.moneymap.data.repository.MoneyMapRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications

@Composable
fun BudgetScreen(
    onWeeklyPlanClick: () -> Unit = {},
    onMonthlyPlanClick: () -> Unit = {},
    onViewAllGoalsClick: () -> Unit = {},
    onCreateGoalClick: () -> Unit = {},
    onGoalClick: (String) -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onTipsClick: () -> Unit = {},
    onChatClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    var dashboardStats by remember { mutableStateOf<DashboardStatsResponse?>(null) }
    var goals by remember { mutableStateOf<List<SavingsGoalDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.getDashboardStats()
            .onSuccess { dashboardStats = it }
            .onFailure { errorMessage = it.message ?: "Could not load budget." }
        repository.getSavingsGoals()
            .onSuccess { goals = it }
            .onFailure { errorMessage = it.message ?: "Could not load goals." }
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Planner",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .clickable { onTipsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Tips", tint = Color(0xFFEAB308))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .clickable { onAlertsClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color(0xFFEF4444))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            BudgetOverviewCard(dashboardStats)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlanCard(
                    modifier = Modifier.weight(1f).clickable { onWeeklyPlanClick() },
                    title = "Weekly",
                    subtitle = "Budget Plan",
                    icon = Icons.Default.CalendarMonth,
                    iconBgColor = Color(0xFF3B82F6)
                )
                PlanCard(
                    modifier = Modifier.weight(1f).clickable { onMonthlyPlanClick() },
                    title = "Monthly",
                    subtitle = "Budget Plan",
                    icon = Icons.Default.CalendarMonth,
                    iconBgColor = Color(0xFFA855F7)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Savings Goals Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Goals",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onViewAllGoalsClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isLoading && goals.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("No savings goals yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Create a goal and it will appear here from your database.", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            } else {
                goals.take(3).forEach { goal ->
                    val progress = if (goal.targetAmount > 0) {
                        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    val percentage = (progress * 100).toInt()
                    Box(modifier = Modifier.clickable { onGoalClick(goal.name) }) {
                        GoalCard(
                            title = goal.name,
                            progressText = "${formatBudgetMoney(goal.currentAmount)} / ${formatBudgetMoney(goal.targetAmount)}",
                            percentage = "$percentage%",
                            progress = progress,
                            icon = Icons.Default.TrackChanges,
                            color = Color(0xFF3B82F6)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Create New Goal Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onCreateGoalClick() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create New Goal",
                        color = Color(0xFF3B82F6),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Extra spacer at the bottom so the FAB doesn't cover content
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Chat FAB
        FloatingActionButton(
            onClick = onChatClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF6366F1),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Chat",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BudgetOverviewCard(stats: DashboardStatsResponse?) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4C6FFF),
            Color(0xFF8A4DFF)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(24.dp)
    ) {
        Column {
            val totalBudget = stats?.budgets?.sumOf { it.limit } ?: 0.0
            val spent = stats?.monthlySpent ?: 0.0
            val percent = if (totalBudget > 0.0) {
                (spent / totalBudget).coerceIn(0.0, 1.0)
            } else {
                0.0
            }
            Text(
                text = "Current Budget Overview",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatBudgetMoney(spent),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Total Budget: ${formatBudgetMoney(totalBudget)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Text(
                    text = "${(percent * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent.toFloat())
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                )
            }
        }
    }
}

private fun formatBudgetMoney(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}

@Composable
fun PlanCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun GoalCard(
    title: String,
    progressText: String,
    percentage: String,
    progress: Float,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progressText,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                Text(
                    text = percentage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = Color(0xFFF1F5F9),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
