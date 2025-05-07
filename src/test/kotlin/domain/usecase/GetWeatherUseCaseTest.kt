package org.damascus.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.damascus.domain.repository.WeatherRepository
import org.junit.jupiter.api.Test

class GetWeatherUseCaseTest {

    private val repository = mockk<WeatherRepository>()
    private val useCase = GetWeatherUseCase(repository)

    @Test
    fun `should return WeatherInfo when repository returns successfully`() = runBlocking {
        val fakeWeather = WeatherInfo(
            latitude = 31.9,
            longitude = 35.9,
            elevation = 800.0,
            timezone = "Asia/Amman",
            weather = Weather(
                temperature = 30.0,
                windSpeed = 10.0,
                windDirection = 270,
                isDay = true,
                weatherCode = 0,
                time = "2025-05-07T14:00"
            ),
            units = WeatherUnit("°C", "km/h", "°")
        )

        coEvery { repository.getWeatherByCity("Amman", "Jordan") } returns fakeWeather

        val result = useCase("Amman", "Jordan")

        assertThat(result).isEqualTo(fakeWeather)
    }


}
