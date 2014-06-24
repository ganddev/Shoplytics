package de.bjoern.ahlfeld.shoplytics.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import de.bjoern.ahlfeld.shoplytics.activities.MainActivity;
import de.bjoern.ahlfeld.shoplytics.services.MessageReceivingService;

public class ExternalReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (!MainActivity.inBackground) {
				MessageReceivingService.sendToApp(extras, context);
			} else {
				MessageReceivingService.saveToLog(extras, context);
			}
		}
	}
}
