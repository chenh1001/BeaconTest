package com.example.beacontest;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.UUID;

public class ScannerActivity extends AppCompatActivity implements BeaconConsumer  {

    protected static final String TAG = "ScannerActivity";

    private String cumulativeLog = "";
    private BeaconManager mBeaconManager;
    private EditText mTextView;
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mTextView = (EditText)findViewById(R.id.textView);

        Identifier myBeaconNamespaceId = Identifier.fromUuid(UUID.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"));
        region = new Region("Com4Com", myBeaconNamespaceId, null, null);

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        mBeaconManager.getBeaconParsers().clear();
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        mBeaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "Beacon Service Connected");

        mBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                updateLog("I just saw a beacon named " + region.getUniqueId() + " for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                updateLog("I no longer see a beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                updateLog("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE (" + state + ")"));
            }

        });

        try {
            mBeaconManager.startMonitoringBeaconsInRegion(region);

        } catch (RemoteException e) {
            Log.e(TAG, "Remote Exception Error: ");
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    public void updateLog(String line) {
        cumulativeLog += (line + "\n");
        runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(cumulativeLog);
            }
        });
    }
}