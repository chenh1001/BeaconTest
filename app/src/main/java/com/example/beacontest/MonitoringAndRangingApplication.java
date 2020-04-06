package com.example.beacontest;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;
import java.util.UUID;

public class MonitoringAndRangingApplication extends Application implements BootstrapNotifier, BeaconConsumer, RangeNotifier {

    private static final String TAG = "MonitoringAndRanging";

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MainActivity rangingActivity = null;
    BeaconManager beaconManager;
    private String cumulativeLog = "";
    private Region region;

    @Override
    public void onCreate() {
        super.onCreate();
        //setContentView(R.layout.activity_scanner);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        //me
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        Identifier myBeaconNamespaceId = Identifier.fromUuid(UUID.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"));
        region = new Region("Com4Com", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        backgroundPowerSaver = new BackgroundPowerSaver(this);

        beaconManager.setBackgroundBetweenScanPeriod(500);
        beaconManager.setBackgroundScanPeriod(1100);
        beaconManager.setForegroundScanPeriod(1100);

        beaconManager.bind(this);
    }

    @Override
    public void didEnterRegion(Region region) {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            Log.d(TAG,"Found monitoring, Starting Ranging");
        }
        catch (RemoteException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Can't start ranging");
        }
    }

    @Override
    public void didExitRegion(Region region) {
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            Log.d(TAG,"Stopped Ranging");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG,"I have just switched from seeing/not seeing beacons: " + state);
        if(state == 1)
            didEnterRegion(region);
    }

    private void sendNotification(String text) {
        Toast.makeText(this,"notification",Toast.LENGTH_SHORT).show();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Beacon Reference Application")
                        .setContentText(text)
                        .setSmallIcon(R.drawable.icon);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() > 0) {
            for (Beacon b : beacons) {
                Log.e(TAG, "Beacon with Instance ID " + b.getId1() + " found! ");
                sendNotification("Beacon with my Instance ID found!");
            }
        }
    }


    /*@Override
    public void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        beaconManager.bind(this);
    }*/

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(this);
    }

}
