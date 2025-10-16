package com.example.ev

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class SlotNumberAdapter(
    private val slots: List<Int>,
    private val onSlotSelected: (Int) -> Unit
) : RecyclerView.Adapter<SlotNumberAdapter.SlotViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slot_number_item, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(slots[position], position == selectedPosition)
    }

    override fun getItemCount() = slots.size

    fun setSelectedSlot(slotNumber: Int) {
        val position = slots.indexOf(slotNumber)
        if (position != -1) {
            val previousSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    fun clearSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val slotNumberTextView: TextView = itemView.findViewById(R.id.slotNumberTextView)
        private val cardView: CardView = itemView.findViewById(R.id.slotCardView)

        fun bind(slotNumber: Int, isSelected: Boolean) {
            slotNumberTextView.text = "Slot $slotNumber"

            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.purple_500))
                slotNumberTextView.setTextColor(Color.WHITE)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
                slotNumberTextView.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedPosition)
                onSlotSelected(slotNumber)
            }
        }
    }
}