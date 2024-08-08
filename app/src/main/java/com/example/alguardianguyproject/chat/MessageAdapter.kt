package com.example.alguardianguyproject.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alguardianguyproject.R

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>(){
    private var messages: List<Message> = emptyList() // Initially empty

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageItemView: MessageItemView = itemView.findViewById(R.id.message_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false) // Use your message item layout
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageItemView.setMessage(message)
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        this.messages = newMessages
        notifyDataSetChanged() // Tell the adapter to refresh
    }
}