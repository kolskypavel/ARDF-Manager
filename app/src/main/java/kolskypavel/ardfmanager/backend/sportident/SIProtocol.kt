package kolskypavel.ardfmanager.backend.sportident

object SIProtocol {
    val STX = 0x02 //Transmission start
    val ETX = 0x03 //Transmission end
    val ACK = 0x06  //Acknowledgment
    val NAK = 0x15  //Negative acknowledgement
    val DLE = 0x10  //Delimiter
    val WAKEUP = 0xFF //Wake up station

    val UNKNOWN = 0x00
    val CONTROL = 0x02
    val START = 0x03
    val FINIFSH = 0x04
    val READ = 0x05
    val CLEAR_START_NBR = 0x06
    val CLEAR = 0x07
    val CHECK = 0x0a


}