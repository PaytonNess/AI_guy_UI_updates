package com.sleepinggrizzly.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout

class MenuChatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var homeButton: Button
    var deleteConversation: Button
    var settingsButton: Button
    var humanAssistance: Button
    var buttonContainer: LinearLayout

    var opened = false

    init {
        orientation = HORIZONTAL
        val inflater1 = LayoutInflater.from(context).inflate(R.layout.menu_chat, this)
        homeButton = inflater1.findViewById(R.id.home)
        deleteConversation = inflater1.findViewById(R.id.delete_conversation)
        settingsButton = inflater1.findViewById(R.id.settings)
        humanAssistance = inflater1.findViewById(R.id.human_assistance)
        buttonContainer = inflater1.findViewById(R.id.buttonLayout)
    }

    fun hideButtons(height: Int) {
        if (height < 302) {
            deleteConversation.visibility = GONE
            settingsButton.visibility = GONE
            humanAssistance.visibility = GONE
        } else if (height < 452) {
            deleteConversation.visibility = VISIBLE
            settingsButton.visibility = GONE
            humanAssistance.visibility = GONE
        } else if (height < 602) {
            deleteConversation.visibility = VISIBLE
            settingsButton.visibility = VISIBLE
            humanAssistance.visibility = GONE
        } else {
            deleteConversation.visibility = VISIBLE
            settingsButton.visibility = VISIBLE
            humanAssistance.visibility = VISIBLE
        }
    }
}