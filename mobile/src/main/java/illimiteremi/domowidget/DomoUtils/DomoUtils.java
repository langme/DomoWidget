package illimiteremi.domowidget.DomoUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.util.ArrayList;

import illimiteremi.domowidget.DomoAdapter.BoxAdapter;
import illimiteremi.domowidget.DomoAdapter.WidgetAdapter;
import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoGeneralSetting.IconSetting;
import illimiteremi.domowidget.DomoService;
import illimiteremi.domowidget.DomoWear.WearSetting;
import illimiteremi.domowidget.DomoWidgetBdd.DomoBaseSQLite;
import illimiteremi.domowidget.DomoWidgetBdd.DomoBoxBDD;
import illimiteremi.domowidget.DomoWidgetBdd.DomoWearBDD;
import illimiteremi.domowidget.DomoWidgetBdd.IconWidgetBDD;
import illimiteremi.domowidget.DomoWidgetBdd.LocationWidgetBDD;
import illimiteremi.domowidget.DomoWidgetBdd.MultiWidgetBDD;
import illimiteremi.domowidget.DomoWidgetBdd.PushWidgetBDD;
import illimiteremi.domowidget.DomoWidgetBdd.StateWidgetBDD;
import illimiteremi.domowidget.DomoWidgetBdd.ToogleWidgetBDD;
import illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget;
import illimiteremi.domowidget.DomoWidgetBdd.VocalWidgetBDD;
import illimiteremi.domowidget.DomoWidgetLocation.LocationWidget;
import illimiteremi.domowidget.DomoWidgetLocation.WidgetLocationProvider;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidget;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidgetProvider;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidgetRess;
import illimiteremi.domowidget.DomoWidgetPush.PushWidget;
import illimiteremi.domowidget.DomoWidgetPush.WidgetPushProvider;
import illimiteremi.domowidget.DomoWidgetState.StateWidget;
import illimiteremi.domowidget.DomoWidgetState.WidgetStateProvider;
import illimiteremi.domowidget.DomoWidgetToogle.ToogleWidget;
import illimiteremi.domowidget.DomoWidgetToogle.WidgetToogleProvider;
import illimiteremi.domowidget.DomoWidgetVocal.VocalWidget;
import illimiteremi.domowidget.DomoWidgetVocal.WidgetVocalProvider;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.BOX;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.ICON;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.INTENT_NO_DATA;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.MULTI;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.MULTI_RESS;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NEW_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NO_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PUSH;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.STATE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.TOOGLE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_LOCATION_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_MULTI_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_PUSH_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_STATE_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_TOOGLE_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.UPDATE_ALL_VOCAL_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.VOCAL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.WEAR;

/**
 * $Description
 *
 * @author XZAQ496 (non modifiable)
 *         Date de création : 13/09/2016 (non modifiable)
 *         <p/>
 *         <b><u>Dernière modification : $Description_modification</u></b>
 *         <li>$Date     : 13/09/2016$</li>
 *         <li>$Author   : XZAQ496$</li>
 *         <li>$Revision :        $</li>
 *         <li>$HeadURL  :        $</li>
 */
public class DomoUtils {

    private static final String TAG      = "[DOMO_UTILS]";

    /**
     * hexStringToARGB
     * @param hexARGB
     * @return
     * @throws IllegalArgumentException
     */
    public static int[] hexStringToRGB(String hexARGB) throws IllegalArgumentException {

        int[] intARGB = new int[3];

        if (hexARGB.length() == 7) {
            intARGB[0] = Integer.valueOf(hexARGB.substring(1, 3), 16);  // Red
            intARGB[1] = Integer.valueOf(hexARGB.substring(3, 5), 16);  // Green
            intARGB[2] = Integer.valueOf(hexARGB.substring(5, 7), 16);  // Blue
        }
        return intARGB;
    }

    /**
     * restartService
     * @param context
     */
    public static void startService(Context context, boolean restart){
        // Démarrage du service
        if (!isServiceRunning(context,DomoService.class) || restart) {
            Intent msgIntent = new Intent(context, DomoService.class);
            context.stopService(msgIntent);
            context.startService(msgIntent);
        }
    }

