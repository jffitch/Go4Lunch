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
import kotlinx.android.synthetic.main.login.*
import com.mathgeniusguide.project8.MainActivity
import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class LoginFragment : Fragment() {
    private val TAG = "Go4Lunch"
    private val RC_SIGN_IN = 9001
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).toolbar.visibility = View.GONE
        (activity as MainActivity).autocompleteFragment.view?.visibility = View.GONE
        // enable Google Sign In Button
        googleButton.setOnClickListener {
            startActivityForResult(
                Auth.GoogleSignInApi.getSignInIntent((activity as MainActivity).googleApiClient),
                RC_SIGN_IN
            )
        }
        callbackManager = CallbackManager.Factory.create();
        facebookButton.setReadPermissions("email", "public_profile")
        facebookButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
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
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        (activity as MainActivity).firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!, { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    (activity as MainActivity).login((activity as MainActivity).firebaseAuth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    // updateUI(null)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // PlaceResult returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Google Sign-In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(result.signInAccount!!)
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        (activity as MainActivity).firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!, { task ->
                Log.d(TAG, "signInWithCredential:onComplete:${task.isSuccessful}")

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful) {
                    Log.w(TAG, "signInWithCredential", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    (activity as MainActivity).login((activity as MainActivity).firebaseAuth.currentUser)
                }
            })
    }
}