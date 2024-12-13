package com.capstone.emoticalm

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.capstone.emoticalm.databinding.ActivityRegisterBinding

import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import java.security.MessageDigest
import java.security.SecureRandom

class RegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {

    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        mBinding = ActivityRegisterBinding.inflate(LayoutInflater.from(this))
        setContentView(mBinding.root)
        mBinding.fullNameTil.onFocusChangeListener = this
        mBinding.emailTil.onFocusChangeListener = this
        mBinding.passwordTil.onFocusChangeListener = this
        mBinding.cPasswordTil.onFocusChangeListener = this
        mBinding.registerBtn.setOnClickListener(this)

        val goToLoginBtn: MaterialButton = findViewById(R.id.GoTologinBtn)
        goToLoginBtn.setOnClickListener {
            // Create an intent to navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        mBinding.registerBtn.setOnClickListener {
            if (validateForm()) {
                registerUser(
                    mBinding.emailEt.text.toString(),
                    mBinding.passwordEt.text.toString()
                )
            }
        }
    }
    private fun validateForm(): Boolean {
        return validateFullName() && validateEmail() && validatePassword() && validatePasswordAndConfirmPassword()
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Save user data to Firestore if user is successfully registered
                    user?.let {
                        saveUserToFirestore(it)
                    }
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    // Navigate to Login or Main Activity
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun saveUserToFirestore(user: FirebaseUser) {
        val fullName = mBinding.fullNameEt.text.toString()
        val email = mBinding.emailEt.text.toString()
        val password = mBinding.passwordEt.text.toString()

        // You should hash the password here before storing it
        val hashedPassword = hashPassword(password) // Use a function to hash the password

        // Create a map of user data
        val userMap = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "userId" to user.uid,
            "password" to hashedPassword
        )

        // Get an instance of Firestore
        val db = FirebaseFirestore.getInstance()

        // Save the user data to Firestore under a collection "users"
        db.collection("users")
            .document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                // Data saved successfully
                Log.d("RegisterActivity", "User data saved to Firestore")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.w("RegisterActivity", "Error saving user data", e)
            }
    }

    // A function to hash the password (you can use any hashing algorithm you prefer)
    private fun hashPassword(password: String): String {
        // Generate a random salt
        val salt = generateSalt()

        // Combine password and salt
        val saltedPassword = password + salt

        // Hash the salted password using SHA-256
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))

        // Convert the hash bytes to Base64 to make it easier to store
        val hashedPassword = Base64.encodeToString(hashBytes, Base64.DEFAULT)

        // Combine the salt and the hashed password and return it
        return "$salt:$hashedPassword"
    }

    // Function to generate a random salt
    private fun generateSalt(): String {
        val salt = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT).trim()
    }

    private fun validateFullName():Boolean{
        var errorMessage: String? = null
        val value: String = mBinding.fullNameEt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Fullname is required"
        }
        if (errorMessage != null){
            mBinding.fullNameTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }
        return errorMessage == null
    }
    private fun validateEmail():Boolean{
        var errorMessage: String? = null
        val value = mBinding.emailEt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Email is required"
        }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            errorMessage = "Email is invalid"
        }
        if (errorMessage != null){
            mBinding.emailTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }
        return errorMessage == null
    }

    private fun validatePassword():Boolean{
        var errorMessage: String? = null
        val value = mBinding.passwordEt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Password is required"
        }else if (value.length < 6){
            errorMessage = "Password must be 6 characters long"
        }
        if (errorMessage != null){
            mBinding.passwordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }
        return errorMessage == null
    }

    private fun validateConfirmPassword():Boolean{
        var errorMessage: String? = null
        val value = mBinding.cPasswordEt.text.toString()
        if (value.isEmpty()){
            errorMessage = "Confirm Password is required"
        }else if (value.length < 6){
            errorMessage = "Confirm Password must be 6 characters long"
        }
        if (errorMessage != null){
            mBinding.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }
        return errorMessage == null
    }

    private fun validatePasswordAndConfirmPassword():Boolean{
        var errorMessage: String? = null
        val password = mBinding.passwordEt.text.toString()
        val confirmPassword = mBinding.cPasswordEt.text.toString()
        if (password!= confirmPassword){
            errorMessage = "Password does not match"
        }
        if (errorMessage != null){
            mBinding.cPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }
        return errorMessage == null
    }

    override fun onClick(view: View?) {
        TODO("Not yet implemented")
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if(view!=null){
            when(view.id){
                R.id.fullNameEt ->{
                    if(hasFocus){
                        if(mBinding.fullNameTil.isErrorEnabled){
                            mBinding.fullNameTil.isErrorEnabled = false
                        }
                    }else{
                        validateFullName()
                    }
                }
                R.id.emailEt ->{
                    if(hasFocus){
                        if(mBinding.emailTil.isErrorEnabled){
                            mBinding.emailTil.isErrorEnabled = false
                        }
                    }else{
                        if(validateEmail()){
//                            Do something
                        }
                    }
                }
                R.id.passwordEt ->{
                    if(hasFocus){
                        if(mBinding.passwordTil.isCounterEnabled){
                            mBinding.passwordTil.isErrorEnabled = false
                        }
                    }else{
                        if(validatePassword() && mBinding.cPasswordEt.text!!.isNotEmpty() && validateConfirmPassword()){
                            if (mBinding.cPasswordTil.isErrorEnabled){
                                mBinding.cPasswordTil.isErrorEnabled = false
                            }
                            mBinding.cPasswordTil.apply {
                                setStartIconDrawable(R.drawable.check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
                R.id.cPasswordEt ->{
                    if (hasFocus){
                        if(mBinding.cPasswordTil.isCounterEnabled){
                            mBinding.cPasswordTil.isErrorEnabled = false
                        }
                    }else{
                        if(validateConfirmPassword()&&validatePassword()&&validatePasswordAndConfirmPassword()){
                            if (mBinding.passwordTil.isErrorEnabled){
                                mBinding.passwordTil.isErrorEnabled = false
                            }
                            mBinding.cPasswordTil.apply {
                                setStartIconDrawable(R.drawable.check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onKey(view: View?, event: Int, keyEvent: KeyEvent): Boolean {
        return false
    }
}