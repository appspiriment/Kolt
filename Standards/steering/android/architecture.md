# Clean Architecture — Android

## Layers

```
presentation (Compose UI, ViewModel)
        │  depends on
        ▼
   domain (UseCases, models, repository interfaces — plain Kotlin, no Android imports)
        ▲  implements
        │
     data
        ├── repository impl   (implements the domain interface; depends on DataSource(s) only)
        └── data source       (the only classes allowed to touch Retrofit/Room/DataStore/etc.)
```

Dependency direction is one-way: `presentation` and `data` depend on `domain`.
`domain` imports no `android.*`/`androidx.*`/Compose types — plain Kotlin,
testable on the JVM with no Robolectric/instrumentation. (This module doesn't
need `commonMain`/`expect`/`actual` — that's a KMP concept. Just don't import
Android framework classes into it; keep it a plain `java`/`kotlin` source
set, or a Kotlin-only Gradle module if you want the boundary enforced by the
build graph.)

**The call chain is strict and every link is mandatory — no skipping a
layer, and no layer depends on another instance of its own layer:**
`ViewModel → UseCase → Repository (interface) → RepositoryImpl →
DataSource → originator (network client / DB / preferences)`. Concretely:

- A `UseCase` may depend on any number of `Repository` interfaces — never on
  another `UseCase`.
- A `RepositoryImpl` may depend on any number of its own `DataSource`
  class(es) — never on another `Repository` (interface or impl), and never
  on Retrofit's API interface, a Room `Dao`, or `DataStore` directly.
- A `DataSource` wraps exactly one originator — never another `DataSource`.

