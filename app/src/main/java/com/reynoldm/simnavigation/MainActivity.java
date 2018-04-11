package com.reynoldm.simnavigation;

import android.content.res.Resources;
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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DirectoryFragment.onItemClickListener, TimeTableFragment.onClassClickListener{

    private static final String TAG = "MainFragment";
    private static JSONArray json;
    private static ArrayList<String[]> ics;
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

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loads venue json into an ArrayList
        parseJSON();

        // Loads ics
        parseICS();

        // Starts application at Home page
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragframe, new NavMapFragment(), "navmap")
                .addToBackStack(null)
                .commit();
        navigation.setSelectedItemId(R.id.navigation_map);
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

    public static JSONArray getJSON() {
        return json;
    }

    public static ArrayList<String[]> getICS() {
        return ics;
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

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
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

    public void parseICS() {
        ArrayList<String[]> tmp = new ArrayList<String[]>();
        CalendarBuilder builder = new CalendarBuilder();
        SimpleDateFormat dfdate0 = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        SimpleDateFormat dfdate = new SimpleDateFormat("dd/MM");
        SimpleDateFormat dftime = new SimpleDateFormat("h:mm aa");

        try {
            Resources res = getResources();
            int resourceIdentifier = res.getIdentifier("sample", "raw", this.getPackageName());
            InputStream is = res.openRawResource(resourceIdentifier);
            Calendar calendar = builder.build(is);

            for(Iterator i =calendar.getComponents().iterator(); i.hasNext();) {
                Component component = (Component) i.next();

                PropertyList properties = component.getProperties();
                String[] property = properties.toString().split("\\n");

                if (property.length>3) {
                    String summary = property[2].split(":")[1];

                    String dtstart = property[3].split(":")[1];
                    String dtend = property[4].split(":")[1];
                    java.util.Date datestart = dfdate0.parse(dtstart);
                    java.util.Date dateend = dfdate0.parse(dtend);

                    String date = dfdate.format(datestart);
                    String start = dftime.format(datestart);
                    String end = dftime.format(dateend);

                    String[] location0 = property[11].split(":")[1].split("\\s");
                    String location = location0[location0.length-1];

                    tmp.add(new String[] {date,summary,start+" - "+end,location});
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        ics = tmp;

    }

}
