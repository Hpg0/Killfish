package com.example.chess.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.engine.EngineConstants
import com.example.chess.engine.moves.Move

// Move Particle data helper
data class MoveParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val gravity: Float
)

// Board Theme Configuration Class
data class BoardTheme(
    val lightSquare: Color,
    val darkSquare: Color,
    val selectedHighlight: Color,
    val lastMoveHighlight: Color,
    val legalMoveDot: Color,
    val kingCheckColor: Color = Color(0xFFD32F2F)
)

val BOARD_THEMES = mapOf(
    "Professional Polish" to BoardTheme(
        lightSquare = Color(0xFFE6E1E5),
        darkSquare = Color(0xFF4A4458),
        selectedHighlight = Color(0x99D0BCFF),
        lastMoveHighlight = Color(0x66D0BCFF),
        legalMoveDot = Color(0x88D0BCFF),
        kingCheckColor = Color(0xFFF2B8B5)
    ),
    "Emerald Green" to BoardTheme(
        lightSquare = Color(0xFFECECD7),
        darkSquare = Color(0xFF739552),
        selectedHighlight = Color(0x99F7F785),
        lastMoveHighlight = Color(0x77BACA44),
        legalMoveDot = Color(0x66111111)
    ),
    "Classic Wood Slate" to BoardTheme(
        lightSquare = Color(0xFFF0D9B5),
        darkSquare = Color(0xFFB58863),
        selectedHighlight = Color(0xAA82A5C9),
        lastMoveHighlight = Color(0x66336699),
        legalMoveDot = Color(0x55111111)
    ),
    "Royal Blue" to BoardTheme(
        lightSquare = Color(0xFFE2E4E6),
        darkSquare = Color(0xFF3B739E),
        selectedHighlight = Color(0xAA99E2E6),
        lastMoveHighlight = Color(0x660099FF),
        legalMoveDot = Color(0x55111111)
    ),
    "Midnight Slate" to BoardTheme(
        lightSquare = Color(0xFF4A525D),
        darkSquare = Color(0xFF262C33),
        selectedHighlight = Color(0xAA8B9BB4),
        lastMoveHighlight = Color(0x66FFCC00),
        legalMoveDot = Color(0x88FFFFFF)
    )
)

