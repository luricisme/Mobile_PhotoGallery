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

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

        // Lấy đường dẫn ảnh từ Intent - Lấy từ MainActivity.java -> LoadIFD.LIFDatabase -> ImageAdap.BindViewHolder
        Intent intent = getIntent();
        String uriString = intent.getStringExtra("imageUri");

        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            // Hiển th hình ảnh chi tiết ở đây
            imageViewEdit.setImageURI(imageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageViewEdit.setImageBitmap(bitmap);
                editedBitmap = bitmap; // Dùng bitmap này để chỉnh sửa
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            public void onClick(View v) { finish();}
        });

        txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo ContentValues để lưu thông tin ảnh
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Edited Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Image after editing");

                // Lấy URI của MediaStore để lưu ảnh vào
                Uri newimageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (newimageUri != null) {
                    try {
                        // Mở OutputStream để ghi ảnh vào MediaStore
                        OutputStream outputStream = getContentResolver().openOutputStream(newimageUri);
                        if (outputStream != null) {
                            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream); // Lưu ảnh vào URI
                            outputStream.close(); // Đóng OutputStream
                            Toast.makeText(ImageEditActivity.this, "Ảnh đã được lưu vào thư viện!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(ImageEditActivity.this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    // Xử lý kết quả Crop
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    editedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    imageViewEdit.setImageBitmap(editedBitmap);
                    imageUri = resultUri;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
