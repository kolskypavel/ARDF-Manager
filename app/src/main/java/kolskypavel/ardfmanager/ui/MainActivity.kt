package kolskypavel.ardfmanager.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.ARDFRepository
import kolskypavel.ardfmanager.backend.sportident.SIReaderState
import kolskypavel.ardfmanager.backend.sportident.SIReaderStatus
import kolskypavel.ardfmanager.databinding.ActivityMainBinding
import kolskypavel.ardfmanager.ui.event.EventViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val eventViewModel: EventViewModel by viewModels()
    private lateinit var siStatusTextView: TextView
    private lateinit var dataProcessor: DataProcessor

    private var usbDetachReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                device?.apply {
                    dataProcessor.detachDevice(device)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ARDFRepository.initialize(this)
        DataProcessor.initialize(this)
        dataProcessor = DataProcessor.get()

        // Set the usb device
        if (intent != null) {
            val device: UsbDevice? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }
            if (device != null) {
                dataProcessor.connectDevice(device)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        siStatusTextView = binding.siStatusView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.event_menu_about_the_app, R.id.event_menu_categories, R.id.navigation_readouts,
                R.id.navigation_results, R.id.categoryCreateDialogFragment, R.id.competitorCreateDialogFragment
                -> {
                    navView.visibility = View.VISIBLE
                    siStatusTextView.visibility = View.VISIBLE
                }

                R.id.eventSelectionFragment -> {
                    navView.visibility = View.GONE
                    siStatusTextView.visibility = View.VISIBLE
                }

                else -> {
                    navView.visibility = View.GONE
                    siStatusTextView.visibility = View.GONE
                }
            }
        }

        //Set the notification channel
        setNotificationChannel()

        //Set the observer for the SI text view
        setStationObserver()
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbDetachReceiver, filter)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            val device: UsbDevice? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }
            if (device != null) {
                dataProcessor.connectDevice(device)
            }
        }
    }

    private fun setNotificationChannel() {
        val channel = NotificationChannel(
            "si_reader_channel",
            "ARDF Manager channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setStationObserver() {
        val siObserver = Observer<SIReaderState> { newState ->
            when (newState.status) {
                SIReaderStatus.CONNECTED -> {
                    if (newState.stationId != null) {
                        siStatusTextView.text =
                            getString(R.string.si_connected, newState.stationId!!)
                    } else {
                        siStatusTextView.text = getString(R.string.si_connected)
                    }
                    siStatusTextView.setBackgroundResource(R.color.green_ok)
                }

                SIReaderStatus.DISCONNECTED -> {
                    siStatusTextView.setText(R.string.si_disconnected)
                    siStatusTextView.setBackgroundResource(R.color.grey)
                }

                SIReaderStatus.READING -> {
                    if (newState.stationId != null && newState.cardId != null) {
                        siStatusTextView.text =
                            getString(R.string.si_reading, newState.stationId!!, newState.cardId!!)
                    } else {
                        siStatusTextView.text = getString(R.string.si_reading)
                    }
                    siStatusTextView.setBackgroundResource(R.color.orange_reading)
                }

                SIReaderStatus.CARD_REMOVED -> {
                    if (newState.stationId != null && newState.cardId != null) {
                        siStatusTextView.text =
                            getString(
                                R.string.si_card_removed,
                                newState.stationId!!,
                                newState.cardId!!
                            )
                    } else {
                        siStatusTextView.text = getString(R.string.si_card_removed)
                    }
                    siStatusTextView.setBackgroundResource(R.color.red_error)
                }

                SIReaderStatus.CARD_READ -> {
                    if (newState.stationId != null && newState.cardId != null) {
                        siStatusTextView.text =
                            getString(
                                R.string.si_card_read,
                                newState.stationId!!,
                                newState.cardId!!
                            )
                    } else {
                        siStatusTextView.text = getString(R.string.si_card_read)
                    }
                    siStatusTextView.setBackgroundResource(R.color.green_ok)
                }
            }
        }
        DataProcessor.get().siReaderState.observe(this, siObserver)
    }
}