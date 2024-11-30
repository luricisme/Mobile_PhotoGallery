package com.example.photo_gallery_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ImageDetailActivity extends AppCompatActivity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thay thế nguyên màn hình hiện tại bằng activity_image_detail
        setContentView(R.layout.activity_image_detail);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.imageViewDetail);

        // Lấy đường dẫn ảnh từ Intent - Lấy từ bên MainActivity.java
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");

        if (imagePath != null) {
            // Hiển th hình ảnh chi tiết ở đây
            imageView.setImageURI(Uri.parse(imagePath));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_detailimage, menu); // toolbar_menu là tên file XML của menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.hide) {
            // Xử lý sự kiện "Hide"
            return true;
        } else if (item.getItemId() == R.id.info) {
            // Xử lý sự kiện "Information"
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // Xử lý sự kiện "Back"
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}