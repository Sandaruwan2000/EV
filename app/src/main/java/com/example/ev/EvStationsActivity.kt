package com.example.ev

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EvStationsActivity : BaseActivity() {

    private lateinit var stationRecyclerView: RecyclerView
    private lateinit var stationAdapter: ChargingStationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ev_stations)

        stationRecyclerView = findViewById(R.id.stationRecyclerView)
        stationRecyclerView.layoutManager = LinearLayoutManager(this)

        stationAdapter = ChargingStationAdapter(emptyList()) { station ->
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("STATION_ID", station.id)
            startActivity(intent)
        }
        stationRecyclerView.adapter = stationAdapter

        fetchChargingStations()
    }

    private fun fetchChargingStations() {
        ApiClient.authenticatedApi.getChargingStations().enqueue(object : Callback<List<ChargingStation>> {
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        stationAdapter.updateStations(it)
                    }
                } else {
                    Toast.makeText(this@EvStationsActivity, "Failed to fetch stations", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                Toast.makeText(this@EvStationsActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}