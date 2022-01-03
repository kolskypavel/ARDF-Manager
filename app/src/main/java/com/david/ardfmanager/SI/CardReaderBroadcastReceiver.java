package com.david.ardfmanager.SI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.readouts.SIReadout;
import com.david.ardfmanager.split.SplitsActivity;


public class CardReaderBroadcastReceiver extends BroadcastReceiver {

    Activity activity;
    private long deviceId = 0;
    boolean flag = false;

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
                SIReadout siReadout = new SIReadout(cardEntry.cardId, cardEntry.startTime, cardEntry.finishTime, cardEntry.checkTime, cardEntry.punches);
                flag = false;
                for(SIReadout sir : MainActivity.siReadoutList){
                    if(siReadout.getCardId() == sir.getCardId() && siReadout.getStartTime() == sir.getStartTime()){
                        flag = true;
                        System.out.println("stejny");
                        Toast.makeText(activity.getApplicationContext(), "tohle uz je vycteny!", Toast.LENGTH_SHORT).show();
                    }else{
                        System.out.println("jiny");
                    }

                }

                if(!flag){
                    MainActivity.siReadoutList.add(siReadout);
                    MainActivity.setAllAdaptersAndSave();
                }else{
                    Toast.makeText(MainActivity.context, "Uz vycteno", Toast.LENGTH_SHORT).show();
                }

                /*if (cardEntry.startTime != 0) {
                    long timeDiff = cardEntry.finishTime - cardEntry.startTime;
                    long minutes = timeDiff / (60*1000);
                    long seconds = (timeDiff - minutes * 60 * 1000) / 1000;
                    long hundreds = (timeDiff - minutes * 60 * 1000 - seconds * 1000);
                    StringBuilder tmpPunches = new StringBuilder();
                    for (int i=0; i<cardEntry.punches.size(); i++) {
                        if (i > 0)
                            tmpPunches.append(", ");
                        tmpPunches.append(cardEntry.punches.get(i).code);
                    }
                    alertDialog.setTitle("Vyčteno: " + String.format("%d:%02d.%02d", minutes, seconds, hundreds));
                    alertDialog.setMessage(tmpPunches);
                }else{
                    alertDialog.setTitle("Lol nejde neni cas");
                    alertDialog.setMessage("");
                }*/
                //Toast.makeText(context, String.valueOf(cardEntry.cardId), Toast.LENGTH_SHORT).show();
                MainActivity.SIStatusText.setText(String.format("Device (%d) card %d read", deviceId, cardEntry.cardId));
                break;
        }
    }
}