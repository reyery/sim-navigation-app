package com.reynoldm.simnavigation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TimeTableFragment extends Fragment {
    onClassClickListener mCallback;

    public interface onClassClickListener {
        void onDestSelected(String venue);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity =(Activity) context;

        try {
            mCallback = (TimeTableFragment.onClassClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(MainActivity.getICS()!=null) {
            return inflater.inflate(R.layout.fragment_timetable, container, false);
        } else {
            return inflater.inflate(R.layout.empty_timetable, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<String[]> timetable = MainActivity.getICS();
        if(timetable!=null) {
            inflateTimetable(timetable);
        }
    }

    public void inflateTimetable( ArrayList<String[]> timetable) {
        // Get today's date
        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM");
//        String today = df.format(d);

        // For testing purposes
        String today = "1/04";

        final ListView resultsListView = getView().findViewById(R.id.timetablelist);

        final List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(getContext(), listItems, R.layout.list_subitem_layout,
                new String[]{"Today","Date","Summary", "Time","Location"},
                new int[]{R.id.bolddate, R.id.date, R.id.summary, R.id.duration, R.id.location});

        final ArrayList<Integer> todayClasses = new ArrayList<>();
        boolean notoday = true;
        boolean once = true;
        boolean last = false;
        int todayindex = 0;
        for(int i=0;i<timetable.size();i++) {
            HashMap<String, String> item = new HashMap<>();
            String date = timetable.get(i)[0];

            if(date.equals(today)) {
                notoday = false;
                todayClasses.add(i);
                item.put("Today", "Today");
            } else {
                item.put("Today", "");
            }

            if(notoday) {
                if(once) {
                    try {
                        Date dtoday = df.parse(today);
                        Date ddate = df.parse(date);

                        if (ddate.after(dtoday)) {
                            HashMap<String, String> item1 = new HashMap<>();
                            item1.put("Today", "Today");
                            item1.put("Date", today);
                            item1.put("Summary", "Nothing Today");
                            item1.put("Time", "");
                            item1.put("Location", "");
                            listItems.add(item1);



                            todayindex = i;
                            once = false;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            item.put("Date", date);
            item.put("Summary", timetable.get(i)[1]);
            item.put("Time", timetable.get(i)[2]);
            item.put("Location", timetable.get(i)[3]);
            listItems.add(item);

            if((i==(timetable.size()-1))&&once) {
                HashMap<String, String> item1 = new HashMap<>();
                item1.put("Today", "Today");
                item1.put("Date", today);
                item1.put("Summary", "No more entries");
                item1.put("Time", "");
                item1.put("Location", "");
                listItems.add(item1);
                last = true;
            }
        }

        resultsListView.setAdapter(adapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> hm = (HashMap) resultsListView.getItemAtPosition(position);
                String value = hm.get("Location");
                mCallback.onDestSelected(value);
            }
        });

        int index;
        if(!notoday) {
            index = todayClasses.get(0);
        } else {
            if(last) {
                index = timetable.size();
            } else {
                index = todayindex;
            }
        }

        final int go = index;
        resultsListView.setSelection(go);

        FloatingActionButton fab = getView().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultsListView.setSelection(go);
            }
        });
    }

}
