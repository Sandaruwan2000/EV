package com.example.ev

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookingActivity : BaseActivity() {

    private var selectedSlotNumber: Int? = null
    private var stationId: String? = null
    private var editBookingId: String? = null
    private lateinit var userRepository: UserRepository
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        userRepository = UserRepository(this)
        editBookingId = intent.getStringExtra("EDIT_BOOKING_ID")

        if (editBookingId == null) {
            // CREATE MODE
            stationId = intent.getStringExtra("STATION_ID")
            if (stationId == null) {
                Toast.makeText(this, "Station ID not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            fetchStationDetails(stationId!!)
            findViewById<Button>(R.id.bookButton).text = "Book Selected Slot"
        } else {
            // EDIT MODE
            findViewById<Button>(R.id.bookButton).text = "Update Booking"
            fetchBookingAndStationDetails(editBookingId!!)
        }

        findViewById<Button>(R.id.dateTimePickerButton).setOnClickListener { showDateTimePicker() }
        findViewById<Button>(R.id.bookButton).setOnClickListener { createOrUpdateBooking() }
    }

    private fun fetchStationDetails(stationId: String, existingBooking: BookingDetails? = null) {
        ApiClient.authenticatedApi.getStationDetails(stationId).enqueue(object : Callback<ChargingStation> {
            override fun onResponse(call: Call<ChargingStation>, response: Response<ChargingStation>) {
                if (response.isSuccessful) {
                    response.body()?.let { station ->
                        findViewById<TextView>(R.id.stationNameTextView).text = station.name
                        findViewById<TextView>(R.id.locationTextView).text = station.location
                        findViewById<TextView>(R.id.evTypeTextView).text = "Type: ${station.type}"

                        val slotsRecyclerView: RecyclerView = findViewById(R.id.slotsRecyclerView)
                        slotsRecyclerView.layoutManager = GridLayoutManager(this@BookingActivity, 4)

                        val bookedSlots = station.schedules.map { it.slotNumber }
                        val slotAdapter = SlotAdapter(station.availableSlots, bookedSlots) { slotNumber ->
                            selectedSlotNumber = slotNumber
                            Toast.makeText(this@BookingActivity, "Selected Slot: $slotNumber", Toast.LENGTH_SHORT).show()
                        }
                        slotsRecyclerView.adapter = slotAdapter

                        // Pre-fill form in edit mode
                        existingBooking?.let { prefillForm(it) }
                    }
                } else {
                    Toast.makeText(this@BookingActivity, "Failed to fetch station details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChargingStation>, t: Throwable) {
                Toast.makeText(this@BookingActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchBookingAndStationDetails(bookingId: String) {
        ApiClient.authenticatedApi.getAllBookings().enqueue(object : Callback<List<BookingDetails>> {
            override fun onResponse(call: Call<List<BookingDetails>>, response: Response<List<BookingDetails>>) {
                if (response.isSuccessful) {
                    val booking = response.body()?.find { it.id == bookingId }
                    if (booking != null) {
                        stationId = booking.chargingStationId
                        fetchStationDetails(stationId!!, booking)
                    } else {
                        Toast.makeText(this@BookingActivity, "Booking details not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            override fun onFailure(call: Call<List<BookingDetails>>, t: Throwable) { /* Handle error */ }
        })
    }

    private fun prefillForm(booking: BookingDetails) {
        findViewById<EditText>(R.id.vehicleTypeEditText).setText(booking.vehicleType)
        findViewById<EditText>(R.id.vehicleNumberEditText).setText(booking.vehicleNumber)
        selectedSlotNumber = booking.slotNumber
        findViewById<Button>(R.id.dateTimePickerButton).text = booking.reservationDateTime
    }

    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                if (isWithin7Days(selectedDate.time)) {
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    TimePickerDialog(this, { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        updateDateInView()
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                } else {
                    Toast.makeText(this, "Reservation must be within 7 days from today.", Toast.LENGTH_LONG).show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun isWithin7Days(date: Date): Boolean {
        val sevenDaysFromNow = Calendar.getInstance()
        sevenDaysFromNow.add(Calendar.DAY_OF_YEAR, 7)
        return date.before(sevenDaysFromNow.time)
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        findViewById<Button>(R.id.dateTimePickerButton).text = SimpleDateFormat(myFormat, Locale.US).format(calendar.time)
    }

    private fun createOrUpdateBooking() {
        val vehicleType = findViewById<EditText>(R.id.vehicleTypeEditText).text.toString()
        val vehicleNumber = findViewById<EditText>(R.id.vehicleNumberEditText).text.toString()
        val reservationDateTime = findViewById<Button>(R.id.dateTimePickerButton).text.toString()

        if (selectedSlotNumber == null) {
            Toast.makeText(this, "Please select a slot", Toast.LENGTH_SHORT).show()
            return
        }
        if (vehicleType.isEmpty() || vehicleNumber.isEmpty()) {
            Toast.makeText(this, "Please enter vehicle details", Toast.LENGTH_SHORT).show()
            return
        }
        if (reservationDateTime == "Select Date & Time") {
            Toast.makeText(this, "Please select a date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to book", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = userRepository.getLocalUser(userId)
            if (user?.nic == null) {
                Toast.makeText(this@BookingActivity, "Could not retrieve user NIC", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val booking = Booking(
                chargingStationId = stationId!!,
                ownerNIC = user.nic,
                slotNumber = selectedSlotNumber!!,
                reservationDateTime = reservationDateTime,
                vehicleType = vehicleType,
                vehicleNumber = vehicleNumber,
                startTime = reservationDateTime,
                endTime = reservationDateTime // Placeholder
            )

            val call = if (editBookingId == null) {
                ApiClient.authenticatedApi.bookStation(booking)
            } else {
                ApiClient.authenticatedApi.updateBooking(editBookingId!!, booking)
            }

            call.enqueue(object : Callback<BookingDetails> {
                override fun onResponse(call: Call<BookingDetails>, response: Response<BookingDetails>) {
                    if (response.isSuccessful) {
                        val message = if (editBookingId == null) "Booking successful!" else "Booking updated!"
                        Toast.makeText(this@BookingActivity, message, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = if (editBookingId == null) "Booking failed" else "Update failed"
                        Toast.makeText(this@BookingActivity, "$errorMsg: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BookingDetails>, t: Throwable) {
                    Toast.makeText(this@BookingActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}