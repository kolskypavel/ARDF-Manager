package kolskypavel.ardfmanager.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.room.ARDFRepository
import kolskypavel.ardfmanager.databinding.ActivityMainBinding
import kolskypavel.ardfmanager.ui.event.EventViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val eventViewModel: EventViewModel by viewModels()
    private lateinit var siStatusTextView: TextView
    private val ACTION_USB_PERMISSION = "kolskypavel.ardfmanager.USB_PERMISSION"

    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val accessory =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_ACCESSORY)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        accessory?.apply {
                            //call method to set up accessory communication
                        }
                    } else {

                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestUSBPermissions()
        ARDFRepository.initialize(this)
        DataProcessor.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        siStatusTextView = binding.siStatusView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.event_menu_about_the_app,
                R.id.event_menu_categories,
                R.id.navigation_readouts,
                R.id.navigation_results
            )
        )
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.event_menu_about_the_app, R.id.event_menu_categories, R.id.navigation_readouts,
                R.id.navigation_results, R.id.categoryCreateDialogFragment, R.id.competitorCreateDialogFragment
                -> {
                    navView.visibility = View.VISIBLE
                    siStatusTextView.visibility = View.VISIBLE
                }

                else -> {
                    navView.visibility = View.GONE
                    siStatusTextView.visibility = View.GONE
                }
            }
        }

    }

    /**
     * Request USB permissions
     */
    private fun requestUSBPermissions() {


//        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
//        val permissionIntent = PendingIntent.getBroadcast(
//            this, 0, Intent(ACTION_USB_PERMISSION),
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val filter = IntentFilter(ACTION_USB_PERMISSION)
//        registerReceiver(usbReceiver, filter)
//
//        manager.requestPermission(manager.deviceList[0], permissionIntent)
    }
}