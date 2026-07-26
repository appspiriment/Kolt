package io.github.appspiriment.kolt.locationpicker

import app.cash.turbine.test
import io.github.appspiriment.kolt.location.GeoLocation
import io.github.appspiriment.kolt.location.LocationResult
import io.github.appspiriment.kolt.location.PlaceSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocationPickerViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(gateway: LocationPickerGateway, config: LocationPickerConfig = LocationPickerConfig()) =
        LocationPickerViewModel(config, gateway)

    @Test
    fun `search below min chars clears results without calling the gateway`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway()
        val vm = viewModel(gateway)

        vm.onIntent(LocationPickerIntent.SearchQueryChanged("ab"))
        advanceTimeBy(1000)

        assertEquals(0, gateway.searchCallCount)
        assertTrue(vm.state.value.placeResults.isEmpty())
        assertFalse(vm.state.value.isSearchingPlaces)
    }

    @Test
    fun `search at min chars debounces then calls the gateway`() = runTest(dispatcher) {
        val place = PlaceSearchResult(label = "Bengaluru, India", latitude = 12.9716, longitude = 77.5946)
        val gateway = FakeLocationPickerGateway(searchResults = listOf(place))
        val vm = viewModel(gateway)

        vm.onIntent(LocationPickerIntent.SearchQueryChanged("ben"))
        assertTrue(vm.state.value.isSearchingPlaces)

        advanceTimeBy(1000)

        assertEquals(1, gateway.searchCallCount)
        assertEquals(listOf(place), vm.state.value.placeResults)
        assertFalse(vm.state.value.isSearchingPlaces)
    }

    @Test
    fun `selecting a search result resolves timezone and switches to manual tab`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway(timezoneResult = "Asia/Kolkata")
        val vm = viewModel(gateway)
        val place = PlaceSearchResult(label = "Bengaluru, India", latitude = 12.9716, longitude = 77.5946)

        vm.onIntent(LocationPickerIntent.SearchResultSelected(place))
        advanceTimeBy(1000)

        val state = vm.state.value
        assertEquals("Bengaluru", state.label)
        assertEquals("12.9716", state.latitude)
        assertEquals("77.5946", state.longitude)
        assertEquals("Asia/Kolkata", state.timezoneId)
        assertEquals(LocationPickerTab.MANUAL, state.selectedTab)
        assertFalse(state.isResolvingTimezone)
    }

    @Test
    fun `selecting a search result with unresolvable timezone surfaces the config error`() = runTest(dispatcher) {
        val config = LocationPickerConfig(timezoneDetectFailedLabel = "no tz")
        val gateway = FakeLocationPickerGateway(timezoneResult = null)
        val vm = viewModel(gateway, config)
        val place = PlaceSearchResult(label = "Nowhere", latitude = 1.0, longitude = 1.0)

        vm.onIntent(LocationPickerIntent.SearchResultSelected(place))
        advanceTimeBy(1000)

        assertEquals("no tz", vm.state.value.locationError)
        assertEquals(LocationPickerTab.MANUAL, vm.state.value.selectedTab)
    }

    @Test
    fun `map pick with a resolved timezone applies it directly without a resolving step`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway()
        val vm = viewModel(gateway)
        val picked = PickedMapLocation(latitude = 10.0, longitude = 20.0, label = "Somewhere", timezoneId = "UTC")

        vm.onIntent(LocationPickerIntent.MapPicked(picked))

        val state = vm.state.value
        assertEquals("Somewhere", state.label)
        assertEquals("UTC", state.timezoneId)
        assertEquals(LocationPickerTab.MANUAL, state.selectedTab)
        assertFalse(state.isResolvingTimezone)
    }

    @Test
    fun `map pick without a resolved timezone falls back to resolveTimezone`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway(timezoneResult = "Europe/London")
        val vm = viewModel(gateway)
        val picked = PickedMapLocation(latitude = 10.0, longitude = 20.0, label = null, timezoneId = null)

        vm.onIntent(LocationPickerIntent.MapPicked(picked))
        advanceTimeBy(1000)

        assertEquals("Europe/London", vm.state.value.timezoneId)
        assertEquals(LocationPickerTab.MANUAL, vm.state.value.selectedTab)
    }

    @Test
    fun `current location success populates fields and switches to manual tab`() = runTest(dispatcher) {
        val success = LocationResult.Success(
            GeoLocation(latitude = 1.0, longitude = 2.0, label = "Home", timezoneId = "Asia/Kolkata")
        )
        val gateway = FakeLocationPickerGateway(currentLocationResult = success)
        val vm = viewModel(gateway)

        vm.onIntent(LocationPickerIntent.TriggerCurrentLocation)
        advanceTimeBy(1000)

        val state = vm.state.value
        assertEquals("Home", state.label)
        assertEquals("1.0", state.latitude)
        assertEquals("2.0", state.longitude)
        assertEquals("Asia/Kolkata", state.timezoneId)
        assertEquals(LocationPickerTab.MANUAL, state.selectedTab)
        assertFalse(state.isFetchingLocation)
    }

    @Test
    fun `current location permission denied surfaces the config error and stays put`() = runTest(dispatcher) {
        val config = LocationPickerConfig(permissionDeniedLabel = "denied")
        val gateway = FakeLocationPickerGateway(currentLocationResult = LocationResult.PermissionDenied)
        val vm = viewModel(gateway, config)

        vm.onIntent(LocationPickerIntent.TriggerCurrentLocation)
        advanceTimeBy(1000)

        assertEquals("denied", vm.state.value.locationError)
        assertFalse(vm.state.value.isFetchingLocation)
        assertEquals(LocationPickerTab.SEARCH, vm.state.value.selectedTab)
    }

    @Test
    fun `current location unavailable includes the platform message`() = runTest(dispatcher) {
        val config = LocationPickerConfig(locationUnavailableLabel = "unavailable")
        val gateway = FakeLocationPickerGateway(currentLocationResult = LocationResult.Unavailable("gps off"))
        val vm = viewModel(gateway, config)

        vm.onIntent(LocationPickerIntent.TriggerCurrentLocation)
        advanceTimeBy(1000)

        assertEquals("unavailable\ngps off", vm.state.value.locationError)
    }

    @Test
    fun `confirm does nothing when the form is invalid`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway()
        val vm = viewModel(gateway)

        vm.effect.test {
            vm.onIntent(LocationPickerIntent.Confirm)
            expectNoEvents()
        }
    }

    @Test
    fun `confirm sends a Confirmed effect with the trimmed valid form`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway()
        val vm = viewModel(gateway)

        vm.onIntent(LocationPickerIntent.ChangeLabel("  Home  "))
        vm.onIntent(LocationPickerIntent.ChangeLatitude("12.9"))
        vm.onIntent(LocationPickerIntent.ChangeLongitude("77.5"))
        vm.onIntent(LocationPickerIntent.ChangeTimezone("Asia/Kolkata"))

        vm.effect.test {
            vm.onIntent(LocationPickerIntent.Confirm)
            val effect = awaitItem()
            assertTrue(effect is LocationPickerEffect.Confirmed)
            assertEquals("Home", effect.result.label)
            assertEquals(12.9, effect.result.latitude)
            assertEquals(77.5, effect.result.longitude)
            assertEquals("Asia/Kolkata", effect.result.timezoneId)
        }
    }

    @Test
    fun `cancel sends a Cancelled effect`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway()
        val vm = viewModel(gateway)

        vm.effect.test {
            vm.onIntent(LocationPickerIntent.Cancel)
            assertEquals(LocationPickerEffect.Cancelled, awaitItem())
        }
    }

    @Test
    fun `selecting a tab clears any existing location error`() = runTest(dispatcher) {
        val gateway = FakeLocationPickerGateway(currentLocationResult = LocationResult.PermissionDenied)
        val vm = viewModel(gateway)

        vm.onIntent(LocationPickerIntent.TriggerCurrentLocation)
        advanceTimeBy(1000)
        assertTrue(vm.state.value.locationError != null)

        vm.onIntent(LocationPickerIntent.SelectTab(LocationPickerTab.MANUAL))

        assertNull(vm.state.value.locationError)
    }
}
