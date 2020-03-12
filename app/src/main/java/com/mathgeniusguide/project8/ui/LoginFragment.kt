package com.mathgeniusguide.project8.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.mathgeniusguide.project8.R
import android.widget.Toast
import android.util.Log
import kotlinx.android.synthetic.main.login_fragment.*
import com.mathgeniusguide.project8.MainActivity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mathgeniusguide.project8.util.Constants
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.util.*

class LoginFragment : Fragment() {
    private val TAG = "Go4Lunch"
    private val RC_SIGN_IN = 9001
    lateinit var act: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // declare activity shorthand variable
        act = activity as MainActivity
        // toolbar invisible
        act.toolbar.visibility = View.GONE
        // hide autocomplete
        act.autocomplete.visibility = View.GONE
        act.autocompleteRV.visibility = View.GONE
        // enable Google Sign In Button
        googleButton.setOnClickListener {
            startActivityForResult(
                Auth.GoogleSignInApi.getSignInIntent(act.googleApiClient),
                RC_SIGN_IN
            )
        }
        // Initialize Facebook Login button
        act.callbackManager = CallbackManager.Factory.create()

        facebookButton.setReadPermissions("email", "public_profile")
        facebookButton.registerCallback(act.callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })

        // drawer locked closed if not logged in
        act.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        act.firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(act) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    act.login(act.firebaseAuth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}