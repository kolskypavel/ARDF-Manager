# Radio-O-Manager Multiplatform Roadmap

This roadmap tracks the move from an Android-only app toward a shared Kotlin
foundation with a desktop beta. The first desktop target is an event-admin MVP,
not full Android parity.

## Current foundation

The `codex/multiplatform-foundation` branch introduces a `:shared` Kotlin
Multiplatform module with Android and desktop JVM targets. Shared code currently
covers portable domain enums, SportIdent code and time helpers, duration
formatting, file definitions, alias validation, control-point parsing, import
validation, result ranking, course evaluation, IOF result-status mapping,
platform-neutral event models, event validation, result placement, project-file
envelope metadata, CSV row formatting, control/punch display formatting,
template rendering, and standard-category row parsing.

Android remains the production app. The Android Room database, UI, USB
SportIdent readout, printing, live result sending, Android resources, and
platform permissions still live in the Android app. Android now has tested
mappers between Room aggregates and shared event models, but Room remains the
Android persistence layer.

## Required gates

Run this gate before committing roadmap/foundation changes:

```shell
./gradlew :shared:check testDebugUnitTest :shared:desktopSmokeRun
git diff --check
```

`desktopSmokeRun` is a shared-module desktop JVM smoke entrypoint. It proves the
desktop target can execute shared business logic, but it is not a desktop app.

Known Room/KSP warnings about missing indexes and `@Transaction` annotations
currently predate this roadmap work. Do not treat those warnings as Stage 1
blockers unless a change in this branch introduces new failures.

## Desktop beta goal

The first desktop release should be a beta event-admin app. It should support:

- creating, opening, editing, saving, and exporting event data;
- managing races, categories, control points, aliases, competitors, readouts,
  and results;
- importing and exporting the supported event/result formats once the shared
  import/export layer is ready.

The desktop beta should explicitly exclude:

- live SportIdent reader integration;
- ticket printing;
- live result sending;
- replacing Android for normal race-day readout operations.

## Stages

### 1. Foundation stabilization

Goal: make the existing shared-module foundation easy to review, test, and hand
off.

- Keep the branch focused on shared extraction and documentation.
- Preserve Android behavior while shared code grows behind tested adapters.
- Document the shared module purpose, desktop smoke task, test gates, and desktop
  limitations.
- Merge only after the required gates pass.

Milestone: the branch is clean, pushed, documented, and another engineer can run
the shared/Android verification commands without extra context.

### 2. Shared event model and services

Goal: move event-domain behavior behind platform-neutral models and services.

- Add shared models for race, category, competitor, alias, control point, result,
  punch, and aggregate race data.
- Keep Android Room entities as platform persistence objects and map them to/from
  shared models.
- Move category validation, competitor validation, alias/control-point
  management, readout/result recalculation, result grouping, and place assignment
  into shared services.
- Use test-driven slices: write shared common tests first, then delegate Android
  code.

Milestone: Android still uses Room, but the core event workflows are expressed
and tested in shared code.

### 3. Shared import/export layer

Goal: make file formats portable while leaving file pickers and permissions on
each platform.

- Move CSV parsing/writing policy, Radio-O-Manager JSON shaping, final/live
  result JSON shaping, and IOF XML policy into shared code where platform-neutral.
- Define shared import/export APIs that accept text/bytes plus shared event data
  and return data or structured validation errors.
- Keep Android streams, content URIs, localized resources, and platform UI in the
  Android app.
- Add golden-file tests for CSV, JSON, and XML.

Milestone: Android import/export behavior remains compatible, and desktop can
reuse the same format code.

### 4. Desktop event-admin MVP

Goal: build the first user-visible desktop app around shared services.

- Add a thin desktop app module or desktop source set, following the shared JVM
  smoke pattern already in place.
- Use file-backed project storage for beta, such as a `.rom.json` event file.
- Build desktop workflows for race selection, category/control-point/alias
  editing, competitor editing, readout/result entry, result recalculation, and
  import/export.

Milestone: a desktop user can create/import an event, edit event data, manually
enter readout-equivalent punch data, recalculate results, save/reopen, and export
results.

### 5. Desktop beta packaging

Goal: produce installable desktop beta artifacts.

- Add version/build metadata for desktop artifacts.
- Add packaging environment checks and package tasks for macOS, Windows, and
  Linux.
- Document beta limitations clearly.
- Verify packaged apps launch and complete the event-admin smoke scenario.

Milestone: beta desktop packages are produced and validated on target desktop
operating systems.

### 6. Post-beta platform features

Goal: add platform-specific capabilities after the event-admin beta is stable.

- Add desktop SportIdent readout behind a platform device interface.
- Add desktop printing behind a platform print interface.
- Add live result sending after the network/result-service logic is isolated
  from Android WorkManager.

Milestone: each platform feature lands behind shared tests plus platform smoke
tests without regressing Android.

## Shared SQL decision

Shared SQL is not on the critical path for the first desktop beta.

Use file-backed project storage for the desktop beta while shared domain models,
services, and import/export APIs stabilize. After the beta, run a bounded shared
SQL spike with Room KMP as the baseline candidate. SQLDelight remains the
fallback/comparison option if Room KMP limitations are unacceptable.

Reasons:

- The current Android app already has a Room schema, DAOs, relations, migrations,
  flows, and transactions.
- Moving persistence into shared SQL now would be a storage migration, not a
  small adapter change.
- The desktop beta primarily needs reliable event files and shared business
  logic.
- Deferring shared SQL reduces risk and gets a useful desktop package in users'
  hands sooner.

## Acceptance criteria for Stage 1

- `docs/multiplatform-roadmap.md` exists and describes the staged roadmap.
- README links to this roadmap.
- The branch passes:

```shell
./gradlew :shared:check testDebugUnitTest :shared:desktopSmokeRun
git diff --check
```

- The branch is committed and pushed to `origin/codex/multiplatform-foundation`.
