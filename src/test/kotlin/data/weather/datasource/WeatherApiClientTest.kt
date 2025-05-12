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
import org.damascus.data.weather.dto.CurrentWeatherDto
import org.damascus.data.weather.dto.CurrentWeatherUnitsDto
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.toDomain
import org.damascus.data.weather.mapper.toDto
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.WeatherInfo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WeatherApiClientTest {

// setup is bellow

    @Test
    fun `getWeatherByIp should return WeatherDto when location found and cache empty`() = runTest {
        mockLocationFound()
        mockCacheMiss()

        val result = apiClient.getWeatherByIp()

        assertEquals(dummyWeatherDto, result)
        coVerify { locationDataSource.getCurrentLocation() }
        coVerify { cacheManager.readCache(ipCacheKey) }
        coVerify { cacheManager.writeCache(ipCacheKey, dummyWeatherDto.toDomain()) }
        verifyApiRequest(expectedLatitude = "30.0", expectedLongitude = "31.0")
    }
    @Test
    fun `getWeatherByIp should return cached WeatherDto when cache hit`() = runTest {
        val cachedWeatherDomain = dummyWeatherDto.copy(timezone = "Cached/Timezone").toDomain()
        mockLocationFound()
        mockCacheHit(data = cachedWeatherDomain)

        val result = apiClient.getWeatherByIp()

        assertEquals(cachedWeatherDomain.toDto(), result)
        coVerify { locationDataSource.getCurrentLocation() }
        coVerify { cacheManager.readCache(ipCacheKey) }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
        assertTrue(!capturedRequest.isCaptured, "API should not have been called on cache hit")
    }
    @Test
    fun `getWeatherByIp should throw LocationNotFoundException when location service returns null`() = runTest {
        mockLocationNotFound()

        val exception = assertFailsWith<LocationNotFoundException> {
            apiClient.getWeatherByIp()
        }

        assertThat(exception.message).isEqualTo("Could not determine location from IP")
        coVerify { locationDataSource.getCurrentLocation() }
        coVerify(exactly = 0) { cacheManager.readCache(any()) }
        assertTrue(!capturedRequest.isCaptured, "API should not have been called")
    }






    // old setup

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var mockEngine: MockEngine
    private lateinit var locationDataSource: LocationDataSource
    private lateinit var apiClient: WeatherApiClient
    private lateinit var cacheManager: WeatherCacheManager
    private val capturedRequest = slot<HttpRequestData>()

    private val dummyIpLocationDto = IpLocationDto(latitude = 30.0, longitude = 31.0)
    private val dummyWeatherDto = WeatherDto(
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
    private val dummyWeatherJson = json.encodeToString(dummyWeatherDto)
    private val ipCacheKey = "ip:${dummyIpLocationDto.latitude},${dummyIpLocationDto.longitude}"
    private val defaultHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun createTestClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
        mockEngine = MockEngine { request ->
            capturedRequest.clear()
            if (!capturedRequest.isCaptured) capturedRequest.captured = request
            handler(this, request)
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    private fun setupDefaultClientAndApi(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
            respond(dummyWeatherJson, HttpStatusCode.OK, defaultHeaders)
        }
    ) {
        val client = createTestClient(handler)
        apiClient = WeatherApiClient(client, locationDataSource, cacheManager)
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

    private fun mockLocationFound(location: IpLocationDto = dummyIpLocationDto) {
        coEvery { locationDataSource.getCurrentLocation() } returns location
    }

    private fun mockLocationNotFound() {
        coEvery { locationDataSource.getCurrentLocation() } returns null
    }

    private fun mockCacheMiss(key: String = ipCacheKey) {
        coEvery { cacheManager.readCache(key) } returns null
    }

    private fun mockCacheHit(key: String = ipCacheKey, data: WeatherInfo = dummyWeatherDto.toDomain()) {
        coEvery { cacheManager.readCache(key) } returns data
    }

    @BeforeEach
    fun setup() {
        locationDataSource = mockk()
        cacheManager = mockk(relaxed = true)
        capturedRequest.clear()
        setupDefaultClientAndApi()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}