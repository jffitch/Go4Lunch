package com.mathgeniusguide.project8

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.mathgeniusguide.go4lunch.database.RestaurantDao
import com.mathgeniusguide.go4lunch.database.RestaurantDatabase
import com.mathgeniusguide.project8.adapter.AutocompleteAdapter
import com.mathgeniusguide.project8.database.ChatItem
import com.mathgeniusguide.project8.database.CoworkerDao
import com.mathgeniusguide.project8.database.FirebaseCoworkerItem
import com.mathgeniusguide.project8.database.RestaurantItem
import com.mathgeniusguide.project8.responses.autocomplete.AutocompleteItem
import com.mathgeniusguide.project8.util.*
import com.mathgeniusguide.project8.util.FirebaseFunctions.createCoworker
import com.mathgeniusguide.project8.util.Functions.coordinateDistance
import com.mathgeniusguide.project8.util.Functions.restaurantItemDetails
import com.mathgeniusguide.project8.util.Functions.setNotificationAlarm
import com.mathgeniusguide.project8.viewmodel.PlacesViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    val viewModel by lazy { ViewModelProviders.of(this).get(PlacesViewModel::class.java) }
    lateinit var navController: NavController
    private val TAG = "Go4Lunch"
    private val ANONYMOUS = "anonymous"
    private lateinit var locationManager: LocationManager
    val placeList = MutableLiveData<MutableList<RestaurantItem>>()
    val markerList = emptyList<Marker?>().toMutableList()
    val autocompleteText = MutableLiveData<String>()
    var chosenPlace: RestaurantItem? = null
    var fetched = false
    var restaurantsLiked = mutableListOf<String>()
    var chattingWith = ""
    var callbackManager: CallbackManager? = null

    // Firebase variables
    lateinit var googleApiClient: GoogleApiClient
    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    var username = ANONYMOUS
    private var useremail = ANONYMOUS
    var userkey = ""
    var photoUrl = ""
    lateinit var database: DatabaseReference
    var firebaseCoworkerList = ArrayList<FirebaseCoworkerItem>()
    var chatList = ArrayList<ChatItem>()

    // Location
    private var locationEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    var latitude = MutableLiveData<Double>()
    var longitude = MutableLiveData<Double>()
    private var radius = 3000

    // Room Database
    private var db: RestaurantDatabase? = null
    private var restaurantDao: RestaurantDao? = null
    private var coworkerDao: CoworkerDao? = null

    // Notifications
    var notificationTime = "12:00:00"

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
        viewModel.fetchSavedCoworkers(this)
    }

    private fun setUpView() {
        // set up layout and toolbar
        setContentView(R.layout.activity_main)
        autocomplete.visibility = View.GONE
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
                R.id.instructions -> instructions()
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
    }

    private fun setUpRoomDatabase() {
        // set up database and dao for Room Database
        db = RestaurantDatabase.getDataBase(this)
        restaurantDao = db?.restaurantDao()
        coworkerDao = db?.coworkerDao()
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
        autocompleteText.postValue("")
        autocompleteET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {

            }

            override fun beforeTextChanged(
                text: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text.toString().length >= 3) {
                    if (autocompleteRV.visibility == View.VISIBLE) {
                        viewModel.fetchAutocompleteList(
                            text.toString(),
                            latitude.value!!,
                            longitude.value!!,
                            radius * 2
                        )
                    }
                    autocompleteText.postValue(text.toString())
                } else {
                    autocompleteText.postValue("")
                }
            }
        })
        autocompleteIV.setOnClickListener {
            if (autocompleteRV.visibility == View.VISIBLE) {
                autocompleteRV.visibility = View.GONE
            } else {
                autocompleteRV.visibility = View.VISIBLE
                if (autocompleteET.text.toString().length >= 3) {
                    viewModel.fetchAutocompleteList(
                        autocompleteET.text.toString(),
                        latitude.value!!,
                        longitude.value!!,
                        radius * 2
                    )
                }
            }
        }
    }

    private fun setUpLocation() {
        // load saved preference for radius
        val pref = getSharedPreferences(Constants.PREF_LOCATION, 0)
        radius = pref?.getInt("radius", 3000) ?: 3000
        notificationTime = pref?.getString("notificationTime", "12:00:00") ?: "12:00:00"

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
                chosenPlace =
                    restaurantItemDetails(it.result, latitude.value!!, longitude.value!!, resources)
                placeList.value!!.add(chosenPlace!!)
                viewModel.insertRestaurantItemIfNotExists(
                    chosenPlace!!.toRestaurantRoomdbItem(
                        resources
                    ), this
                )
                navController.navigate(R.id.load_page_from_map)
            }
        })
        viewModel.savedRestaurants.observe(this, Observer { details ->
            placeList.postValue(details.map { v ->
                restaurantItemDetails(
                    v,
                    latitude.value!!,
                    longitude.value!!
                )
            }.filter {
                coordinateDistance(
                    latitude.value!!,
                    longitude.value!!,
                    it.latitude,
                    it.longitude
                ) < radius
            }.toMutableList())
        })
        viewModel.savedCoworkers.observe(this, Observer { list ->
            for (i in list) {
                if (firebaseCoworkerList.none { i.id == it.id }) {
                    firebaseCoworkerList.add(i.toFirebaseCoworkerItem())
                }
            }
        })
        viewModel.isAutocompleteDataLoading.observe(this, Observer {
            autocompleteProgressScreen.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.autocompleteList?.observe(this, Observer {
            if (it != null) {
                autocompleteRV.bringToFront()
                autocompleteRV.layoutManager = LinearLayoutManager(this)
                autocompleteRV.adapter = AutocompleteAdapter(
                    it.predictions as ArrayList<AutocompleteItem>,
                    this,
                    placeList.value!!,
                    this
                )
            }
        })
    }

    private var itemListener: ValueEventListener = object : ValueEventListener {
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
            val firebaseCoworkerItem = FirebaseCoworkerItem.create()
            // get current data in a map
            val map = currentItem.value as HashMap<String, Any>
            // key will return Firebase ID
            firebaseCoworkerItem.id = currentItem.key
            // remove previous entry for this id if it exists
            firebaseCoworkerList =
                firebaseCoworkerList.filter { it.id != firebaseCoworkerItem.id } as ArrayList<FirebaseCoworkerItem>
            // get saved data for each coworker
            firebaseCoworkerItem.username = map.get("username") as String?
            firebaseCoworkerItem.restaurant = map.get("restaurant") as String?
            firebaseCoworkerItem.restaurantName = map.get("restaurantName") as String?
            firebaseCoworkerItem.liked = map.get("liked") as String?
            firebaseCoworkerItem.photo = map.get("photo") as String?
            firebaseCoworkerList.add(firebaseCoworkerItem)
            viewModel.insertCoworkerItem(firebaseCoworkerItem.toCoworkerRoomdbItem(), this)
        }

        // if coworker list from firebase does not include your username, create new item
        // if coworker list from firebase includes your username, update userkey and restaurantsLiked
        if (username != ANONYMOUS) {
            if (firebaseCoworkerList.none { it.username == username }) {
                userkey = createCoworker(username, "", "", "", photoUrl, database)
                restaurantsLiked = emptyList<String>().toMutableList()
                val firebaseCoworkerItem = FirebaseCoworkerItem()
                firebaseCoworkerItem.id = userkey
                firebaseCoworkerItem.liked = ""
                firebaseCoworkerItem.username = username
                firebaseCoworkerItem.photo = photoUrl
                firebaseCoworkerItem.restaurant = ""
                firebaseCoworkerItem.restaurantName = ""
                firebaseCoworkerList.add(firebaseCoworkerItem)
                // set up notifications for logged in user
                setNotificationAlarm(false, notificationTime, username, this)
            } else {
                userkey = firebaseCoworkerList.first { it.username == username }.id ?: ""
                restaurantsLiked =
                    (firebaseCoworkerList.first { it.username == username }.liked ?: "").split(" , ")
                        .toMutableList()
                // set up notifications for logged in user
                setNotificationAlarm(
                    firebaseCoworkerList.first { it.username == username }.restaurant != "",
                    notificationTime,
                    username,
                    this
                )
            }
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
            chatItem.id = currentItem.key
            // prevent same item from loading repeatedly
            if (chatList.none { it.id == chatItem.id }) {
                // get saved data for each chat
                chatItem.from = map.get("from") as String?
                chatItem.to = map.get("to") as String?
                chatItem.text = map.get("text") as String?
                chatItem.timestamp = map.get("timestamp") as String?
                chatList.add(chatItem)
            }
        }
    }

    private fun locationPermission(): Boolean {
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
        restaurantDao?.selectIds()?.observeOnce(this, Observer {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val now = sdf.format(Date())
            if (it != null) {
                // ignore IDs that are not within the radius
                val nearby = it.filter { restaurant ->
                    coordinateDistance(
                        lat,
                        lng,
                        restaurant.latitude ?: 0.0,
                        restaurant.longitude ?: 0.0
                    ) < radius
                }
                // separate saved IDs by expiration date, recentIds = expiration has not passed, expiredIds = expiration has passed
                // fetch places from API using location and saved IDs
                val recentIds = nearby.filter { v -> v.expiration > now }.map { v -> v.id }
                val expiredIds = nearby.filter { v -> v.expiration <= now }.map { v -> v.id }
                viewModel.fetchPlaces(lat, lng, radius, recentIds, expiredIds, this)
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
                autocomplete.visibility = View.VISIBLE
                return true
            }
            else -> return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(
                item
            )
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
            // retrieve data from firebase database
            database = FirebaseDatabase.getInstance().reference
            database.orderByKey().addListenerForSingleValueEvent(itemListener)
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
        LoginManager.getInstance().logOut()
        callbackManager = null
        // set user information to defaults
        setNotificationAlarm(false, notificationTime, username, this)
        username = ANONYMOUS
        firebaseUser = null
        photoUrl = ""
        // delete chats from this session, they will be reloaded from firebase
        chatList = chatList.filter { it.id != "" } as ArrayList<ChatItem>
        // navigate to login fragment
        // make toolbars invisible
        navController.navigate(R.id.action_logout)
        tabs.visibility = View.GONE
        toolbar.visibility = View.GONE
        autocomplete.visibility = View.GONE
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun yourLunch(): Boolean {
        // if coworker list doesn't load from firebase, or if it's empty, or if none match the user's username, show an error
        if (firebaseCoworkerList == null || firebaseCoworkerList.isEmpty() || firebaseCoworkerList.none { it.username == username }) {
            Toast.makeText(
                this,
                resources.getString(R.string.load_chosen_failed),
                Toast.LENGTH_LONG
            )
                .show()
            return true
        }
        // if coworker list loads correctly from firebase, save id of your chosen restaurant
        val yourRestaurant = firebaseCoworkerList.first { it.username == username }.restaurant
        // if no restaurant has been chosen yet, show an error
        if (yourRestaurant == "") {
            Toast.makeText(this, resources.getString(R.string.no_lunch_chosen), Toast.LENGTH_LONG)
                .show()
            return true
        }
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

    private fun instructions(): Boolean {
        // go to settings fragment
        navController.navigate(R.id.action_instructions)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (result != null) {
            if (result.isSuccess && result.signInAccount != null) {
                // Google Sign-In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(result.signInAccount!!)
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.")
            }
        } else {
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, { task ->
                Log.d(TAG, "signInWithCredential:onComplete:${task.isSuccessful}")

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful) {
                    Log.w(TAG, "signInWithCredential", task.exception)
                    Toast.makeText(
                        this, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    login(firebaseAuth.currentUser)
                }
            })
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
}