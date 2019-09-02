package com.kslimweb.kotlinmessenger.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.views.NewUserMessageRow
import com.kslimweb.kotlinmessenger.models.User
import com.kslimweb.kotlinmessenger.static.StaticVariable
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    private val TAG = NewMessageActivity::class.java.simpleName
    companion object {
        const val USER_KEY = "SELECTED_USER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers() {
        val usersCollection = FirebaseFirestore.getInstance().collection(StaticVariable.USER_COLLECTION)
        usersCollection.get().addOnSuccessListener {
            val adapter = GroupAdapter<ViewHolder>()
            for (document in it.documents) {

                val user = document.toObject(User::class.java)

                // skip over current user document and continue loop
                // prevent it to display current user as available user to send message
                if(user?.username == FirebaseAuth.getInstance().currentUser?.displayName) {
                    continue
                }

                user?.let {
                    adapter.add(NewUserMessageRow(user))
                    Log.d(TAG, user.username)
                    Log.d(TAG, user.profileImageUrl)
                }
            }

            adapter.setOnItemClickListener{item, view ->
                val userItem = item as NewUserMessageRow
                val intent = Intent(view.context, ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, userItem.user)
                startActivity(intent)
                finish()
            }
            new_message_recycler_view.adapter = adapter
        }
    }
}
