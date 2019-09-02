package com.kslimweb.kotlinmessenger.views

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.models.ChatMessage
import com.kslimweb.kotlinmessenger.models.User
import com.kslimweb.kotlinmessenger.static.StaticVariable
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(private val chatMessage: ChatMessage) : Item<ViewHolder>() {

    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val chatPartnerID = if (chatMessage.fromID == FirebaseAuth.getInstance().uid) {
            chatMessage.toID
        } else {
            chatMessage.fromID
        }

        FirebaseFirestore.getInstance().collection(StaticVariable.USER_COLLECTION)
            .document(chatPartnerID).get().addOnCompleteListener{
                val userDocument = it.result
                if (userDocument != null) {
                    chatPartnerUser = userDocument.toObject(User::class.java)

                    viewHolder.itemView.latest_username_text_view.text = chatPartnerUser?.username
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.latest_message_profile_image_view)
                }
            }
        viewHolder.itemView.latest_message_text_view.text = chatMessage.text
    }
}