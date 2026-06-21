package io.github.appspiriment.kolt.location

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** A single place match from [searchPlaces], ready to prefill a location form. */
data class PlaceSearchResult(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

private val placeSearchJson = Json { ignoreUnknownKeys = true }
private val placeSearchClient = HttpClient()

@Serializable
private data class NominatimResult(
    val lat: String,
    val lon: String,
    @SerialName("display_name") val displayName: String,
)

/**
 * Free-text place search via OpenStreetMap's Nominatim geocoding API (no API key required).
 * Returns an empty list on any failure (network error, rate limit, no matches) rather than
 * throwing — callers should treat an empty result as "no matches", not an error.
 *
 * Nominatim's usage policy requires a descriptive User-Agent and caps requests at ~1/sec;
 * callers should debounce input rather than searching on every keystroke.
 */
suspend fun searchPlaces(query: String, limit: Int = 8): List<PlaceSearchResult> {
    if (query.isBlank()) return emptyList()
    return try {
        val httpResponse = placeSearchClient.get("https://nominatim.openstreetmap.org/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", limit)
            header("User-Agent", "Kolt-Location-Lib/1.0")
        }
        if (!httpResponse.status.isSuccess()) return emptyList()
        val results = placeSearchJson.decodeFromString<List<NominatimResult>>(httpResponse.bodyAsText())
        results.mapNotNull { result ->
            val lat = result.lat.toDoubleOrNull()
            val lon = result.lon.toDoubleOrNull()
            if (lat == null || lon == null) null
            else PlaceSearchResult(label = result.displayName, latitude = lat, longitude = lon)
        }
    } catch (e: SerializationException) {
        emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Reverse geocoding via Nominatim: looks up the place name for a given coordinate, e.g. to
 * prefill a label after a user drops a pin on a map. Returns null on any failure (network
 * error, rate limit, no match at that point) rather than throwing.
 *
 * Same usage-policy constraints as [searchPlaces]: descriptive User-Agent, ~1 request/sec cap.
 */
suspend fun reverseGeocode(latitude: Double, longitude: Double): PlaceSearchResult? {
    return try {
        val httpResponse = placeSearchClient.get("https://nominatim.openstreetmap.org/reverse") {
            parameter("lat", latitude)
            parameter("lon", longitude)
            parameter("format", "json")
            header("User-Agent", "Kolt-Location-Lib/1.0")
        }
        if (!httpResponse.status.isSuccess()) return null
        val result = placeSearchJson.decodeFromString<NominatimResult>(httpResponse.bodyAsText())
        val lat = result.lat.toDoubleOrNull() ?: return null
        val lon = result.lon.toDoubleOrNull() ?: return null
        PlaceSearchResult(label = result.displayName, latitude = lat, longitude = lon)
    } catch (e: SerializationException) {
        null
    } catch (e: Exception) {
        null
    }
}
