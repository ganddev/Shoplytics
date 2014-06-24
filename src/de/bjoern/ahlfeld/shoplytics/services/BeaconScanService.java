package de.bjoern.ahlfeld.shoplytics.services;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import de.bjoern.ahlfeld.shoplytics.R;
import de.bjoern.ahlfeld.shoplytics.api.ApiService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BeaconScanService extends Service {

	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
			ESTIMOTE_PROXIMITY_UUID, null, null);
	protected static final String TAG = BeaconScanService.class.getName();

	private BeaconManager beaconManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		beaconManager = new BeaconManager(this);
		// Default values are 5s of scanning and 25s of waiting time to save CPU
		// cycles.
		// In order for this demo to be more responsive and immediate we lower
		// down those values.
		beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(5), 0);

		// Should be invoked in #onCreate.
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				for (Beacon b : beacons) {
					if (Utils.proximityFromAccuracy(Utils.computeAccuracy(b))
							.equals(Utils.Proximity.NEAR)) {
						Log.d(TAG+"beacon-discovered", b.toString());
						SharedPreferences prefs = getApplicationContext().getSharedPreferences(
								getString(R.string.preferences),
								Context.MODE_MULTI_PROCESS);

						if (prefs != null
								&& !prefs.getBoolean(
										getString(R.string.first_launch), true)) {
							Log.d(TAG, "Notify api");
							ApiService.notifyCustomer(b);
						}
						
					}
				}
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Should be invoked in #onStart.
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStart");
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					Log.d(TAG, "service ready");
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// When no longer needed. Should be invoked in #onDestroy.
		if (beaconManager != null) {
			beaconManager.disconnect();
		}
		super.onDestroy();
	}
}
