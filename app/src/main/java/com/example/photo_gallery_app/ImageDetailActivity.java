package com.example.photo_gallery_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ImageDetailActivity extends AppCompatActivity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thay thế nguyên màn hình hiện tại bằng activity_image_detail
        setContentView(R.layout.activity_image_detail);

        imageView = findViewById(R.id.imageViewDetail);

        // Lấy đường dẫn ảnh từ Intent - Lấy từ bên MainActivity.java
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");

        if (imagePath != null) {
            // Hiển th hình ảnh chi tiết ở đây
            imageView.setImageURI(Uri.parse(imagePath));
        }
    }
}