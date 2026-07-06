package com.example.chess.engine.search

import com.example.chess.engine.moves.Move

class TranspositionTable(size: Int = 131072) { // 2^17 entries is approx 2.5MB, highly efficient
    
    companion object {
        const val EXACT = 0
        const val ALPHA = 1 // Upper bound (fail-low)
        const val BETA = 2  // Lower bound (fail-high)
    }

    data class Entry(
        var key: Long = 0L,
        var value: Int = 0,
        var depth: Int = -1,
        var flag: Int = -1,
        var bestMove: Move = Move.NULL
    )

    private val table = Array(size) { Entry() }
    private val mask = size - 1

    fun clear() {
        for (entry in table) {
            entry.key = 0L
            entry.value = 0
            entry.depth = -1
            entry.flag = -1
            entry.bestMove = Move.NULL
        }
    }

    fun lookup(key: Long): Entry? {
        val index = (key xor (key ushr 32)).toInt() and mask
        val entry = table[index]
        return if (entry.key == key && entry.flag != -1) entry else null
    }

    fun store(key: Long, value: Int, depth: Int, flag: Int, bestMove: Move) {
        val index = (key xor (key ushr 32)).toInt() and mask
        val entry = table[index]
        
        // Replace strategy: Replace if depth is greater or equal, or if table slot is empty/different position
        if (entry.key == 0L || entry.key != key || depth >= entry.depth) {
            entry.key = key
            entry.value = value
            entry.depth = depth
            entry.flag = flag
            entry.bestMove = bestMove
        }
    }
}
