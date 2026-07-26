# Coding Standards — Android / KMP

**Authority:** Binding for any project that adopts it. Overrides tool defaults. Deviation = a one-line note in the PR/commit explaining why.
**Stack:** Kotlin, Compose (Multiplatform or Android-only), MVI, Clean Architecture, SOLID. Applies to single-module Android apps and multi-module KMP alike.
**Audience:** Human + AI agents (Claude, Gemini/Antigravity). This is the single source of truth; the per-project `CLAUDE.md` / `AGENTS.md` only point here and add project facts.

> **Agent read order:** this file → the project's `CLAUDE.md`/`AGENTS.md` for project specifics → write code. Don't ask for rules already here.

---

## 0. The Loop (memorize — never violate)

```
Composable --Intent--> ViewModel.onIntent(i)
                          ├─ reduce(state,i) -> newState   PURE. sync. total. no I/O.
                          ├─ _state.update { newState }     atomic, single call
                          └─ effect path: _effects.send(e)  Channel, single-pass
Composable <--state--  StateFlow<State>       (collectAsStateWithLifecycle)
Composable <--effect-- Flow<Effect>           (Channel.receiveAsFlow, consume once)
```

State flows **down**, Intent flows **up**, `Effect` is a **one-shot** side channel (navigate/toast/clipboard/haptic) delivered exactly once.

> **Naming:** the one-shot VM→UI channel is called `Effect` here. Some teams call it `UiEvent`/`SideEffect` — same contract (a `Channel`/`channelFlow` consumed once). Pick one name per project and keep it; examples below use `Effect`.

---

## 1. Universal rules (all layers)

| # | Rule |
|---|---|
| U1 | Every async/fallible API returns a sealed result wrapper (`Result<T>` / `Outcome<T>` / `Either`) or `Flow<Result<T>>`. Never throw across a layer boundary. |
| U2 | Constructor injection only (Hilt/Koin/kotlin-inject). No service locators, no stateful `object` singletons, no `GlobalScope`. |
| U3 | Depend on interfaces, not impls. Impls are `internal`/`private`. Each module/package exposes the minimum public surface. |
| U4 | Immutability by default: `val`, `data class`, read-only collections (`List`, not `MutableList`) in public signatures. |
| U5 | No magic values. Strings/numbers/dimensions → named constants, string resources, or design tokens. |
| U6 | One public type per file; file name = type name. |
| U7 | No TODO/stub/catch-all `else -> {}` that swallows a case in production paths. Handle every branch. |
| U8 | No secrets/PII in logs. Encrypt sensitive data before persistence or transmission. |
| U9 | Dispatchers via an injected provider (`DispatcherProvider`): `io`=I/O, `default`=CPU, `main`=UI/state. Never hardcode `Dispatchers.*` in business code. |

---

## 2. SOLID — concrete obligations

| Principle | Obligation |
|---|---|
| **S**RP | One reason to change per class. ViewModel orchestrates; UseCase = one operation; Repository = one aggregate; Mapper = one direction pair. No god "manager" classes. |
| **O**CP | Extend via new UseCase / new impl / new strategy — not by editing switch-ladders. Sealed types for closed sets (Intent/Effect/Error/State), interfaces for open sets. |
| **L**SP | Every fake/test double must honor the real contract (same result semantics, same ordering). |
| **I**SP | Small role interfaces. A consumer depends only on the methods it uses, not a fat service. |
| **D**IP | High-level code (domain, presentation) depends on abstractions; impls injected at the composition root (Application/DI graph). Domain knows zero platform/SDK types. |

---

## 3. Layer specs

> Multi-module KMP: each layer is a Gradle module (`:domain`, `:data`, `:feature:*`, `:core:*`). Android-only / single-module: each layer is a package (`domain/`, `data/`, `ui/`). The rules are identical — only the boundary mechanism differs.

### 3.1 Domain — pure Kotlin, zero platform deps

- **Entities**: immutable `data class`; value objects over primitives (`Money`, `Email`) where it prevents bugs. No framework annotations, no Android/SDK types, no Compose, no DB types.
- **Repository interfaces** live here; impls live in data. Return the result wrapper / `Flow`.
- **Use cases**: one `operator fun invoke`. Pure business rules — orchestrate repos + domain logic. No Android, no DB, no UI.
- Business-derived values that are not presentation-specific belong on entities/use cases, not the ViewModel.

