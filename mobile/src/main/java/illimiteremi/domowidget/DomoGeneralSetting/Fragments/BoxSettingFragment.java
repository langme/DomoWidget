package illimiteremi.domowidget.DomoGeneralSetting.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.Objects;

import illimiteremi.domowidget.DomoAdapter.BoxAdapter;
import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;

import illimiteremi.domowidget.DomoUtils.DomoHttp;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.BOX;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.DONE;
import static illimiteremi.domowidget.DomoUtils.DomoUtils.hideKeyboard;

public class BoxSettingFragment extends Fragment {

    private static final String   TAG      = "[DOMO_BOX_FRAGMENT]";

    private Context               context;
    private boolean               isNew;            // Première Création du widget

    private Spinner               spinnerBox;       // Spinner de la liste des box
    private AutoCompleteTextView  boxName;          // Nom de la Box
    private AutoCompleteTextView  boxUrlExt;        // Accès externe
    private AutoCompleteTextView  boxUrlInt;        // Accès interne
    private AutoCompleteTextView  boxApiKey;        // Api Key
    private LinearLayout          linearBox;        // Layout de la configuration de la box

    private BoxAdapter            boxAdapter;       // Adapter de la liste des box
    private BoxSetting            boxSetting;       // Objet Box
    private MenuItem              itemAdd;          // Item du menu Ajouter
    private MenuItem              itemSave;         // Item du menu Sauvergarder
    private MenuItem              itemDelete;       // Item du menu Supprimer

    public static class SettingFragment extends DialogFragment {

