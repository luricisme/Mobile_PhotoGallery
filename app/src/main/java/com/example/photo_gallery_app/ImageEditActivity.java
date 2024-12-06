package com.example.photo_gallery_app;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageEditActivity extends AppCompatActivity {
    private ImageView imageViewEdit;
    private ImageButton btnBack, btnCrop, btnFilter;
    private TextView txtSave;
    private Uri imageUri;
    private Bitmap editedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        imageViewEdit = findViewById(R.id.imageViewEdit);

        Intent intent = getIntent();
        String uriString = intent.getStringExtra("imageUri");

        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            // Hiển th hình ảnh chi tiết ở đây
            imageViewEdit.setImageURI(imageUri);
        }

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnCrop = (ImageButton) findViewById(R.id.btnCrop);
        btnFilter = (ImageButton) findViewById(R.id.btnFilter);
        txtSave = (TextView) findViewById(R.id.txtSave);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish();}
        });

        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UCrop.Options options = new UCrop.Options();
                options.setFreeStyleCropEnabled(true);

                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "cropped.jpg")))
                        .withMaxResultSize(2048, 2048)
                        .withOptions(options)
                        .start(ImageEditActivity.this);
            }
        });

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editfilterIntent = new Intent(ImageEditActivity.this, ImageEditFilterActivity.class);
                editfilterIntent.putExtra("imageUri", imageUri.toString()); // Gửi đường dẫn ảnh Uri qua Intent
                startActivityForResult(editfilterIntent, 1);
            }
        });

        txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo ContentValues để lưu thông tin ảnh
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Edited Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Image after editing");

                // Tạo URI mới trong MediaStore để lưu ảnh vào
                Uri newImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (newImageUri != null) {
                    try {
                        // Mở InputStream từ imageUri (ảnh đã chỉnh sửa)
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        if (inputStream != null) {
                            // Mở OutputStream để ghi ảnh vào MediaStore
                            OutputStream outputStream = getContentResolver().openOutputStream(newImageUri);
                            if (outputStream != null) {
                                // Sao chép dữ liệu từ InputStream sang OutputStream
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = inputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, length);
                                }
                                inputStream.close();
                                outputStream.close();
                                Toast.makeText(ImageEditActivity.this, "Ảnh đã được lưu!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ImageEditActivity.this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }

                Intent intent = new Intent("ACTION_LOAD");
                LocalBroadcastManager.getInstance(ImageEditActivity.this).sendBroadcast(intent);

                finish();
            }
        });


    }

    // Xử lý kết quả Crop
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) { // Activity cắt ảnh
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                imageViewEdit.setImageURI(resultUri);
                imageUri = resultUri;
            }
        }
        if (requestCode == 1 && resultCode == RESULT_OK) { // Activity Filter
            Uri resultUri = data.getData();
            if (resultUri != null) {
                imageViewEdit.setImageURI(resultUri);
                imageUri = resultUri;
            }
        }
    }
}
