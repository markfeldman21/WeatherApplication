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
import android.widget.Toast;

import com.markfeldman.sunshine.Activities.DetailActivity;
import com.markfeldman.sunshine.DataHelpers.ForecastAdapter;
import com.markfeldman.sunshine.DataHelpers.JsonParser;
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

    @Override
    public void onClicked(String clickedItemIndex) {
        Intent i = new Intent(getActivity(), DetailActivity.class).putExtra(INTENT_EXTRA,clickedItemIndex);
        startActivity(i);
    }


    public class RetrieveWeatherData extends AsyncTask<String,Void,String[]> {
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String forecastJsonStr;

        RetrieveWeatherData(){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String[] doInBackground(String... weatherArray) {
            final String API_KEY = "4ea939406639022ce1e875b256de6c8a";
            String id = "524901";
            String format = "json";
            String units = "metric";

            int numDays=7;
            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String ID_PARAM = "id";
                final String APP_ID = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,weatherArray[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .appendQueryParameter(ID_PARAM,id)
                        .appendQueryParameter(APP_ID, API_KEY)
                        .build();

                Log.d("t", "LOOK "+builtUri.toString());


                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = null;

                inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    Log.v("1", "nothing retrieved");
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                try {
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");//Helpful for debugging
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (buffer.length() == 0) {
                    Log.v("1", "nothing in bufferedString");
                }else{
                    forecastJsonStr = buffer.toString();
                    //USE JSON OBJECT
                    JsonParser jsonParser = new JsonParser(getActivity());
                    weatherArray = jsonParser.getWeatherDataFromJson(forecastJsonStr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return weatherArray;
        }

        @Override
        protected void onPostExecute(String[] strings) {

            super.onPostExecute(strings);
            if (strings!=null){
                forecastRecycleAdapter.setWeatherData(strings);
            }
        }
    }
}
