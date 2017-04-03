package illimiteremi.domowidget.DomoWidgetPush;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NO_VALUE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PUSH_TIME;

/**
 * Created by rcouturi on 28/06/2016.
 */
public class PushUtils {

    private static final String     TAG      = "[DOMO_PUSH_UTILS]";
    private final Context           context;

    private PushWidget              widget;                 // Objet Widget
    private final RemoteViews       views;                  // Views du Widget
    private BoxSetting              selectedBox;            // Box selectionnée
    private Bitmap                  ressourceIdOn;          // Identifiant de la ressource image On
    private Bitmap                  ressourceIdOff;         // Identifiant de la ressource image Off
    private AppWidgetManager        appWidgetManager;       // Widget Manager

    public PushUtils(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId) {

        this.context          = context;
        this.appWidgetManager = appWidgetManager;
        this.views            = views;

        // Lecture du Widget en base
        widget = (PushWidget) DomoUtils.getObjetById(context, new PushWidget(context, appWidgetId));

        if (widget != null) {
            selectedBox    = widget.getSelectedBox();
            ressourceIdOn  = DomoUtils.getBitmapRessource(context, widget, true);
            ressourceIdOff = DomoUtils.getBitmapRessource(context, widget, false);
        }
        updateWidgetView();
    }

    /**
     * Mise à jour de la wiew du widget
     */
    private void updateWidgetView() {

        views.setTextViewText(R.id.widgetName, widget.getDomoName());
        views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);
        views.setImageViewBitmap(R.id.widgetButton, ressourceIdOff);

        if (widget.getDomoLock() == 0) {
            views.setViewVisibility(R.id.unlockButton, View.INVISIBLE);
        } else {
            views.setViewVisibility(R.id.unlockButton, View.VISIBLE);
        }
        // Mise à jour du widget
        appWidgetManager.updateAppWidget(widget.getDomoId(), views);
    }

    /**
     * Changement d'état du widget
     */
    public void changeWidgetState() {
        new Thread(new Runnable() {
            public void run() {
                // Récuperation de l'information à la box
                if (selectedBox != null) {
                    views.setImageViewBitmap(R.id.widgetButton, ressourceIdOn);
                    // Mise à jour du widget
                    appWidgetManager.updateAppWidget(widget.getDomoId(), views);
                    try {
                        Thread.sleep(PUSH_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Envoi de requete à la box Domotic
                    String action = "&" + widget.getDomoAction();
                    DomoHttp.httpRequest(context, selectedBox, action, null, widget);
                    views.setImageViewBitmap(R.id.widgetButton, ressourceIdOff);

                    // Mise à jour du widget
                    appWidgetManager.updateAppWidget(widget.getDomoId(), views);
                }
            }
        }).start();
    }
}
