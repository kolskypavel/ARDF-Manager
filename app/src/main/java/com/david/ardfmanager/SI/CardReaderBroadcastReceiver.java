package com.david.ardfmanager.SI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.competitors.Competitor;
import com.david.ardfmanager.readouts.SIReadout;

import static android.content.ContentValues.TAG;


public class CardReaderBroadcastReceiver extends BroadcastReceiver {
    
    String TAG = "card_reader_broadcast_receiver";

    Activity activity;
    private long deviceId = 0;
    int readoutIDCounter = 0;

    public CardReaderBroadcastReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        CardReader.Event event = (CardReader.Event)intent.getSerializableExtra("Event");
        switch(event) {
            case DeviceDetected:
                deviceId = intent.getLongExtra("Serial", 0);
                MainActivity.SIStatusText.setText("Device (" + deviceId + ") online");
                MainActivity.SIStatusText.setBackgroundColor(Color.GREEN);
                break;
            case ReadStarted:
                MainActivity.SIStatusText.setText("Device (" + deviceId + ") reading card " + intent.getLongExtra("CardId", 0) + "...");
                break;
            case ReadCanceled:
                MainActivity.SIStatusText.setText("Device (" + deviceId + ") online");
                break;
            case Readout:
                CardReader.CardEntry cardEntry = (CardReader.CardEntry)intent.getParcelableExtra("Entry");
                SIReadout siReadout = new SIReadout(readoutIDCounter++, cardEntry.cardId, cardEntry.startTime, cardEntry.finishTime, cardEntry.checkTime, cardEntry.punches);
                //todo: binary include
                checkAndAddReadout(siReadout);
                MainActivity.SIStatusText.setText(String.format("Device (%d) card %d read", deviceId, cardEntry.cardId));
                break;
        }
    }

    public void checkAndAddReadout(SIReadout siReadout){
        Log.i(TAG, "checkAndAddReadout: started");
        if(MainActivity.siReadoutList.isEmpty()){
            MainActivity.siReadoutList.add(siReadout);
            MainActivity.refreshAndSave();
        }
            for (SIReadout sir : MainActivity.siReadoutList) {
                if (siReadout.getCardId() == sir.getCardId() && siReadout.getStartTime() == sir.getStartTime()) {
                    Toast.makeText(activity.getApplicationContext(), "This readout already exists!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "checkAndAddReadout: a same readout exists!");
                } else {
                    MainActivity.siReadoutList.add(siReadout);
                    MainActivity.refreshAndSave();
                    Log.i(TAG, "checkAndAddReadout: readout added");
                    if(MainActivity.event.getCategoriesList().isEmpty()){
                        Log.e(TAG, "checkAndAddReadout: no competitors");
                    }else {
                        for (Competitor competitor : MainActivity.event.getCompetitorsList()) {
                            Log.i(TAG, "checkAndAddReadout: competitor num: " + competitor.getSINumber());
                            Log.i(TAG, "checkAndAddReadout: readout num: " + siReadout.getCardId());
                            if (competitor.getSINumber() == siReadout.getCardId()) {
                                competitor.setReadoutID(siReadout.getID());
                                MainActivity.refreshAndSave();
                                Log.i(TAG, "checkAndAddReadout: assigned readout id to competitor");
                                return;
                            }
                        }
                        Log.e(TAG, "checkAndAddReadout: did not assign id to competitor");
                    }
                }
        }
    }
}