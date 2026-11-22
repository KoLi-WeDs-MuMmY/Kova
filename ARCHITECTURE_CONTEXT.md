# Kova — Architecture Context & Guardrails

Authoritative technical reference for the **Kova** Kotlin & Jetpack Compose Android application. Updated ahead of **Phase 2: Interactive Lesson Engine**.

---

## 1. Exact Package Structure

Base Android namespace: `com.example` (do **not** rename `com/example/` directories or `namespace`).

| Package | Responsibility | Key Files |
|---|---|---|
| `com.example` | App entry point | `MainActivity.kt` (`enableEdgeToEdge()`, hosts `KovaApp()`) |
| `com.example.model` | Immutable domain & UI state models | `KovaModels.kt` |
| `com.example.data` | Curriculum seed repository & local tutor helpers | `MockLearningRepository.kt`, `Chapter1CurriculumContent.kt`, `LocalLessonTutorEngine.kt` |
| `com.example.data.local` | Dual on-device persistence (Room SQLite + Atomic JSON) | `KovaDatabase.kt`, `KovaRoomEntities.kt`, `KovaDaos.kt`, `StudyPersistenceRepository.kt`, `KovaAppStateStore.kt` |
| `com.example.viewmodel` | Single-source-of-truth MVVM state & persistence orchestration | `KovaViewModel.kt` (`KovaViewModel`, `KovaViewModelFactory`) |
| `com.example.ui` | Root shell, adaptive chrome, shared-element transitions, overlays | `KovaApp.kt` |
| `com.example.ui.screens` | Feature screens & full-screen focus workspaces | `LearnScreen.kt`, `LabScreen.kt`, `AiScreen.kt`, `RevisionScreen.kt`, `FlashcardSprintView.kt`, `DetailSheets.kt`, `LessonFocusWorkspace.kt`, `LessonReaderPane.kt`, `LessonDebuggerPane.kt`, `LessonTestOverlay.kt` |
| `com.example.ui.components` | Tactile 3D primitives, ambient canvas, floating preview, settings | `KovaExpressivePrimitives.kt`, `AmbientMaterialCanvas.kt`, `FloatingCompanionHost.kt`, `DeveloperSettingsSheet.kt`, `KovaAiEmblem.kt` |
| `com.example.ui.feedback` | Zero-asset synthesized PCM audio + native Android haptics | `KovaFeedbackEngine.kt` |
| `com.example.ui.theme` | M3 color schemes, tactile 3D lip palettes, typography, shapes | `Color.kt`, `Theme.kt`, `Type.kt`, `Shapes.kt` |

---

## 2. Screen Composables & Overlays

- **Root Shell (`com.example.ui`)**
  - `KovaApp(viewModel)` — wires `collectAsStateWithLifecycle()`, `KovaFeedbackController`, `KovaTheme`, `FloatingCompanionHost`, `DeveloperSettingsSheet`, and snackbars.
  - `KovaShellContent(...)` — manages `SharedTransitionLayout`, `AnimatedContent` detail transforms, adaptive `KovaNavigationRail` (`>= 600.dp`) vs. `KovaTactileBottomNavBar`, focus-mode chrome fade (`isGlobalChromeHidden`), slide-over `ChapterScopedAiSheet`, and blurred `FlashcardFocusStageOverlay`.
- **Primary Tabs (`com.example.ui.screens`)**
  - `LearnScreen(...)` — Chapter Overview with 3D circular progress rings -> Chapter Focus Mode (hides global chrome, clickable top chapter header bar, floating corner AI circle shortcut, 3D winding quest pedestals, directional glowing connectors, 3D golden assignment trophy). Also exports `ChapterAssignmentPathCard(...)`.
  - `LabScreen(...)` — Synchronized Chapter Assignments Hub + Phase 3 Virtual Folder/File Prompt Builder preview.
  - `AiScreen(...)` — Scoped Lesson Tutor with collapsible Chat History (`Hide/Unhide`), `KovaAiEmblem`, 3-pill Quota Strip, editable Quick Prompts, immediate user bubble + Phase 3 local status note, and 1-tap `Save to Notes`. Also exports `ChapterScopedAiSheet(...)`.
  - `RevisionScreen(...)` — Compact Mastery & Quota Header, active Chapter Revision Sheet shortcut, 3-way segmented binder (`Notes` | `Flashcards` | `SR`), Concept Mastery & Mistake Radar (`Focus Next`), searchable/filterable Highlights, and Due Now vs. Upcoming SR queue. Exports `HighlightCategory.accentColor()` and `HighlightCategory.displayLabel()`.
