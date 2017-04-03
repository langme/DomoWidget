package illimiteremi.domowidget.DomoWear;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetVocal.VocalWidget;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTERCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.WEAR;


public class DomoServiceWear extends WearableListenerService {

    private static final String     TAG      = "[DOMO_WEAR]";
    private Context                 contex;
    protected GoogleApiClient       mApiClient;
    private WearSetting             wearSetting;      // Confifguration de l'env Wear
    private BoxSetting              boxSetting;       // Objet Box

    public DomoServiceWear() {
        Log.d(TAG, "DomoServiceWear");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.contex = getApplicationContext();
        Log.d(TAG, "onCreate()");

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();

        ArrayList<Object> wearObjects = DomoUtils.getAllObjet(contex, WEAR);
        if (wearObjects.size() != 0) {
            wearSetting = (WearSetting) wearObjects.get(0);
            boxSetting = new BoxSetting();
            boxSetting.setBoxId(wearSetting.getBoxId());
            boxSetting = (BoxSetting) DomoUtils.getObjetById(contex, boxSetting);
            Log.d(TAG, "Box associée à Wear " + boxSetting.getBoxName());
        } else {
            Log.e(TAG, "Pas de configuration Android wear !");
        }
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
        Log.d(TAG, "Message wear : " + askMsg);
        if (boxSetting != null) {
            final String answerMsg = DomoHttp.httpRequest(contex, boxSetting, INTERCATION + askMsg, null, null);
            Log.d(TAG, "Reponse wear : " + answerMsg);
            sendMessage(answerMsg);
        }

    }

    /**
     * Envoie un message à la montre
     * @param message message à transmettre
     */
    protected void sendMessage(final String message) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Envoie le message à tous les noeuds/montres connectées
                final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), message, null).await();
                }
            }
        }).start();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
    }


}