Cross-repository orchestration happens one level up, in a `UseCase` that
takes both repositories as constructor params — not inside either
`RepositoryImpl`. This is what makes a `UseCase` "really fully testable" per
[testing.md](testing.md#domain-fakes-and-mocks): every dependency is a
`Repository` interface, so a test fakes (or mocks) each one it needs — no
database, no network.

## Module layout (per journey/feature)

`presentation/` is one package per screen — never a flat folder shared by
every screen in the feature. Each screen package is self-contained: its
`State`/`Intent`/`Effect` live together in one `Contract.kt`, not three
separate files.

```
:feature:orders
  ├── domain/
  │     ├── GetOrdersUseCase.kt / RefreshOrdersUseCase.kt   (operator fun invoke, see below)
  │     ├── OrdersRepository.kt   (interface only)
  │     └── Order.kt              (domain model)
  ├── data/
  │     ├── OrdersRepositoryImpl.kt   (implements OrdersRepository; depends on the two data sources below)
  │     ├── OrdersRemoteDataSource.kt (wraps a Retrofit OrdersApi interface)
  │     ├── OrdersLocalDataSource.kt  (wraps a Room Dao)
  │     ├── OrderDto.kt / OrderEntity.kt
  │     └── OrderMappers.kt       (Dto/Entity → Order, private to `data`)
  └── presentation/
        ├── list/
        │     ├── OrdersListContract.kt   (OrdersListState / Intent / Effect)
        │     ├── OrdersListViewModel.kt
        │     ├── OrdersListRoute.kt      (owns the ViewModel + back-stack calls, see navigation.md)
        │     └── OrdersListScreen.kt     (stateless root composable, see presentation-mvi.md)
        └── detail/
              ├── OrderDetailContract.kt
              ├── OrderDetailViewModel.kt
              ├── OrderDetailRoute.kt
              └── OrderDetailScreen.kt
```

A feature module's public surface to the app module is exactly two things:
its `NavKey` sealed route hierarchy, and one `entryProvider` fragment
function (e.g. `fun EntryProviderBuilder<NavKey>.ordersEntries(backStack: NavBackStack)`)
that the app module's single `NavDisplay` aggregates — see
[navigation.md](navigation.md). Nothing else in `presentation` is exposed,
and nothing in one screen's package imports from another screen's package —
shared state goes through `domain`, not a cross-screen import.

## UseCase shape

One `operator fun invoke(...)` per UseCase, named `{VerbPresentTense}{Noun}UseCase`
(`GetOrdersUseCase`, `RefreshOrdersUseCase`, not `OrdersManager` or
`OrdersInteractor`). Constructor takes `Repository` interfaces only — as
many as the use case actually needs, not capped at one — **never another
`UseCase`**:

```kotlin
// domain — depends on the OrdersRepository interface, nothing else
class GetOrdersUseCase(private val repository: OrdersRepository) {
    suspend operator fun invoke(page: Int): List<Order> = repository.getOrders(page)
}

// domain — combining two repositories in one UseCase is normal
class GetOrdersWithCustomerUseCase(
    private val ordersRepository: OrdersRepository,
    private val customersRepository: CustomersRepository,
) {
    suspend operator fun invoke(orderId: String): OrderWithCustomer {
        val order = ordersRepository.getOrder(orderId)
        val customer = customersRepository.getCustomer(order.customerId)
        return OrderWithCustomer(order, customer, formatCurrency(order.total))
    }
}

// domain — plain function, not a UseCase; this is how logic gets shared
// between UseCases without one UseCase depending on another
fun formatCurrency(amount: Money): String = /* ... */
```

Called as `getOrdersUseCase(page = 0)`. A UseCase that just forwards to one
repository call, like `GetOrdersUseCase`, is fine — it exists for the
call-chain discipline and the seam it gives tests, not because every UseCase
has to contain complex logic or combine multiple sources. If logic needs to
be shared *between* UseCases, it goes in a plain `domain` function or class
(no `UseCase` suffix, not registered as a `UseCase` in DI) that both depend
on — never a `UseCase`-to-`UseCase` call, per the no-same-layer rule above.

## Data layer: Repository → DataSource → originator

A `DataSource` is the only class allowed to import Retrofit, Room, or
`DataStore` directly — and each `DataSource` wraps exactly one originator:

```kotlin
// data — the only class that touches Retrofit; the only place an HttpException
// or IOException is legal to catch — everything above this rethrows as DomainException
class OrdersRemoteDataSource(private val api: OrdersApi) {
    suspend fun fetchOrders(page: Int): List<OrderDto> =
        try {
            api.getOrders(page)
        } catch (e: HttpException) {
            if (e.code() == 401) throw DomainException.Unauthorized(e)
            else throw DomainException.Unknown(e)
        } catch (e: IOException) {
            throw DomainException.NoConnectivity(e)
        }
}

// data — the only class that touches the DB
class OrdersLocalDataSource(private val dao: OrdersDao) {
    suspend fun getCachedOrders(): List<OrderEntity> = dao.getAll()
    suspend fun replaceAll(orders: List<OrderEntity>) = dao.replaceAll(orders)
}

// data — implements the domain interface; depends on DataSources only,
// never on the Retrofit API interface/Dao directly
class OrdersRepositoryImpl(
    private val remote: OrdersRemoteDataSource,
    private val local: OrdersLocalDataSource,
) : OrdersRepository {
    override suspend fun getOrders(page: Int): List<Order> {
        val cached = local.getCachedOrders()
        if (cached.isNotEmpty()) return cached.map { it.toDomain() }
        val fresh = remote.fetchOrders(page)
        local.replaceAll(fresh.map { it.toEntity() })
        return fresh.map { it.toDomain() }
    }
}
```

- **Naming**: `{Noun}RemoteDataSource` / `{Noun}LocalDataSource`, never named
  after the implementation (`OrdersRetrofitDataSource`,
  `OrdersRoomDataSource`) — the point of the seam is that the originator is
  swappable (a test fake, a future migration to Ktor/SQLDelight) without the
  `Repository` caring.
- **Mapping is `data`'s job.** `OrderDto` (wire format) and `OrderEntity` (DB
  row) never cross into `domain`/`presentation` — only `Order` does. Mapper
  functions live in `data`, private to the module. Use kotlinx.serialization
  (`@Serializable`) or Moshi for DTOs — either is fine for an Android-only
  app; avoid Gson for new code (reflection-based, no null-safety, slower).
- **Originators**: **Retrofit** + OkHttp for network (Ktor is also fine if
  the team prefers it — there's no multiplatform constraint forcing either
  way here), **Room** for local SQL, and **`androidx.datastore`** for
  preferences — never `SharedPreferences` directly (still true even without
  a KMP target: DataStore is transactional, `Flow`-based, and doesn't block
  the calling thread the way `SharedPreferences.apply()`/`commit()` can).
- **Exceptions get mapped to `domain` types at the `DataSource` boundary —
  never propagated raw.** An `HttpException`/`IOException` (Retrofit) or
  `SQLException` (Room) is only ever caught inside the `DataSource` that
  produced it, and rethrown as a plain sealed `DomainException` (a `domain`
  model, no Android imports — put it in a shared module every feature's
  `domain` depends on, not duplicated per feature):
  ```kotlin
  // domain — plain Kotlin, shared across features
  sealed class DomainException(cause: Throwable? = null) : Exception(cause) {
      class NoConnectivity(cause: Throwable? = null) : DomainException(cause)
      class Unauthorized(cause: Throwable? = null) : DomainException(cause)
      class NotFound(cause: Throwable? = null) : DomainException(cause)
      class Unknown(cause: Throwable? = null) : DomainException(cause)
  }
  ```
  Without this, a `ViewModel` or test written against `OrdersRepository`
  would have to know about Retrofit to catch its errors — exactly the
  layering leak the rest of this doc forbids for data types, just via
  exceptions instead of DTOs.
