package illimiteremi.domowidget.DomoWidgetVocal;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTERCATION;

public class VocalActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final String TAG                = "[DOMO_VOCAL_ACTIVITY]";
    private static final int    VOICE_REQUEST_CODE = 1234;

    private Context             context;

    private int                 idWidget;               // Id du widget

    private TextToSpeech        tts;                    // TextToSpeech
    private Boolean             ttsIsInit = false;      // Etat init tts

    private TextView            askTextView;            // Question du Toast
    private TextView            answerTextView;         // Reponse du Toast
    private View                layout;                 // Layout du Toast

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DomoUtils.startVoiceService(context, true);
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context       = getApplicationContext();

        Intent VocalIntent = new Intent(context, VocalService.class);
        context.stopService(VocalIntent);

        Bundle extras = getIntent().getExtras();
        idWidget      = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        LayoutInflater inflater = getLayoutInflater();
        layout = inflater.inflate(R.layout.vocal_toast, (ViewGroup) findViewById(R.id.toast_layout_root));
        layout.setMinimumWidth(WindowManager.LayoutParams.MATCH_PARENT);
        askTextView    = (TextView) layout.findViewById(R.id.ask);
        answerTextView = (TextView) layout.findViewById(R.id.answer);

        tts = new TextToSpeech(context, this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, context.getResources().getString(R.string.widget_vocal_speak));
        startActivityForResult(intent, VOICE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // Récuperation de la saisie vocal
            final String askMsg = matches.get(0).toString();
            Log.d(TAG, "Widget Vocal : " + idWidget + " => " + askMsg);

            // Récuperation des informations Widget Vocal
            try {
                VocalWidget widget = new VocalWidget(context, idWidget);
                widget = (VocalWidget) DomoUtils.getObjetById(context, widget);
                if (widget != null) {
                    BoxSetting boxSetting = widget.getSelectedBox();
                    // Execution de l'interaction
                    final String answerMsg = DomoHttp.httpRequest(context, boxSetting, INTERCATION + askMsg, null, widget);

                    // Lecture reponse
                    if (widget.getDomoSynthese().equals(1)) {
                        if (ttsIsInit) {
                            new Thread(new Runnable() {
                                public void run() {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        tts.speak(answerMsg, TextToSpeech.QUEUE_FLUSH, null, null);
                                    } else {
                                        tts.speak(answerMsg, TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }
                            }).start();
                        }
                    }
                    creatCustomToast(askMsg + "...", answerMsg);
                    DomoUtils.updateAllWidget(context);
                }
            } catch (Exception e) {
                Toast.makeText(this,"Erreur Widget...",Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erreur : " + e);
            }
        }
    }

    @Override
    public void onInit(int i) {
        // Log.d(TAG, "onInit");
        if (i == TextToSpeech.SUCCESS) {
            int language = tts.setLanguage(Locale.FRENCH);
            if (language == TextToSpeech.LANG_MISSING_DATA) {
                Toast.makeText(this,"Données vocales non présentes...",Toast.LENGTH_LONG).show();
            } else {
                ttsIsInit = true;
            }
        } else {
            Toast.makeText(this,"Erreur synthese vocale !",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * creatCustomToast
     * @param ask Question
     * @param answer Réponse
     */
    private void creatCustomToast(String ask, String answer) {
        Log.d(TAG,"creatCustomToast");
        askTextView.setText(ask);
        answerTextView.setText(answer);
        final Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM , 0, 250);
        toast.setView(layout);
        Log.d(TAG, ask + "..." + answer);

        // Calcul durée affichage
        long toastTime = TimeUnit.SECONDS.toMillis(answer.length() / 40);
        if (toastTime <= 1000) {
            toastTime = 3000;
        }


        new CountDownTimer(toastTime, 1000) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }
            public void onFinish() {
                toast.show();
            }
        }.start();
    }

}
