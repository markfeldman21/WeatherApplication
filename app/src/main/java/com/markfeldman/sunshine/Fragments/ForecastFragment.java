package com.markfeldman.sunshine.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.markfeldman.sunshine.Activities.DetailActivity;
import com.markfeldman.sunshine.DataHelpers.ForecastAdapter;
import com.markfeldman.sunshine.DataHelpers.JsonParser;
import com.markfeldman.sunshine.DataHelpers.NetworkUtils;
import com.markfeldman.sunshine.R;

import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForecastFragment extends Fragment implements ForecastAdapter.ClickedListener {
    private RecyclerView recyclerView;
    private ForecastAdapter forecastRecycleAdapter;
    private final String INTENT_EXTRA = "Intent Extra";
    private TextView errorMessage;


    public ForecastFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_refresh){
            updateWeather();
        }
        return super.onOptionsItemSelected(item);
    }

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

        recyclerView = (RecyclerView)view.findViewById(R.id.recycleViewForecast);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        forecastRecycleAdapter = new ForecastAdapter(this);
        recyclerView.setAdapter(forecastRecycleAdapter);
        return view;
    }

    public void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));

        new RetrieveWeatherData().execute(location);
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


    public class RetrieveWeatherData extends AsyncTask<String,Void,String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... weatherArray) {
            URL weatherRequest = NetworkUtils.buildUrl(weatherArray[0]);
            try {
                String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequest);
                JsonParser jsonParser = new JsonParser(getActivity());
                weatherArray = jsonParser.getWeatherDataFromJson(jsonWeatherResponse);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
                weatherArray = null;
            }
            return weatherArray;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings!=null){
                showWeatherDataView();
                forecastRecycleAdapter.setWeatherData(strings);
            }else {
                showErrorMessage();
            }
        }
    }
}
