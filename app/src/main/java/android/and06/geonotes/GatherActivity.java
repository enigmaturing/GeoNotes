package android.and06.geonotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.util.List;

public class GatherActivity extends Activity {

    private final NoteLocationListener locationListener = new NoteLocationListener();

    class NoteLocationListener implements LocationListener {
        // Do not forget to activate for this app the access to GPS Position on the mobile phone,
        // after installing it in emulator or the real device. In order to do that, install the app
        // and then go to Settings (Ajustes) -> Permissions (Permisos) -> Permissions for apps
        // (Permisos de aplicaciones) -> Y alli activar ubicacion para esta applicacion
        @Override
        public void onLocationChanged(Location location) {
            TextView textView = (TextView) GatherActivity.this.findViewById(R.id.textview_output);
            textView.setText(textView.getText().toString() + "\n" + location.toString());
            Log.d(GatherActivity.this.getClass().getSimpleName(), "Empfangene Geodaten:\n" + location.toString());
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather);
        //Get a list of strings with the available GPS-Providers, making use of an object of type LocationManager
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(false);
        //Log the list of providers
        for (String provider:providers){
            Log.d(getClass().getSimpleName(), "AVAILABLE PROVIDER: " + provider);
        }
        //Show the list of providers on the spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinnerProviders);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, providers);
        spinner.setAdapter(adapter);
    }

    //When the app is destroyed, we want to stop retrieving information from the gps.
    @Override
    protected void onDestroy(){
        super.onDestroy();
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    // The method onToggleButtonClick is triggered with the toggleButton with id toggle_start (see
    // activity_note_map.xml)
    // If toggle is pressed, setting it to active:
    //      -With the help of a locationManager, use its method "requestLocationUpdates" to set a
    //       locationListener that offers GPS-Coordinates.
    // If toggle is pressed, setting it to inactive:
    //      -With the help of a locationManager, use its method "removeUpdates" to unset the
    //       locationListener that offered GPS-Coordinates.
    // The interface android.Location.LocationListener has to be implemented by a class.
    // Only so it is possible to instantiate a locationListener object.
    @SuppressLint("MissingPermission")
    public void onToggleButtonClick(View view) {
        if (((ToggleButton) view).isChecked()) {
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            Log.d(getClass().getSimpleName(), "Lokalisierung gestartet");
        }else{
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationManager.removeUpdates(locationListener);
            Log.d(getClass().getSimpleName(), "Lokalisierung beendet");
        }
    }
}
