package com.mathgeniusguide.project8

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mathgeniusguide.project8.responses.NearbyPlace
import com.mathgeniusguide.project8.responses.details.*
import com.mathgeniusguide.project8.viewmodel.PlacesViewModel
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    val viewModel by lazy { ViewModelProviders.of(this).get(PlacesViewModel::class.java)}
    lateinit var navController: NavController
    private val TAG = "Go4Lunch"
    private val RC_SIGN_IN = 9001
    private val ANONYMOUS = "anonymous"
    lateinit var locationManager: LocationManager
    val placeList = MutableLiveData<List<NearbyPlace>>()
    var nearbyPlace: NearbyPlace? = null
    var fetched = false

    // Firebase variables
    lateinit var googleApiClient: GoogleApiClient
    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null;
    var username = ANONYMOUS
    var photoUrl = ""

    // Location
    var locationEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        navController = findNavController(nav_host_fragment)
        toolbar.setupWithNavController(navController)
        tabs.setupWithNavController(navController)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        drawer_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> logout()
                else -> it.onNavDestinationSelected(navController)
            }
        }

        latitude.postValue(91.0)
        longitude.postValue(181.0)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser()
        if (firebaseUser != null) {
            username = firebaseUser!!.getDisplayName() ?: ANONYMOUS
            if (firebaseUser!!.getPhotoUrl() != null) {
                photoUrl = firebaseUser!!.getPhotoUrl().toString()
            }
            findNavController(nav_host_fragment).navigate(R.id.action_login)
            drawer_view.visibility = View.VISIBLE
            tabs.visibility = View.VISIBLE
            toolbar.visibility = View.VISIBLE
        }

        // Device Location
        if (!locationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 123)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            locationEnabled = true
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0.toFloat(),
                this
            )
        }
    }

    fun locationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onLocationChanged(location: Location?) {
        latitude.postValue(location?.latitude ?: 91.0)
        longitude.postValue(location?.longitude ?: 181.0)
        if (location != null && latitude.value != 91.0 && longitude.value != 181.0 && !fetched) {
            getNearbyPlaces(location.latitude, location.longitude)
            fetched = true
        }
    }

    fun getNearbyPlaces(lat: Double, lng: Double) {
        // fetch places from API
        viewModel.fetchPlaces(lat, lng)
        viewModel.places?.observe(this, Observer {
            if (it != null) {
                // for each place, use the place_id to fetch details about that place
                viewModel.fetchDetails(it.results.map {v -> v.place_id})
                viewModel.details?.observe(this, Observer {details ->
                    placeList.postValue(details.map {v -> nearbyPlaceDetails(v!!.result)})
                })
            }
        })
    }

    fun coordinateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double) : Int{
        val latDiff = Math.abs(lat1 - lat2)
        val lngDiff = Math.abs(lng1 - lng2) * Math.cos(lat1 * Math.PI / 180.0)
        return (111319.5 * Math.sqrt(latDiff * latDiff + lngDiff * lngDiff)).toInt()
    }

    fun nearbyPlaceDetails(result: DetailsResult) : NearbyPlace {
        val nearbyPlace = NearbyPlace()
        nearbyPlace.id = result.place_id
        nearbyPlace.address = result.formatted_address
        nearbyPlace.phone = result.formatted_phone_number
        nearbyPlace.latitude = result.geometry.location.lat
        nearbyPlace.longitude = result.geometry.location.lng
        nearbyPlace.distance = coordinateDistance(latitude.value!!, longitude.value!!, nearbyPlace.latitude, nearbyPlace.longitude)
        nearbyPlace.website = result.website
        nearbyPlace.name = result.name
        nearbyPlace.rating = result.rating
        nearbyPlace.time = openTime(result.opening_hours)
        if (result.photos != null && result.photos.isNotEmpty()) {
            nearbyPlace.image = result.photos[0].photo_reference
        }
        return nearbyPlace
    }

    // CURRENT GOAL
    fun openTime(times: DetailsOpeningHours?) : String {
        if (times == null) {
            return resources.getString(R.string.open_24_7)
        }
        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_WEEK) - 1
        val todayHour = today.get(Calendar.HOUR_OF_DAY)
        val todayMinute = today.get(Calendar.MINUTE)
        val dayAndTime = "${todayDay}${todayHour}${todayMinute}".toInt()
        if (times.open_now) {
            val periods = times.periods.map{"${it.close.day}${it.close.time}".toInt()}.sorted()
            val periodsAfter = periods.filter{it > dayAndTime}
            val next = if (periodsAfter.isEmpty()) periods[0] else periodsAfter[0]
            val diff = timeDiff(dayAndTime, next)
            val nextDay = next / 10000
            val nextHour = (next / 100) % 100
            val nextMinute = next % 100
            if (diff < 30) {
                return resources.getString(R.string.closing_soon)
            }
            if (diff < 1440) {
                return String.format(resources.getString(R.string.open_until), "${nextHour}:${nextMinute.toString().padStart(2, '0')}")
            }
            return resources.getString(R.string.open_24)
        } else {
            val periods = times.periods.map{"${it.open.day}${it.open.time}".toInt()}
            val periodsAfter = periods.filter{it > dayAndTime}
            val next = if (periodsAfter.isEmpty()) periods[0] else periodsAfter[0]
            val diff = timeDiff(dayAndTime, next)
            val nextDay = next / 10000
            val nextHour = (next / 100) % 100
            val nextMinute = next % 100
            if (diff < 1440) {
                return String.format(resources.getString(R.string.opens_at), "${nextHour}:${nextMinute.toString().padStart(2, '0')}")
            }
            if (listOf(-6, 1).contains(nextDay - todayDay)) {
                return String.format(resources.getString(R.string.opens_tomorrow_at), "${nextHour}:${nextMinute.toString().padStart(2, '0')}")
            }
            return String.format(resources.getString(R.string.opens_day_at), resources.getString(when (nextDay){
                0 -> R.string.sunday
                1 -> R.string.monday
                2 -> R.string.tuesday
                3 -> R.string.wednesday
                4 -> R.string.thursday
                5 -> R.string.friday
                else -> R.string.saturday
            }), "${nextHour}:${nextMinute.toString().padStart(2, '0')}")
        }
    }

    fun timeDiff(start: Int, end: Int) : Int{
        // inputs are 5 digit numbers in form dhhmm
        // convert to minutes, then subtract
        // add 10080 if negative
        val startMinutes = start - 40 * (start / 100) - 4560 * (start / 10000)
        val endMinutes = end - 40 * (end / 100) - 4560 * (end / 10000)
        return (if (endMinutes < startMinutes) 10080 else 0) + endMinutes - startMinutes
    }
    // https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=41.46,-72.8222&radius=3000&type=restaurant&fields=place_id&key=AIzaSyDMWYwVXRhuhSQ5vcom9iAI2-FH6T6QKDI
    // https://maps.googleapis.com/maps/api/place/details/json?place_id=ChIJgSJBwI_O54kRYpfsTlHz2KQ&key=AIzaSyDMWYwVXRhuhSQ5vcom9iAI2-FH6T6QKDI&fields=place_id,formatted_address,formatted_phone_number,geometry/location,website,name,rating,opening_hours

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    fun logout(): Boolean {
        firebaseAuth.signOut()
        Auth.GoogleSignInApi.signOut(googleApiClient);
        username = ANONYMOUS;
        firebaseUser = null
        photoUrl = ""
        drawer_view.visibility = View.GONE
        tabs.visibility = View.GONE
        toolbar.visibility = View.GONE
        findNavController(nav_host_fragment).navigate(R.id.action_logout)
        return true
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
}
