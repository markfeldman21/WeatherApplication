package com.markfeldman.sunshine.Activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.markfeldman.sunshine.Fragments.ForecastFragment;
import com.markfeldman.sunshine.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState==null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sunshine_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), MySettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.map_location){
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }
     private void openPreferredLocationInMap(){
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));

         Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                 .appendQueryParameter("q",location)
                 .build();

         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setData(geoLocation);

         if (i.resolveActivity(getPackageManager())!=null){
             startActivity(i);
         }else{
             Log.v("Main", "An Error Occured");
         }

     }

}