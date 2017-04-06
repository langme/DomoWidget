package illimiteremi.domowidget.wear;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SettingService  extends WearableListenerService {
    private static final String   TAG      = "[DOMO_WEAR_API_SERVICE]";

    private Context context;
    protected GoogleApiClient       mApiClient;       // API Google

    public SettingService() {
        Log.d(TAG, "DomoServiceWear");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();

        // Connection à l'API GOOGLE
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        // Ouvre une connexion vers la montre
        ConnectionResult connectionResult = mApiClient.blockingConnect(30, TimeUnit.SECONDS);
        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }
        // Traitement du message reçu
        final String askMsg = messageEvent.getPath();
        Log.d(TAG, "onMessageReceived : " + askMsg);

        // TO DO SAVE PREFERENCE
        // TO RESTART SERVICE
    }



}
