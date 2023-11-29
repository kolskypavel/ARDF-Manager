package kolskypavel.ardfmanager.backend.sportident

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.felhr.usbserial.UsbSerialDevice
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.backend.DataProcessor
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SPORTIDENT_PRODUCT_ID
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SPORTIDENT_VENDOR_ID
import kotlinx.coroutines.Job


class SIReaderService :
    Service() {
    private var dataProcessor = DataProcessor.get()
    private var device: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var serialDevice: UsbSerialDevice? = null
    private var siPort: SIPort? = null
    private var siJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val usbDevice: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(USB_DEVICE, UsbDevice::class.java)
        } else {
            intent?.getParcelableExtra(USB_DEVICE)
        }

        if (usbDevice != null) {
            when (intent?.action) {
                ReaderActions.START.toString() -> startService(usbDevice)
                ReaderActions.STOP.toString() -> stopService(usbDevice)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startService(newDevice: UsbDevice) {
        if (newDevice.vendorId == SPORTIDENT_VENDOR_ID && newDevice.productId == SPORTIDENT_PRODUCT_ID) {
            device = newDevice
            startSIDevice()

            val notification = NotificationCompat.Builder(this, "si_reader_channel")
                .setSmallIcon(R.drawable.ic_sportident)
                .setContentTitle(getString(R.string.si_connected)).build()

            startForeground(1, notification)
        }
    }

    private fun stopService(removedDevice: UsbDevice) {
        if (removedDevice.vendorId == SPORTIDENT_VENDOR_ID && removedDevice.productId == SPORTIDENT_PRODUCT_ID) {
            siJob?.cancel()
            if (serialDevice != null) {
                serialDevice!!.close()
            }
            device = null
            if (connection != null) {
                connection?.close()
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            dataProcessor.siReaderState.postValue(
                SIReaderState(
                    SIReaderStatus.DISCONNECTED,
                    null,
                    null
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

    enum class ReaderActions {
        START,
        STOP
    }

    companion object {
        const val USB_DEVICE = "USB_DEVICE"
    }
}