    /**
     * Verification si le service est demarré
     * @param context
     * @param serviceClass
     * @return
     */
    private static boolean isServiceRunning(Context context, Class<?> serviceClass){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        // Loop through the running services
        for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                // Log.d(TAG, "Service is running...");
                return true;
            }
        }
        // Log.d(TAG, "Service is not started !");
        return false;
    }

    /**
     * Récuperation des ressources dans le BDD
     * @return
     */
    public static ArrayList<IconSetting> getAllImageRessource(Context context){
        // Récupère dans un Cursor
        ArrayList<IconSetting> listRessource = new ArrayList<IconSetting>();

        DomoBaseSQLite domoBaseSQLite = new DomoBaseSQLite(context, UtilsDomoWidget.NOM_BDD, null, UtilsDomoWidget.VERSION_BDD);
        SQLiteDatabase  bdd = domoBaseSQLite.getWritableDatabase();

        Cursor c = bdd.query(UtilsDomoWidget.TABLE_RESS_WIDGET, new String[] {
                UtilsDomoWidget.COL_ID,
                UtilsDomoWidget.COL_RESS_NAME,
                UtilsDomoWidget.COL_RESS_PATH},null , null, null, null, null);

        // Log.d(TAG, "Récuperation de la totalité des ressources");
        // Si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            // Log.d(TAG, "Pas de ressource !");
            return null;
        }

        while (c.moveToNext()) {
            IconSetting domoRessource = new IconSetting();
            // On lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            domoRessource.setId(c.getInt(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_ID)));
            domoRessource.setRessourceName(c.getString(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_RESS_NAME)));
            domoRessource.setRessourcePath(c.getString(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_RESS_PATH)));

            // Verification de la présence du fichier
            if (domoRessource.getRessourcePath() != null) {
                File file = new File(domoRessource.getRessourcePath());
                if(file.exists()) {
                    listRessource.add(domoRessource);
                } else  {
                    // On efface de la base
                    bdd.delete(UtilsDomoWidget.TABLE_RESS_WIDGET, UtilsDomoWidget.COL_ID + " = " + domoRessource.getId(), null);
                }
            } else {
                listRessource.add(domoRessource);
            }
        }
        // On ferme le cursor
        c.close();
        return listRessource;
    }

    /**
     * Création du message en Bitmap avec la Font Digital
     * @param context
     * @param value
     * @return
     */
    public static Bitmap setColorText(Context context, String value, String colorRGB) {

        // Calcul de la taille du widget
        String hexColor;
        try {
            hexColor = String.format("#%06X", (0xFFFFFF & Integer.parseInt(colorRGB)));
        } catch (Exception e) {
            Log.e(TAG, "Erreur : " + e);
            hexColor = "0xFFFFFF";
        }

        // Log.d(TAG, "Couleur RGB: " + colorRGB + " => " + color[0] + "," + color[1] + "," + color[2]);
        try {
            int bitmapWeight = 50 * value.length();
            final int[] color = DomoUtils.hexStringToRGB(hexColor);
            Bitmap myBitmap = Bitmap.createBitmap(bitmapWeight, 100, Bitmap.Config.ARGB_4444);
            Canvas myCanvas = new Canvas(myBitmap);
            myCanvas.drawARGB(100, color[0], color[1], color[2]);
            Paint paint = new Paint();
            Typeface textFont = Typeface.createFromAsset(context.getAssets(), "fonts/ds_digi.ttf");
            paint.setAntiAlias(true);
            paint.setSubpixelText(true);
            paint.setTypeface(textFont);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(96);
            paint.setTextAlign(Paint.Align.CENTER);
            myCanvas.drawText(value, bitmapWeight / 2, 82, paint);
            return myBitmap;
        } catch (Exception e) {
            Log.e(TAG, "Erreur : " + e);
            return null;
        }
    }

    /**
     * Recherche si une Box est utilsé par un widget
     * @param context
     * @param boxSetting
     * @return
     */
    public static boolean boxIsUsed(Context context, BoxSetting boxSetting){
        try {
            DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
            domoBoxBDD.open();
            Boolean result = domoBoxBDD.isUse(boxSetting.getBoxId());
            domoBoxBDD.close();
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Erreur : " + e);
            return true;
        }
    }

    /**
     * Verification du widget HOME / BDD
     * @param context
     * @param idWidget
     * @return
     */
    public static boolean widgetIsPresent(Context context, Integer idWidget) {
        if (idWidget.equals(NEW_WIDGET)|| idWidget.equals(NO_WIDGET)) {
            Log.e(TAG, "Identifiant Widget non présent !");
            return true;
        }

        // Verification si le widget est présent
        try {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            AppWidgetProviderInfo widgetInfo = awm.getAppWidgetInfo(idWidget);
            int nbProfile = widgetInfo.widgetCategory;
        } catch (Exception e) {
            Log.e(TAG, "Le Widget N°" + idWidget + " est non présent ! La suppression dans la bdd est possible.");
            return false;
        }
        return true;
    }

    /**
     * Création d'un ArrayAdapter pour la liste des box
     * @return
     */
    public static ArrayList<Object> getAllObjet(Context context, String object) {

        ArrayList<Object> objects = new ArrayList<>();

        try {
            switch(object) {
                case BOX:
                    DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                    domoBoxBDD.open();
                    ArrayList<BoxSetting> boxSettings = domoBoxBDD.getAllBox();
                    domoBoxBDD.close();
                    if (boxSettings != null) {
                        for (BoxSetting boxSetting : boxSettings){
                            objects.add(boxSetting);
                        }
                    }
                    break;
                case TOOGLE:
                    ToogleWidgetBDD toogleWidgetBDD = new ToogleWidgetBDD(context);
                    toogleWidgetBDD.open();
                    ArrayList<ToogleWidget> toogleWidgets = toogleWidgetBDD.getAllWidgets();
                    toogleWidgetBDD.close();
                    if (toogleWidgets != null) {
                        for (ToogleWidget widget : toogleWidgets){
                            objects.add(widget);
                        }
                    }
                    break;
                case STATE:
                    StateWidgetBDD stateWidgetBDD = new StateWidgetBDD(context);
                    stateWidgetBDD.open();
                    ArrayList<StateWidget> stateWidgets = stateWidgetBDD.getAllWidgets();
                    stateWidgetBDD.close();
                    if (stateWidgets != null) {
                        for (StateWidget widget : stateWidgets){
                            objects.add(widget);
                        }
                    }
                    break;
                case PUSH:
                    PushWidgetBDD pushWidgetBDD = new PushWidgetBDD(context);
                    pushWidgetBDD.open();
                    ArrayList<PushWidget> pushWidgets = pushWidgetBDD.getAllWidgets();
                    pushWidgetBDD.close();
                    if (pushWidgets != null) {
                        for (PushWidget widget : pushWidgets){
                            objects.add(widget);
                        }
                    }
                    break;
                case LOCATION:
                    LocationWidgetBDD locationWidgetBDD = new LocationWidgetBDD(context);
                    locationWidgetBDD.open();
                    ArrayList<LocationWidget> locationWidgets = locationWidgetBDD.getAllWidgets();
                    locationWidgetBDD.close();
                    if (locationWidgets != null) {
                        for (LocationWidget widget : locationWidgets){
                            objects.add(widget);
                        }
                    }
                    break;
                case MULTI:
                    MultiWidgetBDD multiWidgetBDD = new MultiWidgetBDD(context);
                    multiWidgetBDD.open();
                    ArrayList<MultiWidget> multiWidgets = multiWidgetBDD.getAllWidgets();
                    if (multiWidgets != null) {
                        for (MultiWidget widget : multiWidgets){
                            widget.setMutliWidgetRess(multiWidgetBDD.getAllRessource(widget));
                            objects.add(widget);
                        }
                    }
                    multiWidgetBDD.close();
                    break;
                case VOCAL:
                    VocalWidgetBDD vocalWidgetBDD = new VocalWidgetBDD(context);
                    vocalWidgetBDD.open();
                    ArrayList<VocalWidget> vocalWidgets = vocalWidgetBDD.getAllWidgets();
                    vocalWidgetBDD.close();
                    if (vocalWidgets != null) {
                        for (VocalWidget widget : vocalWidgets){
                            objects.add(widget);
                        }
                    }
                    break;
                case WEAR:
                    DomoWearBDD domoWearBDD = new DomoWearBDD(context);
                    domoWearBDD.open();
                    ArrayList<WearSetting> domoWears = domoWearBDD.getWearSeeting();
                    domoWearBDD.close();
                    if (domoWears != null) {
                        for (WearSetting wear : domoWears){
                            objects.add(wear);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur - getAllObjet : " + e);
        }
        return objects;
    }

    /**
     * Récuperation d'un object en bdd (box / widget)
     * @param context
     * @param object
     * @return
     */
    public static Object getObjetById(Context context, Object object) {

        switch(object.getClass().getSimpleName()) {
            case BOX:
                BoxSetting boxSetting = (BoxSetting) object;
                DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                domoBoxBDD.open();
                boxSetting = domoBoxBDD.getBoxById(boxSetting.getBoxId());
                domoBoxBDD.close();
                return boxSetting;
            case TOOGLE:
                ToogleWidget toogleWidget = (ToogleWidget) object;
                ToogleWidgetBDD toogleWidgetBDD = new ToogleWidgetBDD(context);
                toogleWidgetBDD.open();
                toogleWidget = toogleWidgetBDD.getWidgetById(toogleWidget.getDomoId());
                toogleWidgetBDD.close();
                return toogleWidget;
            case STATE:
                StateWidget stateWidget = (StateWidget) object;
                StateWidgetBDD stateWidgetBDD = new StateWidgetBDD(context);
                stateWidgetBDD.open();
                stateWidget = stateWidgetBDD.getWidgetById(stateWidget.getDomoId());
                stateWidgetBDD.close();
                return stateWidget;
            case PUSH:
                PushWidget pushWidget = (PushWidget) object;
                PushWidgetBDD pushWidgetBDD = new PushWidgetBDD(context);
                pushWidgetBDD.open();
                pushWidget = pushWidgetBDD.getWidgetById(pushWidget.getDomoId());
                pushWidgetBDD.close();
                return pushWidget;
            case LOCATION:
                LocationWidget locationWidget = (LocationWidget) object;
                LocationWidgetBDD locationWidgetBDD = new LocationWidgetBDD(context);
                locationWidgetBDD.open();
                locationWidget = locationWidgetBDD.getWidgetById(locationWidget.getDomoId());
                locationWidgetBDD.close();
                return locationWidget;
            case MULTI:
                MultiWidget multiWidget = (MultiWidget) object;
                MultiWidgetBDD multiWidgetBDD = new MultiWidgetBDD(context);
                multiWidgetBDD.open();
                multiWidget = multiWidgetBDD.getWidgetById(multiWidget.getDomoId());
                multiWidgetBDD.close();
                return multiWidget;
            case MULTI_RESS:
                MultiWidgetRess multiWidgetRess = (MultiWidgetRess) object;
                MultiWidgetBDD multiWidgetRessBDD = new MultiWidgetBDD(context);
                multiWidgetRessBDD.open();
                multiWidgetRess = multiWidgetRessBDD.getAllRessourceById(multiWidgetRess.getId());
                multiWidgetRessBDD.close();
                return multiWidgetRess;
            case VOCAL:
                VocalWidget vocalWidget = (VocalWidget) object;
                VocalWidgetBDD vocalWidgetBDD = new VocalWidgetBDD(context);
                vocalWidgetBDD.open();
                vocalWidget = vocalWidgetBDD.getWidgetById(vocalWidget.getDomoId());
                vocalWidgetBDD.close();
                return vocalWidget;
            case WEAR:
                return null;
        }
        return null;
    }

    /**
     * Mise à jour d'un object dans la BDD
     * @param context
     * @param object
     * @return
     */
    public static int updateObjet(Context context, Object object) {

        int result = -1;

        switch(object.getClass().getSimpleName()) {
            case BOX:
                BoxSetting boxSetting = (BoxSetting) object;
                DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                domoBoxBDD.open();
                result = domoBoxBDD.updateBox(boxSetting);
                domoBoxBDD.close();
                return result;
            case TOOGLE:
                ToogleWidget toogleWidget = (ToogleWidget) object;
                ToogleWidgetBDD toogleWidgetBDD = new ToogleWidgetBDD(context);
                toogleWidgetBDD.open();
                result = toogleWidgetBDD.updateWidget(toogleWidget);
                toogleWidgetBDD.close();
                return result;
            case STATE:
                StateWidget stateWidget = (StateWidget) object;
                StateWidgetBDD stateWidgetBDD = new StateWidgetBDD(context);
                stateWidgetBDD.open();
                result = stateWidgetBDD.updateWidget(stateWidget);
                stateWidgetBDD.close();
                return result;
            case PUSH:
                PushWidget pushWidget = (PushWidget) object;
                PushWidgetBDD pushWidgetBDD = new PushWidgetBDD(context);
                pushWidgetBDD.open();
                result = pushWidgetBDD.updateWidget(pushWidget);
                pushWidgetBDD.close();
                return result;
            case LOCATION:
                LocationWidget locationWidget = (LocationWidget) object;
                LocationWidgetBDD locationWidgetBDD = new LocationWidgetBDD(context);
                locationWidgetBDD.open();
                result = locationWidgetBDD.updateWidget(locationWidget);
                locationWidgetBDD.close();
                return result;
            case MULTI:
                MultiWidget multiWidget = (MultiWidget) object;
                MultiWidgetBDD multiWidgetBDD = new MultiWidgetBDD(context);
                multiWidgetBDD.open();
                result = multiWidgetBDD.updateWidget(multiWidget);
                multiWidgetBDD.close();
                return result;
            case MULTI_RESS:
                MultiWidgetRess multiWidgetRess = (MultiWidgetRess) object;
                MultiWidgetBDD multiWidgetRessBDD = new MultiWidgetBDD(context);
                multiWidgetRessBDD.open();
                result = multiWidgetRessBDD.updateRessource(multiWidgetRess);
                multiWidgetRessBDD.close();
                return result;
            case VOCAL:
                VocalWidget vocalWidget = (VocalWidget) object;
                VocalWidgetBDD vocalWidgetBDD = new VocalWidgetBDD(context);
                vocalWidgetBDD.open();
                result = vocalWidgetBDD.updateWidget(vocalWidget);
                vocalWidgetBDD.close();
                return result;
            case WEAR:
                WearSetting wear = (WearSetting) object;
                DomoWearBDD domoWearBDD = new DomoWearBDD(context);
                domoWearBDD.open();
                result = domoWearBDD.updateWear(wear);
                domoWearBDD.close();
                return result;
        }
        return result;
    }

    /**
     * Mise à jour d'un object dans la BDD
     * @param context
     * @param object
     * @return
     */
    public static long insertObjet(Context context, Object object) {

        long result = 0;

        switch(object.getClass().getSimpleName()) {
            case BOX:
                BoxSetting boxSetting = (BoxSetting) object;
                DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                domoBoxBDD.open();
                result = domoBoxBDD.insertBox(boxSetting);
                domoBoxBDD.close();
                return result;
            case TOOGLE:
                ToogleWidget toogleWidget = (ToogleWidget) object;
                ToogleWidgetBDD toogleWidgetBDD = new ToogleWidgetBDD(context);
                toogleWidgetBDD.open();
                result = toogleWidgetBDD.insertWidget(toogleWidget);
                toogleWidgetBDD.close();
                return result;
            case STATE:
                StateWidget stateWidget = (StateWidget) object;
                StateWidgetBDD stateWidgetBDD = new StateWidgetBDD(context);
                stateWidgetBDD.open();
                result = stateWidgetBDD.insertWidget(stateWidget);
                stateWidgetBDD.close();
                return result;
            case PUSH:
                PushWidget pushWidget = (PushWidget) object;
                PushWidgetBDD pushWidgetBDD = new PushWidgetBDD(context);
                pushWidgetBDD.open();
                result = pushWidgetBDD.insertWidget(pushWidget);
                pushWidgetBDD.close();
                return result;
            case LOCATION:
                LocationWidget locationWidget = (LocationWidget) object;
                LocationWidgetBDD locationWidgetBDD = new LocationWidgetBDD(context);
                locationWidgetBDD.open();
                result = locationWidgetBDD.insertWidget(locationWidget);
                locationWidgetBDD.close();
                return result;
            case MULTI:
                MultiWidget multiWidget = (MultiWidget) object;
                MultiWidgetBDD multiWidgetBDD = new MultiWidgetBDD(context);
                multiWidgetBDD.open();
                result = multiWidgetBDD.insertWidget(multiWidget);
                multiWidgetBDD.close();
                return result;
            case MULTI_RESS:
                MultiWidgetRess multiwidgetRess = (MultiWidgetRess) object;
                MultiWidgetBDD multiWidgetRessBDD = new MultiWidgetBDD(context);
                multiWidgetRessBDD.open();
                result = multiWidgetRessBDD.insertRessource(multiwidgetRess);
                multiWidgetRessBDD.close();
                return result;
            case VOCAL:
                VocalWidget vocalWidget = (VocalWidget) object;
                VocalWidgetBDD vocalWidgetBDD = new VocalWidgetBDD(context);
                vocalWidgetBDD.open();
                result = vocalWidgetBDD.insertWidget(vocalWidget);
                vocalWidgetBDD.close();
                return result;
            case WEAR:
                WearSetting wear = (WearSetting) object;
                DomoWearBDD wearBDD = new DomoWearBDD(context);
                wearBDD.open();
                result = wearBDD.insertWear(wear);
                wearBDD.close();
                return result;
        }
        return result;
    }

    /**
     * Suppression d'un object dans la BDD
     * @param context
     * @param object
     * @return
     */
    public static void removeObjet(Context context, Object object) {

        switch(object.getClass().getSimpleName()) {
            case BOX:
                BoxSetting boxSetting = (BoxSetting) object;
                DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                domoBoxBDD.open();
                domoBoxBDD.removeBox(boxSetting.getBoxId());
                domoBoxBDD.close();
                break;
            case TOOGLE:
                ToogleWidget toogleWidget = (ToogleWidget) object;
                ToogleWidgetBDD toogleWidgetBDD = new ToogleWidgetBDD(context);
                toogleWidgetBDD.open();
                toogleWidgetBDD.removeWidgetById(toogleWidget.getDomoId());
                toogleWidgetBDD.close();
                break;
            case STATE:
                StateWidget stateWidget = (StateWidget) object;
                StateWidgetBDD stateWidgetBDD = new StateWidgetBDD(context);
                stateWidgetBDD.open();
                stateWidgetBDD.removeWidgetById(stateWidget.getDomoId());
                stateWidgetBDD.close();
                break;
            case PUSH:
                PushWidget pushWidget = (PushWidget) object;
                PushWidgetBDD pushWidgetBDD = new PushWidgetBDD(context);
                pushWidgetBDD.open();
                pushWidgetBDD.removeWidgetById(pushWidget.getDomoId());
                pushWidgetBDD.close();
                break;
            case LOCATION:
                LocationWidget locationWidget = (LocationWidget) object;
                LocationWidgetBDD locationWidgetBDD = new LocationWidgetBDD(context);
                locationWidgetBDD.open();
                locationWidgetBDD.removeWidgetById(locationWidget.getDomoId());
                locationWidgetBDD.close();
                break;
            case MULTI:
                MultiWidget multiWidget = (MultiWidget) object;
                MultiWidgetBDD multiWidgetBDD = new MultiWidgetBDD(context);
                multiWidgetBDD.open();
                multiWidgetBDD.removeWidgetById(multiWidget.getDomoId());
                multiWidgetBDD.close();
                break;
            case MULTI_RESS:
                MultiWidgetRess multiWidgetRess = (MultiWidgetRess) object;
                MultiWidgetBDD multiWidgetRessBDD = new MultiWidgetBDD(context);
                multiWidgetRessBDD.open();
                multiWidgetRessBDD.removeRessource(multiWidgetRess.getId());
                multiWidgetRessBDD.close();
                break;
            case ICON:
                IconSetting iconSetting = (IconSetting) object;
                IconWidgetBDD iconWidgetBDD = new IconWidgetBDD(context);
                iconWidgetBDD.open();
                iconWidgetBDD.removeIcon(iconSetting.getId());
                iconWidgetBDD.close();
                break;
            case VOCAL:
                VocalWidget vocalWidget = (VocalWidget) object;
                VocalWidgetBDD vocalWidgetBDD = new VocalWidgetBDD(context);
                vocalWidgetBDD.open();
                vocalWidgetBDD.removeWidgetById(vocalWidget.getDomoId());
                vocalWidgetBDD.close();
                break;
            case WEAR:
                break;
        }
    }

    /**
     * Création d'un ArrayAdapter pour la liste des box ou widgets
     * @return
     */
    public static int getSpinnerPosition(Context context, Object object) {
        int position = 0;

        switch(object.getClass().getSimpleName()) {
            case BOX:
                BoxSetting selectedBox = (BoxSetting) object;
                for (Object boxObject : getAllObjet(context, BOX)) {
                    BoxSetting boxSetting = (BoxSetting) boxObject;
                    if (boxSetting.getBoxId().equals(selectedBox.getBoxId())) {
                        return position;
                    }
                    position++;
                }
                break;
            case TOOGLE:
                ToogleWidget selectedToogle = (ToogleWidget) object;
                for (Object toogleObject : getAllObjet(context, TOOGLE)) {
                    ToogleWidget toogleWidget = (ToogleWidget) toogleObject;
                    if (toogleWidget.getDomoId().equals(selectedToogle.getDomoId())) {
                        return position;
                    }
                    position++;
                }
                break;
            case STATE:
                StateWidget selectedState = (StateWidget) object;
                for (Object stateObject : getAllObjet(context, STATE)) {
                    StateWidget stateWidget = (StateWidget) stateObject;
                    if (stateWidget.getDomoId().equals(selectedState.getDomoId())) {
                        return position;
                    }
                    position++;
                }
                break;
            case PUSH:
                PushWidget selectedPush = (PushWidget) object;
                for (Object pushObject : getAllObjet(context, PUSH)) {
                    PushWidget pushWidget = (PushWidget) pushObject;
                    if (pushWidget.getDomoId().equals(selectedPush.getDomoId())) {
                        return position;
                    }
                    position++;
                }
                break;
            case LOCATION:
                LocationWidget selectedLocation = (LocationWidget) object;
                for (Object locationObject : getAllObjet(context, LOCATION)) {
                    LocationWidget locationWidget = (LocationWidget) locationObject;
                    if (locationWidget.getDomoId().equals(selectedLocation.getDomoId())) {
                        return position;
                    }
                    position++;
                }
                break;
            case MULTI:
                MultiWidget selectedMulti = (MultiWidget) object;
                for (Object multiObjet : getAllObjet(context, MULTI)){
                    MultiWidget multiWidget = (MultiWidget) multiObjet;
                    if (multiWidget.getDomoId().equals(selectedMulti.getDomoId())) {
                        return position;
                    }
                    position++;
                }
                break;
            case VOCAL:
                VocalWidget selectedVocal = (VocalWidget) object;
                for (Object multiObjet : getAllObjet(context, VOCAL)){
                    VocalWidget vocalWidget = (VocalWidget) multiObjet;
                    if (vocalWidget.getDomoId().equals(selectedVocal.getDomoId())) {
                        return position;
                    }
                    position++;
                }
                break;
        }
        return position;

    }

    /**
     * Creation d'un Adapter
     * @param context
     * @param objectType
     * @return
     */
    public static Object createAdapter(Context context, String objectType) {
        switch(objectType) {
            case BOX:
                ArrayList<Object> allBoxObjet = getAllObjet(context, BOX);
                BoxSetting emptyBox = new BoxSetting();
                if (allBoxObjet.size() == 0) {
                    emptyBox.setBoxName(context.getResources().getString(R.string.no_box));
                } else {
                    emptyBox.setBoxName(context.getResources().getString(R.string.select_box));
                }
                emptyBox.setBoxId(0);
                allBoxObjet.add(emptyBox);
                return new BoxAdapter(context, allBoxObjet);
            case TOOGLE:
                ArrayList<Object> allToogle = getAllObjet(context, TOOGLE);
                return new WidgetAdapter(context, allToogle);
            case STATE:
                ArrayList<Object> allStateObjet = getAllObjet(context, STATE);
                return new WidgetAdapter(context, allStateObjet);
            case PUSH:
                ArrayList<Object> allPush = getAllObjet(context, PUSH);
                return new WidgetAdapter(context, allPush);
            case LOCATION:
                ArrayList<Object> allLocationObjet = getAllObjet(context, LOCATION);
                return new WidgetAdapter(context, allLocationObjet);
            case MULTI:
                ArrayList<Object> allMultiObjet = getAllObjet(context, MULTI);
                return new WidgetAdapter(context, allMultiObjet);
            case VOCAL:
                ArrayList<Object> allVocalObjet = getAllObjet(context, VOCAL);
                return new WidgetAdapter(context, allVocalObjet);
        }
        return null;
    }

    /**
     * Récuperation de la ressource image
     * @param context
     * @param object
     * @param isON
     * @return
     */
    public static Bitmap getBitmapRessource(Context context, Object object, boolean isON) {

        Bitmap bitmap = null;

        switch(object.getClass().getSimpleName()) {
            case BOX:
                break;
            case TOOGLE:
                ToogleWidget toogleWidget = (ToogleWidget) object;
                ToogleWidgetBDD toogleWidgetBDD = new ToogleWidgetBDD(context);
                toogleWidgetBDD.open();
                bitmap = toogleWidgetBDD.getRessource(toogleWidget, isON);
                toogleWidgetBDD.close();
                break;
            case STATE:
                break;
            case PUSH:
                PushWidget pushWidget = (PushWidget) object;
                PushWidgetBDD pushWidgetBDD = new PushWidgetBDD(context);
                pushWidgetBDD.open();
                bitmap = pushWidgetBDD.getRessource(pushWidget, isON);
                pushWidgetBDD.close();
                break;
            case LOCATION:
                break;
            case MULTI:
                MultiWidget multiWidget = (MultiWidget) object;
                MultiWidgetBDD multiWidgetBDD = new MultiWidgetBDD(context);
                multiWidgetBDD.open();
                bitmap = multiWidgetBDD.getRessource(multiWidget, isON);
                multiWidgetBDD.close();
                break;
            case MULTI_RESS:
                MultiWidgetRess multiWidgetRess = (MultiWidgetRess) object;
                MultiWidgetBDD multiWidgetRessBDD = new MultiWidgetBDD(context);
                multiWidgetRessBDD.open();
                bitmap = multiWidgetRessBDD.getMutliWidgetRess(multiWidgetRess, isON);
                multiWidgetRessBDD.close();
                break;
        }

        return bitmap;
    }

    /**
     * Mise à jour des widget via intent
     * @param context
     */
    public static void updateAllWidget(Context context) {
        // Mise à jour des widgets ACTION
        Intent updateIntent = new Intent(context, WidgetToogleProvider.class);
        updateIntent.setAction(UPDATE_ALL_TOOGLE_WIDGET);
        context.sendBroadcast(updateIntent);

        // Mise à jour des widgets ETAT
        updateIntent = new Intent(context, WidgetStateProvider.class);
        updateIntent.setAction(UPDATE_ALL_STATE_WIDGET);
        context.sendBroadcast(updateIntent);

        // Mise à jour des widgets PUSH
        updateIntent = new Intent(context, WidgetPushProvider.class);
        updateIntent.setAction(UPDATE_ALL_PUSH_WIDGET);
        context.sendBroadcast(updateIntent);

        // Mise à jour des widgets LOCATION
        updateIntent = new Intent(context, WidgetLocationProvider.class);
        updateIntent.setAction(UPDATE_ALL_LOCATION_WIDGET);
        context.sendBroadcast(updateIntent);

        // Mise à jour des widget MULTI
        updateIntent = new Intent(context, MultiWidgetProvider.class);
        updateIntent.setAction(UPDATE_ALL_MULTI_WIDGET);
        context.sendBroadcast(updateIntent);

        // Mise à jour des widget VOCAL
        updateIntent = new Intent(context, WidgetVocalProvider.class);
        updateIntent.setAction(UPDATE_ALL_VOCAL_WIDGET);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Envoi un Intent NODATA au provider d'un widget
     * @param context
     * @param widgetClass
     * @param widgetId
     */
    public static void sendNoDataIntent(Context context, Class widgetClass, int widgetId) {
        Intent updateIntent = new Intent(context, widgetClass);
        updateIntent.setAction(INTENT_NO_DATA);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        context.sendBroadcast(updateIntent);
    }

    /**
     * hideKeyboard
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}


