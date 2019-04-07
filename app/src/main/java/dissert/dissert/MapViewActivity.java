package dissert.dissert;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import dissert.dissert.utilities.ActivityRecognizer;
import dissert.dissert.utilities.SettingsManager;

public class MapViewActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationNewOverlay;
    private boolean permission;
    private Runnable runnable;
    private RuleController rc;

    private int interval;
    private boolean initialized;

    // Activity Recognition
    private ActivityRecognitionClient arclient;

    private Context ctx;

    private boolean observeRules;

    @Override
    protected void onCreate(Bundle instance) {
        super.onCreate(instance);

        initialized = false;
        rc = new RuleController(this);
        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);
        Toolbar appToolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(appToolbar);
        onMapReady();


        permission = false;
        observeRules = true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.automatic_adaptation:
                item.setChecked(!item.isChecked());
                String newState = "Automatic Adaptation ";
                if (item.isChecked()) {
                    Toast.makeText(this, newState + "ON", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, newState + "OFF", Toast.LENGTH_LONG).show();
                }
                observeRules = item.isChecked();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(MapViewActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("NOW"));
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void onMapReady() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        getLocationPermission();
        Toolbar appToolbar = findViewById(R.id.app_toolbar);
        SettingsManager sm = new SettingsManager();
        mapController = map.getController();
        sm.setSettings(appToolbar, mapController, map);
        interval = sm.getInterval();
        rc.setInterval(interval);
        observeRules = sm.startAdapting();
        myLocationNewOverlay = sm.getMlno();
        rc.setMapTheme(sm.getTheme());


        rc.addMap(map, findViewById(R.id.set));
        rc.addMarkers(getDrawable(R.drawable.marker), getDrawable(R.drawable.marker_notrelevant));


        arclient = ActivityRecognition.getClient(ctx);
        interval = sm.getInterval();
        Handler handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (observeRules) {
                    rc.checkChanges(mapController, myLocationNewOverlay, map);
                    interval = rc.checkInterval();
                }

                TextView li = findViewById(R.id.light);
                if (!initialized) {
                    if (rc.isInitialized()) {
                        if (rc.getCheckActivity()) {
                            registerHandler();
                        } else {
                            ((TextView) findViewById(R.id.movements)).setText("");
                        }

                        initialized = true;
                    }
                } else {
                    if (rc.getCheckLight()) {
                        float light = rc.getLight();
                        li.setText("Light: " + light + " lux");
                    } else {
                        li.setText("");
                    }
                }

                if (rc != null && myLocationNewOverlay != null && myLocationNewOverlay.getLastFix() != null)
                    ((TextView) findViewById(R.id.speed)).setText("Speed: " + myLocationNewOverlay.getLastFix().getSpeed() + " m/s");
                map.invalidate();
                handler.postDelayed(this, interval);
            }
        };
        runnable.run();

        if (permission)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                    .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED)
                return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission = true;
                    //activateUpdates();
                } else {
                    permission = false;
                }
                return;
            }
        }
    }


    private void getLocationPermission() {
        // Here, this Activity is the current activity
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted - let's try and obtain it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            permission = true;
        }
    }




    public void registerHandler() {
        Intent intent = new Intent(this, ActivityRecognizer.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);
        arclient.requestActivityUpdates(3_000L, pendingIntent);
        //Register to receive messages.
        //Observer (broadcastReceiver) receives intents with actions named "activity-update"
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("activity-update"));
        Log.d("ActivityRecognizer", "Registered.");
    }

    // Handler for received intents. Called when an Intent with an action
    // named "activity-update" is broadcasted.
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ActivityRecognizer", "MAPVIEW");
            String type = intent.getStringExtra("type");
            String confidence = intent.getStringExtra("confidence");
            ((TextView) findViewById(R.id.movements)).setText(type + ": " + confidence + "%");
            rc.setActivityState(type);
        }
    };
}