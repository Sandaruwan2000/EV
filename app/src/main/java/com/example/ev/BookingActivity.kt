package com.example.ev

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BookingActivity : BaseActivity() {

    private var stationId: String? = null
    private var editBookingId: String? = null
    private var totalSlots: Int = 0
    private val selectedDate = Calendar.getInstance()
    private var selectedTimeSlot: TimeSlot? = null
    private var selectedSlotNumber: Int? = null
    private var stationBookings: List<BookingDetails> = emptyList()

    private lateinit var timeSlotsRecyclerView: RecyclerView
    private lateinit var slotNumbersRecyclerView: RecyclerView
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    private lateinit var slotNumberAdapter: SlotNumberAdapter
    private lateinit var slotNumberTitleTextView: TextView

    private val dateInputFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        timeSlotsRecyclerView = findViewById(R.id.timeSlotsRecyclerView)
        slotNumbersRecyclerView = findViewById(R.id.slotNumbersRecyclerView)
        slotNumberTitleTextView = findViewById(R.id.slotNumberTitleTextView)

        timeSlotsRecyclerView.layoutManager = LinearLayoutManager(this)
        slotNumbersRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        editBookingId = intent.getStringExtra("EDIT_BOOKING_ID")

        if (editBookingId == null) {
            stationId = intent.getStringExtra("STATION_ID")
            if (stationId == null) {
                Toast.makeText(this, "Station ID not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            findViewById<Button>(R.id.bookButton).text = "Book Selected Slot"
            fetchBookingsForStation(stationId!!) { 
                fetchStationDetails(stationId!!)
            }
        } else {
            findViewById<Button>(R.id.bookButton).text = "Update Booking"
            loadDataForEditMode(editBookingId!!)
        }

        findViewById<Button>(R.id.datePickerButton).setOnClickListener { showDatePicker() }
        findViewById<Button>(R.id.bookButton).setOnClickListener { createOrUpdateBooking() }
    }

    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        for (format in dateInputFormats) {
            try {
                return format.parse(dateString)
            } catch (e: ParseException) { /* Continue */ }
        }
        return null
    }

    private fun fetchStationDetails(stationId: String, existingBooking: BookingDetails? = null) {
        ApiClient.authenticatedApi.getStationDetails(stationId).enqueue(object : Callback<ChargingStation> {
            override fun onResponse(call: Call<ChargingStation>, response: Response<ChargingStation>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        findViewById<TextView>(R.id.stationNameTextView).text = it.name
                        findViewById<TextView>(R.id.locationTextView).text = it.location
                        findViewById<TextView>(R.id.evTypeTextView).text = "Type: ${it.type}"
                        totalSlots = it.availableSlots
                        generateAndDisplayTimeSlots()
                        existingBooking?.let { eb -> prefillForm(eb) }
                    }
                } else { /* Error handling */ }
            }
            override fun onFailure(call: Call<ChargingStation>, t: Throwable) { /* Error handling */ }
        })
    }

    private fun fetchBookingsForStation(stationId: String, onComplete: (() -> Unit)? = null) {
        ApiClient.authenticatedApi.getBookingsForStation(stationId).enqueue(object : Callback<StationBookingsResponse> {
            override fun onResponse(call: Call<StationBookingsResponse>, response: Response<StationBookingsResponse>) {
                if (response.isSuccessful) {
                    stationBookings = response.body()?.data ?: emptyList()
                    onComplete?.invoke()
                } else { /* Error handling */ }
            }
            override fun onFailure(call: Call<StationBookingsResponse>, t: Throwable) { /* Error handling */ }
        })
    }

    private fun loadDataForEditMode(bookingId: String) {
        ApiClient.authenticatedApi.getAllBookings().enqueue(object: Callback<List<BookingDetails>> {
            override fun onResponse(call: Call<List<BookingDetails>>, response: Response<List<BookingDetails>>) {
                if (response.isSuccessful) {
                    val bookingToEdit = response.body()?.find { it.id == bookingId }
                    if (bookingToEdit != null) {
                        stationId = bookingToEdit.chargingStationId
                        fetchBookingsForStation(stationId!!) { 
                            fetchStationDetails(stationId!!, bookingToEdit)
                        }
                    } else { /* Error handling */ }
                } else { /* Error handling */ }
            }
            override fun onFailure(call: Call<List<BookingDetails>>, t: Throwable) { /* Error handling */ }
        })
    }

    private fun prefillForm(booking: BookingDetails) {
        findViewById<EditText>(R.id.vehicleTypeEditText).setText(booking.vehicleType)
        findViewById<EditText>(R.id.vehicleNumberEditText).setText(booking.vehicleNumber)

        val date = parseDate(booking.startTime)
        if (date != null) {
            selectedDate.time = date
            updateDateInView()
            timeSlotsRecyclerView.post { 
                timeSlotAdapter.setSelectedByTime(date)
                displayAvailableSlotNumbers(TimeSlot(date, date), booking.slotNumber)
            }
        } else { /* Error handling */ }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(this, { _, y, m, d -> 
            selectedDate.set(y, m, d)
            updateDateInView() 
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
        
        // Set the minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        
        // Set the maximum date to 7 days from today
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.DAY_OF_YEAR, 7)
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        
        datePickerDialog.show()
    }

    private fun updateDateInView() {
        val myFormat = "MMMM d, yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        findViewById<Button>(R.id.datePickerButton).text = sdf.format(selectedDate.time)
        generateAndDisplayTimeSlots()
        clearSlotNumberSelection()
    }

    private fun generateAndDisplayTimeSlots() {
        val allTimeSlots = (0..22 step 2).map {
            val cal = Calendar.getInstance().apply {
                time = selectedDate.time
                set(Calendar.HOUR_OF_DAY, it)
                set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val startTime = cal.time
            cal.add(Calendar.HOUR_OF_DAY, 2)
            TimeSlot(startTime, cal.time)
        }

        timeSlotAdapter = TimeSlotAdapter(allTimeSlots) { slot ->
            selectedTimeSlot = slot
            displayAvailableSlotNumbers(slot)
        }
        timeSlotsRecyclerView.adapter = timeSlotAdapter
    }

    private fun displayAvailableSlotNumbers(timeSlot: TimeSlot, preSelectedSlot: Int? = null) {
        val bookingsInTimeSlot = stationBookings.filter { b ->
            val bookingStart = parseDate(b.startTime)
            bookingStart != null && bookingStart.time == timeSlot.startTime.time &&
            (b.status.equals("Pending", true) || b.status.equals("Confirmed", true))
        }
        val bookedSlotNumbers = bookingsInTimeSlot.map { it.slotNumber }.toSet()
        val allPossibleSlots = (1..totalSlots).toList()
        
        val availableSlots = allPossibleSlots.filter { slotNum ->
            !bookedSlotNumbers.contains(slotNum) || slotNum == preSelectedSlot
        }

        slotNumberTitleTextView.visibility = View.VISIBLE
        slotNumbersRecyclerView.visibility = View.VISIBLE

        slotNumberAdapter = SlotNumberAdapter(availableSlots) { slotNum ->
            selectedSlotNumber = slotNum
        }
        slotNumbersRecyclerView.adapter = slotNumberAdapter

        if (preSelectedSlot != null) {
            slotNumbersRecyclerView.post { slotNumberAdapter.setSelectedSlot(preSelectedSlot) }
        }
    }

    private fun clearSlotNumberSelection() {
        selectedSlotNumber = null
        slotNumberTitleTextView.visibility = View.GONE
        slotNumbersRecyclerView.visibility = View.GONE
    }

    private fun createOrUpdateBooking() {
        if (selectedTimeSlot == null || selectedSlotNumber == null) {
            Toast.makeText(this, "Please select a time and slot number", Toast.LENGTH_SHORT).show()
            return
        }
        val vehicleType = findViewById<EditText>(R.id.vehicleTypeEditText).text.toString()
        val vehicleNumber = findViewById<EditText>(R.id.vehicleNumberEditText).text.toString()
        if (vehicleType.isEmpty() || vehicleNumber.isEmpty()) {
            Toast.makeText(this, "Please enter vehicle details", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager.getUserId() ?: return
        ApiClient.authenticatedApi.getEvOwnerUserDetails(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    val userNic = response.body()?.nic ?: return

                    val myFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                    val sdf = SimpleDateFormat(myFormat, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

                    val booking = Booking(
                        chargingStationId = stationId!!,
                        ownerNIC = userNic,
                        slotNumber = selectedSlotNumber!!,
                        reservationDateTime = sdf.format(selectedTimeSlot!!.startTime),
                        vehicleType = vehicleType,
                        vehicleNumber = vehicleNumber,
                        startTime = sdf.format(selectedTimeSlot!!.startTime),
                        endTime = sdf.format(selectedTimeSlot!!.endTime)
                    )

                    val bookingCall = if (editBookingId == null) {
                        ApiClient.authenticatedApi.bookStation(booking)
                    } else {
                        ApiClient.authenticatedApi.updateBooking(editBookingId!!, booking)
                    }

                    bookingCall.enqueue(object : Callback<BookingDetails> {
                        override fun onResponse(call: Call<BookingDetails>, res: Response<BookingDetails>) {
                            if (res.isSuccessful) {
                                val message = if (editBookingId == null) "Booking successful!" else "Booking updated!"
                                Toast.makeText(this@BookingActivity, message, Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@BookingActivity, "Booking failed: ${res.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<BookingDetails>, t: Throwable) { /* ... */ }
                    })
                } else { /* ... */ }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) { /* ... */ }
        })
    }
}