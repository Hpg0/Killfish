package com.example.chess.engine.search

import com.example.chess.engine.EngineConstants
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.EngineConstants.EMPTY
import com.example.chess.engine.EngineConstants.PAWN
import com.example.chess.engine.board.BoardState
import com.example.chess.engine.board.Zobrist
import com.example.chess.engine.eval.Evaluation
import com.example.chess.engine.moves.Move
import com.example.chess.engine.moves.MoveGenerator
import com.example.chess.engine.moves.AttackTables

class SearchEngine(private val tt: TranspositionTable = TranspositionTable()) {

    companion object {
        const val MATE_VALUE = 30000
        const val INFINITY = 50000
        const val DRAW_VALUE = 0
    }

    interface SearchListener {
        fun onIterationComplete(depth: Int, score: Int, nodes: Int, timeMs: Long, bestMove: Move, pv: List<Move>)
    }

    var isStop = false
    var nodes = 0
    var hashHits = 0
    var nmpCutoffs = 0
    var lmrReductions = 0
    var betaCutoffs = 0
    var firstMoveCutoffs = 0

    // Heuristics
    private val killerMoves = Array(64) { Array(2) { Move.NULL } } // [depth][slot]
    private val historyTable = Array(13) { IntArray(64) }          // [piece][to]

    fun clearHeuristics() {
        tt.clear()
        for (i in 0..63) {
            killerMoves[i][0] = Move.NULL
            killerMoves[i][1] = Move.NULL
        }
        for (p in 0..12) {
            for (sq in 0..63) {
                historyTable[p][sq] = 0
            }
        }
        hashHits = 0
        nmpCutoffs = 0
        lmrReductions = 0
        betaCutoffs = 0
        firstMoveCutoffs = 0
    }

    fun search(state: BoardState, maxDepth: Int, listener: SearchListener? = null): Move {
        isStop = false
        nodes = 0
        clearHeuristics()

        // High-Speed Opening Book Subsystem Lookup
        val bookMove = com.example.chess.engine.utils.OpeningBook.selectBookMove(state)
        if (bookMove != null && bookMove != Move.NULL) {
            listener?.onIterationComplete(
                depth = 1,
                score = 0,
                nodes = 1,
                timeMs = 1,
                bestMove = bookMove,
                pv = listOf(bookMove)
            )
            return bookMove
        }

        val startTime = System.currentTimeMillis()
        var bestMove = Move.NULL
        var lastScore = 0

        // Iterative Deepening
        for (depth in 1..maxDepth) {
            if (isStop) break

            // Aspiration Windows: Search with a narrow alpha-beta window around lastScore
            var alpha = -INFINITY
            var beta = INFINITY
            if (depth > 2) {
                val window = 50
                alpha = lastScore - window
                beta = lastScore + window
            }

            var score = 0
            while (true) {
                if (isStop) break
                score = negamax(state, alpha, beta, depth, 0)

                // If score is outside window bounds, widen search window to infinity and re-search
                if (score <= alpha) {
                    alpha = -INFINITY
                } else if (score >= beta) {
                    beta = INFINITY
                } else {
                    break
                }
            }

            if (!isStop) {
                lastScore = score
                val ttEntry = tt.lookup(state.hash)
                if (ttEntry != null && ttEntry.bestMove != Move.NULL) {
                    bestMove = ttEntry.bestMove
                }

                // Extract Principal Variation (PV)
                val pv = ArrayList<Move>()
                var tempState = BoardState()
                tempState.copyFrom(state)
                var pvDepth = 0
                while (pvDepth < depth) {
                    val entry = tt.lookup(tempState.hash) ?: break
                    val move = entry.bestMove
                    if (move == Move.NULL) break
                    
                    // Verify if move is valid/legal
                    val legals = MoveGenerator.generateLegalMoves(tempState)
                    if (legals.none { it.value == move.value }) break

                    pv.add(move)
                    tempState.makeMove(move)
                    pvDepth++
                }

                val elapsed = System.currentTimeMillis() - startTime
                listener?.onIterationComplete(depth, lastScore, nodes, elapsed, bestMove, pv)
            }
        }

        return bestMove
    }

