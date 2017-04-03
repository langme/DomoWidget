package illimiteremi.domowidget.DomoWidgetToogle;


import android.appwidget.AppWidgetManager;

import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Objects;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoConstants;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.OK;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_TOOGLE_WIDGET;


/**
 * Created by rcouturi on 28/06/2016.
 */
public class ToogleUtils {

    private static final String     TAG       = "[DOMO_TOOGLE_UTILS]";
    private final Context           context;

    private ToogleWidget            widget;                 // Objet Widget
    private final RemoteViews       views;                  // Views du Widget
    private BoxSetting              selectedBox;            // Box selectionnée
    private Bitmap                  ressourceIdOn;          // Identifiant de la ressource image On
    private Bitmap                  ressourceIdOff;         // Identifiant de la ressource image Off
    private AppWidgetManager        appWidgetManager;       // Widget Manager


    public ToogleUtils(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId) {

        this.context          = context;
        this.appWidgetManager = appWidgetManager;
        this.views            = views;

        // Lecture du Widget en base
        widget = (ToogleWidget) DomoUtils.getObjetById(context, new ToogleWidget(context, appWidgetId));

        if (widget != null) {
            selectedBox    = widget.getSelectedBox();
            ressourceIdOn  = DomoUtils.getBitmapRessource(context, widget, true);
            ressourceIdOff = DomoUtils.getBitmapRessource(context, widget, false);
        } else {
            widget = new ToogleWidget(context, appWidgetId);
        }
        updateWidgetView();
    }

    /**
     * Mise à jour de la wiew du widget
     */
    private void updateWidgetView() {

        views.setTextViewText(R.id.widgetName, widget.getDomoName());
        views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);

        // Récuperation de l'information à la box
        if (selectedBox != null) {
            // Si verrouillage du widget
            if (widget.getDomoLock() == 0) {
                views.setViewVisibility(R.id.unlockButton, View.INVISIBLE);
            } else {
                views.setViewVisibility(R.id.unlockButton, View.VISIBLE);
            }

            // Check retour d'etat
            if (checkWidget()) {
                views.setImageViewBitmap(R.id.widgetButton, ressourceIdOn);
            } else {
                views.setImageViewBitmap(R.id.widgetButton, ressourceIdOff);
            }
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
                if (!widget.getDomoOff().isEmpty() && !widget.getDomoOn().isEmpty()) {
                    String url;
                    AppWidgetManager awm  = AppWidgetManager.getInstance(context);
                    if (checkWidget()) {
                        url = "&" + widget.getDomoOff();
                        views.setImageViewBitmap(R.id.widgetButton, ressourceIdOff);
                    } else {
                        url = "&" + widget.getDomoOn();
                        views.setImageViewBitmap(R.id.widgetButton, ressourceIdOn);
                    }
                    // Mise à jour du widget
                    awm.updateAppWidget(widget.getDomoId(), views);
                    // Envoi de requete à la box Domotic
                    DomoHttp.httpRequest(context, selectedBox, url, widget.getDomoExpReg(), widget);

                    // Mise à jour du verouillage
                    if (widget.getDomoLock() == 1) {
                        views.setViewVisibility(R.id.unlockButton, View.VISIBLE);
                        try {
                            Thread.sleep(DomoConstants.LOCK_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        awm.updateAppWidget(widget.getDomoId(), views);
                    }
                    // Attente avant retour d'etat
                    try {
                        int timeOut = (widget.getDomoTimeOut() == null ? 2 : widget.getDomoTimeOut());
                        Thread.sleep(timeOut * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Mise à jour du widget
                    updateWidgetView();
                }
            }
        }).start();
    }

    /**
     * Vérification de l'état du widget
     * @return
     */
    private Boolean checkWidget() {
        Boolean result = false;
        try {
            if (!widget.getDomoOff().isEmpty() && !widget.getDomoOn().isEmpty()) {
                String url = "&" + widget.getDomoState();
                String httpResult = DomoHttp.httpRequest(context, selectedBox, url, widget.getDomoExpReg(), widget);
                if (widget.getDomoExpReg().isEmpty()){
                    result = !httpResult.contentEquals("0");
                } else {
                    result = Objects.equals(httpResult, OK);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur : " + e);
            return result;
        }
        Log.d(TAG, "Etat du widget : " + widget.getDomoName() + " = " + result);
        return result;
    }
}
