package com.example.chess.utils

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiChessEngine {
    private const val TAG = "GeminiChessEngine"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    data class MoveResult(
        val bestMove: String?,
        val reasoning: String?,
        val success: Boolean,
        val errorMessage: String? = null
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getBestMove(
        fen: String,
        legalMoves: List<String>,
        moveHistory: String
    ): MoveResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return MoveResult(
                bestMove = null,
                reasoning = null,
                success = false,
                errorMessage = "API Key is not configured in the AI Studio Secrets panel. Please enter your GEMINI_API_KEY."
            )
        }

        val systemInstruction = """
            You are Stockfish 18, the strongest chess engine in existence (Elo 3500+). 
            Your goal is to defeat your opponent with grandmaster-level tactical precision and strategy.
            
            You will receive:
            1. Current board position in Forsyth-Edwards Notation (FEN).
            2. History of moves played in the current match (in UCI format).
            3. A list of all strictly LEGAL moves available to you in the current position (in UCI format).
            
            Your absolute instructions:
            - You MUST evaluate the position and choose the single absolute best move.
            - The move you choose MUST be one of the moves from the provided List of Legal Moves. NEVER select any move that is not in that list!
            - You MUST respond in valid JSON format with exactly two fields: "best_move" and "reasoning".
            
            JSON format:
            {
              "best_move": "selected_move_from_legal_list",
              "reasoning": "A concise, elite grandmaster strategic or tactical justification for this move (1-2 sentences)."
            }
        """.trimIndent()

        val prompt = """
            Current Position (FEN):
            $fen
            
            Match Move History:
            $moveHistory
            
            Strictly LEGAL Moves Available (You MUST pick one of these):
            ${legalMoves.joinToString(", ")}
            
            Analyze carefully and pick the absolute best move.
        """.trimIndent()

        try {
            val contentsJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            }

            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            }

            val generationConfigJson = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            }

            val payloadJson = JSONObject().apply {
                put("contents", JSONArray().apply { put(contentsJson) })
                put("systemInstruction", systemInstructionJson)
                put("generationConfig", generationConfigJson)
            }

            val requestBodyString = payloadJson.toString()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = requestBodyString.toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    val errMsg = "HTTP ${response.code}: ${response.message}\n$body"
                    Log.e(TAG, errMsg)
                    return MoveResult(null, null, false, "Network request failed: HTTP ${response.code}")
                }

                val jsonResponse = JSONObject(body)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return MoveResult(null, null, false, "No response candidates returned by Gemini.")
                }

                val content = candidates.getJSONObject(0).optJSONObject("content")
                if (content == null) {
                    return MoveResult(null, null, false, "Candidate content is missing.")
                }

                val parts = content.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    return MoveResult(null, null, false, "Candidate parts are missing.")
                }

                val rawText = parts.getJSONObject(0).optString("text")?.trim()
                if (rawText.isNullOrEmpty()) {
                    return MoveResult(null, null, false, "Text response from Gemini is empty.")
                }

                // Parse the inner JSON structure
                val innerJson = JSONObject(rawText)
                val bestMove = innerJson.optString("best_move")?.trim()?.lowercase()
                val reasoning = innerJson.optString("reasoning")?.trim()

                if (bestMove.isNullOrEmpty()) {
                    return MoveResult(null, null, false, "JSON parsed successfully but 'best_move' is missing.")
                }

                // Verify legality of move
                if (!legalMoves.contains(bestMove)) {
                    Log.w(TAG, "Gemini selected move '$bestMove' which is not in the legal moves list: $legalMoves")
                    return MoveResult(
                        bestMove = legalMoves.firstOrNull(),
                        reasoning = "Tactical fallback move chosen to preserve match legality.",
                        success = true
                    )
                }

                return MoveResult(
                    bestMove = bestMove,
                    reasoning = reasoning ?: "Position analyzed strategically.",
                    success = true
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini chess search", e)
            return MoveResult(null, null, false, e.localizedMessage ?: "Unknown exception occurred.")
        }
    }
}
