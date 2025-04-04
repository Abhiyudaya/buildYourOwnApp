package com.example.adminblinkitclone.auth;

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.activity.AdminMainActivity
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.model.Admins
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.databinding.FragmentOTPBinding
import com.example.adminblinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(inflater, container, false)

        // Get phone number from arguments and display it
        getUserNumber()
        // Configure OTP input fields
        customizingEnteringOTP()
        // Start OTP sending process
        sendOTP()
        // Set login button behavior
        onLoginButtonClicked()
        // Set back button behavior
        onBackButtonClicked()

        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Signing you in...")
            val editTexts = arrayOf(
                binding.etOtp1,
                binding.etOtp2,
                binding.etOtp3,
                binding.etOtp4,
                binding.etOtp5,
                binding.etOtp6
            )
            val otp = editTexts.joinToString("") { it.text.toString() }

            if (otp.length < editTexts.size) {
                Utils.showToast(requireContext(), "Please enter correct OTP")
                Utils.hideDialog()
            } else {
                // Clear OTP fields and focus before verifying
                editTexts.forEach {
                    it.text?.clear()
                    it.clearFocus()
                }
                verifyOtp(otp)
            }
        }
    }

    private fun verifyOtp(otp: String) {
        // Create a user object with an empty UID (it will be updated after sign-in)
        val user = Admins(uid = "", phoneNumber = userNumber)

        // Pass otp, userNumber, and the user object to the ViewModel
        viewModel.signInWithPhoneAuthCredential(otp, userNumber, user)

        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect { isSignedIn ->
                if (isSignedIn) {
                    // Now the user should be signed in. Retrieve the current user UID.
                    val currentUserId = Utils.getCurrentUserId()
                    if (currentUserId != null) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Logged In...")
                        startActivity(Intent(requireActivity(), AdminMainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Error: Unable to retrieve User ID.")
                    }
                }
            }
        }
    }


    private fun sendOTP() {
        Utils.showDialog(requireContext(), "Sending OTP...")
        viewModel.sendOTP(userNumber, requireActivity())
        lifecycleScope.launch {
            viewModel.otpSent.collect { otpSent ->
                if (otpSent) {
                    Utils.hideDialog()
                    Utils.showToast(requireContext(), "OTP sent to the number...")
                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbOtpFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
            })
        }
    }

    private fun getUserNumber() {
        // Retrieve the phone number from the fragment's arguments.
        userNumber = arguments?.getString("number").orEmpty()
        binding.tvUserNumber.text = userNumber
    }
}
