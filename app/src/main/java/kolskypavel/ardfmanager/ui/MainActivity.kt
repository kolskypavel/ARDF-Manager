package kolskypavel.ardfmanager.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mazenrashed.printooth.Printooth
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.AppState
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.files.FileProcessor
import kolskypavel.ardfmanager.backend.results.ResultsProcessor
import kolskypavel.ardfmanager.backend.room.ARDFRepository
import kolskypavel.ardfmanager.backend.sportident.SIReaderStatus
import kolskypavel.ardfmanager.databinding.ActivityMainBinding
import java.lang.ref.WeakReference
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
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

    override fun attachBaseContext(newBase: Context?) {

        val languageCode: String = if (newBase != null) {
            PreferenceManager.getDefaultSharedPreferences(newBase).getString("app_language", "en")
                ?: "en"
        } else {
            "en"
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        val context = newBase?.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    //Apply preferences based on Shared preferences values
    private fun setPreferences() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val keepOpen = sharedPref.getBoolean(getString(R.string.key_keep_screen_open), false)
        if (keepOpen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPreferences()

        //Initialize singletons
        ARDFRepository.initialize(this)
        DataProcessor.initialize(this)
        dataProcessor = DataProcessor.get()
        dataProcessor.resultsProcessor = ResultsProcessor(dataProcessor)
        dataProcessor.fileProcessor = FileProcessor(WeakReference(this))
        Printooth.init(baseContext)

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
                R.id.navigation_categories, R.id.navigation_competitors, R.id.navigation_readouts,
                R.id.navigation_results, R.id.categoryEditDialogFragment, R.id.competitorEditDialogFragment,
                R.id.readoutEditDialogFragment, R.id.resultsShareDialogFragment
                    -> {
                    navView.visibility = View.VISIBLE
                    siStatusTextView.visibility = View.VISIBLE
                }

                R.id.raceSelectionFragment, R.id.readoutDetailFragment -> {
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
        val siObserver = Observer<AppState> { newState ->
            when (newState.siReaderState.status) {
                SIReaderStatus.CONNECTED -> {
                    //Check if race is set
                    if (newState.currentRace != null) {

                        if (newState.siReaderState.stationId != null) {
                            siStatusTextView.text =
                                getString(R.string.si_connected, newState.siReaderState.stationId)
                        } else {
                            siStatusTextView.text = getString(R.string.si_connected)
                        }
                        siStatusTextView.setBackgroundResource(R.color.green_ok)
                    }
                    //Race not selected - warn user
                    else {
                        if (newState.siReaderState.stationId != null) {
                            siStatusTextView.text =
                                getString(
                                    R.string.si_connected_but_no_race,
                                    newState.siReaderState.stationId!!
                                )
                        } else {
                            getString(R.string.si_connected_but_no_race)
                        }
                        siStatusTextView.setBackgroundResource(R.color.yellow_warning)
                    }
                }

                SIReaderStatus.DISCONNECTED -> {
                    siStatusTextView.setText(R.string.si_disconnected)
                    siStatusTextView.setBackgroundResource(R.color.grey)
                }

                SIReaderStatus.READING -> {
                    if (newState.siReaderState.stationId != null &&
                        newState.siReaderState.cardId != null
                    ) {
                        siStatusTextView.text =
                            getString(
                                R.string.si_reading,
                                newState.siReaderState.stationId!!,
                                newState.siReaderState.cardId!!
                            )
                    } else {
                        siStatusTextView.text = getString(R.string.si_reading)
                    }
                    siStatusTextView.setBackgroundResource(R.color.orange_reading)
                }

                SIReaderStatus.ERROR -> {
                    if (newState.siReaderState.stationId != null && newState.siReaderState.cardId != null) {
                        siStatusTextView.text =
                            getString(
                                R.string.si_card_error,
                                newState.siReaderState.stationId!!,
                                newState.siReaderState.cardId!!
                            )
                    } else {
                        siStatusTextView.text = getString(R.string.si_card_error)
                    }
                    siStatusTextView.setBackgroundResource(R.color.red_error)
                }

                SIReaderStatus.CARD_READ -> {
                    if (newState.siReaderState.stationId != null && newState.siReaderState.cardId != null) {
                        siStatusTextView.text =
                            getString(
                                R.string.si_card_read,
                                newState.siReaderState.stationId!!,
                                newState.siReaderState.cardId!!
                            )
                    } else {
                        siStatusTextView.text = getString(R.string.si_card_read)
                    }
                    siStatusTextView.setBackgroundResource(R.color.green_ok)
                }
            }
        }
        DataProcessor.get().currentState.observe(this, siObserver)
    }
}