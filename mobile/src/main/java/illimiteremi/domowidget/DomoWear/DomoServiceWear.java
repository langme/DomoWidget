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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetVocal.VocalWidget;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTERCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.JSON_ASK_TYPE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.JSON_MESSAGE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.SETTING;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.WEAR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.WEAR_INTERACTION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.WEAR_SETTING;


public class DomoServiceWear extends WearableListenerService {

    private static final String     TAG      = "[DOMO_WEAR]";
    private Context                 context;
    protected GoogleApiClient       mApiClient;       // API Google
    private WearSetting             wearSetting;      // Confifguration Wear
    private BoxSetting              boxSetting;       // Confifguration Box

    public DomoServiceWear() {
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

        // Recuperation des informations de la configuration WEAR
        ArrayList<Object> wearObjects = DomoUtils.getAllObjet(context, WEAR);
        if (wearObjects.size() != 0) {
            wearSetting = (WearSetting) wearObjects.get(0);
            boxSetting = new BoxSetting();
            boxSetting.setBoxId(wearSetting.getBoxId());
            boxSetting = (BoxSetting) DomoUtils.getObjetById(context, boxSetting);
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
        Log.d(TAG, "Question wear : " + askMsg);

        try {
            JSONObject jsnObject = new JSONObject(askMsg);
            String asktype       = jsnObject.getString(JSON_ASK_TYPE);
            String message       = jsnObject.getString(JSON_MESSAGE);
            String answerMsg     = "";

            // Selon le type de question
            switch (asktype) {
                case SETTING:
                    // Cas configuration android wear
                    if (message == WEAR_SETTING) {
                        ArrayList<Object> wearObjects = DomoUtils.getAllObjet(context, WEAR);
                        if (wearObjects.size() != 0) {
                            wearSetting = (WearSetting) wearObjects.get(0);
                        } else {
                            wearSetting = null;
                        }
                    }
                    answerMsg = wearSetting.toJson().toString();
                    break;
                case WEAR_INTERACTION:
                    answerMsg = DomoHttp.httpRequest(context, boxSetting, INTERCATION + message, null, null);
                    break;
                default:
                    break;
            }
            // Création de la réponse en json
            jsnObject = new JSONObject();
            jsnObject.put(JSON_ASK_TYPE,asktype);
            jsnObject.put(JSON_MESSAGE, answerMsg);
            Log.d(TAG, "Reponse wear : " + jsnObject.toString());
            if (boxSetting != null) {
                sendMessage(jsnObject.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
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
