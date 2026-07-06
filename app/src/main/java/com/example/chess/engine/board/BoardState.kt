package com.example.chess.engine.board

import com.example.chess.engine.EngineConstants
import com.example.chess.engine.EngineConstants.WHITE
import com.example.chess.engine.EngineConstants.BLACK
import com.example.chess.engine.EngineConstants.BOTH
import com.example.chess.engine.EngineConstants.EMPTY
import com.example.chess.engine.EngineConstants.PAWN
import com.example.chess.engine.EngineConstants.KNIGHT
import com.example.chess.engine.EngineConstants.BISHOP
import com.example.chess.engine.EngineConstants.ROOK
import com.example.chess.engine.EngineConstants.QUEEN
import com.example.chess.engine.EngineConstants.KING
import com.example.chess.engine.EngineConstants.W_PAWN
import com.example.chess.engine.EngineConstants.W_KNIGHT
import com.example.chess.engine.EngineConstants.W_BISHOP
import com.example.chess.engine.EngineConstants.W_ROOK
import com.example.chess.engine.EngineConstants.W_QUEEN
import com.example.chess.engine.EngineConstants.W_KING
import com.example.chess.engine.EngineConstants.B_PAWN
import com.example.chess.engine.EngineConstants.B_KNIGHT
import com.example.chess.engine.EngineConstants.B_BISHOP
import com.example.chess.engine.EngineConstants.B_ROOK
import com.example.chess.engine.EngineConstants.B_QUEEN
import com.example.chess.engine.EngineConstants.B_KING
import com.example.chess.engine.EngineConstants.CASTLE_WK
import com.example.chess.engine.EngineConstants.CASTLE_WQ
import com.example.chess.engine.EngineConstants.CASTLE_BK
import com.example.chess.engine.EngineConstants.CASTLE_BQ
import com.example.chess.engine.moves.Move
import com.example.chess.engine.moves.AttackTables

data class UndoState(
    val castlingRights: Int,
    val enPassant: Int,
    val halfMoveClock: Int,
    val capturedPiece: Int,
    val hash: Long
)

class BoardState {
    val board = IntArray(64)
    var sideToMove: Int = WHITE
    var castlingRights: Int = 0
    var enPassant: Int = -1
    var halfMoveClock: Int = 0
    var fullMoveNumber: Int = 1
    var hash: Long = 0L

    val undoStack = ArrayList<UndoState>()
    val repetitionHistory = ArrayList<Long>()

    // Precalculated castling updates
    private val castlingRightsMask = IntArray(64) { 15 }

    init {
        // Setup masks
        castlingRightsMask[EngineConstants.E1] = 15 xor (CASTLE_WK or CASTLE_WQ)
        castlingRightsMask[EngineConstants.H1] = 15 xor CASTLE_WK
        castlingRightsMask[EngineConstants.A1] = 15 xor CASTLE_WQ
        castlingRightsMask[EngineConstants.E8] = 15 xor (CASTLE_BK or CASTLE_BQ)
        castlingRightsMask[EngineConstants.H8] = 15 xor CASTLE_BK
        castlingRightsMask[EngineConstants.A8] = 15 xor CASTLE_BQ
        
        loadFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    }

    fun copyFrom(other: BoardState) {
        System.arraycopy(other.board, 0, this.board, 0, 64)
        this.sideToMove = other.sideToMove
        this.castlingRights = other.castlingRights
        this.enPassant = other.enPassant
        this.halfMoveClock = other.halfMoveClock
        this.fullMoveNumber = other.fullMoveNumber
        this.hash = other.hash
        this.undoStack.clear()
        this.undoStack.addAll(other.undoStack)
        this.repetitionHistory.clear()
        this.repetitionHistory.addAll(other.repetitionHistory)
    }

    fun getOccupancy(color: Int): Long {
        var bb = 0L
        for (sq in 0..63) {
            val p = board[sq]
            if (p != EMPTY) {
                if (color == BOTH || EngineConstants.colorOf(p) == color) {
                    bb = bb or (1L shl sq)
                }
            }
        }
        return bb
    }

