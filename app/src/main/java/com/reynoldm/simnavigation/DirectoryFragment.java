package com.reynoldm.simnavigation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectoryFragment extends Fragment {
    onItemClickListener mCallback;

    private ArrayAdapter adapter;

    public interface onItemClickListener {
        void onDestSelected(LatLng dest, int floor);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity =(Activity) context;

        try {
            mCallback = (onItemClickListener) activity;
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

        return inflater.inflate(R.layout.fragment_directory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView list = (ListView) getView().findViewById(R.id.directorylist);
        EditText theFilter = (EditText) getView().findViewById(R.id.searchFilter);

        ArrayList<String> names = generateNames();

        adapter = new ArrayAdapter(getContext(), R.layout.list_item_layout, names);
        list.setAdapter(adapter);

        theFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = (String) list.getItemAtPosition(position);
                double[] out = MainActivity.getLatLongF(value);
                Double f = out[2];
                int floor = f.intValue();
//                Toast.makeText(getContext(),"Lat: "+out[0]+" Long: "+out[1]+" Floor: "+floor,Toast.LENGTH_LONG).show();
                LatLng dest = new LatLng(out[0],out[1]);
                mCallback.onDestSelected(dest,floor);
            }
        });

    }

    public ArrayList<String> generateNames() {
        ArrayList<String> load = new ArrayList<>();

        try{
            JSONArray jsonArray=MainActivity.getJSON();

            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1=jsonArray.getJSONObject(i);
                String name =jsonObject1.getString("name");
                load.add(name);
            }

        }catch (JSONException e){e.printStackTrace();}

        return load;
    }

}
