package android.and06.geonotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
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

        //this constructor creates an object of the class Project, given a cursor pointing to a DB
        //where projects are located. See AND07D S.45 Code 3.6.
        public Project(Cursor cursor){
            //call the constructor of this same class (Project), that matches the two types of parameters that
            //I am providing: String and long
            this(cursor.getString(cursor.getColumnIndex("project")), cursor.getLong(cursor.getColumnIndex("id")));
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
    public static class Note extends EntityWithId{

        public final long project;
        public final double latitude;
        public final double longitude;
        public String subject;
        public String note;
        public byte[] data;

        public Note(long id, long project, double latitude, double longitude, String subject, String note, byte[] data){
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

        // setter for the private field (aka. instanzvariable) subject
        public void setSubject(String subject){
            this.subject = subject;
        }

        // setter for the private field (aka. instanzvariable) note
        public void setNote(String note){
            this.note = note;
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

}
