# Project context

## Purpose and scope

A reusable, independently buildable Android-only starter. Clone, rename, configure, and
extend it for a real product. [ARCHITECTURE](ARCHITECTURE.md) owns the canonical patterns,
[AGENTS](../AGENTS.md) the operating rules, and [DECISIONS](DECISIONS.md) the rationale.

The implemented sample is a catalog with list and detail screens. It exercises the
architecture without imposing financial/business behavior from the Crypto reference.
Authentication, durable storage, analytics, realtime streams, multi-step flows, a published
UI library, and shared cross-platform code are outside the initial implementation.
Optional UseCases, validators, and coordinators remain documented extension patterns;
none is added solely to populate a layer.

## Current implementation — 2026-10-14

| Area | Implemented |
|---|---|
| Root / build | `AndroidStarter`; local Git repository on `main`, no remote; one `:app` module, Gradle wrapper, version catalog |
| Identity | `com.example.androidstarter` namespace/application ID; Android Starter label; replace before shipping |
| Toolchain | AGP 9.0.1, Gradle 9.1.0, Kotlin 2.3.21, KSP 2.3.9; JDK 17+ / bytecode 17; tested with JBR 21 |
| Android | minSdk 26, compileSdk/targetSdk 36; API 37 emulator used for device validation |
| UI / DI | Compose BOM 2026.03.00, Material 3, Hilt 2.59.2; light/dark local theme |
| Navigation | Navigation 3 1.0.1, Lifecycle 2.10.0 entry decorators, Hilt Compose 1.3.0; serializable ID-only destination keys |
| Data | Retrofit 3.0.0, OkHttp 4.12.0, kotlinx.serialization 1.9.0; offline demo or configured HTTP API |
| Tests | ViewModels, mapping, HTTP contracts/errors, cancellation, duplicate navigation; Compose navigation/lifecycle test |
| Distribution | Debug signing only; minified release configuration without production signing; no publishing or CI service configured |

Versions are pinned to a compatible baseline, not advertised as latest. The version
catalog is authoritative for dependencies. AGP 9 provides built-in Kotlin; do not add
the legacy `org.jetbrains.kotlin.android` plugin. Upgrade deliberately with validation.

### Implementation map

Paths below are relative to `app/src/main/kotlin/com/example/androidstarter/`:

- `app/`: Hilt application, Activity, Navigation 3 host and destination keys.
- `core/designsystem/`: local Material 3 theme; `core/network/`: shared HTTP/JSON providers;
  `core/result/`: transport-independent result/error contract.
- `feature/products/domain/`: immutable Product and repository interface.
- `feature/products/data/`: DTO/API/demo provider, mapping and repository implementation.
- `feature/products/di/`: binds the repository and chooses demo versus Retrofit API.
- `feature/products/presentation/list/` and `details/`: independent screen contracts,
  ViewModels, routes, and content; `components/`: shared feature feedback UI.

The detail screen reloads the catalog and selects its ID; there is deliberately no cache
or invented detail endpoint. Replace this strategy when the product API requires it.
Entry-scoped ViewModels are retained across configuration changes and cleared on pop.
The route passes restored destination IDs through Hilt assisted injection. A buffered
Channel has one RESUMED route consumer; the host checks its active destination, and the
list gates duplicate taps until resumed. No navigation command is stored in screen state.

### Networking configuration

Without `starter.apiBaseUrl`, `DemoProductsApi` provides deterministic in-memory sample
DTOs without network calls. Setting this Gradle property selects Retrofit at build time.
The placeholder `https://example.invalid/` is never contacted in demo mode.

The sample remote contract is `GET <base URL>/products`, returning an array:

```json
[{"id":"field-notes","name":"Field notes","summary":"A short summary","description":"Full description"}]
```

All fields are required; unknown JSON fields are ignored. Blank IDs/names, duplicate IDs,
missing body, and decoding failures become `InvalidData`. HTTP 401/403 become Unauthorized,
404 NotFound, other 4xx Rejected, and other unsuccessful responses Server. IO failures
become Network; cancellation propagates. There is no automatic retry, auth header, disk
cache, payload logging, or implicit credential storage. Repository tests use MockWebServer.

### Validation record

Executed locally on 2026-10-14:

- `:app:testDebugUnitTest`: 19 tests passed (list/details ViewModels, mapper, repository).
- `:app:assembleDebug` and `:app:assembleRelease`: passed; release is minified and unsigned.
- `:app:lintDebug`: passed with no errors. Remaining warnings advise newer dependency,
  Gradle, AGP, and target SDK versions; they are not suppressed.
- `:app:connectedDebugAndroidTest`: one end-to-end test passed on API 37. It verifies
  list/detail navigation, activity recreation, background/resume, back handling, returning
  without action replay, and opening a different item.
- Manual emulator checks: list/detail screens visually reviewed; detail destination and
  product restored after confirmed process termination and restart (different process IDs).
- Espresso 3.7.0 is explicitly pinned for this emulator; Compose otherwise resolved
  Espresso 3.5.0, which failed on removed Android input APIs. See the official
  [Espresso release notes](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0).

Not yet validated: a real backend, deep links (not implemented), physical devices,
minimum API 26 runtime, production signing/distribution, or product-specific business flows.

Reports are generated in `app/build/reports/`. They are ignored build outputs, not
committed documentation. Test reports are evidence only for the executed configuration.

## Clone, rename, configure

1. Copy/clone the source repository into a product-owned location; start its own Git history
   if appropriate. Keep the wrapper, version catalog, agent contract, and documentation.
2. Rename `rootProject.name`, app label, package directories/declarations, namespace,
   application ID, test packages, and corresponding imports. Replace the example identity.
3. Update the product context below and the feature inventory. Remove or replace the sample
   catalog and demo provider; configure your API contract and endpoint explicitly.
4. Define environments, app links, authentication, persistence/backup policy, and signing
   only as needed. Keep secrets and `local.properties` out of version control. Backup is
   disabled by default; reconsider it when the product introduces durable local data.
5. Validate the configured build, unit tests, lint, and relevant device scenarios per AGENTS.
   Add product-specific CI and release steps; do not assume they exist.

## Replace for a real product

Rewrite this file for the actual app; remove obsolete starter descriptions. Retain links
to the architecture and decisions and keep facts separate from planned work.

- **Product / users / problem:** TODO.
- **Scope / non-goals / acceptance criteria:** TODO.
- **Features / owning packages / business rules:** TODO; link to source and requirements.
- **API contracts / environments / configuration:** TODO; never include secret values.
- **Implemented / partial / planned:** TODO.
- **Verified setup / validation / CI / release:** TODO.
- **Constraints / unresolved decisions:** TODO.
