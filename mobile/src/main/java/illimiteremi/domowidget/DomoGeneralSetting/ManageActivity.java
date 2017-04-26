package illimiteremi.domowidget.DomoGeneralSetting;

import android.Manifest;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import illimiteremi.domowidget.DomoGeneralSetting.Fragments.BoxSettingFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.IconSettingFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WearSettingFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WebViewFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetExportFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetLocationFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetMultiFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetPushFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetStateFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetToogleFragment;
import illimiteremi.domowidget.DomoGeneralSetting.Fragments.WidgetVocalFragment;
import illimiteremi.domowidget.DomoUtils.FileExplorerActivity;
import illimiteremi.domowidget.DomoWidgetBdd.DomoBaseSQLite;
import illimiteremi.domowidget.DomoWidgetBdd.UtilsDomoWidget;
import illimiteremi.domowidget.R;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION_LABEL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.MULTI_LABEL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PERMISSION_OK;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PUSH_LABEL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.STATE_LABEL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.TOOGLE_LABEL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.URL_PAYPAL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.URL_WORDPRESS;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.VOCAL_LABEL;

public class ManageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String   TAG      = "[DOMO_MAIN_FRAGMENT]";

    private Context               context;
    private DrawerLayout          drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();

        // Init du drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Vérification sur la création / update de la BDD
        new CheckDataBase().execute();

    }

    /**
     * CheckDataBase
     */
    private class CheckDataBase extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ManageActivity.this);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.setMessage(context.getResources().getString(R.string.bdd_update));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Connection à la BDD
            DomoBaseSQLite domoBase = new DomoBaseSQLite(getApplicationContext(), UtilsDomoWidget.NOM_BDD, null, UtilsDomoWidget.VERSION_BDD);
            domoBase.getWritableDatabase();
            domoBase.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            // Verification des permissions
            checkPermission();
            // Information de l'intent de création d'un widget
            String intentAction = getIntent().getAction();
            Bundle extras       = getIntent().getExtras();
            Fragment myFragment = null;

            if (intentAction.contentEquals("android.appwidget.action.APPWIDGET_CONFIGURE")){
                int idWidget = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                AppWidgetProviderInfo infos = appWidgetManager.getAppWidgetInfo(idWidget);
                Log.d(TAG, "EXTRA_APPWIDGET_ID = " + idWidget);
                // Selection du fragment suivant le type du widget
                //noinspection deprecation
                switch(infos.label) {
                    case LOCATION_LABEL:
                        myFragment = new WidgetLocationFragment();
                        break;
                    case STATE_LABEL:
                        myFragment = new WidgetStateFragment();
                        break;
                    case PUSH_LABEL:
                        myFragment = new WidgetPushFragment();
                        break;
                    case TOOGLE_LABEL:
                        myFragment = new WidgetToogleFragment();
                        break;
                    case MULTI_LABEL:
                        myFragment = new WidgetMultiFragment();
                        break;
                    case VOCAL_LABEL:
                        myFragment = new WidgetVocalFragment();
                        break;
                    default:
                        break;
                }

                // Affichage du fragment
                if (myFragment != null) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    myFragment.setArguments(extras);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.main_fragment, myFragment).commit();
                }
            } else {
                myFragment = new BoxSettingFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.main_fragment, myFragment).commit();
                toggle.syncState();
                drawer.openDrawer(GravityCompat.START);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment myFragment = null;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        Bundle extras = new Bundle();

        // Selection des menus
        switch (id){
            case R.id.nav_manage:
                myFragment = new BoxSettingFragment();
                break;
            case R.id.nav_upload_pic:
                myFragment = new IconSettingFragment();
                break;
            case R.id.action:
                myFragment = new WidgetToogleFragment();
                break;
            case R.id.push:
                myFragment = new WidgetPushFragment();
                break;
            case R.id.mutli:
                myFragment = new WidgetMultiFragment();
                break;
            case R.id.info:
                myFragment = new WidgetStateFragment();
                break;
            case R.id.gps:
                myFragment = new WidgetLocationFragment();
                break;
            case R.id.vocal:
                myFragment = new WidgetVocalFragment();
                break;
            case R.id.wear:
                myFragment = new WearSettingFragment();
                break;
            case R.id.exporter:
                myFragment = new WidgetExportFragment();
                break;
            case R.id.importer:
                Intent intent = new Intent(this, FileExplorerActivity.class);
                intent.setAction("IMPORT_DOMO_WIDGET");
                startActivity(intent);
                drawer.openDrawer(GravityCompat.START);
                break;
            case R.id.paypal:
                myFragment = new WebViewFragment();
                extras.putString("URL", URL_PAYPAL);
                extras.putString("TITLE", getString(R.string.paypal));
                break;
            case R.id.forum:
                myFragment = new WebViewFragment();
                extras.putString("URL", URL_WORDPRESS);
                extras.putString("TITLE", getString(R.string.tutoriel));
                break;
        }

        // Affichage du fragment
        if (myFragment != null) {
            myFragment.setArguments(extras);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_fragment, myFragment).commit();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_OK: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission accordée !");
                } else {
                    Log.d(TAG, "Permission non accordée !");
                }
            }
        }
    }

    /**
     * checkPermission
     */
    private void checkPermission() {
        // Vérification des permissions
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);


        for (String permission : permissionsNeeded) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Permission deja refusé par l'utilisateur
                ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), PERMISSION_OK);
            }
        }
    }
}
