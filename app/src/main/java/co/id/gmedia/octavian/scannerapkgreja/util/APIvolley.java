package co.id.gmedia.octavian.scannerapkgreja.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;


import co.id.gmedia.octavian.scannerapkgreja.R;

import static javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier;
import static javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory;

public class APIvolley {

    public static RequestQueue requestQueue;
    String token0 = "", token1 = "", token2 = "", token3 = "",token4 = "", token5 = "";

    public APIvolley(final Context context, JSONObject jsonBody, String requestMethod, final String REST_URL, final VolleyCallback callback){


    /*
    NOTE: you have to customize this class before you use it (haeder, etc)
    context : Application context
    jsonBody : jsonBody (usually be used for POST and PUT)
    requestMethod : GET, POST, PUT, DELETE
    REST_URL : Rest API URL
    successDialog : custom Dialog when success call API
    failDialog : custom Dialog when failed call API
    showDialogFlag : 1 = show successDialog / failDialog with filter
    callback : return of the response
    */

        final String requestBody = jsonBody.toString();

        int method = 0;

        switch(requestMethod.toUpperCase()){

            case "GET" :
                method = Request.Method.GET;
                break;
            case "POST" :
                method = Request.Method.POST;
                break;
            case "PUT" :
                method = Request.Method.PUT;
                break;
            case "DELETE" :
                method = Request.Method.DELETE;
                break;
            default: method = Request.Method.GET;

                break;
        }

        token3 = AppSharedPreferences.getUid(context);

        //region initial of stringRequest
        trustAllCertivicate();
        StringRequest stringRequest = new StringRequest(method, REST_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("ApiVolly", REST_URL+" : "+response);
                // Important Note : need to use try catch when parsing JSONObject, no need when parsing string

                if(response == null || response.equals("null")) {

                    Toast.makeText(context, context.getResources().getString(R.string.api_unauthorized), Toast.LENGTH_LONG).show();
                    callback.onError(context.getResources().getString(R.string.api_unauthorized));
                }

                try {

                    callback.onSuccess(response);

                } catch (Exception e) {

                    e.printStackTrace();
//                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
//                    Toast.makeText(context, context.getResources().getString(R.string.api_error), Toast.LENGTH_LONG).show();
                    callback.onError(e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override

            public void onErrorResponse(VolleyError error) {
                Log.d("ApiVolly", REST_URL+" : "+error.toString());
                callback.onError(error.toString());
                return;
            }
        }) {

            // Request Header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Client-Service", "front_end_client");
                params.put("Auth-Key", "gmedia_church");
                params.put("User-id", token3);
                params.put("Token", AppSharedPreferences.getToken(context));
                /*params.put("Userid", AppSharedPreferences.getUserName(context));
                params.put("Token", AppSharedPreferences.getToken(context));*/
                return params;
            }

            @Override
            public String getBodyContentType() {
                return String.format("application/json; charset=utf-8");
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }
        };
        //endregion

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        // retry when timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                20*5000,-1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
        requestQueue.getCache().clear();

    }

    // interface for call callback from response API
    public interface VolleyCallback{
        void onSuccess(String result);
        void onError(String result);
    }

    public void ShowCustomDialog(Context context, int flag, String message){
        if(flag == 1){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private void trustAllCertivicate() {
        try {
            setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    if (hostname.equalsIgnoreCase( "indonesiatraveloc.com") ||
                            hostname.equalsIgnoreCase("talam.gmedia.net.id") ||
                            hostname.equalsIgnoreCase("ipv4-10-27-68.as55666.net") ||
                            hostname.equalsIgnoreCase("gmedia.bz") ||
                            hostname.equalsIgnoreCase("kartikaelectric.com") ||
                            hostname.equalsIgnoreCase("http://gmedia.bz") ||
                            hostname.equalsIgnoreCase("app.midtrans.com") ||
                            hostname.equalsIgnoreCase("office.putmasaripratama.co.id") ||
                            hostname.equalsIgnoreCase("api.sandbox.midtrans.com") ||
                            hostname.equalsIgnoreCase("api.crashlytics.com") ||
                            hostname.equalsIgnoreCase("reports.crashlytics.com") ||
                            hostname.equalsIgnoreCase("settings.crashlytics.com") ||
                            hostname.equalsIgnoreCase("clients4.google.com") ||
                            hostname.equalsIgnoreCase("www.facebook.com") ||
                            hostname.equalsIgnoreCase("www.icon_instagram.com") ||
                            hostname.equalsIgnoreCase("lh1.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh2.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh3.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh4.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh5.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh6.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh7.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh8.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("lh9.googleusercontent.com") ||
                            hostname.equalsIgnoreCase("graph.facebook.com") ||
                            hostname.equalsIgnoreCase("googleusercontent.com") ||
                            hostname.equalsIgnoreCase("scontent.xx.fbcdn.net") ||
                            hostname.equalsIgnoreCase("lookaside.facebook.com")) {
                        return true;
                    } else {
                        return false;
                    }
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
