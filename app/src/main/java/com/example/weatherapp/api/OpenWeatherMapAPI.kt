package com.example.weatherapp.api
import java.net.URL

/**
 * OpenWeatherMapAPI is a class responsible for fetching weather data for a given city from the OpenWeatherMap API.
 * It provides a single public function, fetchOpenWeatherMapData, which accepts city name, unit system
 * and API key as parameters and returns the weather data object.
 */
class OpenWeatherMapAPI {
    /**
     * Fetches weather data for a given city from OpenWeatherMap API.
     *
     * @param city The name of the city to fetch weather data for.
     * @param units The unit system to use for the fetched data.
     * @param apiKey The API key to use for accessing the OpenWeatherMap API.
     *
     * @return A WeatherData object that contains the fetched weather data, or null if the API call fails.
     */
    fun fetchOpenWeatherMapData(city: String, units: String, apiKey: String): WeatherData? {
        val weatherData = WeatherData()
            var response = ""
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=${city},us&units=${units}&appid=$apiKey")
                    .readText(Charsets.UTF_8)
            }
            catch(e : Exception) {
                return null
            }
            // create weather data object and call parseResponse to get json response parsed into object
            // /* Extracting JSON returns from the API */
            weatherData.parseWeatherAPIResponse(response)
        return weatherData
    }
}