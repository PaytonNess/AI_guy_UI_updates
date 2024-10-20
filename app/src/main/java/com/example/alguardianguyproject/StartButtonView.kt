package com.example.alguardianguyproject

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner

class StartButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var startButton: Button
    var spinner: Spinner

    init {
        orientation = HORIZONTAL
        val inflater1 = LayoutInflater.from(context).inflate(R.layout.send_button, this)
        startButton = inflater1.findViewById(R.id.SendButton)
        spinner = inflater1.findViewById(R.id.my_spinner)
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val startButtonRect = Rect()
//        startButton.getHitRect(startButtonRect)
//
//        return if (startButtonRect.contains(event.x.toInt(), event.y.toInt())
//        ) {
//            startButton.performClick() // Handle the touch event normally for buttons
//            super.onTouchEvent(event) // Handle the touch event normally for buttons
//        } else {
//            println("ignored touch in start button view")
//            false // Ignore touch events outside of buttons
//        }
//    }
}