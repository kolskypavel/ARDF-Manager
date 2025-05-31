package kolskypavel.ardfmanager.ui.settings

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import kolskypavel.ardfmanager.R


class PrintsFragment : PreferenceFragmentCompat() {
    private lateinit var prefs: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_prints, rootKey)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setPreferences()
        val printsEnabled =
            prefs.getBoolean(requireContext().getString(R.string.key_prints_enabled), false)
        enableOrDisablePreferences(printsEnabled)
    }

    private fun setPreferences() {
        val editor = prefs.edit()

        //Enable printing
        val enablePrintingPreference =
            findPreference<SwitchPreference>(requireContext().getString(R.string.key_prints_enabled))

        enablePrintingPreference?.setOnPreferenceChangeListener { _, enablePrints ->

            editor.putBoolean(
                requireContext().getString(R.string.key_prints_enabled),
                enablePrints as Boolean
            )
            editor.apply()
            enableOrDisablePreferences(enablePrints)
            true
        }

        //Printer selection
        val printerSelectPreference =
            findPreference<ListPreference>(requireContext().getString(R.string.key_prints_select_printer))

        printerSelectPreference?.setOnPreferenceClickListener {

            val bluetoothAvailable =
                requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

            if (bluetoothAvailable) {
                // Get the BluetoothManager
                val bluetoothAdapter =
                    ContextCompat.getSystemService(
                        requireContext(),
                        BluetoothManager::class.java
                    )?.adapter

                val pairedDevices = emptySet<BluetoothDevice>().toMutableSet()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ) {
                    // Request the BLUETOOTH_CONNECT permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        1
                    )
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S ||
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission already granted, proceed with accessing paired devices
                    if (bluetoothAdapter != null && bluetoothAdapter.bondedDevices != null) {
                        pairedDevices.addAll(bluetoothAdapter.bondedDevices!!)
                    }
                }
                val deviceNames = pairedDevices.map { it.name }.toTypedArray()
                val deviceAddresses = pairedDevices.map { it.address }.toTypedArray()

                printerSelectPreference.entries = deviceNames
                printerSelectPreference.entryValues = deviceAddresses
            }
            //Warning about missing bluetooth
            else {
                val toast = Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.print_bluetooth_not_supported),
                    Toast.LENGTH_LONG
                )
                toast.show()
            }
            true
        }

        printerSelectPreference?.setOnPreferenceChangeListener { _, printer ->
            editor.putString(
                requireContext().getString(R.string.key_prints_select_printer),
                printer.toString()
            )
            editor.apply()

            true
        }
    }

    private fun enableOrDisablePreferences(enable: Boolean) {
        val printerSelectPreference =
            findPreference<ListPreference>(requireContext().getString(R.string.key_prints_select_printer))

        val automaticPrintPreference =
            findPreference<ListPreference>(requireContext().getString(R.string.key_prints_automatic_printout))

        printerSelectPreference?.isEnabled = enable
        automaticPrintPreference?.isEnabled = enable
    }
}