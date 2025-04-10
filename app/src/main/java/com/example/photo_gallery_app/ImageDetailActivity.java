    package com.example.photo_gallery_app;

    import android.app.Dialog;
    import android.app.RecoverableSecurityException;
    import android.content.ContentResolver;
    import android.content.ContentValues;
    import android.content.Intent;
    import android.content.IntentSender;
    import android.database.Cursor;
    import android.media.MediaScannerConnection;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
    import android.provider.MediaStore;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.loader.content.CursorLoader;
    import androidx.localbroadcastmanager.content.LocalBroadcastManager;
    import androidx.viewpager2.widget.ViewPager2;

    import com.github.chrisbanes.photoview.PhotoView;

    import java.io.File;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

    public class ImageDetailActivity extends AppCompatActivity {
        private static final int REQUEST_CODE_PERMISSION = 1;
        private boolean isFavorited = false; // Trạng thái ban đầu
        private boolean isHidden = false; // Trạng thái ban đầu
        private DatabaseHandler databaseHandler;
//        private ImageView imageView;
        private ImageButton btnFavorite, btnEdit, btnShare, btnDelete;
        private Uri imageUri;

        private ViewPager2 viewPager;
        private List<String> imagePaths = new ArrayList<>();
        private String currentImagePath; // Đường dẫn của ảnh chọn vô (từ activity trước - cố định)
        private int currentPosition = 0; // Vị trí hiện tại (khi lướt qua lại sẽ cập nhật - code bên dưới)

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


                //Toast.makeText(ImageDetailActivity.this, String.valueOf(imagePaths.size()), Toast.LENGTH_SHORT).show();

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

                        // Tìm view liên quan đến ảnh ở vị trí này
                        View view = viewPager.findViewWithTag("view_" + position);
                        if (view != null) {
                            PhotoView imageView = view.findViewById(R.id.imageView);
                            imageView.setScale(1f, true);  // Đặt lại tỷ lệ 1:1
                        }

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


                isHidden= databaseHandler.getPhotoHideStatus(imagePath);

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
                    // Tạo AlertDialog xác nhận trước khi xóa
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.dialog_title))
                            .setMessage(getString(R.string.dialog_message))
                            .setPositiveButton(getString(R.string.positive_button), (dialog, which) -> {
                                try {
                                    int imageId = databaseHandler.getImageIdFromPath(imageUri.toString());
                                    // Xóa ảnh khỏi bộ nhớ
                                    ContentResolver contentResolver = getContentResolver();
                                    int rowsDeleted = contentResolver.delete(imageUri, null, null); // Trả về số lượng hàng đã bị xóa

                                    if (rowsDeleted > 0) {
                                        if (imageId != -1) {
                                            databaseHandler.deletePhoto(imageId);
                                        }

                                        // Thông báo với các thành phần khác rằng ảnh đã bị xóa
                                        Intent resultIntent = new Intent("ACTION_LOAD");
                                        LocalBroadcastManager.getInstance(ImageDetailActivity.this).sendBroadcast(resultIntent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (RecoverableSecurityException e) {
                                    IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                                    try {
                                        startIntentSenderForResult(intentSender, REQUEST_CODE_PERMISSION, null, 0, 0, 0, null);
                                    } catch (IntentSender.SendIntentException ex) {
                                        ex.printStackTrace();
                                        Toast.makeText(this, "Error requesting permission: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (SecurityException e) {
                                    Toast.makeText(this, "Permission denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    // General exception handling
                                    e.printStackTrace();
                                    Toast.makeText(this, "Error deleting image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(getString(R.string.negative_button), (dialog, which) -> {
                                dialog.dismiss();
                            });

                    // Lấy ra đối tượng AlertDialog
                    AlertDialog dialog = builder.create();

                    // Thay đổi màu chữ của các nút trong AlertDialog
                    dialog.setOnShowListener(d -> {
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.black));  // Thay đổi màu nút "Có"
                        negativeButton.setTextColor(getResources().getColor(R.color.black));   // Thay đổi màu nút "Hủy"
                    });

                    dialog.show();
                } else {
                    Toast.makeText(this, "Image URI not found", Toast.LENGTH_SHORT).show();
                }
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
            MenuItem hideItem = menu.findItem(R.id.hide);
            if (isHidden) {
                hideItem.setTitle(getString(R.string.detail_show));
            } else {
                hideItem.setTitle(getString(R.string.detail_hide));
            }

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.hide) {
                String imagePath = imagePaths.get(currentPosition); // Đường dẫn hiện tại
                String realPath;
                String newPath = "";

                if (!isHidden) {
                    // Lấy đường dẫn thực tế của ảnh
                    Uri uriPath = Uri.parse(imagePath);
                    realPath = getRealPathFromURI(uriPath);
                } else {
                    // Lấy đường dẫn từ database cho ảnh ẩn
                    realPath = databaseHandler.getHiddenImagePathFromPhotoPath(imagePath);
                }

                if (realPath == null || realPath.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy đường dẫn thực của ảnh", Toast.LENGTH_SHORT).show();
                    return true;
                }

                File imageFile = new File(realPath);

                if (!isHidden) {
                    // Ẩn ảnh
                    File hiddenDir = new File(Environment.getExternalStorageDirectory(), ".inome");

                    if (!hiddenDir.exists()) hiddenDir.mkdirs();

                    if (imageFile.exists()) {
                        File hiddenImage = new File(hiddenDir, imageFile.getName());
                        boolean success = imageFile.renameTo(hiddenImage);

                        if (success) {
                            newPath = hiddenImage.getAbsolutePath();
                            databaseHandler.addHiddenImage(imagePath, newPath);
                            databaseHandler.updatePhotoHiddenStatus(imagePath, true);

                            // Xóa ảnh khỏi MediaStore
                            getContentResolver().delete(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    MediaStore.Images.Media.DATA + "=?",
                                    new String[]{imageFile.getAbsolutePath()}
                            );

                            Toast.makeText(this, "Ảnh đã được ẩn", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Không thể ẩn ảnh", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Ảnh không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Hiển thị lại ảnh
                    File hiddenDir = new File(Environment.getExternalStorageDirectory(), ".inome");
                    File hiddenImage = new File(hiddenDir, imageFile.getName());

                    if (hiddenImage.exists()) {
                        File publicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyAppPhotos");

                        if (!publicDir.exists()) publicDir.mkdirs();

                        File originalFile = new File(publicDir, hiddenImage.getName());
                        boolean success = hiddenImage.renameTo(originalFile);

                        if (success) {
                            newPath = originalFile.getAbsolutePath();
                            databaseHandler.deleteHiddenImage(imagePath);
                            databaseHandler.updatePhotoHiddenStatus(imagePath, false);

                            // Quét lại MediaStore để cập nhật
                            MediaScannerConnection.scanFile(
                                    this,
                                    new String[]{originalFile.getAbsolutePath()},
                                    null,
                                    (path, uri) -> Log.d("MediaScanner", "File updated: " + path)
                            );

                            Toast.makeText(this, "Ảnh đã được hiển thị lại", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Không thể hiển thị lại ảnh", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Ảnh không tồn tại trong thư mục ẩn", Toast.LENGTH_SHORT).show();
                    }
                }

                // Cập nhật trạng thái trong cơ sở dữ liệu
                //databaseHandler.updatePhotoPath(imagePath, newPath);

                // Thoát hoặc cập nhật giao diện
                onBackPressed();
                return true;
            }

            else if (item.getItemId() == R.id.info) {
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
        private String getRealPathFromURI(Uri contentUri) {
            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
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
