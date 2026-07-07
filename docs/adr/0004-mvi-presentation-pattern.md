# ADR 0004: MVI presentation pattern

**Status:** accepted (Phase 1)

## Context

The constitution mandates unidirectional data flow with a single observable state per screen.
The concrete shape (MVVM with multiple fields vs. MVI with one sealed state) still had to be fixed
when building the first real screen (APOD).

## Decision

Every feature follows the same MVI contract, defined next to the screen (`*Contract.kt`):

- **State** — one `sealed interface XxxUiState` (`Loading` / `Content` / `Error(AppError)`;
  `Empty` where a screen can be legitimately empty). The ViewModel exposes exactly one
  `StateFlow<XxxUiState>`.
- **Intents** — one `sealed interface XxxIntent` for every user action; the screen calls a single
  `onIntent(intent)` entry point. No other public mutating methods on the ViewModel.
- **Effects** — one-off events (navigation, opening URLs, snackbars) flow through a
  `Channel<XxxEffect>().receiveAsFlow()`, so they fire exactly once and are not replayed
  on recomposition or configuration change.
- State that must survive process death (selected APOD date, future: search query, filters)
  lives in `SavedStateHandle`, not in the StateFlow.
- Screens split into a stateful wrapper (`XxxScreen`, ViewModel-aware) and a stateless
  `XxxContent(state, onIntent)` that unit/UI tests render directly.

## Consequences

- State transitions are exhaustively testable: tests assert full `Loading → Content/Error`
  sequences with Turbine.
- Recomposition can never observe half-updated state (single atomic state object).
- Slightly more ceremony than ad-hoc MVVM; the shared contract shape keeps it uniform
  across features.
