package com.david.ardfmanager.SI;

import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

class SIReader {
    enum DeviceType {
        Unknown(0x00),
        Control(0x02),
        Start(0x03),
        Finish(0x04),
        Read(0x05),
        ClearStartNbr(0x06),
        Clear(0x07),
        Check(0x0a);

        private int num;

        public int getNum() {
            return this.num;
        }

        DeviceType(int num) {
            this.num = num;
        }
    }

    public static class Info {
        public DeviceType type;
        public boolean extendedMode;
        public int codeNo;
        public long serialNo;
    }

    public static class SiCardInfo {
        public long cardId;
        public byte format;
    }

    public class DeviceEntry
    {
        public String identifier;
        public String osName;
        public Info deviceInfo;
    }

    // logging tag
    private static final String TAG = SIReader.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.svenstorp.siplayground.USB_PERMISSION";

    private UsbSerialDevice port;
    private SIProtocol siprot;
    private Info deviceInfo;

    public SIReader(UsbSerialDevice port)
    {
        this.port = port;
    }

    public void close()
    {
        if (port != null) {
            port.syncClose();
        }
        port = null;
        siprot = null;
        deviceInfo = null;
    }

    public boolean isConnected()
    {
        return (port != null);
    }

    public Info getDeviceInfo()
    {
        return deviceInfo;
    }

    public SIProtocol getProtoObj()
    {
        return siprot;
    }

    public boolean waitForCardInsert(int timeout, SiCardInfo cardInfo)
    {
        if (siprot == null) {
            return false;
        }

        byte[] reply = siprot.readMsg(timeout);
        if (reply != null && reply.length > 0) {
            switch(reply[1]) {
                case (byte) 0xe5:
                case (byte) 0xe6:
                case (byte) 0xe8:
                    cardInfo.cardId = (byteToUnsignedInt(reply[6]) << 16) + (byteToUnsignedInt(reply[7]) << 8) + byteToUnsignedInt(reply[8]);
                    cardInfo.format = reply[1];
                    Log.d(TAG, "Got card inserted event (CardID: " + cardInfo.cardId + ")");
                    return true;
                case (byte) 0xe7:
                    int tmpCardId = (byteToUnsignedInt(reply[5]) << 24) + (byteToUnsignedInt(reply[6]) << 16) + (byteToUnsignedInt(reply[7]) << 8) + byteToUnsignedInt(reply[8]);
                    Log.d(TAG, "Got card removed event (CardID: " + tmpCardId + ")");
                    break;
                default:
                    Log.d(TAG, "Got unknown command waiting for card inserted event");
                    break;
            }
        }

        return false;
    }

    public void sendAck()
    {
        if (siprot != null) {
            siprot.writeAck();
        }
    }

    public void sendNak()
    {
        if (siprot != null) {
            siprot.writeNak();
        }
    }

    public boolean probeDevice()
    {
        boolean ret = false;
        byte[] msg;
        byte[] reply;

        siprot = new SIProtocol(port);

        port.syncOpen();
        port.setDataBits(UsbSerialInterface.DATA_BITS_8);
        port.setParity(UsbSerialInterface.PARITY_NONE);
        port.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

        // Start with determine baudrate
        port.setBaudRate(38400);
        msg = new byte[]{0x4d};
        siprot.writeMsg((byte)0xf0, msg, true);
        reply = siprot.readMsg(1000, (byte)0xf0);
        if (reply == null || reply.length == 0) {
            Log.d(TAG, "No response on high baudrate mode, trying low baudrate");
            port.setBaudRate(4800);
        }
        siprot.writeMsg((byte)0xf0, msg, true);
        reply = siprot.readMsg(1000, (byte)0xf0);
        if (reply != null && reply.length > 0) {
            Log.d(TAG, "Unit responded, reading device info");
            msg = new byte[]{0x00, 0x75};
            siprot.writeMsg((byte) 0x83, msg, true);
            reply = siprot.readMsg(6000, (byte) 0x83);

            if (reply != null && reply.length >= 124) {
                Log.d(TAG, "Got device info response");
                deviceInfo = new Info();
                deviceInfo.codeNo = (byteToUnsignedInt(reply[3]) << 8) + byteToUnsignedInt(reply[4]);
                deviceInfo.type = DeviceType.values()[reply[119]];
                deviceInfo.extendedMode = (reply[122] & 0x01) == 0x01;
                deviceInfo.serialNo = (byteToUnsignedInt(reply[6]) << 24) + (byteToUnsignedInt(reply[7]) << 16) + (byteToUnsignedInt(reply[8]) << 8) + byteToUnsignedInt(reply[9]);
                ret = true;
            } else {
                Log.d(TAG, "Invalid device info response, trying short info");

                msg = new byte[]{0x00, 0x07};
                siprot.writeMsg((byte) 0x83, msg, true);
                reply = siprot.readMsg(6000, (byte)0x83);

                if (reply != null && reply.length >= 10) {
                    Log.d(TAG, "Got device info response");
                    deviceInfo = new Info();
                    deviceInfo.codeNo = (byteToUnsignedInt(reply[3]) << 8) + byteToUnsignedInt(reply[4]);
                    deviceInfo.type = DeviceType.Unknown;
                    deviceInfo.extendedMode = false;
                    deviceInfo.serialNo = (byteToUnsignedInt(reply[6]) << 24) + (byteToUnsignedInt(reply[7]) << 16) + (byteToUnsignedInt(reply[8]) << 8) + byteToUnsignedInt(reply[9]);
                    ret = true;
                }
            }
        }

        if (!ret) {
            if (port != null) {
                port.syncClose();
            }
            port = null;
            siprot = null;
            deviceInfo = null;
        }

        return ret;
    }

    private static int byteToUnsignedInt(byte in)
    {
        return in & 0xff;
    }
}

