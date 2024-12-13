package com.capstone.emoticalm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.emoticalm.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

class SettingsActivity : AppCompatActivity() {

    private lateinit var fullnameField: EditText
    private lateinit var emailField: EditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)
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

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        fullnameField = findViewById(R.id.fullnameField)
        emailField = findViewById(R.id.emailField)

        // Get current user information
        val userId = mAuth.currentUser?.uid

        // Fetch user data from Firestore
        if (userId != null) {
            db.collection("users")  // Ensure you're using the correct collection name
                .document(userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document: DocumentSnapshot? = task.result
                        if (document != null && document.exists()) {
                            // Retrieve data from Firestore
                            val fullName = document.getString("fullName")
                            val email = document.getString("email")

                            // Set the data in the UI
                            fullName?.let {
                                fullnameField.setText(it)
                            }
                            email?.let {
                                emailField.setText(it)
                            }
                        } else {
                            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Initialize Logout button
        val logoutButton: Button = findViewById(R.id.logoutButton)
        // Set OnClickListener for the logout button
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }
    private fun logoutUser() {
        // Sign out the user
        mAuth.signOut()

        // Show a Toast message indicating successful logout
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect the user to the login activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        // Finish the current activity so the user can't go back to it using the back button
        finish()
    }
}
