package illimiteremi.domowidget.wear;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.TimeUnit;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static illimiteremi.domowidget.wear.WearConstants.COL_SHAKE_LEVEL;
import static illimiteremi.domowidget.wear.WearConstants.COL_SHAKE_TIME_OUT;
import static illimiteremi.domowidget.wear.WearConstants.IS_CONNECTED;
import static illimiteremi.domowidget.wear.WearConstants.SETTING;
import static illimiteremi.domowidget.wear.WearConstants.WEAR_SETTING;

/**
 * Created by xzaq496 on 04/04/2017.
 */

public class SensorService extends Service implements SensorEventListener {

    private static final String   TAG      = "[DOMO_WEAR_SHAKE]";

    private SensorManager mSensorManager;
    private Sensor        mAccelerometer;

    private long          lastUpdate;
    private Integer       shakeLevel   = 5;
    private Integer       shakeTimeOut = 5;
    private WearGoogleApi wearGoogleApi;

    private Vibrator      vibrator;

    private final WearGoogleApi.OnGoogleApiMessageReceived meesageReceiveListener = new WearGoogleApi.OnGoogleApiMessageReceived() {
        @Override
        public void onMessageReceive(String messageType, String message) {
            Log.d(TAG, "Message : " + messageType + " - " + message);
            switch (messageType) {
                case IS_CONNECTED:
                    if (message == "TRUE") {
                        wearGoogleApi.sendMessage(SETTING, WEAR_SETTING);
                    }
                    break;
                case SETTING:
                    if (message == WEAR_SETTING) {
                        try {
                            JSONObject jsnObject = new JSONObject(message);
                            shakeTimeOut = Integer.parseInt(jsnObject.getString(COL_SHAKE_TIME_OUT));
                            shakeLevel = Integer.parseInt(jsnObject.getString(COL_SHAKE_LEVEL));
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur : " + e);
                        }
                    }
                    break;
                default:
                // NOTHING
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "Démarrage du service SensorService...");

        wearGoogleApi = new WearGoogleApi(getApplicationContext());
        wearGoogleApi.setOnMessageReceivedListener(meesageReceiveListener);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        super.onCreate();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * getAccelerometer
     * @param event
     */
    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Mouvement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        // Si niveau shake
        if (accelationSquareRoot >= shakeLevel) {
            if (actualTime - lastUpdate < java.util.concurrent.TimeUnit.SECONDS.toNanos(shakeTimeOut)) {
                return;
            }
            Log.d(TAG, "shake shake shake !");

            // Vibration
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        vibrator =  (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(100);
                    } catch (Exception e){
                        Log.e(TAG, "Erreur : " + e);
                    }
                }
            }).start();

            // start activity
            lastUpdate = actualTime;
            Intent intent = new Intent(this, WearActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