        public SettingFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setIcon(R.drawable.no_data);
            alertDialogBuilder.setTitle(getResources().getString(R.string.widget_problem_popup));
            alertDialogBuilder.setMessage(getResources().getString(R.string.box_message_popup));
            alertDialogBuilder.setPositiveButton("OK", null);
            return alertDialogBuilder.create();
        }
    }

    public static class LoadingFragment extends DialogFragment {

        public LoadingFragment() {
        }

        @NonNull
        @Override
        public ProgressDialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage(getContext().getResources().getString(R.string.box_testing));
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

    public class BoxSaving extends AsyncTask<Void, Void, Boolean> {

        DialogFragment dialogFragment;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogFragment = new LoadingFragment();
            dialogFragment.show(getFragmentManager(), "Loading");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return Objects.equals(DomoHttp.httpRequest(context, boxSetting, null, null, null), DONE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            dialogFragment.dismiss();
            if (!aBoolean) {
                new SettingFragment().show(getFragmentManager(), "settingError");
            }
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.save_box), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        // Maj du titre
        getActivity().setTitle(getResources().getString(R.string.fragment_configuration));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_box_setting, container, false);
        setHasOptionsMenu(true);

        boxName       = (AutoCompleteTextView) view.findViewById(R.id.editBoxName);
        boxUrlExt     = (AutoCompleteTextView) view.findViewById(R.id.editUrlExt);
        boxUrlInt     = (AutoCompleteTextView) view.findViewById(R.id.editUrlInt);
        boxApiKey     = (AutoCompleteTextView) view.findViewById(R.id.editKey);
        spinnerBox    = (Spinner) view.findViewById(R.id.spinner);
        linearBox     = (LinearLayout) view.findViewById(R.id.linearBox);

        spinnerBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boxSetting = (BoxSetting) adapterView.getAdapter().getItem(i);
                if (boxSetting.getBoxId() != 0) {
                    boxName.setText(boxSetting.getBoxName());
                    boxUrlExt.setText(boxSetting.getBoxUrlExterne());
                    boxUrlInt.setText(boxSetting.getBoxUrlInterne());
                    boxApiKey.setText(boxSetting.getBoxKey());
                    linearBox.setVisibility(View.VISIBLE);
                } else {
                    linearBox.setVisibility(View.INVISIBLE);
                }
                // Rechargement des menus
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Chargement des spinners
        loadSpinner();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_box_activity, menu);
        itemAdd    = menu.findItem(R.id.add_action);
        itemSave   = menu.findItem(R.id.save_action);
        itemDelete = menu.findItem(R.id.delete_action);

        // Si aucune box est sélectionnée
        if (boxSetting != null) {
            if (boxSetting.getBoxId() == 0){
                itemSave.setVisible(false);
                itemAdd.setVisible(true);
                itemDelete.setVisible(false);
             } else {
                // si la box n'est pas utilisée
                if (DomoUtils.boxIsUsed(context, boxSetting)) {
                    itemDelete.setVisible(false);
                } else {
                    itemDelete.setVisible(true);
                }
            }
        }

        if (isNew) {
            itemAdd.setVisible(false);
        }



        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"idbox = " + boxSetting.getBoxId());
        hideKeyboard(getActivity());
        switch (item.getItemId()) {
            case R.id.save_action:
                Log.d(TAG, "Mise à jour de la box = " + backupBoxData());
                break;
            case R.id.add_action:
                createNewBox();
                break;
            case R.id.delete_action:
                DomoUtils.removeObjet(context, boxSetting);
                // Refresh current fragment
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(this).attach(this).commit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Chargement des Spinners
     */
    private void loadSpinner() {
        boxAdapter = (BoxAdapter) DomoUtils.createAdapter(context, BOX);
        spinnerBox.setAdapter(boxAdapter);
        // Si pas de box sélectionnée
        if (boxSetting == null) {
            if (spinnerBox.getCount() >= 2) {
                spinnerBox.setSelection(0);
            } else {
                spinnerBox.setSelection(spinnerBox.getAdapter().getCount()-1);
            }

        }
    }

    /**
     * Mise à jour de la box dans la bdd
     */
    private boolean backupBoxData() {

        // Verification du formulaire
        Boolean isCheck = true;
        if (boxName.getText().toString().length() == 0) {
            boxName.setError(getResources().getString(R.string.error_name));
            isCheck = false;
        }

        if (boxUrlExt.getText().toString().length() == 0 && boxUrlInt.getText().toString().length() == 0 ) {
            boxUrlExt.setError(getResources().getString(R.string.error_url_ext));
            boxUrlInt.setError(getResources().getString(R.string.error_url_ext));
            isCheck = false;
        }

        if (boxApiKey.getText().toString().length() == 0) {
            boxApiKey.setError(getResources().getString(R.string.error_key));
            isCheck = false;
        }

        if (isCheck){
            // Maj de la box en Bdd
            boxSetting.setBoxName(boxName.getText().toString());
            boxSetting.setBoxUrlExterne(boxUrlExt.getText().toString());
            boxSetting.setBoxUrlInterne(boxUrlInt.getText().toString());
            boxSetting.setBoxKey(boxApiKey.getText().toString());

            if (boxSetting.getBoxId() == 0) {
                long id = DomoUtils.insertObjet(context, boxSetting);
                boxSetting.setBoxId((int) id);
                Log.d(TAG, "Ajout de la box " + id + " - " + boxSetting.getBoxName());
            } else {
                DomoUtils.updateObjet(context, boxSetting);
                Log.d(TAG, "Mise à jour de la box " + boxSetting.getBoxName());
            }

            // Refresh current fragment
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();

            // Test de la configuration
            new BoxSaving().execute();
            isNew = false;
            return true;
        }
        return false;
    }

    /**
     * Création d'une nouvelle Box
     */
    private void createNewBox() {
        // Maj des menus
        itemSave.setVisible(true);
        itemAdd.setVisible(false);
        linearBox.setVisibility(View.VISIBLE);

        // Création d'une nouvelle Box Domotique
        boxSetting = new BoxSetting();
        boxSetting.setBoxName(getResources().getString(R.string.new_box));

        // Ajout d'une box en BDD
        int idBox = (int) DomoUtils.insertObjet(context, boxSetting);
        boxSetting.setBoxId(idBox);
        isNew = true;

        // Position du spinner
        spinnerBox.setEnabled(false);
        boxAdapter = (BoxAdapter) DomoUtils.createAdapter(context, BOX);
        spinnerBox.setAdapter(boxAdapter);
        int position = DomoUtils.getSpinnerPosition(context, boxSetting);
        spinnerBox.setSelection(position);
    }

}
