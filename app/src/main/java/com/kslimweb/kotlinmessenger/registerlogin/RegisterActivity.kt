package com.kslimweb.kotlinmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*
import com.google.firebase.storage.FirebaseStorage
import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.activities.ChatActivity
import com.kslimweb.kotlinmessenger.models.User
import com.kslimweb.kotlinmessenger.static.StaticVariable


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = RegisterActivity::class.java.simpleName
    var selectedPhotoUri: Uri? = null
    var selectedPhoto = false

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        currentUser?.let {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        register_button.setOnClickListener {
            if(validateInputField()) {
                val userName = username_register.text.toString()
                val email = email_register.text.toString()
                val password = password_register.text.toString()

                registerFirebase(userName, email, password)
            }
        }

        had_account.setOnClickListener {
           startActivity(Intent(this, LoginActivity::class.java))
        }

        photo_register.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 200)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            Log.d("main", selectedPhotoUri.toString())

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(this.resources, bitmap)
            photo_register.background = bitmapDrawable
            photo_register.text = ""

            selectedPhoto = true
        }
    }

    private fun validateInputField(): Boolean {

        val userName = username_register.text.toString().trim()
        val email = email_register.text.toString().trim()
        val password = password_register.text.toString().trim()

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Input field is empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if(!email.isValidEmail()) {
            Toast.makeText(this, "Email is not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        if(!selectedPhoto) {
            Toast.makeText(this, "Please select profile image", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun String.isValidEmail(): Boolean
            = this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun registerFirebase(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(userName).build()

                    user!!.updateProfile(profileUpdates).addOnCompleteListener {
                        if (task.isSuccessful) {
                            Log.d(TAG, "User profile updated.")
                        }
                    }

                    uploadImageToFirebaseStorage(user)

                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun uploadImageToFirebaseStorage(user: FirebaseUser) {

        selectedPhoto.let {
            val filename = user.uid
            val storage = FirebaseStorage.getInstance().getReference("/images/$filename")

            storage.putFile(selectedPhotoUri!!).addOnSuccessListener {
                Log.d(TAG, "Uploaded user iamge")

                storage.downloadUrl.addOnSuccessListener {
                    saveUserToDatabase(it.toString())
                }
            }
        }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val username = username_register.text.toString()
        val db = FirebaseFirestore.getInstance()

        val user = User(uid, username, profileImageUrl)

        db.collection(StaticVariable.USER_COLLECTION).document(uid).set(user).addOnCompleteListener{
            Log.d(TAG, "Saved users info to firestore")

            val intent = Intent(this, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
          //  startActivity(intent)

        }.addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
    }
}