```kotlin
class GetCartTotalUseCase(private val repo: CartRepository) {
    suspend operator fun invoke(id: CartId): Result<Money> = repo.total(id)
}
```

### 3.2 Data — single source of truth is local; impls `internal`

- **Reads**: from the local store (Room / SQLDelight / DataStore), exposed as `Flow`. UI/use cases never read remote directly.
- **Writes (offline-capable apps)**: local first → enqueue remote sync. Otherwise: remote call wrapped in result, then cache locally.
- **Remote is isolated**: backend/SDK imports (Retrofit/Firebase/Ktor) live only in the data/remote layer. Swapping the backend = new data-source impl + DI rebind, nothing else.
- **Mappers** are the DTO↔entity (and encrypt/decrypt) boundary; one mapper per entity, one direction pair.
- Wrap every I/O call into the result wrapper; never let exceptions escape the data layer.

### 3.3 Presentation — ViewModel

Extends the MVI ViewModel base class `MviViewModel<State, Intent, Effect>` (KMP-ready — `androidx.lifecycle` ≥ 2.8.0 targets `commonMain`). `dispatch(intent)` is the single entry point. `onIntent` is the handler — it may be `suspend` and may call `updateState` and `sendEffect`.

```kotlin
class CartViewModel(
    private val getTotal: GetCartTotalUseCase,
    private val dispatchers: DispatcherProvider,
) : MviViewModel<CartState, CartIntent, CartEffect>(CartState()) {

    override suspend fun onIntent(intent: CartIntent) = when (intent) {
        CartIntent.Load -> {
            updateState { copy(isLoading = true, error = null) }
            val result = withContext(dispatchers.io) { getTotal(state.value.cartId) }
            result.fold(
                onSuccess = { total -> updateState { copy(isLoading = false, total = total) } },
                onFailure = { err   -> updateState { copy(isLoading = false, error = err) }
                                       sendEffect(CartEffect.ShowError(err)) },
            )
        }
        is CartIntent.QueryChanged -> updateState { copy(query = intent.q) }
        is CartIntent.ItemTapped   -> sendEffect(CartEffect.NavigateToItem(intent.id))
    }
}
```

Rules:
- `updateState { }` is the **only** way to change state — receiver lambda `State.() -> State`, atomic via `StateFlow.update`.
- `sendEffect(e)` posts to a `Channel.BUFFERED` — consumed **exactly once** by the UI.
- All coroutines in `viewModelScope` (provided by the base). No `GlobalScope`.
- `onIntent` is **total** over the sealed Intent — every branch handled; no silent `else -> Unit` swallowing a case.
- For screens with no user-driven intents, use `MviStateViewModel<State, Effect>` instead.

### 3.4 Presentation — State / Intent / Effect contracts

```kotlin
// State — single immutable source of truth for the screen.
data class CartState(
    val isLoading: Boolean = false,
    val items: List<CartItem> = emptyList(),
    val query: String = "",
    val total: Money? = null,
    val error: AppError? = null,
) {
    // Derived/display values are computed props — NOT stored, NOT recomputed in the Composable.
    val visibleItems: List<CartItem> get() = items.filter { it.matches(query) }
    val isEmpty: Boolean get() = !isLoading && visibleItems.isEmpty()
}

// Intent — every user action AND every async result. Sealed (uniform entry into reduce).
sealed interface CartIntent {
    data object Load : CartIntent
    data class Loaded(val total: Money) : CartIntent
    data class LoadFailed(val error: AppError) : CartIntent
    data class QueryChanged(val q: String) : CartIntent
    data class ItemTapped(val id: ItemId) : CartIntent
}

// Effect — one-shot only (nav/toast/clipboard/haptic). Sealed. Channel only, never StateFlow/SharedFlow.
sealed interface CartEffect {
    data class NavigateToItem(val id: ItemId) : CartEffect
    data class ShowMessage(val text: UiText) : CartEffect
}
```

- **State** holds *what to show*; every derived display value is a computed `get()` prop, never a field that can drift.
- **Intent** is a closed sealed set; user actions and async outcomes are both Intents.
- **Effect** is only for things that must happen exactly once with no place in State.
- Use a `UiText`-style wrapper (resource id or literal) for user-facing strings so State stays testable and platform-agnostic.

### 3.5 UI — Composable: stateless, dumb

