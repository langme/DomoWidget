package illimiteremi.domowidget.DomoWidgetBdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;

import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.COL_ID_BOX;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.TABLE_DOMO_WIDGET;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.TABLE_LOCATION_WIDGET;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.TABLE_MUTLI_WIDGET;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.TABLE_PUSH_WIDGET;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.TABLE_STATE_WIDGET;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.TABLE_VOCAL_WIDGET;

/**
 * Created by rcouturi on 02/07/2016.
 */
public class DomoBoxBDD {

    static final String TAG      = "[DOMO_GLOBAL_BOX_BDD]";

    private SQLiteDatabase       bdd;
    private final DomoBaseSQLite domoBaseSQLite;

    public DomoBoxBDD(Context context){
        // On créer la BDD et sa table
        domoBaseSQLite = new DomoBaseSQLite(context, UtilsDomoWidget.NOM_BDD, null, UtilsDomoWidget.VERSION_BDD);
    }

    public void open(){
        // On ouvre la BDD en écriture
        bdd = domoBaseSQLite.getWritableDatabase();
    }

    public void close(){
        // On ferme l'accès à la BDD
        bdd.close();
    }

    public SQLiteDatabase getBDD(){
        return bdd;
    }

    /**
     * Enregistrement d'une box en BDD
     * @param box
     * @return
     */
    public long insertBox(BoxSetting box){
        // Création d'un ContentValues
        ContentValues values = new ContentValues();
        values.put(UtilsDomoWidget.COL_NAME, box.getBoxName());
        values.put(UtilsDomoWidget.COL_BOX_KEY, box.getBoxKey());
        values.put(UtilsDomoWidget.COL_URL_EXTERNE, box.getBoxUrlExterne());
        values.put(UtilsDomoWidget.COL_URL_INTERNE, box.getBoxUrlInterne());
        // On insère l'objet dans la BDD via le ContentValues
        // Log.d(TAG, "Insertion Box <" + box.getBoxName() + "> dans la BDD");
        return bdd.insert(UtilsDomoWidget.TABLE_GLOBAL_SETTING, null, values);
    }

    /**
     * Mise à jour de la box dans la BDD
     * @param box
     * @return
     */
    public int updateBox(BoxSetting box){
        ContentValues values = new ContentValues();
        values.put(UtilsDomoWidget.COL_NAME, box.getBoxName());
        values.put(UtilsDomoWidget.COL_BOX_KEY, box.getBoxKey());
        values.put(UtilsDomoWidget.COL_URL_EXTERNE, box.getBoxUrlExterne());
        values.put(UtilsDomoWidget.COL_URL_INTERNE, box.getBoxUrlInterne());
        // Log.d(TAG, "Result Update = " + nbUpdate);
        return bdd.update(UtilsDomoWidget.TABLE_GLOBAL_SETTING, values, COL_ID_BOX + " = " + box.getBoxId(), null);
    }

    /**
     * Suppression d'une box en BDD
     * @param idBox
     * @return
     */
    public int removeBox(int idBox){
        //Suppression d'un Widget de la BDD grâce à l'ID_WIDGET
        // Log.d(TAG, "Suppression Widget " + idWidget + " dans la BDD");
        return bdd.delete(UtilsDomoWidget.TABLE_GLOBAL_SETTING, COL_ID_BOX + " = " + idBox, null);
    }

