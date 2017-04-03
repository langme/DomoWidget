package illimiteremi.domowidget.DomoWidgetState;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Objects;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.DONE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NO_VALUE;

/**
 * Created by rcouturi on 28/06/2016.
 */
public class StateUtils {

    static final String             TAG      = "[DOMO_STATE_UTILS]";
    private final Context           context;

    private       StateWidget       widget;                 // Objet Widget
    private final RemoteViews       views;                  // Views du Widget
    private       BoxSetting        selectedBox;            // Box selectionnée
    private       AppWidgetManager  appWidgetManager;       // Widget Manager

    public StateUtils(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId) {

        this.context          = context;
        this.appWidgetManager = appWidgetManager;
        this.views            = views;

        // Lecture du Widget en base
        widget = (StateWidget) DomoUtils.getObjetById(context, new StateWidget(context,appWidgetId));
        if (widget != null) {
            selectedBox = widget.getSelectedBox();
        } else {
            widget = new StateWidget(context, appWidgetId);
        }
        updateWidgetView();
    }

    /**
     * Mise à jour de la wiew du widget
     */
    private void updateWidgetView() {

        views.setTextViewText(R.id.widgetName, widget.getDomoName());

        // Récuperation de l'information à la box
        if (selectedBox != null) {
            views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);
            // Création de la requette
            String action = "&" + widget.getDomoState();
            String result = DomoHttp.httpRequest(context, selectedBox, action, null, widget);
            String widgetState = !Objects.equals(result, ERROR) || Objects.equals(result, DONE) ? result + widget.getDomoUnit() : NO_VALUE;
            Bitmap widgetBitmap = DomoUtils.setColorText(context, widgetState, widget.getDomoColor());
            views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);
            Log.d(TAG, "Etat du widget : " + widget.getDomoName() + " = " + widgetState);
        } else {
            Bitmap widgetBitmap = DomoUtils.setColorText(context, NO_VALUE, widget.getDomoColor());
            views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);
        }

        // Mise à jour du widget
        try {
            appWidgetManager.updateAppWidget(widget.getDomoId(), views);
        } catch (Exception e) {
            // Cas particulier si le widget est trop grand (maximum bitmap memory usage)
            Bitmap widgetBitmap = DomoUtils.setColorText(context, context.getResources().getString(R.string.widget_to_long), widget.getDomoColor());
            views.setImageViewBitmap(R.id.widgetButton, widgetBitmap);
            appWidgetManager.updateAppWidget(widget.getDomoId(), views);
            Log.e(TAG, "Erreur : " + e);
        }

    }
}
