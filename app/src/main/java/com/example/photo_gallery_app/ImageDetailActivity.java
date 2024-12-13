    package com.example.photo_gallery_app;

    import android.app.Dialog;
    import android.app.RecoverableSecurityException;
    import android.content.ContentResolver;
    import android.content.Intent;
    import android.content.IntentSender;
    import android.database.Cursor;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;
    import androidx.viewpager2.widget.ViewPager2;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

    public class ImageDetailActivity extends AppCompatActivity {
        private static final int REQUEST_CODE_PERMISSION = 1;
        private boolean isFavorited = false; // Trạng thái ban đầu
        private DatabaseHandler databaseHandler;
//        private ImageView imageView;
        private ImageButton btnFavorite, btnEdit, btnShare, btnDelete;
        private Uri imageUri;

        private ViewPager2 viewPager;
        private List<String> imagePaths = new ArrayList<>();
        private String currentImagePath; // Đường dẫn của ảnh chọn vô (từ activity trước - cố định)
        private int currentPosition; // Vị trí hiện tại (khi lướt qua lại sẽ cập nhật - code bên dưới)

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

//            imageView = findViewById(R.id.imageViewDetail);
            btnFavorite = findViewById(R.id.btnFavorite);

            // Khởi tạo đối tượng DatabaseHandler
            databaseHandler = new DatabaseHandler(this);

//            // Lấy đường dẫn ảnh từ Intent - Lấy từ bên MainActivity.java
//            Intent intent = getIntent();
//            String imagePath = intent.getStringExtra("imagePath");


            // Khởi tạo ViewPager2
            viewPager = findViewById(R.id.viewPager);

            // Lấy danh sách ảnh và chỉ số ảnh hiện tại từ Intent
            Intent intent = getIntent();
            imagePaths = intent.getStringArrayListExtra("imagePaths");  // Lưu tất cả đường dẫn ảnh
            currentImagePath = intent.getStringExtra("currentImagePath");  // Đường dẫn ảnh đang được chọn

            if (imagePaths != null && !imagePaths.isEmpty()) {
                // Xác định vị trí ảnh hiện tại
                currentPosition = imagePaths.indexOf(currentImagePath);

                // Thiết lập Adapter cho ViewPager2
                ImagePagerAdapter adapter = new ImagePagerAdapter(this, imagePaths);
                viewPager.setAdapter(adapter);

                // Đặt ảnh hiện tại
                viewPager.setCurrentItem(currentPosition, false);

                // Hiệu ứng chuyển
                viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
                    @Override
                    public void transformPage(@NonNull View page, float position) {
                        float scaleFactor = 0.85f + (1 - Math.abs(position)) * 0.15f; // Tính tỷ lệ phóng đại
                        page.setScaleX(scaleFactor);  // Áp dụng phóng đại theo chiều ngang
                        page.setScaleY(scaleFactor);  // Áp dụng phóng đại theo chiều dọc

                        // Chỉnh độ mờ của trang tùy theo vị trí
                        page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
                    }
                });

                // Cập nhật các thứ khi chuyển qua lại ảnh
                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);

                        // Cập nhật vị trí hiện tại
                        currentPosition = position;

                        // Lấy đường dẫn ảnh của vị trí hiện tại
                        String imagePath = imagePaths.get(currentPosition);

                        // Cập nhật trạng thái yêu thích từ database
                        isFavorited = databaseHandler.getPhotoFavorStatus(imagePath);
                        updateFavorIcon(btnFavorite, isFavorited);

                        // Cài đặt sự kiện click cho nút "Favor"
                        btnFavorite.setOnClickListener(v -> {
                            isFavorited = !isFavorited; // Đổi trạng thái yêu thích
                            databaseHandler.updatePhotoFavorStatus(imagePath, isFavorited);
                            updateFavorIcon(btnFavorite, isFavorited);
                        });


                        // Cập nhật imageUri để xài cho mấy nút khác
                        imageUri = Uri.parse(imagePath);
                    }
                });

                // Lấy trạng thái "favor" từ database - Lần đầu chọn
                String imagePath = imagePaths.get(currentPosition);
                isFavorited = databaseHandler.getPhotoFavorStatus(imagePath);
                updateFavorIcon(btnFavorite, isFavorited);

                // Xử lý sự kiện click nút "Favor" - Lần đầu chọn
                btnFavorite.setOnClickListener(v -> {
                    isFavorited = !isFavorited; // Đổi trạng thái
                    databaseHandler.updatePhotoFavorStatus(imagePath, isFavorited);
                    updateFavorIcon(btnFavorite, isFavorited);
                });

                // Đặt uri cho ảnh - Lần đầu chọn
                imageUri = Uri.parse(currentImagePath);

            }

            btnEdit = findViewById(R.id.btnEdit);
            btnShare = findViewById(R.id.btnShare);

            btnEdit.setOnClickListener(v -> {
                Intent editIntent = new Intent(ImageDetailActivity.this, ImageEditActivity.class);
                editIntent.putExtra("imageUri", imageUri.toString()); // Gửi đường dẫn ảnh Uri qua Intent
                startActivityForResult(editIntent, 1);
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

            btnDelete = findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(v -> {
                if (imageUri != null) {
                    try {
                        // Also delete the image from the database
                        //DatabaseHandler dbHandler = new DatabaseHandler(this);
                        String imagePath2 = getRealPathFromURI(imageUri); // Get the actual file path from the URI
                        int imageId = databaseHandler.getImageIdFromPath(imageUri.toString());
                        //Toast.makeText(this, "joo" + imageId, Toast.LENGTH_SHORT).show();
                        // Try deleting the image from the MediaStore
                        ContentResolver contentResolver = getContentResolver();
                        int rowsDeleted = contentResolver.delete(imageUri, null, null);

                        if (rowsDeleted > 0) {
                            //Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();



                            if (imageId != -1) {
                                //Toast.makeText(this, "joo00000 " + imageId, Toast.LENGTH_SHORT).show();
                                //databaseHandler.deletePhoto(imageId);
                                databaseHandler.deleteAllPhotos();
                                // Delete from database if image ID is found
                            }

                            // Return to the previous activity with a result indicating the image was deleted
//                            Intent resultIntent = new Intent();
//                            resultIntent.putExtra("imageDeleted", true);  // Truyền thông tin (ví dụ: ảnh đã bị xóa)
//                            setResult(RESULT_OK, resultIntent);
//                            finish();
                            Intent resultIntent = new Intent("ACTION_LOAD");
                            LocalBroadcastManager.getInstance(ImageDetailActivity.this).sendBroadcast(resultIntent);

                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RecoverableSecurityException e) {
                        // If the app doesn't have permission, prompt the user to grant the required permission
                        IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                        try {
                            startIntentSenderForResult(intentSender, REQUEST_CODE_PERMISSION, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                            Toast.makeText(this, "Error requesting permission: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (SecurityException e) {
                        // If the app doesn't have permission, show an error message
                        Toast.makeText(this, "Permission denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // General exception handling
                        e.printStackTrace();
                        Toast.makeText(this, "Error deleting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Image URI not found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private String getRealPathFromURI(Uri contentUri) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                cursor.close();
                return path;
            }
            return null;
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

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 1 && resultCode == RESULT_OK) {
                if (data != null) {
                    // Lấy đường dẫn ảnh đã chỉnh sửa
                    String editedImagePath = data.getStringExtra("editedImagePath");

                    // Thêm ảnh mới vào cuối danh sách
                    imagePaths.add(editedImagePath);

                    // Cập nhật lại adapter của ViewPager2 để hiển thị ảnh mới
                    ImagePagerAdapter adapter = (ImagePagerAdapter) viewPager.getAdapter();
                    adapter.notifyItemInserted(imagePaths.size() - 1);

                    // Cập nhật vị trí ảnh hiện tại để hiển thị ảnh mới
                    viewPager.setCurrentItem(imagePaths.size() - 1, false);
                }
            }
        }
    }
