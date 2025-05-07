package data.location.dataSource

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationApiClient
import org.junit.jupiter.api.Test
import kotlin.test.*

class LocationApiClientTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should return location when city is valid`() = runTest {
        val responseJson = """
            {
                "results": [
                    { "latitude": 30.0, "longitude": 31.0 }
                ]
            }
        """.trimIndent()

        val api = LocationApiClient(mockHttpClient(responseJson))
        val result = api.getCityCoordinates("Cairo", "EG")

        assertNotNull(result)
        assertEquals(30.0, result.latitude)
        assertEquals(31.0, result.longitude)
    }

    @Test
    fun `should return null when city is not found`() = runTest {
        val responseJson = """{ "results": [] }"""
        val api = LocationApiClient(mockHttpClient(responseJson))

        val result = api.getCityCoordinates("Unknown", "ZZ")

        assertNull(result)
    }

    @Test
    fun `should return location from IP lookup`() = runTest {
        val ipJson = """{ "lat": 30.0, "lon": 31.0 }"""
        val api = LocationApiClient(mockHttpClient(ipJson))

        val result = api.getCurrentLocation()

        assertNotNull(result)
        assertEquals(30.0, result.lat)
        assertEquals(31.0, result.lon)
    }

    @Test
    fun `should throw exception on invalid IP JSON`() = runTest {
        val invalidIpJson = """{ "unexpected": "value" }"""
        val api = LocationApiClient(mockHttpClient(invalidIpJson))

        assertFailsWith<Exception> {
            api.getCurrentLocation()
        }
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
