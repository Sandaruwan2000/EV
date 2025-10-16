package com.example.ev

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class BackofficeHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_backoffice_home, container, false)

        val scanQrButton: Button = view.findViewById(R.id.scanQrShortcutButton)
        scanQrButton.setOnClickListener {
            (activity as? BookingConfirmationActivity)?.navigateToQrScan()
        }

        return view
    }
}