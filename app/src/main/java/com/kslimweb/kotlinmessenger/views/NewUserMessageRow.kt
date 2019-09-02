package com.kslimweb.kotlinmessenger.views

import com.kslimweb.kotlinmessenger.R
import com.kslimweb.kotlinmessenger.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.new_message_row.view.*

class NewUserMessageRow(val user: User): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.new_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // will be called in our list for each user object
        viewHolder.itemView.new_message_username.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.new_user_message_image)
    }

}