package com.david.ardfmanager.SI;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static java.lang.Math.min;

public class CardReader extends AsyncTask<String, String, String> {
    public static final String EVENT_IDENTIFIER = "CardReader-Event";
    public static class CardEntry implements Parcelable {

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public CardEntry createFromParcel(Parcel in) {
                return new CardEntry(in);
            }

            public CardEntry[] newArray(int size) {
                return new CardEntry[size];
            }
        };

        public long cardId;
        public long startTime;
        public long finishTime;
        public long checkTime;
        ArrayList<Punch> punches;

        public CardEntry() {
            punches = new ArrayList<Punch>();
        }

        public CardEntry(Parcel in) {
            this.cardId = in.readInt();
            this.startTime = in.readInt();
            this.finishTime = in.readInt();
            this.checkTime = in.readInt();
            this.punches = in.readArrayList(Punch.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.cardId);
            dest.writeLong(this.startTime);
            dest.writeLong(this.finishTime);
            dest.writeLong(this.checkTime);
            dest.writeList(this.punches);
        }
    }
    public enum Event {
        DeviceDetected,
        ReadStarted,
        ReadCanceled,
        Readout
    }
    enum TaskState {
        Probe,
        WaitPerm,
        ProbeSI,
        ReadingCard,
        Quit
    }

    private final int HALF_DAY = 12*3600000;
    private final String TAG = SIProtocol.class.getSimpleName();
    private Context context;
    private long zeroTimeWeekDay;
    private long zeroTimeBase;
    private UsbManager manager;
    UsbDevice device;
    private TaskState taskState;
    private SIReader siReader;

    private static class UsbBroadcastReceiver extends BroadcastReceiver {
        // logging tag
        private final String TAG = UsbBroadcastReceiver.class.getSimpleName();
        // usb permission tag name
        public static final String USB_PERMISSION ="com.svenstorp.siplayground.USB_PERMISSION";
        private CardReader parent;

        public UsbBroadcastReceiver(CardReader parent) {
            this.parent = parent;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (USB_PERMISSION.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.d(TAG, "Permission to connect to device granted");
                    parent.taskState = TaskState.ProbeSI;
                }
                else {
                    Log.d(TAG, "Permission to connect to device denied");
                }
            }
        }
    }

    public CardReader(Context context, Calendar zeroTime) {
        this.context = context;
        this.manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.taskState = TaskState.Probe;

        this.zeroTimeBase = zeroTime.get(Calendar.HOUR_OF_DAY)*3600000 + zeroTime.get(Calendar.MINUTE)*60000 + zeroTime.get(Calendar.SECOND)*1000;
        this.zeroTimeWeekDay = zeroTime.get(Calendar.DAY_OF_WEEK) % 7;
    }

    @Override
    protected String doInBackground(String... params) {

        while(this.taskState != TaskState.Quit) {
            if (this.taskState == TaskState.Probe) {
                if (this.probe()) {
                    Log.d(TAG, "Found USB device, waiting for permissions");
                }
            }
            if (this.taskState == TaskState.Probe || this.taskState == TaskState.WaitPerm) {
                try {
                    Thread.sleep(5000);
                }
                catch(InterruptedException e)
                {
                    Log.d(TAG, "thread sleep interrupted");
                }
            }
            if (this.taskState == TaskState.ProbeSI) {
                Log.d(TAG, "Probing for SI device under USB device");
                UsbDeviceConnection conn = manager.openDevice(device);
                if (conn != null) {
                    UsbSerialDevice port = UsbSerialDevice.createUsbSerialDevice(device, conn);
                    this.siReader = new SIReader(port);
                    if (this.siReader.probeDevice()) {
                        // Found device, continue to card reading!
                        SIReader.Info deviceInfo = this.siReader.getDeviceInfo();
                        Log.d(TAG, "Found device (serial: " + deviceInfo.serialNo + "), continue to reading card");
                        this.taskState = TaskState.ReadingCard;
                        this.emitDeviceDetected(deviceInfo);
                    }
                    else {
                        this.taskState = TaskState.Probe;
                    }
                }
            }
            if (this.taskState == TaskState.ReadingCard) {
                this.readCardOnce();
            }
        }

        if(siReader != null) {
            siReader.close();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(String... text) {

    }

    private boolean probe()
    {
        HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                if (UsbSerialDevice.isSupported(device)) {
                    // There is a supported device connected - request permission to access it.
                    // Create intent (used to get USB permissions)
                    this.taskState = TaskState.WaitPerm;
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbBroadcastReceiver.USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(UsbBroadcastReceiver.USB_PERMISSION);
                    UsbBroadcastReceiver usbReceiver = new UsbBroadcastReceiver(this);
                    context.registerReceiver(usbReceiver, filter);
                    manager.requestPermission(device, pendingIntent);
                    return true;
                } else {
                    device = null;
                }
            }
        }

        return false;
    }

    private void readCardOnce()
    {
        CardEntry entry;
        byte[] msg;
        byte[] reply;
        SIProtocol proto = siReader.getProtoObj();
        SIReader.SiCardInfo cardInfo = new SIReader.SiCardInfo();

        if (siReader.waitForCardInsert(500, cardInfo)) {
            switch(cardInfo.format) {
                case (byte)0xe5: {
                    entry = new CardEntry();

                    // EMIT card reading
                    this.emitReadStarted(cardInfo);

                    proto.writeMsg((byte) 0xb1, null, true);
                    reply = proto.readMsg(5000, (byte) 0xb1);
                    if (reply != null && card5EntryParse(reply, entry)) {
                        proto.writeAck();
                        // EMIT card read out
                        this.emitReadout(entry);
                    } else {
                        // EMIT card read failed
                        this.emitReadCanceled();
                    }
                    break;
                }
                case (byte)0xe6: {
                    entry = new CardEntry();
                    reply = new byte[7 * 128];

                    // EMIT card reading
                    this.emitReadStarted(cardInfo);

                    msg = new byte[]{0x00};
                    byte[] blocks = new byte[]{0, 6, 7, 2, 3, 4, 5};
                    for (int i = 0; i < 7; i++) {
                        msg[0] = blocks[i];
                        proto.writeMsg((byte) 0xe1, msg, true);
                        byte[] tmpReply = proto.readMsg(5000, (byte) 0xe1);
                        if (tmpReply == null || tmpReply.length != 128 + 6 + 3) {
                            // EMIT card read failed
                            reply = null;
                            this.emitReadCanceled();
                            break;
                        }
                        System.arraycopy(tmpReply, 6, reply, i * 128, 128);
                        if (i > 0) {
                            if (tmpReply[124] == (byte) 0xee &&
                                    tmpReply[125] == (byte) 0xee &&
                                    tmpReply[126] == (byte) 0xee &&
                                    tmpReply[127] == (byte) 0xee) {
                                // Stop reading, no more punches
                                break;
                            }
                        }
                    }
                    if (reply != null && card6EntryParse(reply, entry)) {
                        proto.writeAck();
                        // EMIT card readout
                        this.emitReadout(entry);
                    } else {
                        // EMIT card read failed
                        this.emitReadCanceled();
                    }
                    break;
                }
                case (byte)0xe8: {
                    entry = new CardEntry();

                    // EMIT card reading
                    this.emitReadStarted(cardInfo);

                    msg = new byte[]{0x00};
                    proto.writeMsg((byte) 0xef, msg, true);
                    byte[] tmpReply = proto.readMsg(5000, (byte) 0xef);
                    if (tmpReply == null || tmpReply.length != 128 + 6 + 3) {
                        // EMIT card read failed
                        this.emitReadCanceled();
                        break;
                    }

                    int series = tmpReply[24] & 0x0f;
                    int nextBlock = 1;
                    int blockCount = 1;
                    if (series == 0x0f) {
                        // siac
                        nextBlock = 4;
                        blockCount = (tmpReply[22] + 31) / 32;
                    }
                    reply = new byte[128*(1+blockCount)];
                    System.arraycopy(tmpReply, 6, reply, 0, 128);

                    for (int i=nextBlock; i<nextBlock+blockCount; i++) {
                        msg[0] = (byte)i;
                        proto.writeMsg((byte)0xef, msg, true);
                        tmpReply = proto.readMsg(5000, (byte)0xef);
                        if (tmpReply == null || tmpReply.length != 128 + 6 + 3) {
                            // EMIT card read failed
                            reply = null;
                            this.emitReadCanceled();
                            break;
                        }
                        System.arraycopy(tmpReply, 6, reply, (i-nextBlock+1)*128, 128);
                    }
                    if (reply != null && card9EntryParse(reply, entry)) {
                        proto.writeAck();
                        // EMIT card read out
                        this.emitReadout(entry);
                    } else {
                        // EMIT card read failed
                        this.emitReadCanceled();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private boolean card5EntryParse(byte[] data, CardEntry entry)
    {
        boolean ret = false;
        int offset = 0;
        if (data.length == 136) {
            // Start at data part
            offset += 5;
            // Get cardId
            if (data[offset+6] == 0x00 || data[offset+6] == 0x01) {
                entry.cardId = (byteToUnsignedInt(data[offset+4]) << 8) + byteToUnsignedInt(data[offset+5]);
            }
            else if (byteToUnsignedInt(data[offset+6]) < 5) {
                entry.cardId = byteToUnsignedInt(data[offset+6])*100000 + (byteToUnsignedInt(data[offset+4]) << 8) + byteToUnsignedInt(data[offset+5]);
            }
            else {
                entry.cardId = (byteToUnsignedInt(data[offset+6]) << 16) + (byteToUnsignedInt(data[offset+4]) << 8) + byteToUnsignedInt(data[offset+5]);
            }
            entry.startTime = (byteToUnsignedInt(data[offset+19]) << 8) + byteToUnsignedInt(data[offset+20]);
            entry.finishTime = (byteToUnsignedInt(data[offset+21]) << 8) + byteToUnsignedInt(data[offset+22]);
            entry.checkTime = (byteToUnsignedInt(data[offset+25]) << 8) + byteToUnsignedInt(data[offset+26]);
            int punchCount = byteToUnsignedInt(data[offset+23]) - 1;
            for (int i=0; i<punchCount && i<30; i++) {
                Punch punch = new Punch();
                int baseoffset = offset + 32 + (i/5)*16 + 1 + 3*(i%5);
                punch.code = byteToUnsignedInt(data[baseoffset]);
                punch.time = (byteToUnsignedInt(data[baseoffset+1]) << 8) + byteToUnsignedInt(data[baseoffset+2]);
                entry.punches.add(punch);
            }
            for (int i=30; i<punchCount; i++) {
                Punch punch = new Punch();
                int baseoffset = offset + 32 + (i-30)*16;
                punch.code = data[baseoffset];
                punch.time = 0;
                entry.punches.add(punch);
            }

            card5TimeAdjust(entry);

            ret = true;
        }

        return ret;
    }

    private boolean card6EntryParse(byte[] data, CardEntry entry)
    {
        entry.cardId = (byteToUnsignedInt(data[10]) << 24) | (byteToUnsignedInt(data[11]) << 16) | (byteToUnsignedInt(data[12]) << 8) | byteToUnsignedInt(data[13]);

        Punch startPunch = new Punch();
        Punch finishPunch = new Punch();
        Punch checkPunch = new Punch();
        parsePunch(Arrays.copyOfRange(data, 24, 28), startPunch);
        parsePunch(Arrays.copyOfRange(data, 20, 24), finishPunch);
        parsePunch(Arrays.copyOfRange(data, 28, 32), checkPunch);
        entry.startTime = startPunch.time;
        entry.finishTime = finishPunch.time;
        entry.checkTime = checkPunch.time;

        int punches = min(data[18], 192);
        for (int i=0; i<punches; i++) {
            Punch tmpPunch = new Punch();
            if (parsePunch(Arrays.copyOfRange(data, 128+4*i, 128+4*i+4), tmpPunch)) {
                entry.punches.add(tmpPunch);
            }
        }
        return true;
    }

    private boolean card9EntryParse(byte[] data, CardEntry entry)
    {
        entry.cardId = (byteToUnsignedInt(data[25]) << 16) | (byteToUnsignedInt(data[26]) << 8) | byteToUnsignedInt(data[27]);
        int series = data[24] & 0x0f;

        Punch startPunch = new Punch();
        Punch finishPunch = new Punch();
        Punch checkPunch = new Punch();
        parsePunch(Arrays.copyOfRange(data, 12, 16), startPunch);
        parsePunch(Arrays.copyOfRange(data, 16, 20), finishPunch);
        parsePunch(Arrays.copyOfRange(data, 8, 12), checkPunch);
        entry.startTime = startPunch.time;
        entry.finishTime = finishPunch.time;
        entry.checkTime = checkPunch.time;

        if (series == 1) {
            // SI card 9
            int punches = min(data[22], 50);
            for (int i=0; i<punches; i++) {
                Punch tmpPunch = new Punch();
                if (parsePunch(Arrays.copyOfRange(data, 14*4+4*i, 14*4+4*i+4), tmpPunch)) {
                    entry.punches.add(tmpPunch);
                }
            }
        }
        else if(series == 2) {
            // SI card 8
            int punches = min(data[22], 30);
            for (int i=0; i<punches; i++) {
                Punch tmpPunch = new Punch();
                if (parsePunch(Arrays.copyOfRange(data, 34*4+4*i, 34*4+4*i+4), tmpPunch)) {
                    entry.punches.add(tmpPunch);
                }
            }
        }
        else if(series == 4) {
            // pCard
            int punches = min(data[22], 20);
            for (int i=0; i<punches; i++) {
                Punch tmpPunch = new Punch();
                if (parsePunch(Arrays.copyOfRange(data, 44*4+4*i, 44*4+4*i+4), tmpPunch)) {
                    entry.punches.add(tmpPunch);
                }
            }
        }
        else if(series == 15) {
            // SI card 10, 11, siac
            int punches = min(data[22], 128);
            for (int i=0; i<punches; i++) {
                Punch tmpPunch = new Punch();
                if (parsePunch(Arrays.copyOfRange(data, 128+4*i, 128+4*i+4), tmpPunch)) {
                    entry.punches.add(tmpPunch);
                }
            }
        }

        return true;
    }

    private void card5TimeAdjust(CardEntry entry)
    {
        long pmOffset = (zeroTimeBase >= HALF_DAY) ? HALF_DAY : 0;

        if (entry.startTime != 0) {
            entry.startTime = entry.startTime * 1000 + pmOffset;
            if (entry.startTime < zeroTimeBase) {
                entry.startTime += HALF_DAY;
            }
            entry.startTime -= zeroTimeBase;
        }
        if (entry.checkTime != 0) {
            entry.checkTime = entry.checkTime * 1000 + pmOffset;
            if (entry.checkTime < zeroTimeBase) {
                entry.checkTime += HALF_DAY;
            }
            entry.checkTime -= zeroTimeBase;
        }
        long currentBase = pmOffset;
        long lastTime = zeroTimeBase;
        for (Punch punch : entry.punches) {
            long tmpTime = punch.time * 1000 + currentBase;
            //if (tmpTime < lastTime) {
            //    currentBase += HALF_DAY;
            //}
            //tmpTime = punch.time * 1000 + currentBase;
            punch.time = tmpTime - zeroTimeBase;
            lastTime = tmpTime;
        }
        long tmpTime = entry.finishTime * 1000 + currentBase;
        if (tmpTime < lastTime) {
            currentBase += HALF_DAY;
        }
        tmpTime = entry.finishTime * 1000 + currentBase;
        entry.finishTime = tmpTime - zeroTimeBase;
    }

    private boolean parsePunch(byte[] data, Punch punch)
    {
        if (data[0] == (byte)0xee && data[1] == (byte)0xee && data[2] == (byte)0xee && data[3] == (byte)0xee) {
            return false;
        }
        punch.code = byteToUnsignedInt(data[1]) + 256*((byteToUnsignedInt(data[0])>>6) & 0x03);

        long basetime = ((byteToUnsignedInt(data[2]) << 8) | byteToUnsignedInt(data[3])) * 1000;
        if ((data[0] & 0x01) == 0x01) {
            basetime += HALF_DAY;
        }
        int dayOfWeek = (data[0] >> 1) & 0x07;
        if (dayOfWeek < zeroTimeWeekDay) {
            dayOfWeek += 7;
        }
        dayOfWeek -= zeroTimeWeekDay;
        basetime += dayOfWeek * 24 * 3600 * 1000;
        basetime -= zeroTimeBase;

        punch.time = basetime;

        return true;
    }

    private void emitDeviceDetected(SIReader.Info deviceInfo) {
        Intent intent = new Intent(EVENT_IDENTIFIER);
        intent.putExtra("Event", Event.DeviceDetected);
        intent.putExtra("Serial", deviceInfo.serialNo);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void emitReadStarted(SIReader.SiCardInfo cardInfo) {
        Intent intent = new Intent(EVENT_IDENTIFIER);
        intent.putExtra("Event", Event.ReadStarted);
        intent.putExtra("CardId", cardInfo.cardId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void emitReadCanceled() {
        Intent intent = new Intent(EVENT_IDENTIFIER);
        intent.putExtra("Event", Event.ReadCanceled);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void emitReadout(CardEntry entry) {
        Intent intent = new Intent(EVENT_IDENTIFIER);
        intent.putExtra("Event", Event.Readout);
        intent.putExtra("Entry", entry);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static int byteToUnsignedInt(byte in)
    {
        return in & 0xff;
    }
}

