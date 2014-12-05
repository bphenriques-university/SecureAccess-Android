package sirs.ist.pt.secureaccess.ListContent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "keysManager";

    // Contacts table name
    private static final String TABLE_SERVERS = "servers";

    // Contacts Table Columns names
    private static final String KEY_ID = "ID";
    private static final String KEY_MAC = "mac";
    private static final String KEY_SHARED_KEY = "key";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SERVERS_TABLE = "CREATE TABLE " + TABLE_SERVERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MAC + " TEXT,"
                + KEY_SHARED_KEY + " TEXT" + ")";
        db.execSQL(CREATE_SERVERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVERS);

        // Create tables again
        onCreate(db);
    }

    public void addServer(Server s){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MAC, s.getMac()); // Contact Name
        values.put(KEY_SHARED_KEY, s.getKey()); // Contact Phone Number

        // Inserting Row
        db.insert(TABLE_SERVERS, null, values);
        db.close(); // Closing database connection
    }

    public Server getSharedKey(String mac) {
        List<Server> serverList = new ArrayList<Server>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SERVERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Server s = new Server(cursor.getString(1), cursor.getString(2));

                // Adding server to list
                serverList.add(s);
            } while (cursor.moveToNext());
        }

        for (Server server : serverList) {
            if (server.getMac().equals(mac)) {
                return server;
            }
        }


        return null;
    }

    public int updateServer(Server s) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MAC, s.getMac());
        values.put(KEY_SHARED_KEY, s.getKey());

        // updating row
        return db.update(TABLE_SERVERS, values, KEY_MAC + " = ?",
                new String[] { String.valueOf(s.getMac()) });
    }

    public void deleteServer(Server s) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SERVERS, KEY_MAC + " = ?",
                new String[] { String.valueOf(s.getMac()) });
        db.close();
    }

}
