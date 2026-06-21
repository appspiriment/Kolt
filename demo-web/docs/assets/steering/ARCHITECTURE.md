# Architecture — Android / KMP

**Authority:** Binding patterns. `CODING_STANDARDS.md` = the laws (what's allowed); this = the shape (how to build it).
**Read on demand** for the layer/topic you touch. Don't load the whole file for a one-line change.

Section map: §1 Layers · §2 DI strategy · §3 Offline-first data layer · §4 Layer wiring (end-to-end) · §5 Android-only vs KMP deltas · §6 Abstractions catalog (reuse, don't rewrite) · §7 Error handling · §8 Performance.

---

## 1. Layers

```
UI (Compose)  →  Presentation (ViewModel/State/Intent/Effect)  →  Domain (entities, use cases, repo interfaces)
                                                                       ↑
                                          Data (repo impls, local store, remote, mappers)  →  Domain
```

Dependencies point **inward**. Domain depends on nothing platform. UI/Data depend on Domain, never the reverse. Features never depend on each other — shared types go to Domain.

| Layer | Owns | Never contains |
|---|---|---|
| UI | Stateless composables, theme/tokens | Business logic, VM below the Route |
| Presentation | ViewModel, State, Intent, Effect | I/O, platform SDKs, DB/network |
| Domain | Entities, value objects, use cases, repo **interfaces** | Android, Compose, DB, network, DI framework annotations |
| Data | Repo **impls**, local store, remote, mappers, DTOs | UI/presentation types |

---

## 2. DI strategy

- **Constructor injection everywhere.** No service locators, no static singletons holding state. The DI framework wires; classes stay framework-agnostic (plain constructors).
- **Composition root** is the only place that knows concrete impls: `Application` (Android) / platform entry (KMP). Bind interface → impl there.
- **Scope deliberately:** singletons for stateless/shared (repos, DB, dispatchers); per-screen for ViewModels.
- **Pick one** per project: Hilt (Android-only, annotation), Koin (KMP, DSL), kotlin-inject (KMP, compile-time). Keep domain/data classes free of DI annotations where the framework allows (Koin/kotlin-inject do; Hilt needs `@Inject` constructors — acceptable, it's not a platform dep).

```kotlin
// Domain/data class — DI-agnostic, just a constructor:
class CartRepositoryImpl(
    private val local: CartLocalDataSource,
    private val queue: OfflineMutationQueue,
    private val dispatchers: DispatcherProvider,
) : CartRepository

// Composition root binds it (example: Koin)
single<CartRepository> { CartRepositoryImpl(get(), get(), get()) }
```

---

## 3. Offline-first data layer

The single hardest part to get right. The contract: **the app is fully usable with no network, ever.**

### 3.1 The three-tier stack

```
Repository (data)  ── reads ──▶  Local store (Room/SQLDelight)         ← single source of truth for the UI
      │  writes
      ▼
Local store  ──then──▶  OfflineMutationQueue.enqueue(mutation)
                                   │  (background)
                                   ▼
                          SyncEngine drains queue ──▶ RemoteSyncDataSource (push/pull)
                                   │
                                   ▼  pulled records written back to Local store → Flow re-emits → UI updates
```

### 3.2 Hard rules

- **Every read** comes from the local store as a `Flow`. No use case / ViewModel / composable ever reads remote.
- **Every write** goes local **first**, then enqueues a `SyncMutation`. The UI reflects the write instantly from the local Flow — it does not wait for the network.
- **Remote is isolated** behind `RemoteSyncDataSource`. All backend SDK imports live in the sync/remote module only. Swapping backend = new impl + DI rebind, zero other changes.
- **Sync is invisible to upper layers.** Pulled data lands in the local store; the existing `Flow` emits; the UI updates with no knowledge a sync happened.
- **Conflicts** resolved in one place (the SyncEngine / a `ConflictResolver`), by an explicit policy (last-write-wins via `updatedAt`, or field-merge). Never silently in a repository.

### 3.3 Behaviour matrix

| Scenario | Behaviour |
|---|---|
| No network ever | 100% functional via local store |
| Write offline | Saved locally now; queued; UI updates immediately |
| Network returns | SyncEngine drains queue; remote updated |
| Remote changed elsewhere | Pulled next cycle; conflict policy applied; local updated |
| Auth expired | Sync pauses; local CRUD unaffected; resumes after re-auth |
| Backend retired | Swap `RemoteSyncDataSource`; local data untouched |

*(If a project has **no** remote backend, drop the queue/sync tier — the local store is simply the source of truth. Everything above the repository is identical.)*

---

## 4. Layer wiring (end-to-end, one feature)

```
Composable(state, onIntent)
   └─ onIntent(Intent) ─▶ ViewModel
                            ├─ reduce(state,intent) → state          (pure)
                            └─ handleSideEffects → UseCase(params)    (async)
                                                      └─ Repository (interface)
                                                            └─ Local store (read Flow / write+enqueue)
   result re-enters ViewModel as a new Intent ─▶ reduce → state ─▶ StateFlow ─▶ Composable recomposes
   one-shot Effect ─▶ Channel ─▶ HandleEffects (nav/toast)
```

One vertical slice per feature: `Screen + ViewModel + State + Intent + Effect` in presentation; `UseCase`s in domain; `RepositoryImpl + DataSource + Mapper` in data.

---

## 5. Android-only vs KMP — the deltas (same architecture)

The architecture is identical. Only the **mechanism** differs:

| Concern | Android-only | KMP |
|---|---|---|
| Layer boundary | Packages (`domain/`, `data/`, `ui/`) or Gradle modules | Gradle modules (`:domain`, `:data`, `:feature:*`, `:core:*`) |
| Platform code | Direct | `expect/actual` in `commonMain` + `androidMain`/`iosMain` |
| Local store | Room | SQLDelight (or Room KMP) |
| DI | Hilt or Koin | Koin or kotlin-inject |
| ViewModel | `androidx.lifecycle.ViewModel` | `androidx.lifecycle.ViewModel` (KMP) — shared |
| Dispatchers.IO | available | available on JVM/Android/Native; abstract via `DispatcherProvider` |

Write everything in shared/common terms; push only the irreducible platform bits behind `expect/actual` or an interface.

---

## 6. Abstractions catalog — reuse, do NOT rewrite

Every project must provide these in its `:core:common` (KMP) / `core` package — the canonical implementations live in the shared core library, **not** in each feature. **Extend them; never reimplement.** An agent that writes its own MVI base, result type, or use-case base is in violation — find the existing one first.

| Type | Purpose |
|---|---|
| `MviViewModel<S,I,E>` (or your base name) | MVI base: state `StateFlow`, effect `Channel`, pure `reduce`, `handleSideEffects`, `onIntent` |
| `UiState` / `UiIntent` / `UiEffect` | Marker interfaces for the contracts |
| `AppResult<T>` / `AppError` | Sealed result + error for every async/fallible call |
| `UseCase<P,R>` / `FlowUseCase<P,R>` | Single-shot / streaming business operation, dispatcher-bound |
| `DispatcherProvider` | Injectable dispatchers (`io` / `default` / `main`) |
| `safeCall` / `safeFlow` | Wrap I/O into `AppResult` |
| `OfflineMutationQueue`, `SyncMutation`, `RemoteSyncDataSource`, `SyncRecord` | Offline-first sync contracts |

> Before writing any of the above, search the project's core module for an existing one. These are infrastructure, written once per project (or shared via the core library), then reused everywhere.

---

## 7. Error handling

- Every async/fallible call returns `AppResult<T>` (or `Flow<AppResult<T>>`). **No exceptions cross layer boundaries** — the data layer catches and maps to `AppError`.
- Errors are **state**, not crashes: a `VaultResult.Failure` becomes a visible `state.error`, never a silent swallow.
- Every error state has a **recovery path** (a retry Intent). No dead ends.
- Map low-level exceptions to a small `AppError` set (`Network`, `Storage`, `NotFound`, `Validation`, `Unauthorized`, `Unknown`) at the data boundary; upper layers branch on the sealed type, never on raw exceptions.

---

## 8. Performance (efficiency, fewer jank bugs)

- **State stability:** State is an immutable `data class` of stable types. Unstable types (raw `List`, lambdas recreated each call) cause needless recomposition — use immutable collections / hoisted lambdas.
- **Derived values** are computed `get()` props on State or `derivedStateOf` — never recomputed inline in composition.
- **Lazy lists:** always provide stable `key`s; avoid allocating in the item lambda.
- **Don't read `StateFlow` too high.** Collect `state` at the Route; pass slices down so unrelated changes don't recompose whole subtrees.
- **No work in composition:** no I/O, no sorting/filtering, no `Dispatchers` launches in a composable body — that's the ViewModel's job.
- **Coroutines:** correct dispatcher (`io` for I/O, `default` for CPU); structured concurrency only (VM scope); cancel with lifecycle.
- **DB/network:** one query per screen where possible; paginate large lists; never N+1 from the UI.

---

*Laws → `CODING_STANDARDS.md`. Tests → `TESTING.md`. Canonical base classes live in your shared core library.*
