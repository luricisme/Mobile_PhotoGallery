package com.example.photo_gallery_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ImageDetailActivity extends AppCompatActivity {
    private ImageView imageView;
    private ImageButton btnEdit, btnShare;
    private Uri imageUri;

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
            imageUri = Uri.parse(imagePath);
            // Hiển th hình ảnh chi tiết ở đây
            imageView.setImageURI(imageUri);
        }

        btnEdit = (ImageButton) findViewById(R.id.btnEdit);
        btnShare = (ImageButton) findViewById(R.id.btnShare);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(ImageDetailActivity.this, ImageEditActivity.class);
                editIntent.putExtra("imageUri", imageUri.toString()); // Gửi đường dẫn ảnh Uri qua Intent
                startActivity(editIntent);
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý sự kiện khi nhấn vào share
                // Tạo Intent chia sẻ
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Cho phép quyền đọc URI

                // Hiển thị danh sách các ứng dụng có thể chia sẻ
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hình ảnh qua:"));
            }
        });
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