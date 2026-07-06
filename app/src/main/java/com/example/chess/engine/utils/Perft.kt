package com.example.chess.engine.utils

import com.example.chess.engine.board.BoardState
import com.example.chess.engine.moves.MoveGenerator

object Perft {
    fun runPerft(state: BoardState, depth: Int): Long {
        if (depth == 0) return 1L
        val moves = MoveGenerator.generateLegalMoves(state)
        var totalNodes = 0L
        for (move in moves) {
            state.makeMove(move)
            val nodes = runPerft(state, depth - 1)
            totalNodes += nodes
            state.unmakeMove(move)
        }
        return totalNodes
    }

    fun runDivide(state: BoardState, depth: Int): List<String> {
        if (depth <= 0) return emptyList()
        val results = ArrayList<String>()
        val moves = MoveGenerator.generateLegalMoves(state)
        var grandTotal = 0L
        for (move in moves) {
            state.makeMove(move)
            val nodes = runPerft(state, depth - 1)
            grandTotal += nodes
            results.add("${move.toUci()}: $nodes")
            state.unmakeMove(move)
        }
        results.add("\nTotal Nodes: $grandTotal")
        return results
    }
}
