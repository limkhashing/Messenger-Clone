package com.kslimweb.kotlinmessenger.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.activities.NewMessageActivity.Companion.USER_KEY
import com.kslimweb.kotlinmessenger.models.ChatMessage
import com.kslimweb.kotlinmessenger.models.User
import com.kslimweb.kotlinmessenger.views.ChatFromRow
import com.kslimweb.kotlinmessenger.views.ChatToItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private val TAG = this::class.java.simpleName
    private val adapter = GroupAdapter<ViewHolder>()
    private var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        // get clicked user
        toUser = intent.getParcelableExtra(USER_KEY)
        recycler_view_chat_log.adapter = adapter

        supportActionBar?.title = toUser?.username

        listenForMessage()
        send_message.hideKeyboard()
        send_message.setOnClickListener{
            performSendMessage()
        }
    }

    private fun listenForMessage() {
        val fromID= FirebaseAuth.getInstance().uid ?: return
        val toID = toUser!!.uid
        val messageCollectionReference = FirebaseFirestore.getInstance().collection("user-messages").document(fromID).collection(toID)

        // listen for document snapshot and if new message sent it will refresh the screen
        messageCollectionReference.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                // get every previous message before if got
                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val chatMessages =  dc.document.toObject(ChatMessage::class.java)

                        if (chatMessages.fromID == FirebaseAuth.getInstance().uid) {
                            val currentUser = ChatActivity.currentUser ?: return@addSnapshotListener
                            adapter.add(ChatToItem(chatMessages.text, currentUser))
                            Log.d(TAG, "Sent message:${chatMessages.toID}")
                        } else {
                            adapter.add(ChatFromRow(chatMessages.text, toUser!!))
                            Log.d(TAG, "received message:${chatMessages.fromID}")
                        }
                    }
                }
            }
            recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun performSendMessage() {
        // send message to firebase
        val fromID = FirebaseAuth.getInstance().uid ?: return

        val toID = toUser?.uid ?: return
        val text = message_edit_text.text.toString()

        val fromReference = FirebaseFirestore.getInstance().collection("user-messages").document(fromID).collection(toID)
        val toReference = FirebaseFirestore.getInstance().collection("user-messages").document(toID).collection(fromID)

        val latestMessageFromReference = FirebaseFirestore.getInstance().collection("latest-messages").document(fromID).collection(toID)
        val latestMessageToReference = FirebaseFirestore.getInstance().collection("latest-messages").document(toID).collection(fromID)

        val chatMessage = ChatMessage(
            text,
            fromReference.document().id,
            fromID,
            toID,
            System.currentTimeMillis() / 1000
        )

        fromReference.add(chatMessage).addOnCompleteListener{
            message_edit_text.text.clear()
            recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
        }

        toReference.add(chatMessage)

        latestMessageFromReference.document(toID).set(chatMessage)
        latestMessageToReference.document(fromID).set(chatMessage)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
