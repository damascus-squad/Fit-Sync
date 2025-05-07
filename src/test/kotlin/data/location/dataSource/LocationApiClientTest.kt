package data.location.dataSource

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationApiClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocationApiClientTest {

    private lateinit var client: HttpClient
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should return location when city is valid`() = runTest {
        // Given
        val responseJson = """
            {
                "results": [
                    { "latitude": 30.0, "longitude": 31.0 }
                ]
            }
        """.trimIndent()

        client = mockHttpClient(responseJson)
        val api = LocationApiClient(client, json)

        // When
        val result = api.getCityCoordinates("Cairo", "EG")

        // Then
        assertNotNull(result)
        assertEquals(30.0, result.latitude)
        assertEquals(31.0, result.longitude)
    }

    @Test
    fun `should return null when city not found`() = runTest {
        // Given
        val responseJson = """{ "results": [] }"""
        client = mockHttpClient(responseJson)
        val api = LocationApiClient(client, json)

        // When
        val result = api.getCityCoordinates("UnknownCity", "ZZ")

        // Then
        assertNull(result)
    }

    @Test
    fun `should return location when IP lookup succeeds`() = runTest {
        // Given
        val ipJson = """{ "lat": 30.0, "lon": 31.0 }"""
        client = mockHttpClient(ipJson)
        val api = LocationApiClient(client, json)

        // When
        val result = api.getCurrentLocation()

        // Then
        assertNotNull(result)
        assertEquals(30.0, result.lat)
        assertEquals(31.0, result.lon)
    }

    private fun mockHttpClient(responseJson: String): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json")
                    )
                }
            }
        }
    }
}
