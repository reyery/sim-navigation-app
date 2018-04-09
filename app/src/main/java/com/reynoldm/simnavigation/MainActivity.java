package com.reynoldm.simnavigation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DirectoryFragment.onItemClickListener{

    private static final String TAG = "MainFragment";
    private static String json;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    final Fragment homeFragment = getSupportFragmentManager().findFragmentByTag("timetable");
                    if (homeFragment == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragframe, new TimeTableFragment(), "timetable")
                                .addToBackStack(null)
                                .commit();
                    } else {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragframe, homeFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    return true;

                case R.id.navigation_map:
                    final Fragment mapFragment = getSupportFragmentManager().findFragmentByTag("navmap");
                    if (mapFragment == null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragframe, new NavMapFragment(), "navmap")
                                .addToBackStack(null)
                                .commit();
                    } else {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragframe, mapFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    return true;

            }
            return false;
        }
    };


    public void onBackPressed() {
        //TODO add read from backstack then back
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loads venue json into an ArrayList
        ArrayList<String> load = new ArrayList<>();
        json = null;

        try {
            Resources res = getResources();
            int resourceIdentifier = res.getIdentifier("venue", "raw", this.getPackageName());
            InputStream is = res.openRawResource(resourceIdentifier);

            byte[] b = new byte[is.available()];
            is.read(b);
            is.close();

            json = new String(b);

        } catch (Exception e) {
            Log.e(TAG, "Could not find venue.json from raw resources folder");
        }

        // Starts application at Home page
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragframe, new TimeTableFragment(), "timetable")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater(); menuInflater.inflate(R.menu.searchable, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_icon:
                final Fragment directoryFragment = getSupportFragmentManager().findFragmentByTag("directory");
                if (directoryFragment == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragframe, new DirectoryFragment(), "directory")
                            .addToBackStack(null)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragframe, directoryFragment)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item); }
    }

    public static String getJson() {
        return json;
    }

    public static double[] getLatLongF(String venue) {
        double[] latlongf = new double[3];
        try{
            JSONObject jsonObject=new JSONObject(json);
            JSONArray jsonArray=jsonObject.getJSONArray("venues");

            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1=jsonArray.getJSONObject(i);
                String name =jsonObject1.getString("name");
                if(name.equals(venue)) {
                    String lat =jsonObject1.getString("lat");
                    latlongf[0] =Double.parseDouble(lat);
                    String lon =jsonObject1.getString("long");
                    latlongf[1] =Double.parseDouble(lon);
                    String floor =jsonObject1.getString("floor");
                    latlongf[2] =Double.parseDouble(floor);
                }
            }

        }catch (JSONException e){e.printStackTrace();}

        return latlongf;
    }

    public void onDestSelected(LatLng dest, int floor) {
        NavMapFragment mapFragment = (NavMapFragment) getSupportFragmentManager().findFragmentByTag("navmap");
        if (mapFragment == null) {
            mapFragment = new NavMapFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragframe, mapFragment, "navmap")
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragframe, mapFragment)
                    .addToBackStack(null)
                    .commit();
        }
        mapFragment.receiveDest(dest,floor);
    }

}
