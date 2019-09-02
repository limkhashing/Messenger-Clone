package com.kslimweb.kotlinmessenger.views

import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*

class ChatFromRow(val text: String, val user: User) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.from_profile_image_view)
        viewHolder.itemView.from_message.text = text
    }
}