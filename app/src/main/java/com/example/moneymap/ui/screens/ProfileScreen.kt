package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.UpdateSettingsRequest
import com.example.moneymap.data.model.UserProfileResponse
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.data.session.AuthSession
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onPaymentMethodsClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val session = remember(context) { AuthSession(context) }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<ProfileDialog?>(null) }

    fun refreshProfile() {
        scope.launch {
            isLoading = true
            repository.getProfile()
                .onSuccess { profile = it }
                .onFailure { errorMessage = it.message ?: "Could not load profile." }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshProfile()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    IconButton(
                        onClick = { dialog = ProfileDialog.EditProfile },
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF6B7280))
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                ProfileHeader(profile)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                SectionTitle("Account Settings")
                ProfileMenuItem("Personal Information", Icons.Default.Person, Color(0xFFEFF6FF), Color(0xFF3B82F6)) {
                    dialog = ProfileDialog.EditProfile
                }
                ProfileMenuItem("Security & Password", Icons.Default.Lock, Color(0xFFFEF2F2), Color(0xFFEF4444)) {
                    dialog = ProfileDialog.Security
                }
                ProfileMenuItem("Payment Methods", Icons.Default.Payment, Color(0xFFF0FDF4), Color(0xFF22C55E)) {
                    onPaymentMethodsClick()
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionTitle("Preferences")
                ProfileMenuItem("Notifications", Icons.Default.Notifications, Color(0xFFFFF7ED), Color(0xFFF97316)) {
                    dialog = ProfileDialog.Notifications
                }
                ProfileMenuItem("Language", Icons.Default.Language, Color(0xFFF5F3FF), Color(0xFF8B5CF6)) {
                    dialog = ProfileDialog.Language
                }
                ProfileMenuItem("Help & Support", Icons.Default.Help, Color(0xFFF0FDFA), Color(0xFF14B8A6)) {
                    dialog = ProfileDialog.Help
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = {
                        session.clear()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Logout",
                        color = Color(0xFFEF4444),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    when (dialog) {
        ProfileDialog.EditProfile -> EditProfileDialog(
            profile = profile,
            onDismiss = { dialog = null },
            onSave = { name, currency ->
                scope.launch {
                    repository.updateSettings(UpdateSettingsRequest(name = name, currency = currency))
                        .onSuccess {
                            profile = it
                            dialog = null
                        }
                        .onFailure { errorMessage = it.message ?: "Could not update profile." }
                }
            }
        )
        ProfileDialog.Security -> InfoDialog(
            title = "Security & Password",
            message = "Password reset is handled from the login screen. Logout and use Forgot Password when your mail service is connected.",
            onDismiss = { dialog = null }
        )
        ProfileDialog.Notifications -> NotificationDialog(
            onDismiss = { dialog = null },
            onSave = { enabled ->
                scope.launch {
                    repository.updateSettings(
                        UpdateSettingsRequest(
                            notificationsEnabled = enabled,
                            budgetAlerts = enabled,
                            goalReminders = enabled,
                            subscriptionReminders = enabled,
                            weeklyReport = enabled,
                            monthlyReport = enabled,
                        )
                    )
                        .onSuccess {
                            profile = it
                            dialog = null
                        }
                        .onFailure { errorMessage = it.message ?: "Could not update notifications." }
                }
            }
        )
        ProfileDialog.Language -> InfoDialog(
            title = "Language",
            message = "English is active for this build.",
            onDismiss = { dialog = null }
        )
        ProfileDialog.Help -> InfoDialog(
            title = "Help & Support",
            message = "Use Add Expense, Budget, Goals, Subscriptions, and AI Assistant to save real data into your MoneyMap database.",
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

@Composable
fun ProfileHeader(profile: UserProfileResponse?) {
    val name = profile?.name ?: "MoneyMap User"
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "MM" }
    val role = profile?.role?.lowercase(Locale.US)?.replaceFirstChar { it.uppercase() } ?: "Personal"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = "$role Account",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(top = 4.dp)
        )
        profile?.email?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF9CA3AF),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFD1D5DB)
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfileResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by remember(profile?.name) { mutableStateOf(profile?.name.orEmpty()) }
    var currency by remember(profile?.currency) { mutableStateOf(profile?.currency ?: "USD") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personal Information") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = currency, onValueChange = { currency = it.uppercase(Locale.US) }, label = { Text("Currency") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), currency.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NotificationDialog(
    onDismiss: () -> Unit,
    onSave: (Boolean) -> Unit,
) {
    var enabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = enabled, onCheckedChange = { enabled = it })
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (enabled) "Notifications enabled" else "Notifications disabled")
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(enabled) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InfoDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private enum class ProfileDialog {
    EditProfile,
    Security,
    Notifications,
    Language,
    Help
}
