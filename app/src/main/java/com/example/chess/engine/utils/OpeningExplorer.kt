package com.example.chess.engine.utils

object OpeningExplorer {
    data class Opening(
        val name: String,
        val eco: String,
        val uciMoves: List<String>,
        val description: String
    )

    val openingsList = listOf(
        Opening(
            "Sicilian Defense", "B20",
            listOf("e2e4", "c7c5"),
            "The most popular and high-scoring response to 1.e4, leading to asymmetrical, sharp positions."
        ),
        Opening(
            "Ruy Lopez (Spanish Opening)", "C60",
            listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5"),
            "One of the oldest and most thoroughly analyzed openings, emphasizing central control and quick castling."
        ),
        Opening(
            "French Defense", "C00",
            listOf("e2e4", "e7e6"),
            "A solid, counter-attacking opening where Black concedes space but fights for a strong pawn chain."
        ),
        Opening(
            "Caro-Kann Defense", "B12",
            listOf("e2e4", "c7c6"),
            "A robust, hyper-solid alternative to the French, where Black secures a safe home for the light-squared bishop."
        ),
        Opening(
            "Queen's Gambit", "D06",
            listOf("d2d4", "d7d5", "c2c4"),
            "A classic central assault, where White offers a temporary wing pawn to gain control of the central squares."
        ),
        Opening(
            "King's Indian Defense", "E61",
            listOf("d2d4", "g8f6", "c2c4", "g7g6"),
            "A hypermodern opening where Black invites White to build a massive center, intending to counterattack it later."
        ),
        Opening(
            "Italian Game", "C50",
            listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"),
            "Focuses on quick development, control of the center, and attacks on Black's weak f7 square."
        ),
        Opening(
            "Slav Defense", "D10",
            listOf("d2d4", "d7d5", "c2c4", "c7c6"),
            "A rock-solid response to the Queen's Gambit, reinforcing the d5 pawn without blocking the c8 Bishop."
        ),
        Opening(
            "Scandinavian Defense", "B01",
            listOf("e2e4", "d7d5"),
            "An immediate challenge to White's e4 pawn, leading to an open center and rapid queen activation."
        )
    )

    fun getMatchingOpening(playedUciMoves: List<String>): Opening? {
        var bestMatch: Opening? = null
        for (opening in openingsList) {
            if (playedUciMoves.size >= opening.uciMoves.size) {
                val subList = playedUciMoves.subList(0, opening.uciMoves.size)
                if (subList == opening.uciMoves) {
                    // We want the longest matching prefix (most specific opening)
                    if (bestMatch == null || opening.uciMoves.size > bestMatch.uciMoves.size) {
                        bestMatch = opening
                    }
                }
            }
        }
        return bestMatch
    }
}
