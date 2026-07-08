package com.example.chess.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAuthChoiceScreen(viewModel: ChessViewModel) {
    var showAccountChooser by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    
    // Gradient backgrounds for dark cosmic theme
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Elegant Brand Logo Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Application Title
            Text(
                text = "KILLFISH CHESS",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "The Intelligent Neural Trainer",
                fontSize = 16.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Description card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Choose your profile type to begin. Guest mode stores data locally, while Google Accounts unlock 10 minutes of Free Premium Trial with Cloud synchronization.",
                        fontSize = 14.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Google Sign In Button
            Button(
                onClick = { showAccountChooser = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_signin_button"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Simulated Google 'G' icon colors
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google Icon",
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue as Guest Button
            OutlinedButton(
                onClick = {
                    // Continue as Guest mode
                    viewModel.playerTag = "Guest"
                    viewModel.currentUserId = -1L
                    viewModel.hasChosenAuthProfile = true
                    viewModel.determineNextScreenAfterAuth()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                border = BorderStroke(1.5.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("continue_as_guest_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = "Guest Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue as Guest",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Google Account Chooser bottom dialog
    if (showAccountChooser) {
        Dialog(
            onDismissRequest = { showAccountChooser = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .testTag("google_account_chooser_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Title header matching Google Sign In dialog
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Sign In",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Choose an account",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "to continue to KillFish Chess",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAccountChooser = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF4B5563)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // Account list (Pre-seeded with simulated accounts)
                        val simulatedAccounts = listOf(
                            GoogleSimulatedProfile("Magnus Carlsen", "magnus.carlsen@gmail.com", "👑"),
                            GoogleSimulatedProfile("Hikaru Nakamura", "hikaru.nakamura@gmail.com", "🍍"),
                            GoogleSimulatedProfile("Chess Enthusiast", "chess.enthusiast@gmail.com", "♟️")
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(simulatedAccounts) { profile ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.hasChosenAuthProfile = true
                                            viewModel.signInWithGoogleAccount(profile.name, profile.email, profile.avatar)
                                            showAccountChooser = false
                                        }
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                        .testTag("account_item_${profile.email.replace("@", "_")}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF3F4F6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = profile.avatar, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = profile.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1F2937)
                                        )
                                        Text(
                                            text = profile.email,
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }

                            // Add account button
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            showAddAccountDialog = true
                                        }
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                        .testTag("add_account_button")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEBF5FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add account",
                                            tint = Color(0xFF1D4ED8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Add another account",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "By signing in, Google will share your name, email address, and profile picture with KillFish Chess. Review our terms of service.",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Add another account dialog simulator
    if (showAddAccountDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newEmail.contains("@")) {
                            viewModel.hasChosenAuthProfile = true
                            viewModel.signInWithGoogleAccount(newName, newEmail, "♟️")
                            showAddAccountDialog = false
                            showAccountChooser = false
                        } else {
                            isError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    modifier = Modifier.testTag("confirm_add_account_button")
                ) {
                    Text("Add & Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancel", color = Color(0xFF4B5563))
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Account",
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Google Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Create a custom profile address to simulate real Google sign-in credentials.",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                            isError = false
                        },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Magnus Carlsen") },
                        isError = isError,
                        modifier = Modifier.fillMaxWidth().testTag("add_account_name_field"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = {
                            newEmail = it
                            isError = false
                        },
                        label = { Text("Gmail Address") },
                        placeholder = { Text("e.g. magnus@gmail.com") },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("add_account_email_field"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (isError) {
                        Text(
                            text = "Please enter a valid name and email address.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

data class GoogleSimulatedProfile(
    val name: String,
    val email: String,
    val avatar: String
)
