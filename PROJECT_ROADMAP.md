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

## Part B.2: Phase 2 Tactile Dark/AMOLED, 3D Quest Path, Room Persistence & AI/Revision Workspaces (Implemented)

- **Warm Charcoal Dark & True AMOLED Themes**:
  - Preserves **Warm Daylight** (`#FBF8F3`) untouched; introduces **Warm Charcoal Dark** (`#141311` background, `#1D1B18` / `#26231F` / `#302C27` layered surfaces, `#3E3932` warm borders, Warm Emerald `#34D399`/`#059669`, Warm Sky-Cobalt `#60A5FA`/`#2563EB`, Warm Amber `#FBBF24`/`#D97706`) and **True AMOLED** (`#000000` canvas, `#121212` / `#1A1A1A` tactile cards with crisp `1.dp` `#2C2C2C` outlines and `4.dp`–`5.dp` 3D lips).
  - Clean 3-way selector (`Daylight` / `Warm Dark` / `AMOLED`) in Settings.
- **3D Winding Quest Path & Ambient Moving Material Objects**:
  - Inside dropped-down chapters, lessons render as tactile `80.dp` 3D squircle Quest Pedestals (`Kova3DQuestPedestal`) with `6.dp` bottom lips, press-down spring physics, floating title/duration pills, pulsing crown/ring on the active quest, sinusoidal stepping trails, and a **3D Golden Trophy Pedestal** (`Kova3DGoldenTrophyPedestal`) for each Chapter Assignment.
  - Added `AmbientMaterialCanvas` rendering 5 subtle low-opacity (`0.055f–0.072f`) M3 geometric shapes gently drifting/rotating on slow infinite loops and pausing statically when Tactile Spring Motion is OFF.
  - Spring-damped (`spring(dampingRatio = 0.75f, stiffness = 340f)`) transitions across tab switches, chapter drop-downs, binder segments, and tag pickers.
- **Room SQLite Local Persistence & AI / Revision Workspaces**:
  - Added Room + KSP database (`KovaDatabase`, `ChatDao`, `RevisionDao`, `RoomStudyPersistenceRepository`) storing AI threads/messages, saved Revision notes, and Spaced-Repetition flashcard progress across restarts.
  - **AI Tab Makeover**: Compact Top Context & Quota Strip (`Morning Digest`, `Active Pool`, `SR Reserve`), collapsible slide-over Chat History Drawer (or split-pane on `>= 700.dp` tablets) with search/pin/delete/New Thread, editable Quick-Prompt Chips, and 1-tap **"Save to Notes"** (`Key Idea` / `Code Rule` / `Gotcha`) on every AI bubble.
  - **Revision Study Binder Makeover**: Top **Concept Mastery & Mistake Radar Card** with 1-tap **"Focus Next"**, segmented binder (`Highlights` | `Flashcards` | `SR Queue`), offline **60-Second 3D Flashcard Flip Mode** (live countdown, Pause/Resume/Restart, real Y-axis `rotationY` flip, `"Review Again"` / `"Got It"` SR updates, sprint summary), and Due Now vs. Upcoming SR queue.

## Part B.3: Phase 1.3 Focus Workspace, Warm Dark/AMOLED Overhaul, Audio/Haptic Feedback & Structured Workspaces (Implemented)

- **Redesigned Warm Dark & True AMOLED Palettes (Warm Daylight Untouched)**:
  - **Warm Dark**: Canvas `#141210`, layered surfaces `#1C1916` / `#25211D` / `#302B25`, warm borders `#423B32`, balanced Emerald `#22C55E` (`#15803D` lip), Balanced Cobalt `#3B82F6` (`#1D4ED8` lip), Amber `#F59E0B` (`#B45309` lip), and Coral `#F97316` (`#C2410C` lip) highlights.
  - **True AMOLED**: `#000000` background with `#101010` / `#181818` tactile cards, `#262626` borders, and matching `4.dp`–`6.dp` 3D lips.
- **Lesson Entry Focus Fade & Orientation-Aware Progress Bar**:
  - Entering a lesson fades out global app chrome into a clean distraction-free focus canvas (`LessonFocusWorkspace`).
  - Displays a sleek tactile 3D Lesson Progress Bar along the **top edge** in Portrait (`height >= width`) and along the **left side edge rail** in Landscape (`width > height`).
- **Intra-Lesson Split-Screen Ratio Toggle (`75:25 -> 50:50 -> 25:75`)**:
  - Smooth spring-animated pane resizing cycling `75:25 -> 50:50 -> 25:75 -> 75:25`, arranged **Top / Bottom** vertically in Portrait and **Left / Right** side-by-side in Landscape, paired with a clean styled empty **`Code & IDE Space (READY FOR PHASE 2/3)`** placeholder canvas.
- **Exit-from-Lesson Return Animations**:
  - Exiting a lesson smoothly animates chrome, telemetry, and chapter cards back in with staggered spring choreography, triggers a tactile settle/celebratory bounce on the exited lesson pedestal, pulses the chapter's 3D progress ring, and boosts the stepping path connector.
