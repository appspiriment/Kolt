# Testing — Android / KMP

**Goal:** fewer bugs at low cost. Test the logic that breaks, skip the trivial. Read on demand.

## 1. What to test (priority order)

| Must test (high value, cheap) | Skip (low value) |
|---|---|
| **Reducers** — pure `(state, intent) → state`, every branch | Composable pixels (use previews instead) |
| **Use cases** — business rules, success + failure paths | Getters/`data class` equality |
| **Mappers / validators** — every edge case | Framework/DI wiring |
| **Conflict resolution & offline queue** logic | Third-party libs |
| **Repository impls** — local read/write + enqueue (with fakes) | |

Reducers are pure → testing them is trivial and catches most state bugs. Aim **100% branch coverage on reducers**, ~90% on use cases.

## 2. Patterns

**Reducer (no coroutines needed — it's pure):**
```kotlin
@Test
fun `QueryChanged filters visible items`() {
    val vm = CartViewModel(getTotal = FakeGetTotal(), dispatchers = TestDispatchers())
    vm.onIntent(CartIntent.Loaded(listOf(apple, banana)))

    vm.onIntent(CartIntent.QueryChanged("app"))

    assertThat(vm.state.value.visibleItems).containsExactly(apple)
}
```

**Use case (runTest + fake repo):**
```kotlin
@Test
fun `returns Failure when repo errors`() = runTest {
    val useCase = GetCartTotalUseCase(FakeCartRepository(error = AppError.Storage))
    val result = useCase(cartId)
    assertThat(result).isInstanceOf(AppResult.Failure::class.java)
}
```

**Flow (Turbine):**
```kotlin
repo.observeItems().test {
    assertThat(awaitItem()).isEqualTo(AppResult.Success(emptyList()))
    cancelAndIgnoreRemainingEvents()
}
```

## 3. Rules

- **Fakes over mocks.** Hand-write `Fake{Interface}` in `commonTest/.../fake/`; reuse across tests. A fake must honor the real contract (LSP) — same result semantics, same ordering.
- **AAA** structure: Arrange / Act / Assert, one logical assertion per test.
- **Names:** `subject condition expected` in backticks.
- **Deterministic:** inject `DispatcherProvider` (test = `UnconfinedTestDispatcher`/`StandardTestDispatcher`); inject a `Clock`/time provider — never `System.currentTimeMillis()` in code under test. No flaky/sleep-based tests.
- **One behaviour per test.** A failing test name should point at the bug.

## 4. Coverage targets (suggested, CI-enforced)

| Area | Line | Branch |
|---|---|---|
| Reducers | 95% | 100% |
| Use cases | 90% | 85% |
| Mappers / validators | 100% | 100% |
| Crypto / security-critical | 100% | 100% |
| Everything else | 70% | — |

*Stack: kotlin-test / JUnit, Turbine (Flow), Truth or kotlin-test assertions.*
