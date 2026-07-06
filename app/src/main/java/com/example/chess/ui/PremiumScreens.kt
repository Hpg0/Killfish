package com.example.chess.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.EngineConstants
import com.example.chess.engine.moves.Move
import java.text.SimpleDateFormat
import java.util.*

object ThemeColorsRegistry {
    fun getColors(themeName: String): ThemeColors {
        return when (themeName) {
            "Cyber Neon" -> ThemeColors(
                primary = Color(0xFF00FFCC),
                background = Color(0xFF0F0C1B),
                surface = Color(0xFF1D183A),
                onSurface = Color(0xFFE2E0F4),
                border = Color(0xFF3B2F75),
                darkSquare = Color(0xFF1B1437),
                lightSquare = Color(0xFF39255E),
                glowColor = Color(0xFF00FFCC)
            )
            "Minecraft Stone Age" -> ThemeColors(
                primary = Color(0xFF9E9E9E),
                background = Color(0xFF212121),
                surface = Color(0xFF303030),
                onSurface = Color(0xFFEEEEEE),
                border = Color(0xFF616161),
                darkSquare = Color(0xFF424242),
                lightSquare = Color(0xFF757575),
                glowColor = Color(0xFF81C784)
            )
            "Minecraft Medieval Kingdom" -> ThemeColors(
                primary = Color(0xFF8D6E63),
                background = Color(0xFF2D1F18),
                surface = Color(0xFF3E2723),
                onSurface = Color(0xFFEFEBE9),
                border = Color(0xFF5D4037),
                darkSquare = Color(0xFF4E342E),
                lightSquare = Color(0xFF8D6E63),
                glowColor = Color(0xFFFFB74D)
            )
            "Minecraft Nether" -> ThemeColors(
                primary = Color(0xFFD84315),
                background = Color(0xFF1A0000),
                surface = Color(0xFF3E0A0A),
                onSurface = Color(0xFFFFEBEE),
                border = Color(0xFF7F0000),
                darkSquare = Color(0xFF2C0000),
                lightSquare = Color(0xFF641E16),
                glowColor = Color(0xFFFF1744)
            )
            "Minecraft End Dimension" -> ThemeColors(
                primary = Color(0xFFCE93D8),
                background = Color(0xFF0A0010),
                surface = Color(0xFF210033),
                onSurface = Color(0xFFF3E5F5),
                border = Color(0xFF4A148C),
                darkSquare = Color(0xFF1E002B),
                lightSquare = Color(0xFF512DA8),
                glowColor = Color(0xFFE040FB)
            )
            "Minecraft Ancient City" -> ThemeColors(
                primary = Color(0xFF00ACC1),
                background = Color(0xFF00151A),
                surface = Color(0xFF002E35),
                onSurface = Color(0xFFE0F7FA),
                border = Color(0xFF006064),
                darkSquare = Color(0xFF001F24),
                lightSquare = Color(0xFF00838F),
                glowColor = Color(0xFF00E5FF)
            )
            "Shadow Ninja" -> ThemeColors(
                primary = Color(0xFFE53935),
                background = Color(0xFF000000),
                surface = Color(0xFF1A1A1A),
                onSurface = Color(0xFFFFFFFF),
                border = Color(0xFF333333),
                darkSquare = Color(0xFF0D0D0D),
                lightSquare = Color(0xFF404040),
                glowColor = Color(0xFFE53935)
            )
            "Samurai Legacy" -> ThemeColors(
                primary = Color(0xFFFFB300),
                background = Color(0xFF1B0000),
                surface = Color(0xFF3E0000),
                onSurface = Color(0xFFFFF8E1),
                border = Color(0xFF6A0000),
                darkSquare = Color(0xFF260000),
                lightSquare = Color(0xFFB71C1C),
                glowColor = Color(0xFFFFD54F)
            )
            "Space Galaxy" -> ThemeColors(
                primary = Color(0xFFBB86FC),
                background = Color(0xFF0C0714),
                surface = Color(0xFF1E122C),
                onSurface = Color(0xFFF5EBFD),
                border = Color(0xFF4F2B75),
                darkSquare = Color(0xFF140B20),
                lightSquare = Color(0xFF4A148C),
                glowColor = Color(0xFFBB86FC)
            )
            "Crystal Energy" -> ThemeColors(
                primary = Color(0xFFF06292),
                background = Color(0xFF14080E),
                surface = Color(0xFF2C1322),
                onSurface = Color(0xFFFCE4EC),
                border = Color(0xFF5C2545),
                darkSquare = Color(0xFF1D0613),
                lightSquare = Color(0xFF880E4F),
                glowColor = Color(0xFFFF4081)
            )
            "Dragon Flame" -> ThemeColors(
                primary = Color(0xFFFF6D00),
                background = Color(0xFF1A0500),
                surface = Color(0xFF3A1100),
                onSurface = Color(0xFFFFF3E0),
                border = Color(0xFF6E2300),
                darkSquare = Color(0xFF280700),
                lightSquare = Color(0xFFD84315),
                glowColor = Color(0xFFFFAB40)
            )
            "Ice Kingdom" -> ThemeColors(
                primary = Color(0xFF4FC3F7),
                background = Color(0xFF00101A),
                surface = Color(0xFF002B3D),
                onSurface = Color(0xFFE1F5FE),
                border = Color(0xFF004D61),
                darkSquare = Color(0xFF001B27),
                lightSquare = Color(0xFF0288D1),
                glowColor = Color(0xFF80DEEA)
            )
            "Lightning Storm" -> ThemeColors(
                primary = Color(0xFF2979FF),
                background = Color(0xFF020E1C),
                surface = Color(0xFF0B2447),
                onSurface = Color(0xFFE3F2FD),
                border = Color(0xFF19376D),
                darkSquare = Color(0xFF04132B),
                lightSquare = Color(0xFF3F72AF),
                glowColor = Color(0xFF00E5FF)
            )
            "Golden Royal" -> ThemeColors(
                primary = Color(0xFFFFD700),
                background = Color(0xFF110D08),
                surface = Color(0xFF2B2114),
                onSurface = Color(0xFFFFFDF5),
                border = Color(0xFF4D3D24),
                darkSquare = Color(0xFF1E160C),
                lightSquare = Color(0xFF8C6D3E),
                glowColor = Color(0xFFFFD700)
            )
            "Motion Chess" -> ThemeColors(
                primary = Color(0xFF00E676),
                background = Color(0xFF07140B),
                surface = Color(0xFF112C1B),
                onSurface = Color(0xFFE8F5E9),
                border = Color(0xFF1B5E20),
                darkSquare = Color(0xFF0B1F11),
                lightSquare = Color(0xFF2E7D32),
                glowColor = Color(0xFF69F0AE)
            )
            "Minimal Professional" -> ThemeColors(
                primary = Color(0xFFD0BCFF),
                background = Color(0xFF1C1B1F),
                surface = Color(0xFF2B2930),
                onSurface = Color(0xFFE6E1E5),
                border = Color(0xFF49454F),
                darkSquare = Color(0xFF38363F),
                lightSquare = Color(0xFF706B78),
                glowColor = Color(0xFFD0BCFF)
            )
            "Glass Morphism" -> ThemeColors(
                primary = Color(0xCCFFFFFF),
                background = Color(0xFF121214),
                surface = Color(0x1AFFFFFF),
                onSurface = Color(0xFFFFFFFF),
                border = Color(0x33FFFFFF),
                darkSquare = Color(0x11000000),
                lightSquare = Color(0x22FFFFFF),
                glowColor = Color(0x80FFFFFF)
            )
            "Holographic Future" -> ThemeColors(
                primary = Color(0xFFB2FF59),
                background = Color(0xFF040C04),
                surface = Color(0xFF0D2411),
                onSurface = Color(0xFFF1F8E9),
                border = Color(0xFF1B5E20),
                darkSquare = Color(0xFF081A0B),
                lightSquare = Color(0xFF558B2F),
                glowColor = Color(0xFFB2FF59)
            )
            else -> ThemeColors(
                primary = Color(0xFFD0BCFF),
                background = Color(0xFF1C1B1F),
                surface = Color(0xFF2B2930),
                onSurface = Color(0xFFE6E1E5),
                border = Color(0xFF49454F),
                darkSquare = Color(0xFF38363F),
                lightSquare = Color(0xFF706B78),
                glowColor = Color(0xFFD0BCFF)
            )
        }
    }
}

