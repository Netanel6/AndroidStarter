# Canonical Android Starter architecture

This is the canonical implementation contract. [PROJECT_CONTEXT](PROJECT_CONTEXT.md) owns current
status; [AGENTS](../AGENTS.md) owns operating rules and validation commands;
[DECISIONS](DECISIONS.md) owns rationale and revisit conditions. Examples are illustrative
Kotlin with imports omitted; implemented sample source is under `app/src/main/kotlin`.

## 1. Reference and deliberate adaptations

Inspected only `trading/crypto/build.gradle.kts` and selected `trading/crypto/src/`
production/tests in `trading.zip` on 2026-10-14, retrieved from the referenced chat's
attachment. No unrelated trading source or external shared implementations were needed.

Evidence below is relative to `trading/crypto/src/main/kotlin/com/onezerobank/features/crypto/`:

| Inspected reference | Pattern retained / adaptation |
|---|---|
| `orderhistory/ui/history/CryptoOrderHistoryModel.kt`, `CryptoOrderHistoryViewModel.kt`, `CryptoOrderHistoryScreen.kt` in the same directory | Screen contract and event-driven rendering; replace inherited ViewModel/Screen and state-held `Consumable<UiAction>` |
| `lobby/ui/CryptoLobbyModel.kt` | Independent section states; normalize the root contract to `ScreenState / UiState` |
| `assets/domain/GetCryptoAssetsDataUseCaseImpl.kt` | Meaningful fetch/stream orchestration; no mandatory forwarding UseCase |
| `trade/accountselection/data/CryptoTradeRepositoryImpl.kt` | Repository/data-source boundary and mapping; tighten it because some reference methods expose GraphQL-generated types |
| `trade/CryptoTradeFlowSharedViewModel.kt`, `trade/navigation/CryptoTradeFlowCoordinator.kt` | Scoped workflow ownership and a stateless coordinator; remove fragment/proprietary routing and concrete repository coupling |
| `trade/orderdata/validation/CryptoTradeOrderDataValidator.kt`, `CryptoTradeValidationRule.kt` in the same directory | Compose rules; replace `ConfiguredCryptoValidationRule` inheritance with constructor-held configuration |

Also inspected `CryptoAssetsListViewModelTest.kt` and `CryptoTradeFlowCoordinatorTest.kt`
in corresponding test packages. Retain behavioral coverage; exclude inherited test
frameworks, bank plugins, module layout, and GraphQL. AGENTS owns the forbidden list.

## 2. Target stack and dependency direction

Defaults: Kotlin; Jetpack Compose + Material 3; Hilt; Coroutines/Flow/StateFlow;
Retrofit + OkHttp + kotlinx.serialization; Navigation 3; one Gradle `:app` module;
`gradle/libs.versions.toml`; small `AppResult` / `AppError`; local `core/designsystem`.
Preserve established starter choices pending a documented change. Navigation 3 remains
revisable after integration validation. Select compatible versions/SDKs during bootstrap.

```text
Compose content --UiEvent--> ViewModel --optional UseCase--> Repository interface
       ^                        |                               ^
       | StateFlow<ScreenState> |                         implementation in data
       +------------------------+                               |
Route/host <--Flow<UiAction>-----+                         API / local source
   |
navigation / dialog / snackbar / external UI
```

Presentation depends on domain contracts; data implements them through Hilt bindings.
Domain excludes Compose, Android UI, Retrofit, and DTOs. Repositories return domain models;
presentation maps screen models. UseCases neither navigate nor call ViewModels.

## 3. Where new code goes

Paths below are within `app/src/main/kotlin/<base-package>/`; an established Java source
root may also contain Kotlin. Packages express boundaries before Gradle modules do.

