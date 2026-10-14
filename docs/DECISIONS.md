# Architecture decisions

Accepted means an architectural choice, not proof that every optional pattern is implemented. See
[PROJECT_CONTEXT](PROJECT_CONTEXT.md) for evidence of implementation and
[ARCHITECTURE](ARCHITECTURE.md) for mechanics. Defaults apply when no different established
starter choice exists. When superseding an entry, retain its ID/history and link the
replacement; preference alone is not a revisit trigger.

## DEC-001 — Independent Android-only starter

- **Status:** Accepted.
- **Decision:** Maintain an independently buildable Android application starter using Kotlin and Compose/Material 3; no shared iOS/Web runtime requirement.
- **Reason:** Keep ownership, tooling, and product adoption small and explicit.
- **Consequences:** Other platforms may share concepts but do not impose build or library dependencies. Products clone and configure their own repository.
- **Revisit when:** A concrete product requires shared platform code and can justify its coupling and maintenance cost.

## DEC-002 — ViewModel-based MVI/UDF screen contract

- **Status:** Accepted.
- **Decision:** Use screen-local `ScreenState`, `UiState`, `UiEvent`, `UiAction`; expose `StateFlow<ScreenState>` and `onEvent(UiEvent)` from plain ViewModels for non-trivial screens.
- **Reason:** Preserve the inspected Crypto screen model with explicit, testable behavior.
- **Consequences:** Compose renders immutable state. Complex screens may compose concrete section states; no generic state engine is needed.
- **Revisit when:** A demonstrated screen requirement cannot be expressed clearly through this contract; attempt local composition first.

## DEC-003 — Persistent state and transient actions are separate

- **Status:** Accepted; explicit project policy with delivery limitations.
- **Decision:** Emit transient effects through `Flow<UiAction>`; use a buffered Channel for one consumer by default, SharedFlow only for intentional broadcast semantics. Do not store actions in state.
- **Reason:** Repeatable rendering must not replay one-time navigation/dialog/snackbar commands.
- **Consequences:** Specify lifecycle, buffering, collector ownership, stale-action handling, and cancellation behavior. Delivery is not exactly once; durable business outcomes require state/storage independently.
- **Revisit when:** A concrete effect requires guaranteed/restorable processing; model the durable workflow and reconsider transport without putting command wrappers in screen state.

## DEC-004 — Hilt and constructor injection

- **Status:** Accepted default.
- **Decision:** Use Hilt, constructor injection, interface bindings, and providers for external objects.
- **Reason:** Explicit dependencies integrate with Android lifecycle owners and remain easy to replace in tests.
- **Consequences:** Validate scopes and navigation ownership. Prefer direct construction with fakes in unit tests; no service locator or interface per class requirement.
- **Revisit when:** An established starter uses another supported DI solution or validated integration constraints materially outweigh migration cost.

## DEC-005 — Android HTTP networking stack

- **Status:** Accepted default.
- **Decision:** Use Retrofit + OkHttp + kotlinx.serialization, with DTOs and transport handling confined to data. Expose small `AppResult` / `AppError` contracts.
- **Reason:** Separate HTTP/serialization concerns from application models and screen behavior.
- **Consequences:** Validate compatible versions, parsing and error policies; preserve cancellation. GraphQL and bank result types are not starter requirements.
- **Revisit when:** An established stack or a concrete protocol/backend requirement cannot be served reasonably by this choice.

## DEC-006 — Single-module first

- **Status:** Accepted default.
- **Decision:** Start with one Gradle `:app` module and a version catalog. Express boundaries through packages first.
- **Reason:** Reduce bootstrap, build, and dependency-management overhead.
- **Consequences:** Package boundaries require review/testing discipline. New modules need measured build, reuse, isolation, or ownership benefits.
- **Revisit when:** Independent consumers, team ownership, dependency enforcement, or measured build constraints justify a specific extraction.

## DEC-007 — UseCases are optional

- **Status:** Accepted.
- **Decision:** Allow direct ViewModel-to-repository calls; introduce UseCases for meaningful business logic, orchestration, validation, aggregation, or reuse.
- **Reason:** Forwarding-only classes obscure simple execution paths.
- **Consequences:** Features can differ in depth while retaining the same repository boundary. Prefer concrete UseCases; interfaces are justified separately.
- **Revisit when:** Growing/reused behavior needs a dedicated domain operation, not merely because another feature has one.

## DEC-008 — Composition without base-class frameworks

- **Status:** Accepted; strict constraint.
- **Decision:** Use ordinary AndroidX lifecycle classes, concrete collaborators, and composed rules. Do not introduce custom base ViewModels/repositories/UseCases/screens/tests or a generic MVI/validation framework.
- **Reason:** Remove proprietary coupling and hidden inherited behavior from the reference approach.
- **Consequences:** Small explicit boilerplate is acceptable. Reuse helpers, interfaces, functions, and composition; AGENTS owns the exact forbidden list.
- **Revisit when:** Repeated real behavior merits a composed helper. Relaxing the prohibition itself requires an explicit architectural scope change, not convenience.

## DEC-009 — Feature-first packages

- **Status:** Accepted.
- **Decision:** Place feature-owned presentation/domain/data together; place genuinely shared infrastructure in `core` and app composition in `app`.
- **Reason:** Keep code discovery and change scope aligned with user-facing behavior.
- **Consequences:** Screens have separate packages and contracts. Promote shared contracts deliberately; do not couple one feature to another's data implementation.
- **Revisit when:** Concrete ownership or dependency conflicts require adjusted boundaries or justified modules.

## DEC-010 — Local design system

- **Status:** Accepted default.
- **Decision:** Keep theme, tokens, and reusable Compose primitives in `core/designsystem` inside `:app`.
- **Reason:** The starter has no verified independent library consumer or release lifecycle.
- **Consequences:** Feature-specific UI stays local; use composable slots/functions rather than inherited screen wrappers. No separate artifact publishing is required.
- **Revisit when:** Multiple actual applications need independent consumption and can justify versioning and release ownership.

## DEC-011 — Behavioral testing through composition

- **Status:** Accepted.
- **Decision:** Strong unit coverage for ViewModels, UseCases, validators/rules, parsers/mappers, coordinators, and repositories; focused UI/integration tests for framework behavior.
- **Reason:** Protect business and async behavior while keeping the core fast to validate.
- **Consequences:** Use deterministic scheduling and fakes; include error, cancellation, and boundary cases. Share fixtures/rules without inherited MVI test frameworks. Commands live in AGENTS.
- **Revisit when:** Production failures or test instability reveal a concrete missing contract or unsuitable test layer.

## DEC-012 — Navigation 3, subject to integration validation

- **Status:** Accepted and integrated; build and navigation/lifecycle test validated on 2026-10-14; revisable.
- **Decision:** Use Navigation 3 for the Compose host unless an existing choice or demonstrated integration friction justifies another implementation.
- **Reason:** Its explicit back-stack ownership fits the chosen state-driven host boundary.
- **Consequences:** Prove Hilt/ViewModel ownership, cleanup, back behavior, restoration, and required deep links. ViewModels retain semantic actions independent of the navigation library.
- **Revisit when:** A reproducible incompatibility, unmet product requirement, or disproportionate integration cost appears; record evidence, alternative, and migration impact before switching.
