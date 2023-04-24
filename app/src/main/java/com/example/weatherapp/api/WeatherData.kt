package com.example.weatherapp.api

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class represents the weather data retrieved from an API response.
 * The class provides properties for storing various weather-related information.
 * The class includes the following properties:
 *
 * @property updatedAtText A string representing the last time the weather data was updated.
 * @property temp A string representing the current temperature in Celsius.
 * @property tempMin A string representing the minimum temperature in Celsius.
 * @property tempMax A string representing the maximum temperature in Celsius.
 * @property pressure A string representing the current atmospheric pressure in inHg.
 * @property humidity A string representing the current humidity level in percent.
 * @property feelsLike A string representing the current feels-like temperature in Celsius.
 * @property sunrise A long representing the time of sunrise.
 * @property sunset A long representing the time of sunset.
 * @property windSpeed A string representing the current wind speed in mph.
 * @property weatherDescription A string representing the current weather description.
 * @property weatherIcon A string representing the icon of the current weather.
 * @property address A string representing the name of the location and its country code.
 */
class WeatherData {
    lateinit var updatedAtText: String
    lateinit var temp : String
    lateinit var tempMin: String
    lateinit var tempMax : String
    lateinit var pressure : String
    lateinit var humidity : String
    lateinit var feelsLike : String
    var sunrise:Long = 0
    var sunset:Long = 0
    lateinit var windSpeed  : String
    lateinit var weatherDescription : String
    lateinit var weatherIcon : String
    lateinit var address : String

    /**
     * Parses the weather API response JSON and updates the values in the WeatherData object.
     *
     * @param response The JSON string response from the weather API.
     *
     * This function takes in a JSON string response from the weather API and updates the corresponding
     * fields in the WeatherData object with the parsed data.
     * The response is first converted to a JSONObject, and then various data points are extracted
     * from it using JSONObject methods.
     * The parsed data is then used to update the properties of the WeatherData object.
     * This function throws a JSONException if there is an error parsing the JSON data.
     */
    fun parseWeatherAPIResponse(response: String) {

        val jsonObj = JSONObject(response)
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val wind = jsonObj.getJSONObject("wind")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

        val updatedAt:Long = jsonObj.getLong("dt")
        updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
            Date(updatedAt*1000)
        )
        temp = String.format("%.0f",main.getString("temp").toFloat())+"°C"
        tempMin = "Min Temp: " + String.format("%.0f",main.getString("temp_min").toFloat()) +"°C"
        tempMax = "Max Temp: " + String.format("%.0f",main.getString("temp_max").toFloat()) +"°C"
        pressure = main.getString("pressure") +" inHg"
        humidity = main.getString("humidity") +"%"
        feelsLike = String.format("%.0f",main.getString("feels_like").toFloat()) + "°C"

        sunrise = sys.getLong("sunrise")
        sunset = sys.getLong("sunset")
        windSpeed = wind.getString("speed") + "mph"
        weatherDescription = weather.getString("description")
        weatherIcon = weather.getString("icon")

        address = jsonObj.getString("name")+", "+sys.getString("country")

    }
}