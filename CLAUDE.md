# Agent Prompt: "Cosmos" — NASA Explorer Android App

> How to use this document:
> - **Claude Code:** put the "Project Constitution" section into `CLAUDE.md` at the repo root, then start with the "Kickoff Prompt". Work phase by phase.
> - **Cursor / other agents:** paste the whole document as the first message, then say "Start Phase 0".
> - Review every diff. Ask the agent to explain any decision you couldn't defend in an interview.

---

## KICKOFF PROMPT

You are a senior Android engineer. Build a production-quality Android portfolio app called **Cosmos** — a NASA data explorer. The owner of this repo is a junior+/middle Android developer; the codebase must demonstrate modern Android best practices (2026) that a hiring manager would review. Prioritize code clarity, testability, and idiomatic use of the modern stack over feature count.

Work in small, reviewable increments: one feature or module per session, each ending with passing tests, a green `./gradlew check`, and a conventional commit. Never leave the project in a non-compiling state. Before using any library API you are not 100% sure about, consult Context7 docs. After implementing UI, verify it on the emulator via mobile-mcp (launch the app, screenshot each new screen, check for visual defects and crashes in logcat).

---

## PROJECT CONSTITUTION (put in CLAUDE.md / rules)

### Product scope

Cosmos lets users explore NASA's open APIs (https://api.nasa.gov, register a free API key; also the NASA Image and Video Library at https://images-api.nasa.gov which needs no key):

1. **APOD (Astronomy Picture of the Day)** — today's picture with explanation; browsable calendar/history; supports video-type entries (show thumbnail + open externally).
2. **Mars Rover Photos** — infinite-scroll photo feed filterable by rover (Curiosity, Perseverance) and camera; photo detail with pinch-to-zoom.
3. **NASA Media Library Search** — search images across NASA's library with debounced query, paginated results, and search history.
4. **Favorites** — save any APOD entry or photo locally; fully viewable offline, including cached images.
5. **Daily APOD notification** — WorkManager-scheduled daily check that posts a notification with the new APOD (respect POST_NOTIFICATIONS runtime permission on API 33+).
6. **Settings** — theme (system/light/dark, dynamic color toggle), notification on/off, clear cache. Persisted via DataStore.

Non-goals (do NOT build): auth/accounts, backend of our own, iOS/KMP targets, analytics SDKs.

### Tech stack (non-negotiable)

- **Language/build:** Kotlin (latest stable), Gradle Kotlin DSL, **version catalog** (`libs.versions.toml`), **convention plugins** in `build-logic/` for shared module config. KSP, not kapt.
- **UI:** 100% Jetpack Compose + Material 3, edge-to-edge, dynamic color, dark theme. Type-safe Navigation Compose (kotlinx.serialization routes). Adaptive layout for the photo grid (WindowSizeClass — list/detail pane on tablets is a stretch goal).
- **Architecture:** Clean Architecture + **MVI** presentation pattern (sealed UI state, sealed user intents/events, one-off effects via Channel/Flow). Unidirectional data flow. ViewModels expose a single `StateFlow<UiState>`.
- **DI:** Hilt.
- **Async:** Coroutines + Flow only. No RxJava. No LiveData.
- **Network:** Retrofit + OkHttp + kotlinx.serialization converter. OkHttp interceptor injects the API key; logging interceptor in debug builds only. The API key comes from `local.properties` / CI secret via BuildConfig — never hardcode it, never commit it.
- **Local:** Room (entities separate from domain/network models), DataStore Preferences for settings.
- **Pagination:** Paging 3 with **RemoteMediator** for the Mars photos feed (network + Room as single source of truth); plain PagingSource for media search.
- **Images:** Coil 3 with a configured disk cache; enable offline reads of cached images for favorites.
- **Background:** WorkManager (Hilt-integrated) for the daily APOD worker with network constraints and backoff.
- **Quality:** detekt + ktlint (Compose rules), JUnit5 where practical, MockK, Turbine for Flow testing, coroutines-test, Compose UI tests, Roborazzi screenshot tests are a stretch goal.
- **CI:** GitHub Actions — on every PR: lint, detekt, unit tests, assembleDebug; cache Gradle. Separate workflow for release build on tags.

### Module structure (multi-module, enforce dependency direction)

