package com.polurival.cockroachswitcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Polurival
 * on 03.04.2017.
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;

    /**
     * Build url. Example: http://172.16.0.200/?b=1
     *
     * @param context    ApplicationContext
     * @param queryValue http request param value
     * @return URL
     */
    public static URL buildUrl(Context context, String queryValue) {

        final String scheme = context.getString(R.string.scheme_default);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String ip = sharedPreferences.getString(context.getString(R.string.pref_ip_key), context.getString(R.string.pref_ip_default));
        final String port = sharedPreferences.getString(context.getString(R.string.pref_port_key), context.getString(R.string.pref_port_default));

        String authority = ip + ":" + port;
        String queryKey = context.getString(R.string.query_key_default);

        Uri builtUri = new Uri.Builder()
                .scheme(scheme)
                .encodedAuthority(authority)
                .appendQueryParameter(queryKey, queryValue)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Log.i(TAG, url.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, "buildUrl() Bad request url");
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
