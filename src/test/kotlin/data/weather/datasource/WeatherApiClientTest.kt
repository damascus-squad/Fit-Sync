package data.weather.datasource

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.datasource.WeatherCacheManager
import org.damascus.data.weather.dto.*
import org.damascus.data.weather.mapper.toDomain
import org.damascus.data.weather.mapper.toDto
import org.damascus.domain.exception.LocationNotFoundException
import kotlin.test.*

class WeatherApiClientTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val capturedRequest = slot<HttpRequestData>()

    private lateinit var mockEngine: MockEngine
    private lateinit var locationDataSource: LocationDataSource
    private lateinit var cacheManager: WeatherCacheManager
    private lateinit var apiClient: WeatherApiClient

    private val dummyIpLocationDto = IpLocationDto(latitude = 30.0, longitude = 31.0)
    private val dummyLocationDto = LocationDto(latitude = 35.0, longitude = 45.0)
    private val dummyWeatherDto = WeatherDto(
        latitude = 35.0,
        longitude = 45.0,
        generationTimeMs = 12.3,
        utcOffsetSeconds = 7200,
        timezone = "Africa/Cairo",
        timezoneAbbreviation = "EET",
        elevation = 10.0,
        currentWeatherUnitsDto = CurrentWeatherUnitsDto(
            time = "iso8601", interval = "int", temperature = "°C",
            windSpeed = "km/h", windDirection = "°", isDay = "bool", weatherCode = "int"
        ),
        currentWeatherDto = CurrentWeatherDto(
            time = "2025-05-05T12:00", interval = 1, temperature = 26.0,
            windSpeed = 12.0, windDirection = 90, isDay = 1, weatherCode = 0
        )
    )
    private val dummyWeatherJson = json.encodeToString(dummyWeatherDto)
    private val ipCacheKey = "ip:${dummyIpLocationDto.latitude},${dummyIpLocationDto.longitude}"
    private val defaultHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @BeforeTest
    fun setup() {
        locationDataSource = mockk()
        cacheManager = mockk(relaxed = true)
        capturedRequest.clear()
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    private fun setupClientAndApi(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): WeatherApiClient {
        mockEngine = MockEngine { request ->
            if (!capturedRequest.isCaptured) capturedRequest.captured = request
            handler(this, request)
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        return WeatherApiClient(client, locationDataSource, cacheManager)
    }

    @Test
    fun `getWeatherByIp should return WeatherDto when location found and cache empty`() = runTest {
        coEvery { locationDataSource.getCurrentLocation() } returns dummyIpLocationDto
        coEvery { cacheManager.readCache(ipCacheKey) } returns null

        apiClient = setupClientAndApi {
            respond(dummyWeatherJson, HttpStatusCode.OK, defaultHeaders)
        }

        val result = apiClient.getWeatherByIp()

        assertEquals(dummyWeatherDto, result)
        coVerify { locationDataSource.getCurrentLocation() }
        coVerify { cacheManager.readCache(ipCacheKey) }
        coVerify { cacheManager.writeCache(ipCacheKey, dummyWeatherDto.toDomain()) }
        verifyApiRequest("30.0", "31.0")
    }

    @Test
    fun `getWeatherByIp should return cached WeatherDto when cache hit`() = runTest {
        val cached = dummyWeatherDto.copy(timezone = "Cached/Timezone").toDomain()

        coEvery { locationDataSource.getCurrentLocation() } returns dummyIpLocationDto
        coEvery { cacheManager.readCache(ipCacheKey) } returns cached

        apiClient = setupClientAndApi {
            error("API should not be called on cache hit")
        }

        val result = apiClient.getWeatherByIp()

        assertEquals(cached.toDto(), result)
        coVerify { locationDataSource.getCurrentLocation() }
        coVerify { cacheManager.readCache(ipCacheKey) }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
    }

    @Test
    fun `getWeatherByIp should throw LocationNotFoundException when location is null`() = runTest {
        coEvery { locationDataSource.getCurrentLocation() } returns null

        apiClient = setupClientAndApi {
            error("API should not be called if location not found")
        }

        val exception = assertFailsWith<LocationNotFoundException> {
            apiClient.getWeatherByIp()
        }

        assertThat(exception.message).isEqualTo("Could not determine location from IP")
        coVerify { locationDataSource.getCurrentLocation() }
        coVerify(exactly = 0) { cacheManager.readCache(any()) }
    }

    @Test
    fun `getWeatherByCity should call weather API and return WeatherDto`() = runTest {
        apiClient = setupClientAndApi {
            respond(dummyWeatherJson, HttpStatusCode.OK, defaultHeaders)
        }

        val result = apiClient.getWeatherByCity(dummyLocationDto)

        assertEquals(dummyWeatherDto, result)
        verifyApiRequest("35.0", "45.0")
    }

    @Test
    fun `getWeatherByCity should call API with correct URL and parameters`() = runTest {
        apiClient = setupClientAndApi {
            respond(dummyWeatherJson, HttpStatusCode.OK, defaultHeaders)
        }

        apiClient.getWeatherByCity(dummyLocationDto)

        verifyApiRequest("35.0", "45.0")
    }

    private fun verifyApiRequest(
        expectedLatitude: String,
        expectedLongitude: String,
        expectedMethod: HttpMethod = HttpMethod.Get,
        expectedHost: String = "api.open-meteo.com",
        expectedPath: String = "/v1/forecast",
        expectCurrentWeather: Boolean = true,
        expectTimezoneAuto: Boolean = true
    ) {
        assertTrue(capturedRequest.isCaptured, "API request was not captured.")
        val request = capturedRequest.captured
        assertEquals(expectedMethod, request.method)
        assertEquals(expectedHost, request.url.host)
        assertEquals(expectedPath, request.url.encodedPath)
        assertEquals(expectedLatitude, request.url.parameters["latitude"])
        assertEquals(expectedLongitude, request.url.parameters["longitude"])
        assertEquals(expectCurrentWeather.toString(), request.url.parameters["current_weather"])
        assertEquals(if (expectTimezoneAuto) "auto" else null, request.url.parameters["timezone"])
    }
}
