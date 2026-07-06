package com.example.chess.engine.eval

import com.example.chess.engine.EngineConstants
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.EngineConstants.EMPTY
import com.example.chess.engine.EngineConstants.PAWN
import com.example.chess.engine.EngineConstants.KNIGHT
import com.example.chess.engine.EngineConstants.BISHOP
import com.example.chess.engine.EngineConstants.ROOK
import com.example.chess.engine.EngineConstants.QUEEN
import com.example.chess.engine.EngineConstants.KING
import com.example.chess.engine.board.BoardState
import com.example.chess.engine.moves.AttackTables

object Evaluation {
    const val VAL_PAWN = 100
    const val VAL_KNIGHT = 320
    const val VAL_BISHOP = 330
    const val VAL_ROOK = 500
    const val VAL_QUEEN = 900
    const val VAL_KING = 20000

    // Tapered Evaluation Phases
    private const val MAX_PHASE = 24
    // Phase weights for non-pawn pieces
    private const val KNIGHT_PHASE_WT = 1
    private const val BISHOP_PHASE_WT = 1
    private const val ROOK_PHASE_WT = 2
    private const val QUEEN_PHASE_WT = 4

    // Piece Square Tables (PSTs) - Middle-Game (MG) and End-Game (EG)
    // Indexes are from white's perspective. For black, mirror the rank.
    
    private val pstPawnMG = intArrayOf(
          0,   0,   0,   0,   0,   0,   0,   0,
         50,  50,  50,  50,  50,  50,  50,  50,
         10,  10,  20,  30,  30,  20,  10,  10,
          5,   5,  10,  25,  25,  10,   5,   5,
          0,   0,   0,  20,  20,   0,   0,   0,
          5,  -5, -10,   0,   0, -10,  -5,   5,
          5,  10,  10, -20, -20,  10,  10,   5,
          0,   0,   0,   0,   0,   0,   0,   0
    )

    private val pstPawnEG = intArrayOf(
          0,   0,   0,   0,   0,   0,   0,   0,
         50,  50,  50,  50,  50,  50,  50,  50,
         30,  30,  30,  40,  40,  30,  30,  30,
         20,  20,  20,  30,  30,  20,  20,  20,
         10,  10,  10,  20,  20,  10,  10,  10,
          5,   5,   5,  10,  10,   5,   5,   5,
          5,   5,   5, -10, -10,   5,   5,   5,
          0,   0,   0,   0,   0,   0,   0,   0
    )

    private val pstKnightMG = intArrayOf(
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20,   0,   0,   0,   0, -20, -40,
        -30,   0,  10,  15,  15,  10,   0, -30,
        -30,   5,  15,  20,  20,  15,   5, -30,
        -30,   0,  15,  20,  20,  15,   0, -30,
        -30,   5,  10,  15,  15,  10,   5, -30,
        -40, -20,   0,   5,   5,   0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    )

    private val pstKnightEG = intArrayOf(
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20,   0,   5,   5,   0, -20, -40,
        -30,   0,  10,  15,  15,  10,   0, -30,
        -30,   5,  15,  20,  20,  15,   5, -30,
        -30,   5,  15,  20,  20,  15,   5, -30,
        -30,   0,  10,  15,  15,  10,   0, -30,
        -40, -20,   0,   0,   0,   0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    )

    private val pstBishopMG = intArrayOf(
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   5,   5,  10,  10,   5,   5, -10,
        -10,   0,  10,  10,  10,  10,   0, -10,
        -10,  10,  10,  10,  10,  10,  10, -10,
        -10,   5,   0,   0,   0,   0,   5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    )

    private val pstBishopEG = intArrayOf(
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   5,   5,  10,  10,   5,   5, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   0,   0,   5,   5,   0,   0, -10,
        -10,  -5,   0,   0,   0,   0,  -5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    )

    private val pstRookMG = intArrayOf(
          0,   0,   0,   0,   0,   0,   0,   0,
          5,  10,  10,  10,  10,  10,  10,   5,
         -5,   0,   0,   0,   0,   0,   0,  -5,
         -5,   0,   0,   0,   0,   0,   0,  -5,
         -5,   0,   0,   0,   0,   0,   0,  -5,
         -5,   0,   0,   0,   0,   0,   0,  -5,
         -5,   0,   0,   0,   0,   0,   0,  -5,
          0,   0,   0,   5,   5,   0,   0,   0
    )

