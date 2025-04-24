package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply animations
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        binding.splashLogo.startAnimation(fadeIn)
        binding.splashAppName.startAnimation(fadeIn)

        // Transition to LoginActivity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SplashActivity2::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 2-second delay
    }
}