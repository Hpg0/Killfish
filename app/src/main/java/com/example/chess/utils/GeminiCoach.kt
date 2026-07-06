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

object GeminiCoach {
    private const val TAG = "GeminiCoach"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun askCoach(
        currentFen: String,
        history: List<Pair<String, String>>, // Pair(sender, text)
        query: String,
        wins: Int = 0,
        losses: Int = 0,
        draws: Int = 0,
        gamesPlayed: Int = 0,
        benchmarkNps: Int = 0
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getLocalCoachResponse(query, currentFen, wins, losses, draws, gamesPlayed, benchmarkNps)
        }

        val performanceSummary = if (gamesPlayed > 0) {
            "Player stats: $wins wins, $losses losses, $draws draws out of $gamesPlayed games. Engine benchmark: $benchmarkNps NPS."
        } else {
            "Player stats: Brand new user with 0 games played."
        }

        val systemInstruction = """
            You are "KillFish AI Coach", an elite, grandmaster-level chess coach and mentor. 
            Your tone is engaging, futuristic, encouraging, and highly educational. 
            You are speaking to a player using the KillFish Chess app.
            
            Current User Strength & Background:
            $performanceSummary
            
            Please adapt your advice, technical jargon, and study plans to match this player's playing strength.
            - If they have a high win rate, challenge them with advanced grandmaster concepts.
            - If they are losing more games, focus on fundamental tactical safety, blunder checks, and simple visual plans.
            - Reference their current stats to encourage them or set custom weekly study goals!
            
            You have access to:
            1. The current board position in FEN: $currentFen
            2. The player's questions and conversational history.
            
            Provide deep, master-class responses. Offer structured, bulleted lists if explaining plans.
            Always focus on helping the player improve. Keep explanations clear, tactical, and intuitive.
        """.trimIndent()

        try {
            val contentsArray = JSONArray()

            // Add history
            for (chat in history) {
                val role = if (chat.first == "user") "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", chat.second) })
                    })
                })
            }

            // Add active query
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", query) })
                })
            })

            val systemInstructionJson = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            }

            val payloadJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionJson)
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
                    return "My connection to the KillFish neural node was briefly interrupted. Here is some local chess advice: ${getLocalCoachResponse(query, currentFen, wins, losses, draws, gamesPlayed, benchmarkNps)}"
                }

                val jsonResponse = JSONObject(body)
                val candidates = jsonResponse.optJSONArray("candidates") ?: return "Error parsing neural response."
                if (candidates.length() == 0) return "No response candidates returned by KillFish."

                val content = candidates.getJSONObject(0).optJSONObject("content") ?: return "Candidate content missing."
                val parts = content.optJSONArray("parts") ?: return "Content parts missing."
                if (parts.length() == 0) return "Parts are empty."

                return parts.getJSONObject(0).optString("text") ?: "No text content returned."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in askCoach", e)
            return "Neural network lookup failed. Local fallback response: ${getLocalCoachResponse(query, currentFen, wins, losses, draws, gamesPlayed, benchmarkNps)}"
        }
    }

    fun getDirectReply(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Offline mode: API Key is unconfigured."
        }
        try {
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = payload.toString().toRequestBody(mediaType)
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return "Empty response body."
                val json = JSONObject(body)
                val cand = json.optJSONArray("candidates") ?: return "Error parsing candidate list."
                if (cand.length() == 0) return "No response candidates found."
                return cand.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            }
        } catch (e: Exception) {
            return "Error calling neural summarizer: ${e.message}"
        }
    }

    private fun getLocalCoachResponse(
        query: String,
        fen: String,
        wins: Int = 0,
        losses: Int = 0,
        draws: Int = 0,
        gamesPlayed: Int = 0,
        benchmarkNps: Int = 0
    ): String {
        val q = query.lowercase()
        val statsAdaptation = when {
            gamesPlayed == 0 -> "Coach Assessment: Welcome! I see you are brand new to the app. Let's start with basic opening principles."
            wins > losses -> "Coach Assessment: Excellent rating progress ($wins wins out of $gamesPlayed matches)! Your speed profile at $benchmarkNps NPS is solid. We should focus on high-level strategy and master endgames."
            else -> "Coach Assessment: I see you've had a tough series ($losses losses). Let's review fundamental tactical defense, absolute pins, and avoid premature flank pawn advances."
        }

        return when {
            q.contains("opening") || q.contains("start") -> {
                "$statsAdaptation\n\n" +
                "Opening Principles:\n" +
                "1. Control the center squares (e4, d4, e5, d5).\n" +
                "2. Develop your knights before bishops to retain flexibility.\n" +
                "3. Castle early (within 10 moves) to secure your King.\n" +
                "4. Avoid moving the same piece twice in the opening.\n\n" +
                "Try playing the Italian Game (1. e4 e5 2. Nf3 Nc6 3. Bc4) or Queen's Gambit (1. d4 d5 2. c4) for clean positional games!"
            }
            q.contains("tactics") || q.contains("puzzle") || q.contains("fork") || q.contains("pin") -> {
                "Tactics are the foundation of chess victories! Here are the core tactical motifs to look for:\n" +
                "• Pins: Restricting an opponent's piece because moving it would expose a higher-value piece behind it.\n" +
                "• Forks: A single piece (especially Knights!) attacking two or more of your opponent's pieces simultaneously.\n" +
                "• Skewers: An attack on a high-value piece, forcing it to move and exposing a lesser-value piece behind it.\n\n" +
                "Always scan the board for checks, captures, and threats (CCT) on every single turn!"
            }
            q.contains("endgame") || q.contains("mate") || q.contains("pawn") -> {
                "Endgame mastery is what separates amateur players from grandmasters! In the endgame:\n" +
                "1. Activate your King! The King becomes an active attacking piece when queens are traded.\n" +
                "2. Push passed pawns. A passed pawn is a potential new Queen.\n" +
                "3. Keep Rooks active behind passed pawns (on the open files).\n\n" +
                "Practice the King + Rook vs King checkmate pattern. It is vital to learn how to box the opponent's king to the edge of the board."
            }
            q.contains("study") || q.contains("plan") || q.contains("improve") -> {
                "$statsAdaptation\n\n" +
                "Here is your personalized weekly training plan:\n" +
                "• Day 1-3: Solve 10 tactical puzzles daily in our Tactical Puzzles module.\n" +
                "• Day 4-5: Replay Nimzowitsch's interactive books in the AI Chess Library to learn blockade patterns.\n" +
                "• Day 6-7: Play 2 matches against the engine and review the automatic summaries."
            }
            else -> {
                "Greetings! I am the KillFish AI Coach. I am ready to guide you to grandmaster level.\n\n" +
                "$statsAdaptation\n\n" +
                "Ask me about:\n" +
                "• Openings theory or recommended lines\n" +
                "• Strategic concepts like pawn structures and minor piece outposts\n" +
                "• Detailed custom study plans for your skill level\n" +
                "• Solving puzzles and finding tactical combinations\n\n" +
                "Currently, the board position is: `$fen`. Feel free to ask me to analyze it!"
            }
        }
    }
}
