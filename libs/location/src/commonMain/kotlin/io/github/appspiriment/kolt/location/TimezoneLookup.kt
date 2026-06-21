package io.github.appspiriment.kolt.location

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val timezoneLookupJson = Json { ignoreUnknownKeys = true }
private val timezoneLookupClient = HttpClient()

@Serializable
private data class TimeApiTimezoneResponse(
    @SerialName("timeZone") val timeZone: String,
)

/**
 * Looks up the IANA timezone ID for a coordinate via timeapi.io (no API key required), e.g.
 * to resolve a timezone after a user drops a pin on a map. Returns null on any failure
 * (network error, rate limit, no coverage at that point) rather than throwing.
 */
suspend fun lookupTimezone(latitude: Double, longitude: Double): String? {
    return try {
        val httpResponse = timezoneLookupClient.get("https://timeapi.io/api/timezone/coordinate") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
        }
        if (!httpResponse.status.isSuccess()) return null
        timezoneLookupJson.decodeFromString<TimeApiTimezoneResponse>(httpResponse.bodyAsText()).timeZone
    } catch (e: SerializationException) {
        null
    } catch (e: Exception) {
        null
    }
}
