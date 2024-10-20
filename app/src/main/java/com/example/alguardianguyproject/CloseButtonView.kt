package com.example.alguardianguyproject

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout

class CloseButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var closeButton: Button

    init {
        orientation = HORIZONTAL
        val inflater = inflate(context, R.layout.close_button, this)
        closeButton = inflater.findViewById(R.id.CloseButton)
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val closeButtonRect = Rect()
//        closeButton.getHitRect(closeButtonRect)
//
//        return if (closeButtonRect.contains(event.x.toInt(), event.y.toInt())
//        ) {
//            println("close button pressed")
//            super.onTouchEvent(event)
//            performClick() // Handle the touch event normally for buttons
//        } else {
//            false // Ignore touch events outside of buttons
//        }
//    }
//    override fun performClick(): Boolean {
//        super.performClick()
//        return true
//    }

}