```kotlin
// Route: the ONLY place that touches the ViewModel.
@Composable
fun CartRoute(vm: CartViewModel = viewModel(), nav: Navigator) {
    val state by vm.state.collectAsStateWithLifecycle()

    // collectEffects is a lifecycle-aware helper.
    vm.collectEffects { effect ->
        when (effect) {
            is CartEffect.NavigateToItem -> nav.toItem(effect.id)
            is CartEffect.ShowError      -> snackbarHost.showSnackbar(effect.message)
        }
    }

    CartScreen(state, onIntent = vm::dispatch)
}

// Content: pure UI. State in, Intent out. No VM, no business logic, no I/O.
@Composable
fun CartScreen(state: CartState, onIntent: (CartIntent) -> Unit) {
    LaunchedEffect(Unit) { onIntent(CartIntent.Load) }
    when {
        state.isLoading     -> LoadingView()
        state.error != null -> ErrorView(state.error, onRetry = { onIntent(CartIntent.Load) })
        else                -> CartList(state.visibleItems, onIntent)
    }
}
```

Compose rules:
- **No business logic in composables** — no arithmetic, formatting, filtering, date math. Read finished values off `state` (its computed props).
- **No ViewModel reach-in below the Route.** Content composables receive `state` + `(Intent) -> Unit` only.
- **Hoist state to the max.** No `remember { mutableStateOf(...) }` holding business/UI state — that lives in `State`. Allowed local `remember`: pure-ephemeral, zero business meaning (scroll/list state, animation, focus requester, `derivedStateOf` over already-hoisted state).
- **Stateless + previewable.** Every content composable renders from a hand-built `State`; add `@Preview` (light + dark). No preview needs a VM.
- Colors/type/spacing via theme tokens. No hardcoded hex / magic `dp`.
- Effects consumed **once** in `HandleEffects` (lifecycle-aware collect). Never collect the effect flow twice.

### 3.6 DI

- Constructor injection, composition root at `Application`/DI graph. Bind interface → impl; bind concrete backend impl only at the root.
- No field/setter injection in business code, no runtime reflection lookups. Scope deliberately (singleton vs per-screen).

---

## 4. Naming & style

- Files: `{Name}Screen.kt`, `{Name}ViewModel.kt`, `{Name}State.kt`, `{Name}Intent.kt`, `{Name}Effect.kt`, `{Name}RepositoryImpl.kt`, `{Verb}{Noun}UseCase.kt`, `{Name}Mapper.kt`.
- `PascalCase` types, `camelCase` members, `UPPER_SNAKE` consts. No Hungarian / `m`-prefix.
- Functions do one thing; prefer pure functions + small composables.
- Booleans read as predicates (`isLoading`, `hasError`, `canSubmit`).
- Lint clean (ktlint/detekt/Android lint) — no suppression without an inline justification comment.

---

## 5. Testing

- **Unit-test every reducer** (pure → trivial: `(state, intent) -> state`), every use case, every mapper/validator. Aim 100% branch coverage on reducers.
- Use **fakes over mocks** for repositories/data sources; fakes must honor the real contract (LSP).
- Test names: `subject condition expected` (backtick-quoted). AAA structure (Arrange/Act/Assert).
- Flow tests via Turbine; no flaky/time-dependent tests.

---

## 6. Pre-commit self-check (fast gate)

```
[ ] onIntent() total over sealed Intent: every branch handled, no silent else -> Unit
[ ] updateState { } is the only state mutation; no mutable fields / side-channel writes
[ ] No business logic / formatting / filtering inside any @Composable
[ ] State hoisted: no business-state remember{}; derived values are computed get() props
[ ] dispatch(intent) up, collectEffects { } down; effects consumed exactly once
[ ] All async returns the result wrapper; errors surface to State; no silent catch
[ ] Reads from local store; remote/SDK isolated to data layer; backend swappable
[ ] Sensitive data encrypted before persist; nothing secret/PII logged
[ ] Constructor DI; interfaces public, impls internal; no GlobalScope
[ ] Names/files per convention; tokens/resources (no magic values); lint clean
[ ] Unit test added for onIntent handler / use case
```

---

*This is the canonical rule index. Keep it open while coding. Project-specific facts (module graph, chosen libs, result-wrapper name) live in the project's `CLAUDE.md` / `AGENTS.md`, which point back here.*
