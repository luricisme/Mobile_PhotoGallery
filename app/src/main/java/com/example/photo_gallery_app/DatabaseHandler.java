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
    private static final int DATABASE_VERSION = 4;

    // Table and columns (photos)
    private static final String TABLE_PHOTOS = "photos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TAG = "tag";
    private static final String COLUMN_IS_HIDDEN = "is_hidden";
    private static final String COLUMN_IS_FAVOR = "is_favor";
    private static final String COLUMN_FILE_PATH = "file_path";
    private static final String COLUMN_SIZE = "size";

    // Table and columns (album)
    private static final String TABLE_ALBUM = "album";
    private static final String COLUMN_ALBUM_ID = "id_album";
    private static final String COLUMN_ALBUM_NAME = "name";
    private static final String COLUMN_ALBUM_PHOTO_COUNT = "photo_count";

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
                + COLUMN_IS_HIDDEN + " INTEGER, "
                + COLUMN_IS_FAVOR + " INTEGER, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_SIZE + " INTEGER, "
                + COLUMN_ALBUM_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_ALBUM_ID + ") REFERENCES " + TABLE_ALBUM + "(" + COLUMN_ALBUM_ID + ")"
                + ")";
        db.execSQL(CREATE_PHOTOS_TABLE);

        // Create Album Table
        String CREATE_ALBUM_TABLE = "CREATE TABLE " + TABLE_ALBUM + "("
                + COLUMN_ALBUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ALBUM_NAME + " TEXT, "
                + COLUMN_ALBUM_PHOTO_COUNT + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_ALBUM_TABLE);

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO_DELETED);
        onCreate(db);
    }

    // Add a photo
    public void addPhoto(String name, String date, String tag, int isHidden, int isFavor, String filePath, long size, Integer albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TAG, tag);
        values.put(COLUMN_IS_HIDDEN, isHidden);
        values.put(COLUMN_IS_FAVOR, isFavor);
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_SIZE, size);
        if (albumId != null) values.put(COLUMN_ALBUM_ID, albumId);

        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_FILE_PATH}, COLUMN_FILE_PATH + " = ?", new String[]{filePath}, null, null, null);
        if (cursor.getCount() == 0) {
            db.insert(TABLE_PHOTOS, null, values);
        }
        cursor.close();
        db.close();

        if (albumId != null) updatePhotoCount(albumId);
    }

    public int getTotalPhoto() {
        int totalPhoto = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Truy vấn để đếm tổng số ảnh trong bảng photo
            String query = "SELECT COUNT(*) FROM photos";
            cursor = db.rawQuery(query, null);

            // Nếu có kết quả, lấy giá trị đầu tiên
            if (cursor.moveToFirst()) {
                totalPhoto = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return totalPhoto;
    }


    // Add an album
    public void addAlbum(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_NAME, name);
        db.insert(TABLE_ALBUM, null, values);
        db.close();
    }

    // Update photo count in album
    public void updatePhotoCount(int albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PHOTOS + " WHERE " + COLUMN_ALBUM_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(albumId)});

        int photoCount = 0;
        if (cursor.moveToFirst()) {
            photoCount = cursor.getInt(0);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_PHOTO_COUNT, photoCount);
        db.update(TABLE_ALBUM, values, COLUMN_ALBUM_ID + " = ?", new String[]{String.valueOf(albumId)});
        db.close();
    }

    // Add photo to album
    public void addPhotoToAlbum(int photoId, int albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_ID, albumId);
        db.update(TABLE_PHOTOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(photoId)});
        db.close();

        updatePhotoCount(albumId);
    }

    // Get all albums
    public List<Album> getAllAlbums() {
        List<Album> albums = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ALBUM_ID + ", " + COLUMN_ALBUM_NAME + ", " + COLUMN_ALBUM_PHOTO_COUNT + " FROM " + TABLE_ALBUM;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int photoCount = cursor.getInt(2);
                albums.add(new Album(id, name, photoCount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return albums;
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

    public List<String> getFavoritePhotoPaths() {
        List<String> photoPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FILE_PATH + " FROM " + TABLE_PHOTOS + " WHERE " + "is_favor = 1";
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


    // Delete a photo
    public void deletePhoto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_FILE_PATH, COLUMN_ALBUM_ID}, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
            int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALBUM_ID));
            String deleteDate = String.valueOf(System.currentTimeMillis());
            addDeletedPhoto(filePath, deleteDate);
            cursor.close();

            db.delete(TABLE_PHOTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            if (albumId != 0) updatePhotoCount(albumId);
        }
        db.close();
    }

    // Add deleted photo info to photo_deleted table
    private void addDeletedPhoto(String filePath, String deleteDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_DELETE_DATE, deleteDate);
        db.insert(TABLE_PHOTO_DELETED, null, values);
        db.close();
    }

    // Kiểm tra trạng thái
    public boolean getPhotoFavorStatus(String filePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isFavorited = false;

        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_IS_FAVOR},
                COLUMN_FILE_PATH + " = ?", new String[]{filePath},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            isFavorited = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVOR)) == 1;
            cursor.close();
        }
        db.close();
        return isFavorited;
    }

    // Cập nhật trạng thái
    public void updatePhotoFavorStatus(String filePath, boolean isFavorited) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_FAVOR, isFavorited ? 1 : 0);

        db.update(TABLE_PHOTOS, values, COLUMN_FILE_PATH + " = ?", new String[]{filePath});
        db.close();
    }


    // Delete all photos
    public void deleteAllPhotos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTOS, null, null);  // Xóa tất cả các bản ghi trong bảng photos
        db.close();
    }

}

