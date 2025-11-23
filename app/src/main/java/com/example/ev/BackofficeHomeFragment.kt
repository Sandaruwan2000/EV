package com.example.ev

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ev.StationBookingsResponse // Correctly import the data class
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class BackofficeHomeFragment : Fragment() {

    // Views for the dashboard
    private lateinit var stationNameTextView: TextView
    private lateinit var todayBookingCountTextView: TextView
    private lateinit var currentHourBookingCountTextView: TextView
    private lateinit var pendingCountTextView: TextView
    private lateinit var confirmedCountTextView: TextView
    private lateinit var cancelledCountTextView: TextView
    private lateinit var qrScanImageButton: ImageButton
    private lateinit var bookingSearchView: SearchView
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var noStationSelectedTextView: TextView
    private lateinit var noBookingsTextView: TextView
    private lateinit var adapter: StationBookingsAdapter

    private var allBookings: List<BookingDetails> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_backoffice_home, container, false)

        // Initialize all views
        stationNameTextView = view.findViewById(R.id.stationNameTextView)
        todayBookingCountTextView = view.findViewById(R.id.todayBookingCountTextView)
        currentHourBookingCountTextView = view.findViewById(R.id.currentHourBookingCountTextView)
        pendingCountTextView = view.findViewById(R.id.pendingCountTextView)
        confirmedCountTextView = view.findViewById(R.id.confirmedCountTextView)
        cancelledCountTextView = view.findViewById(R.id.cancelledCountTextView)
        qrScanImageButton = view.findViewById(R.id.qrScanImageButton)
        bookingSearchView = view.findViewById(R.id.bookingSearchView)
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView)
        noStationSelectedTextView = view.findViewById(R.id.noStationSelectedTextView)
        noBookingsTextView = view.findViewById(R.id.noBookingsTextView)

        setupRecyclerView()
        setupSearchView()
        setupClickListeners()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadStationBookings()
    }

    private fun setupRecyclerView() {
        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = StationBookingsAdapter(emptyList()) { booking ->
            navigateToQrScan(booking.id)
        }
        bookingsRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        bookingSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBookings(newText)
                return true
            }
        })
    }

    private fun setupClickListeners() {
        qrScanImageButton.setOnClickListener {
            navigateToQrScan()
        }
    }

    private fun navigateToQrScan(bookingId: String? = null) {
        // TODO: Re-enable navigation once the nav graph is configured
        // val action = BackofficeHomeFragmentDirections.actionBackofficeHomeFragmentToQrScanFragment(bookingId)
        // findNavController().navigate(action)
        Toast.makeText(requireContext(), "Navigation not configured yet.", Toast.LENGTH_SHORT).show()
    }

    private fun filterBookings(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allBookings
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            allBookings.filter {
                it.id.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                it.status.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                it.vehicleNumber?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true
            }
        }
        adapter.updateBookings(filteredList)
    }

    private fun loadStationBookings() {
        val stationId = SessionManager.getSelectedStationId()
        val stationName = SessionManager.getSelectedStationName()

        if (stationId == null) {
            noStationSelectedTextView.visibility = View.VISIBLE
            bookingsRecyclerView.visibility = View.GONE
            noBookingsTextView.visibility = View.GONE
            bookingSearchView.visibility = View.GONE
            qrScanImageButton.visibility = View.GONE
            stationNameTextView.text = "No Station Selected"
            return
        }

        stationNameTextView.text = "Station: $stationName"
        qrScanImageButton.visibility = View.VISIBLE

        ApiClient.authenticatedApi.getBookingsForStation(stationId).enqueue(object : Callback<StationBookingsResponse> {
            override fun onResponse(call: Call<StationBookingsResponse>, response: Response<StationBookingsResponse>) {
                if (response.isSuccessful) {
                    allBookings = response.body()?.data ?: emptyList()
                    updateDashboard(allBookings)
                    bookingSearchView.visibility = if (allBookings.isNotEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch bookings: ${response.message()}", Toast.LENGTH_LONG).show()
                    noBookingsTextView.visibility = View.VISIBLE
                    bookingsRecyclerView.visibility = View.GONE
                    bookingSearchView.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<StationBookingsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "An error occurred: ${t.message}", Toast.LENGTH_LONG).show()
                noBookingsTextView.visibility = View.VISIBLE
                bookingsRecyclerView.visibility = View.GONE
                bookingSearchView.visibility = View.GONE
            }
        })
    }

    private fun updateDashboard(bookings: List<BookingDetails>) {
        if (bookings.isEmpty()) {
            noBookingsTextView.visibility = View.VISIBLE
            bookingsRecyclerView.visibility = View.GONE
            noStationSelectedTextView.visibility = View.GONE
            // Clear stats
            todayBookingCountTextView.text = "0"
            currentHourBookingCountTextView.text = "0"
            pendingCountTextView.text = "0"
            confirmedCountTextView.text = "0"
            cancelledCountTextView.text = "0"
            adapter.updateBookings(emptyList()) // Clear the adapter
            return
        }

        bookingsRecyclerView.visibility = View.VISIBLE
        noBookingsTextView.visibility = View.GONE
        noStationSelectedTextView.visibility = View.GONE

        // --- Calculate Statistics ---
        val today = Calendar.getInstance()
        val currentHour = today.get(Calendar.HOUR_OF_DAY)
        val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val todayBookings = bookings.filter { 
            val timeStr = it.startTime ?: it.reservationDateTime
            if (timeStr.isNullOrEmpty() || timeStr.startsWith("0001")) return@filter false
            try {
                val date = apiDateFormat.parse(timeStr)
                if (date != null) {
                    val bookingCalendar = Calendar.getInstance().apply { time = date }
                    bookingCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    bookingCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                } else { false }
            } catch (e: Exception) { false }
        }

        val currentHourBookings = todayBookings.filter { 
            val timeStr = it.startTime ?: it.reservationDateTime
             if (timeStr.isNullOrEmpty()) return@filter false
            try {
                val date = apiDateFormat.parse(timeStr)
                if (date != null) {
                    val bookingCalendar = Calendar.getInstance().apply { time = date }
                    bookingCalendar.get(Calendar.HOUR_OF_DAY) == currentHour
                } else { false }
            } catch (e: Exception) { false }
        }

        val pendingCount = bookings.count { it.status.equals("Pending", ignoreCase = true) }
        val confirmedCount = bookings.count { it.status.equals("Confirmed", ignoreCase = true) }
        val cancelledCount = bookings.count { it.status.equals("Cancelled", ignoreCase = true) || it.status.equals("Rejected", ignoreCase = true) }

        // --- Update UI ---
        todayBookingCountTextView.text = todayBookings.size.toString()
        currentHourBookingCountTextView.text = currentHourBookings.size.toString()
        pendingCountTextView.text = pendingCount.toString()
        confirmedCountTextView.text = confirmedCount.toString()
        cancelledCountTextView.text = cancelledCount.toString()
        
        // Initially display all bookings
        adapter.updateBookings(bookings)
    }
}