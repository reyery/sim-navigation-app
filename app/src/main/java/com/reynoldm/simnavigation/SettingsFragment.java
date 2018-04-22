package com.reynoldm.simnavigation;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
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

        final Button button = getView().findViewById(R.id.button_id);
        final EditText text = getView().findViewById(R.id.timetableurl);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = text.getText().toString();
                Toast.makeText(getContext(),url, Toast.LENGTH_LONG).show();
            }
        });
    }
}
