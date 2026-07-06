package com.example.chess.engine.utils

import com.example.chess.engine.board.BoardState
import com.example.chess.engine.moves.Move
import com.example.chess.engine.moves.MoveGenerator
import kotlin.random.Random

object OpeningBook {
    data class BookMove(
        val uci: String,
        val weight: Int,
        val openingName: String
    )

    private val book = HashMap<String, ArrayList<BookMove>>()

    init {
        // --- White First Moves ---
        registerLine(listOf("e2e4"), 45, "King's Pawn Opening")
        registerLine(listOf("d2d4"), 35, "Queen's Pawn Opening")
        registerLine(listOf("c2c4"), 10, "English Opening")
        registerLine(listOf("g1f3"), 10, "Réti Opening")

        // --- Sicilian Defense (1. e4 c5) ---
        registerLine(listOf("e2e4", "c7c5"), 50, "Sicilian Defense")
        registerLine(listOf("e2e4", "c7c5", "g1f3"), 80, "Sicilian Defense: Open")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6"), 60, "Sicilian: Najdorf/Dragon Prep")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4"), 90, "Sicilian Defense: Open")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5xd4"), 100, "Sicilian Defense: Open")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5xd4", "f3xd4"), 100, "Sicilian Defense: Open")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5xd4", "f3xd4", "g8f6"), 100, "Sicilian Defense: Open")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5xd4", "f3xd4", "g8f6", "b1c3"), 100, "Sicilian Defense: Open")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5xd4", "f3xd4", "g8f6", "b1c3", "a7a6"), 70, "Sicilian Defense: Najdorf Variation")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5xd4", "f3xd4", "g8f6", "b1c3", "g7g6"), 30, "Sicilian Defense: Dragon Variation")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "e7e6"), 25, "Sicilian Defense: French Variation")
        registerLine(listOf("e2e4", "c7c5", "g1f3", "b8c6"), 20, "Sicilian Defense: Old Sicilian")
        registerLine(listOf("e2e4", "c7c5", "b1c3"), 15, "Closed Sicilian")
        registerLine(listOf("e2e4", "c7c5", "c2c3"), 10, "Sicilian Defense: Alapin Variation")

        // --- Ruy Lopez & Italian Game (1. e4 e5) ---
        registerLine(listOf("e2e4", "e7e5"), 35, "Open Game (King's Pawn)")
        registerLine(listOf("e2e4", "e7e5", "g1f3"), 90, "King's Knight Opening")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6"), 90, "Open Game: Main Line")
        
        // Ruy Lopez
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5"), 55, "Ruy Lopez (Spanish Opening)")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6"), 70, "Ruy Lopez: Morphy Defense")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "f1a4"), 95, "Ruy Lopez: Morphy Defense")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "f1a4", "g8f6"), 100, "Ruy Lopez: Closed Defense")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "g8f6"), 30, "Ruy Lopez: Berlin Defense")

        // Italian Game
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"), 40, "Italian Game")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5"), 60, "Italian Game: Giuoco Piano")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "f8c5", "c2c3"), 85, "Italian Game: Giuoco Pianissimo")
        registerLine(listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6"), 35, "Italian Game: Two Knights Defense")

        // --- French Defense (1. e4 e6) ---
        registerLine(listOf("e2e4", "e7e6"), 15, "French Defense")
        registerLine(listOf("e2e4", "e7e6", "d2d4"), 95, "French Defense: Normal")
        registerLine(listOf("e2e4", "e7e6", "d2d4", "d7d5"), 100, "French Defense: Main Line")
        registerLine(listOf("e2e4", "e7e6", "d2d4", "d7d5", "e4e5"), 50, "French Defense: Advance Variation")
        registerLine(listOf("e2e4", "e7e6", "d2d4", "d7d5", "b1c3"), 45, "French Defense: Paulsen Variation")

        // --- Caro-Kann Defense (1. e4 c6) ---
        registerLine(listOf("e2e4", "c7c6"), 12, "Caro-Kann Defense")
        registerLine(listOf("e2e4", "c7c6", "d2d4"), 95, "Caro-Kann Defense: Normal")
        registerLine(listOf("e2e4", "c7c6", "d2d4", "d7d5"), 100, "Caro-Kann Defense: Main Line")
        registerLine(listOf("e2e4", "c7c6", "d2d4", "d7d5", "e4e5"), 55, "Caro-Kann Defense: Advance Variation")
        registerLine(listOf("e2e4", "c7c6", "d2d4", "d7d5", "b1c3"), 40, "Caro-Kann Defense: Classical")

        // --- Scandinavian Defense (1. e4 d5) ---
        registerLine(listOf("e2e4", "d7d5"), 5, "Scandinavian Defense")
        registerLine(listOf("e2e4", "d7d5", "e4xd5"), 95, "Scandinavian: Main Line")
        registerLine(listOf("e2e4", "d7d5", "e4xd5", "d1xd5"), 90, "Scandinavian: Main Line")
        registerLine(listOf("e2e4", "d7d5", "e4xd5", "d1xd5", "b1c3"), 100, "Scandinavian: Main Line")

        // --- Queen's Gambit (1. d4 d5) ---
        registerLine(listOf("d2d4", "d7d5"), 50, "Queen's Pawn Game")
        registerLine(listOf("d2d4", "d7d5", "c2c4"), 85, "Queen's Gambit")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "e7e6"), 50, "Queen's Gambit Declined")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "e7e6", "b1c3"), 75, "QGD: Main Line")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "e7e6", "b1c3", "g8f6"), 90, "QGD: Orthodox Defense")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "c7c6"), 40, "Slav Defense")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "c7c6", "g1f3"), 70, "Slav Defense")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "c7c6", "g1f3", "g8f6"), 100, "Slav Defense")
        registerLine(listOf("d2d4", "d7d5", "c2c4", "d5xc4"), 15, "Queen's Gambit Accepted")

        // --- Indian Defenses (1. d4 Nf6) ---
        registerLine(listOf("d2d4", "g8f6"), 45, "Indian Defense")
        registerLine(listOf("d2d4", "g8f6", "c2c4"), 85, "Indian Defense")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "g7g6"), 40, "King's Indian / Grünfeld Prep")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "g7g6", "b1c3"), 80, "King's Indian / Grünfeld Prep")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "g7g6", "b1c3", "f8g7"), 100, "King's Indian Defense")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "g7g6", "b1c3", "f8g7", "e2e4"), 100, "King's Indian Defense")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "g7g6", "b1c3", "f8g7", "e2e4", "d7d6"), 100, "King's Indian Defense: Orthodox")

        registerLine(listOf("d2d4", "g8f6", "c2c4", "e7e6"), 45, "Indian Defense: Nimzo/Queen's")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "e7e6", "g1f3"), 50, "Queen's Indian Defense")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "e7e6", "b1c3"), 40, "Nimzo-Indian Defense Prep")
        registerLine(listOf("d2d4", "g8f6", "c2c4", "e7e6", "b1c3", "f8b4"), 100, "Nimzo-Indian Defense")
    }

    private fun registerLine(moves: List<String>, weight: Int, openingName: String) {
        if (moves.isEmpty()) return
        val tempState = BoardState()
        tempState.loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

        // Play up to the last move's position
        for (i in 0 until moves.size - 1) {
            val uci = moves[i]
            val legals = MoveGenerator.generateLegalMoves(tempState)
            val matched = legals.find { it.toUci() == uci } ?: return
            tempState.makeMove(matched)
        }

        // The position before the last move is our key position
        val keyFen = normalizeFen(tempState.toFen())
        val nextMoveUci = moves.last()

        val list = book.getOrPut(keyFen) { ArrayList() }
        val existing = list.find { it.uci == nextMoveUci }
        if (existing != null) {
            list.remove(existing)
            list.add(BookMove(nextMoveUci, maxOf(existing.weight, weight), openingName))
        } else {
            list.add(BookMove(nextMoveUci, weight, openingName))
        }
    }

    private fun normalizeFen(fen: String): String {
        val parts = fen.trim().split("\\s+".toRegex())
        if (parts.size >= 4) {
            return "${parts[0]} ${parts[1]} ${parts[2]} ${parts[3]}"
        }
        return fen
    }

    fun hasBookMove(state: BoardState): Boolean {
        val key = normalizeFen(state.toFen())
        return book.containsKey(key)
    }

    fun getAvailableBookMoves(state: BoardState): List<BookMove> {
        val key = normalizeFen(state.toFen())
        return book[key] ?: emptyList()
    }

    /**
     * Looks up and returns a random weighted book move object if available.
     */
    fun selectBookMove(state: BoardState): Move? {
        val key = normalizeFen(state.toFen())
        val bookMoves = book[key] ?: return null
        if (bookMoves.isEmpty()) return null

        val legals = MoveGenerator.generateLegalMoves(state)
        val validBookMoves = bookMoves.filter { bm -> legals.any { it.toUci() == bm.uci } }
        if (validBookMoves.isEmpty()) return null

        // Weighted random selection
        val totalWeight = validBookMoves.sumOf { it.weight }
        if (totalWeight <= 0) return null

        var r = Random.nextInt(totalWeight)
        for (bm in validBookMoves) {
            r -= bm.weight
            if (r < 0) {
                val matchedMove = legals.find { it.toUci() == bm.uci }
                if (matchedMove != null) {
                    return matchedMove
                }
            }
        }
        return null
    }
}
