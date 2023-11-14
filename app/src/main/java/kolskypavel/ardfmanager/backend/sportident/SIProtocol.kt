package kolskypavel.ardfmanager.backend.sportident

object SIProtocol {
    val STX = 0x02 //Transmission start
    val ETX = 0x03 //Transmission end
    val ACK = 0x06
    val NAK = 0x15
    val DLE = 0x10
    val WAKEUP = 0xFF //Wake up station


}