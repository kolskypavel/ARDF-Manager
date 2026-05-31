# Dependency Update TODO

This list was intentionally deferred until after Android regression testing,
baseline labeling, and SI reader hardware checks. Those Android hardware checks
passed on 2026-05-31 and are recorded in
[`android-regression-2026-05-31.md`](android-regression-2026-05-31.md). The
current multiplatform foundation work should remain behavior-preserving for
Android while giving future desktop work a cleaner shared-code base.

## Dependency Management

- Introduce a Gradle version catalog at `gradle/libs.versions.toml` without
  changing dependency versions in the first slice.
- Move root plugin versions and app/shared library coordinates into the version
  catalog so future updates are easier to review.
- Keep coupled versions grouped together, especially Kotlin/KSP, Room
  runtime/compiler, Navigation plugin/runtime artifacts, OkHttp artifacts, and
  AndroidX test artifacts.
- Add dependency update automation, preferably Renovate, with conservative
  grouping rules for Gradle, Kotlin, AndroidX, Firebase, and JitPack libraries.
- Consider Gradle dependency locking and dependency verification metadata,
  especially because several important dependencies are resolved through
  JitPack.

## Library Robustness

- Done: moved `androidx.test.ext:junit-ktx` out of app runtime dependencies
  and into the instrumentation-test dependency scope.
- Checked: `android.enableJetifier=true` is still required while
  `com.github.ISchwarz23:SortableTableView` brings in old support-library
  coordinates. Revisit after replacing or isolating that table-view dependency.
- Gate or remove production `HttpLoggingInterceptor.Level.BODY` logging so live
  result publishing does not expose sensitive payloads or depend on verbose
  logging behavior after OkHttp updates.
- Review the unused `app/libs/android-tableview-kotlin-0.1.0-alpha` tree.
  Delete it if it is historical only, or document it clearly if it is retained
  as source fallback for the SortableTableView dependency.
- Keep shared-module dependencies minimal so the desktop target remains easy to
  build and library updates do not drag Android-only APIs into shared code.

## Verification Policy

- Use the existing project gate for normal dependency-management changes:
  `./gradlew :shared:check testDebugUnitTest :shared:desktopSmokeRun :app:assembleDebug :app:assembleDebugAndroidTest`.
- For dependency version updates, also run Android hardware checks when
  practical, including install, launch, foreground/logcat smoke, and SI reader
  connect/read/disconnect once hardware is available.
- Fix or quarantine the stale CSV instrumentation assertions before treating
  `:app:connectedDebugAndroidTest` as a strict update gate.
- Record dependency-update evidence in commit messages or release notes so the
  source development team can see what was changed, what was verified, and what
  remains deferred.

## Handoff Goal

- Preserve a clean, labeled Android-equivalent baseline that the upstream/fork
  source development team can inspect without needing to accept future desktop
  work.
- Keep handoff branches and tags understandable without Codex-specific context.
- Prefer small, reviewable dependency-update commits after the handoff baseline,
  so the source team can adopt the refactored shared-code foundation without
  also taking unrelated library churn.
