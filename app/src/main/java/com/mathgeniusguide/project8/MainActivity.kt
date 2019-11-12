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
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
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
import com.mathgeniusguide.project8.viewmodel.MyViewModel

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    val viewModel by lazy { ViewModelProviders.of(this).get(MyViewModel::class.java)}
    lateinit var navController: NavController
    private val TAG = "Go4Lunch"
    private val RC_SIGN_IN = 9001
    private val ANONYMOUS = "anonymous"
    lateinit var locationManager: LocationManager
    val placeList = MutableLiveData<ArrayList<NearbyPlace>>()
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
                for (place in it.results) {
                    viewModel.fetchDetails(place.place_id)
                    viewModel.details?.observe(this, Observer {details ->
                        // put values in a nearbyPlace object, then add to placeList
                        // placeList will be observed by MapView and ListView fragments
                        if (details != null) {
                            nearbyPlace = NearbyPlace()
                            val r = details.result
                            nearbyPlace!!.id = place.place_id
                            nearbyPlace!!.address = r.formatted_address
                            nearbyPlace!!.phone = r.formatted_phone_number
                            nearbyPlace!!.latitude = place.geometry.location.lat
                            nearbyPlace!!.longitude = place.geometry.location.lng
                            nearbyPlace!!.distance = coordinateDistance(lat, lng, nearbyPlace!!.latitude, nearbyPlace!!.longitude)
                            nearbyPlace!!.website = r.website
                            nearbyPlace!!.rating = r.rating
                            nearbyPlace!!.name = place.name
                            placeList.value!!.add(nearbyPlace!!)
                        }
                    })
                }
            }
        })
    }

    fun coordinateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double) : Double{
        val latDiff = Math.abs(lat1 - lat2)
        val lngDiff = Math.abs(lng1 - lng2) * Math.cos(lat1 * Math.PI / 180.0)
        return 111319.5 * (latDiff * latDiff + lngDiff * lngDiff)
    }

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
