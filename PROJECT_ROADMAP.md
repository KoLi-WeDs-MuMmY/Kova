# Kova — Project Roadmap & Architecture Blueprint

This document preserves the North Star vision, Phase 1 deliverable contract, and guardrails so future development phases plug directly into the established MVVM architecture without rewriting themes, navigation, or motion primitives.

---

## Part A: North Star Vision (0% → 100% Kotlin & Android Mastery)

**Kova** is a personal, high-craft native Android learning companion built in Kotlin and Jetpack Compose, designed for 8-inch and 11-inch Android tablets (portrait & landscape) as well as compact phones and floating-overlay pairing alongside CodeAssist or Termux.

### Planned 4-Tab Architecture
1. **Learn Tab**: Duolingo-style winding chapter & lesson path. Each lesson teaches from literal word meanings & 10-year-old-friendly analogies, supports interactive text highlighting, includes a step-by-step **Debugger Line Animation** code viewer with a live Variable/State watch panel, displays Desired Output + collapsible **Show Solution**, and ends with an integrated **Lesson Test** (must pass >= 80% inside the lesson to unlock the next lesson). Enforces a max of **3 new lessons per day**.
2. **Lab Tab**: **Virtual Folder/File Code Prompt Builder** (creates virtual `<Folder>` and `<File>` structures bundled into single AI prompts without file uploads) + **Chapter Assignments Hub** (assignments unlock at chapter end; not forced immediately after the final lesson, but required before unlocking the next chapter).
3. **AI Tab**: **Scoped AI Doubt Solver & Tutor** locked strictly to the active lesson/files + a local **Learner Behavior Profile** + **Daily Quota Manager** (reserves 1–2 morning requests for behavior/progress summary, active pool for study, leftover pool for timed spaced-repetition revision pop-ups).
4. **Revision Tab**: Saved color-coded highlighted lesson notes, Chapter Revision Sheets, Spaced-Repetition schedule, and Quota status.
5. **Dual-Screen Companion Experience**: Adaptive tablet layout (`NavigationRail` + two-pane progress/stats layout at `>= 600.dp`) plus a toggleable **Compact Floating Overlay Preview** mode (draggable companion window with corner snap).

---

## Part B: Phase 1 Deliverable (Implemented)

- **Material 3 Extended Expressive Theme & Typography**:
  - Locally bundled Google Fonts: **Space Grotesk** (Display & Headlines), **Plus Jakarta Sans** (Body & Labels), and **JetBrains Mono** (code badges, metrics, and tags).
  - Theme modes: **Dichrome Obsidian Dark** (default `#0A0E14`, Electric Mint `#2EE59D`, Warm Amber `#FFB84D`), **AMOLED True Black** (`#000000` canvas + OLED-safe layered containers), **Daylight Light Mode**, plus **Android 12+ Material You Dynamic Color** toggle (compatible with AMOLED True Black).
  - Layered tonal surface depth with `1.dp` luminous gradient/tonal borders, soft elevation shadows, expressive shapes (`28.dp` hero cards, `20.dp` stat/assignment cards, `26.dp` squircle lesson nodes, `CircleShape` pills, custom scalloped M3 Expressive badges), and an explicit **Motion / Reduced Motion** toggle.
- **Adaptive Navigation & Shared-Element Spring Transforms**:
  - Floating-style `NavigationBar` on compact screens (`< 600.dp`) and expressive `NavigationRail` + two-pane stats layout on tablets (`>= 600.dp`).
  - `SharedTransitionLayout` + `AnimatedContent` container transforms (`spring(dampingRatio = 0.78f, stiffness = 360f)`) from Lesson Nodes, Chapter Assignment Cards, Revision Note Cards, and the Chapter Revision Sheet into full-screen detail views with `BackHandler` collapse.
  - Top-bar **Full View / Floating** pill morphing the app into a draggable floating companion window over a dimmed backdrop with drag handle and corner-snap controls.
