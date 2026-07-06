package com.example.chess.engine.utils

import com.example.chess.engine.board.BoardState
import com.example.chess.engine.search.SearchEngine
import com.example.chess.engine.search.SearchEngine.SearchListener
import com.example.chess.engine.moves.Move

object Benchmark {
    data class BenchmarkResult(
        val totalNodes: Int,
        val timeMs: Long,
        val nps: Int
    )

    private val benchmarkPositions = arrayOf(
        // 1. Starting position
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        // 2. Kiwipete (Tricky tactical middlegame)
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
        // 3. Endgame position with passed pawns
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"
    )

    fun runBenchmark(depth: Int = 4): BenchmarkResult {
        val searcher = SearchEngine()
        var combinedNodes = 0
        val startTime = System.currentTimeMillis()

        for (fen in benchmarkPositions) {
            val state = BoardState()
            state.loadFen(fen)
            searcher.search(state, depth, null)
            combinedNodes += searcher.nodes
        }

        val elapsed = System.currentTimeMillis() - startTime
        val safeElapsed = if (elapsed == 0L) 1L else elapsed
        val nps = ((combinedNodes.toDouble() / safeElapsed) * 1000).toInt()

        return BenchmarkResult(combinedNodes, safeElapsed, nps)
    }
}