```text
app/                         Application, Activity, app composition
  navigation/                destination keys, back stack, route registration
core/
  designsystem/              theme, tokens, reusable visual components
  network/                   OkHttp/Retrofit/Json configuration
  result/                    AppResult, AppError
  di/                        genuinely shared bindings/providers/qualifiers
feature/products/
  data/
    remote/                  ProductsApi, ProductDto
    mapper/                  DTO-to-domain mapping
    ProductsRepositoryImpl.kt
  domain/
    Product.kt
    ProductsRepository.kt
    usecase/                 only when meaningful logic exists
    validation/              feature rules and validators, when needed
  di/                        feature repository bindings
  presentation/list/
    ProductsModel.kt          ScreenState, UiState, UiEvent, UiAction
    ProductsViewModel.kt
    ProductsRoute.kt          lifecycle/DI/action integration
    ProductsScreen.kt         content receiving state and events
    states/                  substantial loading/content/empty/error UI
    components/              feature-local reusable UI
```

Give each screen a distinct package (for example, `presentation/details`) and
`<Screen>Model.kt`; use import aliases when integrating contracts. Mirror packages in
`src/test/kotlin`; Android UI/integration tests use `src/androidTest`. Create no empty
layers or generic `utils`. Promote shared business contracts only for real consumers.

## 4. Screen contract and ownership

| Type | Responsibility |
|---|---|
| `ScreenState` | Immutable snapshot: root UI state, user input, selection, refresh/submission status |
| `UiState` | Screen-specific renderable alternatives or section composition |
| `UiEvent` | User intent or explicit screen input entering `onEvent` |
| `UiAction` | Transient instruction handled once by the active route/host |

Here, persistent means repeatable state for the ViewModel lifetime, not automatic disk or
process persistence. Use `SavedStateHandle` for minimal restorable inputs/IDs; use an
actual repository/store for durable business data. Never save an action queue.

```kotlin
// feature/products/presentation/list/ProductsModel.kt
data class ScreenState(val uiState: UiState = UiState.Loading)

sealed interface UiState {
    data object Loading : UiState
    data object Empty : UiState
    data class Data(val products: List<Product>) : UiState
    data class Error(val error: AppError) : UiState
}

sealed interface UiEvent {
    data object Retry : UiEvent
    data class ProductClicked(val id: String) : UiEvent
}

sealed interface UiAction {
    data class NavigateToDetails(val id: String) : UiAction
}
```

Use `val`, immutable elements, and copied collections: read-only `List` is not inherently
immutable. Use `copy`/`update`; update lambdas may rerun and must be effect-free.
Use `@Immutable` only when its contract holds.

Not every screen needs all four alternatives. Complex screens can use
`UiState.Data(header: HeaderUiState, holdings: HoldingsUiState)` with concrete section
types. Section retries preserve healthy sections. Avoid contradictory booleans/null data,
global `UiState<T>`, and generic reducer/store frameworks.

```kotlin
// ProductsViewModel.kt; repository is main-safe and maps expected failures.
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    private val _actions = Channel<UiAction>(Channel.BUFFERED)
    val actions: Flow<UiAction> = _actions.receiveAsFlow()
    private var loadJob: Job? = null

    init { load() }

    fun onEvent(event: UiEvent) {
        when (event) {
            UiEvent.Retry -> load()
            is UiEvent.ProductClicked -> viewModelScope.launch {
                _actions.send(UiAction.NavigateToDetails(event.id))
            }
        }
    }

    private fun load() {
        loadJob?.cancel() // Latest load wins; not a submission policy.
        loadJob = viewModelScope.launch {
            _state.update { it.copy(uiState = UiState.Loading) }
            val result = repository.getProducts()
            currentCoroutineContext().ensureActive()
            val next = when (result) {
                is AppResult.Success -> if (result.value.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Data(result.value.toList())
                }
                is AppResult.Failure -> UiState.Error(result.error)
            }
            _state.update { it.copy(uiState = next) }
        }
    }

    override fun onCleared() {
        _actions.close()
        super.onCleared()
    }
}
```

Plain AndroidX ViewModels own events, screen state, concurrency, and screen work.
Routes/hosts execute UI effects. Focus, animation, and scroll state may stay in Compose;
business decisions and meaningful input belong to explicit screen/workflow state.

## 5. Actions, lifecycle, and Compose integration

Use `Flow<UiAction>` for navigation, showing/dismissing transient dialogs or sheets,
snackbars, closing a flow, and launching external UI. `ScreenState` contains neither
actions nor consumed flags. A dialog's editable data or an outstanding business decision
can be durable state; its one-time display instruction remains an action. The route may
hold local dialog visibility. If restoration matters, reconstruct the UI from the
underlying workflow data, not by replaying an old command.

