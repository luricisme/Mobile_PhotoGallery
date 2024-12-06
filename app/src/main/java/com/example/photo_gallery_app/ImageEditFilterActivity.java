package com.example.photo_gallery_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageEditFilterActivity extends AppCompatActivity {

    private ImageView imageViewEditFilter;
    private Bitmap originalBitmap, imgB, imgC, imgM, imgBC, imgBM, imgCM, imgBCM;
    private RelativeLayout contrastBar, brightnessBar, maskBar;
    private RecyclerView maskRecyclerView;
    private SeekBar seekBarBrightness, seekBarContrast;
    private ImageButton btnMask, btnBrightness, btnContrast, btnBack, btnCheck;
    private TextView txtOkBrightness, txtOkContrast, txtOkMask;
    private List<MaskItem> maskList;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit_filter);

        imageViewEditFilter = findViewById(R.id.imageViewEditFilter);

        brightnessBar = findViewById(R.id.brightnessBar);
        contrastBar = findViewById(R.id.contrastBar);

        seekBarBrightness = findViewById(R.id.seekBarBrightness);
        seekBarContrast = findViewById(R.id.seekBarContrast);

        txtOkBrightness = findViewById(R.id.txtOkBrightness);
        txtOkContrast = findViewById(R.id.txtOkContrast);

        btnMask = findViewById(R.id.btnMask);
        btnBrightness = findViewById(R.id.btnBrightness);
        btnContrast = findViewById(R.id.btnContrast);
        btnBack = findViewById(R.id.btnBack);
        btnCheck = findViewById(R.id.btnCheck);


        // Load ảnh từ Intent
        Intent intent = getIntent();
        String uriString = intent.getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
        }

        // ảnh bitmap gốc (original), ảnh có B là có độ sáng, có C là tương phản, có M là có bộ lọc
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imgB = originalBitmap.copy(originalBitmap.getConfig(), true);
            imgC = originalBitmap.copy(originalBitmap.getConfig(), true);
            imgM = originalBitmap.copy(originalBitmap.getConfig(), true);
            imgBC = originalBitmap.copy(originalBitmap.getConfig(), true);
            imgBM = originalBitmap.copy(originalBitmap.getConfig(), true);
            imgCM = originalBitmap.copy(originalBitmap.getConfig(), true);
            imgBCM = originalBitmap.copy(originalBitmap.getConfig(), true);
            imageViewEditFilter.setImageBitmap(originalBitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            finish();
        }


        // Danh sách bộ lọc
        maskList = new ArrayList<>();
        maskList.add(new MaskItem("Original", 0));
        maskList.add(new MaskItem("Grey", 1));
        maskList.add(new MaskItem("Cozy", 2));
        maskList.add(new MaskItem("Reverse", 3));
        maskList.add(new MaskItem("Vintage", 4));
        maskList.add(new MaskItem("Blur", 5));
        maskList.add(new MaskItem("Contrast", 6));
        maskList.add(new MaskItem("Neutral", 7));


        // Quay về giao diện chỉnh ảnh
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish();}
        });

        // Quăng ảnh và quay về giao diện chỉnh ảnh
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Tạo Uri từ ảnh đã chỉnh sửa
                    Uri resultUri = createTempUriFromBitmap(getApplicationContext(), imgBCM);

                    // Gửi Uri về Activity ban đầu
                    returnResult(resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Hiển thị thanh chọn bộ lọc
        btnMask.setOnClickListener(v -> {
            showMaskPopup();
        });

        // Hiển thị thanh trượt độ sáng
        btnBrightness.setOnClickListener(v -> {
            brightnessBar.setVisibility(brightnessBar.getVisibility() == RelativeLayout.GONE ? RelativeLayout.VISIBLE : RelativeLayout.GONE);
        });

        // Hiển thị thanh trượt độ tương phản
        btnContrast.setOnClickListener(v -> {
            contrastBar.setVisibility(contrastBar.getVisibility() == RelativeLayout.GONE ? RelativeLayout.VISIBLE : RelativeLayout.GONE);
        });

        txtOkBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brightnessBar.setVisibility(brightnessBar.getVisibility() == RelativeLayout.GONE ? RelativeLayout.VISIBLE : RelativeLayout.GONE);
            }
        });

        txtOkContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contrastBar.setVisibility(contrastBar.getVisibility() == RelativeLayout.GONE ? RelativeLayout.VISIBLE : RelativeLayout.GONE);
            }
        });

        // Điều chỉnh độ sáng
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int brightness = progress - 100;
                imgBCM = adjustBrightness(imgCM, brightness);
                imgBC = adjustBrightness(imgC, brightness);
                imgBM = adjustBrightness(imgM, brightness);
                imgB = adjustBrightness(originalBitmap, brightness);
                imageViewEditFilter.setImageBitmap(imgBCM);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Điều chỉnh độ tương phản
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float contrast = (float) (progress / 100.0);
                imgBCM = adjustContrast(imgBM, contrast);
                imgBC = adjustContrast(imgB, contrast);
                imgCM = adjustContrast(imgM, contrast);
                imgC = adjustContrast(originalBitmap, contrast);
                imageViewEditFilter.setImageBitmap(imgBCM);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    } //onCreate



    // Hiện thanh chọn bộ lọc
    private void showMaskPopup() {
        // Inflate layout cho PopupWindow
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_mask_bar, null);

        // Tạo PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, (int)(110 * getResources().getDisplayMetrics().density), false);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));  // Đặt nền trắng giống mấy nền thường để thành phần lên màu


        txtOkMask = popupView.findViewById(R.id.txtOkMask);
        maskRecyclerView = popupView.findViewById(R.id.maskRecyclerView);

        // Cấu hình RecyclerView trong Popup
        MaskAdapter adapter = new MaskAdapter(this, maskList, mask -> {
            // Áp dụng bộ lọc dựa trên loại bộ lọc
            imgBCM = applyFilter(imgBC, mask.getMaskType());
            imgBM = applyFilter(imgB, mask.getMaskType());
            imgCM = applyFilter(imgC, mask.getMaskType());
            imgM = applyFilter(originalBitmap, mask.getMaskType());
            imageViewEditFilter.setImageBitmap(imgBCM); // Cập nhật ảnh
        });
        maskRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        maskRecyclerView.setAdapter(adapter);

        // Xử lý nút "OK"
        txtOkMask.setOnClickListener(v -> {
            popupWindow.dismiss(); // Đóng Popup khi bấm "OK"
        });

        // Hiển thị PopupWindow ở mép dưới màn hình
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
    }




    // Áp dụng bộ lọc
    private Bitmap applyFilter(Bitmap src, int maskType) {
        switch (maskType) {
            case 0:
                return src; // Không áp dụng loc
            case 1:
                return applyBlackAndWhiteFilter(src); // Bộ lọc trắng đen
            case 2:
                return applySepiaFilter(src); // Bộ lọc ấm áp
            case 3:
                return applyInvertFilter(src); // Bộ lọc ngược màu
            case 4:
                return applyVintageFilter(src); // Bộ lọc cổ điển
            case 5:
                return applyGaussianBlur(src); // Bộ lọc làm mờ
            case 6:
                return applyHighContrastFilter(src); // Bộ lọc tương phản cao
            case 7:
                return applyPosterizeFilter(src); // Bộ lọc trung hòa
            default:
                return src; // Không áp dụng bộ lọc
        }
    }

    // Áp dụng bộ lọc đen trắng
    private Bitmap applyBlackAndWhiteFilter(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // Đen trắng
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Bộ lọc Sepia - nâu cổ điển
    private Bitmap applySepiaFilter(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        cm.setScale(1.2f, 1f, 0.8f, 1f); // Tăng độ ấm màu
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Bộ lọc đảo ngược màu
    private Bitmap applyInvertFilter(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                -1, 0, 0, 0, 255,
                0, -1, 0, 0, 255,
                0, 0, -1, 0, 255,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Bộ lọc cổ điển vintage
    private Bitmap applyVintageFilter(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.5f);  // Giảm độ bão hòa màu
        cm.setScale(1.1f, 0.9f, 0.8f, 1);  // Tạo hiệu ứng ấm
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Bộ lọc làm mờ
    private Bitmap applyGaussianBlur(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        RenderScript rs = RenderScript.create(this);
        Allocation allocationIn = Allocation.createFromBitmap(rs, src);
        Allocation allocationOut = Allocation.createTyped(rs, allocationIn.getType());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blurScript.setRadius(10); // Đặt độ mờ (từ 1 đến 25)
        blurScript.setInput(allocationIn);
        blurScript.forEach(allocationOut);
        allocationOut.copyTo(bitmap); // Ghi kết quả vào bitmap mới
        rs.destroy();
        return bitmap;
    }

    // Bộ lọc tương phản cao
    private Bitmap applyHighContrastFilter(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                2, 0, 0, 0, -255,
                0, 2, 0, 0, -255,
                0, 0, 2, 0, -255,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Bộ lọc giảm số lượng màu - hiệu ứng tranh vẽ
    private Bitmap applyPosterizeFilter(Bitmap src) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setScale(1.2f, 1.2f, 1.2f, 1);
        cm.setSaturation(0.2f); // Giảm bão hòa màu
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }




    // Điều chỉnh độ sáng
    private Bitmap adjustBrightness(Bitmap src, int brightness) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Điều chỉnh độ tương phản
    private Bitmap adjustContrast(Bitmap src, float contrast) {
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        float scale = contrast;
        float translate = (-0.5f * scale + 0.5f) * 255.f;
        cm.set(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(src, 0, 0, paint);
        return bitmap;
    }

    // Hàm tạo Uri từ Bitmap đã chỉnh sửa và lưu vào bộ nhớ tạm
    private Uri createTempUriFromBitmap(Context context, Bitmap bitmap) throws IOException {
        // Lấy thư mục cache của ứng dụng
        File cacheDir = context.getCacheDir();
        // Tạo file tạm trong thư mục cache
        File tempFile = new File(cacheDir, "edited_image.jpg");

        // Lưu ảnh vào file tạm
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // Lưu ảnh dưới dạng JPEG
        }

        // Trả về Uri của file tạm
        return Uri.fromFile(tempFile);
    }

    // Sau khi chỉnh sửa ảnh, tạo Uri và trả về Activity ban đầu
    private void returnResult(Uri resultUri) {
        // Trả lại Uri cho Activity Edit
        Intent resultIntent = new Intent();
        resultIntent.setData(resultUri);  // Đặt Uri của ảnh đã chỉnh sửa
        setResult(RESULT_OK, resultIntent);  // Trả kết quả về Activity ban đầu
        finish();  // Đóng PhotoEditorActivity và quay lại Activity ban đầu
    }
}
