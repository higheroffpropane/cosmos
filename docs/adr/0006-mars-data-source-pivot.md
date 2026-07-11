# ADR 0006: Mars photos come from mars.nasa.gov raw-image feeds

**Status:** accepted (Phase 2) — supersedes the data source assumed in ADR 0005

## Context

Emulator verification of Phase 2 revealed that the officially documented Mars Rover Photos
API (`api.nasa.gov/mars-photos/*`) is permanently offline: NASA's gateway proxies to a
community-maintained Heroku app that no longer exists, and the backing project
(corincerami/mars-photo-api) was archived in October 2025 with no successor. Every request
returns 404 with Heroku's "No such app" page.

NASA's own mission websites are powered by live, keyless raw-image feeds:

- **Perseverance:** `https://mars.nasa.gov/rss/api/?feed=raw_images&category=mars2020&feedtype=json`
  (`images[]`, `camera.instrument`, multi-size `image_files`, `total_results`)
- **Curiosity:** `https://mars.nasa.gov/api/v1/raw_image_items/?condition_1=msl:mission`
  (`items[]`, `instrument`, single `https_url`)

## Decision

- Feed the Mars Rover feature from those two endpoints (absolute URLs on the shared
  Retrofit instance; the api-key interceptor only signs `api.nasa.gov` requests).
- Both feeds support `order=sol+desc` with plain page-number pagination, so the
  RemoteMediator uses a per-rover page cursor — the sol-walking logic from ADR 0005 is
  obsolete and removed.
- Camera filtering is applied locally in the Room query on the cached `instrument` id;
  the per-rover camera chip lists are curated from the instruments each feed actually serves.
- Photo ids are the feeds' string `imageid` (there is no shared numeric id).

## Consequences

- Both rovers work today, with no API key or rate-limit concerns for this feature.
- The endpoints are undocumented; NASA could change them. The DTO/mapper isolation keeps
  any future change confined to `core:network` + one mapper file.
- Camera-filtered feeds only page in rover-wide order, so rare cameras may require more
  scrolling to fill (Paging keeps appending automatically).
