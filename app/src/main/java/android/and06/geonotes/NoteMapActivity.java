package android.and06.geonotes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NoteMapActivity extends Activity implements OnMapReadyCallback {
    // onCreate just shows the mapview as defined by activity_note_map.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_map);
        MapView mapView = ((MapView)findViewById(R.id.mapview));
        mapView.onCreate(savedInstanceState);
        //The method getMapAsync of the mapView-obejct makes it possible or the callback-object
        // to be triggered when the map is ready to be used.
        mapView.getMapAsync(this);
        //Test the method decimalToSexagesimal:
        System.out.println(decimalToSexagesimal(52.514366, 13.350141));
    }
    //onMapReady evaluates the GPS-Position contained in the intent created by GatherActivity and shows
    //it on the map as a marker
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            //Get the actual position form the intent and set it as a marker on the map
            LatLng position = (LatLng) extras.getParcelable("location");
            MarkerOptions options = new MarkerOptions()
                    .position(position)
                    .title("Mein Standort")
                    .snippet("Dies ist ein Infotext")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.crosshair));
            googleMap.addMarker(options);
            //Center the map on the marker and set the zoom-level to 10.0f
            CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(position, 10.0f);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.moveCamera(update);
        }else{
            Toast.makeText(this, R.string.no_actual_position, Toast.LENGTH_SHORT).show();
        }
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




