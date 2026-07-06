# ADR 0002: AGP 9 built-in Kotlin and version alignment strategy

**Status:** accepted (Phase 0)

## Context

The project was scaffolded with AGP 9.2 / Gradle 9.4. Since AGP 9.0, Kotlin support is built into
the Android Gradle plugin: the `org.jetbrains.kotlin.android` plugin must NOT be applied, and AGP
9.2 embeds KGP 2.3.10. Compiler-plugin versions (Compose compiler, kotlinx.serialization) must
match the Kotlin compiler version in use.

## Decision

- Android modules rely on AGP's built-in Kotlin; no explicit Kotlin Android plugin anywhere.
- The catalog pins `kotlin = 2.3.10` — exactly the KGP version embedded in AGP 9.2 — and this
  single version drives `org.jetbrains.kotlin.jvm` (pure JVM modules),
  `org.jetbrains.kotlin.plugin.compose`, and `org.jetbrains.kotlin.plugin.serialization`.
- KSP uses its decoupled versioning (2.3.x line, which supports AGP built-in Kotlin since 2.3.1).
- Kotlin `jvmTarget` is not set explicitly in Android modules: with built-in Kotlin it defaults to
  `compileOptions.targetCompatibility` (Java 11 here). JVM modules set the same target explicitly.

## Consequences

- One Kotlin version across the whole build; no KGP/AGP conflict warnings.
- Upgrading AGP requires checking which KGP it embeds and moving `kotlin` in lockstep.
