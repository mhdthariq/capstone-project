package com.capstone.emoticalm

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide

class SuccessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val imageUrl = intent.getStringExtra("imageUrl") ?: "No image available"
        val predictedLabel = intent.getStringExtra("predictedLabel") ?: "No label available"
        val predictions = intent.getStringExtra("predictions") ?: "No predictions available"

        val imageView: ImageView = findViewById(R.id.imageView)
        val predictedLabelTextView: TextView = findViewById(R.id.predictedLabelTextView)
        val predictionsTextView: TextView = findViewById(R.id.predictionsTextView)

        predictedLabelTextView.text = "Predicted Label: $predictedLabel"
        predictionsTextView.text = "Predictions: $predictions"

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
    }
}
