package com.example.chess.engine

object EngineConstants {
    // Colors
    const val WHITE = 0
    const val BLACK = 1
    const val BOTH = 2

    // Piece Types
    const val EMPTY = 0
    const val PAWN = 1
    const val KNIGHT = 2
    const val BISHOP = 3
    const val ROOK = 4
    const val QUEEN = 5
    const val KING = 6

    // Colored Pieces
    const val W_PAWN = 1
    const val W_KNIGHT = 2
    const val W_BISHOP = 3
    const val W_ROOK = 4
    const val W_QUEEN = 5
    const val W_KING = 6

    const val B_PAWN = 7
    const val B_KNIGHT = 8
    const val B_BISHOP = 9
    const val B_ROOK = 10
    const val B_QUEEN = 11
    const val B_KING = 12

    // Squares
    const val A1 = 0; const val B1 = 1; const val C1 = 2; const val D1 = 3; const val E1 = 4; const val F1 = 5; const val G1 = 6; const val H1 = 7
    const val A2 = 8; const val B2 = 9; const val C2 = 10; const val D2 = 11; const val E2 = 12; const val F2 = 13; const val G2 = 14; const val H2 = 15
    const val A7 = 48; const val B7 = 49; const val C7 = 50; const val D7 = 51; const val E7 = 52; const val F7 = 53; const val G7 = 54; const val H7 = 55
    const val A8 = 56; const val B8 = 57; const val C8 = 58; const val D8 = 59; const val E8 = 60; const val F8 = 61; const val G8 = 62; const val H8 = 63
    
    // Castling Rights (using bit flags)
    const val CASTLE_WK = 1  // White King side
    const val CASTLE_WQ = 2  // White Queen side
    const val CASTLE_BK = 4  // Black King side
    const val CASTLE_BQ = 8  // Black Queen side

    val SQUARE_NAMES = arrayOf(
        "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
        "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
        "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
        "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
        "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
        "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
        "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
        "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"
    )

    fun colorOf(piece: Int): Int {
        if (piece == EMPTY) return BOTH
        return if (piece in W_PAWN..W_KING) WHITE else BLACK
    }

    fun typeOf(piece: Int): Int {
        if (piece == EMPTY) return EMPTY
        return if (piece <= W_KING) piece else piece - 6
    }

    fun createPiece(color: Int, type: Int): Int {
        if (type == EMPTY) return EMPTY
        return if (color == WHITE) type else type + 6
    }
}
