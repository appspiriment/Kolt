# Clean Architecture — KMP

## Layers

```
presentation (Compose UI, ViewModel)
        │  depends on
        ▼
   domain (UseCases, models, repository interfaces — pure Kotlin, commonMain only)
        ▲  implements
        │
     data
        ├── repository impl   (implements the domain interface; depends on DataSource(s) only)
        └── data source       (the only classes allowed to touch Ktor/Room/SQLDelight/DataStore/etc.)
```

Dependency direction is one-way: `presentation` and `data` depend on `domain`.
`domain` depends on nothing platform-specific and imports no Android,
Compose, or `expect`/`actual` types — it's plain `commonMain` Kotlin, testable
on the JVM with no Robolectric/instrumentation.

**The call chain is strict and every link is mandatory — no skipping a
layer, and no layer depends on another instance of its own layer:**
`ViewModel → UseCase → Repository (interface) → RepositoryImpl →
DataSource → originator (network client / DB / preferences)`. Concretely:

- A `UseCase` may depend on any number of `Repository` interfaces — never on
  another `UseCase`.
- A `RepositoryImpl` may depend on any number of its own `DataSource`
  class(es) — never on another `Repository` (interface or impl), and never
  on `HttpClient`, a Room `Dao`, a SQLDelight query object, or `DataStore`
  directly.
- A `DataSource` wraps exactly one originator — never another `DataSource`.

Cross-repository orchestration happens one level up, in a `UseCase` that
takes both repositories as constructor params — not inside either
`RepositoryImpl`. This is what makes a `UseCase` "really fully testable" per
[testing.md](testing.md#domain-fakes-over-mocks): every dependency is a
`Repository` interface, so a test fakes each one it needs — still no mocking
framework, no database, no network, just N simple fakes instead of one.

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
  │     ├── OrdersRemoteDataSource.kt (wraps a Ktor-backed OrdersApi interface)
  │     ├── OrdersLocalDataSource.kt  (wraps a Room Dao / SQLDelight query object)
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

A `DataSource` is the only class allowed to import a network client, DB, or
platform storage API directly — and each `DataSource` wraps exactly one
originator:

```kotlin
// data — the only class that touches Ktor; the only place a ClientRequestException
// or IOException is legal to catch — everything above this rethrows as DomainException
class OrdersRemoteDataSource(private val client: HttpClient) {
    suspend fun fetchOrders(page: Int): List<OrderDto> =
        try {
            client.get("orders") { parameter("page", page) }.body()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Unauthorized) throw DomainException.Unauthorized(e)
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
// never on HttpClient/Dao directly
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
  swappable without the `Repository` caring.
- **Mapping is `data`'s job.** `OrderDto` (wire format) and `OrderEntity` (DB
  row) never cross into `domain`/`presentation` — only `Order` does. Mapper
  functions live in `data`, private to the module. DTOs are `@Serializable`
  (kotlinx.serialization) — not Gson/Moshi, which are JVM-reflection-based
  and don't run on Kotlin/Native, the same Android-only trap as everything
  else flagged in this doc.
- **KMP originators, not Android-only ones** — this is the same trap as
  Hilt/`AndroidViewModel`/Nav2 elsewhere in this steering set: Retrofit is
  JVM/Android-only and won't compile in `commonMain`. Use **Ktor** for
  network, **Room KMP (2.7+) or SQLDelight** for local SQL (either is fine,
  don't mix both in one project), and **`androidx.datastore` multiplatform**
  for preferences — never `SharedPreferences` directly.
- **Exceptions get mapped to `domain` types at the `DataSource` boundary —
  never propagated raw.** A `ClientRequestException` (Ktor), `SQLException`
  (SQLDelight/Room), or any other platform exception type is only ever
  caught inside the `DataSource` that produced it, and rethrown as a plain
  sealed `DomainException` (a `domain` model, no platform imports — put it
  in a shared module every feature's `domain` depends on, not duplicated
  per feature):
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
  would have to know about Ktor to catch its errors — exactly the layering
  leak the rest of this doc forbids for data types, just via exceptions
  instead of DTOs.
- **Error handling — one wrapper, not two, above the `DataSource`.**
  `RepositoryImpl` and `UseCase` don't catch anything — they let the already
  domain-typed exception propagate (that's what keeps a `UseCase` trivially
  fake-testable per [testing.md](testing.md): a fake `Repository` throws
  `DomainException` directly, no Ktor/DB dependency needed in the test
  either). The `ViewModel` is the single place a `DomainException` gets
  caught and turned into `AsyncState.Error(throwable)` (see
  [presentation-mvi.md](presentation-mvi.md)) — `AsyncState` already is this
  project's `Result` type; don't add a second one in `domain`.

## Build-time config: not `BuildConfig`

API keys/base URLs/feature flags baked in at build time need a `commonMain`
answer — Android's `BuildConfig` is Android-only and doesn't exist outside
`androidMain`, the same trap as everything else Android-only in this doc
set. Use the [BuildKonfig](https://github.com/yshrsmz/BuildKonfig) Gradle
plugin (generates a `commonMain`-visible object from Gradle properties) or
an equivalent generated `expect`/`actual` constants object — never a
platform-specific `BuildConfig.API_KEY` referenced from shared code.

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

- **commonMain first.** Write in `commonMain`. Drop to `expect`/`actual` only
  at an actual platform boundary (permissions, sensors, file system, native
  SDKs). If a library already ships a multiplatform artifact
  (`org.jetbrains.androidx.*`, `io.ktor`, `androidx.datastore` KMP, `koin`),
  use it — don't hand-roll `expect`/`actual` for something already solved.
- **DI**: Koin. It's KMP-native (`commonMain` module graph, no codegen), and
  `koinViewModel()`/`get()` works the same in every Route composable. Don't
  introduce Hilt/Dagger into a KMP app — they're Android-only (official
  KMP ViewModel guidance confirms no Hilt support).
- **No God ViewModel.** One ViewModel per journey/screen, scoped to its own
  back-stack `NavEntry` (via `rememberViewModelStoreNavEntryDecorator`, see
  [navigation.md](navigation.md)). Cross-journey state goes through the
  domain layer (a shared UseCase/repository), not a shared ViewModel.
- **Repository interfaces live in `domain`, implementations in `data`.** The
  ViewModel/UseCase never imports a `data`-layer class directly, and
  `RepositoryImpl` never imports a network client/DAO/DataStore directly —
  see [Data layer](#data-layer-repository--datasource--originator) above.
- **No same-layer dependencies.** `UseCase` never depends on another
  `UseCase`; `RepositoryImpl` never depends on another `Repository`;
  `DataSource` never depends on another `DataSource`. Cross-cutting logic
  moves up one layer (a `UseCase` combining two repositories) or sideways as
  a plain, non-`UseCase`/non-`Repository`-suffixed function or class — never
  sideways as a same-type dependency.
- `Kolt/libs` is optional. If it's present in this workspace, check
  [kolt-libs.md](kolt-libs.md) before writing a new util/wrapper — several of
  these problems (async state, KMP theme tokens, KMP logging) are already
  solved there. If it isn't present, the equivalents defined inline in
  [theming.md](theming.md) and [presentation-mvi.md](presentation-mvi.md)
  cover the same ground standalone.
