package com.example.chess.engine.moves

import com.example.chess.engine.EngineConstants

/**
 * Compact Move representation in a single Int (32-bit):
 * - Bits 0-5: Source square (from)
 * - Bits 6-11: Target square (to)
 * - Bits 12-14: Piece type moving (1 to 6)
 * - Bits 15-17: Promotion piece type (0 for none, or 2=KNIGHT, 3=BISHOP, 4=ROOK, 5=QUEEN)
 * - Bit 18: Capture flag
 * - Bit 19: Double pawn push flag
 * - Bit 20: En passant flag
 * - Bit 21: Castling flag
 */
@JvmInline
value class Move(val value: Int) {
    val from: Int get() = value and 0x3F
    val to: Int get() = (value shr 6) and 0x3F
    val pieceType: Int get() = (value shr 12) and 0x07
    val promoType: Int get() = (value shr 15) and 0x07
    val isCapture: Boolean get() = (value and (1 shl 18)) != 0
    val isDoublePush: Boolean get() = (value and (1 shl 19)) != 0
    val isEnPassant: Boolean get() = (value and (1 shl 20)) != 0
    val isCastling: Boolean get() = (value and (1 shl 21)) != 0

    fun toUci(): String {
        if (value == 0) return "0000"
        val fromName = EngineConstants.SQUARE_NAMES[from]
        val toName = EngineConstants.SQUARE_NAMES[to]
        val promoName = when (promoType) {
            EngineConstants.KNIGHT -> "n"
            EngineConstants.BISHOP -> "b"
            EngineConstants.ROOK -> "r"
            EngineConstants.QUEEN -> "q"
            else -> ""
        }
        return "$fromName$toName$promoName"
    }

    override fun toString(): String = toUci()

    companion object {
        val NULL = Move(0)

        fun create(
            from: Int,
            to: Int,
            pieceType: Int,
            promoType: Int = 0,
            isCapture: Boolean = false,
            isDoublePush: Boolean = false,
            isEnPassant: Boolean = false,
            isCastling: Boolean = false
        ): Move {
            var v = from or (to shl 6) or (pieceType shl 12) or (promoType shl 15)
            if (isCapture) v = v or (1 shl 18)
            if (isDoublePush) v = v or (1 shl 19)
            if (isEnPassant) v = v or (1 shl 20)
            if (isCastling) v = v or (1 shl 21)
            return Move(v)
        }
    }
}
