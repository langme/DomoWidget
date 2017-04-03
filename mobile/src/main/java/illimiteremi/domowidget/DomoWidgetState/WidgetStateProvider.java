package illimiteremi.domowidget.DomoWidgetState;

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
import android.widget.RemoteViews;

import java.util.ArrayList;

import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTENT_NO_DATA;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCK_TIME;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_STATE_WIDGET;


/**
 * Implementation of App Widget functionality.
 */
public class WidgetStateProvider extends AppWidgetProvider {

    private static final String TAG = "[DOMO_STATE_PROVIDER]";

    /**
     * Création du widget et ses actions
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Log.d(TAG, "updateAppWidget N°" + appWidgetId);

        // Construction du Widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.state_widget);

        // Création des actions du Widget
        // Intent pour le changement d'état du Widget
        Intent intent = new Intent(context, WidgetStateProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        PendingIntent statePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.widgetButton, statePendingIntent);

        // Récuperation des informations du Widget
        new StateUtils(context, appWidgetManager, views, appWidgetId);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        // Récuperation de l'identifiant du Widget
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        // Action mise à jour des widgets
        if (action.contentEquals(UPDATE_ALL_STATE_WIDGET)) {
            // Log.d(TAG, action);

            new Thread(new Runnable() {
                public void run() {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetStateProvider.class.getName());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                    ArrayList<Integer> newList = new ArrayList<>();
                    // Création de la liste des widgets à mettre à jour.
                    for (int appWidgetId : appWidgetIds) {
                        StateWidget stateWidget = new StateWidget(context, appWidgetId);
                        stateWidget = (StateWidget) DomoUtils.getObjetById(context, stateWidget);
                        if (stateWidget != null) {
                            if (stateWidget.getManuelUpdate().equals(0)) {
                                newList.add(stateWidget.getDomoId());
                            }
                        } else {
                            Log.e(TAG, "Identifiant widget non présent en BDD !");
                        }
                    }
                    // Arraylist to int[]
                    appWidgetIds = new int[newList.size()];
                    for (int i=0; i < appWidgetIds.length; i++) {
                        appWidgetIds[i] = newList.get(i);
                    }
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }).start();
        }

        if (extras != null) {
            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            if (appWidgetId != 0) {
                // Log.d(TAG, action + " - " + appWidgetId);

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.state_widget);
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
    public  void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DomoUtils.startService(context, false);
        for (int appWidgetId : appWidgetIds) {
            try {
                // Vérification de la configuration du widget
                StateWidget widget = (StateWidget) DomoUtils.getObjetById(context, new StateWidget(context, appWidgetId));
                if (widget != null) {
                        // Log.d(TAG, "onUpdate() => Widget State : " + appWidgetId);
                        updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            } catch (SQLiteDatabaseLockedException e) {
                Log.e(TAG, "Erreur : La base n'est pas disponilbe !");
            }

        }
    }
}