    fun computeHash(): Long {
        var h = 0L
        for (sq in 0..63) {
            val p = board[sq]
            if (p != EMPTY) {
                h = h xor Zobrist.pieceKeys[p][sq]
            }
        }
        if (sideToMove == BLACK) {
            h = h xor Zobrist.sideKey
        }
        h = h xor Zobrist.castleKeys[castlingRights]
        if (enPassant != -1) {
            h = h xor Zobrist.enPassantKeys[enPassant]
        }
        return h
    }

    fun loadFen(fen: String) {
        undoStack.clear()
        repetitionHistory.clear()
        
        val parts = fen.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return

        // 1. Piece placement
        val ranks = parts[0].split("/")
        for (r in 0..7) {
            val rankStr = ranks[r]
            var f = 0
            val boardRank = 7 - r // FEN goes 8th rank to 1st rank
            for (char in rankStr) {
                if (char.isDigit()) {
                    val emptySquares = char.toString().toInt()
                    for (i in 0 until emptySquares) {
                        board[boardRank * 8 + f] = EMPTY
                        f++
                    }
                } else {
                    val sq = boardRank * 8 + f
                    board[sq] = when (char) {
                        'P' -> W_PAWN
                        'N' -> W_KNIGHT
                        'B' -> W_BISHOP
                        'R' -> W_ROOK
                        'Q' -> W_QUEEN
                        'K' -> W_KING
                        'p' -> B_PAWN
                        'n' -> B_KNIGHT
                        'b' -> B_BISHOP
                        'r' -> B_ROOK
                        'q' -> B_QUEEN
                        'k' -> B_KING
                        else -> EMPTY
                    }
                    f++
                }
            }
        }

        // 2. Active color
        sideToMove = if (parts.size > 1 && parts[1] == "b") BLACK else WHITE

        // 3. Castling rights
        castlingRights = 0
        if (parts.size > 2) {
            val castling = parts[2]
            if (castling.contains("K")) castlingRights = castlingRights or CASTLE_WK
            if (castling.contains("Q")) castlingRights = castlingRights or CASTLE_WQ
            if (castling.contains("k")) castlingRights = castlingRights or CASTLE_BK
            if (castling.contains("q")) castlingRights = castlingRights or CASTLE_BQ
        }

        // 4. En passant target
        enPassant = -1
        if (parts.size > 3 && parts[3] != "-") {
            val sqName = parts[3]
            val f = sqName[0] - 'a'
            val r = sqName[1] - '1'
            enPassant = r * 8 + f
        }

        // 5. Halfmove clock
        halfMoveClock = if (parts.size > 4) parts[4].toIntOrNull() ?: 0 else 0

        // 6. Fullmove number
        fullMoveNumber = if (parts.size > 5) parts[5].toIntOrNull() ?: 1 else 1

        hash = computeHash()
    }

