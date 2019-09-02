package com.kslimweb.kotlinmessenger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.activities.ChatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = LoginActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        login_button.setOnClickListener{
            val email = email_login.text.toString().trim()
            val password = password_login.text.toString().trim()

            if(validateLoginField(email, password)) {
                loginUser(email, password)
            }
        }

        back_to_register.setOnClickListener{
            onBackPressed()
        }

    }

    private fun validateLoginField(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Input field is empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if(!email.isValidEmail()) {
            Toast.makeText(this, "Email is not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun String.isValidEmail(): Boolean
            = this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmailAndPassword:success")
                    val user = auth.currentUser

                    Log.d(TAG, "Name :" + auth.currentUser?.displayName)

                    updateUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.exception)
                }
            }
    }

    private fun updateUI() {
        // proceed to chat screen
        Log.d(TAG, "Proceed to chat screen")
        startActivity(Intent(this, ChatActivity::class.java))
    }
}
