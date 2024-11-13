package com.example.photo_gallery_app;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LoadImageFromDevice {

    public void loadImages(Context context, List<String> ds, ImageAdapter imageAdapter, RecyclerView recyclerView) {
        try {
            ds.clear();
            String[] projection = {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATA,
            };

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.d("MainActivity", "Đã lấy được dữ liệu từ MediaStore"); // log kiểm tra
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    ds.add(contentUri.toString());
                    cursor.moveToNext();
                }
                cursor.close();
            } else {
                Log.e("MainActivity", "Không có ảnh hoặc cursor null"); // log kiểm tra lỗi
            }


            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                imageAdapter = new ImageAdapter(context, ds);
                recyclerView.setAdapter(imageAdapter);
            } else {
                Log.e("MainActivity", "recyclerView null"); // log nếu recyclerView không tồn tại
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi loadImages(): " + e.getMessage()); // log lỗi chi tiết
            e.printStackTrace();
        }
    }

}
