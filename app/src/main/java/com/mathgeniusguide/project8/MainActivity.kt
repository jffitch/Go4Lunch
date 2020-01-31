package com.mathgeniusguide.project8

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.mathgeniusguide.go4lunch.database.RestaurantDao
import com.mathgeniusguide.go4lunch.database.RestaurantDatabase
import com.mathgeniusguide.project8.util.observeOnce
import com.mathgeniusguide.project8.util.toRestaurantItem
import com.mathgeniusguide.project8.database.ChatItem
import com.mathgeniusguide.project8.database.ChosenRestaurantItem
import com.mathgeniusguide.project8.util.Constants
import com.mathgeniusguide.project8.util.Functions.createRestaurant
import com.mathgeniusguide.project8.util.Functions.nearbyPlaceDetails
import com.mathgeniusguide.project8.database.NearbyPlace
import com.mathgeniusguide.project8.util.Functions.coordinateDistance
import com.mathgeniusguide.project8.viewmodel.PlacesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    val viewModel by lazy { ViewModelProviders.of(this).get(PlacesViewModel::class.java) }
    lateinit var navController: NavController
    private val TAG = "Go4Lunch"
    private val RC_SIGN_IN = 9001
    private val ANONYMOUS = "anonymous"
    lateinit var locationManager: LocationManager
    val placeList = MutableLiveData<MutableList<NearbyPlace>>()
    var chosenPlace: NearbyPlace? = null
    var fetched = false
    var restaurantsLiked = mutableListOf<String>()
    var chattingWith = ""

    // Firebase variables
    lateinit var googleApiClient: GoogleApiClient
    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    var username = ANONYMOUS
    var useremail = ANONYMOUS
    var userkey = ""
    var photoUrl = ""
    lateinit var database: DatabaseReference
    var chosenRestaurantList = ArrayList<ChosenRestaurantItem>()
    var chatList = ArrayList<ChatItem>()

    // Location
    var locationEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()
    var radius = 3000

    // Room Database
    private var db: RestaurantDatabase? = null
    private var dao: RestaurantDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpView()
        setUpNavigation()
        setUpFirebase()
        setUpRoomDatabase()
        setUpGoogleSignIn()
        setUpAutoComplete()
        setUpLocation()
        observeLiveData()
    }

    private fun setUpView() {
        // set up layout and toolbar
        setContentView(R.layout.activity_main)
        autocompleteFragment.view?.visibility = View.GONE
        setSupportActionBar(toolbar)
    }

    private fun setUpNavigation() {
        // attach navController to toolbar and tabs
        navController = findNavController(nav_host_fragment)
        toolbar.setupWithNavController(navController)
        tabs.setupWithNavController(navController)

        // set up drawer layout
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // activate drawer clicks
        drawer_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> logout()
                R.id.your_lunch -> yourLunch()
                R.id.settings -> settings()
                else -> it.onNavDestinationSelected(navController)
            }
        }
    }

    private fun setUpFirebase() {
        // set up firebase login
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        login(firebaseUser)
        // retrieve data from firebase database
        database = FirebaseDatabase.getInstance().reference
        database.orderByKey().addListenerForSingleValueEvent(itemListener)
    }

    private fun setUpRoomDatabase() {
        // set up database and dao for Room Database
        db = RestaurantDatabase.getDataBase(this)
        dao = db?.restaurantDao()
    }

    private fun setUpGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun setUpAutoComplete() {
        val autocomplete = autocompleteFragment as AutocompleteSupportFragment
        Places.initialize(getApplicationContext(), Constants.API_KEY)
        val placesClient = Places.createClient(this)
        autocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
        autocomplete.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // if chosen place already exists in saved list, retrieve data from saved list
                // if chosen place does not already exist in saved list, fetch data from API
                if (placeList.value!!.any {it.id == place.id}) {
                    chosenPlace = placeList.value!!.first {it.id == place.id}
                    navController.navigate(R.id.load_page_from_map)
                } else {
                    autocompleteProgressText.text = String.format(resources.getString(R.string.loading_info_for), place.name)
                    viewModel.fetchOneDetail(place.id!!)
                }
                Log.i(TAG, "Place: " + place.name + ", " + place.id)
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: " + status)
            }
        })
    }

    private fun setUpLocation() {
        // load saved preference for radius
        val pref = getSharedPreferences(Constants.PREF_LOCATION, 0)
        radius = pref?.getInt("radius", 3000) ?: 3000

        // initialize location to impossible values
        latitude.postValue(91.0)
        longitude.postValue(181.0)

        // request location permissions
        if (!locationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                123
            )
        }

        // activate location functionality if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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

    private fun observeLiveData() {
        viewModel.oneDetail?.observe(this, Observer {
            if (it != null) {
                chosenPlace = nearbyPlaceDetails(it.result, latitude.value!!, longitude.value!!, resources)
                placeList.value!!.add(chosenPlace!!)
                viewModel.insertRestaurantItemIfNotExists(chosenPlace!!.toRestaurantItem(resources), this)
                navController.navigate(R.id.load_page_from_map)
            }
        })
        viewModel.savedRestaurants.observe(this, Observer { details ->
            placeList.postValue(details.map { v -> nearbyPlaceDetails(v, latitude.value!!, longitude.value!!) }.filter { coordinateDistance(latitude.value!!, longitude.value!!, it.latitude, it.longitude) < radius}.toMutableList())
        })
        viewModel.isAutocompleteDataLoading.observe(this, Observer {
            autocompleteProgressScreen.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        // get coworker data
        val restaurants = dataSnapshot.child(Constants.CHOSEN_RESTAURANTS).children.iterator()
        while (restaurants.hasNext()) {
            // get current item
            val currentItem = restaurants.next()
            val chosenRestaurantItem = ChosenRestaurantItem.create()
            // get current data in a map
            val map = currentItem.value as HashMap<String, Any>
            // key will return Firebase ID
            // get saved data for each coworker
            chosenRestaurantItem.id = currentItem.key
            chosenRestaurantItem.username = map.get("username") as String?
            chosenRestaurantItem.restaurant = map.get("restaurant") as String?
            chosenRestaurantItem.liked = map.get("liked") as String?
            chosenRestaurantItem.photo = map.get("photo") as String?
            chosenRestaurantList.add(chosenRestaurantItem)
        }

        // if coworker list from firebase does not include your username, add it
        // update userkey and restaurantsLiked
        if (username != ANONYMOUS) {
            if (chosenRestaurantList.none { it.username == username }) {
                createRestaurant(username, "", "", photoUrl, database)
            }
            userkey = chosenRestaurantList.first { it.username == username }.id!!
            restaurantsLiked = chosenRestaurantList.first { it.username == username }.liked!!.split(" , ").toMutableList()
        }

        // get chats data
        val chats = dataSnapshot.child(Constants.CHATS).children.iterator()
        while (chats.hasNext()) {
            // get current item
            val currentItem = chats.next()
            val chatItem = ChatItem.create()
            // get current data in a map
            val map = currentItem.value as HashMap<String, Any>
            // key will return Firebase ID
            // get saved data for each chat
            chatItem.id = currentItem.key
            chatItem.from = map.get("from") as String?
            chatItem.to = map.get("to") as String?
            chatItem.text = map.get("text") as String?
            chatItem.timestamp = map.get("timestamp") as String?
            chatList.add(chatItem)
        }
    }

    fun locationPermission(): Boolean {
        // check whether permissions have been granted
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onLocationChanged(location: Location?) {
        // get latitude and longitude values from each location
        // if latitude and longitude are both valid values, and if nearby places have not been fetched yet, fetch nearby places from API
        latitude.postValue(location?.latitude ?: 91.0)
        longitude.postValue(location?.longitude ?: 181.0)
        if (location != null && latitude.value != 91.0 && longitude.value != 181.0 && !fetched) {
            getNearbyPlaces(location.latitude, location.longitude)
            fetched = true
        }
    }

    private fun getNearbyPlaces(lat: Double, lng: Double) {
        // load saved IDs from database
        dao!!.selectIds().observeOnce(this, Observer {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val now = sdf.format(Date())
            if (it != null) {
                // ignore IDs that are not within the radius
                val nearby = it.filter { coordinateDistance(lat, lng, it.latitude!!, it.longitude!!) < radius}
                // separate saved IDs by expiration date, recentIds = expiration has not passed, expiredIds = expiration has passed
                // fetch places from API using location and saved IDs
                val recentIds = nearby.filter {v -> v.expiration > now}.map {v -> v.id}
                val expiredIds = nearby.filter {v -> v.expiration <= now}.map {v -> v.id}
                viewModel.fetchPlaces(lat, lng, radius, recentIds, expiredIds,this)
            }
        })
    }

    // https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=41.46,-72.8222&radius=3000&type=restaurant&fields=place_id&key=AIzaSyDMWYwVXRhuhSQ5vcom9iAI2-FH6T6QKDI
    // https://maps.googleapis.com/maps/api/place/details/json?place_id=ChIJgSJBwI_O54kRYpfsTlHz2KQ&key=AIzaSyDMWYwVXRhuhSQ5vcom9iAI2-FH6T6QKDI&fields=place_id,formatted_address,formatted_phone_number,geometry/location,website,name,rating,opening_hours
    // https://maps.googleapis.com/maps/api/place/autocomplete/json?location=41.46,-72.8222&radius=3000&key=AIzaSyDMWYwVXRhuhSQ5vcom9iAI2-FH6T6QKDI&input=sub

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
        // if search item clicked, hide toolbar and show autocomplete fragment
        // else, run as normal
        when (item.itemId) {
            R.id.search -> {
                toolbar.visibility = View.GONE
                autocompleteFragment.view?.visibility = View.VISIBLE
                return true
            }
            else -> return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
        }
    }

    fun login(user: FirebaseUser?) {
        firebaseUser = user
        if (user != null) {
            // retrieve username and email from firebase
            username = user.displayName ?: ANONYMOUS
            useremail = user.email ?: ANONYMOUS
            // show username and email in drawer layout
            val header = drawer_view.getHeaderView(0)
            header.findViewById<TextView>(R.id.userName).text = username
            header.findViewById<TextView>(R.id.userEmail).text = useremail
            // show photo in drawer layout if user has one
            if (user.photoUrl != null) {
                photoUrl = user.photoUrl.toString()
                Glide.with(this).load(photoUrl).into(header.findViewById(R.id.userImage))
            }
            // navigate to map fragment and make toolbars visible
            navController.navigate(R.id.action_login)
            tabs.visibility = View.VISIBLE
            toolbar.visibility = View.VISIBLE
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    private fun logout(): Boolean {
        // sign out of firebase
        firebaseAuth.signOut()
        Auth.GoogleSignInApi.signOut(googleApiClient)
        // set user information to defaults
        username = ANONYMOUS
        firebaseUser = null
        photoUrl = ""
        // navigate to login fragment
        // make toolbars invisible
        navController.navigate(R.id.action_logout)
        tabs.visibility = View.GONE
        toolbar.visibility = View.GONE
        autocompleteFragment.view?.visibility = View.GONE
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun yourLunch(): Boolean {
        // if coworker list doesn't load from firebase, or if it's empty, or if none match the user's username, show an error
        if (chosenRestaurantList == null || chosenRestaurantList.isEmpty() || chosenRestaurantList.none { it.username == username }) {
            Toast.makeText(
                this,
                resources.getString(R.string.load_chosen_failed),
                Toast.LENGTH_LONG
            )
                .show()
            return true
        }
        // if coworker list loads correctly from firebase, save id of your chosen restaurant
        val yourRestaurant = chosenRestaurantList.first { it.username == username }.restaurant
        // if nearby place list doesn't load from API, or if it's empty, or if none match the chosen restaurant, show an error
        if (placeList.value == null || placeList.value!!.isEmpty() || placeList.value!!.none { it.id == yourRestaurant }) {
            Toast.makeText(
                this,
                resources.getString(R.string.load_nearby_failed),
                Toast.LENGTH_LONG
            )
                .show()
            return true
        }
        // if no restaurant has been chosen yet, show an error
        if (yourRestaurant == "") {
            Toast.makeText(this, resources.getString(R.string.no_lunch_chosen), Toast.LENGTH_LONG)
                .show()
            return true
        }
        // if everything loads correctly and a restaurant has been chosen, go to that restaurant page
        chosenPlace = placeList.value!!.first { it.id == yourRestaurant }
        navController.navigate(R.id.load_page_from_map)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun settings(): Boolean {
        // go to settings fragment
        navController.navigate(R.id.action_settings)
        drawer_layout.closeDrawer(GravityCompat.START)
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