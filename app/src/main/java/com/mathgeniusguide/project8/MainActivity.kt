package com.mathgeniusguide.project8

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



class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener  {
    lateinit var navController: NavController
    private val TAG = "Go4Lunch"
    private val RC_SIGN_IN = 9001
    private val ANONYMOUS = "anonymous"

    // Firebase variables
    lateinit var googleApiClient: GoogleApiClient
    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null;
    var username = ANONYMOUS
    var photoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        navController = findNavController(nav_host_fragment)
        toolbar.setupWithNavController(navController)
        tabs.setupWithNavController(navController)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        drawer_view.setNavigationItemSelectedListener {
            it.onNavDestinationSelected(navController)
        }

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
            username = firebaseUser!!.getDisplayName()?: ANONYMOUS
            if (firebaseUser!!.getPhotoUrl() != null) {
                photoUrl = firebaseUser!!.getPhotoUrl().toString()
            }
            findNavController(nav_host_fragment).navigate(R.id.action_login)
        }
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
        return when (item.itemId) {
            R.id.logout -> logout()
            else -> item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
        }
    }

    fun logout(): Boolean {
        firebaseAuth.signOut()
        Auth.GoogleSignInApi.signOut(googleApiClient);
        username = ANONYMOUS;
        firebaseUser = null
        photoUrl = ""
        findNavController(nav_host_fragment).navigate(R.id.action_logout)
        return true
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }
}
