import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.domain.exception.WeatherNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WeatherApiClientTest {

    private val json = Json { ignoreUnknownKeys = true }

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

        val mockEngine = MockEngine { request ->
            respond(
                content = if (request.url.toString().contains("geocoding-api")) geoJson else weatherJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
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
        }

        val apiClient = WeatherApiClient(client)

        assertThrows<WeatherNotFoundException> {
            runBlocking {
                apiClient.getWeatherByCity("invalid-city", "invalid-country")
            }
        }
    }
}
