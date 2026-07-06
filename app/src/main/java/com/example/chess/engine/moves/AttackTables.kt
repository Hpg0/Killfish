package com.example.chess.engine.moves

import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.board.BitboardUtils

object AttackTables {
    val pawnAttacks = Array(2) { LongArray(64) }
    val knightAttacks = LongArray(64)
    val kingAttacks = LongArray(64)

    init {
        precalculateNonSliding()
    }

    private fun precalculateNonSliding() {
        for (sq in 0..63) {
            val file = sq % 8
            val rank = sq / 8

            // 1. Pawn Attacks
            // White Pawn Attacks (North-west, North-east)
            var wAttacks = 0L
            if (rank < 7) {
                if (file > 0) wAttacks = wAttacks or (1L shl (sq + 7))
                if (file < 7) wAttacks = wAttacks or (1L shl (sq + 9))
            }
            pawnAttacks[WHITE][sq] = wAttacks

            // Black Pawn Attacks (South-west, South-east)
            var bAttacks = 0L
            if (rank > 0) {
                if (file > 0) bAttacks = bAttacks or (1L shl (sq - 9))
                if (file < 7) bAttacks = bAttacks or (1L shl (sq - 7))
            }
            pawnAttacks[BLACK][sq] = bAttacks

            // 2. Knight Attacks
            var kAttacks = 0L
            val knightOffsets = arrayOf(
                Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
                Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
            )
            for (offset in knightOffsets) {
                val nf = file + offset.first
                val nr = rank + offset.second
                if (nf in 0..7 && nr in 0..7) {
                    kAttacks = kAttacks or (1L shl (nr * 8 + nf))
                }
            }
            knightAttacks[sq] = kAttacks

            // 3. King Attacks
            var kgAttacks = 0L
            for (df in -1..1) {
                for (dr in -1..1) {
                    if (df == 0 && dr == 0) continue
                    val nf = file + df
                    val nr = rank + dr
                    if (nf in 0..7 && nr in 0..7) {
                        kgAttacks = kgAttacks or (1L shl (nr * 8 + nf))
                    }
                }
            }
            kingAttacks[sq] = kgAttacks
        }
    }

    // Dynamic slider attack generation
    fun getBishopAttacks(sq: Int, occupied: Long): Long {
        var attacks = 0L
        val file = sq % 8
        val rank = sq / 8

        // Diagonals: NE, NW, SE, SW
        val directions = arrayOf(Pair(1, 1), Pair(-1, 1), Pair(1, -1), Pair(-1, -1))
        for (dir in directions) {
            var f = file + dir.first
            var r = rank + dir.second
            while (f in 0..7 && r in 0..7) {
                val targetSq = r * 8 + f
                attacks = attacks or (1L shl targetSq)
                if ((occupied and (1L shl targetSq)) != 0L) {
                    break
                }
                f += dir.first
                r += dir.second
            }
        }
        return attacks
    }

    fun getRookAttacks(sq: Int, occupied: Long): Long {
        var attacks = 0L
        val file = sq % 8
        val rank = sq / 8

        // Cardinal: N, S, E, W
        val directions = arrayOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))
        for (dir in directions) {
            var f = file + dir.first
            var r = rank + dir.second
            while (f in 0..7 && r in 0..7) {
                val targetSq = r * 8 + f
                attacks = attacks or (1L shl targetSq)
                if ((occupied and (1L shl targetSq)) != 0L) {
                    break
                }
                f += dir.first
                r += dir.second
            }
        }
        return attacks
    }

    fun getQueenAttacks(sq: Int, occupied: Long): Long {
        return getBishopAttacks(sq, occupied) or getRookAttacks(sq, occupied)
    }

    fun isSquareAttacked(sq: Int, byColor: Int, occupied: Long, board: IntArray): Boolean {
        // 1. Attacked by pawns of opposite color
        val opposingPawnAttacks = pawnAttacks[byColor xor 1][sq]
        val pawns = if (byColor == WHITE) 1L shl sq else 1L shl sq // Pawn attacks from target sq
        if ((pawnAttacks[byColor xor 1][sq] and getPawnBitboard(byColor, board)) != 0L) return true

        // 2. Attacked by Knights
        if ((knightAttacks[sq] and getPieceBitboard(byColor, com.example.chess.engine.EngineConstants.KNIGHT, board)) != 0L) return true

        // 3. Attacked by Bishops/Queens
        val bishopAttacks = getBishopAttacks(sq, occupied)
        val bishopsQueens = getPieceBitboard(byColor, com.example.chess.engine.EngineConstants.BISHOP, board) or
                getPieceBitboard(byColor, com.example.chess.engine.EngineConstants.QUEEN, board)
        if ((bishopAttacks and bishopsQueens) != 0L) return true

        // 4. Attacked by Rooks/Queens
        val rookAttacks = getRookAttacks(sq, occupied)
        val rooksQueens = getPieceBitboard(byColor, com.example.chess.engine.EngineConstants.ROOK, board) or
                getPieceBitboard(byColor, com.example.chess.engine.EngineConstants.QUEEN, board)
        if ((rookAttacks and rooksQueens) != 0L) return true

        // 5. Attacked by King
        if ((kingAttacks[sq] and getPieceBitboard(byColor, com.example.chess.engine.EngineConstants.KING, board)) != 0L) return true

        return false
    }

    private fun getPawnBitboard(color: Int, board: IntArray): Long {
        var bb = 0L
        val pawnCode = if (color == WHITE) com.example.chess.engine.EngineConstants.W_PAWN else com.example.chess.engine.EngineConstants.B_PAWN
        for (i in 0..63) {
            if (board[i] == pawnCode) bb = bb or (1L shl i)
        }
        return bb
    }

    private fun getPieceBitboard(color: Int, type: Int, board: IntArray): Long {
        var bb = 0L
        val pieceCode = com.example.chess.engine.EngineConstants.createPiece(color, type)
        for (i in 0..63) {
            if (board[i] == pieceCode) bb = bb or (1L shl i)
        }
        return bb
    }
}
