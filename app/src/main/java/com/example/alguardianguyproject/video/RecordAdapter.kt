package com.example.alguardianguyproject.video

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.example.alguardianguyproject.R

class RecordAdapter(private val completedStages: List<Int>, private val progress: Double) :
    RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rowTextView: TextView = itemView.findViewById(R.id.rowTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

        init {
            // Set the background here
            itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.loading_row_background)

            rowTextView.gravity = Gravity.CENTER_HORIZONTAL
            rowTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            rowTextView.typeface = ResourcesCompat.getFont(itemView.context, R.font.outfit_medium)
            rowTextView.textSize = 21f

            imageView.layoutParams.width = 55
            imageView.layoutParams.height = 55

            // Add margins to itemView
            val layoutParams = itemView.layoutParams as ViewGroup.MarginLayoutParams
            val marginInPixels = 16 // Replace with your desired margin in pixels
            layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels)
            itemView.layoutParams = layoutParams
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false) // Create item_record.xml for the row layout
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rowTextView.text = getRowText(position)
        val imageResource = if (completedStages[position] == 1) {
            R.drawable.radio_closed
        } else {
            R.drawable.radio_open
        }
        holder.imageView.setImageResource(imageResource)
    }

    override fun getItemCount(): Int = 7 // Replace with actual number of rows

    private fun getRowText(row: Int): String {
        return when (row) {
            0 -> "Uploading Video"
            1 -> "Uploading Audio"
            2 -> "Transcribing"
            3 -> "Deleting Video From Server"
            4 -> "Deleting Audio From Server"
            5 -> "Deleting Video From Device"
            6 -> "Deleting Audio From Device"
            else -> {""}
        }
    }
}