    fun toFen(): String {
        val fenBuilder = StringBuilder()
        for (r in 7 downTo 0) {
            var emptyCount = 0
            for (f in 0..7) {
                val p = board[r * 8 + f]
                if (p == EMPTY) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        fenBuilder.append(emptyCount)
                        emptyCount = 0
                    }
                    fenBuilder.append(when (p) {
                        W_PAWN -> 'P'
                        W_KNIGHT -> 'N'
                        W_BISHOP -> 'B'
                        W_ROOK -> 'R'
                        W_QUEEN -> 'Q'
                        W_KING -> 'K'
                        B_PAWN -> 'p'
                        B_KNIGHT -> 'n'
                        B_BISHOP -> 'b'
                        B_ROOK -> 'r'
                        B_QUEEN -> 'q'
                        B_KING -> 'k'
                        else -> '?'
                    })
                }
            }
            if (emptyCount > 0) {
                fenBuilder.append(emptyCount)
            }
            if (r > 0) fenBuilder.append("/")
        }

        fenBuilder.append(" ")
        fenBuilder.append(if (sideToMove == WHITE) "w" else "b")

        fenBuilder.append(" ")
        var castling = ""
        if ((castlingRights and CASTLE_WK) != 0) castling += "K"
        if ((castlingRights and CASTLE_WQ) != 0) castling += "Q"
        if ((castlingRights and CASTLE_BK) != 0) castling += "k"
        if ((castlingRights and CASTLE_BQ) != 0) castling += "q"
        fenBuilder.append(if (castling.isEmpty()) "-" else castling)

        fenBuilder.append(" ")
        fenBuilder.append(if (enPassant != -1) EngineConstants.SQUARE_NAMES[enPassant] else "-")

        fenBuilder.append(" $halfMoveClock $fullMoveNumber")
        return fenBuilder.toString()
    }

    fun makeMove(move: Move): Boolean {
        val from = move.from
        val to = move.to
        val pType = move.pieceType
        val promoType = move.promoType
        val isCapture = move.isCapture
        val isDoublePush = move.isDoublePush
        val isEnPassant = move.isEnPassant
        val isCastling = move.isCastling

        val movingPiece = board[from]
        val originalSide = sideToMove

        // 1. Save Undo details
        val captured = if (isEnPassant) {
            if (originalSide == WHITE) B_PAWN else W_PAWN
        } else {
            board[to]
        }
        
        undoStack.add(UndoState(castlingRights, enPassant, halfMoveClock, captured, hash))
        repetitionHistory.add(hash)

        // 2. Clear old en passant from hash
        if (enPassant != -1) {
            hash = hash xor Zobrist.enPassantKeys[enPassant]
        }
        // Clear old castling from hash
        hash = hash xor Zobrist.castleKeys[castlingRights]

        // 3. Move piece / Capture
        hash = hash xor Zobrist.pieceKeys[movingPiece][from]
        board[from] = EMPTY

        if (isEnPassant) {
            val capturedSq = if (originalSide == WHITE) to - 8 else to + 8
            hash = hash xor Zobrist.pieceKeys[captured][capturedSq]
            board[capturedSq] = EMPTY
            halfMoveClock = 0
        } else if (isCapture) {
            hash = hash xor Zobrist.pieceKeys[captured][to]
            halfMoveClock = 0
        } else if (pType == PAWN) {
            halfMoveClock = 0
        } else {
            halfMoveClock++
        }

        val placedPiece = if (promoType != EMPTY) {
            EngineConstants.createPiece(originalSide, promoType)
        } else {
            movingPiece
        }
        board[to] = placedPiece
        hash = hash xor Zobrist.pieceKeys[placedPiece][to]

        // 4. Castling execution
        if (isCastling) {
            when (to) {
                EngineConstants.G1 -> { // WK
                    board[EngineConstants.H1] = EMPTY
                    board[EngineConstants.F1] = W_ROOK
                    hash = hash xor Zobrist.pieceKeys[W_ROOK][EngineConstants.H1]
                    hash = hash xor Zobrist.pieceKeys[W_ROOK][EngineConstants.F1]
                }
                EngineConstants.C1 -> { // WQ
                    board[EngineConstants.A1] = EMPTY
                    board[EngineConstants.D1] = W_ROOK
                    hash = hash xor Zobrist.pieceKeys[W_ROOK][EngineConstants.A1]
                    hash = hash xor Zobrist.pieceKeys[W_ROOK][EngineConstants.D1]
                }
                EngineConstants.G8 -> { // BK
                    board[EngineConstants.H8] = EMPTY
                    board[EngineConstants.F8] = B_ROOK
                    hash = hash xor Zobrist.pieceKeys[B_ROOK][EngineConstants.H8]
                    hash = hash xor Zobrist.pieceKeys[B_ROOK][EngineConstants.F8]
                }
                EngineConstants.C8 -> { // BQ
                    board[EngineConstants.A8] = EMPTY
                    board[EngineConstants.D8] = B_ROOK
                    hash = hash xor Zobrist.pieceKeys[B_ROOK][EngineConstants.A8]
                    hash = hash xor Zobrist.pieceKeys[B_ROOK][EngineConstants.D8]
                }
            }
        }

        // 5. Update Castling Rights
        castlingRights = castlingRights and castlingRightsMask[from]
        castlingRights = castlingRights and castlingRightsMask[to]

        // 6. Update En Passant
        if (isDoublePush) {
            enPassant = if (originalSide == WHITE) from + 8 else from - 8
            hash = hash xor Zobrist.enPassantKeys[enPassant]
        } else {
            enPassant = -1
        }

        // 7. Update side to move
        sideToMove = originalSide xor 1
        hash = hash xor Zobrist.sideKey

        // Re-xor castling rights back into hash
        hash = hash xor Zobrist.castleKeys[castlingRights]

        // 8. Update Move number
        if (originalSide == BLACK) {
            fullMoveNumber++
        }

        // 9. King Safety validation
        val kingSq = findKing(originalSide)
        val occupied = getOccupancy(BOTH)
        if (kingSq != -1 && AttackTables.isSquareAttacked(kingSq, sideToMove, occupied, board)) {
            unmakeMove(move)
            return false
        }

        return true
    }

    fun unmakeMove(move: Move) {
        val from = move.from
        val to = move.to
        val pType = move.pieceType
        val promoType = move.promoType
        val isCapture = move.isCapture
        val isDoublePush = move.isDoublePush
        val isEnPassant = move.isEnPassant
        val isCastling = move.isCastling

        // 1. Revert side to move
        sideToMove = sideToMove xor 1

        val undo = undoStack.removeAt(undoStack.size - 1)
        repetitionHistory.removeAt(repetitionHistory.size - 1)

        // 2. Restore history variables
        castlingRights = undo.castlingRights
        enPassant = undo.enPassant
        halfMoveClock = undo.halfMoveClock
        hash = undo.hash

        if (sideToMove == BLACK) {
            fullMoveNumber--
        }

        // 3. Revert piece placement
        val restoredPiece = if (promoType != EMPTY) {
            EngineConstants.createPiece(sideToMove, PAWN)
        } else {
            board[to]
        }

        board[from] = restoredPiece

        if (isEnPassant) {
            board[to] = EMPTY
            val capturedSq = if (sideToMove == WHITE) to - 8 else to + 8
            board[capturedSq] = undo.capturedPiece
        } else if (isCapture) {
            board[to] = undo.capturedPiece
        } else {
            board[to] = EMPTY
        }

        // 4. Revert castling
        if (isCastling) {
            when (to) {
                EngineConstants.G1 -> {
                    board[EngineConstants.F1] = EMPTY
                    board[EngineConstants.H1] = W_ROOK
                }
                EngineConstants.C1 -> {
                    board[EngineConstants.D1] = EMPTY
                    board[EngineConstants.A1] = W_ROOK
                }
                EngineConstants.G8 -> {
                    board[EngineConstants.F8] = EMPTY
                    board[EngineConstants.H8] = B_ROOK
                }
                EngineConstants.C8 -> {
                    board[EngineConstants.D8] = EMPTY
                    board[EngineConstants.A8] = B_ROOK
                }
            }
        }
    }

    fun findKing(color: Int): Int {
        val kingCode = if (color == WHITE) W_KING else B_KING
        for (sq in 0..63) {
            if (board[sq] == kingCode) return sq
        }
        return -1
    }

    fun isDrawByFiftyMoves(): Boolean = halfMoveClock >= 100 // 50 full moves (100 half-moves)

    fun isDrawByRepetition(): Boolean {
        if (repetitionHistory.isEmpty()) return false
        var count = 1
        for (i in 0 until repetitionHistory.size - 1) {
            if (repetitionHistory[i] == hash) {
                count++
                if (count >= 3) return true
            }
        }
        return false
    }

    fun isSquareAttacked(sq: Int, byColor: Int): Boolean {
        return AttackTables.isSquareAttacked(sq, byColor, getOccupancy(BOTH), board)
    }
}
