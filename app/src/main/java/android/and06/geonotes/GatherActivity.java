package android.and06.geonotes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.ArrayList;
import java.util.List;

public class GatherActivity extends Activity {

    private final int MIN_TIME_IN_BACKGROUND = 300000; //Minimum time between two sets of gps-positions (in ms) when the view is in background
    private int minTime = 5000; //Minimum time between two sets of gps-positions (in ms)
    private int minDistance = 5; //Minimum distance between two sets of gps-positions (in m)
    private final NoteLocationListener locationListener = new NoteLocationListener();
    private GeoNotesDatabaseHelper dbHelper = null;
    private GeoNotesDatabaseHelper.Project currentProject;
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
        //AND08D S.18 Auf1.5:
        //We now check if savedInstance is not null. If it is null, it means we are creating the activity right
        //after it was fresh installed, and therefore we create a new Project and so on.
        //If savedInstance is not null, it means the activty is being created after having saved a status
        //of the activity by the overridden method onSaveInstanceState (for example, after a change of orientation)
        //In that case, we must not create a new project, but we have to get the last project that is contained
        //in the Bundle savedInstanceState.
        if (savedInstanceState == null) {
            //Initialize the project name when starting the app with the actual date and time, if there is no last project found
            currentProject = new GeoNotesDatabaseHelper.Project();
            //Get the id of the last project stored in sharedRefereces
            long lastProjectID = getSharedPreferences("preferences", MODE_PRIVATE).getLong("lastProjectID", -1);
            //Get the project corresponding to that id, depending on the value of the id
            final GeoNotesDatabaseHelper.Project retrievedProject = dbHelper.getProject(lastProjectID);
            //if the project with that id from sharedPreferences was NOT found in the table Projects, open a new one
            //straightaway. If the project was found, show an AleryDialog asking the user if he wants to continue
            //with the last opened project
            if (retrievedProject != null) {
                //define a builder for the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //display a title for the alert dialog
                builder.setTitle("Projekt \"" + retrievedProject.getDescription() + "\" weiter bearbeiten?");
                builder.setNegativeButton("NEIN, NEUES PROJEKT ANLEGEN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing, a new project was already instanced to the variable currentProject
                    }
                });
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set de retrieved project as the current project
                        currentProject = retrievedProject;
                        currentNote = dbHelper.getLastNote(currentProject);
                        ((TextView) findViewById(R.id.subject)).setText(currentNote.getSubject());
                        ((TextView) findViewById(R.id.note)).setText(currentNote.getNote());
                        ((TextView) findViewById(R.id.actual_project)).setText(getString(R.string.actual_project) + currentProject.toString());
                    }
                });
                builder.show();
            }
        }else{
            currentProject = savedInstanceState.getParcelable("currentProject");
            currentNote = savedInstanceState.getParcelable("currentNote");
        }
        //Show project name on the textview with id actual_project (this is done thanks to the object currentProject of the inner class GenoTesDatabaseHelper.Project)
        ((TextView) findViewById(R.id.actual_project)).setText(getString(R.string.actual_project) + currentProject.toString());
    }

    //AND08D S.18 Auf.1.4.
    //The method onSaveInstanceState is overridden in order for the objects currentProject and
    //currentNote to be saved in the bundle that we then can get back with the method onRestoreInstanceState()
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentProject", currentProject);
        outState.putParcelable("currentNote", currentNote);
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
            case R.id.menu_delete_note:
                deleteNote();
                break;
            case R.id.menu_send_project:
                sendProject();
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

    private void sendProject() {
        //We check if the project has notes to be exported
        ArrayList<GeoNotesDatabaseHelper.Note> notes = dbHelper.geoNotes(currentProject);
        if (notes.size() == 0){
            Toast.makeText(this, R.string.no_notes_available, Toast.LENGTH_LONG).show();
            return;
        }
        //We check if the external storage is available, because we make use of it in the method
        //serialize of the class GpxGenerator
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "External Storage nicht verfügbar", Toast.LENGTH_LONG).show();
            return;
        }
        //We make and implizit intent with an enclosed gpx file
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/xml");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"javier.glez.martin@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "GPX Project-export: " + currentProject.toString());
        GpxGenerator gpxGenerator = new GpxGenerator();
        //create a gpxfile containing all of the notes of the actual project
        Uri uriGpxFile = gpxGenerator.createGpxFile(notes, currentProject.toString());
        emailIntent.putExtra(Intent.EXTRA_STREAM, uriGpxFile);
        startActivity(Intent.createChooser(emailIntent, "Projekt verschicken"));
    }

    private void deleteNote() {
        //If this is the last note in project, who an AlertDialog informing the user that deleting
        //this note implies deleting the project. The currentNote is the last note in the project
        //if both of the methods: "getPreviousNote and getNextNote" referred to the actual
        //note return null
        if (currentNote == null) {
            Toast.makeText(this, R.string.select_a_note_before_pressing_delete,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.getPreviousNote(currentNote) == null &
            dbHelper.getNextNote(currentNote) == null) {
            //define a builder for the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //display a title for the alert dialog
            builder.setTitle(R.string.confirm_deletion);
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //do nothing in case that the user selects "no" on confirming deletion
                }
            });
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //delete the current project in case that the user confirms the deletion
                    if (dbHelper.delete(currentProject) < 0) {
                        //inform the user in case of an error on deleting the project
                        Toast.makeText(GatherActivity.this,
                                R.string.project_not_deleted,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //if the note was sucessfully deleted, clear the TextViews note and subject
                        //and create a new project because the last one was deleted.
                        ((TextView) GatherActivity.this.findViewById(R.id.note)).setText("");
                        ((TextView) GatherActivity.this.findViewById(R.id.subject)).setText("");
                        //Initialize a new project
                        currentProject = new GeoNotesDatabaseHelper.Project();
                        //Refresh the content of the TextView with the name of the actual project
                        ((TextView) findViewById(R.id.actual_project)).setText
                                (getString(R.string.actual_project) + currentProject.toString());
                        //Inform the user of the fact, that the project was sucessfully deleted
                        Toast.makeText(GatherActivity.this,
                                R.string.project_deleted, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show(); //show the AlertDialog
        //in case that the project does not contain a single note, delete the actual note
        }else {
            if (dbHelper.delete(currentNote) < 0) {
                //inform the user in case of an error on deleting the note
                Toast.makeText(this, R.string.select_a_note_before_pressing_delete,
                        Toast.LENGTH_SHORT).show();
            } else {
                //if the note was sucessfully deleted, clear the TextViews "note" and "subject"
                ((TextView) GatherActivity.this.findViewById(R.id.note)).setText("");
                ((TextView) GatherActivity.this.findViewById(R.id.subject)).setText("");
                //set currentNote to null (because we are now displaying no note in the TextViews)
                currentNote = null;
                //Inform the user of the fact, that the note was sucessfully deleted
                Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
            }
        }
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
        //when leaving this activity (GatherActivity), we save the id of the actual project in the SharedPreferences
        //as lastProjectID, in order to retrieve the same project when creating this activity again
        getSharedPreferences("preferences", MODE_PRIVATE).edit().putLong("lastProjectID", currentProject.id).apply();
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
    //the position of the actual note as a LatLang object, as well as its subject and note text
    public void onButtonShowPositionClick(View view) {
        if (currentNote != null) {
            //Define an intent and pass the following data: position, subject and note.
            Intent intent = new Intent(this, NoteMapActivity.class);
            //The object currentNote is an instance of the inner entity class Note (see class GeoNotesDatabaseHelper)
            //The class Note implements the interface Parcelable und therefore its objects can be sent from
            //one activity to the other encapuslated in an intent.
            intent.putExtra("currentNote", currentNote);
            //insert also an ArrayList containing every note of the current project
            intent.putExtra("notes", dbHelper.geoNotes(currentProject));
            startActivityForResult(intent,0);
        } else {
            Toast.makeText(this, R.string.no_actual_note, Toast.LENGTH_SHORT).show();
        }
    }

    //Get the current note when comming form the NoteMapActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent){
        Bundle extras = returnIntent.getExtras();
        if (extras != null){
            //show the note that was last selected on the activity that we are comming from "NoteMapActivity"
            currentNote = (GeoNotesDatabaseHelper.Note) extras.getParcelable(NoteMapActivity.CURRENT_NOTE);
            ((TextView) findViewById(R.id.subject)).setText(currentNote.getSubject());
            ((TextView) findViewById(R.id.note)).setText(currentNote.getNote());
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
