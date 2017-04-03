package illimiteremi.domowidget.DomoWidgetToogle;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTENT_NO_DATA;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCK_TIME;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_TOOGLE_WIDGET;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetToogleProvider extends AppWidgetProvider {

    private static final String TAG = "[DOMO_TOOGLE_PROVIDER]";

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Log.d(TAG, "updateAppWidget N°" + appWidgetId);

        // Construction du Widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.toogle_widget);

        // Création des actions du Widget
        // Intent pour le changement d'état du Widget
        Intent toogleIntent = new Intent(context, WidgetToogleProvider.class);
        toogleIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        toogleIntent.setAction("android.appwidget.action.APPWIDGET_CHANGE");
        PendingIntent tooglePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, toogleIntent, 0);
        views.setOnClickPendingIntent(R.id.widgetButton, tooglePendingIntent);

        // Intent de déverrouillage
        toogleIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        toogleIntent.setAction("android.appwidget.action.APPWIDGET_UNLOCK");
        PendingIntent unlookPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, toogleIntent, 0);
        views.setOnClickPendingIntent(R.id.unlockButton, unlookPendingIntent);

        // Récuperation des informations du Widget
        new ToogleUtils(context, appWidgetManager, views, appWidgetId);

    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        // Récuperation de l'identifiant du Widget
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        // Action mise à jour des widgets
        if (action.contentEquals(UPDATE_ALL_TOOGLE_WIDGET)) {
            // Log.d(TAG, action);
            new Thread(new Runnable() {
                public void run() {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetToogleProvider.class.getName());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }).start();
        }

        if (extras != null) {
            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            if (appWidgetId != 0) {
                Log.d(TAG, action + " - " + appWidgetId);

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.toogle_widget);
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

                // Action sur widget (ON / OFF)
                if (action.contentEquals("android.appwidget.action.APPWIDGET_CHANGE")) {
                    new Thread(new Runnable() {
                        public void run() {
                            ToogleUtils domoUtils = new ToogleUtils(context, appWidgetManager, views, appWidgetId);
                            domoUtils.changeWidgetState();
                        }
                    }).start();

                }

                // Action unlock
                if (action.contentEquals("android.appwidget.action.APPWIDGET_UNLOCK")) {
                    new Thread(new Runnable() {
                        public void run() {
                            // Log.d(TAG, "Déverouillage du widget...");
                            views.setViewVisibility(R.id.unlockButton, View.INVISIBLE);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            // Attente avant de re-verouillage
                            try {
                                Thread.sleep(LOCK_TIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            views.setViewVisibility(R.id.unlockButton, View.VISIBLE);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
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
            try {
                // Vérification de la configuration du widget
                ToogleWidget toogleWidget = (ToogleWidget) DomoUtils.getObjetById(context, new ToogleWidget(context, appWidgetId));
                if (toogleWidget != null) {
                    // Log.d(TAG, "onUpdate() => Widget Toogle : " + appWidgetId);
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            } catch (SQLiteDatabaseLockedException e) {
                Log.e(TAG, "Erreur : La base n'est pas disponilbe !");
            }
        }
    }
}

