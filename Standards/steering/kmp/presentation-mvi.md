# Presentation: State / Intent / Effect

## Contract

One package per screen, one `Contract.kt` per package holding all three
types — see [architecture.md](architecture.md#module-layout-per-journeyfeature)
for the package layout. Don't split `State`/`Intent`/`Effect` into three
files; don't put two screens' contracts in the same file.

```kotlin
// commonMain — presentation/list/OrdersListContract.kt
data class OrdersListState(
    val orders: AsyncState<ImmutableList<Order>> = AsyncState.Idle,
)

sealed interface OrdersListIntent {
    data object Refresh : OrdersListIntent
    data class SelectOrder(val id: String) : OrdersListIntent
}

sealed interface OrdersListEffect {
    data class NavigateToDetail(val id: String) : OrdersListEffect  // nav effect
    data class ShowSnackbar(val message: String) : OrdersListEffect // UI effect
}
```

`AsyncState<T>` is a standard `commonMain` sealed class this steering set
assumes exists (`Idle` / `Loading` / `Success(data)` / `Error(throwable)`,
~30 lines, no platform deps) — define it once in your `:core` or `:utils`
module if `Kolt/libs` isn't in the workspace. **If it is,**
`utils/src/commonMain/.../state/AsyncState.kt` is this exact shape and is a
vetted "reuse as-is" per [kolt-libs.md](kolt-libs.md) — use it, don't
redefine it.

- `State`: one data class, single source of truth, `@Immutable`.
- `Intent`: every user action, sealed — no bare lambdas calling ViewModel methods directly from UI.
- `Effect`: one-shot only (navigation, snackbar, toast, scroll-to). Never put
  anything here that the UI needs to re-derive after a config change/process
  death — that belongs in `State`.
- **Any `List`/`Map`/`Set` in `State` is `ImmutableList`/`ImmutableMap`/`ImmutableSet`
  (`kotlinx.collections.immutable`), never the plain `kotlin.collections`
  interfaces.** The Compose compiler can't prove a plain `List` won't be
  mutated elsewhere, so it treats it — and the whole `State` class holding
  it — as unstable, and recomposes more than it needs to. `ImmutableList`
  is recognized as stable. `domain`/`data` keep returning plain `List<Order>`
  (that boundary has no Compose involved) — the `ViewModel` converts
  (`.toImmutableList()`) only when it writes the result into `State`.

## No logic in composables — dumb, passive, state-hoisted

A composable is a dumb renderer of the `State` it's handed and a source of
`Intent`s it doesn't interpret — nothing else. All state is hoisted: a
composable owns no business value of its own (`remember { mutableStateOf(...) }`
holding anything beyond transient UI-only state like a scroll offset or a
text-field's uncommitted draft is a smell — the committed value lives in
`State`). It may only branch on `State` shape it was already handed — `when
(state.orders)`, `if (state.isLoading)`, `state.items.isEmpty()`. It must
never derive a *new* value by comparing, computing, or transforming raw data
(`if (order.total > 100)`, `items.filter { it.active }`, date/string
formatting, `remember { ... }` that recomputes a business value). That
derivation happens once in the `ViewModel`, gets written into `State` as an
already-resolved field, and the composable just reads it and emits `Intent`s
back up — it never decides, it only displays and reports.

```kotlin
// wrong — comparison lives in the composable
@Composable
fun OrderRow(order: Order) {
    if (order.total > 100) HighValueBadge()
}

// right — ViewModel resolves it into State, composable only reads
data class OrdersListState(val orders: ImmutableList<OrderUi> = persistentListOf())
data class OrderUi(val id: String, val total: String, val isHighValue: Boolean)

@Composable
fun OrderRow(order: OrderUi) {
    if (order.isHighValue) HighValueBadge()
}
```

Formatting (currency, dates, pluralization) is the same rule: format in the
`ViewModel` into a `String` field on `State`, don't hand the composable a
`Double`/`Instant` and a formatter call. Exception: purely presentational
lookups with no business meaning — `if (isSelected) SelectedColor else
DefaultColor`, `Modifier.alpha(if (visible) 1f else 0f)` — stay in the
composable; they're display, not decisions.

## ViewModel

Don't reach for Kolt's `compose-utils` ViewModel base classes
(`UiStateEventsViewModel`, `UiStateEventsAndroidViewModel`) — they're
Android-only (`AndroidViewModel`, live under `src/main/java`, no
`commonMain`), which breaks a real KMP presentation layer the moment you add
iOS. Use `androidx.lifecycle:lifecycle-viewmodel` directly — it's been a
proper KMP artifact (`commonMain`) since Lifecycle 2.8 — with a small
`commonMain` base:

```kotlin
// commonMain
abstract class MviViewModel<S : Any, I : Any, E : Any>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected fun setState(reducer: S.() -> S) = _state.update(reducer)
    protected fun sendEffect(effect: E) = viewModelScope.launch { _effect.send(effect) }

    abstract fun onIntent(intent: I)
}
```

Skipped: Kolt's `ViewModelDelegate` (`compose-utils/.../base/ViewModelDelegate.kt`)
looks like the right shape (launchSafe/launchIO/launchDefault helpers) but
it's dead code — nothing in the repo actually uses it, and the class it was
meant to de-duplicate (`UiStateEventsAndroidViewModel`) reimplements the same
channel/flow logic by hand instead. Don't copy that pattern as-is.

### Reusable ViewModel logic: composition, not subclassing

Once the same non-trivial logic shows up in 2+ ViewModels (pagination,
debounced search, field validation — see [architecture.md](architecture.md#reuse-over-duplication)
for the threshold), pull it into a small delegate class the ViewModel holds
and forwards to. Don't grow a `PaginatedMviViewModel` subclass — Kotlin has
no multiple inheritance, and a subclass-per-concern chain collapses into a
God base class the moment a ViewModel needs two of these at once.

```kotlin
// commonMain
class PaginationDelegate<T>(
    private val scope: CoroutineScope,
    private val pageSize: Int = 20,
    private val loadPage: suspend (page: Int) -> List<T>,
) {
    private val _items = MutableStateFlow<AsyncState<List<T>>>(AsyncState.Idle)
    val items = _items.asStateFlow()

    fun loadNext() { /* tracks current page, appends, sets Loading/Error */ }
}

class OrdersListViewModel(private val repo: OrdersRepository) : MviViewModel<OrdersListState, OrdersListIntent, OrdersListEffect>(OrdersListState()) {
    private val pagination = PaginationDelegate(viewModelScope) { page -> repo.getOrders(page) }
    // forward OrdersListIntent.LoadMore -> pagination.loadNext(), map pagination.items into setState { }
}
```

## Wiring: the Route composable owns the ViewModel and the effect Flow

Navigation moved to Navigation 3 — see [navigation.md](navigation.md) for why
and for the `NavDisplay`/`entryProvider` setup. The composable named in that
`entryProvider` (call it `OrdersListRoute`, matching Now in Android's naming)
is the equivalent of what used to be the NavGraph's `composable<Route> {}`
block: it's where the ViewModel is instantiated and the only place that
mutates the back stack.

```kotlin
// presentation/list/OrdersListRoute.kt — instantiated from entryProvider in the journey's NavDisplay
@Composable
fun OrdersListRoute(backStack: NavBackStack) {
    val viewModel: OrdersListViewModel = viewModel { OrdersListViewModel(get()) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.effect.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                is OrdersListEffect.NavigateToDetail -> backStack.add(OrderDetail(effect.id))
                is OrdersListEffect.ShowSnackbar -> { /* forward below, not here */ }
            }
        }
    }

    OrdersListScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOrderClick = { id -> viewModel.onIntent(OrdersListIntent.SelectOrder(id)) },
    )
}
```

```kotlin
// presentation/list/OrdersListScreen.kt — stateless root composable, zero navigation/ViewModel awareness
@Composable
fun OrdersListScreen(
    state: OrdersListState,
    onIntent: (OrdersListIntent) -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) { /* ... */ }
```

**Why `OrdersListRoute`, not `OrdersListScreen`, collects the effect Flow:**
only the route-level composable has the back stack. If the stateless screen
collected the raw `Flow<Effect>` itself, it would need the back stack (or a
callback bag) to resolve nav effects — at which point it's no longer a dumb,
`@Preview`-able composable. Rule: **the Route composable is the single effect
collector.** Nav effects resolve to `backStack.add/removeLast`. Non-nav UI
effects (snackbar text) get resolved to a plain value/lambda before reaching
the screen — e.g. hold a local `snackbarHostState` in `OrdersListRoute` and
call `snackbarHostState.showSnackbar(...)` from the same `LaunchedEffect`,
passing only the already-instantiated `SnackbarHostState` down if the screen
needs to host it.

## Known deviation from current official guidance: Channel-based effects

Current Android architecture docs (`developer.android.com/topic/architecture/ui-layer/events`,
updated 2026) explicitly advise **against** `Channel`/`SharedFlow` for
ViewModel → UI events: "When the producer (the ViewModel) outlives the
consumer (Compose UI), these solutions don't guarantee delivery." Their
recommended alternative models everything — including navigation triggers —
as a field in `State` that the UI clears after acting on it (e.g.
`isUserLoggedIn: Boolean`, consumed via `snapshotFlow { state }` in the UI,
not pushed by the ViewModel).

This steering doc keeps the `Channel`-based `Effect` from the original spec
anyway — it's the standard shape across the KMP-MVI ecosystem (Orbit, Circuit,
etc.), and the loss-of-delivery risk Google warns about is specifically
config-change/process-death races, which `flowWithLifecycle(lifecycle)` in
the collector above (not a bare `LaunchedEffect(Unit)` + `collect`) closes
for the vast majority of cases.

**If strict compliance with the official pattern matters more than ecosystem
convention here, say so** — the fix is mechanical: fold `Effect` into
`State` as nullable/consumed fields and drop the `Channel` entirely. Not
doing that by default because it reverses an explicit part of the original
spec, not because it's wrong.

## Accessibility

Non-negotiable per this project's own baseline rules, not optional polish:

- Every `Image`/`Icon` that conveys information has a `contentDescription`;
  every purely decorative one has `contentDescription = null` so screen
  readers skip it — never leave it unset (that reads out the filename/no-op
  garbage on some platforms).
- Interactive elements (buttons, icons-as-buttons, list rows) meet the
  minimum touch target (48.dp) even if the visible icon/text is smaller —
  use `Modifier.minimumInteractiveComponentSize()` or padding, don't shrink
  the tap target to match a small icon.
- Don't hardcode `.sp` values that disable system font-scaling
  (`fontScale`), and don't clip/truncate text in a way that breaks at large
  font scales — verify the screen at 130%+ system font size, not just
  default.
- Semantic grouping: related content that should be announced together
  (e.g. a list row's title + subtitle) uses `Modifier.semantics(mergeDescendants = true)`
  or a single merged node, not N separate unlabeled nodes a screen reader
  reads out one at a time.

## Checklist for any new screen

- [ ] `State`/`Intent`/`Effect` are three separate sealed/data types in one `Contract.kt`, not one blob and not three files.
- [ ] Any `List`/`Map`/`Set` in `State` is `ImmutableList`/`ImmutableMap`/`ImmutableSet`, not the plain `kotlin.collections` type.
- [ ] No composable does value comparison/computation/formatting on raw data, or holds committed business state in `remember` — every non-presentational branch reads a field the ViewModel already resolved into `State`, and all committed state is hoisted.
- [ ] Images/icons have `contentDescription` (or explicit `null` for decorative), touch targets are ≥48.dp, text isn't clipped at large font scales.
- [ ] The screen has its own package under `presentation/`; nothing in it is imported by another screen's package.
- [ ] ViewModel extends the `commonMain` `MviViewModel`, lives in that screen's package.
- [ ] ViewModel is constructed only inside the Route composable named in `entryProvider`.
- [ ] Effect `Flow` is collected exactly once, in that Route composable, with `flowWithLifecycle`.
- [ ] Root composable's signature has no `ViewModel`, back stack, or `Flow` — only `state`, `onIntent`, and resolved lambdas/values.
- [ ] Root composable has a `@Preview` with fake `State`, using
      `androidx.compose.ui.tooling.preview.Preview` — unified into
      `commonMain` since Compose Multiplatform 1.10 (Jan 2026). Don't reach
      for the deprecated per-platform ones
      (`org.jetbrains.compose.ui.tooling.preview.Preview`,
      `androidx.compose.desktop.ui.tooling.preview.Preview`).
