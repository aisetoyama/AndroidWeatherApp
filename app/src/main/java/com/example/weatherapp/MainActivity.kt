package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherapp.api.OpenWeatherMapAPI
import com.example.weatherapp.api.WeatherData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * This class is the main activity of the application which extends the AppCompat Activity. It
 * includes various methods and variables to display the weather information of a city.
 *
 * @property CITY a string variable used to set the default city for the weather information.
 * @property API a string variable used to set the API key for accessing the OpenWeatherMap API.
 * @property UNITS a string variable used to set the unit for temperature measurement.
 * @property mOpenWeatherMapAPI an object of OpenWeatherMapAPI class used to access the API data.
 * @property fusedLocationProviderClient an object of FusedLocationProviderClient class used to get current location.
 *
 * This activity contains the following methods:
 * onCreate() a method that is called when the activity is created.
 * getWeather() a method that fetches the weather data from the OpenWeatherMap API.
 * updateUI() a method that updates the UI with the weather data.
 * updateWeatherIcon() a method that updates the weather icon.
 * getSearchLocation() a method that searches the weather information for a location entered by the user.
 * saveData() a method that saves the last searched location in shared preferences.
 * loadData() a method that loads the last searched location from shared preferences.
 * getCurrentLocation() a method that gets the current location of the device using FusedLocationProviderClient.
 * isLocationEnabled() a method that checks if the location of the device is enabled.
 */
class MainActivity : AppCompatActivity() {

    // Default will be Palo Alto, CA if there is no last searched data AND user has not allowed location services
    var CITY: String = "palo alto,ca"
    var API: String = "3ddaaed47ca3b917cf6be85e7a15e16b"
    var UNITS: String = "metric"
    var mOpenWeatherMapAPI : OpenWeatherMapAPI? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /**
     * Called when the activity is starting. This method is responsible for initializing the UI components,
     * getting the user's current location, loading any previously saved data, and retrieving the weather data.
     *
     * @param savedInstanceState The saved state of the activity, which contains any data provided in onSaveInstanceState()
     * and passed back to onCreate() when the activity is recreated after being destroyed.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        loadData()
        getCurrentLocation()
        getSearchLocation()
        getWeather()
    }

    /**
     * This method is responsible for fetching weather data from the OpenWeatherMapAPI and updating the UI.
     * It launches a coroutine on a background thread using GlobalScope.launch and uses the OpenWeatherMapAPI class
     * to fetch the weather data for a specified city.
     * If the weather data is successfully retrieved, it saves the data using the saveData() method and updates the UI
     * using the updateUI() method. If there is no weather data, it displays a Toast message and hides the progress bar.
     */
    fun getWeather(){
        GlobalScope.launch {
            mOpenWeatherMapAPI = OpenWeatherMapAPI()
            val weatherData = mOpenWeatherMapAPI?.fetchOpenWeatherMapData(CITY, UNITS, API)
            withContext(Dispatchers.Main) {
                if (weatherData != null) {
                    saveData()
                    updateUI(weatherData)
                } else {
                    Toast.makeText(applicationContext, "Sorry, no results.", Toast.LENGTH_LONG).show()
                    findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                }
            }
        }
    }

    /**** UI Updaters ****/