- **Error handling — one wrapper, not two, above the `DataSource`.**
  `RepositoryImpl` and `UseCase` don't catch anything — they let the already
  domain-typed exception propagate (that's what keeps a `UseCase` trivially
  fake-testable per [testing.md](testing.md): a fake `Repository` throws
  `DomainException` directly, no network/DB dependency needed in the test
  either). The `ViewModel` is the single place a `DomainException` gets
  caught and turned into `AsyncState.Error(throwable)` (see
  [presentation-mvi.md](presentation-mvi.md)) — `AsyncState` already is this
  project's `Result` type; don't add a second one in `domain`.

## Build-time config

API keys/base URLs/feature flags baked in at build time: Android's
`BuildConfig` (generated per build variant/flavor from `buildConfigField` in
Gradle) is the right tool here — there's no `commonMain` boundary to keep it
out of. Just don't commit real secrets into `build.gradle.kts`/version
control; pull them from `local.properties`/CI secrets into
`buildConfigField` at build time.

## Reuse over duplication

Extract a delegate/util/component once real duplication shows up — **rule of
three**: the same non-trivial logic in three places (two places if it's
error-prone, like pagination or validation), not on the first or second
write. Speculative abstraction for a single call site is exactly what
[Kolt's dead `ViewModelDelegate`](kolt-libs.md#dead-code--dont-copy-the-pattern)
is a cautionary tale of — a well-shaped class nothing ended up using, sitting
next to two ViewModels that reimplemented its logic by hand instead. If you
extract something, wire the existing call sites to actually use it in the
same change; an unused abstraction is worse than the duplication it was
meant to remove.

Before writing a new one, check in this order: [kolt-libs.md](kolt-libs.md)
(if `Kolt/libs` is in the workspace, using what it lists under "reuse as-is"
is mandatory, not a suggestion) → this project's own `:theme`/shared
component module → then write it.

Three concrete extraction targets in this stack:

- **Reusable Compose components** (buttons, cards, list rows, empty/error
  states) → a shared component module alongside `:theme` (same
  `CompositionLocal`-driven styling, no ViewModel/navigation awareness, pure
  presentation — same contract as the stateless screen composables in
  [presentation-mvi.md](presentation-mvi.md)). Not copy-pasted per screen.
- **Reusable ViewModel logic** (pagination, debounced search, field
  validation) → a small delegate class the ViewModel holds as a `private val`
  and forwards to — composition, not a deeper `MviViewModel` subclass
  hierarchy (Kotlin has no multiple inheritance, and a subclass-per-concern
  chain turns into a God base class fast).
- **Reusable business rules** → a plain `domain` function/class (no
  `UseCase` suffix — see [UseCase shape](#usecase-shape)), not the same
  calculation copy-pasted into two ViewModels and not a `UseCase` depending
  on another `UseCase`.

## Rules

- **DI**: Hilt. It's the officially recommended choice for Android apps of
  any real size — compile-time-checked, first-class `ViewModel`/`WorkManager`
  integration, and (unlike in a KMP project) there's no multiplatform
  constraint ruling it out here. Koin is an acceptable alternative if the
  team already standardized on it, but default to Hilt for a new
  Android-only project.
- **No God ViewModel.** One ViewModel per journey/screen, scoped to its own
  back-stack `NavEntry` (via `rememberViewModelStoreNavEntryDecorator`, see
  [navigation.md](navigation.md)) or `@HiltViewModel` + `hiltViewModel()`.
  Cross-journey state goes through the domain layer (a shared UseCase/
  repository), not a shared ViewModel.
- **Repository interfaces live in `domain`, implementations in `data`.** The
  ViewModel/UseCase never imports a `data`-layer class directly, and
  `RepositoryImpl` never imports Retrofit's API interface/a Room `Dao`/
  `DataStore` directly — see [Data layer](#data-layer-repository--datasource--originator)
  above.
- **No same-layer dependencies.** `UseCase` never depends on another
  `UseCase`; `RepositoryImpl` never depends on another `Repository`;
  `DataSource` never depends on another `DataSource`. Cross-cutting logic
  moves up one layer (a `UseCase` combining two repositories) or sideways as
  a plain, non-`UseCase`/non-`Repository`-suffixed function or class — never
  sideways as a same-type dependency.
- `Kolt/libs` is optional. If it's present in this workspace, check
  [kolt-libs.md](kolt-libs.md) before writing a new util/wrapper — most of it
  is directly usable in an Android-only app (it's KMP-authored, which is a
  superset, not a mismatch, for an Android target). If it isn't present, the
  equivalents defined inline in [theming.md](theming.md) and
  [presentation-mvi.md](presentation-mvi.md) cover the same ground
  standalone.
