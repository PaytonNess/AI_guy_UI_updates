package com.example.alguardianguyproject.chat

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.alguardianguyproject.R

class MessageItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val messageTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.message_item, this, true)
        messageTextView = findViewById(R.id.message_text)
        orientation = HORIZONTAL
        gravity = Gravity.START // Default to left alignment
    }

    fun setMessage(message: Message) {
        messageTextView.text = message.text

        if (message.isUser) {
            gravity = Gravity.END
            setBackgroundResource(R.drawable.user_message_background) // Example background color
            messageTextView.setTextColor(Color.WHITE) // Example text color
        } else {
            gravity = Gravity.START
            setBackgroundResource(R.drawable.model_message_background) // Example background color
            messageTextView.setTextColor(Color.WHITE) // Example text color
        }
    }
}
