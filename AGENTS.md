# Android Starter — agent operating contract

This is a reusable Android application starter. Architecture documents define the
canonical contract; they are not evidence that every optional pattern exists. Check
[PROJECT_CONTEXT](docs/PROJECT_CONTEXT.md) for the verified implementation status.
MUST is mandatory; SHOULD permits a reasoned, recorded exception; MUST NOT is forbidden.

## Load only necessary context

| Task | Load |
|---|---|
| Every task | This file and applicable instructions in the affected directory |
| Product or feature work | [PROJECT_CONTEXT](docs/PROJECT_CONTEXT.md), relevant source and tests |
| New feature, structural change, state management, DI, networking, navigation | Relevant sections of [ARCHITECTURE](docs/ARCHITECTURE.md) |
| Dependency change or reversal of a technical choice | Relevant entries in [DECISIONS](docs/DECISIONS.md), then affected architecture sections |
| Small bug fix | Relevant implementation, callers, and tests; add docs only when needed |
| UI-only change | Relevant screen contract, components, and local design system |

Do not load every document, historical conversation, archive, or unrelated feature.
These files are sufficient to implement the target; the original archive is not a runtime
or development dependency. Read targeted sections with search before expanding context.

## Before editing

- MUST inspect existing code, build files, version catalog, applicable instructions,
  and nearby tests before inventing a helper, abstraction, dependency, or package.
- MUST identify the existing execution path and extend it when it meets the need.
  Absence of implementation is a bootstrap task, not permission to claim a working app.
- MUST distinguish observed implementation, required target, and unresolved decisions.
  Existing established technology choices take precedence over unimplemented defaults;
  record conflicts and a migration decision before changing them. Explicit prohibitions
  below still apply to new code; do not expand legacy violations or silently rewrite them.
- SHOULD choose the smallest coherent change and state material assumptions. Do not
  redesign unrelated features or scaffold unused extension points.

## MUST

- Organize code by feature, with presentation, domain, and data boundaries. Start with
  one Gradle `:app` module; create only packages needed by actual code.
- Use plain AndroidX `ViewModel` and ViewModel-based MVI/UDF. Non-trivial screens expose
  `onEvent(UiEvent)` and a screen-local `ScreenState / UiState / UiEvent / UiAction` contract.
- Expose `StateFlow<ScreenState>` for immutable, repeatable screen rendering and a
  separate `Flow<UiAction>` for transient effects. Keep mutable producers private.
- Keep Compose content state-driven: render state and emit events. A route owns
  lifecycle collection and UI action handling; the ViewModel owns screen behavior.
- Put repository interfaces and application models in domain; data implementations own
  APIs, DTOs, and mapping. Presentation/domain must not depend on data implementations.
- Use Hilt constructor injection by default. Use Hilt bindings/providers where construction
  requires them. Keep dependency lifetimes explicit and testable.
- Preserve coroutine cancellation and define ownership, retry/concurrency policy, and
  cleanup. Do not report cancellation as a user-facing error.
- Define the action stream's collector, buffering, lifecycle, and loss semantics. Never
  promise exactly-once delivery or rely on transient effects to persist business outcomes.
- Add meaningful behavioral tests for changed logic; cover failure and cancellation paths
  where relevant. Follow the validation policy below and report actual results.

## SHOULD

- Call repositories directly for simple operations. Add a UseCase only for meaningful
  business logic, orchestration, validation, aggregation, or reuse.
- Use concrete section-specific state for complex screens. Keep one screen ViewModel
  by default; use a workflow-scoped shared ViewModel only for genuine cross-step state.
- Compose validators/rules and small collaborators. Extract shared code only after a
  concrete reuse or ownership need appears; avoid interfaces for every concrete class.
- Keep reusable visual primitives in `core/designsystem`; feature visuals stay local.
- Use the target stack in ARCHITECTURE when bootstrapping. Pin compatible versions in
  `gradle/libs.versions.toml` after validation; do not copy versions from prose or guess them.

## MUST NOT

- Introduce or copy `MVIViewModelV2`, `BaseViewModel`, `BaseRepository`, `BaseUseCase`,
  `ComposeFragment`, `OZBaseFragmentV2`, `BaseScreen`, `BaseToolbarScreen`, or
  `BaseMVIViewModelV2Test` framework abstractions.
- Introduce bank/company-specific `OZResult`, proprietary navigation helpers,
  inheritance-based validation frameworks, a generic `UiState<T>`, or a generic MVI framework.
  Standard AndroidX superclass use and interface implementation remain valid.
- Put `UiAction`, consumable/event wrappers, pending navigation commands, or one-shot
  effect queues inside persistent `ScreenState` or saved state.
- Require GraphQL, a UseCase per repository method, a separate design-system library,
  cross-platform dependencies, or additional Gradle modules by default.
- Expose DTOs/transport responses above data, access APIs from Compose/ViewModels,
  inject one ViewModel into another, expose mutable state, or use `GlobalScope`.
- Copy proprietary source, credentials, branding, or business rules into the starter.

## Validation

First inspect the wrapper, settings, build scripts, and available tasks. Current commands
for the `:app` debug variant (verified results are maintained in PROJECT_CONTEXT):

```sh
./gradlew :app:tasks --all
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug :app:lintDebug
```

- For application changes, run relevant unit tests, compilation/build, and lint; use
  existing flavor/task names and configured formatting/static checks when different.
- For UI/navigation changes, also exercise affected behavior on a device/emulator and
  run relevant instrumentation tests (`./gradlew :app:connectedDebugAndroidTest` when
  configured). Verify lifecycle, back handling, restoration, and action delivery as applicable.
- For documentation-only changes, check local links, paths, examples, consistency,
  prohibited-pattern wording, and target-versus-current status. No app build is required.
- If tooling, SDK, tests, or the application are absent, state what could not run and why.
  Never fabricate passing results or create an app merely to validate documentation.
- Review the final change scope. Summarize changed behavior, checks run, and unresolved
  limitations. Do not repeatedly run passing checks without a new reason.

## Keep documentation authoritative

- Update PROJECT_CONTEXT when implementation status, product scope, configuration,
  setup requirements, or verified commands change. Replace placeholders with evidence.
- Update ARCHITECTURE when responsibilities, boundaries, or canonical patterns change.
- Add or supersede a decision in DECISIONS when changing a recorded choice; include
  reason, consequences, and a concrete revisit trigger. Preserve decision history.
- Update this contract only for operating rules; README only for entry points and workflow.
- Keep documentation English, concise, and linked. Maintain each detail in one owning
  document; reference it elsewhere rather than copying explanations or examples.
