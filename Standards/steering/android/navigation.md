# Navigation

## Use Navigation 3, not Navigation Compose (Nav2)

Navigation 3 (`androidx.navigation3`) went stable 1.0.0 Nov 2025. Official
Android architecture guidance now lists it as the recommended nav library —
this isn't a KMP-only recommendation, it applies to Android-only apps too.
Don't reach for `androidx.navigation:navigation-compose` / `NavHost` /
`NavGraphBuilder.composable<Route>` in a new project — that's the previous
generation. [Source: developer.android.com/guide/navigation/navigation-3](https://developer.android.com/guide/navigation/navigation-3).

Key shift: the back stack is **caller-owned** (a `SnapshotStateList<NavKey>`
you hold and mutate), not managed inside a `NavController`. There's no route
graph DSL — you resolve a `NavKey` to a `NavEntry` yourself via
`entryProvider`.

## Single Activity

Default: **one Activity, one back stack, one `NavDisplay`** for the whole
app — matches current official guidance. Each feature/journey module
contributes its own sealed `NavKey` subset; the app module aggregates them
into one `entryProvider`.

```kotlin
// :feature:orders, presentation/
@Serializable sealed interface OrdersRoute : NavKey
@Serializable data object OrdersList : OrdersRoute
@Serializable data class OrderDetail(val orderId: String) : OrdersRoute

// :feature:orders exposes exactly this — see architecture.md
fun EntryProviderBuilder<NavKey>.ordersEntries(backStack: NavBackStack) {
    entry<OrdersList> { OrdersListRoute(backStack) }
    entry<OrderDetail> { key -> OrderDetailRoute(key, backStack) }
}
```

```kotlin
// :app module — the single Activity
@Serializable sealed interface AppRoute : NavKey
@Serializable data object Home : AppRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppThemeProvider {
                // Plain @Serializable routes + default reflection-based
                // serialization is fine here — the polymorphic
                // SerializersModule dance in the KMP steering set exists
                // only because Kotlin/Native can't do reflection; an
                // Android-only app doesn't hit that.
                val backStack = rememberNavBackStack(Home)

                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(), // must be first
                        rememberViewModelStoreNavEntryDecorator(),      // scopes VM to the entry, clears on pop
                    ),
                    entryProvider = entryProvider {
                        entry<Home> { HomeRoute(backStack) }
                        ordersEntries(backStack)
                        checkoutEntries(backStack)
                    },
                )
            }
        }
    }
}
```

> `rememberViewModelStoreNavEntryDecorator` and the `entryProvider { entry<T> { } }`
> builder DSL are the documented mechanism, but Nav3 is young enough that
> exact factory-function names have moved between releases — check against
> the `androidx.navigation3.runtime` version pinned in this project before
> copying verbatim.

### Opt-in: multiple Activities

Split a journey into its own Activity (own back stack, own `NavDisplay`)
**only** when there's a concrete reason the single-back-stack model doesn't
cover:

- the journey ships as a standalone embeddable module a host app launches by
  `Intent` without depending on the rest of this app (SDK-style journey —
  e.g. an auth flow or checkout flow embedded in someone else's app),
- it needs its own process, task affinity, or launch mode distinct from the
  rest of the app,
- it's reached only via an external deep link and never composes with the
  rest of the app's back stack.

If none of those apply, don't split — the cost (re-provide theme/DI
`CompositionLocal`s per Activity, no cross-journey shared-element/predictive
back, more places for "one `AppThemeProvider` call" to be missed) isn't
worth it. When a split is warranted, each such Activity still follows
everything below (its own `NavKey` sealed hierarchy, its own
`rememberNavBackStack`, its own `NavDisplay`) — it's the same pattern, just
rooted at an extra Activity instead of a nested route in the main one.

## Navigation is lambda-only outside the entry-owning composable

Screens never hold the back stack or a nav callback map themselves beyond
what's passed in. The composable named in `entryProvider` (e.g.
`OrdersListRoute` above — this is where the ViewModel is instantiated, see
[presentation-mvi.md](presentation-mvi.md)) is the only place that calls
`backStack.add(...)` / `backStack.removeLast()`. It hands the *stateless*
screen only resolved lambdas (`onOrderClick: (String) -> Unit`).

```kotlin
// OrdersListRoute.kt — owns the ViewModel + back-stack mutation for this entry
@Composable
fun OrdersListRoute(backStack: NavBackStack) {
    val viewModel: OrdersListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    OrdersListScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOrderClick = { id -> backStack.add(OrderDetail(id)) },
    )
}
```

## Rules

- No `backStack.add/removeLast` call outside the composable named in `entryProvider` for that route.
- One back stack/`NavDisplay` for the whole app by default (see above). A feature module contributes routes and an `entryProvider` fragment; it never holds its own `NavDisplay` unless it's one of the opt-in split cases.
- Cross-journey navigation is just adding another `NavKey` to the same back stack — no intent/bundle marshaling needed unless you're in the opt-in multi-Activity case.
- Deep links: resolve the intent/URI in the Activity, map it to a `NavKey` (or a list of them, to seed a synthetic back stack), and seed `rememberNavBackStack` with it.
- Routes are `@Serializable` and implement `NavKey` (Nav3 uses kotlinx.serialization for saved-state, independent of any multiplatform concern). Default reflection-based serialization is enough for an Android-only app — no need for the polymorphic `SerializersModule`/`SavedStateConfiguration` setup the KMP steering set requires; add that only if this app ever grows an iOS/desktop target.