| Transport | Semantics / suitable use |
|---|---|
| Buffered `Channel` + `receiveAsFlow()` | Default for one route consumer; queues during collector absence while the ViewModel lives; sends suspend when full |
| `SharedFlow(replay = 0)` | Opt-in broadcast to active subscribers; absent subscribers miss emissions, even with extra buffer capacity |

Channel fan-out is not broadcast: multiple collectors compete for actions. Neither
transport guarantees processing exactly once: cancellation can occur after receipt;
process death loses in-memory events. Buffered actions may also become stale while hidden.
Document whether delayed delivery is acceptable; validate current route/workflow identity
before navigation and prevent duplicate submissions/click navigation where required.
Do not use replay to make navigation survive restoration. Persist business outcomes
independently and make critical operations idempotent.
See Kotlin's [Channel-to-Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/receive-as-flow.html)
and [SharedFlow semantics](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-shared-flow/).

```kotlin
@Composable
fun ProductsRoute(
    viewModel: ProductsViewModel, // Supplied using the correct Hilt/navigation owner.
    onAction: (UiAction) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnAction by rememberUpdatedState(onAction)
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.actions.collect { currentOnAction(it) }
        }
    }
    ProductsScreen(state = state, onEvent = viewModel::onEvent)
}
```

The host exhaustively handles actions. `ProductsScreen` renders state and emits events;
it neither collects effects nor obtains dependencies. Use one action collector per owner.
RESUMED is this example's interaction policy; validate STARTED for other effects and
destination activity in multi-pane hosts. Launch snackbar display in a route-owned scope
to avoid blocking navigation; define its replacement/queue policy.
Use [lifecycle-aware state collection](https://developer.android.com/develop/ui/compose/state#other-supported-types-of-state).

This separate-action-stream policy is intentional. Android's
[UI-event guidance](https://developer.android.com/topic/architecture/ui-layer/events)
emphasizes reducing important outcomes to state because streams cannot guarantee delivery.
We preserve durable outcomes as data and reserve actions for transient UI instructions.

## 6. Repository, result, networking, and DI

```kotlin
// core/result; small application contract, not a framework.
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
enum class AppError { Network, Unauthorized, Server, InvalidData }

// feature/products/domain
data class Product(val id: String, val name: String)
interface ProductsRepository {
    suspend fun getProducts(): AppResult<List<Product>>
}
```

Add error cases only for actionable distinctions. Keep exceptions/raw responses in data;
localize errors in presentation. Do not disguise programming defects as network failures.
Catch specific transport/decoding failures; rethrow `CancellationException` before broad
catches. Unqualified `runCatching` around suspending work must not swallow cancellation.

`ProductsRepositoryImpl` receives `ProductsApi`, maps `ProductDto` to `Product`, and returns
`AppResult`. DTO serialization annotations stay in `data/remote`; mapping stays in
`data/mapper`. No Retrofit `Response`, generated schema type, or DTO escapes to domain/UI.
The Retrofit API can itself be the remote data source; add a wrapper only for real
behavior. Repositories coordinate sources and caching policy when present, not screen state.

Configure OkHttp, Retrofit, and `Json` through shared providers, using Retrofit's
kotlinx.serialization converter compatible with the selected dependency versions.
Define timeouts, base URL/environment, HTTP/empty-body/error mapping, and parsing policy
explicitly. Choose unknown-key/enum behavior from the API contract and test malformed
responses. Keep authentication/logging configuration product-specific; never log secrets
or ship credentials. GraphQL and realtime subscriptions are not requirements.

Use constructor injection for ViewModels and collaborators; bind repository interfaces
with `@Binds`, provide third-party instances with `@Provides`, and use qualifiers for
multiple endpoints/dispatchers. Choose the narrowest justified scope; do not make all
dependencies singleton. Tests should usually construct classes with fakes directly.
Follow [Hilt's construction and binding model](https://developer.android.com/training/dependency-injection/hilt-android).

## 7. Optional UseCases, validation, and async ownership

Use `ViewModel -> Repository` for simple fetching. Extract UseCases for meaningful
calculation, aggregation, validation, orchestration, or reuse. Prefer concrete injected
classes with `invoke`; interfaces are optional. UI formatting/parsing stays in presentation.

Compose validation as a feature-specific rule interface plus a validator holding a list
of rules. Each rule receives immutable input and holds its configuration in its constructor.
Return typed issues; define applicability, ordering/priority, and first-error versus all-error
behavior explicitly. Test each rule and the composed result. No configured-rule superclass
or generic validation engine is required.

- Launch screen work in `viewModelScope`. UseCases/repositories normally suspend or
  expose Flow without launching detached work. UI-only coroutines follow route lifetime.
- Make repository calls main-safe. Suspend networking is not blocking work; move actual
  blocking IO/CPU work to appropriate injected dispatchers. Never block Main.
- Define per-operation concurrency: cancel previous reads when appropriate; serialize or
  reject duplicate submissions. Canceling a client request does not undo a server mutation.
- Cold flows follow collectors. For shared upstreams, choose and test the sharing lifetime;
  lifecycle-aware UI collection alone does not stop independent ViewModel jobs.
- Release subscriptions/resources on cancellation (`awaitClose` for callback sources,
  `finally` where appropriate). Retry only with a bounded, justified policy.
- Work that must survive screen exit needs an explicitly longer-lived owner; durable
  background work requires a suitable persistence/scheduler decision, not `GlobalScope`.

## 8. Navigation, workflows, and design system

The app host owns destination keys, back-stack mutation, saved navigation state, and
route registration. Feature ViewModels emit semantic actions with minimal IDs; they
hold no navigator, Activity, Fragment, or enterprise navigation helper.

[Navigation 3](https://developer.android.com/guide/navigation/navigation-3) is the current
preferred implementation. Validate Hilt/ViewModel owners, entry removal cleanup, back and
predictive-back behavior, deep links if needed, configuration changes, and process-state
restoration before committing to its integration. Do not assume an old navigation-Hilt
helper works unchanged. If concrete friction justifies another choice, update DEC-012 and
PROJECT_CONTEXT while preserving the screen contract.

Use a shared ViewModel only for a defined multi-step workflow. Scope it to a workflow
owner that survives step changes and clears on completion/cancel. Individual steps may
retain separate ViewModels. Pass immutable step input and emit step events/results through
the route; never inject or retain sibling/parent ViewModels. A stateless coordinator can
calculate the next/previous step; mutable workflow state stays with its explicit owner.
Define back/reset/re-entry/restoration behavior and a single owner for workflow actions.
Do not use an app-wide ViewModel or singleton repository as a screen-state bus.

Use local `core/designsystem` for Material 3 theme, colors, typography, spacing, and
reusable UI primitives. Use ordinary composable functions and slots for composition;
feature components own domain-specific UI. Avoid a universal screen wrapper/framework.
Strings use Android resources; include accessibility semantics and necessary previews.
Extract a library only after real independent consumers justify publishing/versioning.

## 9. Testing philosophy

Test observable contracts with `runTest`, controlled dispatchers, and deterministic fakes,
not sleeps/private methods. StateFlow may conflate intermediate emissions: assert meaningful
snapshots. Start action collectors deliberately and verify cleanup.

| Unit | Required focus when behavior changes |
|---|---|
| ViewModel | Event-to-state/action behavior; loading/data/empty/error, retry, stale-result prevention, duplicate input and cancellation |
| UseCase | Business decisions, orchestration, partial failures, reuse boundaries |
| Validator/rule | Boundary values, applicability, ordering, combined issues |
| Parser/mapper | Missing/malformed values, enum evolution, formatting, precision where relevant |
| Coordinator | Next/back/skip/exit transitions and invalid workflow inputs |
| Repository | DTO conversion, HTTP/decoding errors, cancellation, source/cache policy if implemented |

Use controlled HTTP fixtures (for example, MockWebServer) for network contracts when
needed. Add focused Compose/navigation tests for rendering, event callbacks, lifecycle
stop/resume, action collection, and restoration. Reuse test fixtures and dispatcher rules
through composition; do not create inherited MVI test bases. Strong tests protect behavior;
they do not justify adding layers or tests that merely mirror implementation.