    /**
     * Recherche d'une box en BDD via son id
     * @param idBox
     * @return GlobalSetting
     */
    public BoxSetting getBoxById(int idBox){
        // Récupère dans un Cursor
        Cursor c = bdd.query(UtilsDomoWidget.TABLE_GLOBAL_SETTING, new String[] {
                                COL_ID_BOX,
                                UtilsDomoWidget.COL_NAME,
                                UtilsDomoWidget.COL_BOX_KEY,
                                UtilsDomoWidget.COL_URL_EXTERNE,
                                UtilsDomoWidget.COL_URL_INTERNE}, COL_ID_BOX + " LIKE \"" + idBox +"\"" , null, null, null, null);
        // Log.d(TAG, "Récuperation Box <" + idBox + "> dans la BDD");
        // Si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            // Log.d(TAG, "Box non trouvé !");
            return null;
        }
        c.moveToFirst();
        BoxSetting box = cursorToObjet(c);
        c.close();
        return box;
    }

    /**
     * Recherche d'une box en BDD via son Nom
     * @param boxName
     * @return GlobalSetting
     */
    public BoxSetting getBoxByName(String boxName){
        // Récupère dans un Cursor
        Cursor c = bdd.query(UtilsDomoWidget.TABLE_GLOBAL_SETTING, new String[] {
                COL_ID_BOX,
                UtilsDomoWidget.COL_NAME,
                UtilsDomoWidget.COL_BOX_KEY,
                UtilsDomoWidget.COL_URL_EXTERNE,
                UtilsDomoWidget.COL_URL_INTERNE}, UtilsDomoWidget.COL_NAME + " LIKE \"" + boxName +"\"" , null, null, null, null);
        // Log.d(TAG, "Récuperation Box <" + idBox + "> dans la BDD");
        // Si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            // Log.d(TAG, "Box non trouvé !");
            return null;
        }
        c.moveToFirst();
        BoxSetting box = cursorToObjet(c);
        c.close();
        return box;
    }

    /**
     * Recherche d'une box en BDD via son Nom
     * @param boxKey
     * @return GlobalSetting
     */
    public BoxSetting getBoxByKey(String boxKey){
        // Récupère dans un Cursor
        Cursor c = bdd.query(UtilsDomoWidget.TABLE_GLOBAL_SETTING, new String[] {
                COL_ID_BOX,
                UtilsDomoWidget.COL_NAME,
                UtilsDomoWidget.COL_BOX_KEY,
                UtilsDomoWidget.COL_URL_EXTERNE,
                UtilsDomoWidget.COL_URL_INTERNE}, UtilsDomoWidget.COL_BOX_KEY + " LIKE \"" + boxKey +"\"" , null, null, null, null);
        // Log.d(TAG, "Récuperation Box <" + idBox + "> dans la BDD");
        // Si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            // Log.d(TAG, "Box non trouvé !");
            return null;
        }
        c.moveToFirst();
        BoxSetting box = cursorToObjet(c);
        c.close();
        return box;
    }

    /**
     * Récuperation des box
     * @return ArrayList<GlobalSetting>
     */
    public ArrayList<BoxSetting> getAllBox(){
        // Récupère dans un Cursor
        ArrayList<BoxSetting> listBox = new ArrayList<>();

        Cursor c = bdd.query(UtilsDomoWidget.TABLE_GLOBAL_SETTING, new String[] {
                COL_ID_BOX,
                UtilsDomoWidget.COL_NAME,
                UtilsDomoWidget.COL_BOX_KEY,
                UtilsDomoWidget.COL_URL_EXTERNE,
                UtilsDomoWidget.COL_URL_INTERNE},null , null, null, null, null);
        // Log.d(TAG, "Récuperation de la totalité des Box");

        // Si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            // Log.d(TAG, "Box non trouvé !");
            return null;
        }

        while (c.moveToNext()) {
            listBox.add(cursorToObjet(c));
        }
        // On ferme le cursor
        c.close();
        return listBox;
    }

    /**
     * Recherche si une Box est utilsé par un widget
     * @param idBox
     * @return GlobalSetting
     */
    public boolean isUse(int idBox){
        // Récupère dans un Cursor
        Cursor c = bdd.rawQuery("SELECT " + COL_ID_BOX + " FROM "  + TABLE_DOMO_WIDGET + " WHERE " +  COL_ID_BOX + " = " + idBox
                + " UNION "
                + "SELECT " + COL_ID_BOX + " FROM " + TABLE_LOCATION_WIDGET + " WHERE " +  COL_ID_BOX + " = " + idBox
                + " UNION "
                + "SELECT " + COL_ID_BOX + " FROM " + TABLE_PUSH_WIDGET + " WHERE " +  COL_ID_BOX + " = " + idBox
                + " UNION "
                + "SELECT " + COL_ID_BOX + " FROM " + TABLE_STATE_WIDGET + " WHERE " +  COL_ID_BOX + " = " + idBox
                + " UNION "
                + "SELECT " + COL_ID_BOX + " FROM " + TABLE_MUTLI_WIDGET + " WHERE " +  COL_ID_BOX + " = " + idBox
                + " UNION "
                + "SELECT " + COL_ID_BOX + " FROM " + TABLE_VOCAL_WIDGET + " WHERE " +  COL_ID_BOX + " = " + idBox, null);
        // Si aucun élément n'a été retourné dans la requête, on false
        if (c.getCount() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Lecgture du Cursor pour transformation Objet
     * @param c
     * @return
     */
    private BoxSetting cursorToObjet(Cursor c) {
        // On lui affecte toutes les infos grâce aux infos contenues dans le Cursor
        BoxSetting box = new BoxSetting();
        box.setBoxId(c.getInt(c.getColumnIndexOrThrow(COL_ID_BOX)));
        box.setBoxName(c.getString(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_NAME)));
        box.setBoxKey(c.getString(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_BOX_KEY)));
        box.setBoxUrlExterne(c.getString(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_URL_EXTERNE)));
        box.setBoxUrlInterne(c.getString(c.getColumnIndexOrThrow(UtilsDomoWidget.COL_URL_INTERNE)));
        return box;
    }
}