- **Structured AI & Revision Workspaces + Expanded M3 Expressive Ambient Canvas**:
  - **AI Tab**: Structured header with active lesson badge, compact collapsible session counter, 3-column Quota telemetry row, and dedicated monospace code cards inside tutor replies.
  - **Revision Tab**: Crisp section headers/dividers, balanced Coral Weak Spots / Due Now / Gotcha accents, and standardized `spring(dampingRatio = 0.76f, stiffness = 340f)` transitions.
  - **Ambient Canvas**: Expanded to 7 low-opacity M3 Expressive shapes (squircle, hollow ring, curved wave ribbon, pill capsule, 6-lobe scalloped polygon, 4-point star, rounded diamond).
- **Synthesized Duolingo-Style Sounds & Native Haptics**:
  - Added `KovaFeedbackEngine` (zero-asset PCM `AudioTrack` micro-sounds + `HapticFeedbackConstants`) wired to 3D presses, flips, saves, passes, and lock shakes, controlled by a **Sound & Haptics** toggle in Settings (ON by default).

## Part B.4: Phase 1.4 On-Device Persistence, Audible Audio/Haptics, True AMOLED & Layout Polish (Implemented)

- **Crash-Safe App-Private Disk Persistence (Survives App Kill & Restart)**:
  - Added atomic `FileKovaAppStateStore` (`filesDir/kova_app_state_v1.json`) alongside Room SQLite (`KovaDatabase`), hydrating synchronously on startup so Theme Mode, Dynamic Color, Motion, Sound & Haptics, Dev Override, split ratio, expanded chapters, completed lessons, test scores, chapter assignments, daily lesson quota (with automatic calendar-day rollover), active chat thread, composer drafts, and flashcard sprint counts survive cold restarts with zero external storage permissions.
  - **Reset Study Data** clears lessons, quota, chats, notes, and SR/sprint progress while preserving Theme, Dynamic Color, Motion, and Sound & Haptics preferences.
- **Immediate AI Send Bubble & Phase 3 Local Status Note**:
  - Tapping Send immediately appends the user's message bubble (stamped with `HH:mm` time and active lesson badge), clears the composer, plays feedback, persists to Room + disk, and appends a subtle `"SAVED LOCALLY · PHASE 3 READY"` status note below the user bubble.
- **Audible Media-Channel Synthesized Sounds & Real Haptics**:
  - Upgraded `KovaFeedbackEngine` to 44.1 kHz `USAGE_MEDIA` `AudioTrack` with raised-cosine envelopes so soft pops/ticks, rising 3-note success chimes (`C5 -> E5 -> G5`), and gentle low two-note thuds (`G3 -> D3` on locked nodes and `"Review Again"`) are reliably audible even on vibrate, paired with `performHapticFeedback(..., FLAG_IGNORE_VIEW_SETTING)`.
- **True AMOLED (`#000000`) & Zero-Overlap Layout Polish**:
  - Disabled Material 3 tonal elevation wash (`surfaceTint = Color.Transparent`, `LocalTonalElevationEnabled = false`, `tonalElevation = 0.dp` on Settings sheet) and painted the Android window background `#000000` so AMOLED cards (`#111111`/`#181818` with `#282828` outlines and grounded 3D lips) float over pure black.
  - Hardened compact & landscape layouts with `FlowRow` headers/pills, `maxLines = 1` + `TextOverflow.Ellipsis`, and compact-height adaptation inside the `25%` IDE pane so badges, buttons, and split-ratio pills never crowd or overlap.

## Part B.5: Phase 1.5 AI Persistence Fix, Interruptible Audio, Chapter Focus & Blurred Flashcard Stage (Implemented)

- **AI Vanishing Sent Message Fix + Collapsible History + Kova AI Emblem**:
  - Replaced `OnConflictStrategy.REPLACE` with Room `@Upsert` + atomic `@Transaction appendMessagesTransaction` so updating a thread preview never cascade-deletes child `chat_messages`; paired with optimistic in-memory pending-message merging in `KovaViewModel`.
  - Added a clean **Hide / Unhide History** toggle in the AI header (defaults hidden on phones, visible on tablets) and custom geometric 3D **`KovaAiEmblem`** in the header and fresh-thread welcome canvas.
- **Interruptible Zero-Overlap Duolingo-Style Audio & Haptics**:
  - Rebuilt `KovaFeedbackEngine` with a single-slot conflating `AudioTrack` worker, `AtomicLong` generation counter, and immediate `pause() + flush()` reset so rapid taps restart cleanly from `0` with zero audio stacking.
  - Expanded to 8 cues: crisp tap pop, pedestal press, chapter swoosh-pop, split-ratio mechanical tick, flashcard flip, ascending `"Got It"` chime, gentle low thud (`"Review Again"` / locked), and 4-note celebratory arpeggio (`C5 -> E5 -> G5 -> C6`).
- **Chapter Focus Mode & Directional Glowing Quest Wires**:
  - Learn tab opens on the clean **Chapter Overview**; tapping a chapter transitions into **Chapter Focus Mode** (hiding global navigation & dashboard chrome), with a clickable **Top Chapter Header Bar** to return and a floating **corner AI circle button** that launches a **slide-over Chapter AI Sheet** (keeping the chapter open, with an `"AI Lab"` jump button to the full AI tab).
  - Completed/progressed Bezier stepping connectors render a smooth directional luminous flow pulse, while upcoming segments remain dimmed.