- **Focus Workspaces & Overlays (`com.example.ui.screens`)**
  - `DetailSheetHost(...)` — `BackHandler`-guarded host for `DetailDestination.LessonDetail`, `AssignmentDetail`, `NoteDetail`, and `RevisionSheetDetail`.
  - `LessonFocusWorkspace(...)` — Distraction-free Lesson Focus Canvas with orientation-aware tactile progress bar (top horizontal bar in portrait, left vertical rail in landscape) and spring-animated `75:25 -> 50:50 -> 25:75` split panes (Lesson Pane + Code/IDE Debugger Pane).
  - `FlashcardDeckPreviewCard(...)` & `FlashcardFocusStageOverlay(...)` — Blurred-backdrop (`Modifier.blur(18.dp)` on Android 12+) 60-second 3D flip flashcard sprint.
- **Shared Components (`com.example.ui.components`)**
  - `KovaTactileCard`, `KovaTactileButton`, `KovaTactileNodeCap`, `Kova3DQuestPedestal`, `Kova3DGoldenTrophyPedestal`, `Kova3DCircularChapterRing`, `KovaPillBadge`, `KovaScallopedSealBadge`, `Modifier.shakeOnTrigger`, `Modifier.settleOrCelebrateOnReturn`.
  - `AmbientMaterialCanvas`, `FloatingCompanionHost`, `DeveloperSettingsSheet`, `KovaAiEmblem`, `KovaAiWelcomeEmblemCard`.

---

## 3. ViewModel & StateFlow Contract

- **Class**: `com.example.viewmodel.KovaViewModel` (instantiated via `KovaViewModelFactory(context)`).
- **Primary StateFlow**:
  - `val uiState: StateFlow<KovaUiState>` (backed by private `_uiState: MutableStateFlow<KovaUiState>`).
- **Key Derived Properties on `KovaUiState`**:
  - `isLessonFocusMode`, `isChapterFocusMode`, `isGlobalChromeHidden`, `focusedChapter`, `activeLesson`, `activeThread`, `filteredChatThreads`, `filteredHighlights`, `dueNowSrItems`, `upcomingSrItems`, `flashcardDeck`, `masteryRadar`.
  - Lookups: `findLesson(lessonId)`, `findAssignment(assignmentId)`, `findChapter(chapterId)`, `findNote(noteId)`.
- **Core ViewModel Actions**:
  - Navigation & Focus: `selectTab`, `toggleChapterExpanded`, `openChapterFocus`, `closeChapterFocus`, `setChapterAiSheetOpen`, `switchFromChapterFocusToAiTab`, `toggleAiHistoryVisibility`, `setAiHistoryVisibility`, `openLesson`, `openAssignment`, `openNote`, `openRevisionSheet`, `closeDetail`, `cycleLessonSplitRatio`, `setLessonSplitRatio`.
  - Mastery & Progression: `checkDailyRollover`, `passLessonMockTest` (preserved for backwards compatibility; delegates/aligns with real `submitLessonTestPass`), `completeAssignment`.
  - AI & Revision Sync: `selectChatThread`, `createNewLessonThread`, `updateChatSearchQuery`, `togglePinThread`, `deleteChatThread`, `updateChatComposerText`, `insertQuickPrompt`, `sendChatMessage`, `saveAiMessageToNotes`, `saveLessonHighlight`, `askAiAboutLessonHighlight`.
  - Revision & Flashcards: `selectBinderTab`, `updateRevisionSearchQuery`, `selectHighlightFilter`, `focusNextFromRadar`, `startQuickReviewFromQueue`, `openFlashcardFocusStage`, `closeFlashcardFocusStage`, `startOrResumeFlashcardSprint`, `togglePauseFlashcardSprint`, `restartFlashcardSprint`, `flipFlashcard`, `rateFlashcardGotIt`, `rateFlashcardReviewAgain`.
  - Settings & Dev Controls: `toggleFloatingPreview`, `setFloatingPreview`, `setThemeMode`, `setDynamicColorEnabled`, `setExpressiveMotionEnabled`, `setSoundAndHapticsEnabled`, `setDevUnlockAllOverride`, `resetDemoProgress`, `setSettingsOpen`, `triggerLockedNodeFeedback`, `consumeSnackbar`.

---

