package com.example.photo_gallery_app;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageDetailActivity extends AppCompatActivity {
    private boolean isFavorited = false; // Trạng thái ban đầu
    private DatabaseHandler databaseHandler;
    private ImageView imageView;
    private ImageButton btnFavorite, btnEdit, btnShare;
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
        btnFavorite = findViewById(R.id.btnFavorite);

        // Khởi tạo đối tượng DatabaseHandler
        databaseHandler = new DatabaseHandler(this);

        // Lấy đường dẫn ảnh từ Intent - Lấy từ bên MainActivity.java
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");

        if (imagePath != null) {
            imageUri = Uri.parse(imagePath);
            // Hiển thị hình ảnh chi tiết ở đây
            imageView.setImageURI(imageUri);

            // Lấy trạng thái "favor" từ database
            isFavorited = databaseHandler.getPhotoFavorStatus(imagePath);
            updateFavorIcon(btnFavorite, isFavorited);

            // Xử lý sự kiện click nút "Favor"
            btnFavorite.setOnClickListener(v -> {
                isFavorited = !isFavorited; // Đổi trạng thái
                databaseHandler.updatePhotoFavorStatus(imagePath, isFavorited);
                updateFavorIcon(btnFavorite, isFavorited);
            });
        }

        btnEdit = findViewById(R.id.btnEdit);
        btnShare = findViewById(R.id.btnShare);

        btnEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(ImageDetailActivity.this, ImageEditActivity.class);
            editIntent.putExtra("imageUri", imageUri.toString()); // Gửi đường dẫn ảnh Uri qua Intent
            startActivity(editIntent);
        });

        btnShare.setOnClickListener(v -> {
            // Xử lý sự kiện khi nhấn vào share
            // Tạo Intent chia sẻ
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Cho phép quyền đọc URI

            // Hiển thị danh sách các ứng dụng có thể chia sẻ
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ hình ảnh qua:"));
        });
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
            String[] projection = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT
            };

            try (Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    // Lấy thông tin chi tiết
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));

                    // Chuyển đổi thành đối tượng Date
                    Date date = new Date(dateAdded * 1000); // Lưu ý: DATE_ADDED trả về giây, nên cần nhân với 1000 để chuyển thành millisecond

                    // Định dạng lại thành chuỗi
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String formattedDate = sdf.format(date);

                    // Tạo dialog
                    Dialog dialog = new Dialog(ImageDetailActivity.this);
                    dialog.setContentView(R.layout.dialog_image_info);
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

                    dialog.getWindow().setElevation(16f);

                    // Ánh xạ các View trong Dialog
                    TextView tvImageName = dialog.findViewById(R.id.tvImageName);
                    TextView tvImageDateAdded = dialog.findViewById(R.id.tvImageDateAdded);
                    TextView tvImageWH = dialog.findViewById(R.id.tvImageWH);
                    TextView tvImageSize = dialog.findViewById(R.id.tvImageSize);
                    TextView tvImageMimeType = dialog.findViewById(R.id.tvImageMimeType);

                    // Hiển thị thông tin
                    tvImageName.setText(displayName);
                    tvImageDateAdded.setText(formattedDate);
                    tvImageWH.setText(width + "x" + height);
                    tvImageSize.setText(Math.round(size / 1024) + " KB");
                    tvImageMimeType.setText(mimeType);

                    // Hiển thị Dialog
                    dialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else if (item.getItemId() == android.R.id.home) {
            // Xử lý sự kiện "Back"
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
