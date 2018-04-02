package android.and06.geonotes;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.MapView;

public class NoteMapActivity extends Activity {
    // TODO 1: The behaviour of the toggle-button with the id "toggle_start" has to be implemented.
    // TODO 2: The behaviour of the button "Standort anzeigen" has to be implemented.

    // onCreate just shows the mapview as defined by activity_note_map.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_map);
        ((MapView)findViewById(R.id.mapview)).onCreate(savedInstanceState);
        //test the method decimalToSexagesimal:
        System.out.println(decimalToSexagesimal(52.514366, 13.350141));
    }
    @Override
    protected void onResume() {
        super.onResume();
        ((MapView)findViewById(R.id.mapview)).onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        ((MapView)findViewById(R.id.mapview)).onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MapView)findViewById(R.id.mapview)).onDestroy();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ((MapView)findViewById(R.id.mapview)).onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory() {
        ((MapView)findViewById(R.id.mapview)).onLowMemory();
    }

    // decimalToSexagesimal takes two double values (decimalLongitude and decimalLatitude)
    // and returns a string that represents its segaxesimal coordinate conversion.
    // EXAMPLE: decimalToSexagesimal(52.514366, 13.350141) returns -> 52° 30' 51,718'' O / 13° 21' 0,508'' N
    public String decimalToSexagesimal(double decimalLongitude, double decimalLatitude){
        // Compute degrees, minutes and seconds for the longitude value:
        String NOSWlongitude = (decimalLongitude >= 0 ? "O":"W");
        decimalLongitude = Math.abs(decimalLongitude);
        int degreesLongitude = (int) decimalLongitude;
        int minutesLongitude = (int) ((decimalLongitude - degreesLongitude) * 60);
        double secondsLongitude = (((decimalLongitude - degreesLongitude) * 60 ) - minutesLongitude) * 60;

        // Compute degrees, minutes and seconds for the latitude value:
        String NOSWlatitude = (decimalLatitude >= 0 ? "N":"S");
        decimalLatitude = Math.abs(decimalLatitude);
        int degreesLatitude = (int) Math.abs(decimalLatitude);
        int minutesLatitude = (int) ((decimalLatitude - degreesLatitude) * 60);
        double secondsLatitude = (((decimalLatitude - degreesLatitude) * 60 ) - minutesLatitude) * 60;

        return degreesLongitude + "° " + minutesLongitude + "' " +
                String.format("%.3f", secondsLongitude) + "''" + NOSWlongitude + " / " +
                degreesLatitude + "° " + minutesLatitude + "' " +
                String.format("%.3f", secondsLatitude) + "''" + NOSWlatitude;
    }
}




