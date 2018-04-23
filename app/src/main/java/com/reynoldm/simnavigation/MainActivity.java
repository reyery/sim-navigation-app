package com.reynoldm.simnavigation;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.PropertyList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DirectoryFragment.onItemClickListener, TimeTableFragment.onClassClickListener{

    private static final String TAG = "MainFragment";
    private static JSONArray json;
    private static String icsinput;
    private static ArrayList<String[]> ics;
    private static SharedPreferences pref;
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
        new DownloadICS().execute(pref.getString("URL", null));

        // Loads venue json into an ArrayList
        parseJSON();

        // Starts application at Home page
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragframe, new NavMapFragment(), "navmap")
                .addToBackStack(null)
                .commit();
        navigation.setSelectedItemId(R.id.navigation_map);
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

    public static JSONArray getJSON() {
        return json;
    }

    public static ArrayList<String[]> getICS() {
        return ics;
    }

    public static void downloadICS() {
        new DownloadICS().execute(pref.getString("URL", null));
    }

    public static double[] getLatLongF(String venue) {
        double[] latlongf = new double[3];

        try {
            for(int i=0;i<json.length();i++){
                JSONObject jsonObject1=json.getJSONObject(i);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return latlongf;
    }

    public void onDestSelected(LatLng dest, int floor) {

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_map);

        NavMapFragment mapFragment = (NavMapFragment) getSupportFragmentManager().findFragmentByTag("navmap");
        mapFragment.receiveDest(dest,floor);
    }

    public String getResource(String filename) {
        try {
            Resources res = getResources();
            int resourceIdentifier = res.getIdentifier(filename, "raw", this.getPackageName());
            InputStream is = res.openRawResource(resourceIdentifier);

            byte[] b = new byte[is.available()];
            is.read(b);
            is.close();

            return new String(b);

        } catch (Exception e) {
            Log.e(TAG, "Could not find "+filename+" from raw resources folder");
        }

        return null;
    }

    public void parseJSON() {
        String input = getResource("venue");
        try {
            JSONObject jsonObject=new JSONObject(input);
            json =jsonObject.getJSONArray("venues");

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void parseICS() {
        ArrayList<String[]> tmp = new ArrayList<>();
        CalendarBuilder builder = new CalendarBuilder();
        SimpleDateFormat dfdate0 = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        SimpleDateFormat dfdate = new SimpleDateFormat("dd/MM");
        SimpleDateFormat dftime = new SimpleDateFormat("h:mm aa");

        try {
            // For testing with sample.ics
//            Resources res = getResources();
//            int resourceIdentifier = res.getIdentifier("sample", "raw", this.getPackageName());
//            InputStream is = res.openRawResource(resourceIdentifier);

            InputStream is = new ByteArrayInputStream(icsinput.getBytes());
            Calendar calendar = builder.build(is);

            for (Object o : calendar.getComponents()) {
                Component component = (Component) o;

                PropertyList properties = component.getProperties();
                String[] property = properties.toString().split("\\n");

                if (property.length > 3) {
                    String summary = property[2].split(":")[1];

                    String dtstart = property[3].split(":")[1];
                    String dtend = property[4].split(":")[1];
                    Date datestart = dfdate0.parse(dtstart);
                    Date dateend = dfdate0.parse(dtend);

                    String date = dfdate.format(datestart);
                    String start = dftime.format(datestart);
                    String end = dftime.format(dateend);

                    String[] location0 = property[11].split(":")[1].split("\\s");
                    String location = location0[location0.length - 1];

                    tmp.add(new String[]{date, summary, start + " - " + end, location});
                }
            }
            ics = tmp;

        } catch (Exception e) {
            e.printStackTrace();
            ics = null;
        }
    }

    private static class DownloadICS extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                if(params[0]==null) {
                    return null;
                }

                URL url = new URL(params[0]);
                URLConnection con = url.openConnection();
                con.connect();

                InputStream in = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                StringBuffer buffer = new StringBuffer();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }

                return buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            icsinput = result;
            parseICS();
        }
    }

    public static void storePref(String key, String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPref(String key) {
        return pref.getString(key, null);
    }

}
