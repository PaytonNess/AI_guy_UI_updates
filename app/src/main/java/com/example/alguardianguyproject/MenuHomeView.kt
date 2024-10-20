package com.example.alguardianguyproject

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout

class MenuHomeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var reviewSomethingButton: Button
    var mediaTypeSpinner: Spinner
    var savedConversations: Button
    var settingsButton: Button
    var humanAssistance: Button
    var buttonContainer: LinearLayout

    var opened = false

    init {
        orientation = HORIZONTAL
        clipChildren = false
        clipToOutline = false
        clipToPadding = false

        val inflater1 = LayoutInflater.from(context).inflate(R.layout.menu_home, this)
        reviewSomethingButton = inflater1.findViewById(R.id.review_something)
        mediaTypeSpinner = inflater1.findViewById(R.id.my_spinner)
        savedConversations = inflater1.findViewById(R.id.saved_conversations)
        settingsButton = inflater1.findViewById(R.id.settings)
        humanAssistance = inflater1.findViewById(R.id.human_assistance)
        buttonContainer = inflater1.findViewById(R.id.buttonLayout)
    }

    fun hideButtons(height: Int) {
        if (height < 302){
            mediaTypeSpinner.visibility = GONE
            savedConversations.visibility = GONE
            settingsButton.visibility = GONE
            humanAssistance.visibility = GONE
        }
        else if (height < 452){
            mediaTypeSpinner.visibility = VISIBLE
            savedConversations.visibility = GONE
            settingsButton.visibility = GONE
            humanAssistance.visibility = GONE
        }
        else if (height < 602){
            mediaTypeSpinner.visibility = VISIBLE
            savedConversations.visibility = VISIBLE
            settingsButton.visibility = GONE
            humanAssistance.visibility = GONE
        }
        else if (height < 752){
            mediaTypeSpinner.visibility = VISIBLE
            savedConversations.visibility = VISIBLE
            settingsButton.visibility = VISIBLE
            humanAssistance.visibility = GONE
        }
        else {
            mediaTypeSpinner.visibility = VISIBLE
            savedConversations.visibility = VISIBLE
            settingsButton.visibility = VISIBLE
            humanAssistance.visibility = VISIBLE
        }
    }
}