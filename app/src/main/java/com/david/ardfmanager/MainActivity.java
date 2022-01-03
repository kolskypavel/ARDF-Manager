package com.david.ardfmanager;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.david.ardfmanager.SI.CardReader;
import com.david.ardfmanager.SI.CardReaderBroadcastReceiver;
import com.david.ardfmanager.SI.Punch;
import com.david.ardfmanager.category.CategoryListAdapter;
import com.david.ardfmanager.competitors.CompetitorsListAdapter;
import com.david.ardfmanager.competitors.Competitors_fragment;
import com.david.ardfmanager.controlpoint.ControlPoint;
import com.david.ardfmanager.controlpoint.ControlPointAdapter;
import com.david.ardfmanager.controlpoint.ControlPoints_Fragment;
import com.david.ardfmanager.event.Event;
import com.david.ardfmanager.event.EventsManagerActivity;
import com.david.ardfmanager.readouts.SIReadout;
import com.david.ardfmanager.readouts.SIReadoutListAdapter;
import com.david.ardfmanager.readouts.Readouts_fragment;
import com.david.ardfmanager.category.Categories_fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    //ToDo: edit dialogs, saving right after object is created, readout time is broken af, trate somewhere, controlpointlist refactor
    //ToDo: predelat control point string jmeno, cislo input klavesnice misto pickeru

    //public static TracksListAdapter tracksListAdapter;
    public static ControlPointAdapter controlPointAdapter;
    public static CategoryListAdapter categoryListAdapter;
    public static CompetitorsListAdapter competitorsListAdapter;

    public static ArrayList<SIReadout> siReadoutList = new ArrayList<>();
    public static SIReadoutListAdapter siReadoutListAdapter;

    public static Event event;
    public static boolean EVENT_RUNNING = false;


    public static final int INTENT_ADD_TRACK = 1000;
    public static final int INTENT_ADD_COMPETITOR = 1001;

    String CHANNEL_ID = "notif_channel_1";

    public static String path = "";

    private Menu toolBarMenu; //menu in the top toolbar
    public static TextView SIStatusText; //bottom status text

    //notification shit
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    //toolbar nahore
    Toolbar toolbar;

    FloatingActionButton fab;
    BottomNavigationView navView;

    public static Calendar calendar = Calendar.getInstance();
    public static int MIN_BIRTH_YEAR = 1920;
    public static int MAX_BIRTH_YEAR = calendar.get(Calendar.YEAR);

    public static Context context;


    //SI VOLE
    private CardReaderBroadcastReceiver mMessageReceiver = new CardReaderBroadcastReceiver(MainActivity.this);
    private CardReader cardReader;

    //rozmery displeje
    public static DisplayMetrics displayMetrics;

    public static Resources resources;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar at the top
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);

        //bottom si status text
        SIStatusText = (TextView) findViewById(R.id.SIStatusText);
        SIStatusText.setTextColor(Color.WHITE);
        SIStatusText.setBackgroundColor(Color.RED);
        SIStatusText.setText("SI disconnected");


        //Create SIReader
        cardReader = new CardReader(this, Calendar.getInstance());
        cardReader.execute();

        // Set up local broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CardReader.EVENT_IDENTIFIER));

        //bottom fragment navigation controls
        navView = findViewById(R.id.nav_view);

        //rozmery displeje pro dialogy
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //resources
        resources = getResources();

        context = this;
        
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.controlPointsFragment,
                R.id.categories,
                R.id.competitors,
                R.id.readouts,
                R.id.results).build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener(){
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                System.out.println("Destination changed!");
                if(event != null) {
                    getSupportActionBar().setTitle(event.getTitle());
                }
                String label = destination.getLabel().toString();
                if(label == getResources().getString(R.string.title_results)){
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_share));
                } else {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
                }
            }
        });


        Intent intent = getIntent();
        event = (Event)intent.getSerializableExtra("event");
        toolbar.setTitle(event.getTitle());
        if(event.getCategoriesList().isEmpty()){
            Toast.makeText(MainActivity.this, "Nema kategorie", Toast.LENGTH_SHORT).show();
        }
        if(event.getCompetitorsList().isEmpty()){
            Toast.makeText(MainActivity.this, "Nema zavodniky", Toast.LENGTH_SHORT).show();
        }
        controlPointAdapter = new ControlPointAdapter(this, R.layout.controlpoint_view_layout, event.getControlPoints());
        categoryListAdapter = new CategoryListAdapter(this, R.layout.category_view_layout, event.getCategoriesList());
        //tracksListAdapter = new TracksListAdapter(this, R.layout.track_view_layout, event.getTracksList());
        competitorsListAdapter = new CompetitorsListAdapter(this, R.layout.competitor_view_layout, event.getCompetitorsList());
        siReadoutList.clear();
        siReadoutListAdapter = new SIReadoutListAdapter(this, R.layout.sireadout_view_layout, siReadoutList);
        setAllAdaptersAndSave();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String currentFragment = navController.getCurrentDestination().getLabel().toString();
                System.out.println(currentFragment);
                if(currentFragment == getResources().getString(R.string.title_tracks)){
                    Categories_fragment.showCategoryAddDialog(MainActivity.this, null);
                }else if(currentFragment == getResources().getString(R.string.title_competitors)){
                    Competitors_fragment.showCompetitorAddDialog(MainActivity.this, null);
                }else if(currentFragment == getResources().getString(R.string.title_readouts)){
                    ArrayList<Punch> punches = new ArrayList<>();
                    punches.add(new Punch(1,1000));
                    punches.add(new Punch(2,3000));
                    punches.add(new Punch(3,6000));
                    punches.add(new Punch(4,9000));
                    punches.add(new Punch(5,18000));
                    SIReadout siReadout = new SIReadout(1234, 10,100000, 5, punches);
                    siReadoutList.add(siReadout);
                }else if(currentFragment == getResources().getString(R.string.title_control_points)){
                    showAddControlPointDialog();
                }
                setAllAdaptersAndSave();
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String currentFragment = navController.getCurrentDestination().getLabel().toString();
                System.out.println(currentFragment);

                if(currentFragment == getResources().getString(R.string.title_tracks)){

                }else if(currentFragment == getResources().getString(R.string.title_competitors)){

                }else if(currentFragment == getResources().getString(R.string.title_readouts)){

                }else if(currentFragment == getResources().getString(R.string.title_control_points)){
                    for(int i = 0; i < 10; i++){
                        event.addControlPoint(new ControlPoint(String.valueOf(i), i, new Random().nextInt(2)));
                    }
                }
                setAllAdaptersAndSave();
                return false;
            }
        });

        ///////////////////////////////////////////////////////////////////////
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Running notification", NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription("Shows a notification when an event is running.");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(false);
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        //PendingIntent pokus = new PendingIntent(this, MainActivity.class);

         builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_run_circle_24)
                .setContentTitle(event.getTitle() + getResources().getString(R.string.running))//getResources().getString(R.string.ardf_running))
                .setContentText(getResources().getString(R.string.last_read_si))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line...Much longer text that cannot fit one line...Much longer text that cannot fit one line..."))
                .setOngoing(true);
                //.setContentIntent(pokus);

        //jmeno eventu
        //ardf m. je spusten, posl vyct. cip:, zavodniku vycteno: x z poctu zavodniku na trati, prepinac na print

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.toolBarMenu = menu;
        getMenuInflater().inflate(R.menu.appbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {   //handling of topbar buttons(start, stop, settings, help)
        switch (item.getItemId()) {
            case R.id.eventRunningToggleBtn:
                if(EVENT_RUNNING){
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setMessage("Opravdu chcete " + event.getTitle() + " ukončit?");
                    alertBuilder.setPositiveButton("Ano", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            toolBarMenu.getItem(0).setTitle(getResources().getString(R.string.start));
                            notificationManager.cancel(123);
                            Toast.makeText(MainActivity.this, "Event zastaven!", Toast.LENGTH_SHORT).show();
                            EVENT_RUNNING = !EVENT_RUNNING;
                        }
                    });
                    alertBuilder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }else{
                    toolBarMenu.getItem(0).setTitle(getResources().getString(R.string.stop));
                    notificationManager.notify(123, builder.build());
                    Toast.makeText(MainActivity.this, "Event spuštěn!", Toast.LENGTH_SHORT).show();
                    EVENT_RUNNING = !EVENT_RUNNING;
                }
                return true;

            case R.id.trackMacroButton:

                //EventsManagerActivity.saveEventToFile(event);
                setAllAdaptersAndSave();
                return true;

            case R.id.settingsBtn:
                Toast.makeText(this, "Settings!", Toast.LENGTH_LONG).show();
                return true;

            case R.id.helpBtn:
                Toast.makeText(this, "Napoveda!", Toast.LENGTH_LONG).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public static void setAllAdaptersAndSave() {
        if (Categories_fragment.mListView != null) {
            Categories_fragment.mListView.setAdapter(categoryListAdapter);
        }else{
            Log.d("fragments", "Categories fragment object is null");
        }

        if (Competitors_fragment.mListView != null) {
            Competitors_fragment.mListView.setAdapter(competitorsListAdapter);
        }else{
            Log.d("fragments", "Competitors fragment object is null");
        }

        if (Readouts_fragment.mListView != null) {
            Readouts_fragment.mListView.setAdapter(siReadoutListAdapter);
        }else{
            Log.d("fragments", "Readout fragment object is null");
        }

        if (ControlPoints_Fragment.mListView != null) {
            ControlPoints_Fragment.mListView.setAdapter(controlPointAdapter);
        }else{
            Log.d("fragments", "Control points fragment object is null");
        }
        Log.d("SAVING", "BY se ulozilo");
        EventsManagerActivity.saveEventToFile(event);
    }

    @Override
    public void onBackPressed(){    //dismiss back button when event is running
        if(!EVENT_RUNNING) {
            EventsManagerActivity.saveEventToFile(event);
            MainActivity.super.onBackPressed();
        }else{
            Toast.makeText(MainActivity.this, "Event je spuštěn!", Toast.LENGTH_SHORT).show();
        }
    }


    public static boolean checkFilled(Context c, EditText et){
        if(et.getText().toString().equals("")){
            et.setError(c.getResources().getString(R.string.required));
            return false;
        }else{
            return true;
        }
    }

    public static String longTimeToString(long time){
        long minutes = time / (60*1000);
        long seconds = (time - minutes * 60 * 1000) / 1000;
        long hundreds = (time - minutes * 60 * 1000 - seconds * 1000);
        return (String)String.valueOf(minutes) + ":" + String.valueOf(seconds) + "." + String.valueOf(hundreds);
    }

    public void showAddControlPointDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.new_control_point);
        final View customLayout = LayoutInflater.from(this).inflate(R.layout.dialog_add_punch, null);
        builder.setView(customLayout);

        final NumberPicker punchNumPick = customLayout.findViewById(R.id.birthYear);
        final NumberPicker punchCodePick = customLayout.findViewById(R.id.maxAgeNumPick);
        final RadioGroup typePicker = customLayout.findViewById(R.id.typePicker);
        final RadioButton rb_basic_cp = customLayout.findViewById(R.id.rb_basic_cp);
        final RadioButton rb_spec_cp = customLayout.findViewById(R.id.rb_spec_cp);
        final RadioButton rb_beacon_cp = customLayout.findViewById(R.id.rb_beacon_cp);

        punchNumPick.setMinValue(0);
        punchNumPick.setMaxValue(99);

        punchCodePick.setMinValue(0);
        punchCodePick.setMaxValue(99);

        builder.setPositiveButton(R.string.ok, null);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String number = ""+punchNumPick.getValue();
                int code = punchCodePick.getValue();
                int type;
                if(typePicker.getCheckedRadioButtonId() == rb_basic_cp.getId()){
                    type = 0;
                }else if(typePicker.getCheckedRadioButtonId() == rb_beacon_cp.getId()){
                    type = 1;
                }else if(typePicker.getCheckedRadioButtonId() == rb_spec_cp.getId()){
                    type = 2;
                }else{
                    type = -1;
                }

                ControlPoint cp = new ControlPoint(number, code, type);
                event.addControlPoint(cp);
                setAllAdaptersAndSave();
                alert.dismiss();
            }

        });
    }

}
