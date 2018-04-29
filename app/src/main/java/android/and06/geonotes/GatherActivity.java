package android.and06.geonotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.maps.model.LatLng;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class GatherActivity extends Activity {

    private int minTime = 5000; //Minimum time between two sets of gps-positions (in ms)
    private int minDistance = 5; //Minimum distance between two sets of gps-positions (in m)
    private boolean locationWasActivated = false; //Variable to keep track of the activation of the location function
    private LocationManager locationManager;
    private String provider;
    private final NoteLocationListener locationListener = new NoteLocationListener();

    class NoteLocationListener implements LocationListener {
        // Do not forget to activate for this app the access to GPS Position on the mobile phone,
        // after installing it in emulator or the real device. In order to do that, install the app
        // and then go to Settings (Ajustes) -> Permissions (Permisos) -> Permissions for apps
        // (Permisos de aplicaciones) -> Y alli activar ubicacion para esta applicacion
        @Override
        public void onLocationChanged(Location location) {
            //TextView textView = (TextView) GatherActivity.this.findViewById(R.id.textview_output);
            //textView.setText(textView.getText().toString() + "\n" + location.toString());
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
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(false);
        //Log the list of providers
        for (String provider : providers) {
            Log.d(getClass().getSimpleName(), "AVAILABLE PROVIDER: " + provider);
        }
        //Show the list of providers on the spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinnerProviders);
        //Define an adapter with the array of strings "providers" and set it as the source for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, providers);
        spinner.setAdapter(adapter);
        //Check if gps is available as provider. In that case, select it as default value of the spinner
        if (providers.contains("gps") == true) {
            spinner.setSelection(providers.indexOf("gps"));
        }
        //Associate a listener of the class SpinnerProviderItemSelectedListener to the spinner
        //in order to be able to get the moment when a item of the spinner is selected
        spinner.setOnItemSelectedListener(new SpinnerProviderItemSelectedListener());
        //Show information of this provider
        Log.i(getClass().getSimpleName(), showProperties(locationManager, (spinner.getSelectedItem().toString())));
        //Initialize a DateFormat object to be able to format the date and the time to german
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,  DateFormat.MEDIUM, Locale.GERMAN);
        //Get date a time, format it into German form and save it as String in the variable date
        String dateAndTime = dateFormat.format(new java.util.Date());
        //Show project name on the textview with id actual_project
        TextView actualProjectTextView = (TextView) findViewById(R.id.actual_project);
        actualProjectTextView.setText(getString(R.string.actual_project) + dateAndTime);
    }

    // The method onCreateOptionsMenu(Menu menu) inflates the menu to select rad, pkw or pkw-fern.
    // It adds items to the action bar, like they were defined on the layout menu_gather.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_gather, menu);
        return true;
    }

    // The mehtod onOptionsItemSelected(MenuItem item) defines what to do when pressing an item
    // of the menu of this activity (GatherActivity). The layout of that menu was defined in
    // the menu_gather.xml
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.item_foot:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 9000;
                minDistance = 10;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt. Bitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_bicycle:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 4000;
                minDistance = 25;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt. Bitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_car:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 4000;
                minDistance = 50;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt. Bitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_car_fast:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 4000;
                minDistance = 100;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt. Bitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }



        //no inspection simplifiableIfStatement
        if (id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //When this activity is destroyed, we want to stop retrieving information from the gps, to save energy.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    //When this activity is paused, we want to stop retrieving information from the gps, to save energy.
    @Override
    protected void onPause(){
        super.onPause();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        if (((ToggleButton) GatherActivity.this.findViewById(R.id.toggle_start)).isChecked()) {
            locationWasActivated = true;
        }else{
            locationWasActivated = false;
        }
    }

    //When showing this activity again, check if locationWasActivated and then activate the location again
    @SuppressLint("MissingPermission")
    @Override
    protected void onResume(){
        super.onResume();
        if (locationWasActivated == true){
            locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);
            Toast.makeText(GatherActivity.this, "Die Lokalisierung wurde wieder gestartet", Toast.LENGTH_LONG).show();
        }
    }

    //When this activity is stopped, we want to stop retrieving information from the gps, to save energy.
    @Override
    protected void onStop(){
        super.onStop();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        if (((ToggleButton) GatherActivity.this.findViewById(R.id.toggle_start)).isChecked()) {
            locationWasActivated = true;
        }else{
            locationWasActivated = false;
        }
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
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (((ToggleButton) view).isChecked()) {
            Spinner spinner = (Spinner) findViewById(R.id.spinnerProviders);
            provider = (String) spinner.getSelectedItem();
            locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);
            Log.d(getClass().getSimpleName(), "Lokalisierung gestartet");
        } else {
            locationManager.removeUpdates(locationListener);
            Log.d(getClass().getSimpleName(), "Lokalisierung beendet");
        }
    }

    //The class SpinnerProviderItemSelectedListener implements the Interface OnItemSelectedListener,
    //and let us create listeners that can be associated to spinners on our activity, to notice when
    //an item of the spinner has been selected.
    @SuppressLint("MissingPermission")
    class SpinnerProviderItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        //When an item of the is selected, we need to stop the flow of gps data and set it again with the new provider
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (((ToggleButton) GatherActivity.this.findViewById(R.id.toggle_start)).isChecked()) {
                locationManager = (LocationManager) GatherActivity.this.getSystemService(LOCATION_SERVICE);
                locationManager.removeUpdates(locationListener);
                String provider = ((TextView) view).getText().toString();
                locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);
                Log.i(getClass().getSimpleName(), "Provider changed by the user to: " + provider);
                //Show information of the selected provider
                Log.i(getClass().getSimpleName(), showProperties(locationManager, provider));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Log.i(getClass().getSimpleName(), "No item of the spinner was selected");
        }
    }

    //This method returns information about the selected gps-provider
    String showProperties(LocationManager locationManager, String provider) {
        String accuracy = (locationManager.getProvider(provider).getAccuracy() == 1) ? "FINE" : "COARSE";
        return ("provider: " + provider + "\n" +
                "horizontale Genauigkeit: " + accuracy + "\n" +
                "unterstützt Höhenermittlung: " + (locationManager.getProvider(provider).supportsAltitude()) + "\n" +
                "erfordert Satellit: " + (locationManager.getProvider(provider).requiresSatellite()));
    }

    //This method starts a new intent pointing to the NoteMapActivity, passing in the intent
    //the actual position as a LatLang object
    public void onButtonShowPositionClick(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerProviders);
        String provider = (String) spinner.getSelectedItem();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location lastLocation = locationManager.getLastKnownLocation(provider);
        if(lastLocation != null){
            //Define an intent and pass the following data: position, subject and note.
            Intent intent = new Intent(this, NoteMapActivity.class);
            intent.putExtra("location", new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            intent.putExtra("subject", (((TextView)findViewById(R.id.subject)).getText().toString()));
            intent.putExtra("note", (((TextView)findViewById(R.id.note)).getText()).toString());
            startActivity(intent);
        }else{
            Toast.makeText(this, R.string.no_actual_position, Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveNoteButtonClick(View view){
        Toast.makeText(this, R.string.function_not_yet_implemented, Toast.LENGTH_SHORT).show();
    }
}
