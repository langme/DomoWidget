package illimiteremi.domowidget.DomoWidgetBdd;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetLocation.LocationWidget;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidget;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidgetRess;
import illimiteremi.domowidget.DomoWidgetPush.PushWidget;
import illimiteremi.domowidget.DomoWidgetState.StateWidget;
import illimiteremi.domowidget.DomoWidgetToogle.ToogleWidget;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.BOX;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.MULTI;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.MULTI_RESS;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PUSH;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.STATE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.TOOGLE;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ACTION;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_BOX_KEY;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_COLOR;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_DEFAULT_MULTI_RESS;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_DISTANCE;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ETAT;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_EXP_REG;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ID_IMAGE_OFF;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ID_IMAGE_ON;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ID_WIDGET;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_LOCK;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_NAME;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_OFF;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ON;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_PROVIDER;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_TIME_OUT;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_UNIT;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_URL_EXTERNE;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_URL_INTERNE;

/**
 * Created by XZAQ496 on 21/03/2017.
 */

public class ImportWidget {

    private static final String TAG = "[DOMO_IMPORT]";

    private final Context context;

    public ImportWidget(Context context) {
        this.context = context;
    }

    /**
     * readTextFile
     * @param path
     * @return
     */
    public String readTextFile(String path){
        StringBuilder builder = new StringBuilder();
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * isJSONValid
     * @param json
     * @return
     */
    public boolean isJSONValid(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException ex) {
            try {
                new JSONArray(json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /**
     * importBox
     * @param fileContent
     */
    public void importBox(String fileContent) {
        try {
            JSONObject jsnobject = new JSONObject(fileContent);
            JSONArray jsonArray = jsnobject.getJSONArray(BOX);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                BoxSetting boxSetting = new BoxSetting();
                boxSetting.setBoxName(explrObject.getString(COL_NAME));
                boxSetting.setBoxKey(explrObject.getString(COL_BOX_KEY));
                boxSetting.setBoxUrlExterne(explrObject.getString(COL_URL_EXTERNE));
                boxSetting.setBoxUrlInterne(explrObject.getString(COL_URL_INTERNE));
                // Vérification si la box est deja en bdd (key)
                DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                domoBoxBDD.open();
                BoxSetting exisingBox = domoBoxBDD.getBoxByKey(boxSetting.getBoxKey());
                domoBoxBDD.close();

                if (exisingBox == null) {
                    DomoUtils.insertObjet(context, boxSetting);
                    Log.d(TAG, explrObject.toString());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
        }
    }

    /**
     * importToogleWidget
     * @param fileContent
     */
    public void importToogleWidget(String fileContent) {
        try {
            JSONObject jsnobject = new JSONObject(fileContent);
            JSONArray jsonArray = jsnobject.getJSONArray(TOOGLE);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                ToogleWidget toogleWidget = new ToogleWidget(context, -(explrObject.getInt(COL_ID_WIDGET)));
                toogleWidget.setDomoName(explrObject.getString(COL_NAME));
                toogleWidget.setDomoOn(explrObject.getString(COL_ON));
                toogleWidget.setDomoOff(explrObject.getString(COL_OFF));
                toogleWidget.setDomoIdImageOn(explrObject.getInt(COL_ID_IMAGE_ON));
                toogleWidget.setDomoIdImageOff(explrObject.getInt(COL_ID_IMAGE_OFF));
                toogleWidget.setDomoState(explrObject.getString(COL_ETAT));
                toogleWidget.setDomoLock(explrObject.getInt(COL_LOCK));
                toogleWidget.setDomoExpReg(explrObject.getString(COL_EXP_REG));
                toogleWidget.setDomoTimeOut(explrObject.getInt(COL_TIME_OUT));
                if (DomoUtils.getObjetById(context,toogleWidget) == null) {
                    DomoUtils.insertObjet(context, toogleWidget);
                }
                Log.d(TAG, explrObject.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
        }
    }

    /**
     * importPushWidget
     * @param fileContent
     */
    public void importPushWidget(String fileContent) {
        try {
            JSONObject jsnobject = new JSONObject(fileContent);
            JSONArray jsonArray = jsnobject.getJSONArray(PUSH);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                PushWidget pushWidget = new PushWidget(context, -(explrObject.getInt(COL_ID_WIDGET)));
                pushWidget.setDomoName(explrObject.getString(COL_NAME));
                pushWidget.setDomoAction(explrObject.getString(COL_ACTION));
                pushWidget.setDomoIdImageOn(explrObject.getInt(COL_ID_IMAGE_ON));
                pushWidget.setDomoIdImageOff(explrObject.getInt(COL_ID_IMAGE_OFF));
                pushWidget.setDomoLock(explrObject.getInt(COL_LOCK));
                if (DomoUtils.getObjetById(context,pushWidget) == null) {
                    DomoUtils.insertObjet(context, pushWidget);
                }
                Log.d(TAG, explrObject.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
        }
    }

    /**
     * importPushWidget
     * @param fileContent
     */
    public void importStateWidget(String fileContent) {
        try {
            JSONObject jsnobject = new JSONObject(fileContent);
            JSONArray jsonArray = jsnobject.getJSONArray(STATE);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                StateWidget stateWidget = new StateWidget(context, -(explrObject.getInt(COL_ID_WIDGET)));
                stateWidget.setDomoName(explrObject.getString(COL_NAME));
                stateWidget.setDomoState(explrObject.getString(COL_ETAT));
                stateWidget.setDomoUnit(explrObject.getString(COL_UNIT));
                stateWidget.setDomoColor(explrObject.getString(COL_COLOR));
                if (DomoUtils.getObjetById(context,stateWidget) == null) {
                    DomoUtils.insertObjet(context, stateWidget);
                }
                Log.d(TAG, explrObject.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
        }
    }

    /**
     * importLocationWidget
     * @param fileContent
     */
    public void importLocationWidget(String fileContent) {
        try {
            JSONObject jsnobject = new JSONObject(fileContent);
            JSONArray jsonArray = jsnobject.getJSONArray(LOCATION);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                LocationWidget locationWidget = new LocationWidget(context, -(explrObject.getInt(COL_ID_WIDGET)));
                locationWidget.setDomoName(explrObject.getString(COL_NAME));
                locationWidget.setDomoAction(explrObject.getString(COL_ACTION));
                locationWidget.setDomoTimeOut(explrObject.getInt(COL_TIME_OUT));
                locationWidget.setDomoDistance(explrObject.getInt(COL_DISTANCE));
                locationWidget.setDomoProvider(explrObject.getString(COL_PROVIDER));
                if (DomoUtils.getObjetById(context,locationWidget) == null) {
                    DomoUtils.insertObjet(context, locationWidget);
                }
                Log.d(TAG, explrObject.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
        }
    }

    /**
     * importMultiWidget
     * @param fileContent
     */
    public void importMultiWidget(String fileContent) {
        try {
            JSONObject jsnobject = new JSONObject(fileContent);
            JSONArray jsonArray = jsnobject.getJSONArray(MULTI);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                MultiWidget multiWidget = new MultiWidget(context, -(explrObject.getInt(COL_ID_WIDGET)));
                multiWidget.setDomoName(explrObject.getString(COL_NAME));
                multiWidget.setDomoState(explrObject.getString(COL_ETAT));
                multiWidget.setDomoIdImageOn(explrObject.getInt(COL_ID_IMAGE_ON));
                multiWidget.setDomoIdImageOff(explrObject.getInt(COL_ID_IMAGE_OFF));
                multiWidget.setDomoTimeOut(explrObject.getInt(COL_TIME_OUT));

                if (DomoUtils.getObjetById(context,multiWidget) == null) {
                    DomoUtils.insertObjet(context, multiWidget);
                    JSONArray ressArray = explrObject.getJSONArray(MULTI_RESS);
                    for (int x = 0; x < ressArray.length(); i++) {
                        JSONObject explrRessObject = ressArray.getJSONObject(i);
                        MultiWidgetRess multiWidgetRess = new MultiWidgetRess(-(explrRessObject.getInt(COL_ID_WIDGET)));
                        multiWidgetRess.setDomoName(explrRessObject.getString(COL_NAME));
                        multiWidgetRess.setDomoAction(explrRessObject.getString(COL_ACTION));
                        multiWidgetRess.setDomoIdImageOn(explrRessObject.getInt(COL_ID_IMAGE_ON));
                        multiWidgetRess.setDomoIdImageOff(explrRessObject.getInt(COL_ID_IMAGE_OFF));
                        multiWidgetRess.setDomoDefault(explrRessObject.getInt(COL_DEFAULT_MULTI_RESS));
                        DomoUtils.insertObjet(context, multiWidgetRess);
                        Log.d(TAG, explrRessObject.toString());
                    }
                }
                Log.d(TAG, explrObject.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur : " + e);
        }

    }

}
