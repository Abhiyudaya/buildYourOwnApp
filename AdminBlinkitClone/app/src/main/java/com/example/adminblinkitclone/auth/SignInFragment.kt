package com.example.adminblinkitclone.auth;

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.adminblinkitclone.R
import com.example.adminblinkitclone.Utils
import com.example.adminblinkitclone.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        setStatusBarColor()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onContinueButtonClicked()
        getUserNumber()
        animateLoginImagesInfinite()
    }

    private fun onContinueButtonClicked() {
        binding.btnContinue.setOnClickListener {
            val number = binding.etUserNumber.text.toString()
            if(number.isEmpty() || number.length != 10){
                Utils.showToast(requireContext(),"Please enter valid phone number")
            }
            else{
                val bundle = Bundle()
                bundle.putString("number",number)
                findNavController().navigate(R.id.action_signInFragment_to_OTPFragment,bundle)
            }
        }
    }

    private fun getUserNumber() {
        binding.etUserNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                if (number?.length == 10) {
                    binding.btnContinue.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )
                } else {
                    binding.btnContinue.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.grayish_blue)
                    )
                }
            }
            override fun afterTextChanged(s: Editable?) { }
        })
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColorValue = ContextCompat.getColor(requireContext(), R.color.Yellow)
            statusBarColor = statusBarColorValue
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    /**
     * Animate two ImageViews to create an infinitely scrolling, seamless effect.
     * We assume that both ImageViews display the same tileable image.
     */
    private fun animateLoginImagesInfinite() {
        binding.ivLoginImage1.post {
            // Ensure both images are visible and aligned side-by-side.
            val imageWidth = binding.ivLoginImage1.width.toFloat()
            // Set initial positions: image1 at 0, image2 immediately to its right.
            binding.ivLoginImage1.translationX = 0f
            binding.ivLoginImage2.translationX = imageWidth

            // Create a ValueAnimator that will drive the scrolling effect.
            // We animate from 0 to imageWidth (this is one full cycle).
            val animator = ValueAnimator.ofFloat(0f, imageWidth)
            animator.duration = 5000L  // Adjust this duration to change scroll speed.
            animator.repeatCount = ValueAnimator.INFINITE
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener { animation ->
                val offset = animation.animatedValue as Float
                // Image1 moves from 0 to -imageWidth
                binding.ivLoginImage1.translationX = -offset
                // Image2 moves from imageWidth to 0
                binding.ivLoginImage2.translationX = imageWidth - offset
                // When offset resets, the images swap seamlessly.
            }
            animator.start()
        }
    }
}
