package com.capstone.emoticalm

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.capstone.emoticalm.databinding.ActivitySuccessBinding // Correct binding class

class SuccessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySuccessBinding.inflate(layoutInflater) // Use correct binding class
        setContentView(binding.root)

        // Handle bottom navigation item selection
        binding.bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home navigation here if needed
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    // Navigate to Settings Activity
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_history -> {
                    // Navigate to History Activity
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Retrieve data from intent
        val imageUrl = intent.getStringExtra("imageUrl") ?: "No image available"
        val predictedLabel = intent.getStringExtra("predictedLabel") ?: "No label available"
        val suggestion = intent.getStringExtra("suggestion") ?: "No suggestion available"

        // Use the binding to reference the views
        binding.predictedLabelTextView.text = "Predicted Label: $predictedLabel"
        binding.suggestionLabelTextView.text = "Suggestion: $suggestion"

        Glide.with(this)
            .asBitmap()  // Ensures Glide loads the image as a Bitmap
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Apply rotation to the bitmap (e.g., rotate 90 degrees)
                    val rotatedBitmap = rotateImage(resource, -90f)
                    binding.imageView.setImageBitmap(rotatedBitmap)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    // Handle cleanup if necessary
                }
            })
    }
    private fun rotateImage(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)  // Rotate the image by the specified angle
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
