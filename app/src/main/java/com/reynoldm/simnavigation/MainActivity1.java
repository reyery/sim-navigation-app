package com.reynoldm.simnavigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity1 extends AppCompatActivity {

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

//                case R.id.navigation_directory:
//                    final Fragment directoryFragment = getSupportFragmentManager().findFragmentByTag("directory");
//                    if (directoryFragment == null) {
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.fragframe, new DirectoryFragment(), "directory")
//                                .addToBackStack(null)
//                                .commit();
//                    } else {
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.fragframe, directoryFragment)
//                                .addToBackStack(null)
//                                .commit();
//                    }
//                    return true;
            }
            return false;
        }
    };


    public void onBackPressed(){
        //TODO add read from backstack then back
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
//                final Fragment directoryFragment = getSupportFragmentManager().findFragmentByTag("directory");
//                if (directoryFragment == null) {
//                    getSupportFragmentManager().beginTransaction()
//                            .add(R.id.fragframe, new DirectoryFragment(), "directory")
//                            .commit();
//                }
//
//                BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//                navigation.setSelectedItemId(R.id.navigation_directory);

                return true;
            default:
                return super.onOptionsItemSelected(item); }
    }
}
