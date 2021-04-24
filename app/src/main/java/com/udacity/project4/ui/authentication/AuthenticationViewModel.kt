package com.udacity.project4.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.utils.AuthenticationStates
import com.udacity.project4.utils.FirebaseUserLiveData

class AuthenticationViewModel : ViewModel() {

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationStates.AUTHENTICATED
        } else {
            AuthenticationStates.UNAUTHENTICATED
        }
    }
}