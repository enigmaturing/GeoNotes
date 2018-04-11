package android.and06.geonotes;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
            String title = extras.getString("subject");
            String snippet = extras.getString("note");
            MarkerOptions options = new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .anchor(0.5f,0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.crosshair));
            googleMap.addMarker(options);
            // Initialize a CameraPosition object in order to be able to show the map. This can
            // be done in two ways:
            // 1st: Specifying position, zoom, tilt and bearing. In that case we use the constructor
            //      of the class CameraPosition:
            CameraPosition cameraPosition = new CameraPosition(position, 10.0f, 0.0f,0.0f);
            // 2nd: Using the factory method fromLatLngZoom() of the class CameraPosition.Builder.
            // In that case there is no use of a constructor (it is a factory method that
            // automatically returns an object of the class CamerPosition). In this case it is only
            // possible to specify position and zoom (this second way is commented. Uncomment it
            // and comment the first way to go that way.
            //CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(position, 10.0f);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.moveCamera(update);
            //Set the marker on the googleMap with the layout inflated in
            // the innner-class MarkerInfoWindow (AND06 S.64)
            googleMap.setInfoWindowAdapter(new MarkerInfoWindow());
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

    //This inner class implements the interface GoogleMap.InfoWindowAdapter in order for us to inflate
    //te popup_window_layout.xml and be able to set this layout for the marker on the map and get a two lines
    //disposition for the marker (AND06 S.63)
    class MarkerInfoWindow implements GoogleMap.InfoWindowAdapter{
        @Override
        public View getInfoWindow(Marker marker) {
            LayoutInflater inflater = NoteMapActivity.this.getLayoutInflater();
            View popupWindow = inflater.inflate(R.layout.popup_window_layout,null);
            TextView title = (TextView) popupWindow.findViewById(R.id.textview_popup_title);
            title.setText(marker.getTitle());
            TextView snippet = (TextView) popupWindow.findViewById(R.id.textview_popup_snippet);
            snippet.setText(marker.getSnippet());
            return popupWindow;
        }
        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

}




