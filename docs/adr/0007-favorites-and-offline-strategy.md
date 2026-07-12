# ADR 0007: Favorites storage and offline image strategy

**Status:** accepted (Phase 4)

## Context

Favorites must hold two different content types (APOD entries, Mars photos), stay fully
viewable offline including images, and outlive every other cache (the Mars feed cache is
cleared on every refresh).

## Decision

- **One self-contained `favorites` table** with a `type` discriminator and nullable
  per-type payload columns, keyed by a namespaced id (`apod:<date>` / `mars:<imageid>`).
  A favorite row carries everything its card and future detail need — no joins against
  volatile caches. Mapped to a sealed `Favorite` domain type; unknown `type` values map
  to null defensively.
- **Images are prefetched on save.** Toggling a favorite on downloads its image straight
  into Coil's disk cache (no memory cache entry, no decode via `BlackholeDecoder`).
  The app configures a 256 MB disk cache through `SingletonImageLoader.Factory`; Coil 3
  ignores HTTP cache headers by default, so cached images serve offline indefinitely.
  The prefetch hides behind a `fun interface ImagePrefetcher` in `core:domain`, keeping
  the data layer free of Coil and trivially fakeable in tests.
- **Offline banner** (`OfflineBanner` in the design system): feeds that fail to refresh
  while holding cached rows show the banner above the cached content instead of a
  full-screen error; the full error only appears when there is nothing to show.

## Consequences

- Favorites and their images survive process death, reinstalls of other caches, and
  airplane mode (verified on the emulator with a cold restart offline).
- Denormalized rows mean a saved favorite does not reflect later upstream edits —
  accepted: NASA archive entries are effectively immutable.
- The wide nullable table trades relational purity for a single ordered feed and a
  one-query screen; a second content family later (e.g. search results) is one more
  `type` value, not a schema redesign.
