package kolskypavel.ardfmanager.backend.sportident

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.felhr.usbserial.UsbSerialDevice
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SPORTIDENT_PRODUCT_ID
import kolskypavel.ardfmanager.backend.sportident.SIConstants.SPORTIDENT_VENDOR_ID
import java.util.UUID


class SIReader(val context: Context) {

    private var device: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var port: UsbSerialDevice? = null
    private var siPort: SIPort? = null

    private var eventId: UUID? = null

    //  private val ACTION_USB_PERMISSION = "kolskypavel.ardfmanager.USB_PERMISSION"


    fun setReaderDevice(newDevice: UsbDevice) {
        if (newDevice.vendorId == SPORTIDENT_VENDOR_ID && newDevice.productId == SPORTIDENT_PRODUCT_ID) {
            device = newDevice
            startSIDevice()
        }
    }

    private fun startSIDevice() {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        connection = usbManager.openDevice(device)
        port = UsbSerialDevice.createUsbSerialDevice(device, connection)

        //Start the work on the SI reader

    }

    fun detachReaderDevice(removedDevice: UsbDevice) {
        if (removedDevice.vendorId == SPORTIDENT_VENDOR_ID && removedDevice.productId == SPORTIDENT_PRODUCT_ID) {
            device = null
            if (connection != null) {
                connection?.close()
            }
        }
    }
}