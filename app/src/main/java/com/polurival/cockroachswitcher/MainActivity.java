package com.polurival.cockroachswitcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int GET_REQUEST_LOADER_ID = 38;

    private String mGetResponse;

    private ProgressBar mLoadingIndicator;
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    private Button mButton4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLoadingIndicator();

        mButton1 = (Button) findViewById(R.id.button_one);
        mButton2 = (Button) findViewById(R.id.button_two);
        mButton3 = (Button) findViewById(R.id.button_three);
        mButton4 = (Button) findViewById(R.id.button_four);

        if (savedInstanceState != null) {
            boolean isLoadingStarted = savedInstanceState.getBoolean(getString(R.string.is_loading_bundle_key));
            if (isLoadingStarted) {
                showLoadingIndicatorAndHideButtons();
            }
        }

        getSupportLoaderManager().initLoader(GET_REQUEST_LOADER_ID, null, this);

        checkForUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
        Tracking.startUsage(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
        Tracking.stopUsage(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        boolean isLoadingStarted = getSupportLoaderManager().getLoader(GET_REQUEST_LOADER_ID).isStarted();
        outState.putBoolean(getString(R.string.is_loading_bundle_key), isLoadingStarted);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }
                Log.i(TAG, "onCreateLoader() with url");

                mGetResponse = null; // сбрасываем кэш;
                showLoadingIndicatorAndHideButtons();

                if (mGetResponse != null) {
                    deliverResult(mGetResponse);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                String queryUrlString = args.getString(getString(R.string.get_request_query_key));
                if (queryUrlString == null || TextUtils.isEmpty(queryUrlString)) {
                    return null;
                }
                Log.i(TAG, "loadInBackground() " + queryUrlString);

                try {
                    URL url = new URL(queryUrlString);
                    return NetworkUtils.getResponseFromHttpUrl(url);
                } catch (IOException e) {
                    Log.e(TAG, "getResponseFromHttpUrl() error");
                    return null;
                }
            }

            @Override
            public void deliverResult(String getResponse) {
                Log.i(TAG, "deliverResult()");
                mGetResponse = getResponse;
                super.deliverResult(getResponse);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String response) {
        Log.i(TAG, "onLoadFinished() " + response);
        hideLoadingIndicatorAndShowButtons();

        if (response == null) {
            Snackbar.make(findViewById(R.id.activity_main), getString(R.string.connection_error), Snackbar.LENGTH_LONG)
                    .show();
        } else {
            // TODO: 03.04.2017 show/send response
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.i(TAG, "onLoaderReset()");
        // do nothing
    }

    private void initLoadingIndicator() {
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mLoadingIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLoadingIndicatorAndShowButtons();

                final boolean isLoaderCanceled = getSupportLoaderManager().getLoader(GET_REQUEST_LOADER_ID).cancelLoad();
                Log.e(TAG, "Loader is canceled: " + isLoaderCanceled);
            }
        });
    }

    private void showLoadingIndicatorAndHideButtons() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        turnButtons(false);
    }

    private void hideLoadingIndicatorAndShowButtons() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        turnButtons(true);
    }

    public void onClick(View v) {
        String queryValue = null;
        switch (v.getId()) {
            case R.id.button_one:
                queryValue = getString(R.string.button_one_text);
                break;
            case R.id.button_two:
                queryValue = getString(R.string.button_two_text);
                break;
            case R.id.button_three:
                queryValue = getString(R.string.button_three_text);
                break;
            case R.id.button_four:
                queryValue = getString(R.string.button_four_text);
                break;
        }
        makeGetQuery(queryValue);
    }

    private void makeGetQuery(String queryValue) {
        URL queryUrl = NetworkUtils.buildUrl(getApplicationContext(), queryValue);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(getString(R.string.get_request_query_key), queryUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> getRequestLoader = loaderManager.getLoader(GET_REQUEST_LOADER_ID);
        if (getRequestLoader == null) {
            loaderManager.initLoader(GET_REQUEST_LOADER_ID, queryBundle, this);
        } else {
            loaderManager.restartLoader(GET_REQUEST_LOADER_ID, queryBundle, this);
        }
    }

    private void turnButtons(boolean isOn) {
        mButton1.setEnabled(isOn);
        mButton2.setEnabled(isOn);
        mButton3.setEnabled(isOn);
        mButton4.setEnabled(isOn);
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }
}
