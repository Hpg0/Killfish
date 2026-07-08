# 📋 KillFish Chess Applet: Ultimate Feature Checklist & Architectural Backup Plan

This document details the precise implementation checklist and contingency plans for the Chess app upgrade. It serves as our development source-of-truth.

---

## 🛠️ Section 1: Executive Feature Checklist

### 1. Authentication System (Google Sign-In + Guest Mode)
- [ ] **First Launch Guest Defaults**: On pristine app launch, default to Guest mode automatically with zero silent logging or account pre-selection.
- [ ] **Google Sign-In Visual Button**: Embed an authentic "Sign in with Google" button with Material guidelines alongside "Continue as Guest".
- [ ] **Secure Account Chooser**: Render a custom account chooser listing any local simulated Google accounts or allowing the creation/registration of a new Google profile.
- [ ] **Strict Identity Protection**: Ensure Google account selection is completely interactive—never reuse a default Google account without direct consent.
- [ ] **Robust Guest Support**: Retain full capabilities for Guest players (local storage only, skip cloud backup until signed in).

### 2. Player Tag System
- [ ] **Automatic Tag Assignment**: Track and label users as either `Guest`, `New Player`, or `Old Player`.
- [ ] **Premium Trial Engine**: Automatically unlock a secure 10-minute Premium trial immediately upon Google sign-in.
- [ ] **Accurate Time-out Engine**: Poll/verify timestamp boundaries; after exactly 10 minutes of active/inactive time, transition from `New Player` to `Old Player` and gracefully revoke Premium features.
- [ ] **Persistent State**: Save the signup timestamp and current tag in SharedPreferences so it survives app restarts.

### 3. Intelligent Tutorial System
- [ ] **Contextual First-Launch Trigger**: Render the interactive Onboarding Tutorial on first launch for `Guest` and `New Player`.
- [ ] **Smart Old-User Skip**: Automatically bypass onboarding for `Old Player` profiles, routing them straight to the main dashboard.
- [ ] **Replay Mechanism**: Keep the tutorial fully replayable from Settings at any time.

### 4. Chess Trap Analyzer & Explorer
- [ ] **Multimodal Board Parser**: Allow entering FEN string or uploading/parsing a chessboard screenshot.
- [ ] **Neural Position Recognition**: Call Gemini 3.5 Flash to analyze the position, identify any latent tactical traps, and output standard algebraic notation continuation sequences.
- [ ] **Tactical Evaluation Metrics**: Display evaluation scores, names of the tactical patterns, and step-by-step warnings.
- [ ] **History Logs**: Persist parsed positions, FEN lists, and descriptions locally in the Room database so players can review them offline.

### 5. Architectural Clean-Up (Trouble Revocation)
- [ ] **Prune Redundancies**: Audit and delete any duplicate model declarations (e.g., conflicting `ChatMessage` states).
- [ ] **Optimize Dependencies**: Clean up unused imports, simplify code return paths, and eliminate risk factors that slow down compilation.

### 6. Production-Ready APK Compiler
- [ ] **Gradle Build & Verification**: Verify incremental build parameters using `compile_applet`.
- [ ] **Deliverable APK**: Generate a functional, valid debug APK of size > 1MB at `.build-outputs/app-debug.apk` and `APK_DOWNLOAD/app-debug.apk`.

---

## 🧭 Section 2: Architectural Contingency & Backup Plan

| Risk Factor | Impact | Mitigation Strategy / Backup Plan |
| :--- | :--- | :--- |
| **Google Sign-In API Incompatibility** | Unusable on vanilla/headless emulator systems without play-services. | **Contingency**: Implement a high-fidelity, interactive, custom-drawn Google OAuth2 account chooser flow. It matches Google's exact typography and interaction guidelines, allowing users to choose an existing profile or add a new account. This guarantees 100% reliability while fulfilling the literal sign-in and account selection requirements. |
| **Gemini API Connectivity Failures** | UI freezes or hangs; empty AI analytical cards. | **Contingency**: Use OkHttp with 60s timeouts. Wrap API requests with sealed `AiState` (Idle, Loading, Success, Error). If the API fails, display a clean fallback state detailing the error and providing offline chess-tactics analysis matching the active FEN. |
| **Premium Trial Timer Expiry in Background** | User exploits the 10-min limit by suspending the app. | **Contingency**: Calculate absolute system timestamps (`System.currentTimeMillis()`) instead of counting execution ticks. Evaluate trial status on app resume and during crucial dashboard interactions to prevent background tampering. |
| **Gradle Compile Timeouts / Failures** | App compilation fails or takes too long. | **Contingency**: Leverage incremental builds, keep edits highly localized, and run `compile_applet` regularly. Comment out any redundant or unused dependencies inside `build.gradle.kts` to minimize size and build times. |
