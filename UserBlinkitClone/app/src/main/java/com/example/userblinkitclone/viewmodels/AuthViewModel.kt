package com.example.userblinkitclone.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.models.Users
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val _verificationId = MutableStateFlow<String?>(null)
    private val _otpsent = MutableStateFlow(false)
    val otpSent = _otpsent

    private val _isSignedInSuccessfully = MutableStateFlow(false)
    val isSignedInSuccessfully = _isSignedInSuccessfully

    private val _isACurrentUser = MutableStateFlow(false)
    val isACurrentUser = _isACurrentUser

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isACurrentUser.value = true
        }
    }

    fun sendOTP(userNumber: String, activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Optionally handle auto-retrieval here.
            }
            override fun onVerificationFailed(e: FirebaseException) {
                // Handle error.
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _verificationId.value = verificationId
                _otpsent.value = true
            }
        }

        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber("+91$userNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Users) {
        val verificationId = _verificationId.value
        if (verificationId == null) {
            // Verification ID is null; handle error accordingly.
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        Utils.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After sign-in, retrieve the current user's UID.
                    val uid = Utils.getAuthInstance().currentUser?.uid
                    if (uid != null) {
                        // Update the user object with the UID and save to the database.
                        FirebaseDatabase.getInstance().getReference("AllUsers")
                            .child("Users")
                            .child(uid)
                            .setValue(user.copy(uid = uid))
                        _isSignedInSuccessfully.value = true
                    }
                } else {
                    // Handle sign-in failure.
                }
            }
    }
}
