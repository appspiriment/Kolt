package io.github.appspiriment.kolt.location

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

actual class PlatformLocationContext

private val ipGeoJson = Json { ignoreUnknownKeys = true }

private val ipGeoClient = HttpClient(CIO)

@Serializable
private data class IpApiCoResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val region: String? = null,
    val timezone: String? = null,
    val error: Boolean = false,
    val reason: String? = null,
)

@Serializable
private data class IpWhoIsResponse(
    val success: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val region: String? = null,
    val timezone: IpWhoIsTimezone? = null,
    val message: String? = null,
)

@Serializable
private data class IpWhoIsTimezone(val id: String? = null)

@Serializable
private data class IpApiComResponse(
    val status: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val city: String? = null,
    val regionName: String? = null,
    val timezone: String? = null,
    val message: String? = null,
)

/** One best-effort attempt against a single IP-geolocation provider. Returns null if this provider couldn't resolve a location, so the caller can fall through to the next one. */
private fun interface IpGeoProvider {
    suspend fun fetch(): GeoLocation?
}

private suspend inline fun <reified T> fetchJson(url: String): T? {
    val httpResponse = ipGeoClient.get(url)
    if (!httpResponse.status.isSuccess()) return null
    return try {
        ipGeoJson.decodeFromString<T>(httpResponse.bodyAsText())
    } catch (e: SerializationException) {
        null
    }
}

private val ipGeoProviders: List<IpGeoProvider> = listOf(
    IpGeoProvider {
        val response = fetchJson<IpApiCoResponse>("https://ipapi.co/json/") ?: return@IpGeoProvider null
        val lat = response.latitude
        val lon = response.longitude
        if (response.error || lat == null || lon == null) null
        else GeoLocation(latitude = lat, longitude = lon, label = response.city ?: response.region, timezoneId = response.timezone)
    },
    IpGeoProvider {
        val response = fetchJson<IpWhoIsResponse>("https://ipwho.is/") ?: return@IpGeoProvider null
        val lat = response.latitude
        val lon = response.longitude
        if (!response.success || lat == null || lon == null) null
        else GeoLocation(latitude = lat, longitude = lon, label = response.city ?: response.region, timezoneId = response.timezone?.id)
    },
    IpGeoProvider {
        val response = fetchJson<IpApiComResponse>("http://ip-api.com/json/") ?: return@IpGeoProvider null
        val lat = response.lat
        val lon = response.lon
        if (response.status != "success" || lat == null || lon == null) null
        else GeoLocation(latitude = lat, longitude = lon, label = response.city ?: response.regionName, timezoneId = response.timezone)
    },
)

/**
 * Desktop has no GPS hardware and no OS-level location permission. This always
 * performs a best-effort IP-based lookup — no consent prompt is shown here;
 * callers should ask the user before invoking this if desired.
 *
 * Tries each provider in [ipGeoProviders] in turn (ipapi.co, ipwho.is, ip-api.com) since
 * these free-tier services are rate-limited and occasionally return errors or block pages.
 */
actual class CurrentLocationProvider actual constructor(
    private val context: PlatformLocationContext
) {
    actual suspend fun getCurrentLocation(): LocationResult {
        for (provider in ipGeoProviders) {
            val location = try {
                provider.fetch()
            } catch (e: Exception) {
                null
            }
            if (location != null) return LocationResult.Success(location)
        }
        return LocationResult.Unavailable("Could not determine location from IP address")
    }
}
