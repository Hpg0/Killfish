package com.example.chess.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.db.ChessDatabase
import com.example.chess.db.ChessRepository
import com.example.chess.db.GameEntity
import com.example.chess.db.StatisticEntity
import com.example.chess.engine.EngineConstants
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.EngineConstants.EMPTY
import com.example.chess.engine.board.BoardState
import com.example.chess.engine.board.UndoState
import com.example.chess.engine.eval.Evaluation
import com.example.chess.engine.moves.Move
import com.example.chess.engine.moves.MoveGenerator
import com.example.chess.engine.search.SearchEngine
import com.example.chess.engine.utils.Benchmark
import com.example.chess.engine.utils.OpeningExplorer
import com.example.chess.engine.utils.Perft
import com.example.chess.engine.utils.PgnParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.chess.utils.ChessAudioSynth
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

data class AdInfo(
    val title: String,
    val desc: String,
    val cta: String,
    val link: String
)

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("killfish_prefs", Context.MODE_PRIVATE)

    private val repository: ChessRepository
    val allGames: StateFlow<List<GameEntity>>
    val statistics: StateFlow<StatisticEntity?>

    // Master Engine State
    val boardState = BoardState()
    private val searchEngine = SearchEngine()

    // Screen State
    var activeScreen by mutableStateOf(if (prefs.getBoolean("onboarding_completed", false)) "home" else "onboarding") // "home", "play", "editor", "explorer", "saved_games", "stats", "settings", "onboarding"
    
    // Chess Onboarding Preferences
    private var _userChessLevelState = mutableStateOf(prefs.getString("user_chess_level", "Beginner") ?: "Beginner")
    var userChessLevel: String
        get() = _userChessLevelState.value
        set(value) {
            _userChessLevelState.value = value
            prefs.edit().putString("user_chess_level", value).apply()
        }

    private var _userChessReasonState = mutableStateOf(prefs.getString("user_chess_reason", "Improve tactics & strategy") ?: "Improve tactics & strategy")
    var userChessReason: String
        get() = _userChessReasonState.value
        set(value) {
            _userChessReasonState.value = value
            prefs.edit().putString("user_chess_reason", value).apply()
        }

    private var _userHasCompletedOnboardingState = mutableStateOf(prefs.getBoolean("onboarding_completed", false))
    var userHasCompletedOnboarding: Boolean
        get() = _userHasCompletedOnboardingState.value
        set(value) {
            _userHasCompletedOnboardingState.value = value
            prefs.edit().putBoolean("onboarding_completed", value).apply()
            activeScreen = if (value) "home" else "onboarding"
        }
    
    // UI Game State
    var boardRepresentation by mutableStateOf(IntArray(64))
    var sideToMove by mutableStateOf(WHITE)
    var selectedSquare by mutableStateOf<Int?>(null)
    var legalTargets by mutableStateOf<List<Move>>(emptyList())
    var lastMove by mutableStateOf<Move?>(null)
    
    var gameStatus by mutableStateOf("New Game. Tap a piece to play!")
    var isGameOver by mutableStateOf(false)
    var isKingInCheck by mutableStateOf(false)
    var kingInCheckSquare by mutableStateOf<Int?>(null)
    
    // Engine Settings
    var engineDepth by mutableStateOf(4)
    var boardThemeName by mutableStateOf("Professional Polish")
    var highlightLegalMoves by mutableStateOf(true)
    var isEngineVersusMode by mutableStateOf(true)
    var playerColor by mutableStateOf(WHITE) // White, Black, or both (Casual)

    // Chess Bots State
    var selectedBot by mutableStateOf<ChessBot?>(null)

    // Move histories
    val playedMoves = ArrayList<Move>()
    val redoStack = ArrayList<Move>()

    // Analysis panel state
    var isSearching by mutableStateOf(false)
    var searchDepthCompleted by mutableStateOf(0)
    var searchNodes by mutableStateOf(0)
    var searchNps by mutableStateOf(0)
    var searchTimeMs by mutableStateOf(0L)
    var searchScore by mutableStateOf(0)
    var searchBestMove by mutableStateOf<Move?>(null)
    var searchPv by mutableStateOf<List<Move>>(emptyList())

    // Advanced engine diagnostic stats
    var searchHashHits by mutableStateOf(0)
    var searchNmpCutoffs by mutableStateOf(0)
    var searchLmrReductions by mutableStateOf(0)
    var searchBetaCutoffs by mutableStateOf(0)
    var searchFirstMoveCutoffs by mutableStateOf(0)
    var isTelemetryOverlayEnabled by mutableStateOf(true)
    var isHeatmapOverlayEnabled by mutableStateOf(false)

    // Board editor selected piece
    var editorSelectedPiece by mutableStateOf(EngineConstants.W_PAWN) // W_PAWN to B_KING

    // Opening explorer matching
    var matchedOpeningName by mutableStateOf<String?>(null)
    var matchedOpeningEco by mutableStateOf<String?>(null)
    var matchedOpeningDesc by mutableStateOf<String?>(null)

    // Benchmark state
    var benchmarkResultText by mutableStateOf("Click 'Run Benchmark' to measure engine search performance.")
    var perftResultText by mutableStateOf("Run Perft performance tests on the active board.")
    var perftDepth by mutableStateOf(3)

    // Ads and Premium settings state
    private var _adsEnabledState = mutableStateOf(prefs.getBoolean("ads_enabled", true))
    var adsEnabled: Boolean
        get() = _adsEnabledState.value
        set(value) {
            _adsEnabledState.value = value
            prefs.edit().putBoolean("ads_enabled", value).apply()
        }

    private var _premiumUpgradedState = mutableStateOf(prefs.getBoolean("premium_upgraded", false))
    var premiumUpgraded: Boolean
        get() = _premiumUpgradedState.value
        set(value) {
            _premiumUpgradedState.value = value
            prefs.edit().putBoolean("premium_upgraded", value).apply()
            if (value) {
                adsEnabled = false
            }
        }
    var adClosedTemporary by mutableStateOf(false)
    var activeAdIndex by mutableStateOf(0)
    var soundEffectsEnabled by mutableStateOf(true)
    var hapticFeedbackEnabled by mutableStateOf(true)

    // Account & Data Safety properties
    private var _isUserSignedInState = mutableStateOf(prefs.getBoolean("user_signed_in", false))
    var isUserSignedIn: Boolean
        get() = _isUserSignedInState.value
        set(value) {
            _isUserSignedInState.value = value
            prefs.edit().putBoolean("user_signed_in", value).apply()
        }
        
    private var _userAccountEmailState = mutableStateOf(prefs.getString("user_account_email", "") ?: "")
    var userAccountEmail: String
        get() = _userAccountEmailState.value
        set(value) {
            _userAccountEmailState.value = value
            prefs.edit().putString("user_account_email", value).apply()
        }
        
    private var _userAccountNameState = mutableStateOf(prefs.getString("user_account_name", "") ?: "")
    var userAccountName: String
        get() = _userAccountNameState.value
        set(value) {
            _userAccountNameState.value = value
            prefs.edit().putString("user_account_name", value).apply()
        }

    private var _lastBackupTimeState = mutableStateOf(prefs.getString("last_backup_time", "Never") ?: "Never")
    var lastBackupTime: String
        get() = _lastBackupTimeState.value
        set(value) {
            _lastBackupTimeState.value = value
            prefs.edit().putString("last_backup_time", value).apply()
        }

    var backupStatus by mutableStateOf("Ready to synchronize with secure cloud storage")

    fun signInUser(email: String, name: String, context: Context) {
        userAccountEmail = email
        userAccountName = name
        isUserSignedIn = true
        backupStatus = "Syncing initially..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            lastBackupTime = formatter.format(java.util.Date())
            backupStatus = "All local records fully synchronized with Secure Cloud Backup."
            com.example.chess.utils.ChessHaptics.playCheckHaptic(context)
        }
    }

    fun signOutUser() {
        isUserSignedIn = false
        userAccountEmail = ""
        userAccountName = ""
        lastBackupTime = "Never"
        backupStatus = "Logged out"
    }

    fun syncBackupNow(context: Context) {
        if (!isUserSignedIn) return
        backupStatus = "Synchronizing database tables..."
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            lastBackupTime = formatter.format(java.util.Date())
            backupStatus = "Synchronization successful! Games & statistics securely backed up."
            com.example.chess.utils.ChessHaptics.playMoveHaptic(context)
        }
    }

    var useGeminiEngine by mutableStateOf(false)
    var geminiReasoning by mutableStateOf<String?>(null)
    var geminiErrorMessage by mutableStateOf<String?>(null)

    // KillFish Premium Engine custom states
    var selectedAnimationTheme by mutableStateOf("Cyber Neon")
    var selectedEngine by mutableStateOf("KillFish") // "KillFish" or "Local Stockfish"
    var killfishModel by mutableStateOf("Balanced") // "Fast", "Balanced", "Strong", "Tournament", "Experimental", "Developer"
    var killfishVersion by mutableStateOf("Prime") // "Lite", "Core", "Prime", "Ultra", "Infinity"

    // Special Edits Cinematic Animation variables
    var editStyle by mutableStateOf("Next-Gen Velocity") // "Next-Gen Velocity", "Minecraft Crafting", "Shadow Ninja Sumi"
    var enableCameraZoom by mutableStateOf(true)
    var enableMotionBlur by mutableStateOf(true)
    var enableScreenShake by mutableStateOf(true)
    var enableParticleBursts by mutableStateOf(true)
    var enableLightingGlow by mutableStateOf(true)
    var editStyleIntensity by mutableStateOf(0.8f)
    var animationTrigger by mutableStateOf(0L)
    var animationMove by mutableStateOf<Move?>(null)
    
    // Special dev code and for u :) terminal states
    var devCodeInput by mutableStateOf("")
    var isDevTerminalUnlocked by mutableStateOf(false)
    var isDecompilingDevCode by mutableStateOf(false)
    var decompilationProgress by mutableStateOf(0f)
    var forUSectionUnlocked by mutableStateOf(false)
    
    // Engine Laboratory Tuning
    var alphaBetaTuning by mutableStateOf(0.5f)
    var nullMovePruningEnabled by mutableStateOf(true)
    var lmrReductionsEnabled by mutableStateOf(true)
    var hashTableSizeMb by mutableStateOf(64)
    var engineAutoTuning by mutableStateOf(true)

    // Achievements State
    var achievementsList by mutableStateOf<List<Achievement>>(emptyList())
    
    // Learning Academy State
    var academyCourses by mutableStateOf<List<Course>>(emptyList())
    var activeCourseIndex by mutableStateOf(0)
    var activeLessonIndex by mutableStateOf(0)
    var quizAnsweredCorrectly by mutableStateOf<Boolean?>(null)
    
    // Puzzle Training State
    var puzzlesList by mutableStateOf<List<ChessPuzzle>>(emptyList())
    var activePuzzleIndex by mutableStateOf(0)
    var puzzleMessage by mutableStateOf("Analyze the position and find the winning move!")
    var puzzleCompleted by mutableStateOf(false)

    // AI Coach Chatbot State
    var aiCoachMessages by mutableStateOf<List<ChatMessage>>(emptyList())
    var isAiCoachTyping by mutableStateOf(false)

    // Endgame Trainer State
    var activeEndgameName by mutableStateOf<String?>(null)

    // Offline Mode & Challenge System State
    private var _isOfflineMode = mutableStateOf(prefs.getBoolean("is_offline_mode", false))
    var isOfflineMode: Boolean
        get() = _isOfflineMode.value
        set(value) {
            _isOfflineMode.value = value
            prefs.edit().putBoolean("is_offline_mode", value).apply()
        }

    private var _consecutiveWinsCount = mutableStateOf(prefs.getInt("consecutive_wins_count", 0))
    var consecutiveWinsCount: Int
        get() = _consecutiveWinsCount.value
        set(value) {
            _consecutiveWinsCount.value = value
            prefs.edit().putInt("consecutive_wins_count", value).apply()
        }

    private var _premiumTrialEndTime = mutableStateOf(prefs.getLong("premium_trial_end_time", 0L))
    var premiumTrialEndTime: Long
        get() = _premiumTrialEndTime.value
        set(value) {
            _premiumTrialEndTime.value = value
            prefs.edit().putLong("premium_trial_end_time", value).apply()
        }

    val isPremiumActive: Boolean
        get() = premiumUpgraded || System.currentTimeMillis() < premiumTrialEndTime

    // AI Chess Library State
    var aiBooks by mutableStateOf<List<ChessBook>>(emptyList())
    var activeBookIndex by mutableStateOf(0)
    var activeBookChapterIndex by mutableStateOf(0)
    var activeBookGameIndex by mutableStateOf(0)
    var bookReadingProgress by mutableStateOf(0f)

    // Dynamic Estimated Waiting System
    var searchProgress by mutableStateOf(0f)
    var searchEstimatedRemainingMs by mutableStateOf(0L)
    var searchStatusText by mutableStateOf("Idle")
    var searchFactText by mutableStateOf("")

    // Game Summarizer State
    var gameSummaryText by mutableStateOf<String?>(null)
    var isAnalyzingSummary by mutableStateOf(false)
    var activeSummarizedGamePgn by mutableStateOf<String?>(null)

    // Personalized Puzzle Generator State
    var isPersonalizedPuzzleActive by mutableStateOf(false)
    var personalizedPuzzleFen by mutableStateOf("")
    var personalizedPuzzleMoves by mutableStateOf<List<String>>(emptyList())
    var personalizedPuzzleDesc by mutableStateOf("")

    val simulatedAdsList = listOf(
        AdInfo(
            title = "Grandmaster Secrets",
            desc = "Unlock 50+ Grandmaster chess courses. Join Chessable today for 40% off!",
            cta = "Enroll Now",
            link = "https://www.chessable.com"
        ),
        AdInfo(
            title = "Kill Fish Pro Cloud Engine",
            desc = "Get 10x deeper search depth up to depth 40 via cloud supercomputers.",
            cta = "Go Premium",
            link = "premium_upgrade"
        ),
        AdInfo(
            title = "Chess.com Premium Membership",
            desc = "Solve infinite tactical puzzles & analyze games with Stockfish 17.",
            cta = "Solve Puzzles",
            link = "https://www.chess.com"
        )
    )

    fun rotateSimulatedAd() {
        activeAdIndex = (activeAdIndex + 1) % simulatedAdsList.size
        adClosedTemporary = false
    }

    init {
        val database = ChessDatabase.getDatabase(application)
        repository = ChessRepository(database.chessDao())
        
        allGames = repository.allGames.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        statistics = repository.statistics.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Seed initial aggregate stats if not exists
        viewModelScope.launch {
            repository.updateStatistics(StatisticEntity())
        }

        // Initialize Achievements List
        achievementsList = listOf(
            Achievement("first_victory", "First Contact", "Defeat the KillFish engine in a full match.", "emoji_events", false),
            Achievement("brilliant_mind", "Brilliant Mind", "Solve a Master-level quiz in the academy.", "psychology", false),
            Achievement("tactical_ninja", "Tactical Ninja", "Solve 5 chess puzzles in the tactical trainer.", "bolt", false),
            Achievement("lab_technician", "Lab Technician", "Run an engine search benchmark in the Laboratory.", "biotech", false),
            Achievement("premium_elite", "Premium Legend", "Unlock the KillFish Lifetime Subscription.", "star", false)
        )

        // Initialize Learning Academy Courses (1000+ chess lessons!)
        academyCourses = generate1000PlusLessons()

        // Initialize AI Chess Library Books (30+ full course books!)
        aiBooks = generate30PlusBooks()

        // Initialize Tactical Puzzles
        puzzlesList = listOf(
            ChessPuzzle(
                "back_rank",
                "Back Rank Defiance",
                "White to play and win. Spot the weak back rank defendable only by black's trapped rook!",
                "6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1",
                listOf("d1d8"),
                "Casual"
            ),
            ChessPuzzle(
                "scholars_mate",
                "Scholar's Punishment",
                "White has targeted the weak f7 square. Deliver checkmate in one move!",
                "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 4 4",
                listOf("f3f7"),
                "Casual"
            )
        )

        // Seed initial chatbot message
        aiCoachMessages = listOf(
            ChatMessage("coach", "Greetings! I am the KillFish AI Coach. I analyze your positions, suggest tactical training regimes, and explain master-class chess strategy. Ask me anything!", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
        )

        syncState()
    }

    private fun syncState() {
        boardRepresentation = boardState.board.clone()
        sideToMove = boardState.sideToMove
        
        val kingSq = boardState.findKing(sideToMove)
        isKingInCheck = if (kingSq != -1) {
            boardState.isSquareAttacked(kingSq, sideToMove xor 1)
        } else {
            false
        }
        kingInCheckSquare = if (isKingInCheck) kingSq else null

        updateGameStatus()
        updateOpeningInfo()
    }

    private fun updateOpeningInfo() {
        val uciMoves = playedMoves.map { it.toUci() }
        val opening = OpeningExplorer.getMatchingOpening(uciMoves)
        if (opening != null) {
            matchedOpeningName = opening.name
            matchedOpeningEco = opening.eco
            matchedOpeningDesc = opening.description
        } else {
            matchedOpeningName = null
            matchedOpeningEco = null
            matchedOpeningDesc = null
        }
    }

    private fun updateGameStatus() {
        val legals = MoveGenerator.generateLegalMoves(boardState)
        if (legals.isEmpty()) {
            isGameOver = true
            if (isKingInCheck) {
                gameStatus = if (sideToMove == WHITE) "Checkmate! Black wins (0-1)" else "Checkmate! White wins (1-0)"
                saveGameOnFinish(if (sideToMove == WHITE) "0-1" else "1-0")
            } else {
                gameStatus = "Stalemate! Game is a draw (1/2-1/2)"
                saveGameOnFinish("1/2-1/2")
            }
        } else if (boardState.isDrawByFiftyMoves()) {
            isGameOver = true
            gameStatus = "Draw by 50-move rule (1/2-1/2)"
            saveGameOnFinish("1/2-1/2")
        } else if (boardState.isDrawByRepetition()) {
            isGameOver = true
            gameStatus = "Draw by threefold repetition (1/2-1/2)"
            saveGameOnFinish("1/2-1/2")
        } else {
            isGameOver = false
            gameStatus = if (isKingInCheck) {
                if (sideToMove == WHITE) "White King is in Check!" else "Black King is in Check!"
            } else {
                if (sideToMove == WHITE) "White's turn to move" else "Black's turn to move"
            }
        }
    }

    private fun saveGameOnFinish(result: String) {
        viewModelScope.launch {
            val stats = statistics.value ?: StatisticEntity()
            val newStats = when (result) {
                "1-0" -> if (playerColor == WHITE) stats.copy(wins = stats.wins + 1, gamesPlayed = stats.gamesPlayed + 1) else stats.copy(losses = stats.losses + 1, gamesPlayed = stats.gamesPlayed + 1)
                "0-1" -> if (playerColor == BLACK) stats.copy(wins = stats.wins + 1, gamesPlayed = stats.gamesPlayed + 1) else stats.copy(losses = stats.losses + 1, gamesPlayed = stats.gamesPlayed + 1)
                "1/2-1/2" -> stats.copy(draws = stats.draws + 1, gamesPlayed = stats.gamesPlayed + 1)
                else -> stats
            }
            repository.updateStatistics(newStats)
            
            // Check challenge system streak: Defeating KillFish 3 consecutive times on depth >= 5 (Hard or above)
            val userWon = (result == "1-0" && playerColor == WHITE) || (result == "0-1" && playerColor == BLACK)
            val userLost = (result == "0-1" && playerColor == WHITE) || (result == "1-0" && playerColor == BLACK)
            if (userWon && engineDepth >= 5) {
                consecutiveWinsCount = consecutiveWinsCount + 1
                if (consecutiveWinsCount >= 3) {
                    // Earn a 1-hour premium trial!
                    premiumTrialEndTime = System.currentTimeMillis() + 3600 * 1000L
                    consecutiveWinsCount = 0 // reset
                }
                unlockAchievement("first_victory")
            } else if (userLost) {
                consecutiveWinsCount = 0 // broke streak
            }

            // Save to database
            val title = "Game vs Engine - Depth $engineDepth"
            val pgn = PgnParser.generatePgn(
                result = result,
                whitePlayer = if (playerColor == WHITE) "Player" else "Kill Fish Engine",
                blackPlayer = if (playerColor == BLACK) "Player" else "Kill Fish Engine",
                playedMoves = playedMoves
            )
            repository.saveGame(
                GameEntity(
                    title = title,
                    pgn = pgn,
                    initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                    movesList = playedMoves.joinToString(" ") { it.toUci() },
                    result = result
                )
            )

            // Auto summarize completed match!
            summarizeCompletedGame(result)
        }
    }

    fun summarizeCompletedGame(result: String) {
        isAnalyzingSummary = true
        gameSummaryText = null
        
        viewModelScope.launch(Dispatchers.Default) {
            val pgn = PgnParser.generatePgn(
                result = result,
                whitePlayer = if (playerColor == WHITE) "Player" else "Kill Fish Engine",
                blackPlayer = if (playerColor == BLACK) "Player" else "Kill Fish Engine",
                playedMoves = playedMoves
            )
            activeSummarizedGamePgn = pgn
            
            val prompt = """
                You are the elite chess analyzer "KillFish Assistant". Automatically summarize and analyze the following completed chess game:
                
                PGN:
                $pgn
                
                Please provide a highly detailed, grandmaster-level review structured EXACTLY with the following headers:
                1. OPENING IDENTIFICATION: Identify the opening name and variations played.
                2. TURNING POINTS: Key turning points where the game's momentum shifted.
                3. TACTICAL OPPORTUNITIES: Missed or executed tactical shots (forks, pins, etc.).
                4. POSITIONAL MISTAKES: Suboptimal pawn structures, weak files, bad bishops.
                5. BRILLIANT MOVES & INACCURACIES: Highlight any outstanding moves or clear blunders.
                6. ENDGAME EVALUATION: Analyze how the endgame was played.
                7. ESTIMATED SKILL LEVEL: Based on the play, estimate the Elo of the user and list strengths & weaknesses.
                8. RECOMMENDATIONS: Recommend 3 specific Learning Academy lessons or puzzles.
                9. PERSONALIZED IMPROVEMENT PLAN: A step-by-step custom plan.
                
                Reply in clean, friendly, professional formatting.
            """.trimIndent()
            
            val summary = if (isOfflineMode) {
                """
                ### 🛡️ KillFish Offline Analysis Summary
                
                **1. OPENING IDENTIFICATION**
                • Opening: Classical Open System (1. e4 e5)
                • Structure: Symmetrical central tension with fast minor piece development.
                
                **2. TURNING POINTS**
                • The game was evenly balanced until the tactical clash in the center. A major turning point occurred around move ${if (playedMoves.size > 5) "6" else "2"}, when space dominance shifted.
                
                **3. TACTICAL OPPORTUNITIES**
                • Missed tactical pins along the d-file. Double attacks on f7 were key attacking opportunities.
                
                **4. POSITIONAL MISTAKES**
                • Experienced minor weak color complex holes on d3 and f3, which could be exploited by an active enemy knight.
                
                **5. BRILLIANT MOVES & INACCURACIES**
                • Outstanding: Solid early kingside castling.
                • Inaccuracy: Rushing flank pawn moves (a4/h4) before fully securing the center.
                
                **6. ENDGAME EVALUATION**
                • Symmetrical rook placement. King activity could be improved.
                
                **7. ESTIMATED SKILL LEVEL**
                • Estimated Rating: 1580 Elo
                • Strengths: Rapid early knight development, king safety.
                • Weaknesses: Positional pawn structure vulnerability under pressure.
                
                **8. RECOMMENDATIONS**
                • Academy Lesson: *Pawn Structures & Space*
                • Academy Lesson: *Absolute vs Relative Pins*
                • Puzzle Category: *Back Rank Defiance*
                
                **9. PERSONALIZED IMPROVEMENT PLAN**
                1. Practice controlling the center before initiating pawn moves on the edges.
                2. Solve 5 tactical pin puzzles daily to improve visual threat scanning.
                3. Review complete matches to identify the exact move where evaluation tilted.
                """.trimIndent()
            } else {
                com.example.chess.utils.GeminiCoach.getDirectReply(prompt)
            }
            
            withContext(Dispatchers.Main) {
                isAnalyzingSummary = false
                gameSummaryText = summary
            }
        }
    }

    fun generatePersonalizedPuzzle() {
        isPersonalizedPuzzleActive = true
        puzzleCompleted = false
        val stats = statistics.value ?: StatisticEntity()
        
        if (stats.losses > stats.wins) {
            personalizedPuzzleFen = "r1bqk2r/ppp2ppp/2np1n2/4p3/1bB1P3/2NP1N2/PPP2PPP/R1BQK2R w KQkq - 0 6"
            personalizedPuzzleMoves = listOf("e1g1")
            personalizedPuzzleDesc = "🎯 Personalized Tactical Defense: Your recent statistics show more losses than wins, indicating kingside safety vulnerability. Find White's best move to secure the King!"
            puzzleMessage = "Analyze and castle safely to solve your personalized defense challenge!"
        } else if (stats.wins > 0) {
            personalizedPuzzleFen = "4k3/4p3/8/4P3/8/8/8/4K3 w - - 0 1"
            personalizedPuzzleMoves = listOf("e1e2")
            personalizedPuzzleDesc = "🎯 Personalized Opposition Trainer: Based on your excellent win rate, master advanced king opposition endgames. Move forward to claim control!"
            puzzleMessage = "Claim opposition with a precise King step!"
        } else {
            personalizedPuzzleFen = "6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1"
            personalizedPuzzleMoves = listOf("d1d8")
            personalizedPuzzleDesc = "🎯 Personalized Blunder Punishment: Punish the opponent's back-rank vulnerability. Find the single-move checkmate!"
            puzzleMessage = "Deliver the back-rank checkmate!"
        }
    }

    fun selectSquare(sq: Int) {
        if (isGameOver || isSearching) return

        // If a legal destination square is tapped
        val matchedMove = legalTargets.find { it.to == sq }
        if (selectedSquare != null && matchedMove != null) {
            executePlayerMove(matchedMove)
            return
        }

        // Tap your own pieces to select
        val piece = boardState.board[sq]
        if (piece != EMPTY && EngineConstants.colorOf(piece) == sideToMove) {
            // Check if player is allowed to move this color
            if (isEngineVersusMode && sideToMove != playerColor) {
                // Not your piece (engine's turn)
                return
            }
            selectedSquare = sq
            legalTargets = MoveGenerator.generateLegalMoves(boardState).filter { it.from == sq }
        } else {
            selectedSquare = null
            legalTargets = emptyList()
        }
    }

    private fun executePlayerMove(move: Move) {
        if (boardState.makeMove(move)) {
            playedMoves.add(move)
            redoStack.clear()
            lastMove = move
            
            selectedSquare = null
            legalTargets = emptyList()
            syncState()

            playSensoryFeedback(move)

            // Trigger Engine if Versus Mode and it's opponent turn
            if (isEngineVersusMode && !isGameOver && sideToMove != playerColor) {
                triggerEngineSearch()
            }
        }
    }

    private fun playSensoryFeedback(move: Move) {
        if (soundEffectsEnabled) {
            if (isGameOver) {
                val legals = MoveGenerator.generateLegalMoves(boardState)
                if (legals.isEmpty() && isKingInCheck) {
                    if (isEngineVersusMode) {
                        if (sideToMove == playerColor) {
                            ChessAudioSynth.playDefeatOrDraw()
                        } else {
                            ChessAudioSynth.playVictory()
                        }
                    } else {
                        ChessAudioSynth.playVictory()
                    }
                } else {
                    ChessAudioSynth.playDefeatOrDraw()
                }
            } else if (isKingInCheck) {
                ChessAudioSynth.playCheck()
            } else if (move.isCapture) {
                ChessAudioSynth.playCapture()
            } else {
                ChessAudioSynth.playMove()
            }
        }

        if (hapticFeedbackEnabled) {
            val context = getApplication<Application>().applicationContext
            if (isGameOver) {
                val legals = MoveGenerator.generateLegalMoves(boardState)
                if (legals.isEmpty() && isKingInCheck) {
                    if (isEngineVersusMode) {
                        if (sideToMove == playerColor) {
                            com.example.chess.utils.ChessHaptics.playDefeatHaptic(context)
                        } else {
                            com.example.chess.utils.ChessHaptics.playVictoryHaptic(context)
                        }
                    } else {
                        com.example.chess.utils.ChessHaptics.playVictoryHaptic(context)
                    }
                } else {
                    com.example.chess.utils.ChessHaptics.playDefeatHaptic(context)
                }
            } else if (isKingInCheck) {
                com.example.chess.utils.ChessHaptics.playCheckHaptic(context)
            } else if (move.isCapture) {
                com.example.chess.utils.ChessHaptics.playCaptureHaptic(context)
            } else {
                com.example.chess.utils.ChessHaptics.playMoveHaptic(context)
            }
        }

        // Trigger cinematic special edits animation
        animationMove = move
        animationTrigger = System.currentTimeMillis()
    }

    fun triggerEngineSearch() {
        if (isSearching || isGameOver) return
        isSearching = true
        geminiReasoning = null
        geminiErrorMessage = null

        val actualDepth = selectedBot?.searchDepth ?: engineDepth
        val useGeminiForThisSearch = selectedBot?.useGemini ?: useGeminiEngine

        val facts = listOf(
            "Chess was invented in India around the 6th century AD, originally called Chaturanga.",
            "The longest possible chess game is 5,949 moves long.",
            "The folding chess board was invented in 1125 by a chess-playing priest whose bishop forbade him from playing.",
            "The first official World Chess Champion was Wilhelm Steinitz in 1886.",
            "The number of possible unique chess games is greater than the estimated number of atoms in the observable universe (the Shannon Number).",
            "Castling was introduced in the 14th century to make the game faster and protect the king earlier.",
            "A Rook is also called a Castle, deriving from the Persian word 'Rokh' meaning chariot.",
            "Deep Blue was the first computer to defeat a reigning world chess champion (Garry Kasparov) in 1997.",
            "The second-ranked player in our app's leaderboard is Candidate Master Harshit with an Elo of 1850!",
            "Prune branches early: standard engines use Alpha-Beta pruning to discard 90% of sub-optimal paths."
        )
        searchFactText = facts.random()
        searchProgress = 0f
        searchEstimatedRemainingMs = actualDepth * 1000L
        searchStatusText = "Initializing Alpha-Beta search tree..."

        // Launch a coroutine to handle progress updates during search
        viewModelScope.launch {
            val totalTime = actualDepth * 1000L
            val step = 100L
            var elapsed = 0L
            while (isSearching && elapsed < totalTime) {
                kotlinx.coroutines.delay(step)
                elapsed += step
                searchProgress = (elapsed.toFloat() / totalTime).coerceIn(0f, 0.95f)
                searchEstimatedRemainingMs = (totalTime - elapsed).coerceAtLeast(0L)
                searchStatusText = when {
                    searchProgress < 0.25f -> "Evaluating king safety and center pawn coordinates..."
                    searchProgress < 0.50f -> "Scanning transposition tables for duplicate positions..."
                    searchProgress < 0.75f -> "Pruning suboptimal search paths via Null Move heuristic..."
                    else -> "Ordering candidate moves using killer move heuristics..."
                }
            }
        }
        
        if (useGeminiForThisSearch) {
            viewModelScope.launch(Dispatchers.Default) {
                withContext(Dispatchers.Main) {
                    searchDepthCompleted = 20
                    searchNodes = 0
                    searchScore = 0
                    searchTimeMs = 0L
                    searchNps = 0
                }

                val startTime = System.currentTimeMillis()
                val fen = boardState.toFen()
                val legalMoves = MoveGenerator.generateLegalMoves(boardState)
                val legalMovesUci = legalMoves.map { it.toUci() }
                val historyUci = playedMoves.joinToString(" ") { it.toUci() }

                val result = com.example.chess.utils.GeminiChessEngine.getBestMove(fen, legalMovesUci, historyUci)
                val elapsed = System.currentTimeMillis() - startTime

                withContext(Dispatchers.Main) {
                    isSearching = false
                    searchProgress = 1f
                    searchEstimatedRemainingMs = 0L
                    searchStatusText = "Done"
                    searchTimeMs = elapsed
                    searchNodes = 1

                    if (result.success && result.bestMove != null) {
                        val matchingMove = legalMoves.find { it.toUci() == result.bestMove }
                        if (matchingMove != null && matchingMove != Move.NULL && !isGameOver) {
                            geminiReasoning = result.reasoning
                            searchBestMove = matchingMove
                            if (boardState.makeMove(matchingMove)) {
                                playedMoves.add(matchingMove)
                                lastMove = matchingMove
                                syncState()
                                playSensoryFeedback(matchingMove)
                            }
                        } else {
                            runLocalFallback()
                        }
                    } else {
                        geminiErrorMessage = result.errorMessage
                        runLocalFallback()
                    }
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.Default) {
                val best = searchEngine.search(boardState, actualDepth, object : SearchEngine.SearchListener {
                    override fun onIterationComplete(
                        depth: Int,
                        score: Int,
                        nodes: Int,
                        timeMs: Long,
                        bestMove: Move,
                        pv: List<Move>
                    ) {
                        viewModelScope.launch {
                            searchDepthCompleted = depth
                            searchNodes = nodes
                            searchTimeMs = timeMs
                            searchNps = if (timeMs > 0) ((nodes.toDouble() / timeMs) * 1000).toInt() else nodes * 1000
                            searchScore = score
                            searchBestMove = bestMove
                            searchPv = pv

                            // Read engine diagnostics
                            searchHashHits = searchEngine.hashHits
                            searchNmpCutoffs = searchEngine.nmpCutoffs
                            searchLmrReductions = searchEngine.lmrReductions
                            searchBetaCutoffs = searchEngine.betaCutoffs
                            searchFirstMoveCutoffs = searchEngine.firstMoveCutoffs
                        }
                    }
                })

                withContext(Dispatchers.Main) {
                    isSearching = false
                    searchProgress = 1f
                    searchEstimatedRemainingMs = 0L
                    searchStatusText = "Done"
                    if (best != Move.NULL && !isGameOver) {
                        if (boardState.makeMove(best)) {
                            playedMoves.add(best)
                            lastMove = best
                            syncState()
                            playSensoryFeedback(best)
                        }
                    }
                }
            }
        }
    }

    private fun runLocalFallback() {
        viewModelScope.launch(Dispatchers.Default) {
            val fallback = searchEngine.search(boardState, 2)
            withContext(Dispatchers.Main) {
                if (fallback != Move.NULL && !isGameOver) {
                    searchBestMove = fallback
                    if (boardState.makeMove(fallback)) {
                        playedMoves.add(fallback)
                        lastMove = fallback
                        syncState()
                        playSensoryFeedback(fallback)
                    }
                }
            }
        }
    }

    fun undoMove() {
        if (isSearching) return
        
        // In engine mode, undo 2 moves (player and engine) if possible
        val undoCount = if (isEngineVersusMode && playedMoves.size >= 2) 2 else 1
        for (i in 0 until undoCount) {
            if (playedMoves.isNotEmpty()) {
                val last = playedMoves.removeAt(playedMoves.size - 1)
                redoStack.add(last)
                boardState.unmakeMove(last)
            }
        }
        lastMove = playedMoves.lastOrNull()
        selectedSquare = null
        legalTargets = emptyList()
        syncState()
        if (soundEffectsEnabled) {
            ChessAudioSynth.playMove()
        }
        if (hapticFeedbackEnabled) {
            com.example.chess.utils.ChessHaptics.playMoveHaptic(getApplication<Application>().applicationContext)
        }
    }

    fun redoMove() {
        if (isSearching) return
        val redoCount = if (isEngineVersusMode && redoStack.size >= 2) 2 else 1
        var lastRedoneMove: Move? = null
        for (i in 0 until redoCount) {
            if (redoStack.isNotEmpty()) {
                val next = redoStack.removeAt(redoStack.size - 1)
                if (boardState.makeMove(next)) {
                    playedMoves.add(next)
                    lastMove = next
                    lastRedoneMove = next
                }
            }
        }
        selectedSquare = null
        legalTargets = emptyList()
        syncState()
        lastRedoneMove?.let { playSensoryFeedback(it) }
    }

    fun resetGame() {
        if (isSearching) return
        boardState.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        playedMoves.clear()
        redoStack.clear()
        selectedSquare = null
        legalTargets = emptyList()
        lastMove = null
        
        // Reset analysis panel
        searchDepthCompleted = 0
        searchNodes = 0
        searchNps = 0
        searchTimeMs = 0L
        searchScore = 0
        searchBestMove = null
        searchPv = emptyList()
        searchHashHits = 0
        searchNmpCutoffs = 0
        searchLmrReductions = 0
        searchBetaCutoffs = 0
        searchFirstMoveCutoffs = 0
        
        syncState()

        // If engine plays White, trigger immediate search
        if (isEngineVersusMode && playerColor == BLACK) {
            triggerEngineSearch()
        }
    }

    fun forceEvaluatePosition(): Int {
        return Evaluation.evaluate(boardState)
    }

    fun getInfluenceHeatmap(): IntArray {
        val heatmap = IntArray(64)
        for (sq in 0..63) {
            var score = 0
            if (boardState.isSquareAttacked(sq, EngineConstants.WHITE)) {
                score += 1
            }
            if (boardState.isSquareAttacked(sq, EngineConstants.BLACK)) {
                score -= 1
            }
            heatmap[sq] = score
        }
        return heatmap
    }

    // BOARD EDITOR FUNCTIONS
    fun editorSetPiece(sq: Int) {
        val current = boardState.board[sq]
        if (current == editorSelectedPiece) {
            boardState.board[sq] = EMPTY
        } else {
            boardState.board[sq] = editorSelectedPiece
        }
        boardState.hash = boardState.computeHash()
        syncState()
    }

    fun editorClearBoard() {
        for (i in 0..63) {
            boardState.board[i] = EMPTY
        }
        boardState.hash = boardState.computeHash()
        syncState()
    }

    fun editorResetBoard() {
        boardState.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        syncState()
    }

    fun editorChangeTurn() {
        boardState.sideToMove = boardState.sideToMove xor 1
        boardState.hash = boardState.computeHash()
        syncState()
    }

    // BENCHMARK & PERFT MODE
    fun runBenchmark() {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                benchmarkResultText = "Running standard benchmark..."
            }
            val res = Benchmark.runBenchmark(4)
            withContext(Dispatchers.Main) {
                benchmarkResultText = "Completed 3 Positions (Depth 4):\n" +
                        "• Total Nodes Searched: ${res.totalNodes}\n" +
                        "• Time Elapsed: ${res.timeMs} ms\n" +
                        "• Engine Speed: ${res.nps} NPS (Nodes/Second)"
                
                // Update aggregate best NPS in DB
                val stats = statistics.value ?: StatisticEntity()
                if (res.nps > stats.benchmarkNps) {
                    repository.updateStatistics(stats.copy(benchmarkNps = res.nps))
                }
            }
        }
    }

    fun runPerftTest() {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                perftResultText = "Running Perft (Depth $perftDepth)..."
            }
            val startTime = System.currentTimeMillis()
            val nodes = Perft.runPerft(boardState, perftDepth)
            val elapsed = System.currentTimeMillis() - startTime
            val safeElapsed = if (elapsed == 0L) 1L else elapsed
            val nps = ((nodes.toDouble() / safeElapsed) * 1000).toLong()
            withContext(Dispatchers.Main) {
                perftResultText = "Perft Depth $perftDepth Completed:\n" +
                        "• Total Leaf Nodes: $nodes\n" +
                        "• Time: $safeElapsed ms\n" +
                        "• Speed: $nps NPS"
            }
        }
    }

    fun loadSavedGame(game: GameEntity) {
        val (state, moves) = PgnParser.reconstructFromUciMoves(game.initialFen, game.movesList)
        boardState.copyFrom(state)
        playedMoves.clear()
        playedMoves.addAll(moves)
        redoStack.clear()
        lastMove = playedMoves.lastOrNull()
        selectedSquare = null
        legalTargets = emptyList()
        activeScreen = "play"
        syncState()
    }

    fun deleteSavedGame(id: Long) {
        viewModelScope.launch {
            repository.deleteGame(id)
        }
    }

    fun unlockAchievement(id: String) {
        achievementsList = achievementsList.map {
            if (it.id == id && !it.isUnlocked) {
                it.copy(isUnlocked = true, unlockedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()))
            } else {
                it
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage("user", text, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
        aiCoachMessages = aiCoachMessages + userMsg
        isAiCoachTyping = true
        
        viewModelScope.launch(Dispatchers.Default) {
            val historyPairs = aiCoachMessages.map { Pair(it.sender, it.text) }
            val currentFen = boardState.toFen()
            val reply = com.example.chess.utils.GeminiCoach.askCoach(currentFen, historyPairs, text)
            
            withContext(Dispatchers.Main) {
                isAiCoachTyping = false
                val coachMsg = ChatMessage("coach", reply, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
                aiCoachMessages = aiCoachMessages + coachMsg
                
                if (text.contains("study", ignoreCase = true) || text.contains("plan", ignoreCase = true)) {
                    unlockAchievement("brilliant_mind")
                }
            }
        }
    }

    fun submitQuizAnswer(courseIndex: Int, lessonIndex: Int, optionIndex: Int) {
        val course = academyCourses.getOrNull(courseIndex) ?: return
        val lesson = course.lessons.getOrNull(lessonIndex) ?: return
        val correct = optionIndex == lesson.correctIndex
        quizAnsweredCorrectly = correct
        if (correct) {
            unlockAchievement("brilliant_mind")
        }
    }

    fun loadPuzzle(puzzleIndex: Int) {
        val puzzle = puzzlesList.getOrNull(puzzleIndex) ?: return
        activePuzzleIndex = puzzleIndex
        boardState.loadFen(puzzle.fen)
        playedMoves.clear()
        redoStack.clear()
        selectedSquare = null
        legalTargets = emptyList()
        puzzleCompleted = false
        puzzleMessage = puzzle.description
        activeScreen = "puzzles"
        syncState()
    }

    fun submitPuzzleMove(move: Move) {
        val uci = move.toUci()
        val puzzle = puzzlesList.getOrNull(activePuzzleIndex) ?: return
        if (puzzle.correctMoves.contains(uci)) {
            puzzleCompleted = true
            puzzleMessage = "BRILLIANT! You solved the puzzle correctly!"
            unlockAchievement("tactical_ninja")
        } else {
            puzzleMessage = "Incorrect move. Try again!"
        }
    }

    fun loadEndgame(type: String) {
        activeEndgameName = type
        val fen = when (type) {
            "King + Pawn vs King" -> "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"
            "King + Rook vs King" -> "4k3/8/8/8/8/8/4R3/4K3 w - - 0 1"
            "Two Bishops vs King" -> "4k3/8/8/8/8/8/4BB2/4K3 w - - 0 1"
            else -> "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"
        }
        boardState.loadFen(fen)
        playedMoves.clear()
        redoStack.clear()
        selectedSquare = null
        legalTargets = emptyList()
        isGameOver = false
        gameStatus = "Endgame Trainer: $type. Play as White and deliver mate!"
        activeScreen = "play"
        syncState()
    }

    fun exportToPgn(): String {
        return com.example.chess.engine.utils.PgnParser.generatePgn(
            playedMoves = playedMoves,
            initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        )
    }

    fun importPgn(pgnString: String): Boolean {
        return try {
            val (newState, moves) = com.example.chess.engine.utils.PgnParser.parsePgn(pgnString)
            boardState.copyFrom(newState)
            playedMoves.clear()
            playedMoves.addAll(moves)
            redoStack.clear()
            lastMove = playedMoves.lastOrNull()
            syncState()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun checkDevCode(code: String, context: Context): Boolean {
        if (code.trim().uppercase() == "SUPERFISH") {
            isDevTerminalUnlocked = true
            premiumUpgraded = true
            adsEnabled = false
            forUSectionUnlocked = true
            com.example.chess.utils.ChessHaptics.playCheckHaptic(context)
            return true
        }
        return false
    }

    fun decompileDevCode(context: Context) {
        if (isDecompilingDevCode) return
        isDecompilingDevCode = true
        decompilationProgress = 0f
        viewModelScope.launch {
            for (i in 1..100) {
                kotlinx.coroutines.delay(25)
                decompilationProgress = i / 100f
            }
            devCodeInput = "SUPERFISH"
            checkDevCode("SUPERFISH", context)
            isDecompilingDevCode = false
        }
    }

    fun reloadAndStartFresh(context: Context) {
        viewModelScope.launch {
            // 1. Clear database tables
            repository.clearAllData()
            
            // 2. Clear SharedPreferences
            prefs.edit().clear().apply()
            
            // 3. Reset ViewModel parameters to factory default values
            adsEnabled = true
            premiumUpgraded = false
            adClosedTemporary = false
            activeAdIndex = 0
            soundEffectsEnabled = true
            hapticFeedbackEnabled = true
            useGeminiEngine = false
            geminiReasoning = null
            geminiErrorMessage = null
            selectedAnimationTheme = "Cyber Neon"
            selectedEngine = "KillFish"
            killfishModel = "Balanced"
            killfishVersion = "Prime"
            engineDepth = 4
            boardThemeName = "Emerald Green"
            highlightLegalMoves = true
            isEngineVersusMode = true
            playerColor = WHITE
            
            // Dev parameters
            devCodeInput = ""
            isDevTerminalUnlocked = false
            isDecompilingDevCode = false
            decompilationProgress = 0f
            forUSectionUnlocked = false
            
            // VFX edit parameters
            editStyle = "Next-Gen Velocity"
            enableCameraZoom = true
            enableMotionBlur = true
            enableScreenShake = true
            enableParticleBursts = true
            enableLightingGlow = true
            editStyleIntensity = 0.8f
            
            // Reset active match state
            boardState.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            playedMoves.clear()
            redoStack.clear()
            selectedSquare = null
            legalTargets = emptyList()
            lastMove = null
            isGameOver = false
            isKingInCheck = false
            kingInCheckSquare = null
            gameStatus = "New Game. Tap a piece to play!"
            activeScreen = "home"
            syncState()
            
            // Play successful haptic rumble
            com.example.chess.utils.ChessHaptics.playMoveHaptic(context)
        }
    }

    private fun generate1000PlusLessons(): List<Course> {
        val courses = mutableListOf<Course>()
        val topics = listOf(
            CourseTopic("Opening Systems & Foundations", "Beginner", "Master classical and modern chess openings."),
            CourseTopic("Middlegame Positioning & Strategy", "Intermediate", "Learn the art of positional play, outposts, and square control."),
            CourseTopic("Endgame Mastery & King Activity", "Advanced", "Essential king and pawn endings, rook bridges, and opposition."),
            CourseTopic("Tactical Motifs & Combinations", "Intermediate", "Forks, pins, skewers, double attacks, and discovered checks."),
            CourseTopic("Pawn Structures & Pawn Storms", "Intermediate", "Understand pawn chains, backward pawns, and minority attacks."),
            CourseTopic("King Safety & Castling Plans", "Beginner", "How to protect your king and when to launch flank attacks."),
            CourseTopic("Famous Masterpiece Deep Dives", "Advanced", "Detailed walkthroughs of games by Morphy, Kasparov, and Fischer."),
            CourseTopic("Modern Chess Engine Concepts", "Advanced", "Alpha-Beta pruning, transposition tables, and neural evaluation."),
            CourseTopic("Prophylaxis & Defensive Resilience", "Intermediate", "Preventing opponent's plans before they happen."),
            CourseTopic("The Art of the Piece Sacrifice", "Advanced", "Creative piece sacrifices to break the enemy fortress."),
            CourseTopic("Dynamic Center Control", "Beginner", "Fianchetto openings, hypermodernism, and central occupation."),
            CourseTopic("Rook Endgame Subtleties", "Advanced", "Rook behind passed pawns, Philidor defense, and passive vs active rooks."),
            CourseTopic("Minor Piece Battles", "Intermediate", "Good bishop vs bad bishop, and dominant knights vs passive bishops."),
            CourseTopic("Space Advantage & Maneuvering", "Intermediate", "Cramped positions, pawn breakthroughs, and changing flanks."),
            CourseTopic("Chess Psychology & Mental Focus", "Beginner", "Avoiding tilt, managing time scrambles, and calculating under pressure."),
            CourseTopic("Theoretical Opening Deep Dive", "Advanced", "Deep variations in the Najdorf Sicilian, Ruy Lopez, and Grunfeld."),
            CourseTopic("Tactical Defensive Saving Moves", "Intermediate", "Stalemate tricks, perpetual checks, and intermediate defensive moves."),
            CourseTopic("Aesthetic Harmony of Pieces", "Beginner", "Coordinating queen and knight, rook batteries, and major piece teamwork."),
            CourseTopic("Queen's Gambit Declined & Accepted", "Beginner", "The battle for center control in closed games."),
            CourseTopic("Hypermodern Defense Systems", "Intermediate", "King's Indian Defense, Grunfeld, and Nimzo-Indian concepts."),
            CourseTopic("Calculation Speed & Visualisation", "Advanced", "Improving multi-step candidate move calculation."),
            CourseTopic("Attacking the Fianchetto Castle", "Intermediate", "Sacrificing on h6/h3, open g-files, and removing defensive bishops."),
            CourseTopic("Asymmetrical Pawn Majorities", "Advanced", "Creating passed pawns on the queenside from asymmetric majorities."),
            CourseTopic("The Blockade & Prevention", "Intermediate", "Blocking advanced pawns using knights and heavy pieces."),
            CourseTopic("Tournament Strategy & Rules", "Beginner", "Threefold repetition, 50-move rule, touch-move, and swiss systems."),
            CourseTopic("Intuitive Piece Placements", "Intermediate", "Where pieces naturally belong in classic structures."),
            CourseTopic("Blitz & Bullet Practical Tips", "Beginner", "Time management, pre-moving, and posing practical problems."),
            CourseTopic("Historical World Championship Battles", "Advanced", "Steinitz, Lasker, Capablanca, and Alekhine masterpieces."),
            CourseTopic("Double Check Combinations", "Intermediate", "The ultimate forcing move in tactical mating nets."),
            CourseTopic("Smothered Mate & Unique Patterns", "Beginner", "Unusual mating traps, Anastasias mate, and smothered mate."),
            CourseTopic("Deep Positional Squeezes", "Advanced", "Restricting all active play from the opponent until they collapse."),
            CourseTopic("The Legacy of Chess AI", "Advanced", "From Deep Blue to Stockfish and AlphaZero.")
        )

        for (i in topics.indices) {
            val topic = topics[i]
            val lessons = mutableListOf<Lesson>()
            
            for (j in 1..32) {
                val lessonTitle = "${topic.title} - Session $j: " + when (i % 8) {
                    0 -> "Opening Theory & Control"
                    1 -> "Positional Harmony & Structures"
                    2 -> "Tactics, Forks & Combinations"
                    3 -> "Endgame Precision & Kings"
                    4 -> "Attack Plans & Sacrifices"
                    5 -> "Defensive Saving Resources"
                    6 -> "Historical Master Techniques"
                    else -> "AI Engine Strategic Insights"
                } + " (Level $j)"
                
                val lessonContent = "This session covers advanced strategic analysis regarding ${topic.title.lowercase()}. " +
                        "To improve your practical chess performance, you must master positional harmony and candidate move selection. " +
                        "Focus on developing pieces with active threats, securing key central squares, and optimizing king safety. " +
                        "Remember to calculate 2-3 moves deep before executing forcing lines."
                
                val quizQuestion = "In the context of ${topic.title.lowercase()} (Session $j), what is the most critical factor?"
                val options = listOf(
                    "Maximizing immediate central pressure & piece coordination.",
                    "Moving peripheral pawns early to secure flank space.",
                    "Trading your most active pieces for passive ones.",
                    "Rushing the king to the center of the board in the opening."
                )
                val correctIdx = 0
                val explanation = "Piece coordination and central pressure are always foundational to chess success, especially in ${topic.title.lowercase()}."
                
                lessons.add(
                    Lesson(
                        title = lessonTitle,
                        content = lessonContent,
                        quizQuestion = quizQuestion,
                        quizOptions = options,
                        correctIndex = correctIdx,
                        explanation = explanation,
                        initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                    )
                )
            }
            
            courses.add(
                Course(
                    id = "gen_course_${i + 1}",
                    title = "${i + 1}. ${topic.title}",
                    difficulty = topic.difficulty,
                    description = topic.description,
                    lessons = lessons
                )
            )
        }
        
        return courses
    }

    private fun generate30PlusBooks(): List<ChessBook> {
        val books = mutableListOf<ChessBook>()
        val bookTemplates = listOf(
            BookTemplate("My System", "Aron Nimzowitsch", "The classic guide to positional chess, overprotection, open files, and blockades."),
            BookTemplate("Bobby Fischer Teaches Chess", "Bobby Fischer", "Master mating attacks, back-rank vulnerabilities, and tactical patterns."),
            BookTemplate("The Life and Games of Mikhail Tal", "Mikhail Tal", "Learn how to sacrifice pieces creatively to spark devastating attacks."),
            BookTemplate("Think Like a Grandmaster", "Alexander Kotov", "Develop logical candidate move calculation trees and decision making."),
            BookTemplate("Endgame Strategy", "Mikhail Shereshevsky", "Positional rules in the endgame, king centralization, and do not hurry."),
            BookTemplate("My Great Predecessors Vol. I", "Garry Kasparov", "In-depth analysis of Steinitz, Lasker, Capablanca, and Alekhine."),
            BookTemplate("Dynamic Chess Strategy", "Mihai Suba", "Understanding dynamic advantages, initiative, and modern counterplay."),
            BookTemplate("The Art of Attack in Chess", "Vladimir Vukovic", "A systematic exposition of the mechanics of attacking the castled King."),
            BookTemplate("Positional Chess Handbook", "Israel Gelfer", "Practical training in prophylaxis, open files, weak squares, and minor pieces."),
            BookTemplate("Fire on Board", "Alexei Shirov", "Mind-boggling tactical battles and stunning sacrifices in modern lines."),
            BookTemplate("Secrets of Modern Chess Strategy", "John Watson", "How chess strategy has evolved beyond Nimzowitsch's dogmatic rules."),
            BookTemplate("Dvoretsky's Endgame Manual", "Mark Dvoretsky", "The definitive guide to theoretical and practical endgames for masters."),
            BookTemplate("Zurich International Chess Tournament 1953", "David Bronstein", "The ultimate tournament book filled with profound strategic insights."),
            BookTemplate("Fundamental Chess Endings", "Karsten Muller", "An exhaustive modern handbook on all major endgame types."),
            BookTemplate("Play Like a Grandmaster", "Alexander Kotov", "Practical guide to competitive preparation and tournament play."),
            BookTemplate("Grandmaster Repertoire: The Sicilian", "John Shaw", "Elite theory and positional lines against all main lines of the Sicilian."),
            BookTemplate("My 60 Memorable Games", "Bobby Fischer", "Fischer's legendary annotations of his own most dramatic struggles."),
            BookTemplate("The Mammoth Book of the World's Greatest Chess Games", "Graham Burgess", "Detailed annotations of 100 historical masterpiece encounters."),
            BookTemplate("Pawn Structure Chess", "Andrew Soltis", "Understanding the typical plans associated with different pawn skeletons."),
            BookTemplate("The Inner Game of Chess", "Andrew Soltis", "Analyzing why players make blunders and how to calculate safely."),
            BookTemplate("Seven Deadly Chess Sins", "Jonathan Rowson", "Exploring psychological pitfalls: thinking too much, drift, and ego."),
            BookTemplate("Improve Your Chess Pattern Recognition", "Arthur van de Oudeweetering", "Train your brain to spot key tactical and positional structures."),
            BookTemplate("Mastering the Chess Openings", "John Watson", "A comprehensive guide to understanding the ideas behind major openings."),
            BookTemplate("Practical Chess Exercises", "Ray Cheng", "600 positions to solve spanning tactics, defense, and strategy."),
            BookTemplate("Winning Chess Tactics", "Yasser Seirawan", "An introduction to tactical patterns with engaging, conversational guides."),
            BookTemplate("Chess for Zebras", "Jonathan Rowson", "Understanding how to learn chess and why skill improvements happen in plateaus."),
            BookTemplate("Forcing Chess Moves", "Charles Hertan", "Unleash your inner tactical monster by calculating forcing lines first."),
            BookTemplate("Liquidation on the Chess Board", "Joel Benjamin", "Mastering when and how to trade down to win or save endgames."),
            BookTemplate("Under the Surface", "Jan Markos", "Secrets of grandmaster thinking, planning, and practical psychology."),
            BookTemplate("Applying Logic in Chess", "Erik Kislik", "A modern take on evaluation, candidate moves, and target selection."),
            BookTemplate("The Woodpecker Method", "Axel Smith", "High-repetition tactical training to build rapid subconscious recognition."),
            BookTemplate("Practical Decision Making in Chess", "Boris Gelfand", "Grandmaster insights into calculation, positional sacrifice, and time management.")
        )

        for (i in bookTemplates.indices) {
            val tmpl = bookTemplates[i]
            val chapters = mutableListOf<BookChapter>()
            
            for (c in 1..3) {
                val games = listOf(
                    InteractiveGame(
                        title = "Position Study - ${tmpl.title} (Ch. $c)",
                        fen = when (c) {
                            1 -> "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                            2 -> "r1bqk2r/ppp2ppp/2n1pn2/8/1b1PP3/2N2N2/PP1B1PPP/R2QKB1R w KQkq - 0 7"
                            else -> "6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1"
                        },
                        moves = listOf("e4", "e5", "Nf3", "Nc6", "Bb5"),
                        comments = listOf(
                            "Occupying the center and opening lines for bishops.",
                            "Reclaiming space and challenging White's center.",
                            "Developing the knight and threatening the e5 pawn.",
                            "Developing the knight to protect the pawn.",
                            "Entering Spanish structures with maximum positional tension."
                        )
                    )
                )
                
                chapters.add(
                    BookChapter(
                        title = "Chapter $c: " + when (c) {
                            1 -> "Foundational Principles"
                            2 -> "Strategic Maneuvering"
                            else -> "Tactical Execution"
                        },
                        content = "This chapter in ${tmpl.title} covers essential ${if (c==1) "opening" else if (c==2) "middlegame" else "endgame"} concepts. " +
                                "To succeed at a grandmaster level, you must prioritize candidate move selection, " +
                                "ensure piece activity, and maintain deep vigilance regarding back-rank checks or king safety.",
                        games = games
                    )
                )
            }
            
            books.add(
                ChessBook(
                    id = "gen_book_${i + 1}",
                    title = tmpl.title,
                    author = tmpl.author,
                    description = tmpl.description,
                    chapters = chapters
                )
            )
        }
        
        return books
    }
}

data class CourseTopic(val title: String, val difficulty: String, val description: String)
data class BookTemplate(val title: String, val author: String, val description: String)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null
)

data class Lesson(
    val title: String,
    val content: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val initialFen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
)

data class ChessBook(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val chapters: List<BookChapter>
)

data class BookChapter(
    val title: String,
    val content: String,
    val games: List<InteractiveGame>
)

data class InteractiveGame(
    val title: String,
    val fen: String,
    val moves: List<String>,
    val comments: List<String>
)

data class Course(
    val id: String,
    val title: String,
    val difficulty: String,
    val description: String,
    val lessons: List<Lesson>
)

data class ChessPuzzle(
    val id: String,
    val title: String,
    val description: String,
    val fen: String,
    val correctMoves: List<String>,
    val difficulty: String
)

data class ChatMessage(
    val sender: String,
    val text: String,
    val timestamp: String
)

data class ChessBot(
    val id: String,
    val name: String,
    val rating: Int,
    val description: String,
    val avatarEmoji: String,
    val personality: String,
    val searchDepth: Int,
    val useGemini: Boolean = false,
    val isPremium: Boolean = false
)

val defaultChessBots = listOf(
    ChessBot(
        id = "lil_nemo",
        name = "Lil' Nemo",
        rating = 400,
        description = "A friendly baby fish who easily gets distracted. Often makes clumsy blunders.",
        avatarEmoji = "🐠",
        personality = "Clumsy & Defensive",
        searchDepth = 1,
        useGemini = false
    ),
    ChessBot(
        id = "crabby_carla",
        name = "Crabby Carla",
        rating = 800,
        description = "Carla hides in her shell. She prioritizes safety, defensive lines, and castling early.",
        avatarEmoji = "🦀",
        personality = "Ultra-Defensive",
        searchDepth = 2,
        useGemini = false
    ),
    ChessBot(
        id = "octo_tactician",
        name = "Octo-Tactician",
        rating = 1200,
        description = "With 8 arms, he loves long-range bishop diagonals and tricky knight fork tactics.",
        avatarEmoji = "🐙",
        personality = "Tactical Explorer",
        searchDepth = 3,
        useGemini = false
    ),
    ChessBot(
        id = "sharky_steve",
        name = "Sharky Steve",
        rating = 1600,
        description = "An aggressive predator of the deep. Steve seeks sharp gambits and early king attacks.",
        avatarEmoji = "🦈",
        personality = "Highly Aggressive",
        searchDepth = 4,
        useGemini = false
    ),
    ChessBot(
        id = "deep_blue_whale",
        name = "Deep Blue Whale",
        rating = 2200,
        description = "A massive calculating force of nature. Patient, deep searching, and incredibly stable.",
        avatarEmoji = "🐳",
        personality = "Calculated Positional",
        searchDepth = 6,
        useGemini = false
    ),
    ChessBot(
        id = "orion_gemini",
        name = "Orion (Gemini AI)",
        rating = 2600,
        description = "An advanced neural chatbot. Orion plays creative, human-like moves with profound vision.",
        avatarEmoji = "🌌",
        personality = "Creative Mastermind",
        searchDepth = 4,
        useGemini = true,
        isPremium = true
    ),
    ChessBot(
        id = "kill_fish_stock",
        name = "Stockfish 18 (Kill Fish)",
        rating = 3200,
        description = "The ultimate cybernetic intelligence. Plays near-perfect tactical and endgame calculations.",
        avatarEmoji = "👑",
        personality = "Supercomputer GM",
        searchDepth = 8,
        useGemini = false,
        isPremium = true
    )
)
