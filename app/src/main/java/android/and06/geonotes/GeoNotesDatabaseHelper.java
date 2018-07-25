package android.and06.geonotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.DateFormat;
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
            "PRIMARY KEY(latitude, longtiude));";

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

    //inner class for the table "Projects" according to the Object-relationales Mapping (AND07D S.20)
    public static class Project{
        public final long id; //this field can be public, since it can not be edited because it is a constant (final)
        private String description;

        public Project(long id, String description){
            this.id = id;
            this.description = description;
        }

        public Project(){
            this(new Date().getTime(), "");
        }

        @Override
        public String toString(){
            return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(new Date(id));
        }

        //this method returns the values contained in an object of the class Project, in form of a ContentValue
        public ContentValues getContentValues(){
            ContentValues values = new ContentValues(2);
            values.put("id", id);
            values.put("description", description);
            return values;
        }
    }

    //inner class for the table "Locations" according to the Object-relationales Mapping (AND07D S.23 Auf.2.4.)
    public static class Location{
        public final double latitude;
        public final double longitude;
        public final int altitude;
        public final String provider;

        public Location(double latitude, double longitude, int altitude, String provider){
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
            this.provider = provider;
        }

        //this method returns the values contained in an object of the class Location, in form of a ContentValue
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
    public static class Note{
        public final long id;
        public final String project;
        public final double latitude;
        public final double longitude;
        public String subject;
        public String note;
        public byte[] data;

        public Note(long id, String project, double latitude, double longitude, String subject, String note, byte[] data){
            this.id = id;
            this.project = project;
            this.latitude = latitude;
            this.longitude = longitude;
            this.subject = subject;
            this.note = note;
            this.data = data;
        }

        public Note(String project, double latitude, double longitude, String subject, String note){
            this(new Date().getTime(), project, latitude, longitude, subject, note, null);
        }

        //this method returns the values contained in an object of the class Note, in form of a ContentValue
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
    }


}
