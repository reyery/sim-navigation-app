package com.reynoldm.simnavigation;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsFragment extends Fragment {
    private static String url;
    private Button button;
    private EditText text;
    private TextView warning;
    private Spinner spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button = getView().findViewById(R.id.download);
        text = getView().findViewById(R.id.timetableurl);
        warning = getView().findViewById(R.id.invalid_url);
        spinner = getView().findViewById(R.id.duration);

        text.setText(MainActivity.getURLPref());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.duration, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(MainActivity.getSelectionPref());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        MainActivity.setDurationPref(0);
                        break;
                    case 1:
                        // 5 min
                        MainActivity.setDurationPref(300000);
                        break;
                    case 2:
                        // 15 min
                        MainActivity.setDurationPref(900000);
                        break;
                    case 3:
                        // 30 min
                        MainActivity.setDurationPref(1800000);
                        break;
                    case 4:
                        // 1 hour
                        MainActivity.setDurationPref(3600000);
                        break;
                }
                MainActivity.setSelectionPref(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                url = text.getText().toString();
                new checkURL().execute(url);
            }
        });
    }

    private class checkURL extends AsyncTask<String, Void, Boolean>  {
        @Override
        protected void onPreExecute() {
            warning.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String url = params[0];

                if(url.length()>10) {
                    HttpURLConnection.setFollowRedirects(false);
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setRequestMethod("HEAD");
                    return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
                } else {
                    return false;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean exist) {
            if(exist) {
                MainActivity.setURLPref(url);
                new DownloadICS(getActivity()).execute(MainActivity.getURLPref());
                Toast.makeText(getContext(), "iCalendar file downloaded", Toast.LENGTH_LONG).show();
            } else {
                warning.setVisibility(View.VISIBLE);
            }
        }
    }


}
