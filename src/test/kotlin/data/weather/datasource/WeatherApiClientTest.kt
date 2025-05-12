package data.weather.datasource

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationApiClient
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.dto.CurrentWeatherDto
import org.damascus.data.weather.dto.CurrentWeatherUnitsDto
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.exception.LocationNotFoundException
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        val location = LocationDto(
            name = "Cairo",
            region = "Cairo",
            country = "EG",
            latitude = 30.0444,
            longitude = 31.2357
        )
        coEvery { locationDataSource.getCityCoordinates("Cairo", "EG") } returns LocationDto(30.0, 31.0)

        // When
        val result = apiClient.getWeatherByCity(location)

        // Then
        assertEquals(
            expected = LocationDto(
                30.0, 31.0

            ), actual = LocationDto(result.latitude, result.longitude)
        )
    }

    @Test
    fun `should return WeatherDto when IP is valid`() = runTest {
        // Given
        coEvery { locationDataSource.getCurrentLocation() } returns IpLocationDto(30.0, 31.0)

        // When
        val result = apiClient.getWeatherByIp()

        // Then
        assertEquals(
            expected = LocationDto(
                30.0, 31.0

            ), actual = LocationDto(result.latitude, result.longitude)
        )
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

    @Test
    fun `should serialize and deserialize CurrentWeather`() {
        // Given
        val weather = CurrentWeatherDto(
            time = "2025-05-05T12:00",
            interval = 1,
            temperature = 26.0,
            windSpeed = 12.0,
            windDirection = 90,
            isDay = 1,
            weatherCode = 0
        )

        // When
        val jsonString = json.encodeToString(weather)
        val deserialized = json.decodeFromString<CurrentWeatherDto>(jsonString)

        // Then
        assertEquals(weather, deserialized)
    }

    @Test
    fun `should serialize and deserialize CurrentWeatherUnits`() {
        // Given
        val units = CurrentWeatherUnitsDto(
            time = "iso8601",
            interval = "int",
            temperature = "°C",
            windSpeed = "km/h",
            windDirection = "°",
            isDay = "bool",
            weatherCode = "int"
        )

        // When
        val jsonString = json.encodeToString(units)
        val deserialized = json.decodeFromString<CurrentWeatherUnitsDto>(jsonString)

        // Then
        assertEquals(units, deserialized)
    }

    @Test
    fun `should serialize and deserialize WeatherDto`() {
        // Given
        val dto = WeatherDto(
            latitude = 30.0,
            longitude = 31.0,
            generationTimeMs = 12.3,
            utcOffsetSeconds = 7200,
            timezone = "Africa/Cairo",
            timezoneAbbreviation = "EET",
            elevation = 10.0,
            currentWeatherUnitsDto = CurrentWeatherUnitsDto(
                time = "iso8601",
                interval = "int",
                temperature = "°C",
                windSpeed = "km/h",
                windDirection = "°",
                isDay = "bool",
                weatherCode = "int"
            ),
            currentWeatherDto = CurrentWeatherDto(
                time = "2025-05-05T12:00",
                interval = 1,
                temperature = 26.0,
                windSpeed = 12.0,
                windDirection = 90,
                isDay = 1,
                weatherCode = 0
            )
        )

        // When
        val jsonString = json.encodeToString(dto)
        val deserialized = json.decodeFromString<WeatherDto>(jsonString)

        // Then
        assertEquals(dto, deserialized)
    }

    @Test
    fun `should return WeatherDto when real IP location is returned`() = runTest {
        // Given
        val locationClient = createMockClient("""{ "lat": 30.0, "lon": 31.0 }""")
        val weatherClient = createMockClient(dummyWeatherJson)
        val locationDataSource = LocationApiClient(locationClient)
        val apiClient = WeatherApiClient(weatherClient, locationDataSource)

        // When
        val result = apiClient.getWeatherByIp()

        // Then
        assertEquals(LocationDto(30.0, 31.0), LocationDto(result.latitude, result.longitude))
    }

    private fun createMockClient(responseJson: String): HttpClient {
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
