package com.example.chess.ui

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.db.TrapHistoryEntity
import com.example.chess.utils.GeminiChessEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessTrapScreen(viewModel: ChessViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = ThemeColorsRegistry.getColors(viewModel.selectedAnimationTheme)
    
    var fenInput by remember { mutableStateOf("r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3") } // Prussian Defense
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<GeminiChessEngine.TrapAnalysisResult?>(null) }
    var statusMessage by remember { mutableStateOf("Ready to scan position") }

    val historyList by viewModel.trapHistoryList.collectAsState(initial = emptyList())

    // Templates to allow instant testing with high-fidelity chessboard scenarios
    val sampleTemplates = listOf(
        ChessTemplate("Legal's Trap", "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3", "Pins, deflection, and smothered sacrifices."),
        ChessTemplate("Noah's Ark", "r1bqk1nr/1ppp1ppp/p1n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 0 4", "Trapped bishop tactics in the Ruy Lopez."),
        ChessTemplate("Fishing Pole", "r1bqk2r/pppp1ppp/2n2n2/4p1B1/4P3/2NP4/PPP2PPP/R2QKBNR b KQkq - 2 4", "Dangerous kingside counterattacks.")
    )

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    selectedImageBitmap = bitmap
                    
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    selectedImageBytes = outputStream.toByteArray()
                    statusMessage = "Image loaded successfully"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Button Navigation Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.activeScreen = "home" },
                    modifier = Modifier.testTag("trap_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CHESS TRAP ANALYZER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.primary
                    )
                    Text(
                        text = "Identify motifs, opening traps, and tactical threats",
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // FEN Entry & Screenshot Selection Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Input Chess State",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    // Text Field for FEN Notation
                    OutlinedTextField(
                        value = fenInput,
                        onValueChange = { fenInput = it },
                        label = { Text("Forsyth-Edwards Notation (FEN)") },
                        placeholder = { Text("e.g. rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurface.copy(alpha = 0.2f),
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trap_fen_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Or Upload Screenshot Board",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface.copy(alpha = 0.8f)
                        )

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.15f), contentColor = colors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("upload_screenshot_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload PNG", fontSize = 12.sp)
                        }
                    }

                    // Selected screenshot preview if available
                    selectedImageBitmap?.let { bitmap ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Board Screenshot Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxHeight()
                            )
                            IconButton(
                                onClick = {
                                    selectedImageBitmap = null
                                    selectedImageUri = null
                                    selectedImageBytes = null
                                    statusMessage = "Image cleared"
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Image", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Launch button for position scan
                    Button(
                        onClick = {
                            isAnalyzing = true
                            statusMessage = "Analyzing position using Gemini 3.5 Flash..."
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = GeminiChessEngine.analyzeChessPosition(
                                    fen = fenInput.ifBlank { null },
                                    imageBytes = selectedImageBytes
                                )
                                withContext(Dispatchers.Main) {
                                    isAnalyzing = false
                                    analysisResult = result
                                    if (result.success) {
                                        statusMessage = "Analysis completed!"
                                        // Save to history
                                        viewModel.saveTrapToHistory(
                                            fen = fenInput,
                                            trapName = result.trapName,
                                            description = result.explanation,
                                            motif = result.motif,
                                            dangerousMoves = result.dangerousMoves,
                                            continuation = result.continuation,
                                            evaluation = result.evaluation
                                        )
                                    } else {
                                        statusMessage = "Analysis failed: ${result.errorMessage}"
                                    }
                                }
                            }
                        },
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("recognize_position_btn")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Board State...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.QueryStats, contentDescription = "Analyze")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RECOGNIZE POSITION & TRAPS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Template Quick Pick Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK TEST SCENARIOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    sampleTemplates.forEach { template ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, if (fenInput == template.fen) colors.primary else colors.onSurface.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(220.dp)
                                .clickable {
                                    fenInput = template.fen
                                    selectedImageBitmap = null
                                    selectedImageUri = null
                                    selectedImageBytes = null
                                    statusMessage = "Loaded ${template.name} FEN template"
                                }
                                .testTag("template_card_${template.name.replace(" ", "_")}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = template.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = template.description, fontSize = 11.sp, color = colors.onSurface.copy(alpha = 0.6f), lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Status Information Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAnalyzing) Icons.Default.Sync else Icons.Default.Info,
                        contentDescription = "Status",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Trap Analysis Result Display
        analysisResult?.let { result ->
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically() + fadeIn()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trap_analysis_result_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Header with Name & Evaluation score
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.trapName.uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.primary
                                    )
                                    Text(
                                        text = "Core Motif: ${result.motif}",
                                        fontSize = 12.sp,
                                        color = colors.onSurface.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                // Evaluation Badge
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (result.evaluation.contains("+")) Color(0xFF10B981) else Color(0xFFEF4444)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = result.evaluation,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Divider(color = colors.onSurface.copy(alpha = 0.1f))

                            // Danger Warning Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Caution", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Column {
                                    Text("DANGEROUS MOVES TO AVOID:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                    Text(text = result.dangerousMoves, fontSize = 13.sp, color = colors.onSurface, lineHeight = 18.sp)
                                }
                            }

                            // Continuation Line
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("OPTIMAL CONTINUATION SEQUENCE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colors.background),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = result.continuation,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = colors.primary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            // Full Explanatory Report
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("DETAILED AI TACTICAL REPORT:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onSurface.copy(alpha = 0.6f))
                                Text(
                                    text = result.explanation,
                                    fontSize = 13.sp,
                                    color = colors.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                )
                            }

                            // Action buttons: Copy FEN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Chess FEN", fenInput)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "FEN notation copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_fen_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy FEN")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trap History Log Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCANNING HISTORY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 1.sp
                )
                
                if (historyList.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearAllTrapHistory() },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear History", fontSize = 11.sp)
                    }
                }
            }
        }

        // History list items
        if (historyList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty History",
                            tint = colors.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trap history saved yet. Scanned boards are automatically saved here for offline review.",
                            fontSize = 12.sp,
                            color = colors.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(historyList) { historyItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.05f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            fenInput = historyItem.fen
                            analysisResult = GeminiChessEngine.TrapAnalysisResult(
                                trapName = historyItem.trapName,
                                motif = historyItem.motif,
                                dangerousMoves = historyItem.dangerousMoves,
                                continuation = historyItem.continuation,
                                evaluation = historyItem.evaluation,
                                explanation = historyItem.description,
                                success = true
                            )
                            statusMessage = "Loaded scan from history record"
                        }
                        .testTag("history_record_item_${historyItem.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = historyItem.trapName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "FEN: ${historyItem.fen.take(24)}...",
                                fontSize = 11.sp,
                                color = colors.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        
                        // Evaluation badge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (historyItem.evaluation.contains("+")) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                contentColor = if (historyItem.evaluation.contains("+")) Color(0xFF10B981) else Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = historyItem.evaluation,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ChessTemplate(
    val name: String,
    val fen: String,
    val description: String
)
