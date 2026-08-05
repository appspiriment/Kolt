# Unit Testing

## Where tests live

| Layer | Test source set | Why |
|---|---|---|
| `domain` (UseCases, models) | `test` (local unit test) | Plain Kotlin, zero Android deps — runs on the JVM, no emulator/Robolectric needed. |
| `presentation` (ViewModel, delegates) | `test` | `ViewModel` + coroutines are plain-JVM testable — no Robolectric needed just to test state/effect logic. |
| `data` (repository impls) | `test` for the mapping/logic; Robolectric or instrumented (`androidTest`) only for something that genuinely needs a real Android framework class (e.g. a Room in-memory DB integration test) | Fake/mock the network/DB client at the interface boundary for everything else; don't spin up Robolectric to test a mapper function. |
| Compose UI (screens) | optional, `androidTest` (`androidx.compose.ui.test.junit4`) | See [Compose UI tests](#compose-ui-tests-optional) below — most of the value here is already covered by ViewModel tests plus `@Preview`. |

Use JUnit4 or JUnit5 (whichever this project already standardized on) —
there's no multiplatform constraint here forcing `kotlin.test`. `kotlin.test`
also works fine on Android (it delegates to JUnit under the hood) if the team
wants consistency with a KMP module elsewhere in the org, but it's not
required.

## Domain: fakes and mocks

UseCases depend on repository *interfaces* (see [architecture.md](architecture.md))
— small, hand-written fakes are still often less code and less brittle than a
mock for a simple interface, but on an Android-only project there's no
reason to avoid a mocking framework either:

```kotlin
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

**MockK is the standard choice here** — it's JVM-bytecode-based, which was a
hard blocker in the KMP steering set (breaks the moment a test moves into
`commonTest`/Kotlin-Native) but is simply not a concern on an Android-only
project. Reach for a hand-written fake first when the interface is small
(readable, no framework magic, doubles as documentation of the contract);
reach for MockK when the interface is large or the test only cares about two
of a dozen methods and a fake would be mostly unused boilerplate.

## ViewModel: drive Intents, assert State, Turbine for Effect

```kotlin
class OrdersListViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `Refresh loads orders into state`() = runTest(dispatcher) {
        val viewModel = OrdersListViewModel(GetOrdersUseCase(FakeOrdersRepository(orders = listOf(testOrder))))

        viewModel.onIntent(OrdersListIntent.Refresh)
        advanceUntilIdle()

        assertEquals(AsyncState.Success(persistentListOf(testOrder)), viewModel.state.value.orders)
    }

    @Test
    fun `SelectOrder sends NavigateToDetail effect`() = runTest(dispatcher) {
        val viewModel = OrdersListViewModel(GetOrdersUseCase(FakeOrdersRepository()))

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
- `Dispatchers.setMain`/`resetMain` via JUnit4 `@Before`/`@After` (or a
  `MainDispatcherRule` if the project already has one) — no multiplatform
  reason to avoid a JUnit4 `@Rule` here, unlike the KMP steering set.

## Compose UI tests (optional)

Most of the behavior worth testing is already covered by the ViewModel test
above plus the `@Preview` every stateless screen composable already has
(required by [presentation-mvi.md](presentation-mvi.md)'s checklist). Add an
actual Compose UI test only for interaction the ViewModel test can't see —
gesture handling, animation-driven visibility, focus order. Use
`createComposeRule()` (or `createAndroidComposeRule<ComponentActivity>()` if
the test needs a real Activity context) from
`androidx.compose.ui.test.junit4` — the standard Android instrumented-test
setup. Assert on semantics (`onNodeWithText`, `onNodeWithContentDescription`),
never on internal implementation (composable function calls, recomposition
count).

## Rules

- Test the `domain` UseCase and the `ViewModel`, not the Route composable's
  wiring (`backStack.add(...)` calls) — that's better covered by the
  `@Preview`-ability requirement and, if it matters enough, a thin
  Compose UI/navigation test, not a unit test asserting internals.
- Fakes for small/stable interfaces, MockK for large/volatile ones — pick one
  per test, don't mix both for the same dependency across a test class.
- One test class per production class (`OrdersListViewModelTest` next to
  `OrdersListViewModel` in the same screen package), not one giant test file
  per feature.
- A `Contract.kt`'s `State`/`Intent`/`Effect` are plain data — don't unit
  test them directly, test the `ViewModel` that produces/consumes them.
