# Lusalos — Laro ng Lahi

A modern, culturally authentic 2D Filipino strategy board game for Android inspired by Nine Men's Morris (Mills), featuring intelligent offline AI opponents, real-time online multiplayer, automatic matchmaking, and a competitive Filipino rank progression system.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and design decisions have been confirmed and will guide the entire implementation:

- **Authentication & Backend Integration**: Full Firebase backend (Firestore & Authentication) with seamless guest mode for offline practice against AI, and Google Sign-In for ranked online matches, player challenges, and cloud-synced leaderboards.
- **Visual Aesthetic & Theming**: Traditional warm Narra wood board texture with woven Banig accents, contrasting polished Coconut shell (dark wood/charcoal) and Capiz pearl (ivory/gold) game pieces, accompanied by the animated Salakot-wearing mascot.
- **Game Engine & Rules Standard**: Complete Nine Men's Morris rules with 3 distinct phases: Placement Phase (alternating 9 pieces on 24 board intersections), Movement Phase (sliding along valid paths to adjacent intersections), and the Flying Phase (when a player has exactly 3 pieces left, pieces can fly to any vacant point). Mill formation triggers removal of an opponent's piece (with protected mill exception handling).

---

### 1. Overview & Core Concept

**Lusalos — Laro ng Lahi** elevates traditional Filipino tabletop strategy into a modern Android experience. It honors the cultural heritage of indigenous and folk games while delivering competitive, tournament-grade gameplay.

- **What It Does**: Provides a complete 2D board game where players place and maneuver 9 pieces across 3 concentric squares to form mills (3 in a row), capture opposing pieces, and outwit opponents either offline against heuristic/Minimax AI or online against other players.
- **Target Audience**: Casual board game lovers, strategic thinkers, Filipino cultural gaming enthusiasts, and competitive online players of all ages.
- **Key Value**: Immediate, responsive touch gameplay on mobile; high-depth AI suitable for both beginners and experts; clean real-time multiplayer with zero wait times via intelligent matchmaking; and proud Filipino cultural identity.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Splash Screen (3 Seconds)**:
   - Cultural entrance animation: "LARO NG LAHI" title in golden typography.
   - Mascot illustration: cheerful Filipino character wearing a traditional woven salakot hat.
   - Smooth crossfade transition into the Main Dashboard (or Auth Screen if unauthenticated).

2. **Authentication & Profile Gate**:
   - Google Sign-In button powered by Jetpack Credential Manager for online competitive features.
   - "Continue as Guest" one-tap entry for offline practice against AI bots.
   - User profile showing username, avatar, current Filipino rank badge, competitive points, and match stats.

3. **Main Dashboard & Navigation**:
   - Modern Material 3 Bottom Navigation: **Home**, **Play**, **Rankings**, and **Profile**.
   - Quick action hero cards: *Quick Match*, *Play vs AI*, and *Find a Player / Challenge*.
   - Live notification badge for incoming friend challenges.

4. **Play vs AI (Offline Practice)**:
   - Difficulty selection: **Easy** (heuristic/greedy), **Medium** (defensive & mill-blocking), and **Hard** (Minimax with alpha-beta pruning & positional evaluation).
   - Instant restart, interactive move undo, mill highlights, and match review.

5. **Online Multiplayer & Matchmaking**:
   - **Quick Match**: Dynamic wait screen with animated radar, live queue status, and automatic matching into a synchronized Firestore match room.
   - **Find a Player**: Real-time user search by username, challenge invitations, and instant accept/decline banners.
   - **Game Room**: Turn timers, connection status indicator, opponent profile display, real-time board sync, resignation confirmation, and victory fanfare.

6. **How to Play Tutorial**:
   - Interactive 7-step guide explaining Board Layout, Placement Phase, Movement Phase, Mill Detection & Piece Removal, Flying Rule, Win Conditions, and the Filipino Rank System.

#### Visual Identity & Color Palette

