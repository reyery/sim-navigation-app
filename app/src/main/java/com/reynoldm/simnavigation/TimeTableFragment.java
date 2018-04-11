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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TimeTableFragment extends Fragment {
    onClassClickListener mCallback;

    public interface onClassClickListener {
        public void onDestSelected(LatLng dest, int floor);
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
        return inflater.inflate(R.layout.fragment_timetable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get today's date
//        Date d = Calendar.getInstance().getTime();
//        SimpleDateFormat df = new SimpleDateFormat("dd/MM");
//        String today = df.format(d);

        String today = "13/04";

        final ListView resultsListView = (ListView) getView().findViewById(R.id.timetablelist);

        ArrayList<String[]> timetable = MainActivity.getICS();

        final List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(getContext(), listItems, R.layout.list_subitem_layout,
                new String[]{"Date","Summary", "Time","Location"},
                new int[]{R.id.date, R.id.summary, R.id.duration, R.id.location});

        final ArrayList<Integer> todayClasses = new ArrayList<Integer>();
        for(int i=0;i<timetable.size();i++) {
            HashMap<String, String> item = new HashMap<String, String>();
            String date = timetable.get(i)[0];

            if(date.equals(today)) {
                todayClasses.add(i);
            }

            item.put("Date", date);
            item.put("Summary", timetable.get(i)[1]);
            item.put("Time", timetable.get(i)[2]);
            item.put("Location", timetable.get(i)[3]);
            listItems.add(item);
        }

        resultsListView.setAdapter(adapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(todayClasses.contains(position)) {
                    HashMap<String, String> hm = (HashMap) resultsListView.getItemAtPosition(position);
                    String value = hm.get("Location");
                    double[] out = MainActivity.getLatLongF(value);
                    Double f = out[2];
                    int floor = f.intValue();
//                    Toast.makeText(getContext(), "Lat: " + out[0] + " Long: " + out[1] + " Floor: " + floor, Toast.LENGTH_LONG).show();
                    LatLng dest = new LatLng(out[0], out[1]);
                    mCallback.onDestSelected(dest, floor);
                }
            }
        });
        final int go = todayClasses.get(0);
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
