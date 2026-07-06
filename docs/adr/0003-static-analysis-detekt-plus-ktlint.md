# ADR 0003: Static analysis — detekt and ktlint side by side

**Status:** accepted (Phase 0)

## Context

We want both code-smell analysis and strict formatting, including Compose-specific rules
(modifier conventions, composable naming, preview visibility). Options considered: detekt with
its `detekt-formatting` wrapper (runs ktlint inside detekt), or dedicated plugins for each tool.

## Decision

- **detekt** (via its Gradle plugin) for code smells, configured by `config/detekt/detekt.yml`
  building on the default config, with Compose-aware relaxations (`ignoreAnnotated: [Composable]`
  for naming/complexity/magic-number rules).
- **ktlint** (via `org.jlleitschuh.gradle.ktlint`) for formatting, configured through
  `.editorconfig` (`android_studio` code style).
- Both load the `io.nlopez.compose.rules` rule sets (detekt and ktlint flavors respectively).
- `detekt-formatting` is NOT used, so formatting is owned by exactly one tool (ktlint) and there
  are no duplicate/conflicting findings.
- Both are applied to every module by the convention plugins and hook into `./gradlew check`,
  which CI runs on every PR.

## Consequences

- `./gradlew check` fails on any smell or formatting violation (`maxIssues: 0`).
- `./gradlew ktlintFormat` auto-fixes formatting locally.
- Two tools to version-bump, but each has a single responsibility.
