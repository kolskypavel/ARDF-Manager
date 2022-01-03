package com.david.ardfmanager.SI;

class SICRC {
    private static final int POLYNOM = 0x8005;
    static int calc(int uiCount, byte[] pucDat)
    {
        int uiTmp1, uiVal;
        int pucDatIndex = 0;

        if (uiCount < 2) return 0;

        uiTmp1 = pucDat[pucDatIndex++];
        uiTmp1 = (uiTmp1 << 8) + pucDat[pucDatIndex++];

        if (uiCount == 2) return uiTmp1;
        for (int iTmp = (uiCount >> 1); iTmp > 0; iTmp--)
        {
            if (iTmp > 1) {
                uiVal = pucDat[pucDatIndex++];
                uiVal = (uiVal << 8) + pucDat[pucDatIndex++];
            }
            else {
                if ((uiCount & 1) == 1) {
                    uiVal = pucDat[pucDatIndex];
                    uiVal <<= 8;
                }
                else {
                    uiVal = 0;
                }
            }

            for (int uiTmp=0; uiTmp<16; uiTmp++) {
                if ((uiTmp1 & 0x8000) == 0x8000) {
                    uiTmp1 <<= 1;
                    if ((uiVal & 0x8000) == 0x8000) uiTmp1++;
                    uiTmp1 ^= POLYNOM;
                }
                else {
                    uiTmp1 <<= 1;
                    if ((uiVal & 0x8000) == 0x8000) uiTmp1++;
                }
                uiVal <<= 1;
            }
        }

        return (uiTmp1 & 0xffff);
    }
}