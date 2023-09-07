package com.example.socialapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter (var messages : List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val messageText : TextView = itemView.findViewById(R.id.message_item_title)
        val messageSender : TextView = itemView.findViewById(R.id.message_item_sender)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view :View = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageAdapter.MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.messageText
        holder.messageSender.text = message.messageSender
        if (holder.messageSender.text.toString() == TopicActivity.userNickname){
            holder.messageSender.setTextColor(Color.parseColor("#3a0ca3"))
            holder.messageText.setTextColor(Color.parseColor("#f72585"))
        }
    }

}