package illimiteremi.domowidget.DomoWidgetMulti;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Objects;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoConstants;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.DONE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NO_VALUE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PUSH_TIME;

/**
 * Created by rcouturi on 28/06/2016.
 */
public class MultiUtils {

    private static final String             TAG      = "[DOMO_MULTI_UTILS]";

    private final        Context           context;

    private final        MultiWidget       widget;                 // Objet Widget
    private final        RemoteViews       views;                  // Views du Widget
    private              BoxSetting        selectedBox;            // Box selectionnée
    private final        AppWidgetManager  appWidgetManager;       // Widget Manager

    public MultiUtils(Context context, AppWidgetManager  appWidgetManager,  RemoteViews views, Integer appWidgetId) {

        this.context          = context;
        this.appWidgetManager = appWidgetManager;
        this.views            = views;

        widget = (MultiWidget) DomoUtils.getObjetById(context, new MultiWidget(context, appWidgetId));

        if (widget != null) {
            selectedBox = widget.getSelectedBox();
            if (widget.getMutliWidgetRess() == null) {
                // Ajout d'une ressource vide (si vide)
                MultiWidgetRess emptyRess = new MultiWidgetRess(appWidgetId);
                emptyRess.setDomoName(context.getResources().getString(R.string.widget_action_new));
                DomoUtils.insertObjet(context, emptyRess);
                ArrayList<MultiWidgetRess> emptyRessList = new ArrayList<>();
                emptyRessList.add(emptyRess);
                widget.setMutliWidgetRess(emptyRessList);
            }
        }
    }

    /**
     * Mise à jour de la view
     */
    public void updatewidget() {
        if (widget != null) {
            if (!widget.getDomoState().isEmpty()) {
                updateWidgetSate();
            } else {
                updateWidgetView();
            }
        }
    }

    /**
     * Mise à jour de la wiew du widget
     */
    private void updateWidgetView() {
        // Nom de l'action
        MultiWidgetRess multiWidgetRess = widget.getMutliWidgetRess().get(0);
        Bitmap widgetBitmap = DomoUtils.setColorText(context, multiWidgetRess.getDomoName(), DomoConstants.DEFAULT_COLOR);
        views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);
        views.setScrollPosition(R.id.ressListView, 0);

        // Nom du widget
        views.setTextViewText(R.id.widgetName, widget.getDomoName());
        views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);

        // Mise à jour du widget
        appWidgetManager.updateAppWidget(widget.getDomoId(), views);
    }

    /**
     * updateWidgetSate
     */
    public void updateWidgetSate() {
        // Log.d(TAG, "Mise à jour du widget...");
        try {
            // Nom du widget
            views.setTextViewText(R.id.widgetName, widget.getDomoName());
            views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);
            appWidgetManager.updateAppWidget(widget.getDomoId(), views);

            String widgetState = NO_VALUE;
            if (selectedBox != null) {
                // Time Out avant retour
                Thread.sleep(widget.getDomoTimeOut() * 1000);
                // URL ETAT
                String action = "&" + widget.getDomoState();
                String result = DomoHttp.httpRequest(context, selectedBox, action, null, widget);
                widgetState = !Objects.equals(result, ERROR) || Objects.equals(result, DONE) ? result : NO_VALUE;
            }

            // Retour d'etat
            Bitmap widgetBitmap = DomoUtils.setColorText(context, widgetState, DomoConstants.DEFAULT_COLOR);
            views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);
            appWidgetManager.updateAppWidget(widget.getDomoId(), views);

        } catch (Exception e) {
            Log.e(TAG, "Erreur : " + e);
        }
    }

    /**
     * Changement d'état du widget
     */
    public void changeWidgetState(final MultiWidgetRess ress, final int idRow, final boolean isSelected) {

        new Thread(new Runnable() {
            public void run() {

                // Mise à jour du Nom de l'action
                views.setScrollPosition(R.id.ressListView, idRow);
                Bitmap widgetBitmap = DomoUtils.setColorText(context, ress.getDomoName(), DomoConstants.DEFAULT_COLOR);
                views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);
                appWidgetManager.updateAppWidget(widget.getDomoId(), views);

                // Action si l'action est selectionné
                if (isSelected) {
                // Chargement en cours...
                widgetBitmap = DomoUtils.setColorText(context, "En cours...", DomoConstants.DEFAULT_COLOR);
                views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);

                Bitmap on  = DomoUtils.getBitmapRessource(context, ress, true);
                Bitmap off = DomoUtils.getBitmapRessource(context, ress, false);

                views.setImageViewBitmap(R.id.imageView, on);
                appWidgetManager.updateAppWidget(widget.getDomoId(), views);

                // URL ACTION
                String action = "&" + ress.getDomoAction();
                DomoHttp.httpRequest(context, selectedBox, action, null, widget);

                // Attente pour Annimation
                try {
                    Thread.sleep(PUSH_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                views.setImageViewBitmap(R.id.imageView, off);
                appWidgetManager.updateAppWidget(widget.getDomoId(), views);

                // Mise à jour des widget MULTI
                Intent updateIntent = new Intent(context, MultiWidgetProvider.class);
                updateIntent.setAction("android.appwidget.action.MULTI_WIDGET_UPDATE");
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.getDomoId());
                updateIntent.putExtra(DomoConstants.POSITION_VIEW, idRow);
                context.sendBroadcast(updateIntent);
            }

            }
        }).start();
    }

    public MultiWidget getWidget(){
        return widget;
    }


}
