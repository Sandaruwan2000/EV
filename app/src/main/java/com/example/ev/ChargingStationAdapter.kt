package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChargingStationAdapter(
    private var stations: List<ChargingStation>
) : RecyclerView.Adapter<ChargingStationAdapter.ViewHolder>() {

    var onBookNowClicked: ((ChargingStation) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val locationTextView: TextView = view.findViewById(R.id.locationTextView)
        val typeTextView: TextView = view.findViewById(R.id.typeTextView)
        val availableSlotsTextView: TextView = view.findViewById(R.id.availableSlotsTextView)
        val bookNowButton: Button = view.findViewById(R.id.bookNowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.charging_station_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = stations[position]
        holder.nameTextView.text = station.name
        holder.locationTextView.text = station.location
        holder.typeTextView.text = "Type: ${station.type}"
        holder.availableSlotsTextView.text = "Available Slots: ${station.availableSlots}"

        holder.bookNowButton.setOnClickListener {
            onBookNowClicked?.invoke(station)
        }
    }

    override fun getItemCount() = stations.size

    fun updateStations(newStations: List<ChargingStation>) {
        stations = newStations
        notifyDataSetChanged()
    }
}