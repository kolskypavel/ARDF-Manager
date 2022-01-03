package com.david.ardfmanager.SI;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;

public class SIProtocol {
    //private UsbSerialPort port;
    private UsbSerialDevice port;
    private ArrayList<byte[]> msgCache;

    private final String TAG = SIProtocol.class.getSimpleName();
    private static final int SI_STX = 0x02;
    private static final int SI_ETX = 0x03;
    private static final int SI_ACK = 0x06;
    private static final int SI_NAK = 0x15;
    private static final int SI_DLE = 0x10;

    private static final int WRITE_TIMEOUT = 500;

    //public SIProtocol(UsbSerialPort port)
    public SIProtocol(UsbSerialDevice port)
    {
        this.port = port;
        this.msgCache = new ArrayList<byte[]>();
    }

    public int writeMsg(byte command, byte[] data)
    {
        return this.writeMsg(command, data, true);
    }

    public int writeMsg(byte command, byte[] data, boolean extended)
    {
        int datalen = 0;
        int size;

        if (data != null) {
            datalen = data.length;
        }

        if (extended) {
            size = datalen + 7;
        }
        else {
            size = datalen + 4;
        }

        byte[] buffer = new byte[size];
        buffer[0] = (byte)0xff;
        buffer[1] = SI_STX;
        buffer[2] = command;
        if (extended) {
            buffer[3] = (byte)datalen;
            if (data != null) {
                System.arraycopy(data, 0, buffer, 4, data.length);
            }
            int crc = SICRC.calc(datalen+2, Arrays.copyOfRange(buffer, 2, buffer.length));
            buffer[datalen+4] = (byte)((crc & 0xff00) >> 8);
            buffer[datalen+5] = (byte)(crc & 0xff);
            buffer[datalen+6] = SI_ETX;
        }
        else {
            if (data != null) {
                System.arraycopy(data, 0, buffer, 3, data.length);
            }
            buffer[datalen+3] = SI_ETX;
        }

        int writtenBytes = this.port.syncWrite(buffer, WRITE_TIMEOUT);

        return (writtenBytes == buffer.length) ? 0 : -1;
    }

    public int writeAck()
    {
        byte[] buffer = new byte[4];
        buffer[0] = (byte)0xff;
        buffer[1] = SI_STX;
        buffer[2] = SI_ACK;
        buffer[3] = SI_ETX;

        int writtenBytes = this.port.syncWrite(buffer, WRITE_TIMEOUT);

        return (writtenBytes == buffer.length) ? 0 : -1;
    }

    public int writeNak()
    {
        byte[] buffer = new byte[4];
        buffer[0] = (byte)0xff;
        buffer[1] = SI_STX;
        buffer[2] = SI_NAK;
        buffer[3] = SI_ETX;

        int writtenBytes = this.port.syncWrite(buffer, WRITE_TIMEOUT);

        return (writtenBytes == buffer.length) ? 0 : -1;
    }

    public byte[] readMsg(int timeout)
    {
        return this.readMsg(timeout, (byte) 0x00);
    }

    public byte[] readMsg(int timeout, byte filter)
    {
        byte[] buffer;
        int bufferSize = 0;
        byte[] tmpBuffer;
        int tmpBufferIndex = 0;
        int bytesRead = 0;
        boolean eof = false;
        boolean dle = false;

        if ((tmpBuffer = this.dequeueCache(filter)) != null) {
            return tmpBuffer;
        }

        // Try to read all bytes from port
        buffer = new byte[4096];
        tmpBuffer = new byte[4096];

        do {
            if (tmpBufferIndex >= bytesRead) {
                bytesRead = this.port.syncRead(tmpBuffer, timeout);
                if (bytesRead == 0) {
                    break;
                }
                tmpBufferIndex = 0;
            }
            byte incByte = tmpBuffer[tmpBufferIndex++];

            if (!(bufferSize == 0 && incByte == (byte)0xff) && !(bufferSize == 0 && incByte == 0x00) && !(bufferSize == 1 && incByte == (byte)SI_STX)) {
                buffer[bufferSize++] = incByte;
            }

            // Check if we have received a NAK
            if (bufferSize == 1 && incByte == (byte)SI_NAK) {
                eof = true;
            }

            // If we have got to message type
            if (bufferSize > 1) {
                // If the command is in extended range
                if (byteToUnsignedInt(buffer[1]) > 0x80) {
                    if (bufferSize > 2 && bufferSize >= byteToUnsignedInt(buffer[2]) + 6) {
                        eof = true;
                        // TODO check crc
                    }
                }
                // normal command range
                else {
                    // If last char was a DLE, just continue
                    if(dle) {
                        dle = false;
                    }
                    // Is this byte a DLE
                    else if(incByte == SI_DLE) {
                        dle = true;
                    }
                    // Is this byte ETX (end)
                    else if(incByte == SI_ETX) {
                        eof = true;
                    }
                }
            }

            // Check if message should be cached
            if (eof && filter != 0x00 && bufferSize > 1 && filter != buffer[1]) {
                enqueueCache(Arrays.copyOfRange(buffer, 0, bufferSize));

                eof = false;
                dle = false;
                bufferSize = 0;
            }
        }
        while (!eof);

        if (eof && bufferSize > 0) {
            return Arrays.copyOfRange(buffer, 0, bufferSize);
        }
        else {
            return null;
        }
    }

    private void enqueueCache(byte[] buffer)
    {
        msgCache.add(buffer);
    }

    private byte[] dequeueCache(byte filter)
    {
        for (int i=0; i<msgCache.size(); i++) {
            if (filter == 0x00 || msgCache.get(i)[1] == filter) {
                return msgCache.remove(i);
            }
        }

        return null;
    }

    private static int byteToUnsignedInt(byte in)
    {
        return in & 0xff;
    }
}

