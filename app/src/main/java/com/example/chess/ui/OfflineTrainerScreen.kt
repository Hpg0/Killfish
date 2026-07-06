package com.example.chess.ui

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.random.Random

// Data structures for Offline Training Questions
data class VisionQuestion(
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class NotationQuestion(
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class BlunderScenario(
    val title: String,
    val fen: String,
    val candidateMove: String,
    val correctClassification: String, // "Blunder", "Mistake", "Excellent"
    val description: String,
    val explanation: String
)

@Composable
fun OfflineTrainerScreen(viewModel: ChessViewModel) {
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    var activeTab by remember { mutableStateOf(0) } // 0 = Vision, 1 = Notation, 2 = Blunders
    
    // Global Score tracking
    var correctCount by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(0) }
    
    // Interactive feedback states
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    
    // Active training questions
    var currentVisionQuestion by remember { mutableStateOf(generateVisionQuestion()) }
    var currentNotationQuestion by remember { mutableStateOf(generateNotationQuestion()) }
    
    // Blunder Scenarios pool
    val blunderScenarios = remember { getBlunderScenarios() }
    var currentBlunderIndex by remember { mutableStateOf(0) }
    val currentBlunder = blunderScenarios[currentBlunderIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        item {
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
                        text = "OFFLINE COGNITION TRAINER",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = colors.primary
                    )
                    Text(
                        text = "Everything Randomized • Works 100% Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Pill Tabs Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Vision", "Notation", "Blunders")
                tabs.forEachIndexed { index, title ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) colors.primary else Color.Transparent)
                            .clickable {
                                activeTab = index
                                // Reset selection states
                                selectedAnswer = null
                                isAnswerCorrect = null
                                // Regenerate question
                                if (index == 0) currentVisionQuestion = generateVisionQuestion()
                                if (index == 1) currentNotationQuestion = generateNotationQuestion()
                                if (index == 2) currentBlunderIndex = Random.nextInt(blunderScenarios.size)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.Black else colors.onSurface
                        )
                    }
                }
            }
        }

        // Score HUD Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "TRAINING SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = colors.primary
                            )
                            Text(
                                text = "$correctCount / $totalCount Solved",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.onSurface
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            correctCount = 0
                            totalCount = 0
                            selectedAnswer = null
                            isAnswerCorrect = null
                        }
                    ) {
                        Text("Reset", color = colors.primary)
                    }
                }
            }
        }

        // Tab Content Display
        item {
            when (activeTab) {
                0 -> VisionTrainerTab(
                    question = currentVisionQuestion,
                    selectedAnswer = selectedAnswer,
                    isAnswerCorrect = isAnswerCorrect,
                    onAnswerSelected = { ans ->
                        selectedAnswer = ans
                        val correct = ans == currentVisionQuestion.correctAnswer
                        isAnswerCorrect = correct
                        totalCount++
                        if (correct) correctCount++
                    },
                    onNextQuestion = {
                        selectedAnswer = null
                        isAnswerCorrect = null
                        currentVisionQuestion = generateVisionQuestion()
                    },
                    colors = colors
                )
                1 -> NotationTrainerTab(
                    question = currentNotationQuestion,
                    selectedAnswer = selectedAnswer,
                    isAnswerCorrect = isAnswerCorrect,
                    onAnswerSelected = { ans ->
                        selectedAnswer = ans
                        val correct = ans == currentNotationQuestion.correctAnswer
                        isAnswerCorrect = correct
                        totalCount++
                        if (correct) correctCount++
                    },
                    onNextQuestion = {
                        selectedAnswer = null
                        isAnswerCorrect = null
                        currentNotationQuestion = generateNotationQuestion()
                    },
                    colors = colors
                )
                2 -> BlunderFinderTab(
                    scenario = currentBlunder,
                    selectedAnswer = selectedAnswer,
                    isAnswerCorrect = isAnswerCorrect,
                    onAnswerSelected = { ans ->
                        selectedAnswer = ans
                        val correct = ans == currentBlunder.correctClassification
                        isAnswerCorrect = correct
                        totalCount++
                        if (correct) correctCount++
                    },
                    onNextQuestion = {
                        selectedAnswer = null
                        isAnswerCorrect = null
                        var nextIdx = currentBlunderIndex
                        while (nextIdx == currentBlunderIndex) {
                            nextIdx = Random.nextInt(blunderScenarios.size)
                        }
                        currentBlunderIndex = nextIdx
                    },
                    colors = colors
                )
            }
        }
    }
}

