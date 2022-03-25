package com.david.ardfmanager.SI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.competitors.Competitor;
import com.david.ardfmanager.readouts.SIReadout;


public class CardReaderBroadcastReceiver extends BroadcastReceiver {

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
                for(SIReadout sir : MainActivity.siReadoutList){
                    if(siReadout.getCardId() == sir.getCardId() && siReadout.getStartTime() == sir.getStartTime()){
                        Toast.makeText(activity.getApplicationContext(), "This readout already exists!", Toast.LENGTH_SHORT).show();
                    }else{
                        MainActivity.siReadoutList.add(siReadout);
                        MainActivity.refreshAndSave();
                        for(Competitor competitor : MainActivity.event.getCompetitorsList()){
                            if(competitor.getSINumber() == siReadout.getCardId()){
                                competitor.setReadoutID(siReadout.getID());
                                break;
                            }
                        }
                    }
                }
                MainActivity.SIStatusText.setText(String.format("Device (%d) card %d read", deviceId, cardEntry.cardId));
                break;
        }
    }
}