- **Structured Revision Study Binder & Blurred-Backdrop Focused Flashcard Stage**:
  - Rebuilt Revision with a compact Mastery & Quota header, active Chapter Revision Sheet shortcut, clean 3-way segmented binder (`Notes` | `Flashcards` | `SR`), and zero-overlap `FlowRow` cards.
  - Added a Preview Card with `"Launch Focus Stage"` opening a full-screen **Focused Flashcard Stage** over a real blurred backdrop (`Modifier.blur(18.dp)` on Android 12+ + frosted scrim), tactile 3D-flip cards, and dedicated scrollable monospace Kotlin code blocks.

## Part B.6: Phase 2.1 Lesson Engine UX, IDE Workspace & Navigation Overhaul (Implemented)

- **Strict 3-Font Typography Discipline**: Enforced `Space Grotesk` (headings/titles), `Plus Jakarta Sans` (body/UI controls), and `JetBrains Mono` (code blocks, IDE, badges) across every screen.
- **Spacious AI Tutor & Corner Status Popover**: Slim header with `KovaAiEmblem`, History toggle, and expandable top-right status/quota popover (`Ready` / `Thinking` / `Offline` + quota telemetry).
- **Distraction-Free Reader & Plain-English Math**: Plain human math phrasing, `"Explain →"` conversational focus view, Desired Output with collapsed-by-default `"Show Solution & Line Notes"`, and clean Chapter 1 pacing (Lesson 1.1 free of `+=`, introduced in Lesson 1.3).
- **Practice-First IDE & Smart Back-Navigation**: IDE opens in Practice/Editor mode by default with an `"Unlock Debugger"` gate; Smart System Back-Navigation restores exact lesson, scroll offset, and state after jumping to AI, routes tabs back to Home, and prompts double-Back to exit on Home.

## Part B.7: Phase 2.2 AI Cleanup, Structured Lesson Topics & Blurred 70:30 Debugger Stage (Implemented)

- **AI Context Banner Scoping & Centered Emblem**: Removed bottom clutter cards, scoped the `"Scoped Tutor · Lesson X.Y"` banner strictly to lesson-launched threads (hidden on fresh `New Chat` general threads), added `KovaAiCenteredEmptyState` centerpiece, and rendered dark-framed `KovaFramedCodeBlock` snippets inside replies.
- **Intra-Lesson Structurization & Dedicated Focused Topic Pages**: Removed stacked top highlight banners; structured every lesson into (1) Clean Header & Progress → (2) Numbered Topics List (`1.1`, `1.2`, ...) where tapping any topic opens a Dedicated Focused Topic Page (analogy → Kotlin connection → dark-framed monospace code block → plain-English symbol breakdown) → (3) Practice Task & Framed Desired Output with interactive code-writing checklist, `"Open IDE to Practice"`, and collapsed `"Show Solution"` → (4) Visual Separator Bar → End-of-Lesson Detailed Revision (Topic Recap, Word Breakdowns, and filterable Pre-Highlighted Takeaways + Saved Highlights moved exclusively here) → (5) Integrated 5-Question Closed-Book Test.
- **IDE Split Default OFF, Exact Line Alignment & Blurred 70:30 Debugger Stage**:
  - Split IDE defaults to **OFF** on lesson open; top bar (portrait) / left rail (landscape) exposes an `"IDE"` toggle opening IDE at `50:50` alongside the `50:50 -> 25:75 -> 75:25` split cycle pill.
  - Added dedicated dark code-workbench theme tokens across Daylight, Warm Dark, and AMOLED, and exact `TextLayoutResult`-aligned line-number gutters even when lines wrap.
  - Tapping `"Unlock Debugger"` / `"Debugger 70:30"` opens a full-screen blurred-backdrop (`Modifier.blur(18.dp)` + dark scrim) **Adaptive 70:30 Debugger Stage** (`DebuggerFocusStageOverlay`: vertical 70:30 in portrait, side-by-side 70:30 in landscape) pairing a 70% Debugger/Watch/Console workbench with a 30% Plain-English Step Explanation panel.

---

## Part C: Upcoming Phase 3 (Live Gemini AI, Lab Prompt Builder & Local Terminal / File Runner Integration)

- **Lab Virtual Prompt Builder**: Interactive `<Folder>` / `<File>` tree builder & single-prompt bundler in `Lab`.
- **Local Terminal / File Runner Integration**: Export, sync, and execute virtual `<Folder>` / `<File>` Kotlin project bundles directly alongside external Android developer tools—including **Termux**, **CodeAssist**, and **GitHub** workflows—so learners can compile and run multi-file projects from Kova's Lab and Practice workspace.
- **Live Firebase AI Logic (`Firebase.ai`)**: Swap `LocalLessonTutorEngine` behind the existing ViewModel/Repository boundary for live scoped Gemini tutoring, Morning Digest summaries, Learner Behavior Profile, and timed Spaced-Repetition pop-ups.