    private fun negamax(state: BoardState, initialAlpha: Int, initialBeta: Int, depth: Int, ply: Int): Int {
        nodes++
        var alpha = initialAlpha
        var beta = initialBeta

        // Check for draw conditions (3-fold repetition or 50-move rule)
        if (ply > 0 && (state.isDrawByFiftyMoves() || state.isDrawByRepetition())) {
            return DRAW_VALUE
        }

        // 1. TT Lookup
        val ttEntry = tt.lookup(state.hash)
        var ttMove = Move.NULL
        if (ttEntry != null) {
            hashHits++
            if (ttEntry.depth >= depth) {
                ttMove = ttEntry.bestMove
                when (ttEntry.flag) {
                    TranspositionTable.EXACT -> return ttEntry.value
                    TranspositionTable.ALPHA -> {
                        if (ttEntry.value <= alpha) return alpha
                        if (ttEntry.value < beta) beta = ttEntry.value
                    }
                    TranspositionTable.BETA -> {
                        if (ttEntry.value >= beta) return beta
                        if (ttEntry.value > alpha) alpha = ttEntry.value
                    }
                }
                if (alpha >= beta) return ttEntry.value
            }
        }

        // Leaf Node -> Quiescence Search
        if (depth <= 0) {
            return quiescence(state, alpha, beta)
        }

        val inCheck = state.isSquareAttacked(state.findKing(state.sideToMove), state.sideToMove xor 1)

        // 2. Null Move Pruning (NMP)
        if (!inCheck && depth >= 3 && ply > 0 && hasMajorPieces(state, state.sideToMove)) {
            // Null Move execution
            state.sideToMove = state.sideToMove xor 1
            state.hash = state.hash xor Zobrist.sideKey
            if (state.enPassant != -1) {
                state.hash = state.hash xor Zobrist.enPassantKeys[state.enPassant]
            }
            val oldEp = state.enPassant
            state.enPassant = -1

            val nullScore = -negamax(state, -beta, -beta + 1, depth - 1 - 2, ply + 1)

            // Undo Null Move
            state.sideToMove = state.sideToMove xor 1
            state.hash = state.hash xor Zobrist.sideKey
            state.enPassant = oldEp
            if (state.enPassant != -1) {
                state.hash = state.hash xor Zobrist.enPassantKeys[state.enPassant]
            }

            if (nullScore >= beta) {
                nmpCutoffs++
                return beta // Fail-high cutoff
            }
        }

        // 3. Move Generation and Ordering
        val moves = MoveGenerator.generatePseudoLegalMoves(state)
        val orderedMoves = orderMoves(moves, state, ttMove, ply)

        var legalMovesCount = 0
        var bestLocalMove = Move.NULL
        var originalAlpha = alpha

        for (i in orderedMoves.indices) {
            val move = orderedMoves[i]
            if (!state.makeMove(move)) continue

            legalMovesCount++

            var score: Int
            if (legalMovesCount == 1) {
                // Search first (principal) move fully
                score = -negamax(state, -beta, -alpha, depth - 1, ply + 1)
            } else {
                // 4. Late Move Reduction (LMR) for quiet, late-ordered moves
                if (depth >= 3 && !move.isCapture && move.promoType == EMPTY && !inCheck) {
                    lmrReductions++
                    // Try a search with reduced depth
                    val reduction = if (legalMovesCount > 4) 2 else 1
                    score = -negamax(state, -alpha - 1, -alpha, depth - 1 - reduction, ply + 1)
                    
                    // If reduced search fails high, we must re-search at full depth
                    if (score > alpha) {
                        score = -negamax(state, -beta, -alpha, depth - 1, ply + 1)
                    }
                } else {
                    score = -negamax(state, -beta, -alpha, depth - 1, ply + 1)
                }
            }

            state.unmakeMove(move)

            if (isStop) return 0

            // Cutoff / Update bounds
            if (score >= beta) {
                betaCutoffs++
                if (legalMovesCount == 1) {
                    firstMoveCutoffs++
                }
                // Beta cutoff -> store in TT and record killers/history if quiet
                tt.store(state.hash, beta, depth, TranspositionTable.BETA, move)
                if (!move.isCapture) {
                    storeKiller(move, ply)
                    historyTable[move.pieceType][move.to] += depth * depth
                }
                return beta
            }

            if (score > alpha) {
                alpha = score
                bestLocalMove = move
            }
        }

        // 5. Mate or Draw terminal detection
        if (legalMovesCount == 0) {
            return if (inCheck) {
                // Checkmate score relative to distance from root
                -MATE_VALUE + ply
            } else {
                DRAW_VALUE // Stalemate
            }
        }

        // 6. TT Store
        val flag = if (alpha <= originalAlpha) TranspositionTable.ALPHA else if (alpha >= beta) TranspositionTable.BETA else TranspositionTable.EXACT
        tt.store(state.hash, alpha, depth, flag, bestLocalMove)

        return alpha
    }

    private fun quiescence(state: BoardState, initialAlpha: Int, beta: Int): Int {
        nodes++
        var alpha = initialAlpha

        // Stand-pat score (lower bound of evaluation)
        val eval = Evaluation.evaluate(state)
        if (eval >= beta) return beta
        if (eval > alpha) alpha = eval

        // Capture generation only
        val moves = MoveGenerator.generatePseudoLegalMoves(state).filter { it.isCapture }
        val orderedMoves = orderMoves(moves, state, Move.NULL, 0)

        for (move in orderedMoves) {
            if (!state.makeMove(move)) continue
            val score = -quiescence(state, -beta, -alpha)
            state.unmakeMove(move)

            if (score >= beta) return beta
            if (score > alpha) alpha = score
        }

        return alpha
    }

    private fun orderMoves(moves: List<Move>, state: BoardState, ttMove: Move, ply: Int): List<Move> {
        val scoredMoves = moves.map { move ->
            val score = when {
                move.value == ttMove.value -> 1000000 // Best move from TT
                move.isCapture -> {
                    val victim = if (move.isEnPassant) PAWN else EngineConstants.typeOf(state.board[move.to])
                    val attacker = move.pieceType
                    900000 + (victim * 10) - attacker // MVV-LVA
                }
                ply < 64 && move.value == killerMoves[ply][0].value -> 800000
                ply < 64 && move.value == killerMoves[ply][1].value -> 750000
                else -> historyTable[move.pieceType][move.to] // History heuristic
            }
            Pair(move, score)
        }
        return scoredMoves.sortedByDescending { it.second }.map { it.first }
    }

    private fun storeKiller(move: Move, ply: Int) {
        if (ply >= 64) return
        if (killerMoves[ply][0].value != move.value) {
            killerMoves[ply][1] = killerMoves[ply][0]
            killerMoves[ply][0] = move
        }
    }

    private fun hasMajorPieces(state: BoardState, color: Int): Boolean {
        // Returns true if color has anything on board other than King and Pawns
        val targetRange = if (color == WHITE) EngineConstants.W_KNIGHT..EngineConstants.W_QUEEN else EngineConstants.B_KNIGHT..EngineConstants.B_QUEEN
        for (sq in 0..63) {
            if (state.board[sq] in targetRange) return true
        }
        return false
    }
}
