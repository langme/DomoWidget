package illimiteremi.domowidget.DomoWidgetMulti;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.widget.RemoteViews;

import illimiteremi.domowidget.DomoUtils.DomoConstants;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTENT_NO_DATA;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCK_TIME;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_MULTI_WIDGET;

/**
 * Implementation of App Widget functionality.
 */
public class MultiWidgetProvider extends AppWidgetProvider {

    private static final String TAG         = "[DOMO_MULTI_PROVIDER]";
    private static int          rowSelected = -1;

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Log.d(TAG, "updateAppWidget " + appWidgetId);

        // RemoteViews Service pour la ListView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.multi_widget);

        // Intent du clique sur image button
        Intent templateIntent = new Intent(context, MultiWidgetProvider.class);
        templateIntent.setAction("android.appwidget.action.RESS_BUTTON_PUSH");
        templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId );
        templateIntent.setData(Uri.parse(templateIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, templateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.ressListView, pendingIntent);

        // Intent refresh
        Intent intent = new Intent(context, MultiWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setAction("android.appwidget.action.MULTI_WIDGET_UPDATE");
        PendingIntent statePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widgetButton, statePendingIntent);

        // Intent Remote Service
        Intent svcIntent = new Intent(context, MultiWidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(R.id.ressListView, svcIntent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.ressListView);

        // Mise à jour de la view
        MultiUtils multiUtils = new MultiUtils(context, appWidgetManager, remoteViews, appWidgetId);
        multiUtils.updatewidget();

    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        // Récuperation de l'identifiant du Widget
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        // Action mise à jour du widget
        if (action.contentEquals(UPDATE_ALL_MULTI_WIDGET)) {
            // Log.d(TAG, action);
            new Thread(new Runnable() {
                public void run() {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MultiWidgetProvider.class.getName());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }
            }).start();
        }

        if (extras != null) {
            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            if (appWidgetId != 0) {
                // Log.d(TAG, "Widget = " + appWidgetId );

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.multi_widget);
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                MultiUtils multiUtils = new MultiUtils(context, appWidgetManager, views, appWidgetId);
                int idRow = extras.getInt(DomoConstants.POSITION_VIEW);

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

                if (action.contentEquals("android.appwidget.action.MULTI_WIDGET_UPDATE")) {
                    MultiWidget widget = multiUtils.getWidget();
                    multiUtils.updateWidgetSate();
                    if (!widget.getDomoState().isEmpty()) {
                        // Affichage du retour
                        multiUtils.updateWidgetSate();
                    } else {
                        // Affichage de l'action du Widget
                        // Log.d(TAG, "rowSelected = " + rowSelected);
                        multiUtils.changeWidgetState(widget.getMutliWidgetRess().get(idRow), idRow, false);
                    }
                }

                // Button du widget
                if (action.contentEquals("android.appwidget.action.RESS_BUTTON_PUSH")) {
                    try {
                        MultiWidget widget = multiUtils.getWidget();
                        MultiWidgetRess multiWidgetRess = widget.getMutliWidgetRess().get(idRow);
                        // Action - Push sur l'action
                        if (rowSelected == idRow) {
                            multiUtils.changeWidgetState(multiWidgetRess, idRow, true);
                            rowSelected = -1;
                        } else {
                            rowSelected = idRow;
                            multiUtils.changeWidgetState(multiWidgetRess, idRow, false);
                        }
                        // Log.d(TAG, "IdRaw = " + idRow + " - Action = " + multiWidgetRess.getDomoName());
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur : " + e);
                    }
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
            try {
                // Vérification de la configuration du widget
                MultiWidget widget = (MultiWidget) DomoUtils.getObjetById(context, new MultiWidget(context, appWidgetId));
                if (widget != null) {
                    // Log.d(TAG, "onUpdate() => Widget Multi : " + appWidgetId);
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            } catch (SQLiteDatabaseLockedException e) {
                Log.e(TAG, "Erreur : La base n'est pas disponilbe !");
            }
        }
    }

}