## 4. Persistence Architecture (Dual Local Storage)

1. **Atomic App-Private JSON Store (`com.example.data.local.FileKovaAppStateStore`)**
   - **Location**: `File(appContext.filesDir, "kova_app_state_v1.json")` (temp-file + atomic rename, zero storage permissions).
   - **Interface (`KovaAppStateStore`)**:
     - `load(): PersistedAppState?` (synchronous cold-start hydration inside `createInitialState()`)
     - `save(state: PersistedAppState)`
     - `clear()`
   - **Stores**: `themeModeName`, `dynamicColorEnabled`, `expressiveMotionEnabled`, `soundAndHapticsEnabled`, `devUnlockAllOverride`, `lessonSplitRatioName`, `expandedChapterIds`, `dailyLessonsRemaining`, `activeAiPoolRemaining`, `lastQuotaDateIso`, `activeThreadId`, `chatComposerDraft`, `sprintGotItCount`, `sprintReviewAgainCount`, `sprintCurrentIndex`, `lessonStates` (`statusName`, `passScorePercent`), `chapterStates` (`isUnlocked`, `assignmentUnlocked`, `assignmentCompleted`).
2. **Room SQLite Database (`com.example.data.local.KovaDatabase`)**
   - **Database File**: `"kova_study_companion.db"`, `version = 1`.
   - **Tables / Entities**: `chat_threads` (`ChatThreadEntity`), `chat_messages` (`ChatMessageEntity` with `CASCADE` FK to `chat_threads`), `revision_notes` (`RevisionNoteEntity`), `spaced_repetition_items` (`SpacedRepetitionEntity`).
   - **Repository (`StudyPersistenceRepository` -> `RoomStudyPersistenceRepository` / `InMemoryStudyPersistenceRepository`)**:
     - Flows: `chatThreads`, `revisionNotes`, `spacedRepetitionItems`, `observeMessages(threadId)`
     - Mutations: `ensureSeeded`, `upsertThread`, `appendMessage`, `appendMessages` (Room `@Transaction` using `@Upsert` — NEVER `OnConflictStrategy.REPLACE` on `chat_threads` because SQLite `REPLACE` triggers `CASCADE` delete of child messages!), `setThreadPinned`, `deleteThread`, `markMessageSaved`, `saveRevisionNote`, `upsertSpacedRepetitionItem`, `resetAll`.

---

## 5. Sound & Haptics Controller (`com.example.ui.feedback`)

- **Engine**: `KovaSoundSynthesizer` — 44.1 kHz PCM `AudioTrack` routed via `USAGE_MEDIA` / `CONTENT_TYPE_MUSIC` (audible even on vibrate), driven by a single-slot conflating worker thread with an `AtomicLong` generation counter and immediate `pause() + flush()` preemption so rapid clicks restart cleanly from sample 0 with zero audio overlap.
- **Controller (`KovaFeedbackController` provided via `LocalKovaFeedback`)**:
  - `tap(view)` — `CLOCK_TICK` + crisp bubble pop (`playTapPop`)
  - `pedestalPress(view)` — `KEYBOARD_TAP` + deeper keycap thock (`playPedestalPop`)
  - `chapterSwoosh(view)` — `KEYBOARD_TAP` + rising velvet swoosh-pop (`playChapterSwoosh`)
  - `splitTick(view)` — `CLOCK_TICK` + mechanical double-detent tick (`playSplitTick`)
  - `cardFlip(view)` — `KEYBOARD_TAP` + breezy flip snap (`playCardFlip`)
  - `gotItChime(view)` — `CONFIRM` + ascending 2-note spark chime E5 -> B5 (`playGotItChime`)
  - `success(view)` — `CONFIRM` + celebratory 4-note rising arpeggio C5 -> E5 -> G5 -> C6 (`playSuccessChime`)
  - `errorLock(view)` — `REJECT` + gentle low two-note thud G3 -> D3 (`playSoftThud`)
  - `softThud(view)` — `KEYBOARD_TAP` + gentle low two-note thud G3 -> D3 (`playSoftThud`)

---

## 6. Theme, Typography & Shape Tokens (`com.example.ui.theme`)

