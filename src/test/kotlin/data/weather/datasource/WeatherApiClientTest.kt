package data.weather.datasource

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.exception.LocationNotFoundException
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class WeatherApiClientTest {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var client: HttpClient
    private lateinit var locationDataSource: LocationDataSource
    private lateinit var apiClient: WeatherApiClient

    private val dummyWeatherJson = """
        {
            "latitude": 30.0,
            "longitude": 31.0,
            "generationtime_ms": 12.3,
            "utc_offset_seconds": 7200,
            "timezone": "Africa/Cairo",
            "timezone_abbreviation": "EET",
            "elevation": 10.0,
            "current_weather_units": {
                "time": "iso8601",
                "interval": "int",
                "temperature": "°C",
                "windspeed": "km/h",
                "winddirection": "°",
                "is_day": "bool",
                "weathercode": "int"
            },
            "current_weather": {
                "temperature": 26.0,
                "windspeed": 12.0,
                "time": "2025-05-05T12:00",
                "interval": 1,
                "winddirection": 90,
                "is_day": 1,
                "weathercode": 0
            }
        }
    """.trimIndent()

    @BeforeEach
    fun setup() {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            engine {
                addHandler {
                    respond(
                        content = dummyWeatherJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json")
                    )
                }
            }
        }

        locationDataSource = mockk()
        apiClient = WeatherApiClient(client, locationDataSource)
    }

    @Test
    fun `should return WeatherDto when city is valid`() = runTest {
        // Given
        coEvery { locationDataSource.getCityCoordinates("Cairo", "EG") } returns LocationDto(30.0, 31.0)

        // When
        val result = apiClient.getWeatherByCity("Cairo", "EG")

        // Then
        assertEquals(30.0, result.latitude)
        assertEquals(26.0, result.currentWeather.temperature)
    }

    @Test
    fun `should throw exception when city is invalid`() = runTest {
        // Given
        coEvery { locationDataSource.getCityCoordinates("UnknownCity", "XX") } returns null

        // When
        val exception = assertFailsWith<LocationNotFoundException> {
            apiClient.getWeatherByCity("UnknownCity", "XX")
        }

        // Then
        assertThat(exception.message).isEqualTo("City not found: UnknownCity, XX")
    }

    @Test
    fun `should return WeatherDto when IP is valid`() = runTest {
        // Given
        coEvery { locationDataSource.getCurrentLocation() } returns IpLocationDto(30.0, 31.0)

        // When
        val result = apiClient.getWeatherByIp()

        // Then
        assertEquals(31.0, result.longitude)
        assertEquals("Africa/Cairo", result.timezone)
    }

    @Test
    fun `should throw exception when IP is invalid`() = runTest {
        // Given
        coEvery { locationDataSource.getCurrentLocation() } returns null

        // When
        val exception = assertFailsWith<LocationNotFoundException> {
            apiClient.getWeatherByIp()
        }

        // Then
        assertThat(exception.message).isEqualTo("Could not determine location from IP")
    }
}
