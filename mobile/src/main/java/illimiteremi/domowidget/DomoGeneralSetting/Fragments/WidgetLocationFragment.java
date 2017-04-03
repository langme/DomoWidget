package illimiteremi.domowidget.DomoGeneralSetting.Fragments;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import illimiteremi.domowidget.DomoAdapter.BoxAdapter;
import illimiteremi.domowidget.DomoAdapter.WidgetAdapter;
import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoUtils.DomoConstants;
import illimiteremi.domowidget.DomoUtils.DomoUtils;
import illimiteremi.domowidget.DomoWidgetLocation.LocationWidget;
import illimiteremi.domowidget.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.BOX;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NEW_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NO_WIDGET;
import static illimiteremi.domowidget.DomoUtils.DomoUtils.hideKeyboard;

public class WidgetLocationFragment extends Fragment {

    private static final String   TAG                       =  "[DOMO_GPS_SEETING]";

    private Context               context;                  // Context

    private int                   newIdWidget;              // ID du widget à créer
    private boolean               isConfigured;             // Widget configuré

    private AutoCompleteTextView  name;                     // Nom du Widget
    private AutoCompleteTextView  action;                   // Action domotique GPS
    private AutoCompleteTextView  timeOut;                  // TimeOut maj position
    private AutoCompleteTextView  distance;                 // Distance maj position
    private Spinner               spinnerBox;               // Spinner liste des box
    private Spinner               providerSpinner;          // Spinner GPS
    private Spinner               spinnerWidgets;           // Spinner de la liste des widgets
    private LinearLayout          linearLayoutWidget;       // Layout de la configuration du widget

    private BoxSetting            selectedBox;              // Box domotique utilisé par le widget
    private LocationWidget        widget;                   // Widget Location
    private WidgetAdapter         widgetAdapter;            // Adapter de la liste des widgets
    private ArrayAdapter<String>  providerAdapter;          // Adapter pour le fournisseur GPS
    private MenuItem              deleteAction;             // MenuItem Delete

