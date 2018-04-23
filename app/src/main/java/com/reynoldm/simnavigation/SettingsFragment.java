package com.reynoldm.simnavigation;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsFragment extends Fragment {
    private static String url;
    private Button button;
    private EditText text;
    private TextView warning;


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

        text.setText(MainActivity.getPref("URL"));

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
                MainActivity.storePref("URL", url);
                MainActivity.downloadICS();
                Toast.makeText(getContext(), "iCalendar file downloaded", Toast.LENGTH_LONG).show();
            } else {
                warning.setVisibility(View.VISIBLE);
            }
        }
    }

}
