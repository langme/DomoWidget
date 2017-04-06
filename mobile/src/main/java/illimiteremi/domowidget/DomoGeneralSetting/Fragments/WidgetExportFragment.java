package illimiteremi.domowidget.DomoGeneralSetting.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.widget.Toast;


import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetLocation.LocationWidget;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidget;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidgetRess;
import illimiteremi.domowidget.DomoWidgetPush.PushWidget;
import illimiteremi.domowidget.DomoWidgetState.StateWidget;
import illimiteremi.domowidget.DomoWidgetToogle.ToogleWidget;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.*;
import static illimiteremi.domowidget.DomoUtils.DomoUtils.hideKeyboard;
import static illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget.*;

/**
 * Created by xzaq496 on 17/02/2017.
 */

public class WidgetExportFragment extends Fragment {

    private static final String TAG = "[DOMO_EXPORT]";
    private Context             context;
    private TextView            exportTextView;

    private File                file;
    private boolean             readyToSend;
    private final JSONObject    expotJson = new JSONObject();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_export_settings, menu);
        MenuItem itemSend = menu.findItem(R.id.action_send_mail);
        if (readyToSend) {
            itemSend.setVisible(true);
        } else {
            itemSend.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard(getActivity());
        switch (item.getItemId()) {
            case R.id.action_send_mail:
                Log.d(TAG, "Send a mail...");
                Uri path = Uri.fromFile(file);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                // set the type to 'email'
                emailIntent.setType("vnd.android.cursor.dir/email");
                // the attachment
                emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                // the mail subject
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.export_send_title));
                startActivity(Intent.createChooser(emailIntent , "Send email..."));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        // Maj du titre
        getActivity().setTitle(getResources().getString(R.string.export));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export_setting, container, false);
        setHasOptionsMenu(true);

        exportTextView   = (TextView) view.findViewById(R.id.exportTextView);

        new ExportTask().execute();

        return view;
    }

    /**
     * ExportTask
     */
    private class ExportTask extends AsyncTask<Void, Void, SpannableStringBuilder> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.setMessage(getResources().getString(R.string.export_loading));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected SpannableStringBuilder doInBackground(Void... params) {

            SpannableStringBuilder result = new SpannableStringBuilder ();

            SpannableString boxSpannable = new SpannableString("BOX DOMOTIQUE\n" + exportBox());
            boxSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, boxSpannable.length(), 0);
            result.append(boxSpannable);

            SpannableString toggleSpannable = new SpannableString("\nACTION\n" + exportToogleWidget());
            toggleSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, toggleSpannable.length(), 0);
            result.append(toggleSpannable);

            SpannableString stateSpannable = new SpannableString("\nINFO\n" + exportStateWidget());
            stateSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, stateSpannable.length(), 0);
            result.append(stateSpannable);

            SpannableString pushSpannable = new SpannableString("\nACTION\n" + exportPushWidget());
            pushSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, pushSpannable.length(), 0);
            result.append(pushSpannable);

            SpannableString multiSpannable = new SpannableString("\nMULTI-ACTION\n" + exportMultiWidget());
            multiSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, multiSpannable.length(), 0);
            result.append(multiSpannable);

            SpannableString locationSpannable = new SpannableString("\nGPS\n" + exportLocationWidget());
            locationSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, locationSpannable.length(), 0);
            result.append(locationSpannable);

            return result;

        }

        @Override
        protected void onPostExecute(SpannableStringBuilder s) {
            super.onPostExecute(s);
            exportTextView.setVisibility(View.VISIBLE);
            exportTextView.setText(s);
            try {
                // Create the file.
                String path = Environment.getExternalStorageDirectory () + File.separator + "/DomoWidget/";
                File folder = new File(path);
                folder.mkdirs();
                file = new File(folder, "domowidget.txt");
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(expotJson.toString());
                myOutWriter.close();
                fOut.flush();
                fOut.close();
                Log.d(TAG, "Fichier exporté : " + file.getAbsolutePath());

                // Message de sauvegarde
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.export_ok), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                readyToSend = true;

                // Rechargement des menus
                getActivity().invalidateOptionsMenu();
            } catch (IOException e) {
                Log.d(TAG, "Erreur : " + e);
            }
        }
    }

    /**
     * exportBox
     */
    private StringBuffer exportBox() {
        JSONObject jsonToStore = new JSONObject();
        JSONArray boxList      = new JSONArray();
        StringBuffer toJson    = new StringBuffer();

        ArrayList<Object> boxObjs =  DomoUtils.getAllObjet(context, BOX);
        for (Object boxObj  :  boxObjs) {
            BoxSetting box = (BoxSetting) boxObj;
            JSONObject jsonBoox = new JSONObject();
            try {
                jsonBoox.put(COL_ID_BOX, box.getBoxId());
                jsonBoox.put(COL_NAME, box.getBoxName());
                jsonBoox.put(COL_BOX_KEY, box.getBoxKey());
                jsonBoox.put(COL_URL_INTERNE, box.getBoxUrlInterne());
                jsonBoox.put(COL_URL_EXTERNE, box.getBoxUrlExterne());
                Log.d(TAG, "Export Box : " + jsonBoox);
                boxList.put(jsonBoox);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Erreur : " + e);
            }
        }

        try {
            jsonToStore.put(BOX, boxList).toString();
            expotJson.accumulate(BOX,boxList);
            toJson.append(jsonToStore);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toJson;
    }

    /**
     * exportToogleWidget
     * @return StringBuffer
     */
    private StringBuffer exportToogleWidget() {
        JSONObject jsonToStore = new JSONObject();
        JSONArray list         = new JSONArray();
        StringBuffer toJson    = new StringBuffer();

        ArrayList<Object> widgetObjs =  DomoUtils.getAllObjet(context, TOOGLE);
        for (Object obj  :  widgetObjs) {
            ToogleWidget widget = (ToogleWidget) obj;
            JSONObject json = new JSONObject();
            try {
                json.put(COL_ID_WIDGET, widget.getDomoId());
                json.put(COL_NAME, widget.getDomoName());
                json.put(COL_ID_BOX, widget.getDomoBox());
                json.put(COL_ON, widget.getDomoOn());
                json.put(COL_OFF, widget.getDomoOff());
                json.put(COL_ID_IMAGE_ON, widget.getDomoIdImageOn());
                json.put(COL_ID_IMAGE_OFF, widget.getDomoIdImageOff());
                json.put(COL_ETAT, widget.getDomoState());
                json.put(COL_LOCK, widget.getDomoLock());
                json.put(COL_EXP_REG, widget.getDomoExpReg());
                json.put(COL_TIME_OUT, widget.getDomoTimeOut());
                Log.d(TAG, "Export " + widget.getDomoName() + " : " + json);
                list.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Erreur : " + e);
            }
        }

        try {
            jsonToStore.putOpt(TOOGLE, list).toString();
            expotJson.accumulate(TOOGLE,list);
            toJson.append(jsonToStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toJson;
    }

    /**
     * exportStateWidget
     * @return StringBuffer
     */
    private StringBuffer exportStateWidget() {
        JSONObject jsonToStore = new JSONObject();
        JSONArray list         = new JSONArray();
        StringBuffer toJson    = new StringBuffer();

        ArrayList<Object> widgetObjs =  DomoUtils.getAllObjet(context, STATE);
        for (Object obj  :  widgetObjs) {
            StateWidget widget = (StateWidget) obj;
            JSONObject json = new JSONObject();
            try {
                json.put(COL_ID_WIDGET, widget.getDomoId());
                json.put(COL_NAME, widget.getDomoName());
                json.put(COL_ID_BOX, widget.getDomoBox());
                json.put(COL_ETAT, widget.getDomoState());
                json.put(COL_UNIT, widget.getDomoUnit());
                json.put(COL_COLOR, widget.getDomoColor());
                Log.d(TAG, "Export " + widget.getDomoName() + " : " + json);
                list.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Erreur : " + e);
            }
        }

        try {
            jsonToStore.put(STATE, list).toString();
            expotJson.accumulate(STATE,list);
            toJson.append(jsonToStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toJson;
    }

    /**
     * exportPushWidget
     * @return StringBuffer
     */
    private StringBuffer exportPushWidget() {
        JSONObject jsonToStore = new JSONObject();
        JSONArray list         = new JSONArray();
        StringBuffer toJson    = new StringBuffer();

        ArrayList<Object> widgetObjs =  DomoUtils.getAllObjet(context, PUSH);
        for (Object obj  :  widgetObjs) {
            PushWidget widget = (PushWidget) obj;
            JSONObject json = new JSONObject();
            try {
                json.put(COL_ID_WIDGET, widget.getDomoId());
                json.put(COL_NAME, widget.getDomoName());
                json.put(COL_ID_BOX, widget.getDomoBox());
                json.put(COL_ACTION, widget.getDomoAction());
                json.put(COL_ID_IMAGE_ON, widget.getDomoIdImageOn());
                json.put(COL_ID_IMAGE_OFF, widget.getDomoIdImageOff());
                json.put(COL_LOCK, widget.getDomoLock());
                Log.d(TAG, "Export " + widget.getDomoName() + " : " + json);
                list.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Erreur : " + e);
            }
        }
        try {
            jsonToStore.put(PUSH, list).toString();
            expotJson.accumulate(PUSH,list);
            toJson.append(jsonToStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toJson;
    }

    /**
     * exportMultiWidget
     * @return StringBuffer
     */
    private StringBuffer exportMultiWidget() {
        JSONObject jsonToStore = new JSONObject();
        JSONArray list         = new JSONArray();
        StringBuffer toJson    = new StringBuffer();

        ArrayList<Object> widgetObjs =  DomoUtils.getAllObjet(context, MULTI);
        for (Object obj  :  widgetObjs) {
            MultiWidget widget = (MultiWidget) obj;
            JSONObject json    = new JSONObject();
            JSONArray ressList = new JSONArray();
            try {
                json.put(COL_ID_WIDGET, widget.getDomoId());
                json.put(COL_NAME, widget.getDomoName());
                json.put(COL_ID_BOX, widget.getDomoBox());
                json.put(COL_ETAT, widget.getDomoState());
                json.put(COL_ID_IMAGE_ON, widget.getDomoIdImageOn());
                json.put(COL_ID_IMAGE_OFF, widget.getDomoIdImageOff());
                json.put(COL_TIME_OUT, widget.getDomoTimeOut());

                for (MultiWidgetRess ress : widget.getMutliWidgetRess()) {
                    JSONObject jsonRess = new JSONObject();
                    jsonRess.put(COL_ID_WIDGET, ress.getDomoId());
                    jsonRess.put(COL_NAME, ress.getDomoName());
                    jsonRess.put(COL_ACTION, ress.getDomoAction());
                    jsonRess.put(COL_ID_IMAGE_ON, ress.getDomoIdImageOn());
                    jsonRess.put(COL_ID_IMAGE_OFF, ress.getDomoIdImageOff());
                    jsonRess.put(COL_DEFAULT_MULTI_RESS, ress.getDomoDefault());
                    ressList.put(jsonRess);
                }
                json.put(MULTI_RESS, ressList);
                Log.d(TAG, "Export " + widget.getDomoName() + " : " + json);
                list.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Erreur : " + e);
            }
        }

        try {
            jsonToStore.put(MULTI, list).toString();
            expotJson.accumulate(MULTI,list);
            toJson.append(jsonToStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toJson;
    }

    /**
     * exportLocationWidget
     * @return StringBuffer
     */
    private StringBuffer exportLocationWidget() {
        JSONObject jsonToStore = new JSONObject();
        JSONArray list         = new JSONArray();
        StringBuffer toJson    = new StringBuffer();

        ArrayList<Object> widgetObjs =  DomoUtils.getAllObjet(context, LOCATION);
        for (Object obj  :  widgetObjs) {
            LocationWidget widget = (LocationWidget) obj;
            JSONObject json = new JSONObject();
            try {
                json.put(COL_ID_BOX, widget.getDomoBox());
                json.put(COL_NAME, widget.getDomoName());
                json.put(COL_ID_WIDGET, widget.getDomoId());
                json.put(COL_ACTION, widget.getDomoAction());
                json.put(COL_TIME_OUT, widget.getDomoTimeOut());
                json.put(COL_DISTANCE, widget.getDomoDistance());
                json.put(COL_LOCATION, widget.getDomoLocation());
                json.put(COL_PROVIDER, widget.getDomoProvider());
                Log.d(TAG, "Export " + widget.getDomoName() + " : " + json);
                list.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Erreur : " + e);
            }
        }

        try {
            jsonToStore.put(LOCATION, list).toString();
            expotJson.accumulate(LOCATION,list);
            toJson.append(jsonToStore);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return toJson;
    }

}
