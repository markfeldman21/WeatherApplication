package com.markfeldman.sunshine.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.markfeldman.sunshine.Activities.DetailActivity;
import com.markfeldman.sunshine.DataHelpers.ForecastAdapter;
import com.markfeldman.sunshine.DataHelpers.JsonParser;
import com.markfeldman.sunshine.DataHelpers.NetworkUtils;
import com.markfeldman.sunshine.DataHelpers.SunshinePreferences;
import com.markfeldman.sunshine.R;

import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForecastFragment extends Fragment implements ForecastAdapter.ClickedListener, LoaderManager.LoaderCallbacks<String>,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    private RecyclerView recyclerView;
    private ForecastAdapter forecastRecycleAdapter;
    private final String INTENT_EXTRA = "Intent Extra";
    private TextView errorMessage;
    private final static int SEARCH_LOADER = 22;
    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private ProgressBar progressBar;
    private String location;


    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        errorMessage = (TextView)view.findViewById(R.id.tv_error_message_display);
        progressBar = (ProgressBar)view.findViewById(R.id.pb_loading_indicator);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycleViewForecast);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        forecastRecycleAdapter = new ForecastAdapter(this);
        recyclerView.setAdapter(forecastRecycleAdapter);



        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);



        return view;
    }

    public void updateWeather(){
        location = SunshinePreferences.getPreferredWeatherLocation(getActivity());
        URL weatherRequest = NetworkUtils.buildUrl(location);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA,weatherRequest.toString());

        //CHECK IF LOADER EXISTS
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();

        Loader<String> githubSearchLoader = loaderManager.getLoader(SEARCH_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(SEARCH_LOADER, queryBundle, this);
        }
    }

    private void showErrorMessage(){
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);

    }

    private void showWeatherDataView() {
        errorMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClicked(String clickedItemIndex) {
        Intent i = new Intent(getActivity(), DetailActivity.class).putExtra(INTENT_EXTRA,clickedItemIndex);
        startActivity(i);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return  new AsyncTaskLoader<String>(getActivity()) {
            String mJsonResult;
            @Override
            protected void onStartLoading() {
                progressBar.setVisibility(View.VISIBLE);
                super.onStartLoading();
                if (args == null){
                    return;
                }
                if (mJsonResult != null){
                    deliverResult(mJsonResult);
                }else{
                    forceLoad();
                }

            }

            @Override
            public String loadInBackground() {
                String searchQueryURLString = args.getString(SEARCH_QUERY_URL_EXTRA);
                if (searchQueryURLString==null){
                    return null;
                }
                try {
                    URL githubUrl = new URL(searchQueryURLString);
                    String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(githubUrl);
                    return jsonWeatherResponse;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String data) {
                mJsonResult = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        String weatherArray[] = null;
        progressBar.setVisibility(View.INVISIBLE);
        if (data == null){
            showErrorMessage();
        }else{
            JsonParser jsonParser = new JsonParser(getActivity());
            try {
                weatherArray = jsonParser.getWeatherDataFromJson(data);
                showWeatherDataView();
                forecastRecycleAdapter.setWeatherData(weatherArray);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_key))){
            location = sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
