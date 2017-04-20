package com.polurival.cockroachswitcher;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;

/**
 * Created by SBT-Polshchikov-YuV
 * on 20.04.2017.
 */

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    private static final String TAG = AppWidgetProvider.class.getSimpleName();

    private static final String ONE = "1";
    private static final String TWO = "2";
    private static final String THREE = "3";
    private static final String FOUR = "4";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_layout);
        ComponentName watchWidget = new ComponentName(context, AppWidgetProvider.class);

        remoteViews.setOnClickPendingIntent(R.id.button_one, getPendingSelfIntent(context, ONE));
        remoteViews.setOnClickPendingIntent(R.id.button_two, getPendingSelfIntent(context, TWO));
        remoteViews.setOnClickPendingIntent(R.id.button_three, getPendingSelfIntent(context, THREE));
        remoteViews.setOnClickPendingIntent(R.id.button_four, getPendingSelfIntent(context, FOUR));

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        switch (intent.getAction()) {
            case ONE:
                new WidgetAsyncTask(context).execute(ONE);
                break;
            case TWO:
                new WidgetAsyncTask(context).execute(TWO);
                break;
            case THREE:
                new WidgetAsyncTask(context).execute(THREE);
                break;
            case FOUR:
                new WidgetAsyncTask(context).execute(FOUR);
                break;
            default:
                break;
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private class WidgetAsyncTask extends AsyncTask<String, Void, String> {

        private Context mContext;

        private WidgetAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... queryValue) {
            URL queryUrl = NetworkUtils.buildUrl(mContext, queryValue[0]);
            try {
                return NetworkUtils.getResponseFromHttpUrl(queryUrl);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(mContext, mContext.getString(R.string.connection_error), Toast.LENGTH_SHORT)
                        .show();
            } else {
                Log.d(TAG, result);
            }
        }
    }
}
