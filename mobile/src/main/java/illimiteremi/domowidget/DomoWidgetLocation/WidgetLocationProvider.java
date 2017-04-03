package illimiteremi.domowidget.DomoWidgetLocation;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTENT_NO_DATA;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCK_TIME;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_LOCATION_WIDGET;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetLocationProvider extends AppWidgetProvider {

    private static final String TAG = "[DOMO_GPS_PROVIDER]";

    /**
     * Création du widget et ses actions
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        Log.d(TAG, "updateAppWidget N°" + appWidgetId);

        // Construction du Widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.location_widget);

        // Intent pour la mise à jour de la possition
        Intent intent = new Intent(context, WidgetLocationProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        PendingIntent locationPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.widgetButton, locationPendingIntent);

        // Image du GPS
        int ressourceId = context.getResources().getIdentifier("ic_gps_white", "drawable", context.getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), ressourceId);
        views.setImageViewBitmap(R.id.widgetButton, bitmap);

        // Mise à jour des informations du Widget
        new LocationUtils(context, appWidgetManager, views, appWidgetId);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        // Récuperation de l'identifiant du Widget
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        // Action mise à jour des widgets GPS
        if (action.contentEquals(UPDATE_ALL_LOCATION_WIDGET)) {
            new Thread(new Runnable() {
                public void run() {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetLocationProvider.class.getName());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }).start();
        }

        if (extras != null) {
            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            if (appWidgetId != 0) {

                // Log.d(TAG, "appWidgetId = " + appWidgetId);

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.location_widget);
                final AppWidgetManager appWidgetManager  = AppWidgetManager.getInstance(context);

                // Action mise à jour du widget quand pas de connection
                if (action.contentEquals(INTENT_NO_DATA)) {
                    new Thread(new Runnable() {
                        public void run() {
                            views.setTextColor(R.id.widgetName, Color.RED);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            try {
                                Thread.sleep(LOCK_TIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            views.setTextColor(R.id.widgetName, Color.WHITE);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    }).start();
                }

                // Action mise à jour du widget
                if (action.contentEquals("android.appwidget.action.APPWIDGET_UPDATE")) {
                    new Thread(new Runnable() {
                        public void run() {
                            updateAppWidget(context, appWidgetManager, appWidgetId);
                        }
                    }).start();
                }
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DomoUtils.startService(context, false);
        for (int appWidgetId : appWidgetIds) {
            // Vérification de la configuration du widget
            try {
                LocationWidget widget = (LocationWidget) DomoUtils.getObjetById(context, new LocationWidget(context, appWidgetId));
                if (widget != null) {
                    // Log.d(TAG, "onUpdate() => Widget LocationWidget : " + appWidgetId);
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            } catch (SQLiteDatabaseLockedException e) {
                Log.e(TAG, "Erreur : La base n'est pas disponilbe !");
            }
        }
    }

}

