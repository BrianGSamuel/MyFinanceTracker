package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.databinding.ActivitySplash2Binding

class SplashActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivitySplash2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplash2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        binding.splashLogo.startAnimation(fadeIn)
        binding.splashAppName.startAnimation(fadeIn)

        // Wait for user tap to transition
        binding.root.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
