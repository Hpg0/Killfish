package com.example.chess.engine.moves

import com.example.chess.engine.EngineConstants
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.EngineConstants.BOTH
import com.example.chess.engine.EngineConstants.EMPTY
import com.example.chess.engine.EngineConstants.PAWN
import com.example.chess.engine.EngineConstants.KNIGHT
import com.example.chess.engine.EngineConstants.BISHOP
import com.example.chess.engine.EngineConstants.ROOK
import com.example.chess.engine.EngineConstants.QUEEN
import com.example.chess.engine.EngineConstants.KING
import com.example.chess.engine.board.BoardState
import com.example.chess.engine.board.BitboardUtils

object MoveGenerator {

    fun generateLegalMoves(state: BoardState): List<Move> {
        val pseudoMoves = generatePseudoLegalMoves(state)
        val legalMoves = ArrayList<Move>()
        for (move in pseudoMoves) {
            if (state.makeMove(move)) {
                legalMoves.add(move)
                state.unmakeMove(move)
            }
        }
        return legalMoves
    }

    fun generatePseudoLegalMoves(state: BoardState): List<Move> {
        val moves = ArrayList<Move>()
        val us = state.sideToMove
        val them = us xor 1
        val occupied = state.getOccupancy(BOTH)

        for (sq in 0..63) {
            val p = state.board[sq]
            if (p == EMPTY || EngineConstants.colorOf(p) != us) continue
            val type = EngineConstants.typeOf(p)

            when (type) {
                PAWN -> generatePawnMoves(sq, us, state, occupied, moves)
                KNIGHT -> generateKnightMoves(sq, them, state, moves)
                BISHOP -> generateBishopMoves(sq, them, state, occupied, moves)
                ROOK -> generateRookMoves(sq, them, state, occupied, moves)
                QUEEN -> generateQueenMoves(sq, them, state, occupied, moves)
                KING -> generateKingMoves(sq, them, state, occupied, moves)
            }
        }

        generateCastlingMoves(us, state, occupied, moves)
        return moves
    }

    private fun generatePawnMoves(sq: Int, us: Int, state: BoardState, occupied: Long, moves: ArrayList<Move>) {
        val file = sq % 8
        val rank = sq / 8

        if (us == WHITE) {
            // 1. Single Push
            val to = sq + 8
            if (to in 0..63 && state.board[to] == EMPTY) {
                if (to / 8 == 7) {
                    addPawnPromotions(sq, to, false, moves)
                } else {
                    moves.add(Move.create(sq, to, PAWN))
                    // 2. Double Push
                    val to2 = sq + 16
                    if (rank == 1 && state.board[to2] == EMPTY) {
                        moves.add(Move.create(sq, to2, PAWN, isDoublePush = true))
                    }
                }
            }

            // 3. Normal Captures (North-west, North-east)
            val captureTargets = intArrayOf(sq + 7, sq + 9)
            for (target in captureTargets) {
                if (target in 0..63) {
                    val tf = target % 8
                    val tr = target / 8
                    // Avoid wrapping around ranks or files
                    if (kotlin.math.abs(tf - file) == 1 && tr - rank == 1) {
                        val tp = state.board[target]
                        if (tp != EMPTY && EngineConstants.colorOf(tp) == BLACK) {
                            if (tr == 7) {
                                addPawnPromotions(sq, target, true, moves)
                            } else {
                                moves.add(Move.create(sq, target, PAWN, isCapture = true))
                            }
                        }
                        // En Passant
                        if (target == state.enPassant) {
                            moves.add(Move.create(sq, target, PAWN, isCapture = true, isEnPassant = true))
                        }
                    }
                }
            }
        } else {
            // 1. Single Push
            val to = sq - 8
            if (to in 0..63 && state.board[to] == EMPTY) {
                if (to / 8 == 0) {
                    addPawnPromotions(sq, to, false, moves)
                } else {
                    moves.add(Move.create(sq, to, PAWN))
                    // 2. Double Push
                    val to2 = sq - 16
                    if (rank == 6 && state.board[to2] == EMPTY) {
                        moves.add(Move.create(sq, to2, PAWN, isDoublePush = true))
                    }
                }
            }

            // 3. Normal Captures (South-west, South-east)
            val captureTargets = intArrayOf(sq - 9, sq - 7)
            for (target in captureTargets) {
                if (target in 0..63) {
                    val tf = target % 8
                    val tr = target / 8
                    if (kotlin.math.abs(tf - file) == 1 && rank - tr == 1) {
                        val tp = state.board[target]
                        if (tp != EMPTY && EngineConstants.colorOf(tp) == WHITE) {
                            if (tr == 0) {
                                addPawnPromotions(sq, target, true, moves)
                            } else {
                                moves.add(Move.create(sq, target, PAWN, isCapture = true))
                            }
                        }
                        // En Passant
                        if (target == state.enPassant) {
                            moves.add(Move.create(sq, target, PAWN, isCapture = true, isEnPassant = true))
                        }
                    }
                }
            }
        }
    }

    private fun addPawnPromotions(from: Int, to: Int, isCapture: Boolean, moves: ArrayList<Move>) {
        moves.add(Move.create(from, to, PAWN, promoType = QUEEN, isCapture = isCapture))
        moves.add(Move.create(from, to, PAWN, promoType = ROOK, isCapture = isCapture))
        moves.add(Move.create(from, to, PAWN, promoType = BISHOP, isCapture = isCapture))
        moves.add(Move.create(from, to, PAWN, promoType = KNIGHT, isCapture = isCapture))
    }

