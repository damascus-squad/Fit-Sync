package data.weather.datasource

import org.junit.jupiter.api.Assertions.*
import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.dto.AutoLocationDto
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.domain.exception.WeatherNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.damascus.data.weather.dto.*
import org.damascus.domain.exception.LocationNotFoundException
import kotlin.test.*

class WeatherApiClientTest {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var client: HttpClient
    private lateinit var locationDataSource: LocationDataSource
    private lateinit var apiClient: WeatherApiClient

    @Test
    fun `should return WeatherDto when city and country are valid`() = runBlocking {
        val geoJson = """{"results": [{"latitude": 31.9, "longitude": 35.9}]}"""
        val weatherJson = """
            {
                "latitude": 31.9,
                "longitude": 35.9,
                "elevation": 800.0,
                "timezone": "Asia/Amman",
                "generationtime_ms": 0.0,
                "utc_offset_seconds": 10800,
                "timezone_abbreviation": "GMT+3",
                "current_weather": {
                    "time": "2025-05-07T14:00",
                    "interval": 900,
                    "temperature": 30.0,
                    "windspeed": 10.0,
                    "winddirection": 270,
                    "is_day": 1,
                    "weathercode": 0
                },
                "current_weather_units": {
                    "time": "iso8601",
                    "interval": "seconds",
                    "temperature": "°C",
                    "windspeed": "km/h",
                    "winddirection": "°",
                    "is_day": "",
                    "weathercode": "wmo code"
                }
            }
        """.trimIndent()
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

        val mockEngine = MockEngine { request ->
            respond(
                content = if (request.url.toString().contains("geocoding-api")) geoJson else weatherJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
    @BeforeTest
    fun setup() {
        client = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
                json(json)
            }
            engine {
                addHandler { request ->
                    respond(
                        content = dummyWeatherJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json")
                    )
                }
            }
        }
    }

    @Test
    fun `should return WeatherDto when city is valid`() = runTest {
        locationDataSource = object : LocationDataSource {
            override suspend fun getCityCoordinates(city: String, country: String) = LocationDto(30.0, 31.0)
            override suspend fun getCurrentLocation() = null
        }

        apiClient = WeatherApiClient(client, locationDataSource)
        }

        val apiClient = WeatherApiClient(client)

        val result = apiClient.getWeatherByCity("Amman", "Jordan")

        assertThat(result.latitude).isEqualTo(31.9)
        assertThat(result.currentWeather.temperature).isEqualTo(30.0)
    }

    @Test
    fun `should throw WeatherNotFoundException when city and country are invalid`() {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"results": []}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        val result = apiClient.getWeatherByCity("Cairo", "EG")
        assertEquals(30.0, result.latitude)
        assertEquals(26.0, result.currentWeather.temperature)
    }

    @Test
    fun `getWeatherByCity should throw LocationNotFoundException when city is invalid`() = runTest {
        locationDataSource = object : LocationDataSource {
            override suspend fun getCityCoordinates(city: String, country: String) = null
            override suspend fun getCurrentLocation() = null
        }

        val apiClient = WeatherApiClient(client)
        apiClient = WeatherApiClient(client, locationDataSource)

        assertThrows<WeatherNotFoundException> {
            runBlocking {
                apiClient.getWeatherByCity("invalid-city", "invalid-country")
            }
        assertFailsWith<LocationNotFoundException> {
            apiClient.getWeatherByCity("UnknownCity", "XX")
        }
    }

    @Test
    fun `should return WeatherDto when IP location is valid`() = runTest {
        locationDataSource = object : LocationDataSource {
            override suspend fun getCityCoordinates(city: String, country: String) = null
            override suspend fun getCurrentLocation() = AutoLocationDto(lat = 30.0, lon = 31.0)
        }

        apiClient = WeatherApiClient(client, locationDataSource)

        val result = apiClient.getWeatherByIp()
        assertEquals(31.0, result.longitude)
        assertEquals("Africa/Cairo", result.timezone)
    }

    @Test
    fun `should throw LocationNotFoundException when IP fails`() = runTest {
        locationDataSource = object : LocationDataSource {
            override suspend fun getCityCoordinates(city: String, country: String) = null
            override suspend fun getCurrentLocation() = null
        }

        apiClient = WeatherApiClient(client, locationDataSource)

        assertFailsWith<LocationNotFoundException> {
            apiClient.getWeatherByIp()
        }
    }
}