package com.example.photo_gallery_app;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
<<<<<<< Updated upstream
=======
import android.widget.ImageButton;
>>>>>>> Stashed changes
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ImageDetailActivity extends AppCompatActivity {
    private boolean isFavorited = false; // Trạng thái ban đầu
    private DatabaseHandler databaseHandler;
    private ImageView imageView;
<<<<<<< Updated upstream
=======
    private ImageButton btnFavorite;
>>>>>>> Stashed changes

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
        btnFavorite = findViewById(R.id.btnFavorite);


        // Khởi tạo đối tượng DatabaseHandler
        databaseHandler = new DatabaseHandler(this);

        // Lấy đường dẫn ảnh từ Intent - Lấy từ bên MainActivity.java
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");

        if (imagePath != null) {
            // Hiển th hình ảnh chi tiết ở đây
            imageView.setImageURI(Uri.parse(imagePath));
<<<<<<< Updated upstream
=======

            // Lấy trạng thái "favor" từ database
            isFavorited = databaseHandler.getPhotoFavorStatus(imagePath);
            updateFavorIcon(btnFavorite, isFavorited);

            // Xử lý sự kiện click nút "Favor"
            btnFavorite.setOnClickListener(v -> {
                isFavorited = !isFavorited; // Đổi trạng thái
                databaseHandler.updatePhotoFavorStatus(imagePath, isFavorited);
                updateFavorIcon(btnFavorite, isFavorited);
            });
>>>>>>> Stashed changes
        }
    }

    // Hàm cập nhật giao diện icon
    private void updateFavorIcon(ImageButton btnFavorite, boolean isFavorited) {
        if (isFavorited) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_selected); // Đã yêu thích
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_thick); // Không yêu thích
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