    @Override
    public void onResume() {
        super.onResume();
        if (newIdWidget == 0) {
            // Rechargement des spinners
            loadSpinner();
            // Rechargement des menus
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Annulation du widget et suppression de la bdd
        if (!isConfigured && newIdWidget != 0) {
            Log.d(TAG, "Widget non enregistré");
            DomoUtils.removeObjet(context, widget);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newIdWidget);
            getActivity().setResult(RESULT_CANCELED);
            getActivity().finish();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save_settings, menu);
        MenuItem saveAction   = menu.findItem(R.id.save_action);
        deleteAction = menu.findItem(R.id.delete_action);

        if (newIdWidget != 0) {
            // Check si nouveau widget
            linearLayoutWidget.setVisibility(View.VISIBLE);
            LocationWidget emptyWidget = new LocationWidget(context, NEW_WIDGET);
            emptyWidget.setDomoName(context.getResources().getString(R.string.new_widget));
            widgetAdapter.insert(emptyWidget, 0);
            widgetAdapter.notifyDataSetChanged();
            saveAction.setVisible(true);
            deleteAction.setVisible(false);
            spinnerWidgets.performClick();
        } else {
            // Check si aucun widget
            if (widgetAdapter.getCount() == 0) {
                linearLayoutWidget.setVisibility(View.INVISIBLE);
                spinnerWidgets.setEnabled(false);
                saveAction.setVisible(false);
                deleteAction.setVisible(false);
                LocationWidget noWidget = new LocationWidget(context, NO_WIDGET);
                noWidget.setDomoName(context.getResources().getString(R.string.no_widget));
                widgetAdapter.add(noWidget);
            } else {
                // Affichage du layout
                widget = (LocationWidget) spinnerWidgets.getSelectedItem();
                linearLayoutWidget.setVisibility(View.VISIBLE);
                saveAction.setVisible(true);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideKeyboard(getActivity());
        switch (item.getItemId()) {
            case R.id.save_action:
                Log.d(TAG, "Mise à jour du widget " + widget.getDomoName());
                backupWidgetData();
                break;
            case R.id.delete_action:
                Log.d(TAG, "Suppression du widget en bdd : " + widget.getDomoName());
                DomoUtils.removeObjet(context, widget);
                widget = null;
                // Refresh current fragment
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(this).attach(this).commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        // Maj du titre
        getActivity().setTitle(getResources().getString(R.string.fragment_gps));

        // Récuperation de l'id Widget
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Création d'un nouveau Widget GPS
            newIdWidget = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            isConfigured = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_setting, container, false);
        setHasOptionsMenu(true);

        name               = (AutoCompleteTextView) view.findViewById(R.id.editName);
        action             = (AutoCompleteTextView) view.findViewById(R.id.editEtat);
        timeOut            = (AutoCompleteTextView) view.findViewById(R.id.editTimeOut);
        distance           = (AutoCompleteTextView) view.findViewById(R.id.editDistance);
        providerSpinner    = (Spinner) view.findViewById(R.id.spinnerProvider);
        spinnerBox         = (Spinner) view.findViewById(R.id.spinnerBox);
        spinnerWidgets     = (Spinner) view.findViewById(R.id.spinnerWidgets);
        linearLayoutWidget = (LinearLayout) view.findViewById(R.id.linearWidget);

        // Chargement des spinners
        loadSpinner();

        // Listener de la liste des widgets
        spinnerWidgets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                widget = (LocationWidget) adapterView.getAdapter().getItem(i);
                // Chargement des informations du Widget
                if (widget != null) {
                    try {
                        name.setText(widget.getDomoName());
                        action.setText(widget.getDomoAction());
                        timeOut.setText(String.valueOf(widget.getDomoTimeOut()));
                        distance.setText(String.valueOf(widget.getDomoDistance()));
                        selectedBox = widget.getSelectedBox();
                        providerSpinner.setSelection(providerAdapter.getPosition(widget.getDomoProvider()));

                        // Selection du spinner box associé au widget
                        if (selectedBox != null) {
                            int spinnerPostion = DomoUtils.getSpinnerPosition(context, selectedBox);
                            spinnerBox.setSelection(spinnerPostion);
                        } else {
                            spinnerBox.setSelection(spinnerBox.getAdapter().getCount()-1);
                        }
                        // Check si widget present sur le bureau
                        if (widget.getPresent()) {
                            deleteAction.setVisible(false);
                        } else {
                            deleteAction.setVisible(true);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Erreur " + e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Listener de la liste des box
        spinnerBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBox = (BoxSetting) adapterView.getAdapter().getItem(i);
                if (selectedBox.getBoxId() != 0) {
                    widget.setDomoBox(selectedBox.getBoxId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    /**
     * Chargement des Spinners
     */
    private void loadSpinner()  {
        // Chargement du spinner de la liste des widgets
        widgetAdapter = (WidgetAdapter) DomoUtils.createAdapter(context, LOCATION);
        spinnerWidgets.setAdapter(widgetAdapter);

        // Chargement du spinner Box
        BoxAdapter boxAdapter = (BoxAdapter) DomoUtils.createAdapter(context, BOX);
        spinnerBox.setAdapter(boxAdapter);

        // Chargement du spinner Provider
        providerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, DomoConstants.PROVIDER_TYPE.toList());
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        providerSpinner.setAdapter(providerAdapter);
    }

    /**
     * Mise à jour du widget dans la bdd
     */
    private void backupWidgetData() {

        widget.setDomoName(name.getText().toString());
        widget.setDomoAction(action.getText().toString());
        widget.setDomoDistance(Integer.valueOf(distance.getText().toString()));
        widget.setDomoTimeOut(Integer.valueOf(timeOut.getText().toString()));
        widget.setDomoProvider(providerSpinner.getSelectedItem().toString());
        selectedBox = (BoxSetting) spinnerBox.getSelectedItem();
        widget.setDomoBox(selectedBox.getBoxId());

        // Si nouveau widget
        if (newIdWidget != 0) {
            widget.setDomoId(newIdWidget);
            if (DomoUtils.insertObjet(context, widget) != -1) {
                isConfigured = true;
                Log.d(TAG, "Création du widget en BDD");
            }
            // RESULT OK
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newIdWidget);
            getActivity().setResult(RESULT_OK, resultValue);
            getActivity().finish();
        } else {
            // Update d'un widget
            DomoUtils.updateObjet(context, widget);
            // Message de sauvegarde
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.save_box), Toast.LENGTH_SHORT).show();
        }

        // Mise à jour des widgets LOCATION
        DomoUtils.startService(context, true);
    }

}
