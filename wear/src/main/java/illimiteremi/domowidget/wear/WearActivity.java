package illimiteremi.domowidget.wear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.wear.WearConstants.WEAR_INTERACTION;

public class WearActivity extends Activity {

    private static final String   TAG                            = "[DOMO_WEAR_ACTIVITY]";
    private static final int      SPEECH_RECOGNIZER_REQUEST_CODE = 0;

    private Context                 context;
    private WearGoogleApi           wearGoogleApi;
    private DelayedConfirmationView mDelayedConfirmationView;
    private String                  recognizedText;
    private Boolean                 cancelMessage;

    private final WearGoogleApi.OnGoogleApiMessageReceived meesageReceiveListener = new WearGoogleApi.OnGoogleApiMessageReceived() {
        @Override
        public void onMessageReceive(String messageType, String message) {
            Log.d(TAG, "Message : " + messageType + " - " + message);
            if (messageType.contains(WEAR_INTERACTION)) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_RECOGNIZER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                recognizedText = results.get(0);
                Log.d(TAG, "Message : " + recognizedText);
                mDelayedConfirmationView.setVisibility(View.VISIBLE);
                mDelayedConfirmationView.start();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        setContentView(R.layout.activity_main);
        cancelMessage = false;

        // DelayedConfirmationView
        mDelayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        mDelayedConfirmationView.setVisibility(View.INVISIBLE);
        mDelayedConfirmationView.setTotalTimeMs(5000);
        mDelayedConfirmationView.setListener(
                new DelayedConfirmationView.DelayedConfirmationListener() {
                    @Override
                    public void onTimerFinished(View view) {
                        Log.d(TAG,"onTimerFinished()");
                        if (cancelMessage) {
                            cancelMessage = false;
                        } else {
                            wearGoogleApi.sendMessage(WEAR_INTERACTION, recognizedText);
                        }
                    }

                    @Override
                    public void onTimerSelected(View view) {
                        Log.d(TAG,"onTimerSelected()");
                        cancelMessage = true;
                        finish();
                    }
                });

        // onClick
        onClickMe(null);

        // Init GoogleApi
        wearGoogleApi = new WearGoogleApi(context);
        wearGoogleApi.setOnMessageReceivedListener(meesageReceiveListener);

        // start service
        Intent msgIntent = new Intent(context, SensorService.class);
        context.startService(msgIntent);
    }


    /**
     * onClickMe
     */
    public void onClickMe(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_RECOGNIZER_REQUEST_CODE);
    }
}