    private fun generateKnightMoves(sq: Int, them: Int, state: BoardState, moves: ArrayList<Move>) {
        var attacks = AttackTables.knightAttacks[sq]
        while (attacks != 0L) {
            val to = BitboardUtils.getLSB(attacks)
            attacks = attacks and (attacks - 1) // pop LSB
            val tp = state.board[to]
            if (tp == EMPTY) {
                moves.add(Move.create(sq, to, KNIGHT))
            } else if (EngineConstants.colorOf(tp) == them) {
                moves.add(Move.create(sq, to, KNIGHT, isCapture = true))
            }
        }
    }

    private fun generateBishopMoves(sq: Int, them: Int, state: BoardState, occupied: Long, moves: ArrayList<Move>) {
        var attacks = AttackTables.getBishopAttacks(sq, occupied)
        while (attacks != 0L) {
            val to = BitboardUtils.getLSB(attacks)
            attacks = attacks and (attacks - 1)
            val tp = state.board[to]
            if (tp == EMPTY) {
                moves.add(Move.create(sq, to, BISHOP))
            } else if (EngineConstants.colorOf(tp) == them) {
                moves.add(Move.create(sq, to, BISHOP, isCapture = true))
            }
        }
    }

    private fun generateRookMoves(sq: Int, them: Int, state: BoardState, occupied: Long, moves: ArrayList<Move>) {
        var attacks = AttackTables.getRookAttacks(sq, occupied)
        while (attacks != 0L) {
            val to = BitboardUtils.getLSB(attacks)
            attacks = attacks and (attacks - 1)
            val tp = state.board[to]
            if (tp == EMPTY) {
                moves.add(Move.create(sq, to, ROOK))
            } else if (EngineConstants.colorOf(tp) == them) {
                moves.add(Move.create(sq, to, ROOK, isCapture = true))
            }
        }
    }

    private fun generateQueenMoves(sq: Int, them: Int, state: BoardState, occupied: Long, moves: ArrayList<Move>) {
        var attacks = AttackTables.getQueenAttacks(sq, occupied)
        while (attacks != 0L) {
            val to = BitboardUtils.getLSB(attacks)
            attacks = attacks and (attacks - 1)
            val tp = state.board[to]
            if (tp == EMPTY) {
                moves.add(Move.create(sq, to, QUEEN))
            } else if (EngineConstants.colorOf(tp) == them) {
                moves.add(Move.create(sq, to, QUEEN, isCapture = true))
            }
        }
    }

    private fun generateKingMoves(sq: Int, them: Int, state: BoardState, occupied: Long, moves: ArrayList<Move>) {
        var attacks = AttackTables.kingAttacks[sq]
        while (attacks != 0L) {
            val to = BitboardUtils.getLSB(attacks)
            attacks = attacks and (attacks - 1)
            val tp = state.board[to]
            if (tp == EMPTY) {
                moves.add(Move.create(sq, to, KING))
            } else if (EngineConstants.colorOf(tp) == them) {
                moves.add(Move.create(sq, to, KING, isCapture = true))
            }
        }
    }

    private fun generateCastlingMoves(us: Int, state: BoardState, occupied: Long, moves: ArrayList<Move>) {
        if (us == WHITE) {
            val kingSq = EngineConstants.E1
            if (state.board[kingSq] != EngineConstants.W_KING) return
            
            // Check if king is currently in check
            if (AttackTables.isSquareAttacked(kingSq, BLACK, occupied, state)) return

            // King-side
            if ((state.castlingRights and EngineConstants.CASTLE_WK) != 0) {
                val f1 = EngineConstants.F1
                val g1 = EngineConstants.G1
                if (state.board[f1] == EMPTY && state.board[g1] == EMPTY) {
                    if (!state.isSquareAttacked(f1, BLACK) && !state.isSquareAttacked(g1, BLACK)) {
                        moves.add(Move.create(kingSq, g1, KING, isCastling = true))
                    }
                }
            }

            // Queen-side
            if ((state.castlingRights and EngineConstants.CASTLE_WQ) != 0) {
                val d1 = EngineConstants.D1
                val c1 = EngineConstants.C1
                val b1 = EngineConstants.B1
                if (state.board[d1] == EMPTY && state.board[c1] == EMPTY && state.board[b1] == EMPTY) {
                    if (!state.isSquareAttacked(d1, BLACK) && !state.isSquareAttacked(c1, BLACK)) {
                        moves.add(Move.create(kingSq, c1, KING, isCastling = true))
                    }
                }
            }
        } else {
            val kingSq = EngineConstants.E8
            if (state.board[kingSq] != EngineConstants.B_KING) return

            // Check if king is currently in check
            if (AttackTables.isSquareAttacked(kingSq, WHITE, occupied, state)) return

            // King-side
            if ((state.castlingRights and EngineConstants.CASTLE_BK) != 0) {
                val f8 = EngineConstants.F8
                val g8 = EngineConstants.G8
                if (state.board[f8] == EMPTY && state.board[g8] == EMPTY) {
                    if (!state.isSquareAttacked(f8, WHITE) && !state.isSquareAttacked(g8, WHITE)) {
                        moves.add(Move.create(kingSq, g8, KING, isCastling = true))
                    }
                }
            }

            // Queen-side
            if ((state.castlingRights and EngineConstants.CASTLE_BQ) != 0) {
                val d8 = EngineConstants.D8
                val c8 = EngineConstants.C8
                val b8 = EngineConstants.B8
                if (state.board[d8] == EMPTY && state.board[c8] == EMPTY && state.board[b8] == EMPTY) {
                    if (!state.isSquareAttacked(d8, WHITE) && !state.isSquareAttacked(c8, WHITE)) {
                        moves.add(Move.create(kingSq, c8, KING, isCastling = true))
                    }
                }
            }
        }
    }
}
