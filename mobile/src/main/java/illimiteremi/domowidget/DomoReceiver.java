package illimiteremi.domowidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import illimiteremi.domowidget.DomoUtils.DomoUtils;

public class DomoReceiver extends BroadcastReceiver {

    private static final  String      TAG = "[DOMO_RECEIVER]";

    public DomoReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

       // <action android:name="android.intent.action.BOOT_COMPLETED" />
       // <action android:name="android.appwidget.action.LOCATION_WIDGET_CHANGED" />

       String action = intent.getAction();
       Log.d(TAG, action);

        if (intent.getAction().contains(Intent.ACTION_BOOT_COMPLETED)) {
            // Démarrage du service d'écoute vocal au Boot
            DomoUtils.startVoiceService(context, true);
        }

        // Démarrage du service GPS et update widget Boot et changement position GPS
        DomoUtils.startService(context, true);
    }
}
