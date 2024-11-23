package com.example.photo_gallery_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PhotoAlbum.db";
    private static final int DATABASE_VERSION = 2;  // Cập nhật version

    // Table and columns (photos)
    private static final String TABLE_PHOTOS = "photos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TAG = "tag";
    private static final String COLUMN_ALBUM_NAME = "album_name";
    private static final String COLUMN_IS_HIDDEN = "is_hidden";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_SIZE = "size";

    // Table and columns (photo_deleted)
    private static final String TABLE_PHOTO_DELETED = "photo_deleted";
    private static final String COLUMN_DELETE_DATE = "delete_date";

    // Constructor
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Photos Table
        String CREATE_PHOTOS_TABLE = "CREATE TABLE " + TABLE_PHOTOS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_TAG + " TEXT, "
                + COLUMN_ALBUM_NAME + " TEXT, "
                + COLUMN_IS_HIDDEN + " INTEGER, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_SIZE + " INTEGER"
                + ")";
        db.execSQL(CREATE_PHOTOS_TABLE);

        // Create PhotoDeleted Table
        String CREATE_PHOTO_DELETED_TABLE = "CREATE TABLE " + TABLE_PHOTO_DELETED + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_DELETE_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_PHOTO_DELETED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ và tạo lại bảng mới (chỉ thêm bảng mới mà không ảnh hưởng bảng cũ)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO_DELETED);
        onCreate(db);
    }

    // Add a photo
    public void addPhoto(String name, String date, String tag, String albumName, int isHidden, String filePath, long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TAG, tag);
        values.put(COLUMN_ALBUM_NAME, albumName);
        values.put(COLUMN_IS_HIDDEN, isHidden);
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_SIZE, size);

        // Kiểm tra trùng lặp dựa vào file_path
        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_FILE_PATH}, COLUMN_FILE_PATH + " = ?", new String[]{filePath}, null, null, null);
        if (cursor.getCount() == 0) { // Nếu không tồn tại, thêm mới
            db.insert(TABLE_PHOTOS, null, values);
        }
        cursor.close();
        db.close();
    }

    // Get all photos
    public List<String> getAllPhotoPaths() {
        List<String> photoPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FILE_PATH + " FROM " + TABLE_PHOTOS;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                photoPaths.add(filePath);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return photoPaths;
    }

    // Update photo metadata
    public void updatePhotoMetadata(int id, String name, String tag, String albumName, int isHidden) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TAG, tag);
        values.put(COLUMN_ALBUM_NAME, albumName);
        values.put(COLUMN_IS_HIDDEN, isHidden);

        db.update(TABLE_PHOTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Update photo size or date if edited
    public void updatePhotoIfChanged(String filePath, long newSize, String newDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SIZE, newSize);
        values.put(COLUMN_DATE, newDate);

        db.update(TABLE_PHOTOS, values, COLUMN_FILE_PATH + " = ?", new String[]{filePath});
        db.close();
    }

    // Delete a photo (add to photo_deleted)
    public void deletePhoto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Lấy thông tin ảnh cần xóa trước khi xóa
        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_FILE_PATH}, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
            String deleteDate = String.valueOf(System.currentTimeMillis()); // Thời gian xóa
            addDeletedPhoto(filePath, deleteDate); // Thêm ảnh vào bảng photo_deleted
            cursor.close();
        }

        // Xóa ảnh khỏi bảng photos
        db.delete(TABLE_PHOTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Thêm thông tin ảnh đã xóa vào bảng photo_deleted
    private void addDeletedPhoto(String filePath, String deleteDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_DELETE_DATE, deleteDate);

        db.insert(TABLE_PHOTO_DELETED, null, values);
        db.close();
    }

    // Lấy tất cả ảnh đã xóa
    public List<String> getDeletedPhotos() {
        List<String> deletedPhotos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FILE_PATH + ", " + COLUMN_DELETE_DATE + " FROM " + TABLE_PHOTO_DELETED;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                String deleteDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELETE_DATE));
                deletedPhotos.add("Path: " + filePath + ", Deleted on: " + deleteDate);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return deletedPhotos;
    }

    // Delete all photos
    public void deleteAllPhotos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTOS, null, null);  // Xóa tất cả các bản ghi trong bảng photos
        db.close();
    }

}
