package illimiteremi.domowidget;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetLocation.LocationWidget;
import illimiteremi.domowidget.DomoWidgetLocation.WidgetLocationProvider;
import illimiteremi.domowidget.DomoWidgetVocal.VocalService;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION_WIDGET_CHANGED;

public class DomoService extends Service {

    private static final String TAG = "[DOMO_SERVICE]";
    private Context context;
    private BroadcastReceiver mReceiver;
    private LocationManager mLocationManager;
    private ArrayList<ListenerGPS> listenerGPSs;

    private class ListenerGPS implements LocationListener {

        private final LocationWidget widget;

        public ListenerGPS(LocationWidget widget) {
            this.widget = widget;
        }

        @Override
        public void onLocationChanged(Location location) {
            // Log.d(TAG, "onLocationChanged : Widget = " + widget.getDomoName());
            try {
                widget.setDomoLocation(location.getLatitude() + "," + location.getLongitude());
                // Envoi de la position à toute les box
                String action = "&" + widget.getDomoAction() + "&value=" + widget.getDomoLocation();
                BoxSetting boxSetting = widget.getSelectedBox();

                if (boxSetting != null) {
                    // Envoi de la postion à la box
                    DomoHttp.httpRequest(context, boxSetting, action, null, widget);
                    Log.d(TAG, widget.getDomoName() + " => " + widget.getDomoLocation());
                    // Sauvegarde de la postion pour le widget
                    DomoUtils.updateObjet(getApplicationContext(), widget);
                    // Mise à jour des widgets LOCATION
                    Intent updateIntent = new Intent(getApplicationContext(), WidgetLocationProvider.class);
                    updateIntent.setAction("android.appwidget.action.LOCATION_WIDGET_UPDATE_ALL");
                    sendBroadcast(updateIntent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur : " + e);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Log.d(TAG, "onStatusChanged : Widget = " + widget.getDomoName());
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Log.d(TAG, "onProviderEnabled : Widget = " + widget.getDomoName());
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Log.d(TAG, "onProviderDisabled : Widget = " + widget.getDomoName());
        }
    }

    /**
     * Constructeur
     */
    public DomoService() {
        Log.d(TAG, "Démarrage du service DOMO-WIDGET...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        // Creation du receiver de maj des widgets
        createBroadcastReceiver();
        createLocation();
        super.onCreate();
    }

    /**
     * Creation du service de location
     */
    private void createLocation() {
        // Gestion du service GPS
        try {
            ArrayList<Object> objects = DomoUtils.getAllObjet(context, LOCATION);
            ArrayList<LocationWidget> locationWidgets = new ArrayList<>();
            listenerGPSs = new ArrayList<>();
            for (Object locationObject : objects) {
                locationWidgets.add((LocationWidget) locationObject);
            }

            if (locationWidgets.size() != 0) {
                // Pour chaque Widget Location
                mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                for (final LocationWidget widget : locationWidgets) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "Problème de permission ACCESS_FINE_LOCATION !");
                            return;
                        } else {
                            Log.d(TAG, "Construction du listener GPS : " + widget.getDomoName()
                                    + " / " + widget.getDomoTimeOut()
                                    + "min / " + widget.getDomoDistance()
                                    + "m / " + widget.getDomoProvider());
                            // ListenerGPS - TEMPS
                            ListenerGPS timeListener = new ListenerGPS(widget);
                            listenerGPSs.add(timeListener);
                            mLocationManager.requestLocationUpdates(widget.getDomoProvider(), TimeUnit.MINUTES.toMillis(widget.getDomoTimeOut()), 0, timeListener);
                            // ListenerGPS - DISTANCE
                            ListenerGPS distanceListener = new ListenerGPS(widget);
                            listenerGPSs.add(distanceListener);
                            mLocationManager.requestLocationUpdates(widget.getDomoProvider(), TimeUnit.SECONDS.toMillis(10), widget.getDomoDistance(), distanceListener);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur : " + e);
                    }
                }
            }
        }  catch (SQLiteDatabaseLockedException e) {
            Log.e(TAG, "Erreur : La base n'est pas disponilbe !");
        }
    }

    /**
     * Création du BroadcastReceiver de Mise à jour du Widget
     */
    private void createBroadcastReceiver() {

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                Log.d(TAG, action);
                if (action.contentEquals("android.intent.action.SCREEN_ON") || action.contentEquals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    DomoUtils.updateAllWidget(context);
                    DomoHttp.checkWifi(context);
                    DomoUtils.startVoiceService(context, false);
                }

                if (action.contentEquals("android.intent.action.SCREEN_OFF")) {
                    DomoUtils.stopVoiceService(context);
                }

            }
        };

        // Construction du receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        // Log.d(TAG, "Création BroadcastReceiver...");

        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // Log.d(TAG, "onBind...");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        // Log.d(TAG, "Service OnDestroy");
        unregisterReceiver(mReceiver);
        // Log.d(TAG, "Nombre de Listener GPS à détruire : " + listenerGPSs.size());
        if (listenerGPSs.size() != 0) {
            for (ListenerGPS listenerGPS : listenerGPSs) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Problème de permission ACCESS_FINE_LOCATION !");
                    return;
                } else {
                    mLocationManager.removeUpdates(listenerGPS);
                }
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        // Log.d(TAG, "onLowMemory !");
        super.onLowMemory();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Log.d(TAG, "onTaskRemoved : " + rootIntent.toString());
        super.onTaskRemoved(rootIntent);
        Intent msgIntent = new Intent(context, DomoReceiver.class);
        msgIntent.setAction(LOCATION_WIDGET_CHANGED);
        context.sendBroadcast(msgIntent);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Log.d(TAG, "onConfigurationChanged...");
        super.onConfigurationChanged(newConfig);
    }
}
