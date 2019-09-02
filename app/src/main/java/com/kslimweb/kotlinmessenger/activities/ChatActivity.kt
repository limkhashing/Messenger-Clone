package com.kslimweb.kotlinmessenger.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.activities.NewMessageActivity.Companion.USER_KEY
import com.kslimweb.kotlinmessenger.models.ChatMessage
import com.kslimweb.kotlinmessenger.models.User
import com.kslimweb.kotlinmessenger.registerlogin.RegisterActivity
import com.kslimweb.kotlinmessenger.static.StaticVariable
import com.kslimweb.kotlinmessenger.views.LatestMessageRow
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private val TAG = ChatActivity::class.java.simpleName
    private val adapter = GroupAdapter<ViewHolder>()
    private val latestMessageMap = HashMap<String, ChatMessage>()

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.setIcon(R.drawable.ic_lock_black_24dp)

        latest_messages_recycler_view.adapter = adapter
        latest_messages_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        verifyUserLogin()
        fetchCurrentUser()
        listenForMessage()

        // set item listener for latest messages recycler view adapter
        adapter.setOnItemClickListener{ item, view ->
            Log.d(TAG, "Success")
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
    }

    private fun refreshRecyclerViewMessage() {
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForMessage() {
        val fromID = FirebaseAuth.getInstance().uid ?: return

        // TODO for now, there is no way to retrieve all subcollection
        // will come back to here in future if Firebase updates query engine
        val latestMessageSubCollectionReference =
            FirebaseFirestore.getInstance().collection("latest-messages").document(fromID).collection("RzzUf3VcF5RpbHRaOpOVzjhF5HA3")

        latestMessageSubCollectionReference.addSnapshotListener{ querySnapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                for (documentSnapshot in querySnapshot.documents) {
                    val chatMessage = documentSnapshot.toObject(ChatMessage::class.java)

                    latestMessageMap[documentSnapshot.id] = chatMessage!!
//                adapter.add(LatestMessageRow(chatMessage))
                    refreshRecyclerViewMessage()
                }
            }

        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        FirebaseFirestore.getInstance().collection(StaticVariable.USER_COLLECTION)
            .document(uid).get().addOnCompleteListener{
                val currentUserDocument = it.result
                if (currentUserDocument != null) {
                    currentUser = currentUserDocument.toObject(User::class.java)
                }
            }
    }

    private fun verifyUserLogin() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
