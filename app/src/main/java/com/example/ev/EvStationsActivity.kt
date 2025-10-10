package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EvStationsActivity : AppCompatActivity() {

    private lateinit var chargingStationRecyclerView: RecyclerView
    private lateinit var chargingStationAdapter: ChargingStationAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_stations)

        sessionManager = SessionManager(this)

        chargingStationRecyclerView = findViewById(R.id.chargingStationRecyclerView)
        chargingStationRecyclerView.layoutManager = LinearLayoutManager(this)

        chargingStationAdapter = ChargingStationAdapter(emptyList()) { station ->
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("STATION_ID", station.id)
            startActivity(intent)
        }
        chargingStationRecyclerView.adapter = chargingStationAdapter

        fetchChargingStations()
    }

    private fun fetchChargingStations() {
        RetrofitInstance.getApi(sessionManager).getChargingStations().enqueue(object : Callback<List<ChargingStation>> {
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val filteredStations = it.filter { station -> station.availableSlots > 0 && station.isActive }
                        chargingStationAdapter.updateStations(filteredStations)
                    }
                } else {
                    Log.e("EvStationsActivity", "Failed to fetch charging stations: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                Log.e("EvStationsActivity", "Failed to fetch charging stations", t)
            }
        })
    }
}