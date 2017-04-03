package illimiteremi.domowidget.DomoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XZAQ496 on 27/12/2016.
 */

public class DomoConstants {

    public static final String POSITION_VIEW         = "POSITION_VIEW";
    public static final String CONFIGURATION         = "CONFIGURATION";
    public static final int    LOCK_TIME             = 5 * 1000;
    public static final String DEFAULT_COLOR         = "0";
    public static final int    DEFAULT_TIMEOUT       = 2;
    public static final int    DEFAULT_WEAR_TIMEOUT  = 0;
    public static final String JEEDOM_URL            = "/core/api/jeeApi.php?apikey=";
    public static final String COMMANDE              = "type=cmd&id=";
    public static final String GEOLOC                = "type=geoloc&id=";
    public static final String SCENARIO              = "type=scenario&id=";
    public static final String ACTION                = "&action=";
    public static final String INTERCATION           = "&type=interact&query=%";

    public static final String TOOGLE     = "ToogleWidget";
    public static final String STATE      = "StateWidget";
    public static final String PUSH       = "PushWidget";
    public static final String LOCATION   = "LocationWidget";
    public static final String MULTI      = "MultiWidget";
    public static final String VOCAL      = "VocalWidget";
    public static final String WEAR       = "WearSetting";
    public static final String BOX        = "BoxSetting";
    public static final String MULTI_RESS = "MultiWidgetRess";
    public static final String ICON       = "IconSetting";

    public static final String ERROR             = "ERROR";
    public static final String OK                = "OK";
    public static final String NOK               = "NOK";
    public static final String DONE              = "DONE";
    public static final int    PERMISSION_OK     = 1;
    public static final int    NEW_WIDGET        = -1;
    public static final int    NO_WIDGET         = -2;

    public static final int    MOBILE_TIME_OUT   = 1000;
    public static final int    WIFI_TIME_OUT     = 250;

    public static final String IMPORT_DOMO_WIDGET = "IMPORT_DOMO_WIDGET";
    public static final String IMPORT_ICON        = "IMPORT_ICON";

    // GPS
    public static final String  UPDATE_ALL_LOCATION_WIDGET = "android.appwidget.action.LOCATION_WIDGET_UPDATE_ALL";
    public static final String  LOCATION_WIDGET_CHANGED    = "android.appwidget.action.LOCATION_WIDGET_CHANGED";

    public static final int     TIMEOUT_LOCATION           = 15;
    public static final int     DISTANCE_LOCATION          = 100;
    public static final String  LOCATION_LABEL             = "Widget GPS";
    public enum PROVIDER_TYPE {
        GPS          (0, "gps"),
        NETWORK      (1, "network"),
        PASSIVE      (2, "passive");

        private int     code       = 0;
        private String  provider   = "";

        /**
         * MEDIA_TYPE constructor
         * @param _code
         * @param _provider
         */
        PROVIDER_TYPE(int _code, String _provider) {
            code       = _code;
            provider   = _provider;
        }

        /**
         * getCode
         * @return
         */
        public int getCode() { return code; }

        /**
         * getLibelle
         * @return
         */
        public String getProvider() {
            return provider;
        }


        /**
         * Retourne la liste des medias (en string)
         * @return
         */
        public static List<String> toList() {
            List<String> myList = new ArrayList<>();
            for (PROVIDER_TYPE media: PROVIDER_TYPE.values()) {
                myList.add(media.getProvider());
            }
            return myList;
        }
    }

    // STATE
    public static final String  UPDATE_ALL_STATE_WIDGET = "android.appwidget.action.STATE_WIDGET_UPDATE_ALL";
    public static final String  NO_VALUE                = "--.-";
    public static final String  STATE_LABEL             = "Widget Info";

    // TOOGLE
    public static final String  UPDATE_ALL_TOOGLE_WIDGET = "android.appwidget.action.TOOGLE_WIDGET_UPDATE_ALL";
    public static final String  TOOGLE_LABEL             = "Widget Action";

    // PUSH
    public static final String  UPDATE_ALL_PUSH_WIDGET = "android.appwidget.action.PUSH_WIDGET_UPDATE_ALL";
    public static final String  PUSH_LABEL             = "Widget Push";
    public static final int     PUSH_TIME              = 1000;

    //MULTI
    public static final String  UPDATE_ALL_MULTI_WIDGET = "android.appwidget.action.MULTI_WIDGET_UPDATE_ALL";
    public static final String  MULTI_LABEL             = "Widget Mutli";

    // VOCAL
    public static final String  UPDATE_ALL_VOCAL_WIDGET = "android.appwidget.action.VOCAL_WIDGET_UPDATE_ALL";
    public static final String  VOCAL_LABEL             = "Widget Vocal";

    // ALL
    public static final String  INTENT_NO_DATA          = "android.appwidget.action.APPWIDGET_NODATA";

}