package com.example.chess.engine.utils

import com.example.chess.engine.board.BoardState
import com.example.chess.engine.moves.Move
import com.example.chess.engine.moves.MoveGenerator

object PgnParser {

    fun generatePgn(
        event: String = "Casual Game",
        site: String = "Kill Fish Android",
        date: String = "2026.07.04",
        whitePlayer: String = "Player",
        blackPlayer: String = "Kill Fish Engine",
        result: String = "*",
        initialFen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        playedMoves: List<Move>
    ): String {
        val sb = StringBuilder()
        sb.append("[Event \"$event\"]\n")
        sb.append("[Site \"$site\"]\n")
        sb.append("[Date \"$date\"]\n")
        sb.append("[White \"$whitePlayer\"]\n")
        sb.append("[Black \"$blackPlayer\"]\n")
        sb.append("[Result \"$result\"]\n")
        if (initialFen != "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") {
            sb.append("[SetUp \"1\"]\n")
            sb.append("[FEN \"$initialFen\"]\n")
        }
        sb.append("\n")

        var moveNum = 1
        for (i in playedMoves.indices) {
            val move = playedMoves[i]
            if (i % 2 == 0) {
                sb.append("$moveNum. ")
                moveNum++
            }
            sb.append("${move.toUci()} ")
        }
        sb.append(result)
        return sb.toString()
    }

    /**
     * Reconstructs played moves and BoardState from space-separated UCI moves.
     */
    fun reconstructFromUciMoves(initialFen: String, uciMovesString: String): Pair<BoardState, List<Move>> {
        val state = BoardState()
        state.loadFen(initialFen)
        
        val movesList = ArrayList<Move>()
        val tokens = uciMovesString.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        
        for (token in tokens) {
            val legals = MoveGenerator.generateLegalMoves(state)
            val matchedMove = legals.find { it.toUci() == token } ?: break
            
            state.makeMove(matchedMove)
            movesList.add(matchedMove)
        }
        return Pair(state, movesList)
    }

    /**
     * Reconstructs played moves and BoardState from standard PGN string (with comments, tags, move numbers, and SAN notation).
     */
    fun parsePgn(pgnString: String): Pair<BoardState, List<Move>> {
        val state = BoardState()
        var initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        
        // Extract FEN if specified in [FEN "f..."] header
        val fenRegex = "\\[FEN\\s+\"([^\"]+)\"\\]".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = fenRegex.find(pgnString)
        if (matchResult != null) {
            initialFen = matchResult.groupValues[1]
        }
        state.loadFen(initialFen)
        
        // Remove headers
        var body = pgnString.lines()
            .filter { !it.trim().startsWith("[") }
            .joinToString(" ")
            
        // Remove comments { ... } and ( ... )
        body = body.replace("\\{[^}]*\\}".toRegex(), "")
        body = body.replace("\\([^)]*\\)".toRegex(), "")
        
        // Remove move numbers like "1." or "1..." or "12."
        body = body.replace("\\d+\\.+".toRegex(), " ")
        
        // Remove result markers
        body = body.replace("1-0", " ")
        body = body.replace("0-1", " ")
        body = body.replace("1/2-1/2", " ")
        body = body.replace("*", " ")
        
        val tokens = body.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val movesList = ArrayList<Move>()
        
        for (token in tokens) {
            val legals = MoveGenerator.generateLegalMoves(state)
            val matchedMove = findMatchingMove(state, legals, token) ?: break
            
            state.makeMove(matchedMove)
            movesList.add(matchedMove)
        }
        return Pair(state, movesList)
    }

    private fun findMatchingMove(state: BoardState, legals: List<Move>, token: String): Move? {
        val cleanToken = token.trim().replace("+", "").replace("#", "").replace("?", "").replace("!", "")
        if (cleanToken.isEmpty()) return null
        
        // 1. Try UCI matching directly
        val uciMatch = legals.find { it.toUci() == cleanToken.lowercase() }
        if (uciMatch != null) return uciMatch
        
        // 2. Castling matching
        if (cleanToken == "O-O" || cleanToken == "0-0") {
            return legals.find { it.isCastling && (it.to == 6 || it.to == 62) }
        }
        if (cleanToken == "O-O-O" || cleanToken == "0-0-0") {
            return legals.find { it.isCastling && (it.to == 2 || it.to == 58) }
        }
        
        // 3. SAN matching
        // Extract piece type
        val firstChar = cleanToken[0]
        val pieceType = when (firstChar) {
            'K' -> 6
            'Q' -> 5
            'R' -> 4
            'B' -> 3
            'N' -> 2
            else -> 1 // Pawn
        }
        
        // Parse promotion
        var promoType = 0
        var tokenWithoutPromo = cleanToken
        if (cleanToken.contains("=")) {
            val parts = cleanToken.split("=")
            tokenWithoutPromo = parts[0]
            if (parts.size > 1 && parts[1].isNotEmpty()) {
                promoType = when (parts[1][0]) {
                    'Q' -> 5
                    'R' -> 4
                    'B' -> 3
                    'N' -> 2
                    else -> 0
                }
            }
        } else if (pieceType == 1 && cleanToken.length >= 3 && (cleanToken.endsWith("Q") || cleanToken.endsWith("R") || cleanToken.endsWith("B") || cleanToken.endsWith("N"))) {
            val lastChar = cleanToken.last()
            promoType = when (lastChar) {
                'Q' -> 5
                'R' -> 4
                'B' -> 3
                'N' -> 2
                else -> 0
            }
            tokenWithoutPromo = cleanToken.substring(0, cleanToken.length - 1)
        }
        
        // The destination square is the last 2 characters of the remaining token (excluding promotion)
        if (tokenWithoutPromo.length < 2) return null
        val destStr = tokenWithoutPromo.takeLast(2)
        val destSq = com.example.chess.engine.EngineConstants.SQUARE_NAMES.indexOf(destStr)
        if (destSq == -1) return null
        
        // Disambiguation
        // The remaining characters between the starting piece letter (if any) and the destSq name
        val coreToken = if (pieceType == 1) tokenWithoutPromo else tokenWithoutPromo.substring(1)
        // Strip 'x' (captures)
        val disambigPart = coreToken.dropLast(2).replace("x", "")
        
        var depFile: Char? = null
        var depRank: Char? = null
        for (char in disambigPart) {
            if (char in 'a'..'h') depFile = char
            if (char in '1'..'8') depRank = char
        }
        
        return legals.find { move ->
            if (move.to != destSq) return@find false
            if (move.pieceType != pieceType) return@find false
            if (promoType != 0 && move.promoType != promoType) return@find false
            
            val depSqName = com.example.chess.engine.EngineConstants.SQUARE_NAMES[move.from]
            if (depFile != null && depSqName[0] != depFile) return@find false
            if (depRank != null && depSqName[1] != depRank) return@find false
            
            true
        }
    }
}
