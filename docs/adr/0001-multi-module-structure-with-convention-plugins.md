# ADR 0001: Multi-module structure with convention plugins

**Status:** accepted (Phase 0)

## Context

The app will grow to five features backed by network, database, and datastore layers. A single
module would couple everything together, slow incremental builds, and make layer violations
invisible. Repeating Gradle configuration in every module is error-prone.

## Decision

- Split the project into `:app`, `:core:*`, and `:feature:*` modules with an enforced dependency
  direction (features → core; `core:domain`/`core:common` are pure JVM modules with no Android
  dependencies).
- Centralize shared build configuration in an included build `build-logic/` exposing convention
  plugins (`cosmos.android.application`, `cosmos.android.library`, `cosmos.android.compose`,
  `cosmos.android.feature`, `cosmos.jvm.library`, `cosmos.hilt`). Module build files declare only
  what is unique to the module (namespace, dependencies).
- All versions live in a single version catalog (`gradle/libs.versions.toml`), shared with
  `build-logic` via `versionCatalogs { from(files(...)) }`.

## Consequences

- New modules are one-liners: apply a convention plugin, set a namespace.
- Dependency direction is enforced by the build graph, not by discipline.
- Slightly more upfront complexity in `build-logic/`, which is standard practice in modern
  multi-module Android projects (cf. Now in Android).
