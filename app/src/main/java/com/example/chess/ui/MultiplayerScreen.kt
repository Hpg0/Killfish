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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.moves.Move
import com.example.chess.utils.ChessHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

enum class MultiplayerMode {
    PASS_AND_PLAY, MATCHMAKING
}

enum class MatchmakingState {
    IDLE, SEARCHING, CONNECTING, ACTIVE_GAME, GAME_OVER
}

data class MultiplayerChatMessage(
    val sender: String,
    val text: String,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMode by remember { mutableStateOf<MultiplayerMode?>(null) }
    var matchmakingState by remember { mutableStateOf(MatchmakingState.IDLE) }
    
    // Pass & Play states
    var p1Name by remember { mutableStateOf("Player 1") }
    var p2Name by remember { mutableStateOf("Player 2") }
    var passPlayTimeLimit by remember { mutableStateOf("10 Min Rapid") }
    var autoRotateBoard by remember { mutableStateOf(true) }
    
    // Matchmaking game states
    var opponentName by remember { mutableStateOf("") }
    var opponentRating by remember { mutableStateOf(1200) }
    var opponentAvatar by remember { mutableStateOf("👤") }
    var matchmakingTimeControl by remember { mutableStateOf("5 Min Blitz") }
    var userColorInMatchmaking by remember { mutableStateOf(WHITE) }

    // Game timers
    var whiteTimerSeconds by remember { mutableStateOf(600) }
    var blackTimerSeconds by remember { mutableStateOf(600) }
    var timerRunning by remember { mutableStateOf(false) }

    // Floating Emoji states
    val floatingEmojis = remember { mutableStateListOf<Pair<String, Float>>() } // (Emoji, offsetFraction)

    // Chat and notification states
    val chatMessages = remember { mutableStateListOf<MultiplayerChatMessage>() }
    var chatExpanded by remember { mutableStateOf(false) }

    // Ensure we release engine when on this screen
    LaunchedEffect(selectedMode) {
        if (selectedMode != null) {
            viewModel.isEngineVersusMode = false
            viewModel.resetGame()
            if (selectedMode == MultiplayerMode.PASS_AND_PLAY) {
                whiteTimerSeconds = when (passPlayTimeLimit) {
                    "3 Min Blitz" -> 180
                    "5 Min Blitz" -> 300
                    "10 Min Rapid" -> 600
                    "30 Min Classical" -> 1800
                    else -> 600
                }
                blackTimerSeconds = whiteTimerSeconds
                timerRunning = true
            }
        } else {
            viewModel.isEngineVersusMode = true
            timerRunning = false
        }
    }

    // Active Timer countdown loop
    LaunchedEffect(timerRunning, viewModel.sideToMove, whiteTimerSeconds, blackTimerSeconds) {
        if (timerRunning && (selectedMode == MultiplayerMode.PASS_AND_PLAY || matchmakingState == MatchmakingState.ACTIVE_GAME)) {
            while (true) {
                delay(1000)
                if (viewModel.sideToMove == WHITE) {
                    if (whiteTimerSeconds > 0) {
                        whiteTimerSeconds--
                    } else {
                        // Black wins on time
                        timerRunning = false
                        viewModel.isGameOver = true
                        viewModel.gameStatus = "Black wins on time!"
                        if (selectedMode == MultiplayerMode.MATCHMAKING) {
                            matchmakingState = MatchmakingState.GAME_OVER
                            if (userColorInMatchmaking == WHITE) {
                                viewModel.updateCurrentUserEloAndStats(userWon = false, draw = false, opponentRating = opponentRating)
                            } else {
                                viewModel.updateCurrentUserEloAndStats(userWon = true, draw = false, opponentRating = opponentRating)
                            }
                        }
                        break
                    }
                } else {
                    if (blackTimerSeconds > 0) {
                        blackTimerSeconds--
                    } else {
                        // White wins on time
                        timerRunning = false
                        viewModel.isGameOver = true
                        viewModel.gameStatus = "White wins on time!"
                        if (selectedMode == MultiplayerMode.MATCHMAKING) {
                            matchmakingState = MatchmakingState.GAME_OVER
                            if (userColorInMatchmaking == BLACK) {
                                viewModel.updateCurrentUserEloAndStats(userWon = false, draw = false, opponentRating = opponentRating)
                            } else {
                                viewModel.updateCurrentUserEloAndStats(userWon = true, draw = false, opponentRating = opponentRating)
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    // Handle Matchmaking simulated search & opponent smart play loop
    val handleOpponentMove: suspend () -> Unit = {
        delay(1500 + Random().nextInt(2500).toLong()) // human-like reflection delay
        if (!viewModel.isGameOver) {
            // Let the engine think of a response and perform it
            viewModel.isEngineVersusMode = true
            viewModel.triggerEngineSearch()
            viewModel.isEngineVersusMode = false
            
            ChessHaptics.playMoveHaptic(context)
            
            // Randomly simulate chat preset based on state
            if (Random().nextFloat() < 0.25f) {
                val systemMsgs = listOf(
                    "Nice move!", "Ah, good play.", "Oops, I missed that.", "This is going to be close!", "Good game!"
                )
                val text = systemMsgs[Random().nextInt(systemMsgs.size)]
                chatMessages.add(MultiplayerChatMessage(sender = opponentName, text = text))
            }
            // Randomly simulate reaction emojis
            if (Random().nextFloat() < 0.20f) {
                val reactionEmojis = listOf("👍", "😮", "😡", "😭", "😀")
                val chosen = reactionEmojis[Random().nextInt(reactionEmojis.size)]
                floatingEmojis.add(Pair(chosen, Random().nextFloat()))
            }
        }
    }

    LaunchedEffect(viewModel.sideToMove, matchmakingState) {
        if (selectedMode == MultiplayerMode.MATCHMAKING && matchmakingState == MatchmakingState.ACTIVE_GAME) {
            if (viewModel.sideToMove != userColorInMatchmaking) {
                handleOpponentMove()
            }
        }
    }

    // Monitor for actual local match finishes
    LaunchedEffect(viewModel.isGameOver) {
        if (viewModel.isGameOver && selectedMode != null) {
            timerRunning = false
            if (selectedMode == MultiplayerMode.MATCHMAKING && matchmakingState == MatchmakingState.ACTIVE_GAME) {
                matchmakingState = MatchmakingState.GAME_OVER
                val gameResult = viewModel.gameStatus
                val userWon = (gameResult.contains("White wins") && userColorInMatchmaking == WHITE) ||
                              (gameResult.contains("Black wins") && userColorInMatchmaking == BLACK)
                val draw = gameResult.contains("Draw") || gameResult.contains("Stalemate") || gameResult.contains("repetition")
                
                viewModel.updateCurrentUserEloAndStats(userWon = userWon, draw = draw, opponentRating = opponentRating)
                chatMessages.add(MultiplayerChatMessage(sender = "System", text = "Game concluded: $gameResult", isSystem = true))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedMode) {
                            null -> "MULTIPLAYER ARENA"
                            MultiplayerMode.PASS_AND_PLAY -> "PASS & PLAY MODE"
                            MultiplayerMode.MATCHMAKING -> "ONLINE MATCHMAKING"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        ChessHaptics.playMoveHaptic(context)
                        if (selectedMode != null) {
                            selectedMode = null
                            matchmakingState = MatchmakingState.IDLE
                        } else {
                            viewModel.activeScreen = "home"
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.primary,
                    navigationIconContentColor = colors.primary
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedMode) {
                null -> {
                    // Multiplayer Selection Menu
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            // Arena Header Graphic
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(colors.surface, colors.background)
                                        )
                                    )
                                    .border(BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Groups,
                                        contentDescription = "Multiplayer",
                                        tint = colors.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "LIVE PVP NETWORK",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                        color = colors.primary
                                    )
                                    Text(
                                        text = "Challenge local players or fight in global lobbies",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // PASS & PLAY CARD
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ChessHaptics.playMoveHaptic(context)
                                        selectedMode = MultiplayerMode.PASS_AND_PLAY
                                    },
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PhonelinkRing, contentDescription = "Local", tint = colors.primary, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Pass & Play", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Two players, same screen. Perfect for coffee shops and travel.", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.7f))
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = colors.primary)
                                }
                            }
                        }

                        // ONLINE ARENA MATCHMAKING CARD
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ChessHaptics.playMoveHaptic(context)
                                        selectedMode = MultiplayerMode.MATCHMAKING
                                        matchmakingState = MatchmakingState.IDLE
                                    },
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(colors.glowColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Wifi, contentDescription = "Online", tint = colors.glowColor, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Online Arena (Matchmaking)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Instant matching with live opponents based on your rating.", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.7f))
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = colors.primary)
                                }
                            }
                        }

                        // Connected network ping telemetry
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Text(
                                        text = "KILLFISH GATEWAY: US-EAST (ACTIVE) | PING: 24MS | PLAYERS: 12,492",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                MultiplayerMode.PASS_AND_PLAY -> {
                    // Local Device Multiplayer Gameplay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // OPPONENT PANEL (At the top, rotated if autoRotate is true and it's Black's turn)
                        val topRotation = if (autoRotateBoard && viewModel.sideToMove == BLACK) 180f else 0f
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rotate(topRotation),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, if (viewModel.sideToMove == BLACK) colors.primary else colors.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        Text("♟️", fontSize = 24.sp)
                                    }
                                    Column {
                                        Text(p2Name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Black Player", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                // Timer count
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (viewModel.sideToMove == BLACK) colors.primary.copy(alpha = 0.15f) else colors.surface
                                    )
                                ) {
                                    Text(
                                        text = formatTime(blackTimerSeconds),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = if (viewModel.sideToMove == BLACK) colors.primary else colors.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // CHESSBOARD VIEW
                        val boardRotation = if (autoRotateBoard && viewModel.sideToMove == BLACK) 180f else 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .rotate(boardRotation)
                        ) {
                            Chessboard(
                                board = viewModel.boardRepresentation,
                                selectedSquare = viewModel.selectedSquare,
                                legalTargets = viewModel.legalTargets,
                                lastMove = viewModel.lastMove,
                                kingInCheckSquare = viewModel.kingInCheckSquare,
                                themeName = viewModel.boardThemeName,
                                playerColor = WHITE, // keep view steady or flip manually
                                highlightLegals = viewModel.highlightLegalMoves,
                                onSquareClick = { 
                                    ChessHaptics.playMoveHaptic(context)
                                    viewModel.selectSquare(it) 
                                },
                                modifier = Modifier.fillMaxSize(),
                                heatmap = null,
                                animationTrigger = viewModel.animationTrigger,
                                animationMove = viewModel.animationMove,
                                editStyle = viewModel.editStyle,
                                enableCameraZoom = viewModel.enableCameraZoom,
                                enableMotionBlur = viewModel.enableMotionBlur
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ACTIVE USER PANEL
                        val bottomRotation = if (autoRotateBoard && viewModel.sideToMove == BLACK) 180f else 0f
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rotate(bottomRotation),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, if (viewModel.sideToMove == WHITE) colors.primary else colors.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                        Text("♙", fontSize = 24.sp, color = colors.primary)
                                    }
                                    Column {
                                        Text(p1Name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("White Player", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                // Timer count
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (viewModel.sideToMove == WHITE) colors.primary.copy(alpha = 0.15f) else colors.surface
                                    )
                                ) {
                                    Text(
                                        text = formatTime(whiteTimerSeconds),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = if (viewModel.sideToMove == WHITE) colors.primary else colors.onSurface
                                    )
                                }
                            }
                        }

                        // LOCAL SETTINGS BAR
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Switch(
                                    checked = autoRotateBoard,
                                    onCheckedChange = { autoRotateBoard = it },
                                    thumbContent = {
                                        Icon(Icons.Default.Autorenew, contentDescription = "Rotate", modifier = Modifier.size(16.dp))
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rotate Board on Turn", style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    ChessHaptics.playMoveHaptic(context)
                                    viewModel.resetGame()
                                    whiteTimerSeconds = 600
                                    blackTimerSeconds = 600
                                    timerRunning = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Restart")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restart")
                            }
                        }
                    }
                }

                MultiplayerMode.MATCHMAKING -> {
                    // Online Matchmaking UI & Gameplay
                    when (matchmakingState) {
                        MatchmakingState.IDLE -> {
                            // Lobby Setup
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item {
                                    Text(
                                        "MATCHMAKING ROOMS",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = colors.primary
                                    )
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                                        border = BorderStroke(1.dp, colors.border)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("Set Your Game Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                                            // Time control chips
                                            Text("Time Controls", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                listOf("3 Min Blitz", "5 Min Blitz", "10 Min Rapid", "30 Min Classical").forEach { tc ->
                                                    FilterChip(
                                                        selected = matchmakingTimeControl == tc,
                                                        onClick = {
                                                            ChessHaptics.playMoveHaptic(context)
                                                            matchmakingTimeControl = tc
                                                        },
                                                        label = { Text(tc) }
                                                    )
                                                }
                                            }

                                            // Player side selection
                                            Text("Your Preference Side", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                FilterChip(
                                                    selected = userColorInMatchmaking == WHITE,
                                                    onClick = { userColorInMatchmaking = WHITE },
                                                    label = { Text("Play White") }
                                                )
                                                FilterChip(
                                                    selected = userColorInMatchmaking == BLACK,
                                                    onClick = { userColorInMatchmaking = BLACK },
                                                    label = { Text("Play Black") }
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Button(
                                        onClick = {
                                            ChessHaptics.playMoveHaptic(context)
                                            matchmakingState = MatchmakingState.SEARCHING
                                            coroutineScope.launch {
                                                // Simulated search delay
                                                delay(4000)
                                                matchmakingState = MatchmakingState.CONNECTING
                                                delay(2000)
                                                
                                                // Seed Opponent
                                                val namesList = listOf("AlphaPawn", "Grandmaster_Ray", "ChessViper", "TacticalTitan", "ZeroKFish")
                                                opponentName = namesList[Random().nextInt(namesList.size)]
                                                opponentRating = (viewModel.currentUser.value?.eloRating ?: 1200) + Random().nextInt(100) - 50
                                                val avatars = listOf("🤖", "🦊", "🦁", "🦉", "🐺")
                                                opponentAvatar = avatars[Random().nextInt(avatars.size)]
                                                
                                                whiteTimerSeconds = when (matchmakingTimeControl) {
                                                    "3 Min Blitz" -> 180
                                                    "5 Min Blitz" -> 300
                                                    "10 Min Rapid" -> 600
                                                    "30 Min Classical" -> 1800
                                                    else -> 600
                                                }
                                                blackTimerSeconds = whiteTimerSeconds
                                                
                                                chatMessages.clear()
                                                chatMessages.add(MultiplayerChatMessage(sender = "System", text = "Connected to match with $opponentName!", isSystem = true))
                                                chatMessages.add(MultiplayerChatMessage(sender = "System", text = "Good luck and have fun!", isSystem = true))
                                                
                                                viewModel.resetGame()
                                                matchmakingState = MatchmakingState.ACTIVE_GAME
                                                timerRunning = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Search")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("FIND MATCH NOW", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    }
                                }
                            }
                        }

                        MatchmakingState.SEARCHING -> {
                            // Pulsing, gorgeous searching loader
                            val infiniteTransition = rememberInfiniteTransition(label = "SearchPulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary.copy(alpha = 0.15f * pulseScale))
                                            .border(BorderStroke(2.dp, colors.primary), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Wifi,
                                            contentDescription = "Searching",
                                            tint = colors.primary,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(
                                        "ANALYZING PLAYER POOL...",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                        color = colors.primary
                                    )
                                    Text(
                                        "Searching active nodes near your rating (${viewModel.currentUser.value?.eloRating ?: 1200} ELO)...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        MatchmakingState.CONNECTING -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = colors.glowColor)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        "SYNCHRONIZING SECURE ROOM...",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                                        color = colors.glowColor
                                    )
                                    Text(
                                        "Connecting to game servers US-East-1d...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        MatchmakingState.ACTIVE_GAME -> {
                            // Active Online Gameplay
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // OPPONENT BOX (At top)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, if (viewModel.sideToMove != userColorInMatchmaking) colors.primary else colors.border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(opponentAvatar, fontSize = 28.sp)
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(opponentName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF4CAF50))
                                                    )
                                                }
                                                Text("$opponentRating ELO • Connected", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                            }
                                        }

                                        // Timer display
                                        val oppTime = if (userColorInMatchmaking == WHITE) blackTimerSeconds else whiteTimerSeconds
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (viewModel.sideToMove != userColorInMatchmaking) colors.primary.copy(alpha = 0.15f) else colors.surface
                                            )
                                        ) {
                                            Text(
                                                text = formatTime(oppTime),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                color = if (viewModel.sideToMove != userColorInMatchmaking) colors.primary else colors.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // CHESSBOARD
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                ) {
                                    Chessboard(
                                        board = viewModel.boardRepresentation,
                                        selectedSquare = viewModel.selectedSquare,
                                        legalTargets = viewModel.legalTargets,
                                        lastMove = viewModel.lastMove,
                                        kingInCheckSquare = viewModel.kingInCheckSquare,
                                        themeName = viewModel.boardThemeName,
                                        playerColor = userColorInMatchmaking, // Lock to assigned color
                                        highlightLegals = viewModel.highlightLegalMoves,
                                        onSquareClick = {
                                            if (viewModel.sideToMove == userColorInMatchmaking) {
                                                ChessHaptics.playMoveHaptic(context)
                                                viewModel.selectSquare(it)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                        heatmap = null,
                                        animationTrigger = viewModel.animationTrigger,
                                        animationMove = viewModel.animationMove,
                                        editStyle = viewModel.editStyle,
                                        enableCameraZoom = viewModel.enableCameraZoom,
                                        enableMotionBlur = viewModel.enableMotionBlur
                                    )

                                    // Floating reactions anim overlaid
                                    floatingEmojis.forEachIndexed { index, pair ->
                                        val emojiAnimY = remember { Animatable(0f) }
                                        LaunchedEffect(pair) {
                                            emojiAnimY.animateTo(
                                                targetValue = -300f,
                                                animationSpec = tween(2000, easing = LinearOutSlowInEasing)
                                            )
                                            floatingEmojis.remove(pair)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .offset(
                                                    x = (pair.second * 300).dp,
                                                    y = emojiAnimY.value.dp
                                                )
                                        ) {
                                            Text(pair.first, fontSize = 40.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // USER BOX (At bottom)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, if (viewModel.sideToMove == userColorInMatchmaking) colors.primary else colors.border)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(viewModel.currentUser.value?.avatarEmoji ?: "👤", fontSize = 28.sp)
                                            Column {
                                                Text(viewModel.currentUser.value?.username ?: "Guest Player", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text("${viewModel.currentUser.value?.eloRating ?: 1200} ELO • You", style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.6f))
                                            }
                                        }

                                        // Timer display
                                        val userTime = if (userColorInMatchmaking == WHITE) whiteTimerSeconds else blackTimerSeconds
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (viewModel.sideToMove == userColorInMatchmaking) colors.primary.copy(alpha = 0.15f) else colors.surface
                                            )
                                        ) {
                                            Text(
                                                text = formatTime(userTime),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                color = if (viewModel.sideToMove == userColorInMatchmaking) colors.primary else colors.onSurface
                                            )
                                        }
                                    }
                                }

                                // REAL-TIME INTERACTIVE CHAT PANEL
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Chat overlay expansion
                                    OutlinedButton(
                                        onClick = { chatExpanded = !chatExpanded },
                                        border = BorderStroke(1.dp, colors.border)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Chat Room (${chatMessages.size})", fontSize = 12.sp)
                                    }

                                    // Interactive Reactions Selector
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("👍", "😭", "😮", "😡", "😀").forEach { emoji ->
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.surface)
                                                    .border(BorderStroke(1.dp, colors.border), CircleShape)
                                                    .clickable {
                                                        ChessHaptics.playMoveHaptic(context)
                                                        floatingEmojis.add(Pair(emoji, Random().nextFloat()))
                                                        // Auto mock reply based on reaction
                                                        coroutineScope.launch {
                                                            delay(2000)
                                                            val replyEmoji = if (emoji == "👍") "👍" else "😀"
                                                            floatingEmojis.add(Pair(replyEmoji, Random().nextFloat()))
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(emoji, fontSize = 18.sp)
                                            }
                                        }
                                    }
                                }

                                // Expanded chat messages box
                                AnimatedVisibility(visible = chatExpanded) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .padding(top = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                                        border = BorderStroke(1.dp, colors.border)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            LazyColumn(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                items(chatMessages) { msg ->
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(
                                                            text = "${msg.sender}:",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = if (msg.isSystem) colors.primary else colors.glowColor
                                                        )
                                                        Text(msg.text, fontSize = 11.sp, color = colors.onSurface)
                                                    }
                                                }
                                            }

                                            // Quick presets row
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                val presets = listOf("GL!", "Nice Move!", "Are you bot?", "Oops!", "GG!")
                                                presets.forEach { text ->
                                                    Box(
                                                        modifier = Modifier
                                                            .background(colors.background, RoundedCornerShape(4.dp))
                                                            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(4.dp))
                                                            .clickable {
                                                                ChessHaptics.playMoveHaptic(context)
                                                                val senderName = viewModel.currentUser.value?.username ?: "Player"
                                                                chatMessages.add(MultiplayerChatMessage(sender = senderName, text = text))
                                                                
                                                                // Simulate direct auto response
                                                                coroutineScope.launch {
                                                                    delay(1500)
                                                                    val reply = when (text) {
                                                                        "GL!" -> "Thanks! You too"
                                                                        "Nice Move!" -> "Thank you! I found it after some thought"
                                                                        "Are you bot?" -> "No, I am human 100%"
                                                                        "Oops!" -> "Haha, it happens to the best of us"
                                                                        "GG!" -> "GG! Well played"
                                                                        else -> "👍"
                                                                    }
                                                                    chatMessages.add(MultiplayerChatMessage(sender = opponentName, text = reply))
                                                                }
                                                            }
                                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(text, fontSize = 10.sp, color = colors.primary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        MatchmakingState.GAME_OVER -> {
                            // Concluded Lobby screen
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, colors.primary)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = colors.primary, modifier = Modifier.size(64.dp))
                                        Text(
                                            "MATCH CONCLUDED",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = viewModel.gameStatus,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )

                                        Divider(color = colors.border)

                                        // Rating Change telemetry
                                        val currentElo = viewModel.currentUser.value?.eloRating ?: 1200
                                        Text(
                                            text = "Your Rating: $currentElo ELO",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.glowColor
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    ChessHaptics.playMoveHaptic(context)
                                                    selectedMode = null
                                                    matchmakingState = MatchmakingState.IDLE
                                                },
                                                border = BorderStroke(1.dp, colors.border)
                                            ) {
                                                Text("Exit Arena")
                                            }

                                            Button(
                                                onClick = {
                                                    ChessHaptics.playMoveHaptic(context)
                                                    matchmakingState = MatchmakingState.IDLE
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black)
                                            ) {
                                                Text("Find New Match")
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
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d", m, s)
}
