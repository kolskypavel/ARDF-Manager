package kolskypavel.ardfmanager.backend.sportident

object SIConstants {
    const val SPORTIDENT_VENDOR_ID = 4292
    const val SPORTIDENT_PRODUCT_ID = 32778

    const val STX: Byte = 0x02 //Transmission start
    const val ETX: Byte = 0x03 //Transmission end
    const val ACK: Byte = 0x06  //Acknowledgment
    const val NAK: Byte = 0x15 //Negative acknowledgement
    const val DLE: Byte = 0x10  //Delimiter
    const val WAKEUP: Byte = 0xFF.toByte() //Wake up station
    const val GET_SYSTEM_INFO: Byte = 0x83.toByte()
    const val EXTENDED_MODE: Byte = 0x01.toByte()
    const val ZERO: Byte = 0x00

    const val DELAY_INTERVAL = 5000L
    const val WRITE_TIMEOUT = 500
    const val BAUDRATE_LOW = 4800
    const val BAUDRATE_HIGH = 38400
    const val POLYNOM = 0x8005

    const val SI_CARD5: Byte = 0xE5.toByte()
    const val GET_SI_CARD5: Byte = 0xB1.toByte()
    const val SI_CARD6: Byte = 0xE6.toByte()
    const val GET_SI_CARD6: Byte = 0xE1.toByte()
    const val SI_CARD8_9_SIAC: Byte = 0xE8.toByte()
    const val GET_SI_CARD8_9_SIAC: Byte = 0xEF.toByte()
    const val SI_CARD_REMOVED: Byte = 0xE7.toByte()

    const val UNKNOWN: Byte = 0x00
    const val CONTROL: Byte = 0x02
    const val START: Byte = 0x03
    const val FINISH: Byte = 0x04
    const val READ: Byte = 0x05
    const val CLEAR_START_NBR: Byte = 0x06
    const val CLEAR: Byte = 0x07
    const val CHECK: Byte = 0x0a


}