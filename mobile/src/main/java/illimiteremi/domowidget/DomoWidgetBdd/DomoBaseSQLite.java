package illimiteremi.domowidget.DomoWidgetBdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoConstants;
import illimiteremi.domowidget.DomoUtils.DomoUtils;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.BOX;

/**
 * Created by rcouturi on 02/07/2016.
 */
public class DomoBaseSQLite extends SQLiteOpenHelper {

    private static final String  TAG      = "[DOMO_BDD]";
    private final        Context context;

    public DomoBaseSQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        // Log.d(TAG, "Version de la BDD : " + version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "Création des tables dans BDD...");
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_TOOGLE_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_STATE_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_RES_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_PUSH_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_LOCATION_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_MUTLI_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_MUTLI_RESS_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_GLOBAL_SETTING_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_VOCAL_BDD);
        sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_WEAR_BDD);
        addRessource(sqLiteDatabase);
        Log.d(TAG, "Fin Création BDD");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(TAG, "Version de la base : " + oldVersion + "/" + newVersion);

        if (newVersion >=14) {
            Log.d(TAG, "Mise à jour version 14");
            try {
                sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_MUTLI_BDD);
                sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_MUTLI_RESS_BDD);
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=15) {
            Log.d(TAG, "Mise à jour version 15");
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_DOMO_WIDGET + " ADD COLUMN "
                        + UtilsDomoWidget.COL_TIME_OUT + " INTEGER DEFAULT 2");
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=16) {
            Log.d(TAG, "Mise à jour version 16");
            try {
                ContentValues values = new ContentValues();
                values.put(UtilsDomoWidget.COL_RESS_NAME, "light_red");
                sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
                values.put(UtilsDomoWidget.COL_RESS_NAME, "light_50");
                sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
                values.put(UtilsDomoWidget.COL_RESS_NAME, "light_blue");
                sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
                values.put(UtilsDomoWidget.COL_RESS_NAME, "light_green");
                sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=17) {
            Log.d(TAG, "Mise à jour version 17");
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_MUTLI_WIDGET + " ADD COLUMN "
                        + UtilsDomoWidget.COL_TIME_OUT + " INTEGER DEFAULT 2");
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=18) {
            Log.d(TAG, "Mise à jour version 18");
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_STATE_WIDGET + " ADD COLUMN "
                        + UtilsDomoWidget.COL_COLOR + " TEXT DEFAULT 'FF000000'");
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=19) {
            Log.d(TAG, "Mise à jour version 19");
            try {
                sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_GLOBAL_SETTING_BDD);
                Log.d(TAG, "Migration des table pour gestion des BOX (ajout ID_BOX)");
                // Ajout de de l'identifiant BOX pour chaque widget
                try {
                    sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_DOMO_WIDGET + " ADD COLUMN " + UtilsDomoWidget.COL_ID_BOX + " INTEGER");
                } catch (SQLiteException e) {
                        Log.e(TAG, "SQLite erreur " + e);
                }

                try {
                    sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_LOCATION_WIDGET + " ADD COLUMN " + UtilsDomoWidget.COL_ID_BOX + " INTEGER");
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQLite erreur " + e);
                }

                try {
                    sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_STATE_WIDGET + " ADD COLUMN " + UtilsDomoWidget.COL_ID_BOX + " INTEGER");
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQLite erreur " + e);
                }

                try {
                    sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_MUTLI_WIDGET + " ADD COLUMN " + UtilsDomoWidget.COL_ID_BOX + " INTEGER");
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQLite erreur " + e);
                }

                try {
                    sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_PUSH_WIDGET + " ADD COLUMN " + UtilsDomoWidget.COL_ID_BOX + " INTEGER");
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQLite erreur " + e);
                }
                migrationAddBox();
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=20) {
            Log.d(TAG, "Mise à jour version 20");
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_STATE_WIDGET + " ADD COLUMN "
                        + UtilsDomoWidget.COL_MANUEL_UPDATE + " INTEGER DEFAULT 0");
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=21) {
            Log.d(TAG, "Mise à jour version 21");
            try {
                sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_VOCAL_BDD);
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=22) {
            Log.d(TAG, "Mise à jour version 22");
            try {
                sqLiteDatabase.execSQL(UtilsDomoWidget.CREATE_WEAR_BDD);
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=23) {
            Log.d(TAG, "Mise à jour version 23");
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_DOMO_WEAR
                        + " ADD COLUMN " + UtilsDomoWidget.COL_SHAKE_TIME_OUT + " INTEGER DEFAULT 5");
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_DOMO_WEAR
                        + " ADD COLUMN " + UtilsDomoWidget.COL_SHAKE_LEVEL + " INTEGER DEFAULT 5");
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }

        if (newVersion >=24) {
            Log.d(TAG, "Mise à jour version 24");
            try {
                sqLiteDatabase.execSQL("ALTER TABLE " + UtilsDomoWidget.TABLE_VOCAL_WIDGET
                        + " ADD COLUMN " + UtilsDomoWidget.COL_KEYPHRASE + " TEXT");
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLite erreur " + e);
            }
        }
    }

    /**
     * Ajout des box suivant les widgets déjà presents
     */
    private void migrationAddBox() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Création de la liste des widgets à migrer
                    ArrayList<String> widgetList = new ArrayList<>();
                    widgetList.add(DomoConstants.TOOGLE);
                    widgetList.add(DomoConstants.PUSH);
                    widgetList.add(DomoConstants.STATE);
                    widgetList.add(DomoConstants.LOCATION);
                    widgetList.add(DomoConstants.MULTI);

                    for (String widget: widgetList) {
                        Log.d(TAG, "Migration : " + widget);
                        ArrayList<Object> widgets =  DomoUtils.getAllObjet(context, widget);
                        if (widgets != null) {
                            for (Object object : widgets) {
                                try {
                                    Log.d(TAG, "Début Migration : " + object);
                                    // Récupération de l'information box
                                    Field boxField = object.getClass().getDeclaredField("selectedBox");
                                    boxField.setAccessible(true);
                                    BoxSetting boxSetting = (BoxSetting) boxField.get(object);

                                    Log.d(TAG, "BOX : " + boxSetting);
                                    // Récupération de l'information KEY
                                    Field keyfield = object.getClass().getDeclaredField("domoKey");
                                    keyfield.setAccessible(true);
                                    String boxKey = (String) keyfield.get(object);
                                    Log.d(TAG, "KEY : " + boxKey);
                                    // Récupération de l'information URL
                                    Field urlField = object.getClass().getDeclaredField("domoUrl");
                                    urlField.setAccessible(true);
                                    String boxUrl = (String) urlField.get(object);
                                    Log.d(TAG, "URL : " + boxUrl);
                                    // Récupération de l'information selectedBox
                                    Field selectedBoxField = object.getClass().getDeclaredField("selectedBox");
                                    selectedBoxField.setAccessible(true);
                                    String selectedBox = (String) keyfield.get(object);

                                    if (!boxKey.isEmpty() & !boxUrl.isEmpty() & selectedBox.isEmpty()) {
                                        long boxReference;
                                        if (boxSetting == null) {
                                            // Ajout d'une nouvelle box en BDD si non présente
                                            DomoBoxBDD domoBoxBDD = new DomoBoxBDD(context);
                                            domoBoxBDD.open();
                                            BoxSetting exisingBox = domoBoxBDD.getBoxByKey(boxKey);
                                            domoBoxBDD.close();
                                            if (exisingBox == null) {
                                                int objetNumber = DomoUtils.getAllObjet(context, BOX).size();
                                                int boxNumber = (objetNumber == 0) ? 1 : objetNumber + 1;
                                                boxSetting = new BoxSetting();
                                                boxSetting.setBoxKey(boxKey);
                                                boxSetting.setBoxName("BOX DOMOTIQUE " + boxNumber);
                                                boxSetting.setBoxUrlExterne(boxUrl);
                                                boxSetting.setBoxUrlInterne("");
                                                boxReference = DomoUtils.insertObjet(context, boxSetting);
                                                Log.d(TAG, "Ajout d'une nouvelle Box en bdd : " + boxSetting.getBoxName());
                                            } else {
                                                boxReference = exisingBox.getBoxId();
                                            }
                                        } else {
                                            boxReference = boxSetting.getBoxId();
                                        }
                                        // Mise à jour du widget avec l'id BOX
                                        Field domoBoxField = object.getClass().getDeclaredField("domoBox");
                                        domoBoxField.setAccessible(true);
                                        domoBoxField.set(object, (int) boxReference);
                                        DomoUtils.updateObjet(context, object);
                                        Log.d(TAG, "Mise à jour du widget " + object + " avec la Box :  " + boxReference);
                                    } else {
                                        Log.d(TAG, "Impossible d'associer une box au widget ! / Widget déjà mgré !");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Erreur : " + e);
                                }
                            }
                        }
                    }
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQLite erreur " + e);
                }
            }
        }).start();
        // Mise à jour des widgets
        DomoUtils.updateAllWidget(context);
    }

    /**
     * ajout des ressources de la base
     * @param sqLiteDatabase
     */
    private static void addRessource(SQLiteDatabase sqLiteDatabase) {
        // Ajout resource
        Log.d(TAG, "Ajout des ressources Images");
        ContentValues values = new ContentValues();
        values.put(UtilsDomoWidget.COL_RESS_NAME, "toggle_metal_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "toggle_metal_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "light_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "light_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "store_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "store_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "door_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "door_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "fibaro_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "fibaro_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "arcade_red_push");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "arcade_red_release");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "clap_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "clap_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "vmc_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "vmc_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "garage_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "garage_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "spot_on");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "spot_off");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "light_red");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "light_50");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "light_blue");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
        values.put(UtilsDomoWidget.COL_RESS_NAME, "light_green");
        sqLiteDatabase.insert(UtilsDomoWidget.TABLE_RESS_WIDGET, null, values);
    }
}
