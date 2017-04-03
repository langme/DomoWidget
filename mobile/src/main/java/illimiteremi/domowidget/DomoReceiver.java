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

       String action = intent.getAction();
       Log.d(TAG, action);

       DomoUtils.startService(context, true);

    }
}
