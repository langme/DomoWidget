package illimiteremi.domowidget.DomoUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.security.cert.CertificateException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import illimiteremi.domowidget.DomoGeneralSetting.BoxSetting;
import illimiteremi.domowidget.DomoWidgetLocation.LocationWidget;
import illimiteremi.domowidget.DomoWidgetLocation.WidgetLocationProvider;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidget;
import illimiteremi.domowidget.DomoWidgetMulti.MultiWidgetProvider;
import illimiteremi.domowidget.DomoWidgetPush.PushWidget;
import illimiteremi.domowidget.DomoWidgetPush.WidgetPushProvider;
import illimiteremi.domowidget.DomoWidgetState.StateWidget;
import illimiteremi.domowidget.DomoWidgetState.WidgetStateProvider;
import illimiteremi.domowidget.DomoWidgetToogle.ToogleWidget;
import illimiteremi.domowidget.DomoWidgetToogle.WidgetToogleProvider;
import illimiteremi.domowidget.DomoWidgetVocal.VocalWidget;
import illimiteremi.domowidget.DomoWidgetVocal.WidgetVocalProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static illimiteremi.domowidget.DomoUtils.DomoConstants.DONE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.ERROR;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.JEEDOM_URL;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.LOCATION;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.MULTI;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.NOK;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.OK;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.PUSH;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.STATE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.TOOGLE;
import static illimiteremi.domowidget.DomoUtils.DomoConstants.VOCAL;

/**
 * Created by rcouturi on 26/09/2016.
 */

public class DomoHttp {

    private static final String     TAG           = "[DOMO_HTTP]";

    private static Boolean          wifiIsOn      = false;

    private static class HttpRequest implements Callable<String> {

        final Context      context;
        final Object       object;
        final BoxSetting   box;
        final String       action;
        final String       expRegu;
        Request            request;
        Integer            timeOut;
        OkHttpClient       client;

        public HttpRequest(Context context, BoxSetting box, String action, String expRegu, Object object) {
            this.context = context;
            this.box     = box;
            this.action  = (action == null) ? "" : action;
            this.expRegu = (expRegu == null) ? "" : expRegu;
            this.object  = object;
        }

        @Override
        public String call() throws Exception {

            Response httpResponse;

            // Création de la requete http suivant le type de connexion
            if (wifiIsOn) {
                try {
                    // Url Interne (en Wifi)
                    timeOut = DomoConstants.WIFI_TIME_OUT;
                    request = new Request.Builder().url(box.getBoxUrlInterne() + JEEDOM_URL + box.getBoxKey() + action).build();
                } catch (Exception e) {
                    Log.e(TAG, "Erreur : " + e);
                    // Url Externe (si l'url n'est pas correct)
                    timeOut = DomoConstants.MOBILE_TIME_OUT;
                    request = new Request.Builder().url(box.getBoxUrlExterne() + JEEDOM_URL + box.getBoxKey() + action).build();
                }
            } else {
                // Url Externe
                timeOut = DomoConstants.MOBILE_TIME_OUT;
                request = new Request.Builder().url(box.getBoxUrlExterne() + JEEDOM_URL + box.getBoxKey() + action).build();
            }

            try {
                // Tentative d'execution
                Log.d(TAG, "Requête     = " + action);
                OkHttpClient client = getOkHttpClient(timeOut);
                httpResponse = client.newCall(request).execute();
            } catch (Exception e) {
                Log.e(TAG, "L'adresse ne répond pas !");
                try {
                    client = getOkHttpClient(DomoConstants.MOBILE_TIME_OUT);
                    request = new Request.Builder().url(box.getBoxUrlExterne() + JEEDOM_URL + box.getBoxKey() + action).build();
                    Log.d(TAG, "Nouvelle tentative...");
                    httpResponse = client.newCall(request).execute();
                } catch (Exception e1) {
                    sendNoData(context,  object);
                    Log.e(TAG, "L'adresse ne répond pas !");
                    return ERROR;
                }
            }

            try {
                String charResponse = httpResponse.body().string();
                int codeRetour = httpResponse.code();
                Log.d(TAG, "Code Retour = " + codeRetour);

                if (codeRetour == 200) {
                    // Si pas d'action specifique
                    if (action.isEmpty()) {
                        return DONE;
                    }
                    if (!charResponse.isEmpty()) {
                        Log.d(TAG, "Réponse     = " + charResponse);
                        if (!expRegu.isEmpty()) {
                            // Expression réguliere
                            Log.d(TAG, "Exp Réguli. = " + expRegu + " / " + charResponse + " => " + charResponse.matches(expRegu));
                            if (charResponse.matches(expRegu)) {
                                return OK;
                            } else {
                                return NOK;
                            }
                        } else {
                            // Resultat à afficher
                            if (charResponse.matches("error")) {
                                charResponse = ERROR;
                                sendNoData(context, object);
                            }
                            return charResponse;
                        }
                    } else {
                        // Pas de réponse Jeedom : Demande d'action
                        return DONE;
                    }
                } else {
                    // Pas de réponse http
                    return ERROR;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Erreur : " + e);
                return ERROR;
            }
        }
    }

    /**
     * Vérification de la connexion Wifi
     * @param context
     * @return
     */
    public static boolean checkWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                wifiIsOn = true;
                return true;
            }

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                wifiIsOn = false;
                return false;
            }
        }
        return false;
    }

    /**
     * sendNoData widget intent
     * @param context
     * @param object
     */
    private static void sendNoData(Context context,  Object object) {
        switch(object.getClass().getSimpleName()) {
            case TOOGLE:
                ToogleWidget toogleWidget = (ToogleWidget) object;
                DomoUtils.sendNoDataIntent(context, WidgetToogleProvider.class, toogleWidget.getDomoId());
            case STATE:
                StateWidget stateWidget = (StateWidget) object;
                DomoUtils.sendNoDataIntent(context, WidgetStateProvider.class, stateWidget.getDomoId());
            case PUSH:
                PushWidget pushWidget = (PushWidget) object;
                DomoUtils.sendNoDataIntent(context, WidgetPushProvider.class, pushWidget.getDomoId());
            case LOCATION:
                LocationWidget locationWidget = (LocationWidget) object;
                DomoUtils.sendNoDataIntent(context, WidgetLocationProvider.class, locationWidget.getDomoId());
            case MULTI:
                MultiWidget multiWidget = (MultiWidget) object;
                DomoUtils.sendNoDataIntent(context, MultiWidgetProvider.class, multiWidget.getDomoId());
            case VOCAL:
                VocalWidget vocalWidget = (VocalWidget) object;
                DomoUtils.sendNoDataIntent(context, WidgetVocalProvider.class, vocalWidget.getDomoId());
            default:
        }
    }

    /**
     * Creation d'un client okHttp (with self secure HTTPS)
     * @param connectTimeout
     * @return
     */
    private static OkHttpClient getOkHttpClient(int connectTimeout) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            builder.readTimeout(10000, TimeUnit.MILLISECONDS);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execution d'une requete HTTP / HTTPS
     * @param context
     * @param box
     * @param action
     * @param expRegu
     * @param widgetObj
     * @return
     */
    public static String httpRequest(Context context, BoxSetting box, String action, String expRegu, Object widgetObj) {

        ExecutorService service = Executors.newSingleThreadExecutor();
        HttpRequest httpRequest = new HttpRequest(context, box, action, expRegu, widgetObj);
        Future<String> future = service.submit(httpRequest);

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR;
        }
    }
}
