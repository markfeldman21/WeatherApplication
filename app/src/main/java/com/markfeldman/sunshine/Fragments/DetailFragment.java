package com.markfeldman.sunshine.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import com.markfeldman.sunshine.R;

public class DetailFragment extends Fragment {
    private final String INTENT_EXTRA = "Intent Extra";
    private TextView detailWeather;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private String forecastString;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detailfragment,menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (shareActionProvider!=null){
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }else{
            Log.d(LOG_TAG,"Share Action is Null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        detailWeather = (TextView)view.findViewById(R.id.detail_text);
        Intent i = getActivity().getIntent();
        if (i!=null && i.hasExtra(INTENT_EXTRA)){
            forecastString = i.getStringExtra(INTENT_EXTRA);
            detailWeather.setText(forecastString);
        }
        return view;
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastString + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

}
