package com.example.myapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import com.example.myapp.models.User
import com.example.myapp.models.UserResponse

class MainActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            registerUser()
        }

        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val allPrefs = sharedPref.all
        Log.d("SharedPreferences", "All preferences: $allPrefs")

        // Uncomment this line to clear SharedPreferences for debugging
        // clearSharedPreferences()

        if (isUserAuthenticated()) {
            navigateToMainPage()
        }
    }

    private fun registerUser() {
        val username = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(username, email, password)

        val apiService = RetrofitClient.getClient().create(ApiService::class.java)
        val call = apiService.registerUser(user)

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()!!
                    saveUserToPreferences(user)
                    Toast.makeText(this@MainActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                    navigateToMainPage()
                } else {
                    Toast.makeText(this@MainActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Registration failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserToPreferences(user: User) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", user.username)
            putString("email", user.email)
            putString("password", user.password)
            apply()
        }
        Log.d("SharedPreferences", "Saved user: ${user.username}, ${user.email}, ${user.password}")
    }

    private fun isUserAuthenticated(): Boolean {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        return username != null
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearInputFields() {
        nameEditText.text = null
        emailEditText.text = null
        passwordEditText.text = null
    }

    private fun clearSharedPreferences() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        Log.d("SharedPreferences", "All preferences cleared")
    }
}
