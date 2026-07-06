package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.EngineConstants
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.moves.Move
import com.example.chess.engine.utils.OpeningExplorer
import com.example.chess.engine.utils.PgnParser
import com.example.chess.ui.ChessViewModel
import com.example.chess.ui.Chessboard
import com.example.chess.ui.PieceGraphic
import com.example.ui.theme.KillFishTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: ChessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KillFishTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: ChessViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (viewModel.activeScreen != "onboarding") {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = viewModel.activeScreen == "home",
                        onClick = { viewModel.activeScreen = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = viewModel.activeScreen == "play",
                        onClick = { viewModel.activeScreen = "play" },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Play") },
                        label = { Text("Play") },
                        modifier = Modifier.testTag("nav_play")
                    )
                    NavigationBarItem(
                        selected = viewModel.activeScreen == "stats",
                        onClick = { viewModel.activeScreen = "stats" },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Diagnostics") },
                        label = { Text("Diagnostics") },
                        modifier = Modifier.testTag("nav_stats")
                    )
                    NavigationBarItem(
                        selected = viewModel.activeScreen == "settings",
                        onClick = { viewModel.activeScreen = "settings" },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (viewModel.activeScreen) {
                    "home" -> com.example.chess.ui.HomeDashboardScreen(viewModel)
                    "play" -> PlayScreen(viewModel)
                    "academy" -> com.example.chess.ui.LearningAcademyScreen(viewModel)
                    "puzzles" -> com.example.chess.ui.PuzzleTrainingScreen(viewModel)
                    "chatbot" -> com.example.chess.ui.AiCoachChatbotScreen(viewModel)
                    "laboratory" -> com.example.chess.ui.EngineLaboratoryScreen(viewModel)
                    "premium" -> com.example.chess.ui.PremiumScreen(viewModel)
                    "achievements" -> com.example.chess.ui.AchievementsScreen(viewModel)
                    "editor" -> BoardEditorScreen(viewModel)
                    "explorer" -> OpeningExplorerScreen(viewModel)
                    "saved_games" -> SavedGamesScreen(viewModel)
                    "stats" -> StatsDiagnosticsScreen(viewModel)
                    "settings" -> SettingsScreen(viewModel)
                    "library" -> com.example.chess.ui.ChessLibraryScreen(viewModel)
                    "offline_trainer" -> com.example.chess.ui.OfflineTrainerScreen(viewModel)
                    "onboarding" -> com.example.chess.ui.OnboardingScreen(viewModel)
                }
            }
            if (viewModel.activeScreen != "settings" && viewModel.activeScreen != "premium" && viewModel.activeScreen != "chatbot" && viewModel.activeScreen != "home" && viewModel.activeScreen != "onboarding") {
                AdBanner(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PlayScreen(viewModel: ChessViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header Bar (Sleek design matching HTML top bar)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Kill Fish",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Quick info badge for active mode
                val modeText = if (viewModel.selectedBot != null) "Vs ${viewModel.selectedBot!!.name}" else if (viewModel.isEngineVersusMode) "Vs Engine" else "Casual Play"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = modeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Live Chess Game Panel: Opponent Profile, Board + Eval, User Profile
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. OPPONENT BOT PROFILE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = viewModel.selectedBot?.avatarEmoji ?: "🤖",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    text = viewModel.selectedBot?.name ?: "Default Engine",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (viewModel.selectedBot != null) "${viewModel.selectedBot!!.rating} Elo • ${viewModel.selectedBot!!.personality}" else "2500 Elo • Classic Stockfish",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Active thinking indicator or move turn label
                        if (viewModel.isSearching) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val pulseScale by infiniteTransition.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.4f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                        .background(Color(0xFF00E676), CircleShape)
                                )
                                Text(
                                    text = "Thinking...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFF00E676)
                                )
                            }
                        } else {
                            val isEngineTurn = if (viewModel.playerColor == WHITE) viewModel.sideToMove != WHITE else viewModel.sideToMove == WHITE
                            if (isEngineTurn && !viewModel.isGameOver) {
                                Text(
                                    text = "TO MOVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // 2. BOARD AND LIVE EVALUATION PANEL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Animated Evaluation Bar
                    val rawScore = viewModel.searchScore
                    val cappedScore = rawScore.coerceIn(-1000, 1000)
                    val targetPct = 0.5f + (cappedScore.toFloat() / 2000f)
                    val normalizedPct by animateFloatAsState(
                        targetValue = targetPct,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "eval_bar"
                    )

                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(320.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight((1f - normalizedPct).coerceIn(0.01f, 0.99f))
                                    .background(Color(0xFF1C1B1F))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(normalizedPct.coerceIn(0.01f, 0.99f))
                                    .background(Color(0xFFE6E1E5))
                            )
                        }
                        
                        Text(
                            text = "DEPTH ${viewModel.searchDepthCompleted}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 7.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (normalizedPct > 0.5f) Color(0xFF1C1B1F) else Color(0xFFE6E1E5),
                            modifier = Modifier.rotate(-90f),
                            maxLines = 1
                        )
                    }

                    // Interactive Chessboard View
                    Chessboard(
                        board = viewModel.boardRepresentation,
                        selectedSquare = viewModel.selectedSquare,
                        legalTargets = viewModel.legalTargets,
                        lastMove = viewModel.lastMove,
                        kingInCheckSquare = viewModel.kingInCheckSquare,
                        themeName = viewModel.boardThemeName,
                        playerColor = viewModel.playerColor,
                        highlightLegals = viewModel.highlightLegalMoves,
                        onSquareClick = { viewModel.selectSquare(it) },
                        modifier = Modifier.weight(1f),
                        heatmap = if (viewModel.isHeatmapOverlayEnabled) viewModel.getInfluenceHeatmap() else null,
                        animationTrigger = viewModel.animationTrigger,
                        animationMove = viewModel.animationMove,
                        editStyle = viewModel.editStyle,
                        enableCameraZoom = viewModel.enableCameraZoom,
                        enableMotionBlur = viewModel.enableMotionBlur,
                        enableScreenShake = viewModel.enableScreenShake,
                        enableParticleBursts = viewModel.enableParticleBursts,
                        enableLightingGlow = viewModel.enableLightingGlow,
                        editStyleIntensity = viewModel.editStyleIntensity
                    )
                }

                // 3. USER PLAYER PROFILE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👤",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    text = "Candidate Master Harshit",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "1850 Elo • Active Member",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (viewModel.isGameOver) {
                            Text(
                                text = "GAME OVER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            val isMyTurn = if (viewModel.playerColor == WHITE) viewModel.sideToMove == WHITE else viewModel.sideToMove != WHITE
                            if (isMyTurn) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Text(
                                        text = "YOUR TURN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tactical Actions / Quick Action Row (Compact controller right below board)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Move Button
                    IconButton(
                        onClick = { viewModel.undoMove() },
                        enabled = viewModel.playedMoves.isNotEmpty() && !viewModel.isSearching,
                        modifier = Modifier.testTag("btn_undo")
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo Move")
                    }

                    // Flip Board Button
                    IconButton(
                        onClick = { 
                            viewModel.playerColor = if (viewModel.playerColor == WHITE) BLACK else WHITE 
                        },
                        enabled = !viewModel.isSearching,
                        modifier = Modifier.testTag("btn_flip")
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Flip Board")
                    }

                    // Engine Think Button
                    Button(
                        onClick = { viewModel.triggerEngineSearch() },
                        enabled = !viewModel.isSearching && !viewModel.isGameOver,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_think")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = "Think", modifier = Modifier.size(16.dp))
                            Text("Engine Think", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetGame() },
                        enabled = !viewModel.isSearching,
                        modifier = Modifier.testTag("btn_reset")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "New Game / Reset")
                    }
                }
            }
        }

        // NEURAL ENGINE INSIGHTS DASHBOARD TABS (Saves hundreds of vertical pixels!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
                    ) {
                        val tabs = listOf(
                            "Bots" to Icons.Default.Face,
                            "Coach" to Icons.Default.Psychology,
                            "Metrics" to Icons.Default.Troubleshoot,
                            "Moves" to Icons.Default.MenuBook
                        )
                        tabs.forEachIndexed { index, (label, icon) ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                modifier = Modifier.height(44.dp),
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }

                    Box(modifier = Modifier.padding(14.dp)) {
                        when (selectedTabIndex) {
                            0 -> {
                                // Tab 0: Choose Chess Bot Opponent
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CHOOSE BOT OPPONENT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.2.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        if (viewModel.selectedBot != null) {
                                            TextButton(
                                                onClick = { viewModel.selectedBot = null },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = "Reset to Default",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Playing default engine",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        items(com.example.chess.ui.defaultChessBots) { bot ->
                                            val isSelected = viewModel.selectedBot?.id == bot.id
                                            val isPremiumLocked = bot.isPremium && !viewModel.premiumUpgraded

                                            Card(
                                                modifier = Modifier
                                                    .width(160.dp)
                                                    .clickable {
                                                        if (isPremiumLocked) {
                                                            viewModel.activeScreen = "premium"
                                                        } else {
                                                            viewModel.selectedBot = bot
                                                            viewModel.isEngineVersusMode = true
                                                        }
                                                    },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) {
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    }
                                                ),
                                                border = BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .background(
                                                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
                                                                    shape = CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = bot.avatarEmoji,
                                                                style = MaterialTheme.typography.titleSmall
                                                            )
                                                        }

                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            if (isPremiumLocked) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Lock,
                                                                    contentDescription = "Premium Locked",
                                                                    tint = MaterialTheme.colorScheme.secondary,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            } else if (isSelected) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = "Selected",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Text(
                                                        text = bot.name,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Rating",
                                                            tint = Color(0xFFFFB300),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "${bot.rating} Elo",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = bot.personality,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = bot.description,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // Tab 1: AI Coach & Match Openings
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (viewModel.matchedOpeningName != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Explore, 
                                                        contentDescription = "Opening", 
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "${viewModel.matchedOpeningName} (${viewModel.matchedOpeningEco})",
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = viewModel.matchedOpeningDesc ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (viewModel.geminiReasoning != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Psychology,
                                                    contentDescription = "Gemini AI",
                                                    tint = MaterialTheme.colorScheme.tertiary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "GEMINI COACH INSIGHTS",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Black,
                                                            letterSpacing = 1.2.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = viewModel.geminiReasoning ?: "",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                        ),
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Gemini Coach",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "AI coach analysis will appear here after moves are made.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    if (viewModel.geminiErrorMessage != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Warning",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "API KEY CONFIGURATION NEEDED",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Black,
                                                            letterSpacing = 1.2.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Please enter your GEMINI_API_KEY in the Secrets Panel. Fell back to local chess engine to keep game playable.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Tab 2: Advanced Diagnostics & Telemetry
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    val evalVal = viewModel.searchScore.toDouble() / 100.0
                                    val evalText = if (evalVal >= 0) "+${String.format(java.util.Locale.US, "%.2f", evalVal)}" else String.format(java.util.Locale.US, "%.2f", evalVal)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "ENGINE STATUS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.2.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = viewModel.gameStatus,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "EVALUATION",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.2.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = evalText,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Best Move Suggestion",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val bestMoveText = viewModel.searchBestMove?.toUci() ?: "Calculating..."
                                        Text(
                                            text = bestMoveText,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "SPEED",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${viewModel.searchNps / 1000}k NPS",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp))
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "SEARCH NODES",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${viewModel.searchNodes}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                                                )
                                            }
                                        }
                                    }

                                    if (viewModel.searchPv.isNotEmpty()) {
                                        Text(
                                            text = "PV Line: ${viewModel.searchPv.joinToString(" ") { it.toUci() }}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    // Board Overlays Config Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = viewModel.isHeatmapOverlayEnabled,
                                            onClick = { viewModel.isHeatmapOverlayEnabled = !viewModel.isHeatmapOverlayEnabled },
                                            label = { 
                                                Text(
                                                    "Influence Heatmap", 
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                                ) 
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.GridOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        FilterChip(
                                            selected = viewModel.highlightLegalMoves,
                                            onClick = { viewModel.highlightLegalMoves = !viewModel.highlightLegalMoves },
                                            label = { 
                                                Text(
                                                    "Legal Targets", 
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                                ) 
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.RadioButtonChecked,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    // Advanced diagnostic telemetry parameter grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            TelemetryBlock(
                                                title = "HASH HITS (TT)",
                                                value = "${viewModel.searchHashHits}",
                                                subtitle = "Transposition table saves",
                                                icon = Icons.Default.Layers
                                            )
                                            val fmcPct = if (viewModel.searchBetaCutoffs > 0) (viewModel.searchFirstMoveCutoffs.toFloat() / viewModel.searchBetaCutoffs * 100).toInt() else 0
                                            TelemetryBlock(
                                                title = "ORDERING PRECISION",
                                                value = "$fmcPct%",
                                                subtitle = "FMC Beta-cutoff ratio",
                                                icon = Icons.Default.Speed
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            TelemetryBlock(
                                                title = "NMP CUTOFFS",
                                                value = "${viewModel.searchNmpCutoffs}",
                                                subtitle = "Null move pruning exits",
                                                icon = Icons.Default.ContentCut
                                            )
                                            TelemetryBlock(
                                                title = "LMR REDUCTIONS",
                                                value = "${viewModel.searchLmrReductions}",
                                                subtitle = "Quiet move depth reductions",
                                                icon = Icons.Default.CompareArrows
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "MOVE ORDERING HEURISTICS STRENGTH",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    val fmcPct = if (viewModel.searchBetaCutoffs > 0) (viewModel.searchFirstMoveCutoffs.toFloat() / viewModel.searchBetaCutoffs * 100).toInt() else 0
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(if (viewModel.searchBetaCutoffs > 0) (fmcPct / 100f).coerceIn(0f, 1f) else 0f)
                                                .fillMaxHeight()
                                                .background(
                                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primary,
                                                            MaterialTheme.colorScheme.secondary
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total beta cutoffs: ${viewModel.searchBetaCutoffs}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "FMC score: ${viewModel.searchFirstMoveCutoffs}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            3 -> {
                                // Tab 3: Move List & PGN Sharing
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (viewModel.playedMoves.isNotEmpty()) {
                                        Text(
                                            text = "MOVE HISTORY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.2.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        val movePairs = viewModel.playedMoves.chunked(2)
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            itemsIndexed(movePairs) { index, pair ->
                                                val whiteMove = pair.getOrNull(0)?.toUci() ?: ""
                                                val blackMove = pair.getOrNull(1)?.toUci() ?: ""
                                                val moveNum = index + 1
                                                val isLastPair = index == movePairs.lastIndex

                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (isLastPair) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                                    border = if (isLastPair) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "$moveNum.",
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                            color = if (isLastPair) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Text(
                                                            text = whiteMove,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium),
                                                            color = if (isLastPair) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        if (blackMove.isNotEmpty()) {
                                                            Text(
                                                                text = blackMove,
                                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium),
                                                                color = if (isLastPair) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No moves played yet. Start moving pieces to generate list.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    // PGN Analysis & Share Center
                                    var pgnImportText by remember { mutableStateOf("") }
                                    val context = LocalContext.current
                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                                    Text(
                                        text = "PGN ANALYSIS & SHARE CENTER",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    val currentPgn = viewModel.exportToPgn()

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 80.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                            item {
                                                Text(
                                                    text = if (viewModel.playedMoves.isEmpty()) "No moves played yet. Start moving pieces to generate PGN." else currentPgn,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentPgn))
                                            android.widget.Toast.makeText(context, "Game PGN copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                            com.example.chess.utils.ChessHaptics.playMoveHaptic(context)
                                        },
                                        enabled = viewModel.playedMoves.isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                            Text("Copy Game PGN", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Import Section
                                    Text(
                                        text = "IMPORT PGN RECORD",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    OutlinedTextField(
                                        value = pgnImportText,
                                        onValueChange = { pgnImportText = it },
                                        label = { Text("Paste Chess PGN String") },
                                        placeholder = { Text("e.g. 1. e4 e5 2. Nf3 Nc6...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    OutlinedButton(
                                        onClick = {
                                            if (pgnImportText.trim().isEmpty()) {
                                                android.widget.Toast.makeText(context, "Please enter or paste a PGN string!", android.widget.Toast.LENGTH_SHORT).show()
                                                return@OutlinedButton
                                            }
                                            val success = viewModel.importPgn(pgnImportText)
                                            if (success) {
                                                android.widget.Toast.makeText(context, "PGN Game loaded successfully! Replay starts on active board.", android.widget.Toast.LENGTH_LONG).show()
                                                com.example.chess.utils.ChessHaptics.playCheckHaptic(context)
                                                pgnImportText = ""
                                            } else {
                                                android.widget.Toast.makeText(context, "Error: Invalid PGN structure or contains illegal moves!", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Publish, contentDescription = "Import", modifier = Modifier.size(16.dp))
                                            Text("Import & Analyze Game", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Footer & Developer Credits
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Kill Fish Chess Engine • Made by Harshit Premi (hpg2.0)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Depth search powered by Alpha-Beta Negamax",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun BoardEditorScreen(viewModel: ChessViewModel) {
    var fenInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Board Editor",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Setup custom positions or load standard FEN strings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Active Editor Board
        item {
            Chessboard(
                board = viewModel.boardRepresentation,
                selectedSquare = null,
                legalTargets = emptyList(),
                lastMove = null,
                kingInCheckSquare = null,
                themeName = viewModel.boardThemeName,
                playerColor = viewModel.playerColor,
                highlightLegals = false,
                onSquareClick = { viewModel.editorSetPiece(it) }
            )
        }

        // Piece Palette
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Piece Palette",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // White Pieces Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (pieceType in EngineConstants.W_PAWN..EngineConstants.W_KING) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (viewModel.editorSelectedPiece == pieceType) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = if (viewModel.editorSelectedPiece == pieceType) 2.dp else 1.dp,
                                        color = if (viewModel.editorSelectedPiece == pieceType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    .clickable { viewModel.editorSelectedPiece = pieceType },
                                contentAlignment = Alignment.Center
                            ) {
                                PieceGraphic(piece = pieceType, size = 40.dp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Black Pieces Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (pieceType in EngineConstants.B_PAWN..EngineConstants.B_KING) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (viewModel.editorSelectedPiece == pieceType) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = if (viewModel.editorSelectedPiece == pieceType) 2.dp else 1.dp,
                                        color = if (viewModel.editorSelectedPiece == pieceType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    .clickable { viewModel.editorSelectedPiece = pieceType },
                                contentAlignment = Alignment.Center
                            ) {
                                PieceGraphic(piece = pieceType, size = 40.dp)
                            }
                        }
                    }
                }
            }
        }

        // Active setup actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = { viewModel.editorClearBoard() }) {
                    Text("Clear Board")
                }
                OutlinedButton(onClick = { viewModel.editorResetBoard() }) {
                    Text("Standard Board")
                }
                FilledTonalButton(onClick = { viewModel.editorChangeTurn() }) {
                    Text(if (viewModel.sideToMove == WHITE) "Turn: White" else "Turn: Black")
                }
            }
        }

        // FEN loading panel
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "FEN Import/Export",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    OutlinedTextField(
                        value = fenInput,
                        onValueChange = { fenInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Paste FEN String") },
                        placeholder = { Text("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledTonalButton(onClick = {
                            if (fenInput.trim().isNotEmpty()) {
                                try {
                                    viewModel.boardState.loadFen(fenInput.trim())
                                    viewModel.resetGame()
                                    fenInput = ""
                                } catch (e: Exception) {
                                    // simple fallback
                                }
                            }
                        }) {
                            Text("Load Position")
                        }

                        Button(onClick = {
                            fenInput = viewModel.boardState.toFen()
                        }) {
                            Text("Get Active FEN")
                        }
                    }
                }
            }
        }

        // Save and Apply button
        item {
            Button(
                onClick = { viewModel.activeScreen = "play" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Analyze Edited Position")
            }
        }
    }
}

@Composable
fun OpeningExplorerScreen(viewModel: ChessViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Opening Explorer",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Learn standard openings and load their lines directly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(OpeningExplorer.openingsList) { opening ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opening.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = opening.eco,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val formattedMoves = opening.uciMoves.chunked(2).mapIndexed { idx, pair ->
                        val w = pair.getOrNull(0) ?: ""
                        val b = pair.getOrNull(1) ?: ""
                        "${idx + 1}. $w $b"
                    }.joinToString(" ")

                    Text(
                        text = "UCI Line: $formattedMoves",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = opening.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val (state, moves) = PgnParser.reconstructFromUciMoves(
                                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                                opening.uciMoves.joinToString(" ")
                            )
                            viewModel.boardState.copyFrom(state)
                            viewModel.playedMoves.clear()
                            viewModel.playedMoves.addAll(moves)
                            viewModel.redoStack.clear()
                            viewModel.lastMove = moves.lastOrNull()
                            viewModel.selectedSquare = null
                            viewModel.legalTargets = emptyList()
                            viewModel.activeScreen = "play"
                            // If engine is versus black, let it think if side is black
                            if (viewModel.isEngineVersusMode && viewModel.sideToMove != viewModel.playerColor) {
                                viewModel.triggerEngineSearch()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Load")
                            Text("Load position onto Board")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SavedGamesScreen(viewModel: ChessViewModel) {
    val savedGames by viewModel.allGames.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Saved Games",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Load and analyze your historic matches",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (savedGames.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved games yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Finish games in Versus mode to record them here automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedGames) { game ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Badge(
                                containerColor = when (game.result) {
                                    "1-0", "0-1" -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(game.result, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(game.date))
                        Text(
                            text = "Played on: $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Moves: ${game.movesList}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.loadSavedGame(game) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Load")
                                    Text("Analyze Game")
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteSavedGame(game.id) },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatsDiagnosticsScreen(viewModel: ChessViewModel) {
    val stats by viewModel.statistics.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Performance Diagnostics",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Benchmark engine speed and debug move generators",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Match Statistics Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Match Record",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Wins", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${stats?.wins ?: 0}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Losses", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${stats?.losses ?: 0}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFFC62828)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Draws", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${stats?.draws ?: 0}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }

        // Engine Speed Benchmark
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Engine Speed Benchmark",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(Icons.Default.Speed, contentDescription = "Speed")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Measures nodes searched per second on three standardized standard chess positions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = viewModel.benchmarkResultText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.runBenchmark() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = "Run")
                            Text("Run Benchmark")
                        }
                    }
                }
            }
        }

        // Perft debugger
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Perft Move Gen Verification",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(Icons.Default.BugReport, contentDescription = "Debug")
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Recursively searches and counts move leaf nodes. Verifies move generator correctness and speed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Search Depth: ${viewModel.perftDepth}", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = viewModel.perftDepth.toFloat(),
                            onValueChange = { viewModel.perftDepth = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.perftResultText,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.runPerftTest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Run")
                            Text("Run Perft")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsScreen(viewModel: ChessViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Customize game engine strength, colors, and rules",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Engine Strength Settings
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Engine Customization",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust the intelligence and processing engine of your computer opponent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gemini GM / Stockfish 18 Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Stockfish 18 Level (Gemini AI)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "ELITE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Uses Gemini Superintelligence to calculate moves & explain strategy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = viewModel.useGeminiEngine,
                            onCheckedChange = { viewModel.useGeminiEngine = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (viewModel.useGeminiEngine) {
                        Text(
                            text = "Thinking Depth: Managed by Gemini AI (Elo 3500+)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thinking Depth: ${viewModel.engineDepth}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Slider(
                                value = viewModel.engineDepth.toFloat(),
                                onValueChange = { viewModel.engineDepth = it.toInt() },
                                valueRange = 1f..8f,
                                steps = 6,
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Match Preference Settings
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Match Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Vs Engine active toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Versus Engine", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Engine triggers responses on its move", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = viewModel.isEngineVersusMode,
                            onCheckedChange = { viewModel.isEngineVersusMode = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Color Selector
                    if (viewModel.isEngineVersusMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Play Match as", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = viewModel.playerColor == WHITE,
                                    onClick = { viewModel.playerColor = WHITE; viewModel.resetGame() },
                                    label = { Text("White") }
                                )
                                FilterChip(
                                    selected = viewModel.playerColor == BLACK,
                                    onClick = { viewModel.playerColor = BLACK; viewModel.resetGame() },
                                    label = { Text("Black") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Board customization theme
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Aesthetic Styling",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Theme selector
                    Text("Board Theme Preset", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Emerald Green", "Classic Wood Slate", "Royal Blue", "Midnight Slate").forEach { theme ->
                            FilterChip(
                                selected = viewModel.boardThemeName == theme,
                                onClick = { viewModel.boardThemeName = theme },
                                label = { Text(theme.split(" ")[1]) }, // short name
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cinematic Animation Theme", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    var expandedAnimationThemes by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedAnimationThemes = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(viewModel.selectedAnimationTheme)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        
                        DropdownMenu(
                            expanded = expandedAnimationThemes,
                            onDismissRequest = { expandedAnimationThemes = false },
                            modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
                        ) {
                            listOf(
                                "Minecraft Stone Age",
                                "Minecraft Medieval Kingdom",
                                "Minecraft Nether",
                                "Minecraft End Dimension",
                                "Minecraft Ancient City",
                                "Shadow Ninja",
                                "Samurai Legacy",
                                "Cyber Neon",
                                "Space Galaxy",
                                "Crystal Energy",
                                "Dragon Flame",
                                "Ice Kingdom",
                                "Lightning Storm",
                                "Golden Royal",
                                "Motion Chess",
                                "Minimal Professional",
                                "Glass Morphism",
                                "Holographic Future"
                            ).forEach { animTheme ->
                                DropdownMenuItem(
                                    text = { Text(animTheme) },
                                    onClick = {
                                        viewModel.selectedAnimationTheme = animTheme
                                        expandedAnimationThemes = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Highlight legal moves
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Legal Move Hints", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Render overlays showing legal moves", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = viewModel.highlightLegalMoves,
                            onCheckedChange = { viewModel.highlightLegalMoves = it }
                        )
                    }
                }
            }
        }

        // Cinematic Special Edits VFX Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Edits VFX",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Special Edits & VFX",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize cinematic move edits inspired by popular social media edits, Minecraft, and Shadow Ninja styles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit Style Selector (Three Options)
                    Text(
                        text = "Cinematic Edit Style",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val editStyles = listOf(
                            "Next-Gen Velocity" to "Velocity",
                            "Minecraft Crafting" to "Minecraft",
                            "Shadow Ninja Sumi" to "Ninja Sumi"
                        )
                        editStyles.forEach { (styleId, label) ->
                            val isSelected = viewModel.editStyle == styleId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.editStyle = styleId },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // VFX Toggle Controls
                    Text(
                        text = "Toggle Special Effects",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Camera Zoom Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Camera Zoom & Pan", style = MaterialTheme.typography.bodyMedium)
                            Text("Dynamic zoom framing on destination square", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.enableCameraZoom,
                            onCheckedChange = { viewModel.enableCameraZoom = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Motion Blur Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Motion Blur Speedlines", style = MaterialTheme.typography.bodyMedium)
                            Text("Rushing visual line overlay upon action impact", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.enableMotionBlur,
                            onCheckedChange = { viewModel.enableMotionBlur = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Screen Shake Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Screen Shake", style = MaterialTheme.typography.bodyMedium)
                            Text("Simulate board rumble and tactile displacement", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.enableScreenShake,
                            onCheckedChange = { viewModel.enableScreenShake = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Particle Bursts Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Particle Explosions", style = MaterialTheme.typography.bodyMedium)
                            Text("Disperse blocky pixels, shadows, or sparks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.enableParticleBursts,
                            onCheckedChange = { viewModel.enableParticleBursts = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lighting Glow Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lighting Glow Aura", style = MaterialTheme.typography.bodyMedium)
                            Text("Atmospheric colored radial glow propagation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.enableLightingGlow,
                            onCheckedChange = { viewModel.enableLightingGlow = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Intensity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Effect Intensity: ${(viewModel.editStyleIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Slider(
                        value = viewModel.editStyleIntensity,
                        onValueChange = { viewModel.editStyleIntensity = it },
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Sound & Acoustics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Sensory & Haptic Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Immerse yourself with high-fidelity digital synthesized board sounds and physical tactile responses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Synthesized Sounds", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Realistic retro wood taps, capture snaps, and check alerts", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = viewModel.soundEffectsEnabled,
                            onCheckedChange = { viewModel.soundEffectsEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tactile Haptics", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Sophisticated vibration patterns for moves, captures, and check alerts", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = viewModel.hapticFeedbackEnabled,
                            onCheckedChange = { viewModel.hapticFeedbackEnabled = it }
                        )
                    }
                }
            }
        }

        // Network Synchronization (Offline Mode) & Ad Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Network & Synchronization",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (viewModel.isOfflineMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (viewModel.isOfflineMode) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = if (viewModel.isOfflineMode) "OFFLINE" else "ONLINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toggle between local processing (Offline Mode) and cloud synchronization services (Online Mode).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Offline Mode Toggle Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Mode Active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (viewModel.isOfflineMode) 
                                    "Prioritizes local storage, local engine analysis, and local academies." 
                                else 
                                    "Enables Gemini cloud tutor coaching, automatic summaries, and backup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = viewModel.isOfflineMode,
                            onCheckedChange = { viewModel.isOfflineMode = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Ads Control Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Ad Banner",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Disable ads by subscribing to any tier in the Premium Shop.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = viewModel.adsEnabled && !viewModel.isPremiumActive,
                            onCheckedChange = { 
                                if (!viewModel.isPremiumActive) {
                                    viewModel.adsEnabled = it
                                }
                            },
                            enabled = !viewModel.isPremiumActive
                        )
                    }
                }
            }
        }

        // Developer Credits Card
        item {
            val uriHandler = LocalUriHandler.current
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "App Information & Credits",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Developer",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Made by Harshit Premi",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "YouTube Channel: hpg2.0",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Crafted with passion using Kotlin & Jetpack Compose",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Thank you for supporting Kill Fish! This engine is a lightweight hybrid array + bitboard implementation built from scratch to bring standard Stockfish-like depth search directly inside Jetpack Compose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { 
                            try {
                                uriHandler.openUri("https://www.youtube.com/@hpg2.0")
                            } catch (e: Exception) {
                                try {
                                    uriHandler.openUri("https://www.youtube.com/results?search_query=hpg2.0")
                                } catch (ex: Exception) {}
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF0000), // YouTube Red!
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "YouTube logo",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }



        // Account & Cloud Data Safety Card
        item {
            val context = LocalContext.current
            var inputEmail by remember { mutableStateOf("harshitpremi09@gmail.com") }
            var inputName by remember { mutableStateOf("Harshit Premi") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isUserSignedIn) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Account",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Account & Cloud Data Safety",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prevent data loss! Associate your account to secure all saved chess matches, Elo progress, and personalized analytical settings in our real-time cloud backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (viewModel.isUserSignedIn) {
                        // User Account Active Details
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = viewModel.userAccountName.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = viewModel.userAccountName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = viewModel.userAccountEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = "Sync",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = viewModel.backupStatus,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = "Last synchronized: ${viewModel.lastBackupTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.syncBackupNow(context)
                                    android.widget.Toast.makeText(context, "Force Cloud Backup sync completed! Local database matches uploaded.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                                    Text("Sync Backup Now", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.signOutUser()
                                    android.widget.Toast.makeText(context, "Signed out from account. Local mode active.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                                    Text("Sign Out", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    } else {
                        // Sign-In Form for backing up data
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                label = { Text("Enter Account Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it },
                                label = { Text("Account Backup Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (inputEmail.trim().isEmpty() || inputName.trim().isEmpty()) {
                                        android.widget.Toast.makeText(context, "Name and Email cannot be empty!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail.trim()).matches()) {
                                        android.widget.Toast.makeText(context, "Please enter a valid email address!", android.widget.Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.signInUser(inputEmail.trim(), inputName.trim(), context)
                                    android.widget.Toast.makeText(context, "Welcome, $inputName! Account synchronized.", android.widget.Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = "Sign In", modifier = Modifier.size(18.dp))
                                    Text("Authenticate & Enable Cloud Backup", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chess Level & Onboarding Tutorial Launcher Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Chess Level",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Chess Level & App Tutorial",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Review or modify your selected chess level, training goal, customized 'Watch Out' tips, and explore the tailored app tutorial guide at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Level: ${viewModel.userChessLevel.uppercase()}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Goal: ${viewModel.userChessReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.activeScreen = "onboarding"
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Launch Tutorial", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // About & tech stack specifications
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Technical Specification",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Core Representation: Array + Bitboard Hybrid\n" +
                                "• State Hashing: Incremental Zobrist\n" +
                                "• Search Engine: Alpha-Beta Negamax, Iterative Deepening, Aspiration Windows, Null Move Pruning, Late Move Reduction (LMR), Quiescence Search\n" +
                                "• Positional Evaluator: Midgame/Endgame Tapered PST\n" +
                                "• Storage Architecture: Room SQLite Database",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AdBanner(viewModel: ChessViewModel) {
    if (!viewModel.adsEnabled || viewModel.premiumUpgraded || viewModel.adClosedTemporary) {
        return
    }

    val ad = viewModel.simulatedAdsList[viewModel.activeAdIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("simulated_ad_banner"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Sponsor" indicator icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Announcement,
                    contentDescription = "Ad Sponsor",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "SPONSOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ad.desc,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Close button "X"
                IconButton(
                    onClick = { viewModel.adClosedTemporary = true },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Ad",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Call to action button
                Button(
                    onClick = {
                        if (ad.link == "premium_upgrade") {
                            viewModel.premiumUpgraded = true
                            viewModel.adsEnabled = false
                        } else {
                            viewModel.rotateSimulatedAd()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = ad.cta,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryBlock(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black, 
                        fontSize = 8.sp, 
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold, 
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