    private val pstRookEG = intArrayOf(
          0,   0,   0,   0,   0,   0,   0,   0,
         10,  10,  10,  10,  10,  10,  10,  10,
          0,   0,   0,   0,   0,   0,   0,   0,
          0,   0,   0,   0,   0,   0,   0,   0,
          0,   0,   0,   0,   0,   0,   0,   0,
          0,   0,   0,   0,   0,   0,   0,   0,
          0,   0,   0,   0,   0,   0,   0,   0,
          0,   0,   0,   0,   0,   0,   0,   0
    )

    private val pstQueenMG = intArrayOf(
        -20, -10, -10,  -5,  -5, -10, -10, -20,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
         -5,   0,   5,   5,   5,   5,   0,  -5,
          0,   0,   5,   5,   5,   5,   0,  -5,
        -10,   5,   5,   5,   5,   5,   0, -10,
        -10,   0,   5,   0,   0,   5,   0, -10,
        -20, -10, -10,  -5,  -5, -10, -10, -20
    )

    private val pstQueenEG = intArrayOf(
        -20, -10, -10,  -5,  -5, -10, -10, -20,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -10,   5,   5,   5,   5,   5,   5, -10,
         -5,   5,   5,   5,   5,   5,   5,  -5,
         -5,   5,   5,   5,   5,   5,   5,  -5,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -20, -10, -10,  -5,  -5, -10, -10, -20
    )

    private val pstKingMG = intArrayOf(
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -10, -20, -20, -20, -20, -20, -20, -10,
         20,  20,   0,   0,   0,   0,  20,  20,
         20,  30,  10,   0,   0,  10,  30,  20
    )

    private val pstKingEG = intArrayOf(
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10,   0,   0, -10, -20, -30,
        -30, -10,  20,  30,  30,  20, -10, -30,
        -30, -10,  30,  40,  40,  30, -10, -30,
        -30, -10,  30,  40,  40,  30, -10, -30,
        -30, -10,  20,  30,  30,  20, -10, -30,
        -30, -30,   0,   0,   0,   0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50
    )

    fun evaluate(state: BoardState): Int {
        var mgWhite = 0
        var egWhite = 0
        var mgBlack = 0
        var egBlack = 0

        var gamePhaseValue = 0

        // 1. Material and Positional (PST) scoring
        for (sq in 0..63) {
            val p = state.board[sq]
            if (p == EMPTY) continue

            val color = EngineConstants.colorOf(p)
            val type = EngineConstants.typeOf(p)

            val relativeSq = if (color == WHITE) sq else sq xor 56 // Mirror rank for black

            var pieceMg = 0
            var pieceEg = 0

            when (type) {
                PAWN -> {
                    pieceMg = VAL_PAWN + pstPawnMG[relativeSq]
                    pieceEg = VAL_PAWN + pstPawnEG[relativeSq]
                }
                KNIGHT -> {
                    pieceMg = VAL_KNIGHT + pstKnightMG[relativeSq]
                    pieceEg = VAL_KNIGHT + pstKnightEG[relativeSq]
                    gamePhaseValue += KNIGHT_PHASE_WT
                }
                BISHOP -> {
                    pieceMg = VAL_BISHOP + pstBishopMG[relativeSq]
                    pieceEg = VAL_BISHOP + pstBishopEG[relativeSq]
                    gamePhaseValue += BISHOP_PHASE_WT
                }
                ROOK -> {
                    pieceMg = VAL_ROOK + pstRookMG[relativeSq]
                    pieceEg = VAL_ROOK + pstRookEG[relativeSq]
                    gamePhaseValue += ROOK_PHASE_WT
                }
                QUEEN -> {
                    pieceMg = VAL_QUEEN + pstQueenMG[relativeSq]
                    pieceEg = VAL_QUEEN + pstQueenEG[relativeSq]
                    gamePhaseValue += QUEEN_PHASE_WT
                }
                KING -> {
                    pieceMg = VAL_KING + pstKingMG[relativeSq]
                    pieceEg = VAL_KING + pstKingEG[relativeSq]
                }
            }

            if (color == WHITE) {
                mgWhite += pieceMg
                egWhite += pieceEg
            } else {
                mgBlack += pieceMg
                egBlack += pieceEg
            }
        }

        // 2. Tapered interpolation based on game phase
        // Phase is 0 in pure endgames and MAX_PHASE in starting setups
        val phase = if (gamePhaseValue > MAX_PHASE) MAX_PHASE else gamePhaseValue
        val mgScore = mgWhite - mgBlack
        val egScore = egWhite - egBlack

        val finalScore = (mgScore * phase + egScore * (MAX_PHASE - phase)) / MAX_PHASE

        // 3. Score alignment with side to move (Negamax convention)
        return if (state.sideToMove == WHITE) finalScore else -finalScore
    }
}
