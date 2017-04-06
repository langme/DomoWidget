package illimiteremi.domowidget.wear;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONObject;

import java.util.List;

import static illimiteremi.domowidget.wear.WearConstants.IS_CONNECTED;
import static illimiteremi.domowidget.wear.WearConstants.JSON_ASK_TYPE;
import static illimiteremi.domowidget.wear.WearConstants.JSON_MESSAGE;

/**
 * Created by xzaq496 on 05/04/2017.
 */

public class WearGoogleApi implements com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    private static final String        TAG      = "[DOMO_WEAR_GOOGLE_API]";
    private String                     nodeId;
    private OnGoogleApiMessageReceived mListener;
    private com.google.android.gms.common.api.GoogleApiClient googleApiClient;

    /**
     * interface de onGoogleApiMessageReceived
     */
    public interface OnGoogleApiMessageReceived {
        void onMessageReceive(String messageType, String message);
    }

    /**
     * setOnMessageReceivedListener
     * @param listener
     */
    public void setOnMessageReceivedListener(OnGoogleApiMessageReceived listener) {
        mListener = listener;
    }

    /**
     * WearGoogleApi
     * @param context
     */
    public WearGoogleApi(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    googleApiClient = new com.google.android.gms.common.api.GoogleApiClient.Builder(context)
                            .addApi(Wearable.API)
                            .addConnectionCallbacks(WearGoogleApi.this)
                            .build();
                    Wearable.MessageApi.addListener(googleApiClient, WearGoogleApi.this);
                    googleApiClient.connect();

                } catch (Exception e){
                    Log.e(TAG, "Erreur : " + e);
                }
            }
        }).start();
    }

    /**
     * sendMessage
     * @param messageType
     * @param message
     */
    public void sendMessage(final String messageType, final String message) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonSendMessage = new JSONObject();
                        jsonSendMessage.put(JSON_ASK_TYPE, messageType);
                        jsonSendMessage.put(JSON_MESSAGE, message);
                        Log.d(TAG, "sendMessage() => " + jsonSendMessage.toString());
                        // googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                        Wearable.MessageApi.sendMessage(googleApiClient, nodeId, jsonSendMessage.toString(), null);
                    } catch (Exception e){
                        Log.e(TAG, "Erreur : " + e);
                    }
                }
            }).start();
        }
    }

    /**
     * disconnect
     */
    public void disconnect() {
        mListener.onMessageReceive(IS_CONNECTED, "FALSE");
        // Envoi information connection au listener
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                googleApiClient.blockingConnect();
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                    Log.d(TAG, "connecté à : " + nodes.get(0).getDisplayName());
                    // Envoi information connection au listener
                    mListener.onMessageReceive(IS_CONNECTED, "TRUE");
                }
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String jsoMessage = messageEvent.getPath();
        Log.d(TAG, "onMessageReceived => " + jsoMessage);
        try {
            JSONObject jsnObject = new JSONObject(jsoMessage);
            String asktype = jsnObject.getString(JSON_ASK_TYPE);
            String message = jsnObject.getString(JSON_MESSAGE);
            mListener.onMessageReceive(asktype, message);
        } catch (Exception e) {
            Log.e(TAG , "Erreur : " + e);
        }
    }
}
