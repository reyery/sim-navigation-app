package com.reynoldm.simnavigation;

import android.app.Activity;
import android.os.AsyncTask;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.PropertyList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DownloadICS extends AsyncTask<String, Void, String> {
    private static String icsinput;
    private final WeakReference<Activity> weakActivity;

    DownloadICS (Activity myActivity){
        this.weakActivity = new WeakReference<>(myActivity);
    }

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

    private void parseICS() {
        ArrayList<String[]> tmp = new ArrayList<>();
        CalendarBuilder builder = new CalendarBuilder();
        SimpleDateFormat dfdate0 = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        dfdate0.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        SimpleDateFormat dfdate = new SimpleDateFormat("dd/MM");
        SimpleDateFormat dftime = new SimpleDateFormat("h:mm aa");
        dftime.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));

        Date today = java.util.Calendar.getInstance().getTime();

        Activity activity = weakActivity.get();
        NotificationHelper noti = new NotificationHelper(activity);
        Boolean first = true;
        int choice = MainActivity.getSelectionPref();

        try {

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

                    datestart.getTime();

                    String[] location0 = property[11].split(":")[1].split("\\s");
                    String location = location0[location0.length - 1];

                    if(choice!=0) {
                        if (datestart.after(today) && first) {
                            noti.scheduleNotification(noti.getNotification(summary, start + " - " + end, location), datestart.getTime(), MainActivity.getDurationPref());
                            first = false;
                        }
                    }

                    tmp.add(new String[]{date, summary, start + " - " + end, location});
                }
            }
            noti.scheduleNotification(noti.getNotification("Interaction Design", "8:30 AM - 10:30 AM", "A.1.14"), System.currentTimeMillis(), 5000);
            MainActivity.setICS(tmp);

        } catch (Exception e) {
            e.printStackTrace();
            MainActivity.setICS(null);
        }
    }
}