- **Learn Path & Mastery Gating (Mocked via `MockLearningRepository`)**:
  - Hero Chapter Card (`Chapter 1: Kotlin Foundations`, animated mastery ring/bar, `3/3 Daily Lessons Left` pill, `Revision Sheet` pill).
  - S-curve winding path for Lessons `1.1`–`1.4` with animated Bezier connectors, completed emerald nodes, pulsing active node halo + `START` pill, and locked node spring-shake feedback.
  - Integrated Lesson placeholder with `"Pass Mock Test (85%)"` simulation unlocking the next lesson, animating the connector green, and decrementing daily quota (`3 -> 2`).
  - Synchronized **Chapter 1 Assignment** trophy card across `Learn` and `Lab` gating **Chapter 2: Functions & Lambdas** (`"Complete Chapter 1 Assignment to Unlock"`).
- **Lab, AI & Revision Skeletons + Developer Settings**:
  - Styled Phase 3 previews for the Virtual Folder/File Prompt Builder and Scoped Lesson Tutor + Quota pills.
  - Color-coded Highlighted Notes (`Yellow` = Key Idea, `Green` = Code Rule, `Pink` = Ask AI) with category filters and Spaced-Repetition timers.
  - Developer Settings sheet with Theme Mode, Dynamic Color, Tactile Spring Motion, and **Dev Override: Unlock All & Reset Daily Limit**.

## Part B.1: Phase 1.1 Visual & Interaction Overhaul (Implemented)

- **Tactile Warm Game-Board Theme (Zero Neon / Glow)**:
  - Defaults to **Warm Daylight** (`#FBF8F3` warm ivory canvas, `#FFFFFF` cards with `#E7DFD0` warm outlines), alongside **Warm Matte Dark** (`#171614`) and **AMOLED True Black** (`#000000`) plus optional Material You Dynamic Color.
  - Accent pairs with solid `4.dp`–`6.dp` 3D bottom lips: Rich Emerald (`#16A34A` / `#15803D`) for completed/primary, Royal Cobalt (`#2563EB` / `#1D4ED8`) for active quests/chapters, Warm Amber (`#F59E0B` / `#B45309`) for assignments/streaks, and Stone (`#E5E7EB` / `#CBD5E1`, `#64748B` labels) for locked nodes.
  - Physical downward spring press animations (`KovaTactileCard`, `KovaTactileButton`, `KovaTactileNodeCap`) with subtle native Android haptic ticks and zero neon drop-shadows or glowing rings.
- **3D Circular Chapter Rings & Click-to-Drop-Down Lessons**:
  - Tactile 3D Chapter Cards featuring thick beveled **3D Circular Progress Rings** framing chapter emblems that animate smoothly (`0% -> 25% -> 50% -> 75% -> 100%`).
  - Clicking a Chapter Card toggles its dropped-down lesson trail using `AnimatedVisibility(expandVertically(spring(dampingRatio = 0.72f, stiffness = 320f)) + fadeIn())` (Chapter 1 starts expanded; Chapter 2 starts collapsed and can be tapped to peek at locked nodes and the amber gate banner).
  - Preserves all ViewModel state, `MockLearningRepository` data, mastery gating, Learn↔Lab assignment sync, Dev Override, and shared-element `SharedTransitionLayout` transforms across all 4 tabs.

---

## Part C: Phase Boundaries & Upcoming Phases

### Strict Phase 1 Guardrails (Enforced)
- Clean MVVM (`KovaViewModel` + `StateFlow<KovaUiState>` + strongly typed domain models).
- All mock data isolated inside `MockLearningRepository` (behind `LearningRepository` interface) so future phases swap persistence cleanly without touching UI shells or transitions.
- No live Gemini API calls, no TTS engines, no full curriculum text, and no database persistence in Phase 1.

### Planned Phase 2 (Interactive Curriculum & Local Persistence)
- Replace `MockLearningRepository` with Room / Firestore-backed curriculum repository.
- Implement interactive lesson reading with text highlighting (`Yellow` / `Green` / `Pink`), Debugger Line Animation + live Variable Watch panel, Desired Output + collapsible Solution, and real >= 80% Lesson Tests.

### Planned Phase 3 (Lab Prompt Builder & Scoped AI Tutor)
- Implement interactive `<Folder>` / `<File>` tree builder & prompt bundler in `Lab`.
- Integrate Firebase AI Logic (`Firebase.ai`) for the Scoped Lesson Tutor, Morning Digest, Learner Behavior Profile, and timed Spaced-Repetition pop-ups within the Daily Quota Manager.
