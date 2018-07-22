package android.and06.geonotes;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GeoNotesDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "GeoNotes";
    public static final int DB_VERSION = 1;

    // SQL Commands are defined as static final Strings:
    private final String CREATE_PROJECTS = "CREATE TABLE IF NOT EXISTS Projects(" +
            "id INTEGER PRIMARY KEY NOT NULL, description TEXT);";
    private final String CREATE_LOCATIONS = "CREATE TABLE IF NOT EXISTS Locations(" +
            "latitude REAL NOT NULL," +
            "longitude REAL NOT NULL," +
            "altitude INTEGER NOT NULL," +
            "provider TEXT NOT NULL," +
            "FOREIGN KEY(latitude, longtiude)" +
            ");";
    private final String CREATE_NOTES = "CREATE TABLE IF NOT EXISTS Notes(" +
            "id INTEGER PRIMARY KEY NOT NULL," +
            "project INTEGER NOT NULL," +
            "latitude REAL NOT NULL," +
            "longitude REAL NOT NULL," +
            "subject TEXT NOT NULL," +
            "note TEXT NOT NULL,                                                                                                                              data BLOB," +
            "CONSTRAINT ProjectFK FOREIGN KEY(project) REFERENCES Projects(id) ON DELETE RESTRICT ON UPDATE CASCADE" +
            "CONSTRAINT LocationFK FOREIGN KEY(latitude, longitude) REFERENCES Locations(latitude, longitude)" +
            ");";

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
}
