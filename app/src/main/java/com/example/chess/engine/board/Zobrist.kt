package com.example.chess.engine.board

import kotlin.random.Random

object Zobrist {
    private const val SEED = 18022026L // Fixed seed for deterministic hashing

    val pieceKeys = Array(13) { LongArray(64) }
    var sideKey: Long = 0L
    val castleKeys = LongArray(16)
    val enPassantKeys = LongArray(64)

    init {
        val random = Random(SEED)
        // 1. Pieces on squares
        for (p in 1..12) {
            for (sq in 0..63) {
                pieceKeys[p][sq] = random.nextLong()
            }
        }
        // 2. Side to move
        sideKey = random.nextLong()
        // 3. Castling rights
        for (i in 0..15) {
            castleKeys[i] = random.nextLong()
        }
        // 4. En passant squares
        for (sq in 0..63) {
            enPassantKeys[sq] = random.nextLong()
        }
    }
}
