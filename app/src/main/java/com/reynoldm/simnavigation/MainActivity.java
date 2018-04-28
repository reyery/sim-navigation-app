package com.reynoldm.simnavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements DirectoryFragment.onItemClickListener, TimeTableFragment.onClassClickListener{

    private static final String TAG = "MainFragment";
    private static JSONArray json;
    private static ArrayList<String[]> ics;
    private static SharedPreferences pref;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_timetable:
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

                case R.id.navigation_search:
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

            }
            return false;
        }
    };


    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences("Pref", 0);

        new DownloadICS(this).execute(getURLPref());

        // Loads venue json into an ArrayList
        parseJSON();

        // Starts application at Home page
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_map);

        Intent intent = getIntent();
        if("com.reynoldm.simnavigation.NAVIGATE".equals(intent.getAction())) {


            final String location = intent.getStringExtra("location");
            Log.e(TAG, intent.getAction()+" location "+location);

            // Needs to wait for fragment to load
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    onDestSelected(location);
                }
            }, 1000);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater(); menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_icon:
                final Fragment settingsFragment = getSupportFragmentManager().findFragmentByTag("settings");
                if (settingsFragment == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragframe, new SettingsFragment(), "settings")
                            .addToBackStack(null)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragframe, settingsFragment)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item); }
    }

    public void onDestSelected(String venue) {
        LatLng dest = null;
        int floor = 0;

        try {
            for(int i=0;i<json.length();i++){
                JSONObject jsonObject1=json.getJSONObject(i);
                String name =jsonObject1.getString("name");
                if(name.equals(venue)) {
                    double lat = Double.parseDouble(jsonObject1.getString("lat"));
                    double lon = Double.parseDouble(jsonObject1.getString("long"));
                    floor = Integer.parseInt(jsonObject1.getString("floor"));
                    dest = new LatLng(lat, lon);

                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_map);

        NavMapFragment mapFragment = (NavMapFragment) getSupportFragmentManager().findFragmentByTag("navmap");
        mapFragment.receiveDest(dest,floor);
    }

    public void parseJSON() {
        try {
            Resources res = getResources();
            int resourceIdentifier = res.getIdentifier("venue", "raw", this.getPackageName());
            InputStream is = res.openRawResource(resourceIdentifier);

            byte[] b = new byte[is.available()];
            is.read(b);
            is.close();
            String input = new String(b);

            JSONObject jsonObject=new JSONObject(input);
            json =jsonObject.getJSONArray("venues");

        } catch (IOException e) {
            Log.e(TAG, "Could not find venue from raw resources folder");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getURLPref() {
        return pref.getString("URL", null);
    }

    public static void setURLPref(String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("URL", value);
        editor.apply();
    }

    public static long getDurationPref() {
        return pref.getLong("Duration", 0);
    }

    public static void setDurationPref(long value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong("Duration", value);
        editor.apply();
    }

    public static int getSelectionPref() {
        return pref.getInt("Selection", 0);
    }

    public static void setSelectionPref(int value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("Selection", value);
        editor.apply();
    }

    public static ArrayList<String[]> getICS() {
        return ics;
    }

    public static void setICS(ArrayList<String[]> in) {
        ics = in;
    }

    public static JSONArray getJSON() {
        return json;
    }

}
