package com.google.android.gms.samples.vision.face.facetracker.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by amritkaur on 5/7/2017.
 */

public class customDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String TABLE_NAME = "ImageData";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FILE_PATH = "filepath";
    public static final String COLUMN_EMOTION = "emotion";
    public static final String COLUMN_VALUE = "value";
    private HashMap hp;

    public customDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TABLE_NAME +
                        "("+COLUMN_ID+" integer primary key, "+ COLUMN_FILE_PATH +" text,"+COLUMN_EMOTION+" text,"+COLUMN_VALUE+" text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertData (String file_path, String emotion, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FILE_PATH, file_path);
        contentValues.put(COLUMN_EMOTION, emotion);
        contentValues.put(COLUMN_VALUE, value);
        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_NAME+" where "+COLUMN_ID+" ="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public boolean updateData (Integer id, String file_path, String emotion, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FILE_PATH, file_path);
        contentValues.put(COLUMN_EMOTION, emotion);
        contentValues.put(COLUMN_VALUE, value);
        db.update(TABLE_NAME, contentValues, COLUMN_ID+" = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteData (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                COLUMN_ID+" = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllData() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_FILE_PATH)));
            res.moveToNext();
        }
        return array_list;
    }
}
