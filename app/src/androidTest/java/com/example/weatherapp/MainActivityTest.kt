import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.api.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testCityIsSet() {
        val activityScenario = activityScenarioRule.scenario
        activityScenario.onActivity {
            it.CITY = "palo alto,ca"
            assertEquals("palo alto,ca", it.CITY)
        }
    }

    @Test
    fun testApiIsSet() {
        val activityScenario = activityScenarioRule.scenario
        activityScenario.onActivity {
            val api = it.API
            assertEquals("3ddaaed47ca3b917cf6be85e7a15e16b", api)
        }
    }

    @Test
    fun testUnitsIsSet() {
        val activityScenario = activityScenarioRule.scenario
        activityScenario.onActivity {
            val units = it.UNITS
            assertEquals("metric", units)
        }
    }

    @Test
    fun testWeatherDataIsLoaded() {
        val activityScenario = activityScenarioRule.scenario
        activityScenario.onActivity {
            val weatherData = it.mOpenWeatherMapAPI?.fetchOpenWeatherMapData(
                "Palo Alto,CA",
                "metric",
                "3ddaaed47ca3b917cf6be85e7a15e16b"
            )
            assertNotNull(weatherData)
        }
    }

    @Test
    fun testWeatherDataUpdatesUI() {
        val activityScenario = activityScenarioRule.scenario
        activityScenario.onActivity {
            val weatherData = WeatherData()
            weatherData.address = "Palo Alto, CA"
            weatherData.updatedAtText = "2023-04-23 11:00:00"
            weatherData.weatherDescription = "scattered clouds"
            weatherData.temp = "17"
            weatherData.tempMin = "10"
            weatherData.tempMax = "20"
            weatherData.sunrise = 1650867640L
            weatherData.sunset = 1650911222L
            weatherData.windSpeed = "3"
            weatherData.pressure = "1016"
            weatherData.humidity = "75"
            weatherData.feelsLike = "16"
            weatherData.weatherIcon = "03d"
            it.updateUI(weatherData)
            onView(withId(R.id.address)).check(matches(withText("Palo Alto, CA")))
            onView(withId(R.id.updated_at)).check(matches(withText("2023-04-23 11:00:00")))
            onView(withId(R.id.status)).check(matches(withText("scattered clouds")))
            onView(withId(R.id.temp)).check(matches(withText("17")))
            onView(withId(R.id.temp_min)).check(matches(withText("10")))
            onView(withId(R.id.temp_max)).check(matches(withText("20")))
            onView(withId(R.id.sunrise)).check(matches(withText("06:20 AM")))
            onView(withId(R.id.sunset)).check(matches(withText("07:20 PM")))
            onView(withId(R.id.wind)).check(matches(withText("3")))
            onView(withId(R.id.pressure)).check(matches(withText("1016")))
            onView(withId(R.id.humidity)).check(matches(withText("75")))
            onView(withId(R.id.feels_like)).check(matches(withText("16")))
        }
    }
}
