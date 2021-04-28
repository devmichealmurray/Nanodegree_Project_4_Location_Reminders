package com.udacity.project4.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.ui.activities.RemindersActivity
import com.udacity.project4.utils.AuthenticationStates

private const val TAG = "* * AuthenticationActivity * *"

class AuthenticationActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthenticationViewModel>()
    private lateinit var authActivityBinding: ActivityAuthenticationBinding
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result -> resultHandler(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authActivityBinding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        authActivityBinding.activity = this
    }

    override fun onStart() {
        super.onStart()
        authViewModel.authenticationState.observe(this, authStateObserver)
    }


    fun launchSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.AuthUITheme)
            .setLogo(R.drawable.map)
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()
            .apply {
                startForResult.launch(this)
            }
    }

    private fun resultHandler(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                Toast.makeText(this, "Sign In Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, RemindersActivity::class.java))
            }
            else -> {
                val response = IdpResponse.fromResultIntent(result.data)
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Sign In Failed -- E: ${response?.error?.errorCode}")
            }
        }
    }

    private val authStateObserver = Observer<AuthenticationStates> { authState ->
        if (authState == AuthenticationStates.AUTHENTICATED)
            startActivity(Intent(this, RemindersActivity::class.java))
    }

}