// ==========================================
// VISION TRAINING TAB
// ==========================================
@Composable
fun VisionTrainerTab(
    question: VisionQuestion,
    selectedAnswer: String?,
    isAnswerCorrect: Boolean?,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    colors: ThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CHESS BOARD VISION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }

            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                color = colors.onSurface
            )

            // Dynamic grid list of option buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val buttonColor = if (isSelected) {
                        if (isAnswerCorrect == true) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    } else {
                        colors.background
                    }

                    val contentColor = if (isSelected) Color.White else colors.onSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(buttonColor)
                            .border(
                                BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) (if (isAnswerCorrect == true) Color.Green else Color.Red) else colors.border
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = selectedAnswer == null) {
                                onAnswerSelected(option)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )
                    }
                }
            }

            // Correct/Incorrect message banner & explanation
            if (selectedAnswer != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAnswerCorrect == true) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFFB71C1C).copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, if (isAnswerCorrect == true) Color.Green else Color.Red)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAnswerCorrect == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = if (isAnswerCorrect == true) "Correct" else "Incorrect",
                                tint = if (isAnswerCorrect == true) Color.Green else Color.Red
                            )
                            Text(
                                text = if (isAnswerCorrect == true) "EXCELLENT WORK!" else "INCORRECT ANSWER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isAnswerCorrect == true) Color.Green else Color.Red
                            )
                        }
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                Button(
                    onClick = { onNextQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Next Question", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

// ==========================================
// CHESS NOTATION TAB
// ==========================================
@Composable
fun NotationTrainerTab(
    question: NotationQuestion,
    selectedAnswer: String?,
    isAnswerCorrect: Boolean?,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    colors: ThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ALGEBRAIC NOTATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }

            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                color = colors.onSurface
            )

            // Dynamic grid list of option buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                question.options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val buttonColor = if (isSelected) {
                        if (isAnswerCorrect == true) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    } else {
                        colors.background
                    }

                    val contentColor = if (isSelected) Color.White else colors.onSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(buttonColor)
                            .border(
                                BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) (if (isAnswerCorrect == true) Color.Green else Color.Red) else colors.border
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = selectedAnswer == null) {
                                onAnswerSelected(option)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                            color = contentColor
                        )
                    }
                }
            }

            // Correct/Incorrect message banner & explanation
            if (selectedAnswer != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAnswerCorrect == true) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFFB71C1C).copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, if (isAnswerCorrect == true) Color.Green else Color.Red)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAnswerCorrect == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = if (isAnswerCorrect == true) "Correct" else "Incorrect",
                                tint = if (isAnswerCorrect == true) Color.Green else Color.Red
                            )
                            Text(
                                text = if (isAnswerCorrect == true) "NOTATION MASTERED!" else "INCORRECT NOTATION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isAnswerCorrect == true) Color.Green else Color.Red
                            )
                        }
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                Button(
                    onClick = { onNextQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Next Question", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

// ==========================================
// BLUNDER FINDER TAB
// ==========================================
@Composable
fun BlunderFinderTab(
    scenario: BlunderScenario,
    selectedAnswer: String?,
    isAnswerCorrect: Boolean?,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    colors: ThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "BLUNDER DETECTION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = colors.primary
                )
            }

            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )

            // Embedded Mini Chess Board!
            MiniChessboardView(fen = scenario.fen, colors = colors)

            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Proposed Move: ${scenario.candidateMove}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = colors.primary,
                textAlign = TextAlign.Center
            )

            Divider(color = colors.border.copy(alpha = 0.5f))

            // Classifications list
            val classifications = listOf("Blunder", "Mistake", "Excellent")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                classifications.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val buttonColor = if (isSelected) {
                        if (isAnswerCorrect == true) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    } else {
                        colors.background
                    }
                    val contentColor = if (isSelected) Color.White else colors.onSurface

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(buttonColor)
                            .border(
                                BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) (if (isAnswerCorrect == true) Color.Green else Color.Red) else colors.border
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = selectedAnswer == null) {
                                onAnswerSelected(option)
                            }
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = contentColor
                        )
                    }
                }
            }

            // Correct/Incorrect message banner & explanation
            if (selectedAnswer != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAnswerCorrect == true) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFFB71C1C).copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, if (isAnswerCorrect == true) Color.Green else Color.Red)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAnswerCorrect == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = if (isAnswerCorrect == true) "Correct" else "Incorrect",
                                tint = if (isAnswerCorrect == true) Color.Green else Color.Red
                            )
                            Text(
                                text = if (isAnswerCorrect == true) "TACTICAL ANALYSIS CORRECT!" else "TACTICAL ANALYSIS ERROR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (isAnswerCorrect == true) Color.Green else Color.Red
                            )
                        }
                        Text(
                            text = scenario.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                Button(
                    onClick = { onNextQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Next Tactical Scenario", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

// ==========================================
// RANDOM VISION QUESTION GENERATOR
// ==========================================
fun generateVisionQuestion(): VisionQuestion {
    val files = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    val ranks = listOf("1", "2", "3", "4", "5", "6", "7", "8")
    
    val randType = Random.nextInt(3)
    if (randType == 0) {
        // Color of square
        val randFileIdx = Random.nextInt(8)
        val randRankIdx = Random.nextInt(8)
        val file = files[randFileIdx]
        val rank = ranks[randRankIdx]
        val square = "$file$rank"
        
        val isDark = (randFileIdx + randRankIdx) % 2 == 0
        val correct = if (isDark) "Dark" else "Light"
        
        return VisionQuestion(
            prompt = "What is the background color of the square $square?",
            options = listOf("Light", "Dark"),
            correctAnswer = correct,
            explanation = "On a standard chess board, a1 is a dark square. By alternating colors along ranks and files, the square $square evaluates to a $correct square."
        )
    } else if (randType == 1) {
        // Knight jump
        val randFileIdx = Random.nextInt(6) + 1
        val randRankIdx = Random.nextInt(6) + 1
        val sq1 = "${files[randFileIdx]}${ranks[randRankIdx]}"
        
        val jumps = listOf(
            Pair(2, 1), Pair(2, -1), Pair(-2, 1), Pair(-2, -1),
            Pair(1, 2), Pair(1, -2), Pair(-1, 2), Pair(-1, -2)
        )
        
        val isKnightJump = Random.nextBoolean()
        val sq2 = if (isKnightJump) {
            val jump = jumps[Random.nextInt(jumps.size)]
            "${files[randFileIdx + jump.first]}${ranks[randRankIdx + jump.second]}"
        } else {
            // Pick a non-knight jump
            var f = randFileIdx + 1
            var r = randRankIdx + 1
            if (f > 7) f = 0
            if (r > 7) r = 0
            "${files[f]}${ranks[r]}"
        }
        
        val correct = if (isKnightJump) "Yes" else "No"
        return VisionQuestion(
            prompt = "Can a Knight jump from $sq1 to $sq2 in exactly one move?",
            options = listOf("Yes", "No"),
            correctAnswer = correct,
            explanation = "A Knight's movement is characterized by an 'L' shape: 2 squares in one direction and 1 square perpendicularly. From $sq1, the square $sq2 is ${if (isKnightJump) "" else "NOT "}a valid landing target."
        )
    } else {
        // Same diagonal
        val randFileIdx = Random.nextInt(8)
        val randRankIdx = Random.nextInt(8)
        val sq1 = "${files[randFileIdx]}${ranks[randRankIdx]}"
        
        val isOnSameDiagonal = Random.nextBoolean()
        val sq2 = if (isOnSameDiagonal) {
            val offset = listOf(-3, -2, -1, 1, 2, 3).filter { 
                (randFileIdx + it) in 0..7 && (randRankIdx + it) in 0..7 
            }.randomOrNull() ?: 1
            
            val f = (randFileIdx + offset).coerceIn(0, 7)
            val r = (randRankIdx + offset).coerceIn(0, 7)
            "${files[f]}${ranks[r]}"
        } else {
            var f = randFileIdx + 2
            var r = randRankIdx + 1
            if (f > 7) f = 0
            if (r > 7) r = 0
            "${files[f]}${ranks[r]}"
        }
        
        // Let's verify same diagonal math
        val f1 = files.indexOf(sq1.substring(0,1))
        val r1 = ranks.indexOf(sq1.substring(1,2))
        val f2 = files.indexOf(sq2.substring(0,1))
        val r2 = ranks.indexOf(sq2.substring(1,2))
        val verified = abs(f1 - f2) == abs(r1 - r2) && (sq1 != sq2)
        val correct = if (verified) "Yes" else "No"
        
        return VisionQuestion(
            prompt = "Do $sq1 and $sq2 share the same diagonal line on the chess board?",
            options = listOf("Yes", "No"),
            correctAnswer = correct,
            explanation = "Squares on the same diagonal share the same coordinate difference offset. From $sq1 to $sq2, they ${if (verified) "DO" else "DO NOT"} lie along the exact diagonal vector."
        )
    }
}

// ==========================================
// RANDOM NOTATION QUESTION GENERATOR
// ==========================================
fun generateNotationQuestion(): NotationQuestion {
    val types = listOf("Pawn Move", "Knight Move", "Bishop Capture", "Kingside Castle", "Promotion")
    val selectedType = types.random()
    
    return when (selectedType) {
        "Pawn Move" -> {
            val froms = listOf("e2", "d2", "c2", "f2")
            val selectedFrom = froms.random()
            val to = selectedFrom.replace("2", "4")
            val opt = to
            NotationQuestion(
                prompt = "A Pawn moves forward two squares from $selectedFrom to $to. What is the standard algebraic notation?",
                options = listOf(opt, "P$to", "${selectedFrom}-$to", "1.$to"),
                correctAnswer = opt,
                explanation = "Pawn moves do not use piece letters like P. Only the destination square name '$opt' is written."
            )
        }
        "Knight Move" -> {
            val tos = listOf("f3", "c3", "d2", "e2")
            val selectedTo = tos.random()
            val opt = "N$selectedTo"
            NotationQuestion(
                prompt = "A Knight jumps to the square $selectedTo. What is the standard algebraic notation?",
                options = listOf(opt, "K$selectedTo", "Kn$selectedTo", "n$selectedTo"),
                correctAnswer = opt,
                explanation = "Knight moves are designated by 'N'. 'K' is strictly reserved for the King to avoid notation ambiguity."
            )
        }
        "Bishop Capture" -> {
            val tos = listOf("d4", "e5", "f6", "c3")
            val selectedTo = tos.random()
            val opt = "Bxd4"
            NotationQuestion(
                prompt = "A Bishop captures an opposing piece on d4. What is the standard algebraic notation?",
                options = listOf(opt, "Bd4", "Bxd4", "Bxd4#", "B:d4").shuffled().distinct(),
                correctAnswer = "Bxd4",
                explanation = "Captures are always indicated by inserting a lower-case 'x' between the piece symbol 'B' and the destination square."
            )
        }
        "Kingside Castle" -> {
            NotationQuestion(
                prompt = "White castles on the kingside (short castle). What is the standard algebraic notation?",
                options = listOf("O-O", "O-O-O", "o-o-o", "K-R"),
                correctAnswer = "O-O",
                explanation = "Kingside castling is indicated by 'O-O' (two capital letter Os). Queenside (long) castling uses 'O-O-O'."
            )
        }
        else -> {
            val ranks = listOf("h8", "g8", "f8", "a8")
            val target = ranks.random()
            val opt = "${target}=Q"
            NotationQuestion(
                prompt = "A White pawn moves to $target and promotes to a Queen. What is the standard algebraic notation?",
                options = listOf(opt, "${target}Q", "${target}(Q)", "P${target}=Q"),
                correctAnswer = opt,
                explanation = "Promotion is annotated by the destination square, followed immediately by '=' and the desired piece letter 'Q'."
            )
        }
    }
}

// ==========================================
// PREDEFINED BLUNDER SCENARIOS (Everything randomized / offline)
// ==========================================
fun getBlunderScenarios(): List<BlunderScenario> {
    return listOf(
        BlunderScenario(
            title = "Fool's Mate Vulnerability",
            fen = "rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR w KQkq - 0 1",
            candidateMove = "1. h4",
            correctClassification = "Blunder",
            description = "White has pushed the f and g pawns early. If White plays a slow move like h4, Black has a lethal response.",
            explanation = "White plays 1. h4??, allowing Black to execute an immediate 1... Qh4# delivering mate in one square."
        ),
        BlunderScenario(
            title = "Back-Rank Checkmate Shield",
            fen = "3r2k1/ppp2ppp/8/8/8/8/PPP2PPP/3R2K1 w - - 0 1",
            candidateMove = "1. Rxd8",
            correctClassification = "Excellent",
            description = "The rook on d1 faces off against Black's rook on d8. Black's king is locked behind their pawns.",
            explanation = "1. Rxd8+ is Excellent. It forces a back-rank mate or massive material advantage as Black's king has no escape square."
        ),
        BlunderScenario(
            title = "Scholar's Mate Defense",
            fen = "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR b KQkq - 0 1",
            candidateMove = "1... Nf6",
            correctClassification = "Excellent",
            description = "White is aiming at the weak f7 square with both Queen and Bishop.",
            explanation = "1... Nf6 blocking the Queen's access to the f7 square is Excellent. It prevents immediate mate on f7."
        ),
        BlunderScenario(
            title = "Foolish King Outing",
            fen = "rnbqkbnr/pppp1ppp/8/4p3/8/5P2/PPPPP1PP/RNBQKBNR w KQkq - 0 1",
            candidateMove = "1. Kf2",
            correctClassification = "Blunder",
            description = "The king steps out to f2 voluntarily in the opening phase.",
            explanation = "1. Kf2?? is a Blunder. It breaks castling rights, exposes the King to severe tactical threats, and serves no strategic purpose."
        ),
        BlunderScenario(
            title = "Back-Rank Negligence",
            fen = "6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1",
            candidateMove = "1. Rd8",
            correctClassification = "Excellent",
            description = "The White rook can swoop down to the 8th rank. Black has no pieces defending the d8 square.",
            explanation = "1. Rd8# is Excellent. It instantly ends the game with a back-rank mate because Black's pawns block their own king."
        ),
        BlunderScenario(
            title = "Premature Queen Attack",
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            candidateMove = "1. Qh5",
            correctClassification = "Mistake",
            description = "White brings their Queen out to h5 on the very first move.",
            explanation = "1. Qh5 is a Mistake. It is playable but strategically bad as Black can easily gain development tempos attacking the exposed Queen."
        )
    )
}
