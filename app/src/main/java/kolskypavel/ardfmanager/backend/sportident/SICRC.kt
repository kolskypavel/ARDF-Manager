package kolskypavel.ardfmanager.backend.sportident

/**
 * Calculate the sportident CRC
 */
class SICRC {
    private val POLYNOM = 0x8005
    fun calc(uiCount: Int, pucDat: ByteArray): Int {
        var uiTmp1: Int
        var uiVal: Int
        var pucDatIndex = 0
        if (uiCount < 2) return 0
        uiTmp1 = pucDat[pucDatIndex++].toInt()
        uiTmp1 = (uiTmp1 shl 8) + pucDat[pucDatIndex++]
        if (uiCount == 2) return uiTmp1
        for (iTmp in uiCount shr 1 downTo 1) {
            if (iTmp > 1) {
                uiVal = pucDat[pucDatIndex++].toInt()
                uiVal = (uiVal shl 8) + pucDat[pucDatIndex++]
            } else {
                if (uiCount and 1 == 1) {
                    uiVal = pucDat[pucDatIndex].toInt()
                    uiVal = uiVal shl 8
                } else {
                    uiVal = 0
                }
            }
            for (uiTmp in 0..15) {
                if (uiTmp1 and 0x8000 == 0x8000) {
                    uiTmp1 = uiTmp1 shl 1
                    if (uiVal and 0x8000 == 0x8000) uiTmp1++
                    uiTmp1 = uiTmp1 xor POLYNOM
                } else {
                    uiTmp1 = uiTmp1 shl 1
                    if (uiVal and 0x8000 == 0x8000) uiTmp1++
                }
                uiVal = uiVal shl 1
            }
        }
        return uiTmp1 and 0xffff
    }
}