    /**
     * Updates the UI elements with the weather data obtained from the OpenWeatherMapAPI.
     *
     * @param weatherData an instance of [WeatherData] containing the weather information.
     */
    fun updateUI(weatherData: WeatherData) {
        findViewById<TextView>(R.id.address).text = weatherData.address
        findViewById<TextView>(R.id.updated_at).text =  weatherData.updatedAtText
        findViewById<TextView>(R.id.status).text = weatherData.weatherDescription
        findViewById<TextView>(R.id.temp).text = weatherData.temp
        findViewById<TextView>(R.id.temp_min).text = weatherData.tempMin
        findViewById<TextView>(R.id.temp_max).text = weatherData.tempMax
        findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
            Date(weatherData.sunrise*1000)
        )
        findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
            Date(weatherData.sunset*1000)
        )
        findViewById<TextView>(R.id.wind).text = weatherData.windSpeed
        findViewById<TextView>(R.id.pressure).text = weatherData.pressure
        findViewById<TextView>(R.id.humidity).text = weatherData.humidity
        findViewById<TextView>(R.id.feels_like).text = weatherData.feelsLike

        updateWeatherIcon(weatherData.weatherIcon)
    }

    /**
     * Updates the weather icon based on the provided weather icon code using Picasso library to load the image from the
     * OpenWeatherMap API. If the image fails to load, a default image is used instead.
     *
     * @param weatherIcon A string representing the weather icon code.
     */
    private fun updateWeatherIcon(weatherIcon: String) {
        val imageView = findViewById<ImageView>(R.id.statusIcon)
        try {
            Picasso.get().load("https://openweathermap.org/img/wn/${weatherIcon}@2x.png").into(imageView)
        } catch (e:Exception){
            Picasso.get().load("https://www.clipartmax.com/png/middle/249-2493751_no-rain-icon-weather-seasons-icons-in-svg-and-png-iconscout-no.png").into(imageView)
        }
    }

    /** Location Related Methods **/

    /**
     * Gets the location that the user searches for, and updates the weather data for that location if a valid location was entered.
     *
     * This method retrieves the text entered into the "et_city_name" EditText field, checks if the input is a valid location name using regex,
     * updates the global CITY variable with the entered location name, and calls the getWeather() function to retrieve and display
     * the weather data for the entered location.
     *
     * If an invalid location name was entered, a toast message will be displayed to the user indicating that a valid location should be entered.
     * The method also clears the EditText field and hides the keyboard after the user has entered a location.
     */
    private fun getSearchLocation() {
        val searchedLocation = findViewById<EditText>(R.id.et_city_name)
        searchedLocation.setOnClickListener {
            val mgr: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.hideSoftInputFromWindow(searchedLocation.getWindowToken(), 0)
            val location = searchedLocation.text.toString()
            val regex = Regex("^.*[a-zA-Z]+.*$")
            if (regex.matches(location)) {
                CITY=location
                getWeather()
            } else {
                Toast.makeText(this, "Please enter valid location", Toast.LENGTH_SHORT).show()
            }
            Log.d("Location", location)
            searchedLocation.text.clear()
            searchedLocation.clearFocus()
        }
    }

    /** For auto loading previous search locations **/

    /**
     * Saves the last searched location to the device's shared preferences.
     * The last searched location is stored under the key "lastLocation".
     * This method retrieves the last searched location from the CITY variable,
     * and uses the Android SharedPreferences API to save it.
     * The saved location can be retrieved later to display weather information for that location.
     */
    private fun saveData() {
        val lastSearchedLocation = CITY
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("lastLocation", lastSearchedLocation)
        }.apply()
    }

    /**
     * Loads the previously saved location from the shared preferences, if available, and updates the CITY field.
     * The CITY field is used to fetch the weather data for the location.
     * If no location is found in the shared preferences, the default location (specified in the "CITY" constant) is used.
     */
    private fun loadData(){
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedLocation = sharedPreferences.getString("lastLocation", CITY)
        if (savedLocation != null) {
            CITY = savedLocation;
        }
    }

    /** Current location **/

    /**
     * Tries to get the current location of the device using fused location provider client.
     *
     * If the app has location permission and the device has location services enabled, it gets the last known location
     * and retrieves the city and state name from the obtained coordinates using Geocoder.
     * If the obtained location or address is null, a toast message is displayed indicating no location found.
     * If the app does not have location permission, it requests the user for permission.
     * If the location services are disabled on the device, it prompts the user to turn it on.
     */
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (checkPermissions()){
            if(isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) {
                    task ->
                    val location:Location? = task.result
                    if(location == null) {
                        Toast.makeText(this, "No location found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Location found", Toast.LENGTH_SHORT).show()
                        val gcd = Geocoder(this, Locale.getDefault())
                        val addresses: List<Address>? = gcd.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses!!.isNotEmpty()) {
                            val currentCity = addresses[0].locality
                            val currentState = addresses[0].adminArea
                            CITY = "$currentCity,$currentState"
                            getWeather()
                        }
                    }
                }

            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show()
                val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    /**
     * Checks if location services are enabled on the device.
     *
     * @return true if location services are enabled, false otherwise.
     */
    private fun isLocationEnabled():Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Requests location permission from the user.
     *
     * If the user grants the permission, the app can access the user's current location.
     * If the user denies the permission, the app cannot access the user's current location.
     * The result of the permission request is returned to the onRequestPermissionsResult method.
     * This method is called when the user clicks the "Allow" or "Deny" button on the permission dialog.
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
        PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    /**
     * A companion object for MainActivity that contains a constant integer value used
     * as a request code for requesting location permission.
     *
     * @property PERMISSION_REQUEST_ACCESS_LOCATION the request code used for requesting location permission
     */
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
    }

    /**
     * Checks if the app has been granted the necessary permissions to access location services.
     *
     * @return true if the app has been granted the necessary permissions, false otherwise.
     */
    private fun checkPermissions(): Boolean {
        if(ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    /**
     * Called when the user responds to the permission request dialog box. Handles the permission results
     * and takes appropriate actions based on the user's response. If the permission is granted, it calls
     * the [getCurrentLocation] method to get the current location of the device. If the permission is not
     * granted, displays a toast message indicating that the location permission was not granted.
     *
     * @param requestCode The request code passed in [ActivityCompat.requestPermissions].
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Location not shared", Toast.LENGTH_SHORT).show()
            }
        }
    }
}