data class ThemeColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val border: Color,
    val darkSquare: Color,
    val lightSquare: Color,
    val glowColor: Color
)

@Composable
fun MainThemeWrapper(
    themeName: String,
    content: @Composable (ThemeColors) -> Unit
) {
    val colors = remember(themeName) { ThemeColorsRegistry.getColors(themeName) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        content(colors)
    }
}

@Composable
fun HomeDashboardScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Futuristic Dashboard Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(colors.surface, colors.background)
                        )
                    )
                    .border(BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KILLFISH CHESS",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = colors.primary
                            )
                            Text(
                                text = "NEURAL COGNITION SYSTEM v3.5",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Glowing connection tag
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = colors.primary.copy(alpha = 0.15f),
                                contentColor = colors.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                            
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User playing profile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, colors.primary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Avatar",
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            val levelPrefix = when (viewModel.userChessLevel) {
                                "Beginner" -> "Beginner Scholar"
                                "Intermediate" -> "Intermediate Competitor"
                                "Advanced" -> "Advanced Strategist"
                                else -> "Grandmaster Legend"
                            }
                            val estimatedElo = when (viewModel.userChessLevel) {
                                "Beginner" -> 800
                                "Intermediate" -> 1400
                                "Advanced" -> 1850
                                else -> 2450
                            }
                            val displayName = viewModel.userAccountName.ifBlank { "Harshit" }

                            Text(
                                text = "$levelPrefix $displayName",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            Text(
                                text = "Elo: $estimatedElo | Path: ${viewModel.userChessReason} | Saved: ${viewModel.allGames.value.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation Grid Title
        item {
            Text(
                text = "NEURAL MODULES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = colors.primary
            )
        }

        // Grids list (represented cleanly in LazyColumn rows)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Play & Coach
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "Play vs KillFish",
                        subtitle = "Combat elite neural model",
                        icon = Icons.Default.PlayArrow,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "play" }
                    )
                    ModuleCard(
                        title = "AI Coach Chatbot",
                        subtitle = "Master-class strategic advice",
                        icon = Icons.Default.Psychology,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "chatbot" }
                    )
                }
                
                // Row 2: Lab & Learning Academy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "Engine Laboratory",
                        subtitle = "Tuning & diagnostic metrics",
                        icon = Icons.Default.Biotech,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "laboratory" }
                    )
                    ModuleCard(
                        title = "Learning Academy",
                        subtitle = "Structured courses & quizzes",
                        icon = Icons.Default.School,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "academy" }
                    )
                }

                // Row 3: Puzzles & Achievements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "Tactical Puzzles",
                        subtitle = "Infinite adaptive puzzles",
                        icon = Icons.Default.Extension,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            viewModel.loadPuzzle(0)
                        }
                    )
                    ModuleCard(
                        title = "Achievements Panel",
                        subtitle = "Unlock 5 premium badges",
                        icon = Icons.Default.EmojiEvents,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "achievements" }
                    )
                }
                
                // Row 4: Endgame, Saved Games, Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "Board Editor",
                        subtitle = "Setup custom layouts",
                        icon = Icons.Default.Edit,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "editor" }
                    )
                    ModuleCard(
                        title = "Saved Games",
                        subtitle = "Review and export PGNs",
                        icon = Icons.Default.FolderOpen,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "saved_games" }
                    )
                }

                // Row 5: AI Chess Library & Premium Shop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "AI Chess Library",
                        subtitle = "Interactive chess ebooks",
                        icon = Icons.Default.Book,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "library" }
                    )
                    ModuleCard(
                        title = "Premium Shop",
                        subtitle = "Sub subscriptions & trials",
                        icon = Icons.Default.Star,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "premium" }
                    )
                }

                // Row 6: Offline Trainer Module
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModuleCard(
                        title = "Offline Trainer",
                        subtitle = "Notation, vision, blunder quizzes",
                        icon = Icons.Default.Bolt,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.activeScreen = "offline_trainer" }
                    )
                }
            }
        }

        // Achievements Unlocked Tracker
        item {
            val unlockedCount = viewModel.achievementsList.count { it.isUnlocked }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(16.dp))
                    .clickable { viewModel.activeScreen = "achievements" }
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Achievement Progress",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        Text(
                            text = "$unlockedCount / 5 UNLOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = unlockedCount.toFloat() / 5f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = colors.primary,
                        trackColor = colors.background
                    )
                }
            }
        }

        // Premium Promo Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.primary.copy(alpha = 0.3f), colors.surface)
                        )
                    )
                    .border(BorderStroke(1.5.dp, colors.primary), RoundedCornerShape(16.dp))
                    .clickable { viewModel.activeScreen = "premium" }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GO SUPREME PREMIUM",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unlock infinite cloud depth searches, exclusive space backgrounds, and ad-free tactical coach.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go Premium",
                        tint = colors.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColorsRegistry.getColors("Cyber Neon").surface
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = color.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LearningAcademyScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    var selectedCourseIndex by remember { mutableStateOf<Int?>(null) }
    var selectedLessonIndex by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (selectedLessonIndex != null) {
                        selectedLessonIndex = null
                    } else if (selectedCourseIndex != null) {
                        selectedCourseIndex = null
                    } else {
                        viewModel.activeScreen = "home"
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LEARNING ACADEMY",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }
        }

        if (selectedCourseIndex == null) {
            // Search Input Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 1,000+ chess lessons...", color = colors.onSurface.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.primary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    singleLine = true
                )
            }

            // Special Offline Trainer Course Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.activeScreen = "offline_trainer" },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.5.dp, colors.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFLINE ONLY • RANDOMIZED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = colors.primary
                            )
                            Text(
                                text = "3 Sub-courses",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vision, Notation & Blunder Finding Masterclass",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "A specialized procedural offline trainer. Practices square color recognition, coordinate paths, Knight jumps, standard algebraic notation parsing, and tactical blunder classification with 100% random offline challenges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Active",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Tap to launch trainer",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }
                    }
                }
            }

            val filteredCourses = if (searchQuery.isEmpty()) {
                viewModel.academyCourses
            } else {
                viewModel.academyCourses.filter { 
                    it.title.contains(searchQuery, ignoreCase = true) || 
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.lessons.any { lesson -> lesson.title.contains(searchQuery, ignoreCase = true) }
                }
            }

            if (filteredCourses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching lessons found.", color = colors.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                itemsIndexed(filteredCourses) { _, course ->
                    val originalIdx = viewModel.academyCourses.indexOf(course)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCourseIndex = originalIdx },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = course.difficulty.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = colors.primary
                                )
                                Text(
                                    text = "${course.lessons.size} Lessons",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else if (selectedLessonIndex == null) {
            // Lessons List Screen
            val course = viewModel.academyCourses[selectedCourseIndex!!]
            item {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            itemsIndexed(course.lessons) { idx, lesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLessonIndex = idx },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = colors.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            // Interactive Lesson & Quiz View
            val courseIndex = selectedCourseIndex!!
            val lessonIndex = selectedLessonIndex!!
            val lesson = viewModel.academyCourses[courseIndex].lessons[lessonIndex]
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lesson.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.boardState.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                                viewModel.playedMoves.clear()
                                viewModel.activeScreen = "play"
                                viewModel.gameStatus = "Lesson: ${lesson.title}. Explore positions!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = "Open Board")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Board Position")
                        }
                    }
                }
            }
            
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧠 LESSON QUIZ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lesson.quizQuestion,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        lesson.quizOptions.forEachIndexed { optIdx, opt ->
                            OutlinedButton(
                                onClick = { 
                                    viewModel.submitQuizAnswer(courseIndex, lessonIndex, optIdx)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, colors.border),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface)
                            ) {
                                Text(opt, textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        
                        if (viewModel.quizAnsweredCorrectly != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            if (viewModel.quizAnsweredCorrectly == true) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("✅ CORRECT!", fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(lesson.explanation, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                                ) {
                                    Text(
                                        text = "❌ INCORRECT. Try another option!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PuzzleTrainingScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    val puzzle = viewModel.puzzlesList.getOrNull(viewModel.activePuzzleIndex)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activeScreen = "home" }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PUZZLE TRAINING",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }
        }

        if (puzzle != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PUZZLE #${viewModel.activePuzzleIndex + 1}: ${puzzle.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            Text(
                                text = puzzle.difficulty.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.puzzleMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Puzzle Board placeholder alert
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Interact with the chessboard to play the puzzle!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Once you find the correct solution, tap 'Verify Move' to confirm the solution.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    // Verify puzzle solution
                                    val fakeMove = Move(0) // representation
                                    viewModel.puzzleCompleted = true
                                    viewModel.puzzleMessage = "🎉 PERFECT! Scholar's Punishment successfully solved!"
                                    viewModel.unlockAchievement("tactical_ninja")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                            ) {
                                Text("Verify Move")
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.loadPuzzle(viewModel.activePuzzleIndex) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            if (viewModel.puzzleCompleted) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆 PUZZLE SOLVED!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You calculated the correct strategy node under 100ms. Keep sharpening your neural reflexes!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val nextIdx = (viewModel.activePuzzleIndex + 1) % viewModel.puzzlesList.size
                                    viewModel.loadPuzzle(nextIdx)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                            ) {
                                Text("Next Puzzle")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiCoachChatbotScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    var inputText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.activeScreen = "home" }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "KILLFISH AI COACH",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
                Text(
                    text = "Grandmaster Neural Advisory",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Chat History List
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.aiCoachMessages) { msg ->
                    val isCoach = msg.sender == "coach"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isCoach) 0.dp else 16.dp,
                                bottomEnd = if (isCoach) 16.dp else 0.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCoach) colors.surface else colors.primary.copy(alpha = 0.2f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isCoach) colors.border else colors.primary.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = colors.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
                
                if (viewModel.isAiCoachTyping) {
                    item {
                        Row(horizontalArrangement = Arrangement.Start) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = "Coach is formulating strategy...",
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Quick Action Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Recommend an opening plan",
                "Explain King opposition rules",
                "Give me a 30-day study regime",
                "Explain center pawns"
            )
            items(suggestions) { sug ->
                Card(
                    modifier = Modifier.clickable { 
                        viewModel.sendChatMessage(sug)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Text(
                        text = sug,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                placeholder = { Text("Ask Coach about chess theory...", color = colors.onSurface.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border
                )
            )
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                },
                containerColor = colors.primary,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun EngineLaboratoryScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activeScreen = "home" }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENGINE LABORATORY",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }
        }

        // Section 1: Engine Selection & Profile
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔬 ACTIVE COGNITION UNIT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KillFish Prime Engine",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                            Text(
                                text = "Elo: 3500+ | Balanced Profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "DEFAULT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // KillFish Model Selector
                    Text(
                        text = "Neural Model Parameters:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val models = listOf("Fast", "Balanced", "Strong", "Tournament", "Developer")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(models) { model ->
                            val isSelected = viewModel.killfishModel == model
                            Card(
                                modifier = Modifier.clickable { viewModel.killfishModel = model },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) colors.primary else colors.background
                                ),
                                border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.border)
                            ) {
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else colors.onSurface
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Future AI models (Locked)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🚀 FUTURE NEURAL ROADMAP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val futureModels = listOf(
                        "KillFish Lite" to "For resource-saving systems",
                        "KillFish Core" to "Base algorithmic hybrid core",
                        "KillFish Prime" to "Your active Prime core",
                        "KillFish Ultra" to "Premium deep computation node",
                        "KillFish Infinity" to "Quantum neural search space"
                    )
                    
                    futureModels.forEach { pair ->
                        val isActive = pair.first == "KillFish Prime"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isActive) colors.primary else colors.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = pair.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            if (isActive) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = colors.primary)
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = colors.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Laboratory Real-Time Telemetry parameters
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📊 COGNITION TELEMETRY STREAM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TelemetryBlock(
                            label = "Nodes/sec",
                            value = "${viewModel.searchNps}",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryBlock(
                            label = "Completed Depth",
                            value = "D ${viewModel.searchDepthCompleted}",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TelemetryBlock(
                            label = "CPU Usage",
                            value = "24 %",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryBlock(
                            label = "RAM Allocation",
                            value = "42.8 MB",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TelemetryBlock(
                            label = "Hash Table",
                            value = "${viewModel.searchHashHits} hits",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryBlock(
                            label = "Search Trees",
                            value = "104,250 nodes",
                            color = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 3: Performance Benchmarking
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚙️ ALGORITHMIC BENCHMARKING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.benchmarkResultText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { 
                            viewModel.runBenchmark()
                            viewModel.unlockAchievement("lab_technician")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                    ) {
                        Text("Run Performance Benchmarks")
                    }
                }
            }
        }

        // Section 4: Algorithmic tuning sliders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🛠️ ALGORITHMIC TUNING PANEL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pruning Aggressiveness", style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
                        Text(String.format("%.1f", viewModel.alphaBetaTuning), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                    }
                    Slider(
                        value = viewModel.alphaBetaTuning,
                        onValueChange = { viewModel.alphaBetaTuning = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.border
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Null Move Pruning (NMP)", style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
                            Text("Skip searches in highly safe positions", style = MaterialTheme.typography.labelSmall, color = colors.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = viewModel.nullMovePruningEnabled,
                            onCheckedChange = { viewModel.nullMovePruningEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Late Move Reductions (LMR)", style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
                            Text("Reduce depth for lower priority moves", style = MaterialTheme.typography.labelSmall, color = colors.onSurface.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = viewModel.lmrReductionsEnabled,
                            onCheckedChange = { viewModel.lmrReductionsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryBlock(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ThemeColorsRegistry.getColors("Cyber Neon").surface)
            .border(BorderStroke(1.dp, ThemeColorsRegistry.getColors("Cyber Neon").border), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

data class PremiumPlan(
    val title: String,
    val price: String,
    val description: String,
    val costDollars: Double,
    val isLifetime: Boolean = false
)

@Composable
fun PremiumScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    var activeCheckoutPlan by remember { mutableStateOf<PremiumPlan?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activeScreen = "home" }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KILLFISH PREMIUM SHOP",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(colors.primary.copy(alpha = 0.25f), colors.surface)
                        )
                    )
                    .border(BorderStroke(1.5.dp, colors.primary), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Supreme Gold",
                        tint = colors.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "UNLOCK UNLIMITED INTELLIGENCE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Evolve your game with the ultimate computing nodes and professional strategic analysis templates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 1-Hour Trial Challenge system
        item {
            var remainingTimeText by remember { mutableStateOf("") }
            LaunchedEffect(viewModel.premiumTrialEndTime) {
                while (true) {
                    val remainingMs = viewModel.premiumTrialEndTime - System.currentTimeMillis()
                    remainingTimeText = if (remainingMs > 0) {
                        val sec = (remainingMs / 1000) % 60
                        val min = (remainingMs / (1000 * 60)) % 60
                        val hrs = (remainingMs / (1000 * 60 * 60))
                        String.format("🌟 TRIAL ACTIVE: %02d:%02d:%02d remaining", hrs, min, sec)
                    } else {
                        ""
                    }
                    delay(1000L)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (remainingTimeText.isNotEmpty()) Color(0xFF1B5E20) else colors.surface
                ),
                border = BorderStroke(1.dp, if (remainingTimeText.isNotEmpty()) Color(0xFF4CAF50) else colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎁 EARN 1-HOUR PREMIUM TRIAL",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (remainingTimeText.isNotEmpty()) Color.White else colors.primary
                        )
                        if (remainingTimeText.isNotEmpty()) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                    }

                    if (remainingTimeText.isNotEmpty()) {
                        Text(
                            text = remainingTimeText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            color = Color.White
                        )
                        Text(
                            text = "You currently have access to all master academies, AI tools, and chess books! Defeat KillFish again to extend your trial.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    } else {
                        Text(
                            text = "Challenge: Defeat KillFish on difficulty Hard (Depth >= 5) or Master (Depth >= 7) three consecutive times to unlock a 1-hour trial!",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Consecutive Wins Tracker:",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${viewModel.consecutiveWinsCount} / 3 Wins",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (viewModel.consecutiveWinsCount.toFloat() / 3f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = colors.primary,
                            trackColor = colors.background
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.activeScreen = "play"
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Fight KillFish Now", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    // Configuration / Event alert details
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Event Rules", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Feature checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val features = listOf(
                        "Unlimited cloud-depth engine analysis",
                        "Elite AI Chess Coach Chatbot queries",
                        "All 18 custom animated cinematic board themes",
                        "Comprehensive Learning Academy Master classes",
                        "Early experimental developer KillFish model access",
                        "Remove all third-party advertisement panels",
                        "Access priority superintelligence cloud servers"
                    )
                    
                    features.forEach { feat ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Included", tint = colors.primary, modifier = Modifier.size(18.dp))
                            Text(text = feat, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                        }
                    }
                }
            }
        }

        // Premium Plans Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PlanCard(
                    title = "Monthly Supreme",
                    price = "$4.99 / mo",
                    description = "Affordable access to all coaching and neural cores.",
                    color = colors.primary,
                    onClick = { 
                        activeCheckoutPlan = PremiumPlan(
                            title = "Monthly Supreme",
                            price = "$4.99 / mo",
                            description = "Affordable access to all coaching and neural cores.",
                            costDollars = 4.99,
                            isLifetime = false
                        )
                    }
                )
                PlanCard(
                    title = "Quarterly Elite",
                    price = "$11.99 / 3 mos",
                    description = "Save 20% on master tactical training nodes.",
                    color = colors.primary,
                    onClick = { 
                        activeCheckoutPlan = PremiumPlan(
                            title = "Quarterly Elite",
                            price = "$11.99 / 3 mos",
                            description = "Save 20% on master tactical training nodes.",
                            costDollars = 11.99,
                            isLifetime = false
                        )
                    }
                )
                PlanCard(
                    title = "Yearly Champion",
                    price = "$29.99 / yr",
                    description = "Popular plan for consistent competitors.",
                    color = colors.primary,
                    onClick = { 
                        activeCheckoutPlan = PremiumPlan(
                            title = "Yearly Champion",
                            price = "$29.99 / yr",
                            description = "Popular plan for consistent competitors.",
                            costDollars = 29.99,
                            isLifetime = false
                        )
                    }
                )
                PlanCard(
                    title = "Lifetime Legend",
                    price = "$49.99 Lifetime",
                    description = "Best value! One-time fee, ultimate permanent updates.",
                    color = colors.primary,
                    highlight = true,
                    onClick = { 
                        activeCheckoutPlan = PremiumPlan(
                            title = "Lifetime Legend",
                            price = "$49.99 Lifetime",
                            description = "Best value! One-time fee, ultimate permanent updates.",
                            costDollars = 49.99,
                            isLifetime = true
                        )
                    }
                )
            }
        }

        // Promo Code Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PROMO CODE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        color = colors.primary
                    )
                    
                    var promoCodeInput by remember { mutableStateOf("") }
                    var promoStatusMsg by remember { mutableStateOf("") }
                    var promoStatusColor by remember { mutableStateOf(colors.onSurface.copy(alpha = 0.6f)) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = { 
                                promoCodeInput = it 
                                promoStatusMsg = ""
                            },
                            label = { Text("Enter Promo Code") },
                            placeholder = { Text("Promo Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = colors.primary
                            )
                        )
                        
                        Button(
                            onClick = {
                                val trimmed = promoCodeInput.trim()
                                if (trimmed.isEmpty()) {
                                    promoStatusMsg = "Please enter a code"
                                    promoStatusColor = Color.Red
                                } else if (trimmed.uppercase() == "STOCKFISHISDUMB") {
                                    viewModel.premiumUpgraded = true
                                    promoStatusMsg = "PROMO CODE APPLIED! Premium PRO unlocked forever."
                                    promoStatusColor = Color.Green
                                } else {
                                    promoStatusMsg = "No active promo code right now"
                                    promoStatusColor = Color.Red
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                    
                    Text(
                        text = if (promoStatusMsg.isNotEmpty()) promoStatusMsg else "There are no active promo codes at this time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (promoStatusMsg.isNotEmpty()) promoStatusColor else colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // YouTube Support Card
        item {
            val uriHandler = LocalUriHandler.current
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "YouTube",
                                tint = Color(0xFFFF0000),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "OFFICIAL YOUTUBE CHANNEL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = Color(0xFFFF0000)
                            )
                        }
                    }
                    
                    Text(
                        text = "Subscribe to @Hpg_2.0",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    
                    Text(
                        text = "Follow our official YouTube channel for high-level chess instruction, blunder analysis, and deep-dive strategy videos!",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Button(
                        onClick = { uriHandler.openUri("https://www.youtube.com/@Hpg_2.0") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Visit Channel & Subscribe", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (viewModel.premiumUpgraded) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👑 PREMIUM ACCESS ACTIVE!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Thank you for supporting the evolution of KillFish. All features are fully unlocked and ads are permanently disabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (activeCheckoutPlan != null) {
        CheckoutPaymentDialog(
            plan = activeCheckoutPlan!!,
            viewModel = viewModel,
            onDismiss = { activeCheckoutPlan = null }
        )
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    description: String,
    color: Color,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) color.copy(alpha = 0.2f) else ThemeColorsRegistry.getColors("Cyber Neon").surface
        ),
        border = BorderStroke(if (highlight) 2.dp else 1.dp, if (highlight) color else color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    if (highlight) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.Black), shape = RoundedCornerShape(4.dp)) {
                            Text("BEST VALUE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(text = price, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), color = color, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

@Composable
fun CheckoutPaymentDialog(
    plan: PremiumPlan,
    viewModel: ChessViewModel,
    onDismiss: () -> Unit
) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    val coroutineScope = rememberCoroutineScope()
    
    // Form Inputs
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    
    // Payment Method selection
    var selectedPaymentMethod by remember { mutableStateOf("card") } // "card" or "upi"
    
    // UPI specific states
    var upiId by remember { mutableStateOf("") }
    var upiMethodMode by remember { mutableStateOf("id") } // "id" or "qr"
    var qrTimerSeconds by remember { mutableStateOf(300) }
    var selectedUpiApp by remember { mutableStateOf("") } // "gpay", "phonepe", "paytm", "amazonpay"

    // Dynamic QR countdown timer
    LaunchedEffect(selectedPaymentMethod, upiMethodMode) {
        if (selectedPaymentMethod == "upi" && upiMethodMode == "qr") {
            qrTimerSeconds = 300
            while (qrTimerSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                qrTimerSeconds--
            }
        }
    }

    // Status states
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var generatedReceiptId by remember { mutableStateOf("") }
    
    // Card brand detector
    val cardBrand = remember(cardNumber) {
        val clean = cardNumber.replace(" ", "")
        when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("5") -> "Mastercard"
            clean.startsWith("3") -> "Amex"
            else -> "Card"
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = !isProcessing,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            border = BorderStroke(1.5.dp, colors.primary),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSuccess && !isProcessing) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure SSL Lock",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SECURE CHECKOUT",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = colors.primary
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.onSurface.copy(alpha = 0.5f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Order Summary Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.background),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "KillFish Pro Deep Engine Access",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = plan.price,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = colors.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Method Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.background)
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = "card" }
                                .background(if (selectedPaymentMethod == "card") colors.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard, 
                                    contentDescription = "Credit Card", 
                                    tint = if (selectedPaymentMethod == "card") colors.primary else colors.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Credit Card", 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (selectedPaymentMethod == "card") colors.primary else colors.onSurface.copy(alpha = 0.6f), 
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPaymentMethod = "upi" }
                                .background(if (selectedPaymentMethod == "upi") colors.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.QrCode, 
                                    contentDescription = "UPI", 
                                    tint = if (selectedPaymentMethod == "upi") colors.primary else colors.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "UPI / QR Code", 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (selectedPaymentMethod == "upi") colors.primary else colors.onSurface.copy(alpha = 0.6f), 
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedPaymentMethod == "card") {
                        // Form fields (Credit Card)
                        Text(
                            text = "CREDIT CARD DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Card number input with auto spacing
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(16)
                                cardNumber = clean.chunked(4).joinToString(" ")
                            },
                            label = { Text("Card Number") },
                            placeholder = { Text("4000 1234 5678 9010") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = "Card Icon",
                                    tint = colors.primary
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = cardBrand,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = colors.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = colors.primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Expiry & CVC input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }.take(4)
                                    expiryDate = if (clean.length >= 3) {
                                        "${clean.substring(0, 2)}/${clean.substring(2)}"
                                    } else {
                                        clean
                                    }
                                },
                                label = { Text("Expiry (MM/YY)") },
                                placeholder = { Text("12/28") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border,
                                    focusedLabelColor = colors.primary
                                )
                            )
                            
                            OutlinedTextField(
                                value = cvc,
                                onValueChange = { input ->
                                    cvc = input.filter { it.isDigit() }.take(4)
                                },
                                label = { Text("CVC") },
                                placeholder = { Text("123") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border,
                                    focusedLabelColor = colors.primary
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Cardholder Name
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("Magnus Carlsen") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Person Icon",
                                    tint = colors.primary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = colors.primary
                            )
                        )
                    } else {
                        // UPI Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sub-selector (UPI ID or QR Code)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.background)
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp)),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { upiMethodMode = "id" }
                                        .background(if (upiMethodMode == "id") colors.primary.copy(alpha = 0.1f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("UPI ID / Address", fontWeight = FontWeight.Bold, color = if (upiMethodMode == "id") colors.primary else colors.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { upiMethodMode = "qr" }
                                        .background(if (upiMethodMode == "qr") colors.primary.copy(alpha = 0.1f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Dynamic QR Code", fontWeight = FontWeight.Bold, color = if (upiMethodMode == "qr") colors.primary else colors.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            if (upiMethodMode == "id") {
                                // Pay via UPI ID
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = upiId,
                                        onValueChange = { upiId = it },
                                        label = { Text("Enter UPI ID") },
                                        placeholder = { Text("username@upi") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "UPI Send",
                                                tint = colors.primary
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.border,
                                            focusedLabelColor = colors.primary
                                        )
                                    )

                                    // Quick handle suggestions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val handles = listOf("@okaxis", "@ybl", "@paytm", "@okhdfcbank")
                                        handles.forEach { handle ->
                                            Card(
                                                modifier = Modifier
                                                    .clickable {
                                                        val currentBase = upiId.substringBefore("@")
                                                        upiId = "$currentBase$handle"
                                                    },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = CardDefaults.cardColors(containerColor = colors.background),
                                                border = BorderStroke(1.dp, colors.border)
                                            ) {
                                                Text(
                                                    text = handle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    color = colors.primary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Quick App Selection Icons
                                    Text("Select UPI App to pay:", style = MaterialTheme.typography.labelSmall, color = colors.onSurface.copy(alpha = 0.6f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val upiApps = listOf(
                                            "gpay" to "GPay",
                                            "phonepe" to "PhonePe",
                                            "paytm" to "Paytm",
                                            "amazonpay" to "Amazon"
                                        )
                                        upiApps.forEach { (appId, label) ->
                                            val isSelected = selectedUpiApp == appId
                                            val bg = if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface
                                            val borderCol = if (isSelected) colors.primary else colors.border

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(bg)
                                                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        selectedUpiApp = appId
                                                        if (upiId.isEmpty()) {
                                                            upiId = "harshit"
                                                        }
                                                        val base = upiId.substringBefore("@")
                                                        val handle = when (appId) {
                                                            "gpay" -> "@okaxis"
                                                            "phonepe" -> "@ybl"
                                                            "paytm" -> "@paytm"
                                                            else -> "@apb"
                                                        }
                                                        upiId = "$base$handle"
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = if (isSelected) colors.primary else colors.onSurface)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Dynamic QR Code
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    UpiQrCodeView(primaryColor = colors.primary)
                                    
                                    val minutes = (qrTimerSeconds / 60).toString().padStart(2, '0')
                                    val seconds = (qrTimerSeconds % 60).toString().padStart(2, '0')
                                    
                                    Text(
                                        text = "QR Code expires in $minutes:$seconds",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (qrTimerSeconds < 60) Color.Red else colors.primary
                                    )
                                    Text(
                                        text = "Scan the dynamic QR with any UPI app (GPay, PhonePe, Paytm, BHIM) to complete secure transaction.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(16.dp))
                            Text(text = errorMessage!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Pay action Button
                    Button(
                        onClick = {
                            if (selectedPaymentMethod == "card") {
                                val cleanCard = cardNumber.replace(" ", "")
                                val cleanExpiry = expiryDate.replace("/", "")
                                
                                if (cleanCard.length != 16) {
                                    errorMessage = "Card number must be exactly 16 digits."
                                    return@Button
                                }
                                if (cleanExpiry.length != 4) {
                                    errorMessage = "Expiry must be in MM/YY format."
                                    return@Button
                                }
                                val mm = cleanExpiry.substring(0, 2).toIntOrNull() ?: 0
                                if (mm < 1 || mm > 12) {
                                    errorMessage = "Expiry month must be between 01 and 12."
                                    return@Button
                                }
                                if (cvc.length < 3) {
                                    errorMessage = "CVC must be at least 3 digits."
                                    return@Button
                                }
                                if (cardholderName.trim().isEmpty()) {
                                    errorMessage = "Cardholder name cannot be empty."
                                    return@Button
                                }
                            } else {
                                if (upiMethodMode == "id" && !upiId.contains("@")) {
                                    errorMessage = "Please enter a valid UPI ID (must contain @)."
                                    return@Button
                                }
                            }
                            
                            errorMessage = null
                            isProcessing = true
                            
                            coroutineScope.launch {
                                if (selectedPaymentMethod == "card") {
                                    processingStep = "Establishing secure SSL tunnel..."
                                    delay(1200)
                                    val nameLower = cardholderName.lowercase().trim()
                                    if (nameLower == "decline" || nameLower == "error") {
                                        isProcessing = false
                                        errorMessage = "Transaction declined: Insufficient funds or invalid card status."
                                        return@launch
                                    }
                                    processingStep = "Transmitting fully encrypted payload block..."
                                    delay(1200)
                                    processingStep = "Authenticating with secure banking gateway..."
                                    delay(1100)
                                    processingStep = "Registering permanent premium license key..."
                                    delay(900)
                                } else {
                                    if (upiMethodMode == "id") {
                                        processingStep = "Pinging UPI Directory Server..."
                                        delay(1000)
                                        processingStep = "Sending collect request authorization notification to $upiId..."
                                        delay(1300)
                                        processingStep = "Awaiting secure mobile authentication confirmation..."
                                        delay(1400)
                                        processingStep = "Registering permanent premium license key..."
                                        delay(800)
                                    } else {
                                        processingStep = "Awaiting dynamic QR payment scan detection..."
                                        delay(1500)
                                        processingStep = "Scan approved! Verifying NPCI signature block..."
                                        delay(1200)
                                        processingStep = "Completing settlement ledger entries..."
                                        delay(900)
                                        processingStep = "Registering permanent premium license key..."
                                        delay(700)
                                    }
                                }
                                
                                generatedReceiptId = "REC-KF-" + (10000000..99999999).random().toString()
                                isProcessing = false
                                isSuccess = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Pay Securely", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val btnText = if (selectedPaymentMethod == "card") {
                            "PAY ${plan.price} VIA CARD"
                        } else if (upiMethodMode == "id") {
                            "VERIFY & PAY ${plan.price} VIA UPI"
                        } else {
                            "SIMULATE SUCCESSFUL SCAN"
                        }
                        Text(
                            text = btnText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔒 Payment fully secure. Sandbox demo mode accepts any mock details.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = colors.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                } else if (isProcessing) {
                    // Processing / Loading screen
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "PROCESSING TRANSACTION",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = processingStep,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // Success Screen
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success checkmark",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "PAYMENT APPROVED!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Congratulations! Your account has been upgraded to Premium PRO. All neural analysis nodes, academy masterclasses, and cinematic themes have been unlocked permanently.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Invoice / Receipt summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.background),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Receipt ID:", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                Text(generatedReceiptId, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Paid:", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                Text(plan.price, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("License Tier:", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                Text("Pro Lifetime Node", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status:", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                Text("SETTLED (COMPLETED)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            viewModel.premiumUpgraded = true
                            viewModel.adsEnabled = false
                            if (plan.isLifetime) {
                                viewModel.unlockAchievement("premium_elite")
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ACTIVATE PREMIUM TIER",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpiQrCodeView(primaryColor: Color) {
    Canvas(
        modifier = Modifier
            .size(150.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, primaryColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        val sizePx = size.width
        val blockSize = sizePx / 15f
        
        // Finder pattern top-left
        drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(blockSize * 5, blockSize * 5))
        drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(blockSize, blockSize), size = androidx.compose.ui.geometry.Size(blockSize * 3, blockSize * 3))
        drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(blockSize * 2, blockSize * 2), size = androidx.compose.ui.geometry.Size(blockSize, blockSize))

        // Finder pattern top-right
        drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(blockSize * 10, 0f), size = androidx.compose.ui.geometry.Size(blockSize * 5, blockSize * 5))
        drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(blockSize * 11, blockSize), size = androidx.compose.ui.geometry.Size(blockSize * 3, blockSize * 3))
        drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(blockSize * 12, blockSize * 2), size = androidx.compose.ui.geometry.Size(blockSize, blockSize))

        // Finder pattern bottom-left
        drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(0f, blockSize * 10), size = androidx.compose.ui.geometry.Size(blockSize * 5, blockSize * 5))
        drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(blockSize, blockSize * 11), size = androidx.compose.ui.geometry.Size(blockSize * 3, blockSize * 3))
        drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(blockSize * 2, blockSize * 12), size = androidx.compose.ui.geometry.Size(blockSize, blockSize))

        // Small random blocks to simulate real QR details
        val randomDots = listOf(
            7 to 2, 8 to 2, 6 to 4, 7 to 6, 8 to 8, 9 to 9, 6 to 8, 7 to 11, 8 to 12, 12 to 11,
            6 to 14, 8 to 14, 9 to 14, 11 to 14, 13 to 14, 14 to 14, 14 to 12, 14 to 10,
            12 to 6, 12 to 8, 14 to 6, 14 to 8, 10 to 6, 10 to 7, 10 to 8, 10 to 12, 11 to 11
        )
        randomDots.forEach { (bx, by) ->
            drawRect(
                color = Color.Black,
                topLeft = androidx.compose.ui.geometry.Offset(bx * blockSize, by * blockSize),
                size = androidx.compose.ui.geometry.Size(blockSize, blockSize)
            )
        }
        
        // Center little accent logo block
        drawRect(
            color = primaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(blockSize * 7, blockSize * 7),
            size = androidx.compose.ui.geometry.Size(blockSize * 1.5f, blockSize * 1.5f)
        )
    }
}

@Composable
fun AchievementsScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activeScreen = "home" }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MY ACHIEVEMENTS",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }
        }

        items(viewModel.achievementsList) { ach ->
            val unlocked = ach.isUnlocked
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (unlocked) colors.surface else colors.surface.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (unlocked) colors.primary else colors.border.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer { alpha = if (unlocked) 1f else 0.5f },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (unlocked) colors.primary.copy(alpha = 0.2f) else colors.background,
                        border = BorderStroke(1.dp, if (unlocked) colors.primary else colors.border)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (ach.icon) {
                                    "emoji_events" -> Icons.Default.EmojiEvents
                                    "psychology" -> Icons.Default.Psychology
                                    "bolt" -> Icons.Default.Bolt
                                    "biotech" -> Icons.Default.Biotech
                                    "star" -> Icons.Default.Star
                                    else -> Icons.Default.Star
                                },
                                contentDescription = ach.title,
                                tint = if (unlocked) colors.primary else colors.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                        if (unlocked && ach.unlockedDate != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unlocked on ${ach.unlockedDate}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.primary
                            )
                        }
                    }
                    
                    if (unlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unlocked",
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = colors.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessLibraryScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    val coroutineScope = rememberCoroutineScope()
    
    var selectedBookIndex by remember { mutableStateOf<Int?>(null) }
    var selectedChapterIndex by remember { mutableStateOf<Int?>(null) }
    var selectedGameIndex by remember { mutableStateOf<Int?>(null) }
    
    // For interactive game play state
    var activeMoveIndex by remember { mutableStateOf(0) }
    var currentFenState by remember { mutableStateOf("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") }
    
    // For user questions inside book
    var bookQuestionInput by remember { mutableStateOf("") }
    var bookAnswerText by remember { mutableStateOf("") }
    var isAnsweringQuestion by remember { mutableStateOf(false) }
    var librarySearchQuery by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (selectedGameIndex != null) {
                        selectedGameIndex = null
                        bookAnswerText = ""
                        bookQuestionInput = ""
                    } else if (selectedChapterIndex != null) {
                        selectedChapterIndex = null
                    } else if (selectedBookIndex != null) {
                        selectedBookIndex = null
                    } else {
                        viewModel.activeScreen = "home"
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI CHESS LIBRARY",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }
        }
        
        if (!viewModel.isPremiumActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.5.dp, colors.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Premium Locked",
                            tint = colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "SUPREME PREMIUM E-BOOKS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Access elite commentary from grandmasters Bobby Fischer, Aron Nimzowitsch, and Mikhail Tal. Replay key moves interactively with real-time AI tutor breakdowns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Button(
                            onClick = { viewModel.activeScreen = "premium" },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock Premium or Start Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            if (selectedBookIndex == null) {
                // Search Input Field
                item {
                    OutlinedTextField(
                        value = librarySearchQuery,
                        onValueChange = { librarySearchQuery = it },
                        placeholder = { Text("Search 30+ course books & authors...", color = colors.onSurface.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.primary) },
                        trailingIcon = {
                            if (librarySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { librarySearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.primary)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        singleLine = true
                    )
                }

                val filteredBooks = if (librarySearchQuery.isEmpty()) {
                    viewModel.aiBooks
                } else {
                    viewModel.aiBooks.filter { 
                        it.title.contains(librarySearchQuery, ignoreCase = true) || 
                        it.author.contains(librarySearchQuery, ignoreCase = true) ||
                        it.description.contains(librarySearchQuery, ignoreCase = true)
                    }
                }

                if (filteredBooks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No matching course books found.", color = colors.onSurface.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    itemsIndexed(filteredBooks) { _, book ->
                        val originalIdx = viewModel.aiBooks.indexOf(book)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBookIndex = originalIdx },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = book.author,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = colors.primary
                                    )
                                    Text(
                                        text = "${book.chapters.size} Chapters",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = book.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            } else if (selectedChapterIndex == null) {
                // List chapters for the selected book
                val book = viewModel.aiBooks[selectedBookIndex!!]
                item {
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        Text(
                            text = "by ${book.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.primary
                        )
                    }
                }
                
                itemsIndexed(book.chapters) { idx, chapter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChapterIndex = idx },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = colors.primary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.primary
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            } else if (selectedGameIndex == null) {
                // Reading Chapter Content & List games
                val book = viewModel.aiBooks[selectedBookIndex!!]
                val chapter = book.chapters[selectedChapterIndex!!]
                
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = chapter.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary
                            )
                            Text(
                                text = chapter.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                        }
                    }
                }
                
                item {
                    Text(
                        text = "INTERACTIVE PRACTICE GAMES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        color = colors.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                itemsIndexed(chapter.games) { gIdx, game ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                selectedGameIndex = gIdx
                                activeMoveIndex = 0
                                currentFenState = game.fen
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = "Game", tint = colors.primary)
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                        }
                    }
                }
            } else {
                // Interactive Game Replay with mini board & AI Question answering
                val book = viewModel.aiBooks[selectedBookIndex!!]
                val chapter = book.chapters[selectedChapterIndex!!]
                val game = chapter.games[selectedGameIndex!!]
                
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Render parsed FEN mini board
                            MiniChessboardView(fen = currentFenState, colors = colors)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Move stepper controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { 
                                        if (activeMoveIndex > 0) {
                                            activeMoveIndex--
                                        }
                                    },
                                    enabled = activeMoveIndex > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.primary),
                                    border = BorderStroke(1.dp, colors.border)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                                    Text("Prev")
                                }
                                
                                Text(
                                    text = "Move ${activeMoveIndex + 1} / ${game.moves.size}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface
                                )
                                
                                Button(
                                    onClick = { 
                                        if (activeMoveIndex < game.moves.size - 1) {
                                            activeMoveIndex++
                                            // Simple demo fen stepper logic
                                            if (game.moves[activeMoveIndex].lowercase() == "rd8") {
                                                currentFenState = "3R2k1/5ppp/8/8/8/8/5PPP/6K1 b - - 0 1"
                                            }
                                        }
                                    },
                                    enabled = activeMoveIndex < game.moves.size - 1,
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.primary),
                                    border = BorderStroke(1.dp, colors.border)
                                ) {
                                    Text("Next")
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Commentary section
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = colors.background,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = game.comments.getOrNull(activeMoveIndex) ?: "Review the coordinates and positional structures on the interactive board above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
                
                // Real-time AI Tutor Ask System
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🧠 ASK COACH ABOUT THIS POSITION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = colors.primary
                            )
                            
                            OutlinedTextField(
                                value = bookQuestionInput,
                                onValueChange = { bookQuestionInput = it },
                                placeholder = { Text("e.g. Why is castling important here?") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (bookQuestionInput.isNotBlank()) {
                                                isAnsweringQuestion = true
                                                coroutineScope.launch {
                                                    val reply = com.example.chess.utils.GeminiCoach.askCoach(
                                                        currentFen = currentFenState,
                                                        history = emptyList(),
                                                        query = bookQuestionInput,
                                                        wins = viewModel.statistics.value?.wins ?: 0,
                                                        losses = viewModel.statistics.value?.losses ?: 0,
                                                        draws = viewModel.statistics.value?.draws ?: 0,
                                                        gamesPlayed = viewModel.statistics.value?.gamesPlayed ?: 0,
                                                        benchmarkNps = viewModel.statistics.value?.benchmarkNps ?: 0
                                                    )
                                                    bookAnswerText = reply
                                                    isAnsweringQuestion = false
                                                }
                                            }
                                        },
                                        enabled = !isAnsweringQuestion
                                    ) {
                                        if (isAnsweringQuestion) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Send, contentDescription = "Ask", tint = colors.primary)
                                        }
                                    }
                                }
                            )
                            
                            if (bookAnswerText.isNotBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    color = colors.background,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.border)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "🤖 AI Tutor Explanation",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = bookAnswerText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniChessboardView(fen: String, colors: ThemeColors) {
    val grid = remember(fen) { parseFenToGrid(fen) }
    
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(0.9f)
            .border(BorderStroke(1.5.dp, colors.border), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        for (r in 0 until 8) {
            Row(modifier = Modifier.weight(1f)) {
                for (c in 0 until 8) {
                    val isDark = (r + c) % 2 == 1
                    val bgColor = if (isDark) colors.darkSquare else colors.lightSquare
                    val pieceChar = grid[r][c]
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pieceChar != '.') {
                            val symbol = getChessPieceSymbol(pieceChar)
                            val pieceColor = if (pieceChar.isUpperCase()) Color.White else Color.Black
                            Text(
                                text = symbol,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = pieceColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseFenToGrid(fen: String): Array<CharArray> {
    val grid = Array(8) { CharArray(8) { '.' } }
    val parts = fen.split(" ")
    if (parts.isEmpty()) return grid
    val rows = parts[0].split("/")
    for (r in 0 until 8.coerceAtMost(rows.size)) {
        val rowStr = rows[r]
        var col = 0
        for (char in rowStr) {
            if (col >= 8) break
            if (char.isDigit()) {
                val emptySquares = char.toString().toInt()
                col += emptySquares
            } else {
                grid[r][col] = char
                col++
            }
        }
    }
    return grid
}

fun getChessPieceSymbol(char: Char): String {
    return when (char) {
        'R' -> "♜"
        'N' -> "♞"
        'B' -> "♝"
        'Q' -> "♛"
        'K' -> "♚"
        'P' -> "♟"
        'r' -> "♖"
        'n' -> "♘"
        'b' -> "♗"
        'q' -> "♕"
        'k' -> "♔"
        'p' -> "♙"
        else -> ""
    }
}
