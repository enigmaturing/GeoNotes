package android.and06.geonotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class GatherActivity extends Activity {

    private final int MIN_TIME_IN_BACKGROUND = 300000; //Minimum time between two sets of gps-positions (in ms) when the view is in background
    private int minTime = 5000; //Minimum time between two sets of gps-positions (in ms)
    private int minDistance = 5; //Minimum distance between two sets of gps-positions (in m)
    private final NoteLocationListener locationListener = new NoteLocationListener();
    private GeoNotesDatabaseHelper dbHelper = null;
    private  GeoNotesDatabaseHelper.Project currentProject;
    private GeoNotesDatabaseHelper.Note currentNote = null;  //this is the Note that was taken for the last time
    private Location lastLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather);
        //Get a list of strings with the available GPS-Providers, making use of an object of type LocationManager
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(false);
        //Log the list of providers
        for (String provider : providers) {
            Log.d(getClass().getSimpleName(), "AVAILABLE PROVIDER: " + provider);
        }
        //Show the list of providers on the spinner
        Spinner spinner = findViewById(R.id.spinnerProviders);
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
        //Initialize the instance of the class GeoNotesDatabaseHelper that we have declared as private field above
        if (dbHelper == null) dbHelper = new GeoNotesDatabaseHelper(this);
        //Initialize the project name when starting the app with the actual date and time
        currentProject = new GeoNotesDatabaseHelper.Project();
        //Show project name on the textview with id actual_project (this is done thanks to the object currentProject of the inner class GenoTesDatabaseHelper.Project)
        ((TextView) findViewById(R.id.actual_project)).setText(getString(R.string.actual_project) + currentProject.toString());
    }

    // The method onCreateOptionsMenu(Menu menu) inflates the menu to select rad, pkw or pkw-fern.
    // It adds items to the action bar, like they were defined on the layout menu_gather.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gather, menu);
        return true;
    }

    // The mehtod onOptionsItemSelected(MenuItem item) defines what to do when pressing an item
    // of the menu of this activity (GatherActivity). The layout of that menu was defined in
    // the menu_gather.xml
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item_quiet:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 60000;
                minDistance = 10;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt.\nBitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_foot:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 9000;
                minDistance = 10;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt.\nBitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_bicycle:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 4000;
                minDistance = 25;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt.\nBitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_car:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 4000;
                minDistance = 50;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt.\nBitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.item_car_fast:
                // ATTENTION!! The method onOptionsItemSelected DOES NOT sets a mark on the selected
                // radiobutton. We have to do it manually with the method setChecked(true);
                item.setChecked(true);
                minTime = 4000;
                minDistance = 100;
                Toast.makeText(this, "Neues GPS-Intervall ausgewählt.\nBitte Lokalisierung neu starten.", Toast.LENGTH_LONG).show();
                break;
            case R.id.menu_edit_project:
                openEditProjectDialog();
                break;
            case R.id.menu_choose_project:
                openChooseProjectDialog();
                break;
            default:
                break;
        }

        //no inspection simplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This method is triggered when the user presses the option button "choose project"
    private void openChooseProjectDialog() {
        //read Projects from DB and save them in an ArrayList named projects
        final ArrayList<GeoNotesDatabaseHelper.Project> projects = dbHelper.getProjects();
        //continue to display the Project names on an alertDialog only if there are projects to show
        if (projects.size() == 0){
            Toast.makeText(this, R.string.no_project_saved_yet, Toast.LENGTH_LONG).show();
            return;
        }
        //define a builder for the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //display a title for the alert dialog
        builder.setTitle(R.string.choose_a_project);
        //save the descriptionname of all projects in an arraylist callled items
        ArrayList<String> items = new ArrayList<String>();
        for (GeoNotesDatabaseHelper.Project p : projects){
            items.add(p.toString());
        }
        //convert the projectNames from an ArrayList of Strings to an array of CharSequence with
        //the method toArray() of the class ArrayList<E> (see AND07D pag.47)
        CharSequence[] projectNames = items.toArray(new CharSequence[items.size()]);
        builder.setItems(projectNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int whichElement) {
                //because projects is an arrayList, it has a method called get(whichElement), see here:
                Log.d(GatherActivity.this.getClass().getSimpleName(), "Ausgewähltes Projekt: " + projects.get(whichElement));
                //get chosen project:
                currentProject = projects.get(whichElement);
                //edit TextView with the name of the recent selected project:
                TextView projectTitle = (TextView) GatherActivity.this.findViewById(R.id.actual_project);
                projectTitle.setText(GatherActivity.this.getString(R.string.actual_project) + currentProject.toString());
                //show last note from the selected project
                currentNote = dbHelper.getLastNote(currentProject);
                ((TextView)findViewById(R.id.subject)).setText(currentNote.getSubject());
                ((TextView)findViewById(R.id.note)).setText(currentNote.getNote());
            }
        });
        builder.show();
    }



    //This method is triggered when the user presses the option button "edit project"
    private void openEditProjectDialog() {
        Log.d(this.getClass().getSimpleName(), "Edit Project dialog would be opened here");
        //create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.project_title_alert_dialog);
        //set an specific layout for the AlertDialog
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_project, null);
        builder.setView(dialogView);
        //set text of the layout
        ((TextView) dialogView.findViewById(R.id.textview_dialog_editproject_id)).setText(currentProject.toString());
        ((TextView) dialogView.findViewById(R.id.edittext_dialog_editproject_description)).setText(currentProject.getDescription());
        //define actions of the buttons of the AlertDialog
        builder.setPositiveButton(R.string.project_name_change, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //change project name
                String description = ((TextView) dialogView.findViewById(R.id.edittext_dialog_editproject_description)).getText().toString().trim();
                currentProject.setDescription(description);
                //update name of the project in the textview "Aktuelles Project: "
                TextView projectTitle = (TextView) GatherActivity.this.findViewById(R.id.actual_project);
                projectTitle.setText(currentProject.toString());
                //update name of the project in the databank
                dbHelper.update(currentProject);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Because all we want to do here is to hide the AlertDiaslog, we do not have to implement nothing
                //for this button. Nevertheless, we have to include the setNegativeButton, otherwise the Cancel
                //button wouldn't be displayed.
            }
        });
        builder.show();
    }

    //When this activity is destroyed, we want to stop retrieving information from the gps, to save energy.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocationUpdates();
    }

    //When this activity goes to background, we want to stop retrieving information from the gps, to save energy.
    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        super.onPause();
        if (((ToggleButton) GatherActivity.this.findViewById(R.id.toggle_start)).isChecked() == true) {
            minTime = MIN_TIME_IN_BACKGROUND;
            removeLocationUpdates();
            requestLocationUpdates();
            Toast.makeText(GatherActivity.this, "Activity gerät im Hintergrund.\n" +
                    "Location Updates werden nun nur noch alle " + MIN_TIME_IN_BACKGROUND / 1000 + "Sek. empfangen.", Toast.LENGTH_LONG).show();
        }
    }

    //When showing this activity again, check if locationWasActivated and then activate the location again
    @Override
    protected void onResume() {
        super.onResume();
        if (((ToggleButton) GatherActivity.this.findViewById(R.id.toggle_start)).isChecked() == true) {
            requestLocationUpdates();
            Toast.makeText(GatherActivity.this, "Die Lokalisierung wurde wieder gestartet.\n" +
                    "Location Updates werden nun alle " + minTime / 1000 + "Sek. empfangen", Toast.LENGTH_LONG).show();
        }
    }

    //When this activity is stopped, we want to stop retrieving information from the gps, to save energy.
    @Override
    protected void onStop() {
        super.onStop();
        removeLocationUpdates();
        Toast.makeText(GatherActivity.this, "Die Lokalisierung wurde beendet.", Toast.LENGTH_LONG).show();
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
            requestLocationUpdates();
            Log.d(getClass().getSimpleName(), "Lokalisierung gestartet");
        } else {
            removeLocationUpdates();
            Log.d(getClass().getSimpleName(), "Lokalisierung beendet");
        }
    }

    public void onButtonPreviousNoteClick(View view) {
        //Get the previous note to the actual one and show its content on the TextView
        GeoNotesDatabaseHelper.Note previousNote = null;
        if (currentNote == null){
            previousNote = dbHelper.getLastNote(currentProject);
        }else{
            previousNote = dbHelper.getPreviousNote(currentNote);
        }
        if (previousNote == null) return;

        currentNote = previousNote;

        //Show subject and note on the corresponding textviews of the GatherActivity
        ((TextView) findViewById(R.id.subject)).setText(currentNote.getSubject());
        ((TextView) findViewById(R.id.note)).setText(currentNote.getNote());
    }

    public void onButtonNextNoteClick(View view) {
        //Get the next note to the actual one and show its content on the TextView
        if (currentNote == null) return;
        GeoNotesDatabaseHelper.Note nextNote = dbHelper.getNextNote(currentNote);
        if (nextNote == null){
            currentNote = null;
            ((TextView) findViewById(R.id.note)).setText("");
            ((TextView) findViewById(R.id.subject)).setText("");
        }else{
            currentNote = nextNote;
            ((TextView) findViewById(R.id.note)).setText(currentNote.getNote());
            ((TextView) findViewById(R.id.subject)).setText(currentNote.getSubject());
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
                removeLocationUpdates();
                requestLocationUpdates();
                //Show information of the selected provider
                LocationManager locationManager = (LocationManager) GatherActivity.this.getSystemService(LOCATION_SERVICE);
                Log.i(getClass().getSimpleName(), "Provider changed by the user to: " +showProperties(locationManager, getProvider()));
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
        lastLocation = getLastKnownLocation();
        if (lastLocation != null) {
            //Define an intent and pass the following data: position, subject and note.
            Intent intent = new Intent(this, NoteMapActivity.class);
            intent.putExtra("location", new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
            intent.putExtra("subject", (((TextView) findViewById(R.id.subject)).getText().toString()));
            intent.putExtra("note", (((TextView) findViewById(R.id.note)).getText()).toString());
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_actual_position, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocation(){
        String provider = getProvider();
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        lastLocation = locationManager.getLastKnownLocation(provider);
        return lastLocation;
    }

    // This method triggers when pressing the button "Notiz speichern"
    public void onSaveNoteButtonClick(View view) {
        //checking if subject and note are not empty strings. If empty, exit this method
        String subject = ((TextView) findViewById(R.id.subject)).getText().toString().trim();
        String note = ((TextView) findViewById(R.id.note)).getText().toString().trim();
        if (subject.length() == 0 || note.length() == 0){
            Toast.makeText(this, R.string.empty_field_not_allowed, Toast.LENGTH_LONG).show();
            return;
        }
        //checking if there is an active note
        if (currentNote == null){
            //if not, get the last known location and save the project, the location and the note.
            Location lastLocation = getLastKnownLocation();
            if (lastLocation == null) {
                Toast.makeText(this, R.string.no_knownLastPosition_available, Toast.LENGTH_LONG).show();
                return;
            }
            //Projekt speichern
            //dbHelper.insert("Projects", currentProject.getContentValues());   //<- we called the insert method this way before we had the entity class Project extending the class "Entity" (see AND07D S.33 Auf.2.2)
            dbHelper.insert(currentProject);
            //Location speichern
            GeoNotesDatabaseHelper.Location location = new GeoNotesDatabaseHelper.Location(lastLocation.getLatitude(),
                    lastLocation.getLongitude(),
                    (int) lastLocation.getAltitude(),
                    lastLocation.getProvider());
            //dbHelper.insert("Locations", location.getContentValues());  //<- we called the insert method this way before we had the entity-class Location extending the class "Entity" (see AND07D S.33 Auf.2.2)
            dbHelper.insert(location);
            //Notiz speichern
            currentNote = new GeoNotesDatabaseHelper.Note(currentProject.id,
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude(),
                    subject,
                    note);
            //dbHelper.insert("Notes", currentNote.getContentValues());   //<- we called the insert method this way before we had the entity-class Note extending the class "Entity" (see AND07D S.33 Auf.2.2)
            dbHelper.insert(currentNote);
            Toast.makeText(this, R.string.note_sucessfully_created, Toast.LENGTH_LONG).show();
            Log.d(getClass().getSimpleName(), "Neue notiz angelegt und in DB gespeichert.");
        }else{
            //if there is an active note, just update note and subject for the already given location of the existing note
            currentNote.setSubject(subject);
            currentNote.setNote(note);
            //dbHelper.update("Notes", currentNote.getContentValues());   //<- we called the insert method this way before we had the entity-class Note extending the class "Entity" (see AND07D S.33 Auf.2.2)
            dbHelper.update(currentNote);
            Toast.makeText(this, R.string.note_sucessfully_edited, Toast.LENGTH_LONG).show();
            Log.d(getClass().getSimpleName(), "Notiz existiert bereits in DB, die Notiz wurde aktualisiert.");
        }
        //After having saved the note, show an alert dialog asking if the updated note has to be once more edited.
        //Having this AlertDialog allows the user to keep on adding editions to the last notiz, as long as the user
        //presses "yes"
        //When the user presses "no" ,the app understands that he wants to make a new note and not edit the last
        //one any longer
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_note);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //In case the user pressed "no", delete strings subject and note and set the current Note to null,
                //preparing the user to grab a new position for the next note
                ((TextView) GatherActivity.this.findViewById(R.id.subject)).setText("");
                ((TextView) GatherActivity.this.findViewById(R.id.note)).setText("");
                //set the field "currentNote" of the external class GatherActivity to null, so that a new
                //notize will be created for the next time
                GatherActivity.this.currentNote = null;
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //In case the user pressed "yes", do nothing, because the already existing note was
                //already edited. The field "currentNote" keeps refering to the just edited note,
                //so that the app keeps on updating its subject and thema the next time that the user
                //presses "notiz speichern"
            }
        });
        builder.show();
    }

    private String getProvider() {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerProviders);
        return(String)spinner.getSelectedItem();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates(){
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(getProvider(), minTime, minDistance, locationListener);
    }

    @SuppressLint("MissingPermission")
    private void removeLocationUpdates(){
        LocationManager locationManager = (LocationManager) GatherActivity.this.getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

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
}