- **Theme Modes (`ThemeMode`)**: `WARM_DAYLIGHT` (default), `WARM_DARK`, `AMOLED_BLACK`, plus optional Android 12+ `dynamicColor` (which preserves `#000000` background in AMOLED).
- **CompositionLocals**:
  - `LocalKovaTactileColors: KovaTactileColors` — `cardFace`, `cardElevatedFace`, `cardOutline`, `cardLip`, `cardBorderWidthDp`, `emeraldFace/Lip/Soft/onEmeraldFace/onEmeraldSoft`, `cobaltFace/Lip/Soft/onCobaltFace/onCobaltSoft`, `amberFace/Lip/Soft/onAmberFace/onAmberSoft`, `coralFace/Lip/Soft/onCoralFace/onCoralSoft`, `stoneFace/Lip/stoneLabel`, `ringTrack`, `ringTrackBevel`, `pathInactive`, `keyIdea`, `codeRule`, `gotcha`, `isDarkSurface`, `isAmoled`.
  - `LocalKovaMotion: KovaMotionConfig` — `enabled`, `containerBoundsSpec`, `expressiveSpring()` (`spring(dampingRatio = 0.76f, stiffness = 340f)` or `snap()`).
  - `LocalTonalElevationEnabled` is explicitly forced to `false` (`surfaceTint = Color.Transparent`) so True AMOLED remains pure `#000000`.
- **Typography (`Type.kt`)**:
  - `SpaceGroteskFontFamily` (Display & Headlines), `PlusJakartaSansFontFamily` (Body & Labels), `JetBrainsMonoFontFamily` + `KovaCodeTextStyle` (code snippets, debugger lines, badges, metrics).
- **Expressive Shapes (`KovaExpressiveShapes`)**:
  - `HeroChapterCard` (`28.dp`), `StatAndAssignmentCard` (`20.dp`), `LessonSquircleNode` (`26.dp`), `FloatingCompanionWindow` (`28.dp`), `Pill` (`CircleShape`), `AsymmetricPreviewCard`, `ScallopedSeal`, `SoftCloverBadge`.

---

## 7. Non-Negotiable Rules (NEVER BREAK)

1. **3-Lessons/Day Limit & Calendar Rollover**: `QuotaState.dailyLessonsMax = 3`. Passing a *newly completed* lesson decrements `dailyLessonsRemaining` by 1 (unless `devUnlockAllOverride == true`). Opening an uncompleted (`UNLOCKED`) lesson at `0/3` blocks entry with lock shake/snackbar. Crossing a new calendar date (`lastQuotaDateIso != todayIso`) automatically restores `3/3` while preserving all completed lessons and scores.
2. **Mastery Gating & Synchronized Chapter Assignments**: Lessons unlock sequentially only upon passing the previous lesson's integrated test (`>= 80%`). Finishing the final lesson in a chapter unlocks that chapter's Assignment trophy (synced live across `Learn` and `Lab`). Completing the Chapter Assignment unlocks the next chapter.
3. **Dev Override**: Enabling `devUnlockAllOverride` unlocks every chapter, lesson, and assignment and restores `3/3` daily quota; disabling resumes standard mastery gating cleanly.
4. **Crash-Safe Disk Persistence & Room Integrity**: Every study progression, highlight note, chat message, SR update, split ratio, and setting persists to disk (`FileKovaAppStateStore` + `KovaDatabase`). Never use `OnConflictStrategy.REPLACE` on `ChatThreadEntity` (`@Upsert` + optimistic pending buffer prevents vanishing AI messages). `resetDemoProgress()` resets study data while preserving Theme Mode, Dynamic Color, Motion, and Sound & Haptics preferences.
5. **Instant-Restart Zero-Overlap Audio & Haptics**: Always trigger audio/haptics through `LocalKovaFeedback.current` so rapid clicks preempt cleanly on `USAGE_MEDIA` and respect the Settings toggle.
6. **Three Grounded Themes (Zero Neon / Pure AMOLED)**: Never hardcode hex colors in feature composables—always use `MaterialTheme.colorScheme` and `LocalKovaTactileColors`. Keep `WARM_DAYLIGHT` (`#FBF8F3`), `WARM_DARK` (`#141210`), and `AMOLED_BLACK` (`#000000` with `LocalTonalElevationEnabled = false`) crisp and legible.
7. **Phase 1 UX Preservation**: Keep 3D circular chapter rings, Chapter Focus Mode + corner AI circle shortcut, blurred-backdrop 3D Flashcard Focus Stage, persistent AI chat threads with Hide/Unhide history, `75:25 -> 50:50 -> 25:75` split-pane cycling, portrait/landscape progress bars, and `BackHandler` support intact.
