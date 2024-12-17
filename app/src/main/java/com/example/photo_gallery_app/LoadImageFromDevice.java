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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class LoadImageFromDevice {

    private final DatabaseHandler databaseHandler;
    private final ExecutorService executorService;
    private final Handler uiHandler;

    // Constructor để nhận DatabaseHandler và khởi tạo ExecutorService
    public LoadImageFromDevice(Context context) {
        databaseHandler = new DatabaseHandler(context);
        executorService = Executors.newSingleThreadExecutor(); // Dùng 1 luồng để xử lý background task
        uiHandler = new Handler(Looper.getMainLooper()); // Để cập nhật UI từ background thread
    }

    // Lấy ảnh từ MediaStore và lưu vào database nếu chưa có
    public void loadImagesFromDevice(Context context) {

        //databaseHandler.deleteAllPhotos();
        executorService.execute(() -> {
            try {
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

                        // Kiểm tra xem ảnh đã tồn tại trong cơ sở dữ liệu chưa
                        if (!databaseHandler.isPhotoExists(filePath)) {
                            databaseHandler.addPhoto(name, date, null, 0, 0, filePath, size);
                        }

                        cursor.moveToNext();
                    }
                    cursor.close();
                } else {
                    Log.e("LoadImageFromDevice", "Không có ảnh hoặc cursor null");
                }

                // Cập nhật giao diện khi hoàn tất
//                uiHandler.post(() ->
//                        Toast.makeText(context, "Tải ảnh từ thiết bị hoàn tất!", Toast.LENGTH_SHORT).show()
//                );

            } catch (Exception e) {
                Log.e("LoadImageFromDevice", "Lỗi khi loadImagesFromDevice(): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Lấy ảnh từ database và hiển thị lên RecyclerView
    public void loadImagesFromDatabase(Context context, List<String> ds, ImageAdapter imageAdapter, RecyclerView recyclerView) {
        executorService.execute(() -> {
            try {
                ds.clear();
                List<String> photoPaths = databaseHandler.getAllPhotoPaths();
                ds.addAll(photoPaths);

                uiHandler.post(() -> {
                    if (recyclerView != null) {
                        //recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        imageAdapter.setDs(context, ds);
                        recyclerView.setAdapter(imageAdapter);
                    } else {
                        Log.e("LoadImageFromDevice", "recyclerView null");
                    }
                });
            } catch (Exception e) {
                Log.e("LoadImageFromDevice", "Lỗi khi loadImagesFromDatabase(): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Lấy ảnh yêu thích từ database và hiển thị lên RecyclerView
    public void loadImagesFavoriteFromDatabase(Context context, List<String> ds, ImageAdapter imageAdapter, RecyclerView recyclerView) {
        executorService.execute(() -> {
            try {
                ds.clear();
                List<String> photoPaths = databaseHandler.getFavoritePhotoPaths();
                ds.addAll(photoPaths);

                uiHandler.post(() -> {
                    if (recyclerView != null) {
                        //recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        imageAdapter.setDs(context, ds);
                        recyclerView.setAdapter(imageAdapter);
                    } else {
                        Log.e("LoadImageFromDevice", "recyclerView null");
                    }
                });
            } catch (Exception e) {
                Log.e("LoadImageFromDevice", "Lỗi khi loadImagesFavoriteFromDatabase(): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Lấy ảnh theo album từ database và hiển thị lên RecyclerView
    public void loadImagesInAlbumFromDatabase(Context context, List<String> ds, ImageAdapter imageAdapter, RecyclerView recyclerView, int id) {
        executorService.execute(() -> {
            try {
                ds.clear();
                List<String> photoPaths = databaseHandler.getPhotosByAlbumId(id);
                ds.addAll(photoPaths);

                uiHandler.post(() -> {
                    if (recyclerView != null) {
                        //recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        imageAdapter.setDs(context, ds);
                        recyclerView.setAdapter(imageAdapter);
                    } else {
                        Log.e("LoadImageFromDevice", "recyclerView null");
                    }
                });
            } catch (Exception e) {
                Log.e("LoadImageFromDevice", "Lỗi khi loadImagesInAlbumFromDatabase(): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Dọn dẹp ExecutorService khi không cần nữa
    public void shutdownExecutor() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
