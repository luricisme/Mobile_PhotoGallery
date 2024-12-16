package com.example.photo_gallery_app;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PhotoAlbum.db";
    private static final int DATABASE_VERSION = 10;

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


    private static final String TABLE_PHOTO_IN_ALBUM = "photo_in_album";
    private static final String COLUMN_PHOTO_ALBUM_ID = "photo_id";

    // Constructor
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Photos
        String CREATE_PHOTOS_TABLE = "CREATE TABLE " + TABLE_PHOTOS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_TAG + " TEXT, "
                + COLUMN_IS_HIDDEN + " INTEGER, "
                + COLUMN_IS_FAVOR + " INTEGER, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_SIZE + " INTEGER"
                + ")";
        db.execSQL(CREATE_PHOTOS_TABLE);

        // Tạo bảng Album
        String CREATE_ALBUM_TABLE = "CREATE TABLE " + TABLE_ALBUM + "("
                + COLUMN_ALBUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ALBUM_NAME + " TEXT, "
                + COLUMN_ALBUM_PHOTO_COUNT + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_ALBUM_TABLE);

        // Tạo bảng PhotoDeleted
        String CREATE_PHOTO_DELETED_TABLE = "CREATE TABLE " + TABLE_PHOTO_DELETED + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FILE_PATH + " TEXT, "
                + COLUMN_DELETE_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_PHOTO_DELETED_TABLE);

        // Tạo bảng TablePhotoOnAlbum
        String CREATE_PHOTO_IN_ALBUM_TABLE = "CREATE TABLE " + TABLE_PHOTO_IN_ALBUM + "("
                + COLUMN_PHOTO_ALBUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ALBUM_ID + " INTEGER, "
                + COLUMN_ID + " INTEGER, "
                + "FOREIGN KEY (" + COLUMN_ALBUM_ID + ") REFERENCES " + TABLE_ALBUM + " (" + COLUMN_ALBUM_ID + "), "
                + "FOREIGN KEY (" + COLUMN_ID + ") REFERENCES " + TABLE_PHOTOS + " (" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_PHOTO_IN_ALBUM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALBUM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO_DELETED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO_IN_ALBUM);
        onCreate(db);
    }

    // Thêm ảnh vào bảng photos
    public void addPhoto(String name, String date, String tag, int isHidden, int isFavor, String filePath, long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TAG, tag);
        values.put(COLUMN_IS_HIDDEN, isHidden);
        values.put(COLUMN_IS_FAVOR, isFavor);
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_SIZE, size);

        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_FILE_PATH}, COLUMN_FILE_PATH + " = ?", new String[]{filePath}, null, null, null);
        if (cursor.getCount() == 0) {
            db.insert(TABLE_PHOTOS, null, values);
        }
        cursor.close();
        db.close();
    }

    // Lấy tổng số ảnh
    public int getTotalPhoto() {
        int totalPhoto = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_PHOTOS;
            cursor = db.rawQuery(query, null);

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

    // Thêm album mới
    public void addAlbum(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALBUM_NAME, name);
        db.insert(TABLE_ALBUM, null, values);
        db.close();
    }

    // Cập nhật số ảnh trong album
    public void updatePhotoCount(int albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PHOTO_IN_ALBUM + " WHERE " + COLUMN_ALBUM_ID + " = ?";
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

    // Thêm ảnh vào album
    public void addPhotoToAlbum(int photoId, int albumId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, photoId);
        values.put(COLUMN_ALBUM_ID, albumId);
        db.insert(TABLE_PHOTO_IN_ALBUM, null, values);
        db.close();
        updatePhotoCount(albumId);
    }

    // Lấy tất cả album và ảnh đầu tiên của mỗi album
    public List<Album> getAllAlbumsWithFirstPhoto() {
        List<Album> albums = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Câu truy vấn
        String query = "SELECT " +
                "a." + COLUMN_ALBUM_ID + ", " +
                "a." + COLUMN_ALBUM_NAME + ", " +
                "a." + COLUMN_ALBUM_PHOTO_COUNT + ", " +
                "MIN(p." + COLUMN_FILE_PATH + ") AS first_photo_path " +
                "FROM " + TABLE_ALBUM + " a " +
                "LEFT JOIN " + TABLE_PHOTO_IN_ALBUM + " pi " +
                "ON a." + COLUMN_ALBUM_ID + " = pi." + COLUMN_ALBUM_ID + " " +
                "LEFT JOIN " + TABLE_PHOTOS + " p " + // Kết nối bảng photos để lấy file_path
                "ON pi." + COLUMN_ID + " = p." + COLUMN_ID + " " +
                "GROUP BY a." + COLUMN_ALBUM_ID;


        // Thực thi truy vấn
        Cursor cursor = db.rawQuery(query, null);

        // Xử lý kết quả trả về
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ALBUM_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_ALBUM_NAME));
                @SuppressLint("Range") int photoCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ALBUM_PHOTO_COUNT));

                // Lấy đường dẫn ảnh đầu tiên (có thể null nếu không có ảnh)
                @SuppressLint("Range") String firstPhotoPath = cursor.getString(cursor.getColumnIndex("first_photo_path"));

                // Thêm vào danh sách
                albums.add(new Album(id, name, photoCount, firstPhotoPath));
            } while (cursor.moveToNext());
        }

        // Đóng con trỏ
        cursor.close();
        return albums;
    }

    // Tìm kiếm ảnh theo tên, trả về danh sách
    public List<String> getPhotosByKeyword(String keyword) {
        List<String> photoPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Sử dụng câu lệnh LIKE để tìm ảnh có tên chứa từ khóa
        String query = "SELECT " + COLUMN_FILE_PATH + " FROM " + TABLE_PHOTOS +
                " WHERE " + COLUMN_NAME + " LIKE ?";

        // Thực hiện truy vấn với từ khóa (thêm dấu % trước và sau từ khóa để tìm kiếm mọi nơi trong tên ảnh)
        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                photoPaths.add(filePath);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return photoPaths;
    }



    // Lấy tất cả đường dẫn ảnh
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

    // Lấy ảnh yêu thích
    public List<String> getFavoritePhotoPaths() {
        List<String> photoPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FILE_PATH + " FROM " + TABLE_PHOTOS + " WHERE " + COLUMN_IS_FAVOR + " = 1";
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

    public void deletePhoto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            // Lấy đường dẫn file của ảnh cần xóa
            cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_FILE_PATH}, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                //int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALBUM_ID));
                String deleteDate = String.valueOf(System.currentTimeMillis());

                cursor = db.query(TABLE_PHOTO_IN_ALBUM, new String[]{COLUMN_ALBUM_ID}, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
                int albumId = -1; // Giá trị mặc định nếu không tìm thấy
                if (cursor != null && cursor.moveToFirst()) {
                    albumId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ALBUM_ID));
                }
                // Lưu thông tin ảnh bị xóa vào bảng deleted_photos
                //addDeletedPhoto(filePath, deleteDate);

                // Xóa liên kết trong bảng photo_in_album trước
                db.delete(TABLE_PHOTO_IN_ALBUM, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

                // Xóa ảnh trong bảng photos
                db.delete(TABLE_PHOTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

                updatePhotoCount(albumId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Xóa album khỏi cơ sở dữ liệu
    public void deleteAlbum(int albumId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Xóa liên kết ảnh trong bảng photo_in_album
            db.delete(TABLE_PHOTO_IN_ALBUM, COLUMN_ALBUM_ID + " = ?", new String[]{String.valueOf(albumId)});

            // Xóa album khỏi bảng album
            db.delete(TABLE_ALBUM, COLUMN_ALBUM_ID + " = ?", new String[]{String.valueOf(albumId)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    // Thêm ảnh đã xóa vào bảng photo_deleted
    private void addDeletedPhoto(String filePath, String deleteDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_DELETE_DATE, deleteDate);
        db.insert(TABLE_PHOTO_DELETED, null, values);
        db.close();
    }

    // Kiểm tra trạng thái yêu thích
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

    public int getTotalFavoritedPhotos() {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalFavorited = 0;

        // Truy vấn để đếm số lượng ảnh yêu thích
        String query = "SELECT COUNT(*) FROM " + TABLE_PHOTOS + " WHERE " + COLUMN_IS_FAVOR + " = 1";
        Cursor cursor = db.rawQuery(query, null);

        // Nếu có kết quả, lấy giá trị đầu tiên
        if (cursor != null && cursor.moveToFirst()) {
            totalFavorited = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return totalFavorited;
    }

    public int getImageIdFromPath(String imagePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_ID}, COLUMN_FILE_PATH + " = ?",
                    new String[]{imagePath}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return -1; // Trả về -1 nếu không tìm thấy ID
    }


    // Cập nhật trạng thái yêu thích
    public void updatePhotoFavorStatus(String filePath, boolean isFavorited) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_FAVOR, isFavorited ? 1 : 0);

        db.update(TABLE_PHOTOS, values, COLUMN_FILE_PATH + " = ?", new String[]{filePath});
        db.close();
    }

    // Kiểm tra ảnh đã tồn tại
    public boolean isPhotoExists(String filePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PHOTOS, new String[]{COLUMN_ID}, COLUMN_FILE_PATH + " = ?", new String[]{filePath}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public List<String> getPhotosByAlbumId(int albumId) {
        List<String> photoPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Truy vấn để lấy danh sách file_path từ bảng photos dựa trên id_album
            String query = "SELECT " + TABLE_PHOTOS + "." + COLUMN_FILE_PATH + " " +
                    "FROM " + TABLE_PHOTOS + " " +
                    "INNER JOIN " + TABLE_PHOTO_IN_ALBUM + " " +
                    "ON " + TABLE_PHOTOS + "." + COLUMN_ID + " = " + TABLE_PHOTO_IN_ALBUM + "." + COLUMN_ID + " " +
                    "WHERE " + TABLE_PHOTO_IN_ALBUM + "." + COLUMN_ALBUM_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(albumId)});

            // Thêm các đường dẫn ảnh vào danh sách
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                    photoPaths.add(filePath);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return photoPaths;
    }

    // Lấy ảnh đầu tiên từ tất cả ảnh
    public String getFirstPhotoPath() {
        String filePath = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FILE_PATH + " FROM " + TABLE_PHOTOS + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
        }
        cursor.close();
        return filePath;
    }

    // Lấy ảnh đầu tiên trong ảnh yêu thích
    public String getFirstFavoritePhotoPath() {
        String filePath = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_FILE_PATH + " FROM " + TABLE_PHOTOS + " WHERE " + COLUMN_IS_FAVOR + " = 1 LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
        }
        cursor.close();
        return filePath;
    }

    // Xóa tất cả ảnh
    public void deleteAllPhotos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTO_IN_ALBUM, null, null);
        db.delete(TABLE_ALBUM, null, null);
        db.delete(TABLE_PHOTOS, null, null);
        db.close();
    }
}