@Composable
fun Chessboard(
    board: IntArray,
    selectedSquare: Int?,
    legalTargets: List<Move>,
    lastMove: Move?,
    kingInCheckSquare: Int?,
    themeName: String,
    playerColor: Int,
    highlightLegals: Boolean,
    onSquareClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    heatmap: IntArray? = null,
    animationTrigger: Long = 0L,
    animationMove: Move? = null,
    editStyle: String = "Next-Gen Velocity",
    enableCameraZoom: Boolean = true,
    enableMotionBlur: Boolean = true,
    enableScreenShake: Boolean = true,
    enableParticleBursts: Boolean = true,
    enableLightingGlow: Boolean = true,
    editStyleIntensity: Float = 0.8f
) {
    val theme = BOARD_THEMES[themeName] ?: BOARD_THEMES["Emerald Green"]!!

    // 1. Set up the Animatable progress ticker
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(animationTrigger) {
        if (animationTrigger > 0L) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
        }
    }

    // 2. Compute Transform State
    val shakeOffset = if (animProgress.value > 0f && animProgress.value < 0.6f && enableScreenShake) {
        val decay = (1f - animProgress.value / 0.6f).coerceAtLeast(0f)
        val freq = 45f
        val amplitude = 14.dp * editStyleIntensity * decay
        val dx = kotlin.math.sin(animProgress.value * freq * kotlin.math.PI.toFloat()) * amplitude.value
        val dy = kotlin.math.cos(animProgress.value * freq * 1.3f * kotlin.math.PI.toFloat()) * amplitude.value
        Offset(dx, dy)
    } else {
        Offset(0f, 0f)
    }

    val boardScale = if (animProgress.value > 0f && enableCameraZoom) {
        if (animProgress.value < 0.15f) {
            1f + (animProgress.value / 0.15f) * 0.07f * editStyleIntensity
        } else {
            1.07f - ((animProgress.value - 0.15f) / 0.85f) * 0.07f * editStyleIntensity
        }
    } else {
        1f
    }

    // 3. Compute Particle State
    val particles = remember(animationTrigger) {
        if (animationTrigger == 0L || animationMove == null) emptyList()
        else {
            val count = when (editStyle) {
                "Minecraft Crafting" -> 25
                "Shadow Ninja Sumi" -> 35
                else -> 20 // Next-Gen Velocity
            }
            List(count) {
                val angle = (0..359).random() * (kotlin.math.PI / 180f)
                val speed = (120..380).random().toFloat() * (if (editStyle == "Next-Gen Velocity") 1.4f else 1.0f)
                val size = when (editStyle) {
                    "Minecraft Crafting" -> (8..16).random().toFloat()
                    "Shadow Ninja Sumi" -> (12..30).random().toFloat()
                    else -> (5..11).random().toFloat()
                }
                val color = when (editStyle) {
                    "Minecraft Crafting" -> {
                        val r = (0..2).random()
                        if (r == 0) Color(0xFF81C784)
                        else if (r == 1) Color(0xFFFFD54F)
                        else Color(0xFF4CAF50)
                    }
                    "Shadow Ninja Sumi" -> {
                        val r = (0..2).random()
                        if (r == 0) Color(0xFFE53935)
                        else if (r == 1) Color(0xFF1E1E1E)
                        else Color(0xFFB71C1C)
                    }
                    else -> { // Next-Gen Velocity
                        val r = (0..2).random()
                        if (r == 0) Color(0xFF00FFCC)
                        else if (r == 1) Color(0xFF00E5FF)
                        else Color(0xFFFFFFFF)
                    }
                }
                val gravity = if (editStyle == "Minecraft Crafting") 200f else if (editStyle == "Shadow Ninja Sumi") -40f else 0f
                MoveParticle(
                    angle = angle.toFloat(),
                    speed = speed,
                    size = size,
                    color = color,
                    gravity = gravity
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant)
            .testTag("chessboard_container")
    ) {
        val squareSize = maxWidth / 8
        val density = LocalDensity.current
        val squareSizePx = with(density) { squareSize.toPx() }

        // Put the transforming graphics layer here
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = boardScale,
                    scaleY = boardScale,
                    translationX = shakeOffset.x,
                    translationY = shakeOffset.y
                )
        ) {
            Column {
                for (displayRank in 7 downTo 0) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (displayFile in 0..7) {
                            // Calculate active square taking player's color orientation into account
                            val file = if (playerColor == EngineConstants.BLACK) 7 - displayFile else displayFile
                            val rank = if (playerColor == EngineConstants.BLACK) 7 - displayRank else displayRank
                            val sq = rank * 8 + file

                            val isLight = (rank + file) % 2 != 0
                            var squareColor = if (isLight) theme.lightSquare else theme.darkSquare

                            // Highlight modifiers
                            if (sq == selectedSquare) {
                                squareColor = theme.selectedHighlight
                            } else if (sq == kingInCheckSquare) {
                                squareColor = theme.kingCheckColor
                            } else if (lastMove != null && (sq == lastMove.from || sq == lastMove.to)) {
                                squareColor = theme.lastMoveHighlight
                            }

                            Box(
                                modifier = Modifier
                                    .size(squareSize)
                                    .background(squareColor)
                                    .clickable { onSquareClick(sq) }
                                    .testTag("square_$sq"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (heatmap != null && heatmap[sq] != 0) {
                                    val heat = heatmap[sq]
                                    val overlayColor = if (heat > 0) {
                                        Color(0x0000E6FF).copy(alpha = (0.05f + (heat.coerceAtMost(4) * 0.08f)))
                                    } else {
                                        Color(0x00FF3D00).copy(alpha = (0.05f + ((-heat).coerceAtMost(4) * 0.08f)))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(overlayColor)
                                    )
                                }

                                val piece = board[sq]
                                if (piece != EngineConstants.EMPTY) {
                                    PieceGraphic(piece = piece, size = squareSize)
                                }

                                // Show legal move indicators
                                val isLegalTarget = legalTargets.any { it.to == sq }
                                if (highlightLegals && isLegalTarget) {
                                    if (piece == EngineConstants.EMPTY) {
                                        // Empty square: Show small dot
                                        Box(
                                            modifier = Modifier
                                                .size(squareSize * 0.3f)
                                                .background(theme.legalMoveDot, shape = CircleShape)
                                        )
                                    } else {
                                        // Capture: Show circular border around the square
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(0.85f)
                                                .border(3.dp, theme.legalMoveDot, shape = CircleShape)
                                        )
                                    }
                                }

                                // Optional file/rank legends on edges
                                val showLegendRank = displayFile == 0
                                val showLegendFile = displayRank == 0

                                if (showLegendRank) {
                                    val rankText = (rank + 1).toString()
                                    Text(
                                        text = rankText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) theme.darkSquare else theme.lightSquare,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(start = 2.dp, top = 2.dp)
                                    )
                                }
                                if (showLegendFile) {
                                    val fileText = ('a' + file).toString()
                                    Text(
                                        text = fileText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLight) theme.darkSquare else theme.lightSquare,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 2.dp, bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Motion Blur / Speed Lines Canvas (Overlay)
            if (enableMotionBlur && animProgress.value > 0f && animProgress.value < 0.6f) {
                val alpha = (1f - animProgress.value / 0.6f) * 0.45f * editStyleIntensity
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val lineColor = when (editStyle) {
                        "Minecraft Crafting" -> Color(0xFF81C784).copy(alpha = alpha)
                        "Shadow Ninja Sumi" -> Color(0xFFE53935).copy(alpha = alpha)
                        else -> Color(0xFF00FFCC).copy(alpha = alpha)
                    }
                    val center = Offset(w / 2f, h / 2f)
                    val strokeW = 2.dp.toPx()
                    val angles = listOf(0f, 30f, 60f, 90f, 120f, 150f, 180f, 210f, 240f, 270f, 300f, 330f)
                    for (ang in angles) {
                        val rad = ang * (kotlin.math.PI / 180f)
                        val cos = kotlin.math.cos(rad).toFloat()
                        val sin = kotlin.math.sin(rad).toFloat()
                        val startDist = w * 0.45f + (animProgress.value * 50f)
                        val endDist = startDist - 40f
                        val startPt = Offset(center.x + cos * startDist, center.y + sin * startDist)
                        val endPt = Offset(center.x + cos * endDist, center.y + sin * endDist)
                        drawLine(
                            color = lineColor,
                            start = startPt,
                            end = endPt,
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 5. Lighting FX / Dynamic Glow Aura (Overlay)
            if (enableLightingGlow && animProgress.value > 0f && animProgress.value < 0.7f && animationMove != null) {
                val file = animationMove.to % 8
                val rank = animationMove.to / 8
                val displayFile = if (playerColor == EngineConstants.BLACK) 7 - file else file
                val displayRank = if (playerColor == EngineConstants.BLACK) rank else 7 - rank
                val cX = (displayFile + 0.5f) * squareSizePx
                val cY = (displayRank + 0.5f) * squareSizePx
                val glowAlpha = (1f - animProgress.value / 0.7f) * 0.6f * editStyleIntensity
                val glowColor = when (editStyle) {
                    "Minecraft Crafting" -> Color(0xFFFFD54F)
                    "Shadow Ninja Sumi" -> Color(0xFFE53935)
                    else -> Color(0xFF00FFCC)
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = squareSizePx * (0.5f + animProgress.value * 1.5f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                            center = Offset(cX, cY),
                            radius = radius
                        ),
                        center = Offset(cX, cY),
                        radius = radius
                    )
                }
            }

            // 6. Particle Bursts Canvas (Overlay)
            if (enableParticleBursts && animProgress.value > 0f && animationMove != null && particles.isNotEmpty()) {
                val file = animationMove.to % 8
                val rank = animationMove.to / 8
                val displayFile = if (playerColor == EngineConstants.BLACK) 7 - file else file
                val displayRank = if (playerColor == EngineConstants.BLACK) rank else 7 - rank
                val cX = (displayFile + 0.5f) * squareSizePx
                val cY = (displayRank + 0.5f) * squareSizePx
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val t = animProgress.value
                    val alpha = (1f - t).coerceAtLeast(0f)
                    for (p in particles) {
                        val distance = p.speed * t
                        val px = cX + kotlin.math.cos(p.angle.toDouble()).toFloat() * distance
                        val py = cY + kotlin.math.sin(p.angle.toDouble()).toFloat() * distance + (0.5f * p.gravity * t * t * 500f)
                        if (editStyle == "Minecraft Crafting") {
                            val sizePx = p.size * (1f - t * 0.4f)
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(px - sizePx/2, py - sizePx/2),
                                size = Size(sizePx, sizePx)
                            )
                        } else if (editStyle == "Shadow Ninja Sumi") {
                            val sizePx = p.size * (1f + t * 0.8f)
                            drawCircle(
                                color = p.color.copy(alpha = alpha * 0.4f),
                                center = Offset(px, py),
                                radius = sizePx / 2
                            )
                        } else {
                            val sizePx = p.size * (1f - t)
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                center = Offset(px, py),
                                radius = sizePx / 2
                            )
                        }
                    }
                }
            }

            // 7. Special Shadow Ninja Crimson Screen Slash Overlay
            if (editStyle == "Shadow Ninja Sumi" && animProgress.value > 0.05f && animProgress.value < 0.4f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val progress = (animProgress.value - 0.05f) / 0.35f
                    val pathAlpha = (1f - progress) * 0.9f * editStyleIntensity
                    val path = Path().apply {
                        moveTo(w * -0.1f, h * 0.3f)
                        lineTo(w * 1.1f * progress, h * (0.3f + 0.4f * progress))
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFFE53935).copy(alpha = pathAlpha),
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
fun PieceGraphic(piece: Int, size: androidx.compose.ui.unit.Dp) {
    val glyph = when (piece) {
        EngineConstants.W_PAWN, EngineConstants.B_PAWN -> "♟"
        EngineConstants.W_KNIGHT, EngineConstants.B_KNIGHT -> "♞"
        EngineConstants.W_BISHOP, EngineConstants.B_BISHOP -> "♝"
        EngineConstants.W_ROOK, EngineConstants.B_ROOK -> "♜"
        EngineConstants.W_QUEEN, EngineConstants.B_QUEEN -> "♛"
        EngineConstants.W_KING, EngineConstants.B_KING -> "♚"
        else -> ""
    }

    val isWhite = piece in EngineConstants.W_PAWN..EngineConstants.W_KING
    val bodyColor = if (isWhite) Color(0xFFFFFAED) else Color(0xFF1E2124)
    val outlineColor = if (isWhite) Color(0xFF1E2124) else Color(0xFFF1F3F5)

    Text(
        text = glyph,
        fontSize = (size.value * 0.72f).sp,
        color = bodyColor,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontWeight = FontWeight.Normal,
            shadow = Shadow(
                color = outlineColor,
                offset = Offset(0f, 0f),
                blurRadius = 6f
            )
        )
    )
}
