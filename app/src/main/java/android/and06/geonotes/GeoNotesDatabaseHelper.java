package android.and06.geonotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GeoNotesDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "GeoNotes";
    public static final int DB_VERSION = 1;

    // SQL Commands are defined as static final Strings:
    private final String CREATE_PROJECTS = "CREATE TABLE IF NOT EXISTS Projects(" +
            "id INTEGER PRIMARY KEY NOT NULL, description TEXT);";

    private final String CREATE_LOCATIONS = "CREATE TABLE IF NOT EXISTS Locations(" +
            "latitude REAL NOT NULL, " +
            "longitude REAL NOT NULL, " +
            "altitude INTEGER NOT NULL, " +
            "provider TEXT NOT NULL, " +
            "PRIMARY KEY(latitude, longitude));";

    private final String CREATE_NOTES = "CREATE TABLE IF NOT EXISTS Notes(" +
            "id INTEGER PRIMARY KEY NOT NULL, " +
            "project INTEGER NOT NULL, " +
            "latitude REAL NOT NULL, " +
            "longitude REAL NOT NULL, " +
            "subject TEXT NOT NULL, " +
            "note TEXT NOT NULL, " +
            "data BLOB, " +
            "CONSTRAINT ProjectFK FOREIGN KEY(project) REFERENCES Projects(id) ON DELETE RESTRICT, " +
            "CONSTRAINT LocationFK FOREIGN KEY(latitude, longitude) REFERENCES Locations(latitude, longitude));";

    public GeoNotesDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL(CREATE_PROJECTS);
            db.execSQL(CREATE_LOCATIONS);
            db.execSQL(CREATE_NOTES);
            Log.d(getClass().getSimpleName(), "Datenbank erzeugt in: \"" + db.getPath() + "\"");
        }catch (SQLException ex){
            Log.e(getClass().getSimpleName(), "on Create: " + ex.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //base class for each entity for the DB
    private static abstract class Entity{
        public final String tableName;

        Entity(String tableName){
            this.tableName = tableName;
        }

        public abstract  ContentValues getContentValues();
    }

    //base class for entity-classes that have no id, like the Project class
    private static abstract class EntityWithId extends Entity{
        public final long id;

        EntityWithId(String tableName, long id){
            super(tableName);
            this.id = id;
        }
    }


    //inner entity-class for the table "Projects", acording to the Object-relationales Mapping (AND07D S.20)
    public static class Project extends  EntityWithId{
        private String description;

        public Project(String description, long id){
            super("Projects", id);
            this.description = description;
        }

        public Project(){
            this("", new Date().getTime());
        }

        //this constructor creates an object of the class Project, given a cursor pointing to the table "Projects" of the DB
        //where projects are located. See  AND07D S.45 Code 3.6.
        public Project(Cursor cursor){
            //call the constructor of this same class (Project), that matches the two types of parameters that
            //I am providing: String and long
            this(cursor.getString(cursor.getColumnIndex("description")), cursor.getLong(cursor.getColumnIndex("id")));
        }

        @Override
        public String toString(){
            //check if the actual project description is empty (the project is new)
            if (description.length() == 0){
                //in case that the project description is empty, the description returned must be the id converted in the long date format
                //with no previous project description (because the project is new and there is still no description)
                return (DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(new Date(super.id)));
            }else{
                //in case that the project description is NOT empty, the description returned must be the project description plus the
                //id converted in the short format
                return ((description + " (" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(super.id))) + ")");
            }

        }

        //this method returns the values contained in an object of the class Project, in form of a ContentValue
        @Override
        public ContentValues getContentValues(){
            ContentValues values = new ContentValues(2);
            values.put("id", super.id);
            values.put("description", description);
            return values;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description){
            this.description = description;
        }
    }

    //inner class for the table "Locations" according to the Object-relationales Mapping (AND07D S.23 Auf.2.4.)
    public static class Location extends Entity{
        public final double latitude;
        public final double longitude;
        public final int altitude;
        public final String provider;

        public Location(double latitude, double longitude, int altitude, String provider){
            super("Locations");
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.provider = provider;
        }

        //this method returns the values contained in an object of the class Location, in form of a ContentValue
        @Override
        public ContentValues getContentValues(){
            ContentValues values = new ContentValues(4);
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            values.put("altitude", altitude);
            values.put("provider", provider);
            return values;
        }
    }

    //inner class for the table "Notes" according to the Object-relationales Mapping (AND07D S.23 Auf.2.5.)
    //This class implements the interface Parcelable, in order to be able to send objects of the
    // class Notes from one activity to the other
    public static class Note extends EntityWithId implements Parcelable{

        public final long project;
        public final double latitude;
        public final double longitude;
        public String subject;
        public String note;
        public byte[] data;

        //--------------------------------------------------------
        //implementing the interface parcelable (see AND07D S.59):
        public static final Creator<Note> CREATOR = new Creator<Note>() {
            @Override
            public Note createFromParcel(Parcel in) {
                return new Note(in);
            }

            @Override
            public Note[] newArray(int size) {
                return new Note[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        //the method writeToParcel of the interface Parcelable writes all of the Fields of the class Note in a Parcel object "parcel"
        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(id);
            parcel.writeLong(project);
            parcel.writeDouble(latitude);
            parcel.writeDouble(longitude);
            parcel.writeString(subject);
            parcel.writeString(note);
            parcel.writeByteArray(data);
        }

        //the new constructor of the class Note for the interface Parcelable reads all of the Fields of the class Note
        //from the incomming Parcel object "in", in the same order as they where written by the method writeToParcel()
        //and calls the constructor Note of the class Note with this().
        private Note(Parcel in) {
            this(in.readLong(),
                 in.readLong(),
                 in.readDouble(),
                 in.readDouble(),
                 in.readString(),
                 in.readString(),
                 in.createByteArray());
        }
        /* End of implementation of Parcelable-Interface*/
        //----------------------------------------------------------

        public Note (long id, long project, double latitude, double longitude, String subject, String note, byte[] data) {
            super("Notes", id);
            this.project = project;
            this.latitude = latitude;
            this.longitude = longitude;
            this.subject = subject;
            this.note = note;
            this.data = data;
        }

        public Note(long project, double latitude, double longitude, String subject, String note){
            this(new Date().getTime(), project, latitude, longitude, subject, note, null);
        }

        //with this constructor it is possible to create a Note object with a given cursor pointing
        //at a desired Note saved in the DB in the table Notes. (see AND07D S.49 Auf.3.8.)
        public Note(Cursor cursor) {
            this(cursor.getLong(cursor.getColumnIndex("id")),
                 cursor.getLong(cursor.getColumnIndex("project")),
                 cursor.getDouble(cursor.getColumnIndex("latitude")),
                 cursor.getDouble(cursor.getColumnIndex("longitude")),
                 cursor.getString(cursor.getColumnIndex("subject")),
                 cursor.getString(cursor.getColumnIndex("note")),
                 cursor.getBlob(cursor.getColumnIndex("data")));
        }

        //this method returns the values contained in an object of the class Note, in form of a ContentValue
        @Override
        public ContentValues getContentValues(){
            ContentValues values = new ContentValues(7);
            values.put("id", id);
            values.put("project", project);
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            values.put("subject", subject);
            values.put("note", note);
            values.put("data", data);
            return values;
        }

        //we override the method equals of this class Note, that is heritaged form the class object, in order
        //to be able to use the indexOf method of the class ArrayList in a proper way (AND07D S.68)
        @Override
        public  boolean equals(Object other){
            //if the field id of the objects being compared with the method equals are the same,
            //return true. If not, return false
            //If the second object if not of the class Note, return also false
            if(other instanceof Note) return ((Note) other).id == id;
            return false;
        }

        // setter for the private field (aka. instanzvariable) subject
        public void setSubject(String subject){
            this.subject = subject;
        }

        // setter for the private field (aka. instanzvariable) note
        public void setNote(String note){
            this.note = note;
        }

        public String getSubject() {
            return this.subject;
        }

        public String getNote() {
            return this.note;
        }

    }

    //this method inserts the data providing of the method getContentValues in a given table of our DB
    public void insert(String table, ContentValues values){
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertOrThrow(table,null, values);
        } catch(SQLException ex){
            Log.d(this.getClass().getSimpleName(), ex.toString());
        }finally {
            db.close();
        }
    }

    //this method inserts the data providing of the method getContentValues in a given table of our DB
    public void insert(Entity entity){
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertOrThrow(entity.tableName,null, entity.getContentValues());
        } catch(SQLException ex){
            Log.d(this.getClass().getSimpleName(), ex.toString());
        }finally {
            db.close();
        }
    }

    //this method updates the note or the subject of a note in the db
    //this update version is declared as private in order to avoid calling it from other class with
    //an entity object, and forcing to use the second variant of update (with EntityWitId)
    private void update(String table, ContentValues values){
        SQLiteDatabase db = getWritableDatabase();
        db.update(table, values, "id=" + values.get("id"), null);
        db.close();
    }

    //this method updates the note or the subject of a note in the db
    public void update(EntityWithId entity){
        SQLiteDatabase db = getWritableDatabase();
        db.update(entity.tableName, entity.getContentValues(), "id=" + entity.getContentValues().get("id"), null);
        db.close();
    }

    //this method returns all the projects stored in the DB as an ArrayList of objects of the class Project
    public ArrayList<Project> getProjects(){
        //access to the db in reade mode and instantiate a Cursor object, to be able to iterate through the db
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("Projects", new String[]{"*"}, null, null, null, null, "id", null);
        //define an Arraylist of objects of the entity-class Project
        ArrayList<Project> result = new ArrayList<Project>();
        //iterate through the DB
        while (cursor.moveToNext()){
            //create an object of the Entity-Class Project, using the new constructor Project(cursor) that we created for the Entity-Class Project
            Project project = new Project(cursor);
            //save each obejct of the class project  in the ArrayList
            result.add(project);
        }
        //close db AND cursor!
        cursor.close();
        db.close();
        return result;
    }

    //this method returns the lastNote present on the table Notes of a given project passed as parameter to the method (See AND07D S.48 Cod3.8.)
    public Note getLastNote(Project project){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("Notes", new String[]{"*"}, "project='" + project.id + "'", null, null, null, "id DESC", "1");
        Note lastNote = null;
        if (cursor.moveToNext()){
            lastNote = new Note(cursor);
        }
        cursor.close();
        db.close();
        return lastNote;
    }

    public Note getPreviousNote(Note note) {
        SQLiteDatabase db = getReadableDatabase();
        //Using a cursor, Collect all of the notes within the project relating to the note passed through parameter
        //of this methode, and whoe id is shorter to the id of the note passed through parameter.
        String selection = String.format("project = '%d' AND id <'%s'", note.project, note.id);
        //We order the elements iterating the table on id DESC, in that case, the first element will be the most
        //closest note to the note passed through parameter, earlier to that one.
        Cursor cursor = db.query("Notes", new String[] {"*"}, selection, null, null, null, "id DESC", "1");
        Note previousNote = null;
        //get the first note of the table, which will be the earlier to the note passed through parameter, because
        //that is the way we have ordered that table
        if (cursor.moveToNext())
        {
            previousNote = new Note(cursor);
        }
        cursor.close();
        db.close();
        return previousNote;
    }

    public Note getNextNote(Note note){
        SQLiteDatabase db = getReadableDatabase();
        String selection = String.format("project = '%d' AND id >'%s'", note.project, note.id);
        Cursor cursor = db.query("Notes", new String[] {"*"}, selection, null, null, null, "id ASC", "1");
        Note nextNote = null;
        if (cursor.moveToNext())
        {
            nextNote = new Note(cursor);
        }
        cursor.close();
        db.close();
        return nextNote;
    }

    // This method returns the project saved in the table "Projects", given a certain id as parameter (AND07D S.55 Auf.3.3)
    public Project getProject(long id){
        SQLiteDatabase db = getReadableDatabase();
        String selection = String.format("id = '%s'", id);
        Cursor cursor = db.query("Projects", new String[] {"*"}, selection, null, null, null, null, "1");
        Project project = null;
        if (cursor.moveToNext())
        {
            project = new Project(cursor);
        }
        cursor.close();
        db.close();
        return project;
    }

    //AND07D S.62
    //This method returns an ArrayList<Notes> containing all of the Notes of a given Project
    public  ArrayList<Note> geoNotes(Project project){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("Notes", new String[]{"*"},"project='" + project.id + "'", null, null, null, "id", null);
        ArrayList<Note> notes = new ArrayList<Note>();
        while (cursor.moveToNext()){
            //store each note in the ArrayList, using the constructor that accepts a cursor as parameter
            notes.add(new Note(cursor));
        }
        cursor.close();
        db.close();
        return notes;
    }

    //AND07D Einsendeaufg. 4b
    //This method deletes a given note
    public int delete(Note note){
        //First of all, check if note is not empty. In case we try to delete a note that was
        //typed by the user in the EditViews but not saved and the user presses "Delete note",
        // the db.execSQL will throw an error, because note = null.
        if (note == null){
            Log.e(getClass().getSimpleName(), "ERROR! No note selected yet");
            return -1;
        }
        //Now try to delete the note.
        //If the deletion was sucessful, return a 0. If it was unsuccessful, insert
        //an error message in the log and return -1. Returning this values help us to detect
        //from the activity calling this method, if the deleting process was properly done
        //and act consequently, deleting the text in both of the EditText: Subject and Note
        SQLiteDatabase db = getReadableDatabase();
        try {
            db.execSQL("DELETE FROM Notes WHERE id=" + note.id);
            return 0;
        }catch (SQLException ex){
            Log.e(getClass().getSimpleName(),
                 "ERROR! Not possible to delete the note with id " +
                  note.id + ": " + ex.toString());
            return -1;
        }
    }

    //This method deletes a given project
    public int delete(Project project){
        //Check if the project is not null
        if (project == null){
            Log.e(getClass().getSimpleName(), "ERROR! No project selected");
            return -1;
        }
        //try to delete the note. If it was possible, return a 1. If it was not possible, insert
        //an error message in the log and return -1. Returning this values help us to detect
        //from the activity calling this method, if the deleting process was properly done
        //and in that case, delete the text in both of the EditText: Subject and Note
        SQLiteDatabase db = getReadableDatabase();
        try {
            db.execSQL("DELETE FROM Projects WHERE id=" + project.id);
            return 0;
        }catch (SQLException ex){
            Log.e(getClass().getSimpleName(),
                 "ERROR! Not possible to delete the project with id " +
                  project.id + ": " + ex.toString());
            return -1;
        }
    }
}
