package com.example.photo_gallery_app;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LoadImageFromDevice {

    private final DatabaseHandler databaseHandler;

    // Constructor để nhận DatabaseHandler
    public LoadImageFromDevice(Context context) {
        databaseHandler = new DatabaseHandler(context);
    }

    // Lấy ảnh từ MediaStore và lưu vào database nếu chưa có
    public void loadImagesFromDevice(Context context) {
        try {
            databaseHandler.deleteAllPhotos();
            //databaseHandler.clearDeletedPhotos();
            String[] projection = {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.SIZE,
            };

            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.getCount() > 0) {
                Log.d("LoadImageFromDevice", "Đã lấy được dữ liệu từ MediaStore");
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE);


                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    long id = cursor.getLong(idColumn);

                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    String name = cursor.getString(nameColumn);
                    String date = cursor.getString(dateColumn); // Thời gian tạo
                    String filePath = contentUri.toString();
                    long size = cursor.getLong(sizeColumn);

                    // Kiểm tra và thêm vào database nếu chưa tồn tại
                    databaseHandler.addPhoto(name, date, null, "Default Album", 0, filePath, size);

                    cursor.moveToNext();
                }
                cursor.close();
            } else {
                Log.e("LoadImageFromDevice", "Không có ảnh hoặc cursor null");
            }
        } catch (Exception e) {
            Log.e("LoadImageFromDevice", "Lỗi khi loadImagesFromDevice(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Lấy ảnh từ database và hiển thị lên RecyclerView
    public void loadImagesFromDatabase(Context context, List<String> ds, ImageAdapter imageAdapter, RecyclerView recyclerView) {
        try {
            ds.clear();
            List<String> photoPaths = databaseHandler.getAllPhotoPaths();
            ds.addAll(photoPaths);

            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                imageAdapter = new ImageAdapter(context, ds);
                recyclerView.setAdapter(imageAdapter);
            } else {
                Log.e("LoadImageFromDevice", "recyclerView null");
            }
        } catch (Exception e) {
            Log.e("LoadImageFromDevice", "Lỗi khi loadImagesFromDatabase(): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