- **Primary Colors**:
  - `Narra Gold / Amber`: Deep warm golden amber (`#E0A93B` / `#D4881E`) evoking antique polished Philippine Narra hardwood.
  - `Mahogany Dark Wood`: Board background and frames (`#2B170E` / `#3D2314`).
  - `Capiz Pearl (Player 1 / White)`: Luminous iridescent cream/ivory (`#FBF8EE` with `#D4AF37` subtle gold rim).
  - `Coconut Shell (Player 2 / Black)`: Deep rich organic coconut shell brown (`#1F1511` with `#8C5835` warm rim).
  - `Banig Neutral Accent`: Textured woven undertone (`#F5E6CA` / `#3E2A1D`).
  - `Victory Emerald & Warning Coral`: Culturally grounded success `#2E7D32` and alert `#C62828`.
- **Typography & Layout**:
  - Prominent titles with distinct serif/display character.
  - Clean, legible sans-serif for game status, move history, and timers.
  - Tactile 48dp+ touch intersections on the canvas with dynamic glowing halos on valid moves.

---

### 3. Key Product Decisions & Trade-Offs

#### Decision 1: Pure Kotlin Core Game Engine Decoupled from Compose
- **Chosen Approach**: Build `LusalosEngine` as a pure, dependency-free Kotlin domain module containing the 24-point graph topology, phase state machine, mill detection algorithms, and move validator.
- **Why**: Allows exhaustive unit testing (100% test coverage of all Nine Men's Morris edge cases, including mill exceptions and flying rule) without requiring Compose or Android runtime context, and allows direct reuse by the offline AI.

#### Decision 2: Three-Tier AI with Alpha-Beta Pruning
- **Chosen Approach**:
  - *Easy*: Random legal moves with high preference for forming immediate mills.
  - *Medium*: Greedy heuristic evaluating immediate mill threats, blockades, and piece mobility.
  - *Hard*: Depth-limited Minimax with Alpha-Beta pruning, evaluation function weighting piece count (material), formed mills, open mills (2-in-a-row with free 3rd spot), trapped pieces (mobility), and flying agility.
- **Why**: Ensures snappy frame rates (<200ms evaluation time) on mobile hardware while offering genuine challenge.

#### Decision 3: Firestore Real-Time Match State with Atomic Turns
- **Chosen Approach**: Firestore collections `/matches/{matchId}` listening via Kotlin Flow snapshot listeners. State payload encodes turn index, board 24-point array, current phase, piece counts, mill status, and timestamps.
- **Why**: Zero socket infrastructure required, automatic offline reconnection handling, and native Android Firebase SDK integration.

#### Decision 4: Filipino Competitive Rank Progression
- **Ranks**:
  1. *Baguhan* (Novice): 0–99 pts
  2. *Bagwis* (Swift Wing): 100–249 pts
  3. *Mandirigma* (Warrior): 250–499 pts
  4. *Dalubhasa* (Expert): 500–999 pts
  5. *Bayani* (Hero): 1,000–1,999 pts
  6. *Kampeon* (Champion): 2,000–3,999 pts
  7. *Alamat* (Legend): 4,000+ pts
- **Scoring**: Verified online win: +20 pts; loss/draw: 0 pts; practice games: 0 pts.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                       JETPACK COMPOSE UI                        │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐  │
│  │ SplashScreen │  │  AuthScreen  │  │ MainDashboardScaffold │  │
│  └──────────────┘  └──────────────┘  └───────────────────────┘  │
│         │                 │                       │             │
│         ▼                 ▼                       ▼             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ Navigation & Tabs: Home | Play | Rankings | Profile       │  │
│  └───────────────────────────────────────────────────────────┘  │
│         │                 │                       │             │
│         ▼                 ▼                       ▼             │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐  │
│  │ GameScreen   │  │ Matchmaking  │  │ PlayerSearch / Chlng  │  │
│  │ (Custom      │  │ & QuickMatch │  │ Leaderboard & History │  │
│  │  Canvas)     │  │ Dialog       │  │ Profile Screen        │  │
│  └──────────────┘  └──────────────┘  └───────────────────────┘  │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                     VIEWMODELS & GAME STATE                     │
│  ┌──────────────────────┐              ┌─────────────────────┐  │
│  │ GameViewModel        │              │ OnlineViewModel     │  │
│  │ - Local vs AI State  │              │ - Matchmaking Flow  │  │
│  │ - Undo / Redo / Phase│              │ - Challenge Flow    │  │
│  └──────────┬───────────┘              └──────────┬──────────┘  │
└─────────────┼─────────────────────────────────────┼─────────────┘
              ▼                                     ▼
┌───────────────────────────┐        ┌────────────────────────────┐
│    LUSALOS GAME ENGINE    │        │      FIREBASE REPOSITORIES │
│  - 24 Intersections Graph │        │ - MatchRepository          │
│  - Mill Detection (16)    │        │ - UserRepository & Stats   │
│  - Phase Management       │        │ - LeaderboardRepository    │
│  - Move & Capture Rules   │        │ - ChallengeRepository      │
│  - AI (Easy, Med, Hard)   │        └──────────────┬─────────────┘
└───────────────────────────┘                       │
                                                    ▼
                                     ┌────────────────────────────┐
                                     │     CLOUD FIRESTORE        │
                                     │ /users/{uid}               │
                                     │ /matches/{matchId}         │
                                     │ /matchmaking_queue/{uid}   │
                                     │ /challenges/{challengeId}  │
                                     └────────────────────────────┘
```

#### Board Representation & Graph Topology

- 24 Points labeled `0` to `23` across 3 concentric squares (Outer: 0-7, Middle: 8-15, Inner: 16-23):
  - **Outer Square**:
    `0` (Top-Left), `1` (Top-Mid), `2` (Top-Right),
    `3` (Mid-Right), `4` (Bottom-Right), `5` (Bottom-Mid),
    `6` (Bottom-Left), `7` (Mid-Left).
  - **Middle Square**:
    `8` (TL), `9` (TM), `10` (TR), `11` (MR), `12` (BR), `13` (BM), `14` (BL), `15` (ML).
  - **Inner Square**:
    `16` (TL), `17` (TM), `18` (TR), `19` (MR), `20` (BR), `21` (BM), `22` (BL), `23` (ML).
  - **Connecting Cross-Lines**: `(1, 9, 17)`, `(3, 11, 19)`, `(5, 13, 21)`, `(7, 15, 23)`.
- Total 16 unique lines of 3 forming legal Mills.

#### Step-by-Step Implementation Roadmap

1. **Platform Sync & Dependencies**: Update `metadata.json` and `strings.xml` to `Lusalos`, uncomment required Firebase/Credentials dependencies in `app/build.gradle.kts`.
2. **Domain Game Engine (`com.example.game.*`)**: Implement board constants, adjacent lines graph, mill definitions, `GameState`, `Player`, `GamePhase`, move validation, flying phase, and win/draw detection.
3. **AI Bot Engine (`com.example.ai.*`)**: Heuristic & Minimax game tree search with alpha-beta pruning.
4. **Data Models & Firebase Layer (`com.example.data.*`)**: Firestore models for matches, challenges, queues, and player profiles; zero-trust security rules and repository implementations.
5. **UI & Canvas Game Board (`com.example.ui.game.*`)**: Responsive custom Canvas rendering the Narra wood board, geometric intersection lines, Capiz and Coconut pieces with glowing selection and mill indicators.
6. **Feature Screens**:
   - Splash screen with Salakot mascot animation (3s).
   - Auth screen (Google Sign-In + Guest entry).
   - Main dashboard with 4 bottom navigation tabs.
   - Matchmaking and Player Search / Challenge screens.
   - Leaderboard & Player Profile with match history and rank progression.
   - How to Play interactive rulebook.
7. **Automated Unit & Rule Tests**: Robolectric & JUnit test suite validating game engine rules, mill combinations, flying rules, and AI decisions.
8. **Verification**: Run `compile_applet` and verify zero build errors.
