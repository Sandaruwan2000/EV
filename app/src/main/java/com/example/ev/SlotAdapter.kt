package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SlotAdapter(
    private val totalSlots: Int,
    private val bookedSlots: List<Int>,
    private val onSlotClicked: (Int) -> Unit
) : RecyclerView.Adapter<SlotAdapter.SlotViewHolder>() {

    private var selectedSlot = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slot_item, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slotNumber = position + 1
        holder.slotInfoTextView.text = "Slot $slotNumber"

        when {
            slotNumber == selectedSlot -> {
                holder.itemView.setBackgroundResource(R.drawable.slot_selected_background)
            }
            bookedSlots.contains(slotNumber) -> {
                holder.itemView.setBackgroundResource(R.drawable.slot_booked_background)
                holder.itemView.isClickable = false
            }
            else -> {
                holder.itemView.setBackgroundResource(R.drawable.slot_available_background)
                holder.itemView.setOnClickListener {
                    val previouslySelected = selectedSlot
                    selectedSlot = slotNumber
                    notifyItemChanged(previouslySelected - 1)
                    notifyItemChanged(selectedSlot - 1)
                    onSlotClicked(slotNumber)
                }
            }
        }
    }

    override fun getItemCount() = totalSlots

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val slotInfoTextView: TextView = itemView.findViewById(R.id.slotInfoTextView)
    }
}