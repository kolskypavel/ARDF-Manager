package kolskypavel.ardfmanager.backend.sportident

object SIConstants {
    const val STX: Byte = 0x02 //Transmission start
    const val ETX: Byte = 0x03 //Transmission end
    const val ACK: Byte = 0x06  //Acknowledgment
    const val NAK: Byte = 0x15 //Negative acknowledgement
    const val DLE: Byte = 0x10  //Delimiter
    const val WAKEUP: Byte = 0xFF.toByte() //Wake up station
    const val GET_SYSTEM_INFO: Byte = 0x83.toByte()
    const val EXTENDED_MODE: Byte = 0x01.toByte()
    const val ZERO: Byte = 0x00

    const val UNKNOWN: Byte = 0x00
    const val CONTROL: Byte = 0x02
    const val START: Byte = 0x03
    const val FINISH: Byte = 0x04
    const val READ: Byte = 0x05
    const val CLEAR_START_NBR: Byte = 0x06
    const val CLEAR: Byte = 0x07
    const val CHECK: Byte = 0x0a

    const val POLYNOM = 0x8005
    const val WRITE_TIMEOUT = 500

    const val BAUDRATE_LOW = 4800
    const val BAUDRATE_HIGH = 38400

}