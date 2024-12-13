package com.capstone.emoticalm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.emoticalm.databinding.ActivityHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyList: MutableList<History>
    private lateinit var historyAdapter: HistoryAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHistoryBinding.inflate(layoutInflater)
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
        // Initialize RecyclerView and List
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyList = mutableListOf()

        // Setup the RecyclerView adapter
        historyAdapter = HistoryAdapter(historyList)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Fetch history data from Firestore
        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        db.collection("history") // Assuming your collection is called "history"
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                if (!result.isEmpty) {
                    for (document in result) {
                        val historyItem = document.toObject(History::class.java)

                        // Get Firestore timestamp
                        val serverTimestamp = historyItem.timestamp
                        val firestoreDateFormat =
                            SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                        Log.d("History", "Timestamp from Firestore: ${historyItem.timestamp}")

                        // Parse the timestamp from Firestore into a Date object
                        val parsedServerDate = firestoreDateFormat.parse(serverTimestamp)

                        // Get device time
                        val deviceCurrentTime = System.currentTimeMillis()

                        // Format the device time using SimpleDateFormat for logging
                        val deviceDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val formattedDeviceDate = deviceDateFormat.format(deviceCurrentTime)

                        Log.d("History", "Timestamp from Device: $formattedDeviceDate") // Log the formatted device date

                        // Compare server time with device time (allow a small window for differences)
                        if (parsedServerDate != null && Math.abs(deviceCurrentTime - parsedServerDate.time) < 5000) {
                            // If time difference is less than 5 seconds, it's likely from the server
                            Log.d("Server", "Time is from server")
                            println("Time is from server")
                        } else {
                            Log.d("Device", "Time is from Device")
                            // Otherwise, it's from the user's device
                            println("Time is from device")
                        }

                        historyList.add(historyItem)
                    }
                    val firestoreDateFormat =
                        SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                    // Sort the history list by timestamp
                    historyList.sortWith { h1, h2 ->
                        val date1 = firestoreDateFormat.parse(h1.timestamp)
                        val date2 = firestoreDateFormat.parse(h2.timestamp)
                        date2?.compareTo(date1) ?: 0
                    }

                    historyAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching data: $e", Toast.LENGTH_SHORT).show()
            }
    }

}

//    private fun fetchHistoryData() {
//        db.collection("history") // Assuming your collection is called "history"
//            .get()
//            .addOnSuccessListener { result: QuerySnapshot ->
//                if (!result.isEmpty) {
//                    for (document in result) {
//                        val historyItem = document.toObject(History::class.java)
//                        historyList.add(historyItem)
//                    }
//                    // Sort the history list by timestamp
//                    val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
//                    historyList.sortWith { h1, h2 ->
//                        val date1 = dateFormat.parse(h1.timestamp)
//                        val date2 = dateFormat.parse(h2.timestamp)
//                        date2?.compareTo(date1) ?: 0
//                    }
//                    historyAdapter.notifyDataSetChanged()
//                } else {
//                    Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error fetching data: $e", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
