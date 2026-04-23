package com.example.duedatedock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "assignments.db";

    // ⚠️ Increment this anytime you change schema
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "assignments";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_CLASS_NAME = "class_name";
    public static final String COLUMN_NOTES = "notes";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // =========================
    // CREATE TABLE
    // =========================
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DUE_DATE + " TEXT, " +
                COLUMN_PRIORITY + " TEXT, " +
                COLUMN_CLASS_NAME + " TEXT, " +
                COLUMN_NOTES + " TEXT" +
                ")";

        db.execSQL(createTable);
    }

    // =========================
    // UPGRADE DATABASE (SAFE)
    // =========================
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Safe incremental upgrades (prevents data loss if expanded later)
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN " + COLUMN_PRIORITY + " TEXT");
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN " + COLUMN_CLASS_NAME + " TEXT");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME +
                    " ADD COLUMN " + COLUMN_NOTES + " TEXT");
        }
    }

    // =========================
    // INSERT ASSIGNMENT
    // =========================
    public void insertAssignment(String title,
                                 String dueDate,
                                 String priority,
                                 String className,
                                 String notes) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DUE_DATE, dueDate);
        values.put(COLUMN_PRIORITY, priority);
        values.put(COLUMN_CLASS_NAME, className);
        values.put(COLUMN_NOTES, notes);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // =========================
    // GET ASSIGNMENTS BY DATE
    // =========================
    public Cursor getAssignments(String dueDate) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_NAME,
                null,
                COLUMN_DUE_DATE + "=?",
                new String[]{dueDate},
                null,
                null,
                null
        );
    }

    // =========================
    // DELETE ASSIGNMENT
    // =========================
    public void deleteAssignment(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(
                TABLE_NAME,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }

    // =========================
    // UPDATE ASSIGNMENT
    // =========================
    public void updateAssignment(int id,
                                 String title,
                                 String priority,
                                 String className,
                                 String notes,
                                 String dueDate) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_PRIORITY, priority);
        values.put(COLUMN_CLASS_NAME, className);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_DUE_DATE, dueDate);

        db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        db.close();
    }
}