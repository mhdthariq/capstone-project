package com.capstone.emoticalm

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val historyList: MutableList<History>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.fullnameTextView.text = historyItem.fullname
        holder.emailTextView.text = historyItem.email
        holder.predictedLabelTextView.text = historyItem.predictedLabel

        try {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())

            val parsedDate = dateFormat.parse(historyItem.timestamp)
            // Check if the timestamp is parsed correctly
            if (parsedDate != null) {
                // Get device time
                val deviceCurrentTime = System.currentTimeMillis()

                // Format the device time using SimpleDateFormat for logging
                val deviceDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDeviceDate = deviceDateFormat.format(deviceCurrentTime)

                val formattedDate = SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault())
                    .format(parsedDate)
                holder.timeStamp.text = formattedDeviceDate
            } else {
                holder.timeStamp.text = "Invalid date"
            }
        } catch (e: ParseException) {
            holder.timeStamp.text = "Invalid date"
        }
        // Load the image using Glide and rotate it
        Glide.with(holder.imageView.context)
            .load(historyItem.image)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // Check if the resource is a BitmapDrawable
                    if (resource is BitmapDrawable) {
                        val bitmap = resource.bitmap
                        // Apply the rotation to the loaded bitmap (90 degrees in this case)
                        val rotatedBitmap = rotateImage(bitmap, -90f)
                        // Set the rotated bitmap as the ImageView's source
                        holder.imageView.setImageBitmap(rotatedBitmap)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle any cleanup if necessary
                }
            })

    }
    // Function to rotate the bitmap by the given angle
    private fun rotateImage(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)  // Rotate the image by the specified angle

        // Create a new bitmap from the original bitmap using the transformation
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullnameTextView: TextView = itemView.findViewById(R.id.fullnameTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val predictedLabelTextView: TextView = itemView.findViewById(R.id.predictedLabelTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val timeStamp: TextView = itemView.findViewById(R.id.timeStamp)
    }
}
