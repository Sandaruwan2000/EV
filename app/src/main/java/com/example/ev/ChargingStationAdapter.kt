package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChargingStationAdapter(
    private var stations: List<ChargingStation>,
    private val onItemClicked: (ChargingStation) -> Unit
) : RecyclerView.Adapter<ChargingStationAdapter.ChargingStationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChargingStationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.charging_station_item, parent, false)
        return ChargingStationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChargingStationViewHolder, position: Int) {
        val station = stations[position]
        holder.stationNameTextView.text = station.name
        holder.locationTextView.text = station.location
        holder.evTypeTextView.text = "Type: ${station.type}"
        holder.availableSlotsTextView.text = "Available Slots: ${station.availableSlots}"

        // Use a placeholder image since the API doesn't provide an image URL
        holder.stationImageView.setImageResource(R.drawable.placeholder_image)

        holder.itemView.setOnClickListener {
            onItemClicked(station)
        }
    }

    override fun getItemCount() = stations.size

    fun updateStations(newStations: List<ChargingStation>) {
        stations = newStations
        notifyDataSetChanged()
    }

    class ChargingStationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationImageView: ImageView = itemView.findViewById(R.id.stationImageView)
        val stationNameTextView: TextView = itemView.findViewById(R.id.stationNameTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val evTypeTextView: TextView = itemView.findViewById(R.id.evTypeTextView)
        val availableSlotsTextView: TextView = itemView.findViewById(R.id.availableSlotsTextView)
    }
}