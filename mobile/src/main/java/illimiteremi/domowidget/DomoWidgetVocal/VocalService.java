package illimiteremi.domowidget.DomoWidgetVocal;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import illimiteremi.domowidget.DomoUtils.DomoUtils;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.VOCAL;

public class VocalService extends Service implements edu.cmu.pocketsphinx.RecognitionListener{

    private static final String TAG           = "[DOMO_VOICE_SERVICE]";

    private static final String KWS_SEARCH    = "wakeup";

    private String              keyPhrase     = "";
    private int                 appWidgetId;


    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;

    @Override
    public void onCreate() {
        super.onCreate();

        ArrayList<Object> Objects = DomoUtils.getAllObjet(getApplicationContext(), VOCAL);

        for (Object vocalObject : Objects) {
            VocalWidget vocalWidget = ((VocalWidget) vocalObject);
            appWidgetId = vocalWidget.getDomoId();
            keyPhrase   = vocalWidget.getKeyPhrase();
        }

        // Check si KeyPhrase
        if (!keyPhrase.isEmpty()) {
            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... params) {
                    try {
                        Assets assets = new Assets(getApplicationContext());
                        File assetDir = assets.syncAssets();
                        setupRecognizer(assetDir);
                    } catch (IOException e) {
                        return e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception result) {
                    if (result != null) {
                        Log.e(TAG, "Failed to init recognizer " + result);
                    } else {
                        recognizer.stop();
                        recognizer.startListening(KWS_SEARCH);
                    }
                }
            }.execute();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        Log.d(TAG,"onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.equals(keyPhrase)) {
                recognizer.stop();
                recognizer.startListening(KWS_SEARCH);
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Log.d(TAG, "onResult : " + text);

            // Start interaction
            Intent voiceIntent = new Intent(getApplicationContext(), VocalActivity.class);
            voiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            voiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivity(voiceIntent);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d(TAG, "onError : " + e);
    }

    @Override
    public void onTimeout() {
        Log.d(TAG, "onTimeout");
    }

    /**
     * Configuration du Listener pocketsphinx
     * @param assetsDir
     * @throws IOException
     */
    private void setupRecognizer(File assetsDir) throws IOException {
        Log.d(TAG, "setupRecognizer");
        recognizer = SpeechRecognizerSetup.defaultSetup()
            //Set Dictionary and Acoustic Model files
            .setAcousticModel(new File(assetsDir, "fr-eu-ptm"))
            .setDictionary(new File(assetsDir, "fr.dict"))
            .setKeywordThreshold(1e-45f)
            .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, keyPhrase);
    }
}
