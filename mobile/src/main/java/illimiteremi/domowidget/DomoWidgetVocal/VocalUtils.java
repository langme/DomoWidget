package illimiteremi.domowidget.DomoWidgetVocal;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.util.Objects;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetState.StateWidget;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NO_VALUE;

/**
 * Created by rcouturi on 28/06/2016.
 */
public class VocalUtils {

    static final String             TAG      = "[DOMO_VOCAL_UTILS]";
    private final Context           context;

    private       VocalWidget       widget;                 // Objet Widget
    private final RemoteViews       views;                  // Views du Widget
    private       BoxSetting        selectedBox;            // Box selectionnée
    private       AppWidgetManager  appWidgetManager;       // Widget Manager

    public VocalUtils(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId) {

        this.context          = context;
        this.appWidgetManager = appWidgetManager;
        this.views            = views;

        // Lecture du Widget en base
        // Log.d(TAG, "Récuperation information Widget : " + appWidgetId);
        widget = (VocalWidget) DomoUtils.getObjetById(context, new VocalWidget(context,appWidgetId));

        if (widget != null) {
            selectedBox = widget.getSelectedBox();
        } else {
            widget = new VocalWidget(context, appWidgetId);
        }
        updateWidgetView();
    }

    /**
     * Mise à jour de la wiew du widget
     */
    private void updateWidgetView() {
        // Log.d(TAG, "Mise à jour du widget : " + widget.getDomoId());

        views.setTextViewText(R.id.widgetName, widget.getDomoName());
        views.setTextViewTextSize(R.id.widgetName, TypedValue.COMPLEX_UNIT_PX, 10 * context.getResources().getDisplayMetrics().density);
        views.setImageViewResource(R.id.widgetButton, R.drawable.widget_vocal_preview);

        // Mise à jour du widget
        appWidgetManager.updateAppWidget(widget.getDomoId(), views);
    }
}
