package co.id.gmedia.octavian.scannerapkgreja.util;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;


public class Server {

    public final static String TAG = "tiket_scanner_log";

    private final static String BASE_URL = "http://gmedia.bz/gereja/api/";
    public final static String URL_LOGIN = BASE_URL + "auth/login_scanner";
    public final static String URL_SCAN = BASE_URL + "ticket/scan";
    public final static String URL_LIST_JADWAL = BASE_URL + "jadwal/list_jadwal_scanner";



    //Kunai
    public static final String EXTRA_ID = "ID";

    public static Map<String, String> getTokenHeader(Context context){
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Auth-Key", "gmedia_church");
        headers.put("Client-Service", "front_end_client");
        headers.put("id_scanner", AppSharedPreferences.getUid(context));
        headers.put("token", AppSharedPreferences.getToken(context));
        return headers;
    }

}