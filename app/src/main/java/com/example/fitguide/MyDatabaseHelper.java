package com.example.fitguide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "FitGuide2.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "user";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_DOB = "dob";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_MOBLIE = "mobile";
    private static final String COLUMN_IMAGE_URI = "image_uri";



    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " ("+COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        COLUMN_NAME + " TEXT, " +
                        COLUMN_EMAIL + " TEXT, " +
                        COLUMN_DOB + " TEXT, "+
                        COLUMN_GENDER + " TEXT, "+
                        COLUMN_MOBLIE + " TEXT,"+
                        COLUMN_IMAGE_URI +" TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public void addUser(String name, String email, String dob, String gender, String mobile, String image_uri){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_DOB, dob);
        cv.put(COLUMN_GENDER, gender);
        cv.put(COLUMN_MOBLIE, mobile);
        cv.put(COLUMN_IMAGE_URI, image_uri);

        long result = db.insert(TABLE_NAME, null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
    public User getUserByEmail(String email){
        String query = "SELECT * FROM " + TABLE_NAME+ " WHERE email = " + email + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        // Query the database
        Cursor cursor = db.query(
                TABLE_NAME, // Table name
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_DOB, COLUMN_GENDER, COLUMN_MOBLIE, COLUMN_IMAGE_URI}, // Columns to fetch
                COLUMN_EMAIL + "=?", // WHERE clause
                new String[]{email}, // WHERE arguments
                null, // GROUP BY
                null, // HAVING
                null // ORDER BY
        );

        // Check if the cursor returned a result
        if (cursor != null && cursor.moveToFirst()) {
            // Create a User object with the retrieved data
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBLIE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))

                    );
            cursor.close();
        }
        return user;
    }
    public void UploadPic(User user){

        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("MyDatabaseHelper", "Selected Image URI: " + user.getImage_uri());
        // Prepare the values to update
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, user.getId());
        cv.put(COLUMN_NAME, user.getName());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_DOB, user.getDob());
        cv.put(COLUMN_GENDER, user.getGender());
        cv.put(COLUMN_MOBLIE, user.getMobile());
        cv.put(COLUMN_IMAGE_URI, user.getImage_uri());

        // Update the row where the email matches
        int rowsAffected = db.update(
                TABLE_NAME,        // Table name
                cv,         // New values
                "_id = ?",    // WHERE clause
                new String[]{(user.getId())} // WHERE arguments
        );
        if(rowsAffected == -1){
            Toast.makeText(context,"Failed to Update", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"Successfully updated", Toast.LENGTH_SHORT).show();

        }
    }
}
