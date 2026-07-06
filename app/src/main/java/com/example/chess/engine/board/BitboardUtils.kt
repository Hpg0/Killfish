package com.example.chess.engine.board

object BitboardUtils {
    @JvmStatic
    fun setBit(bb: Long, sq: Int): Long = bb or (1L shl sq)

    @JvmStatic
    fun clearBit(bb: Long, sq: Int): Long = bb and (1L shl sq).inv()

    @JvmStatic
    fun getBit(bb: Long, sq: Int): Boolean = (bb and (1L shl sq)) != 0L

    @JvmStatic
    fun countBits(bb: Long): Int = java.lang.Long.bitCount(bb)

    @JvmStatic
    fun getLSB(bb: Long): Int = java.lang.Long.numberOfTrailingZeros(bb)

    @JvmStatic
    fun popLSB(bb: Long): Pair<Int, Long> {
        val sq = getLSB(bb)
        return Pair(sq, bb and (bb - 1))
    }
}