```
:app                          // Application, MainActivity, root NavHost, DI wiring
:build-logic                  // convention plugins
:core:common                  // Result wrapper, dispatchers qualifier, error types
:core:network                 // Retrofit/OkHttp setup, DTOs, API services
:core:database                // Room db, DAOs, entities
:core:datastore               // settings DataStore
:core:designsystem            // theme, typography, reusable components (CosmosButton, ErrorState, LoadingState, shimmer)
:core:ui                      // shared compose utilities used by features
:core:domain                  // use cases, domain models, repository interfaces (pure Kotlin/JVM module, NO Android deps)
:core:data                    // repository implementations, mappers, RemoteMediator
:feature:apod
:feature:marsrover
:feature:search
:feature:favorites
:feature:settings
```

Rules: features never depend on other features; `:core:domain` is a pure JVM module; DTOs, entities and domain models are separate classes with explicit mappers; repositories return domain models wrapped in a `Result`/error-typed flow — never DTOs.

### Error handling & UX rules

- Every network call maps failures into a sealed `AppError` (NoConnection, Http(code), RateLimited, Unknown) surfaced to UI as retryable error states.
- Every screen has explicit Loading / Content / Empty / Error states — no blank screens.
- Offline behavior: favorites always work offline; feeds show cached data with an "offline" banner when refresh fails.
- Respect process death: state that must survive uses SavedStateHandle (e.g., search query, selected filters).

### Testing requirements (definition of done per feature)

- Unit tests for every use case and repository (MockK + Turbine), covering success, error, and edge cases.
- ViewModel tests asserting full MVI state transitions with a test dispatcher.
- Mapper tests for DTO→domain→entity conversions.
- At least one Compose UI test per feature verifying state rendering (loading/error/content).
- Room DAO instrumented tests for favorites.
- CI must be green before a feature is "done".

### Git & documentation discipline

- Conventional commits (`feat:`, `fix:`, `test:`, `refactor:`, `ci:`); one logical change per commit; feature branches + PRs with descriptions.
- Maintain `docs/adr/` — write a short Architecture Decision Record whenever making a significant choice (MVI vs MVVM, RemoteMediator strategy, module boundaries).
- README must include: banner + screenshots/GIF, feature list, tech stack table, architecture diagram (Mermaid), module graph, "Decisions & trade-offs" section, build instructions (including where to put the NASA API key), CI badge, test coverage note.

---

## PHASED PLAN (execute in order; stop after each phase for my review)

**Phase 0 — Skeleton.** Repo init, version catalog, convention plugins, module structure with empty modules, detekt/ktlint config, GitHub Actions CI, theme + design system basics, root navigation with placeholder screens. Deliverable: green CI on a walking skeleton.

**Phase 1 — APOD.** Network layer for APOD, domain + data + feature module, today's APOD screen + date picker for history, full state handling, tests. Verify on emulator.

**Phase 2 — Mars Rover feed.** Paging 3 + RemoteMediator + Room caching, filter UI (rover/camera), photo detail with zoom, tests including RemoteMediator test.

**Phase 3 — Search.** Media library search with debounce (Flow operators), paging, search history in Room, tests.

**Phase 4 — Favorites + offline.** Favorite toggle from APOD/detail screens, favorites screen, Coil offline caching strategy, offline banner logic, DAO tests.

**Phase 5 — Notifications + Settings.** WorkManager daily APOD worker, permission flow, settings screen with DataStore, worker test.

**Phase 6 — Polish.** Animations (shared element transitions on photo open), adaptive layout pass, accessibility pass (content descriptions, TalkBack sanity check via emulator), baseline profile (stretch), README + screenshots + ADRs finalized, release build config with R8, signed AAB instructions.

---

## AGENT TOOLING SETUP (MCP servers to install before starting)

1. **Context7** — live, version-correct library docs. Always query it before using Paging 3, Media3, Navigation Compose, WorkManager, or Coil APIs.
   `claude mcp add context7 -- npx -y @upstash/context7-mcp`
2. **GitHub MCP (official)** — create branches/PRs, read Actions failures. Use a fine-grained PAT scoped to this repo only.
3. **mobile-mcp** (mobile-next/mobile-mcp) — drive the Android emulator: install the APK, launch, screenshot every new screen, inspect UI elements, and check logcat after each feature.
   `claude mcp add mobile -- npx -y @mobilenext/mobile-mcp@latest` (requires Android SDK + a running AVD)

Workflow contract with these tools: implement → `./gradlew check` → install on emulator → screenshot + logcat verification via mobile-mcp → commit → open PR via GitHub MCP → confirm CI green.

---

## WHAT NOT TO DO

- No god-modules, no `utils` dumping ground, no wildcard imports.
- No `GlobalScope`, no `runBlocking` in production code, no swallowed exceptions.
- No skipping tests "to save time" — untested code is not done.
- No committing secrets, and no committing directly to `main`.
- Do not add libraries beyond the stack above without asking me first and writing an ADR.