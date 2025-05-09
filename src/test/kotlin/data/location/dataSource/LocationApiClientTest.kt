package data.location.dataSource

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationApiClient
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.dto.LocationDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocationApiClientTest {

    private lateinit var json: Json
    private lateinit var client: HttpClient
    private lateinit var api: LocationApiClient

    @BeforeEach
    fun setup() {
        json = Json { ignoreUnknownKeys = true }
    }

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
        api = LocationApiClient(client)

        // When
        val result = api.getCityCoordinates("Cairo", "EG")

        // Then
        assertNotNull(result)
        assertEquals(
            expected = LocationDto(30.0, 31.0),
            actual = LocationDto(result.latitude, result.longitude)
        )
    }

    @Test
    fun `should return null when city is not found`() = runTest {
        // Given
        val responseJson = """{ "results": [] }"""
        val api = LocationApiClient(mockHttpClient(responseJson))

        // When
        val result = api.getCityCoordinates("Unknown", "ZZ")

        // Then
        assertNull(result)
    }

    @Test
    fun `should return location from IP lookup`() = runTest {
        // Given
        val ipJson = """{ "lat": 30.0, "lon": 31.0 }"""
        val api = LocationApiClient(mockHttpClient(ipJson))

        // When
        val result = api.getCurrentLocation()

        // Then
        assertNotNull(result)
        assertEquals(
            expected = IpLocationDto(30.0, 31.0),
            actual = IpLocationDto(result.latitude, result.longitude)
        )
    }

    @Test
    fun `should throw exception on invalid IP JSON`() = runTest {
        // Given
        val invalidIpJson = """{ "unexpected": "value" }"""
        val api = LocationApiClient(mockHttpClient(invalidIpJson))

        // When/Then
        assertFailsWith<Exception> {
            api.getCurrentLocation()
        }
    }

    @Test
    fun `should serialize and deserialize LocationDto`() {
        // Given
        val location = LocationDto(30.0, 31.0)

        // When
        val jsonString = json.encodeToString(location)
        val deserialized = json.decodeFromString<LocationDto>(jsonString)

        // Then
        assertEquals(location, deserialized)
    }

    private fun mockHttpClient(responseJson: String): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            engine {
                addHandler {
                    respond(
                        content = responseJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
        }
    }
}
