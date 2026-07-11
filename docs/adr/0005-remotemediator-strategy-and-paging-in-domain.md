# ADR 0005: Mars feed RemoteMediator strategy and Paging types in the domain layer

**Status:** accepted (Phase 2)

## Context

The Mars Rover Photos API has no cursor-style feed: photos are queried per Martian day
(`sol`) with `page` (25/page), and some sols have no photos (e.g. solar-conjunction gaps).
We want an infinite, offline-cached feed (network + Room as single source of truth)
filterable by rover and camera.

## Decision

**Sol-walking RemoteMediator.** The continuation key is `(sol, page)`, persisted per query
(`rover|camera`) in a `mars_remote_keys` table:

- REFRESH fetches the mission manifest for `max_sol`, clears the rover's cached photos,
  and loads `(max_sol, page 1)`.
- APPEND continues with the stored key. An empty page means the sol is exhausted, so the
  mediator walks to `(sol - 1, page 1)`, scanning at most 15 sols per load to bound API
  usage; if all scanned sols are empty the feed reports end of pagination.
- The camera filter is part of the API query and the local Room query; photos themselves
  are cached per rover, so switching cameras reuses the cache where possible.

**`androidx.paging:paging-common` is allowed in `:core:domain`.** The repository interface
returns `Flow<PagingData<MarsPhoto>>`. paging-common is a Kotlin Multiplatform artifact with
a plain JVM variant — no Android dependency — so the domain module stays a pure JVM module.
The alternative (exposing page-request/page-result models and adapting them in the data
layer) would re-implement half of Paging for no practical gain.

## Consequences

- Feed scrolls seamlessly across sols and survives offline restarts from cache.
- Rare-camera queries may report end-of-pagination early (after 15 empty sols); the UI
  shows what is cached plus an empty state rather than hammering the API.
- Domain unit tests can construct `PagingData` directly (`PagingData.from`).
