package com.markfeldman.sunshine.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.markfeldman.sunshine.Activities.DetailActivity;
import com.markfeldman.sunshine.DataHelpers.ForecastAdapter;
import com.markfeldman.sunshine.Utilities.JsonParser;
import com.markfeldman.sunshine.Utilities.NetworkUtils;
import com.markfeldman.sunshine.DataHelpers.SunshinePreferences;
import com.markfeldman.sunshine.R;

import java.net.URL;

public class ForecastFragment extends Fragment implements ForecastAdapter.ClickedListener, LoaderManager.LoaderCallbacks<String[]>,
        SharedPreferences.OnSharedPreferenceChangeListener
{
    private RecyclerView recyclerView;
    private ForecastAdapter forecastRecycleAdapter;
    private final String INTENT_EXTRA = "Intent Extra";
    private TextView errorMessage;
    private final static int FORECAST_LOADER = 1;
    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private ProgressBar progressBar;
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    public ForecastFragment(){
        setHasOptionsMenu(true);
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

        int loaderID = FORECAST_LOADER;
        LoaderManager.LoaderCallbacks<String[]> callback = this;

        Bundle bundleForLoader = null;
        //The variable callback is passed to the call to initLoader below. This means that whenever the loaderManager has
        //something to notify us of, it will do so through this callback.
        getActivity().getSupportLoaderManager().initLoader(loaderID,bundleForLoader,callback);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            getActivity().getSupportLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }


    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<String[]>(getActivity()) {
            String []jsonResults = null;
            @Override
            protected void onStartLoading() {
                if(jsonResults!=null){
                    deliverResults(jsonResults);
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
                super.onStartLoading();
            }

            @Override
            public String[] loadInBackground() {
                String location = SunshinePreferences.getPreferredWeatherLocation(getActivity());
                URL weatherRequest = NetworkUtils.buildUrl(location);

                try {
                    String jsonRawResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequest);
                    JsonParser jsonParser = new JsonParser(getActivity());
                    String[] jsonArrayResponse = jsonParser.getWeatherDataFromJson(jsonRawResponse);
                    return jsonArrayResponse;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResults(String[] data){
                jsonResults = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        progressBar.setVisibility(View.INVISIBLE);
        forecastRecycleAdapter.setWeatherData(data);

        if (null==data){
            showErrorMessage();
        }else{
            showWeatherDataView();
        }

    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

    private void openPreferredLocationInMap(){
        String addressString = SunshinePreferences.getPreferredWeatherLocation(getActivity());
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!",
                    Toast.LENGTH_LONG).show();

        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCES_HAVE_BEEN_UPDATED = true;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.map_location){
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }
}
