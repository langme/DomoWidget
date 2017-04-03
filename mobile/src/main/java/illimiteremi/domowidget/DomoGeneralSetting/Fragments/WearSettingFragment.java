package illimiteremi.domowidget.DomoGeneralSetting.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import illimiteremi.domowidget.DomoAdapter.BoxAdapter;
import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWear.WearSetting;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.BOX;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.DEFAULT_WEAR_TIMEOUT;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.WEAR;
import static illimiteremi.domowidget.DomoUtils.DomoUtils.hideKeyboard;

public class WearSettingFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks{

    private static final String   TAG      = "[DOMO_WEAR_FRAGMENT]";

    private Context               context;
    private Spinner               spinnerBox;       // Spinner de la liste des box
    private AutoCompleteTextView  timeOut;          // TimeOut avant exécution action
    private WearSetting           wearSetting;      // Confifguration de l'env Wear
    private TextView              textNode;         // Nom de la montre connectée

    private BoxAdapter            boxAdapter;       // Adapter de la liste des box
    private BoxSetting            boxSetting;       // Objet Box

    private GoogleApiClient       mGoogleApiClient; // Api Google

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        // Maj du titre
        getActivity().setTitle(getResources().getString(R.string.fragment_wear));

        // Récuperation de la configuration android Wear en bdd
        ArrayList<Object> wearObjects = DomoUtils.getAllObjet(context, WEAR);
        if (wearObjects.size() != 0) {
            wearSetting = (WearSetting) wearObjects.get(0);
        } else {
            wearSetting = new WearSetting();
            wearSetting.setId((int) DomoUtils.insertObjet(context, wearSetting));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wear_setting, container, false);
        setHasOptionsMenu(true);

        spinnerBox    = (Spinner) view.findViewById(R.id.spinner);
        timeOut       = (AutoCompleteTextView) view.findViewById(R.id.editTimeOut);
        textNode      = (TextView) view.findViewById(R.id.textNode);
        textNode.setFocusable(false);

        // Chargement des spinners
        loadSpinner();

        // Affichage des valeurs enregistrée
        timeOut.setText(String.format(Locale.getDefault(), "%d", wearSetting.getWearTimeOutTimeOut()));
        BoxSetting selectedBox = new BoxSetting();
        selectedBox.setBoxId(wearSetting.getBoxId());
        int spinnerPostion = DomoUtils.getSpinnerPosition(context, selectedBox);
        spinnerBox.setSelection(spinnerPostion);

        spinnerBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boxSetting = (BoxSetting) adapterView.getAdapter().getItem(i);
                if (boxSetting.getBoxId() != 0) {
                    wearSetting.setBoxId(boxSetting.getBoxId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        textNode.setBackgroundColor(Color.argb(100,255,0,0));

        // Connection à GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save_settings, menu);
        MenuItem itemDelete = menu.findItem(R.id.delete_action);
        itemDelete.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard(getActivity());
        switch (item.getItemId()) {
            case R.id.save_action:
                Log.d(TAG, "Mise à jour de la box = " + backupBoxData());
                break;
            case R.id.delete_action:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String watchName = connectedNodes.get(0).getDisplayName();
                            textNode.setText(watchName);
                            textNode.setBackgroundColor(Color.argb(100,13,151,36));
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur : " + e);
                        }
                    }
                });
                Log.d(TAG, connectedNodes.toString());
                mGoogleApiClient.disconnect();
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {
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

        String timeout = timeOut.getText().toString();
        wearSetting.setWearTimeOutTimeOut(timeout.isEmpty() ? DEFAULT_WEAR_TIMEOUT : Integer.parseInt(timeout));
        int updateResult = DomoUtils.updateObjet(context, wearSetting);
        if (updateResult == -1) {
            return false;
        }
        Toast.makeText(getContext(), getContext().getResources().getString(R.string.save_box), Toast.LENGTH_SHORT).show();
        return true;
    }
}
