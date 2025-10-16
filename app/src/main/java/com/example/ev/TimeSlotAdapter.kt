package com.example.ev

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlot>,
    private val onTimeSlotSelected: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.time_slot_item, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.bind(timeSlot, position == selectedPosition)
    }

    override fun getItemCount() = timeSlots.size

    fun setSelectedByTime(startTime: Date) {
        val position = timeSlots.indexOfFirst { it.startTime.time == startTime.time }
        if (position != -1) {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            onTimeSlotSelected(timeSlots[position])
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeSlotTextView: TextView = itemView.findViewById(R.id.timeSlotTextView)
        private val cardView: CardView = itemView.findViewById(R.id.timeSlotCardView)

        fun bind(timeSlot: TimeSlot, isSelected: Boolean) {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val startTime = timeFormat.format(timeSlot.startTime)
            val endTime = timeFormat.format(timeSlot.endTime)
            timeSlotTextView.text = "$startTime - $endTime"

            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.purple_500))
                timeSlotTextView.setTextColor(Color.WHITE)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
                timeSlotTextView.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                onTimeSlotSelected(timeSlot)
            }
        }
    }
}

// Simplified data class, as the adapter no longer needs to know if a slot is booked.
data class TimeSlot(val startTime: Date, val endTime: Date)
