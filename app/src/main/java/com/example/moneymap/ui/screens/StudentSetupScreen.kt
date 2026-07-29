package com.example.moneymap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymap.data.model.UpdateProfileRequest
import com.example.moneymap.data.model.UpdateSettingsRequest
import com.example.moneymap.data.repository.MoneyMapRepository
import com.example.moneymap.ui.theme.Primary
import kotlinx.coroutines.launch

@Composable
fun StudentSetupScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { MoneyMapRepository(context) }
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var institution by remember { mutableStateOf("") }
    var yearOfStudy by remember { mutableStateOf("") }
    var allowance by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val years = listOf("1st Year", "2nd Year", "3rd Year", "4th Year")

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Back", color = Color(0xFF6B7280), fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Student Profile",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Tell us more about yourself",
                fontSize = 18.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // University/College
            Text("University/College", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = institution,
                onValueChange = { institution = it },
                placeholder = { Text("Enter your institution name", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Year of Study
            Text("Year of Study", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                OutlinedTextField(
                    value = yearOfStudy,
                    onValueChange = {},
                    placeholder = { Text("Select year", color = Color(0xFF9CA3AF)) },
                    readOnly = true,
                    trailingIcon = { 
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6)
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year) },
                            onClick = {
                                yearOfStudy = year
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly Allowance
            Text("Monthly Allowance/Income", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = allowance,
                onValueChange = { allowance = it },
                placeholder = { Text("500", color = Color(0xFF9CA3AF)) },
                prefix = { Text("₹ ", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    scope.launch {
                        val settingsRes = repository.updateSettings(
                            UpdateSettingsRequest(role = "STUDENT")
                        )
                        settingsRes.onSuccess {
                            val profileRes = repository.updateProfile(
                                UpdateProfileRequest(
                                    institution = institution.trim().takeIf { it.isNotBlank() },
                                    yearOfStudy = yearOfStudy.trim().takeIf { it.isNotBlank() },
                                    monthlyAllowance = allowance.toDoubleOrNull() ?: 5000.0
                                )
                            )
                            profileRes.onSuccess {
                                isSaving = false
                                onContinue()
                            }.onFailure {
                                isSaving = false
                                errorMessage = it.message ?: "Failed to save profile details."
                            }
                        }.onFailure {
                            isSaving = false
                            errorMessage = it.message ?: "Failed to update role settings."
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
