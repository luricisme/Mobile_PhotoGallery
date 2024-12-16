package com.example.photo_gallery_app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private DatabaseHandler databaseHandler;

    private EditText searchEditText;
    private TextView txtView;
    private ImageButton searchButton;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<String> ds = new ArrayList<>();
    private Uri imageUri;
    private int currentLayout = 1; // Bắt đầu với layout 1 cột
    private ImageButton btnLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thay thế nguyên màn hình hiện tại bằng activity_image_detail
        setContentView(R.layout.activity_search);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Khởi tạo đối tượng DatabaseHandler
        databaseHandler = new DatabaseHandler(this);

        recyclerView = findViewById(R.id.recyclerView);
        searchButton = findViewById(R.id.btnSearch);
        searchEditText = findViewById(R.id.textEdit);
        txtView = findViewById(R.id.textView);

        txtView.setText("");

        // Lấy đường dẫn ảnh từ Intent - Lấy từ bên MainActivity.java
        Intent intent = getIntent();

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(this, ds);
        recyclerView.setAdapter(imageAdapter);

        // Tìm nút chuyển đổi layout
        btnLayout = findViewById(R.id.btn_change_layout);

        // Xử lý sự kiện bấm nút
        btnLayout.setOnClickListener(v -> switchLayout());

        // Xử lý sự kiện nhấn phím Enter trong EditText
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                // Lấy từ khóa tìm kiếm từ EditText
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    // Gọi phương thức tìm kiếm ảnh từ cơ sở dữ liệu
                    searchImages(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        // Xử lý sự kiện nhấn nút tìm kiếm
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchImages(query);
                hideKeyboard();
            } else {
                Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    // Phương thức tìm kiếm ảnh trong cơ sở dữ liệu
    private void searchImages(String query) {
        // Tìm kiếm ảnh trong cơ sở dữ liệu
        List<String> imageItems = databaseHandler.getPhotosByKeyword(query);

        if (imageItems.isEmpty()) {
            recyclerView.setAdapter(null);
            txtView.setText("Không tìm thấy kết quả!");
            Toast.makeText(this, "Không tìm thấy kết quả!", Toast.LENGTH_SHORT).show();
        } else {

            txtView.setText("");
            Toast.makeText(this, imageItems.get(0), Toast.LENGTH_SHORT).show();
            // Cập nhật dữ liệu cho RecyclerView
            //imageAdapter.updateData(imageItems);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            imageAdapter = new ImageAdapter(this, imageItems);
            recyclerView.setAdapter(imageAdapter);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Toast.makeText(this, "dfdsf", Toast.LENGTH_SHORT).show();

        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            searchImages(query);
        }

        setImageLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_detailimage, menu); // toolbar_menu là tên file XML của menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Xử lý sự kiện "Back"
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchLayout() {
        currentLayout++; // Tăng kiểu layout
        if (currentLayout > 3) currentLayout = 1;

        setImageLayout();
    }

    private void setImageLayout() {
        switch (currentLayout) {
            case 1:
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                btnLayout.setImageResource(R.drawable.ic_layout_1);
                break;
            case 2:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                btnLayout.setImageResource(R.drawable.ic_layout_2);
                break;
            case 3:
                recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                btnLayout.setImageResource(R.drawable.ic_layout_3);
                break;
        }
    }

}
