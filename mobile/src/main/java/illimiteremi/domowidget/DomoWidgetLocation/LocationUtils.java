package illimiteremi.domowidget.DomoWidgetLocation;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;

import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;

/**
 * Created by rcouturi on 28/06/2016.
 */
public class LocationUtils {

    private static final String TAG = "[DOMO_GPS_UTILS]";

    private final Context context;

    private       LocationWidget    widget;                 // Objet Widget
    private final RemoteViews       views;                  // Views du Widget
    private       BoxSetting        selectedBox;            // Box selectionnée
    private final AppWidgetManager  appWidgetManager;       // Widget Manager

    public LocationUtils(Context context, AppWidgetManager  appWidgetManager, RemoteViews views, Integer appWidgetId) {

        this.context          = context;
        this.appWidgetManager = appWidgetManager;
        this.views            = views;

        widget = (LocationWidget) DomoUtils.getObjetById(context, new LocationWidget(context, appWidgetId));

        if (widget != null) {
            selectedBox = widget.getSelectedBox();
        }
        updateWidgetView();
    }

    /**
     * Mise à jour de la wiew du widget
     */
    private void updateWidgetView() {
        Log.d(TAG, "Mise à jour du widget : " + widget.getDomoId() + " - " + widget.getDomoName());
        views.setTextViewText(R.id.widgetName, widget.getDomoName());
        views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);

        // Image GPS Vert
        int ressourceId = context.getResources().getIdentifier("ic_gps_green", "drawable", context.getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), ressourceId);
        views.setImageViewBitmap(R.id.widgetButton, bitmap);

        // Envoi de l'information à la box
        if (selectedBox != null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // get the last know location from your location manager.
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // Enregistrement de la postion en bdd (si disponible)
            Location location = locationManager.getLastKnownLocation(widget.getDomoProvider());
            Log.d(TAG, "Widget : " + widget.getDomoName() + " - getLastKnownLocation "  + location);
            if (location != null) {
                widget.setDomoLocation(location.getLatitude() + "," + location.getLongitude());
                DomoUtils.updateObjet(context, widget);
                // Envoi de la postion
                Log.d(TAG, widget.getDomoName() + " => " + widget.getDomoLocation());
                String action = "&" + widget.getDomoAction() + "&value=" + widget.getDomoLocation();
                DomoHttp.httpRequest(context, selectedBox, action, null, widget);
            }
        }

        // Mise à jour du widget
        appWidgetManager.updateAppWidget(widget.getDomoId(), views);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Image GPS Blanc
        ressourceId = context.getResources().getIdentifier("ic_gps_white", "drawable",  context.getPackageName());
        bitmap = BitmapFactory.decodeResource(context.getResources(), ressourceId);
        views.setImageViewBitmap(R.id.widgetButton, bitmap);

        // Mise à jour du widget
        appWidgetManager.updateAppWidget(widget.getDomoId(), views);
    }
}
