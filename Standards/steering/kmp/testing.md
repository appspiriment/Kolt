# Unit Testing

## Where tests live

| Layer | Test source set | Why |
|---|---|---|
| `domain` (UseCases, models) | `commonTest` | Pure Kotlin, zero platform deps — runs on every target for free. |
| `presentation` (ViewModel, delegates) | `commonTest` | The `MviViewModel` base from [presentation-mvi.md](presentation-mvi.md) is `commonMain`; test it there so iOS/desktop get the same coverage as Android, not a copy-pasted Android-only test. |
| `data` (repository impls) | `commonTest` for the mapping/logic, platform test source set only for the actual network/DB call | Fake the network/DB client at the interface boundary; don't spin up Robolectric/instrumentation to test a mapper function. |
| Compose UI (screens) | optional, platform UI test source set | See [Compose UI tests](#compose-ui-tests-optional) below — most of the value here is already covered by ViewModel tests plus `@Preview`. |

Use `kotlin.test` (`@Test`, `@BeforeTest`, `@AfterTest`, `assertEquals`) in
anything under `commonTest` — it's the multiplatform-safe API (maps to
JUnit on JVM/Android, XCTest-backed on Native). Reaching for a bare
`org.junit.Test` or a JUnit4 `@Rule` in `commonTest` won't compile on iOS —
that's an Android/JVM-only tool leaking into shared code, the same mistake
[kolt-libs.md](kolt-libs.md) flags elsewhere in this stack.

## Domain: fakes over mocks

UseCases depend on repository *interfaces* (see [architecture.md](architecture.md))
— small, hand-written fakes are usually less code than wiring a mocking
framework, and they work identically on every target:

```kotlin
// commonTest
class FakeOrdersRepository(
    private val orders: List<Order> = emptyList(),
    private val error: Throwable? = null,
) : OrdersRepository {
    override suspend fun getOrders(page: Int): List<Order> =
        error?.let { throw it } ?: orders
}

class GetOrdersUseCaseTest {
    @Test
    fun `returns orders from repository`() = runTest {
        val useCase = GetOrdersUseCase(FakeOrdersRepository(orders = listOf(testOrder)))
        assertEquals(listOf(testOrder), useCase(page = 0))
    }
}
```

**The mocking-library gotcha:** MockK is JVM-bytecode-based — it doesn't run
on Kotlin/Native, so it breaks the moment a test using it moves from
`androidUnitTest`/`jvmTest` into `commonTest`. Don't reach for it (or any
JVM-only mock framework) for a `commonTest` file. If a hand-written fake
would be excessive boilerplate (a repository interface with a dozen
methods, only two of which matter to this test), use
[Mokkery](https://mokkery.dev) — it's compiler-plugin-driven and actually
multiplatform (KSP-based alternatives like Mockative/kmock can break across
`commonTest` under Kotlin 2.0's source-set separation). Same threshold as
[architecture.md's reuse-over-duplication rule](architecture.md#reuse-over-duplication):
reach for the library once a fake stops being the shortest path, not by
default.

## ViewModel: drive Intents, assert State, Turbine for Effect

```kotlin
// commonTest
class OrdersListViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `Refresh loads orders into state`() = runTest(dispatcher) {
        val viewModel = OrdersListViewModel(FakeOrdersRepository(orders = listOf(testOrder)))

        viewModel.onIntent(OrdersListIntent.Refresh)
        advanceUntilIdle()

        assertEquals(AsyncState.Success(persistentListOf(testOrder)), viewModel.state.value.orders)
    }

    @Test
    fun `SelectOrder sends NavigateToDetail effect`() = runTest(dispatcher) {
        val viewModel = OrdersListViewModel(FakeOrdersRepository())

        viewModel.effect.test {
            viewModel.onIntent(OrdersListIntent.SelectOrder("42"))
            assertEquals(OrdersListEffect.NavigateToDetail("42"), awaitItem())
        }
    }
}
```

- **State**: assert `viewModel.state.value` directly after the coroutine
  settles (`advanceUntilIdle()` with a `StandardTestDispatcher`, or just let
  an `UnconfinedTestDispatcher` run eagerly). Current official guidance is
  explicit about this — treat `StateFlow` as a data holder and assert its
  current value, don't wrap it in Turbine just to read one emission.
- **Effect**: this is a real event stream (one-shot, ordered, can fire
  multiple times per test) — that's exactly what
  [Turbine](https://github.com/cashapp/turbine)'s `Flow<T>.test { }` is for.
  Use it here, not for `State`.
- `Dispatchers.setMain`/`resetMain` via `@BeforeTest`/`@AfterTest` (not a
  JUnit4 `MainDispatcherRule`) so the same test class compiles in
  `commonTest`.

## Compose UI tests (optional)

Most of the behavior worth testing is already covered by the ViewModel test
above plus the `@Preview` every stateless screen composable already has
(required by [presentation-mvi.md](presentation-mvi.md)'s checklist). Add an
actual Compose UI test only for interaction the ViewModel test can't see —
gesture handling, animation-driven visibility, focus order. Use Compose
Multiplatform's `runComposeUiTest` (multiplatform, `commonTest`-capable) over
Android's `createAndroidComposeRule`, which drags in `androidUnitTest`/
instrumentation for something a stateless composable + fake `State` doesn't
need. Assert on semantics (`onNodeWithText`, `onNodeWithContentDescription`),
never on internal implementation (composable function calls, recomposition
count).

## Rules

- Test the `domain` UseCase and the `ViewModel`, not the Route composable's
  wiring (`backStack.add(...)` calls) — that's better covered by the
  `@Preview`-ability requirement and, if it matters enough, a thin
  Compose UI/navigation test, not a unit test asserting internals.
- No mocking framework in `commonTest` unless it's KMP-safe (Mokkery, not
  MockK/Mockative/kmock) — default to a hand-written fake first.
- One test class per production class (`OrdersListViewModelTest` next to
  `OrdersListViewModel` in the same screen package), not one giant test file
  per feature.
- A `Contract.kt`'s `State`/`Intent`/`Effect` are plain data — don't unit
  test them directly, test the `ViewModel` that produces/consumes them.
