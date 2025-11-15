package kolskypavel.ardfmanager.backend.sportident

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.felhr.usbserial.UsbSerialDevice
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.AppState
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_PRODUCT_ID
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SI_VENDOR_ID
import kotlinx.coroutines.Job


class SIReaderService :
    Service() {
    private var dataProcessor = DataProcessor.get()
    private var device: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var serialDevice: UsbSerialDevice? = null
    private var siPort: SIPort? = null
    private var siJob: Job? = null
    private var observer: Observer<AppState>? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val usbDevice: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(USB_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(USB_DEVICE)
        }

        if (usbDevice != null) {
            when (intent?.action) {
                ReaderServiceActions.START.toString() -> startService(usbDevice, this)
                ReaderServiceActions.STOP.toString() -> stopService(usbDevice)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startService(newDevice: UsbDevice, context: Context) {
        if (newDevice.vendorId == SI_VENDOR_ID && newDevice.productId == SI_PRODUCT_ID) {
            device = newDevice
            startSIDevice()

            // Ensure notification channel exists (required on O+)
            createNotificationChannel(context)

            val notification =
                NotificationCompat.Builder(context, SIConstants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_sportident)
                    .setContentTitle(getString(R.string.si_ready))
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .build()

            startForeground(NOTIFICATION_ID, notification)
            setNotificationObserver()
        }
    }

    private fun stopService(removedDevice: UsbDevice) {
        if (removedDevice.vendorId == SI_VENDOR_ID && removedDevice.productId == SI_PRODUCT_ID) {
            siJob?.cancel()

            // Remove the observer if registered
            if (observer != null) {
                dataProcessor.currentState.removeObserver(observer!!)
                observer = null
            }

            if (serialDevice != null) {
                serialDevice!!.close()
            }
            device = null
            if (connection != null) {
                connection?.close()
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            dataProcessor.updateReaderState(
                SIReaderState(
                    SIReaderStatus.DISCONNECTED,
                    null,
                    null, null
                )
            )
        }
    }

    private fun startSIDevice() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        connection = usbManager.openDevice(device)
        serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
        siPort = SIPort(serialDevice!!)

        //Start the work on the SI reader
        siJob = siPort!!.workJob()
        siJob!!.start()
    }

    private fun createNotificationChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use a readable name for the channel; reuse existing string resource
        val channel = NotificationChannel(
            SIConstants.NOTIFICATION_CHANNEL_ID,
            getString(R.string.si_ready),
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(channel)
    }

    private fun setNotificationObserver() {

        observer = Observer { newState ->

            val lastCardString =
                if (newState.siReaderState.lastCard != null) {
                    newState.siReaderState.lastCard.toString()
                } else {
                    getString(R.string.no_cards_yet)
                }
            val notification = NotificationCompat.Builder(this, SIConstants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sportident)
                .setContentTitle(getString(R.string.si_ready))
                .setContentText(getString(R.string.si_last_card, lastCardString))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()

            // Update the existing foreground notification instead of posting a new one
            startForeground(NOTIFICATION_ID, notification)
        }

        dataProcessor.currentState.observeForever(observer!!)
    }

    enum class ReaderServiceActions {
        START,
        STOP
    }

    companion object {
        const val USB_DEVICE = "USB_DEVICE"
        private const val NOTIFICATION_ID = 1
    }
}