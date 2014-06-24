package de.bjoern.ahlfeld.shoplytics.activities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.BeaconManager.MonitoringListener;
import com.estimote.sdk.service.BeaconService;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import de.bjoern.ahlfeld.shoplytics.R;
import de.bjoern.ahlfeld.shoplytics.api.ApiService;
import de.bjoern.ahlfeld.shoplytics.services.BeaconScanService;
import de.bjoern.ahlfeld.shoplytics.services.MessageReceivingService;

public class MainActivity extends Activity {

	// Since this activity is SingleTop, there can only ever be one instance.
	// This variable corresponds to this instance.
	public static Boolean inBackground = true;
	private SharedPreferences savedValues;

	protected static final String TAG = "Shoplytics";

	
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
			null, null, null);

	private BeaconManager beaconManager;
	
	
	private String numOfMissedMessages;
	private TextView tView;
	
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		numOfMissedMessages = getString(R.string.num_of_missed_messages);
		setContentView(R.layout.activity_main);
		tView = (TextView) findViewById(R.id.tViewId);
		tView.setMovementMethod(new ScrollingMovementMethod());
		com.estimote.sdk.utils.L.enableDebugLogging(true);
		/*beaconManager = new BeaconManager(this);
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
						SharedPreferences prefs = getSharedPreferences(
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
		});*/
		startService(new Intent(this, MessageReceivingService.class));
		startService(new Intent(this, BeaconScanService.class));
	}

	@Override
	public void onStart() {
		super.onStart();
		// Should be invoked in #onStart.
		/*beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});*/
		
	}

	
	@Override
	public void onResume() {
		super.onResume();
		
		inBackground = false;
		savedValues = MessageReceivingService.savedValues;
		int numOfMissedMessages = 0;
		if (savedValues != null) {
			numOfMissedMessages = savedValues.getInt(this.numOfMissedMessages,
					0);
		}
		String newMessage = getMessage(numOfMissedMessages);
		if (newMessage != "") {
			Log.i("displaying message", newMessage);
			tView.append(newMessage);
		}
	}

	@Override
	public void onStop() {
		inBackground = true;
		/*try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot stop but it does not matter now", e);
		}*/
		super.onStop();
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	// If messages have been missed, check the backlog. Otherwise check the current intent for a new message.
    private String getMessage(int numOfMissedMessages) {
        String message = "";
        String linesOfMessageCount = getString(R.string.lines_of_message_count);
        if(numOfMissedMessages > 0){
            String plural = numOfMissedMessages > 1 ? "s" : "";
            Log.i("onResume","missed " + numOfMissedMessages + " message" + plural);
            tView.append("You missed " + numOfMissedMessages +" message" + plural + ". Your most recent was:\n");
            for(int i = 0; i < savedValues.getInt(linesOfMessageCount, 0); i++){
                String line = savedValues.getString("MessageLine"+i, "");
                message+= (line + "\n");
            }
            NotificationManager mNotification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification.cancel(R.string.notification_number);
            SharedPreferences.Editor editor=savedValues.edit();
            editor.putInt(this.numOfMissedMessages, 0);
            editor.putInt(linesOfMessageCount, 0);
            editor.commit();
        }
        else{
            Log.i("onResume","no missed messages");
            Intent intent = getIntent();
            if(intent!=null){
                Bundle extras = intent.getExtras();
                if(extras!=null){
                    for(String key: extras.keySet()){
                        message+= key + "=" + extras.getString(key) + "\n";
                    }
                }
            }
        }
        message+="\n";
        return message;
    }
    
	@Override
	public void onDestroy() {
		// When no longer needed. Should be invoked in #onDestroy.
		//beaconManager.disconnect();
		super.onDestroy();
	}
}
