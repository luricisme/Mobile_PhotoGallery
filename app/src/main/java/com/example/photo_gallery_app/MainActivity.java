package com.example.photo_gallery_app;

import static android.content.ContentValues.TAG;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import static androidx.core.app.PendingIntentCompat.getActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photo_gallery_app.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainCallbacks{
    ActivityMainBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    public LoadImageFromDevice loadImageFromDevice = new LoadImageFromDevice(this);
    //private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<String> ds = new ArrayList<>();

    // Định nghĩa các đối tượng Fragment tĩnh
    private static final HomeFragment homeFragment = new HomeFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.green));
//        }

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Mặc định ban đầu là HomeFragment
        replaceFragment(homeFragment, "Home");
        binding.bottomNavigationView.setBackground(null);




        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    999);
        }
        else {

            //loadImages();
        }





        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
//            switch(item.getItemId()){
//                case R.id.home:
//                    replaceFragment(new HomeFragment());
//                    break;
//
//                case R.id.album:
//                    replaceFragment(new AlbumFragment());
//                    break;
//
//                case R.id.favorite:
//                    replaceFragment(new FavoriteFragment());
//                    break;
//
//                case R.id.more:
//                    replaceFragment(new MoreFragment());
//                    break;
//            }
            int itemId = item.getItemId();
            if(itemId == R.id.home){
                replaceFragment(homeFragment, "Home");
            }
            else if(itemId == R.id.album)
            {
                replaceFragment(new AlbumFragment(), "Album");
            }
            else if(itemId == R.id.favorite){
                replaceFragment(new FavoriteFragment(), "Favorite");
            }
            else if(itemId == R.id.more){
                replaceFragment(new MoreFragment(), "More");
            }
            

            return true;
        });

        FloatingActionButton camera = findViewById(R.id.camera);
        camera.setOnClickListener(v -> {
            // Kiểm tra quyền truy cập camera
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                openCamera();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 999) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Đã cấp quyền, tiếp tục thực hiện hành động
                Toast.makeText(this, "Quyền thông qua", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(MainActivity.this, "Cần quyền camera để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Load(){
        loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesFromDatabase(this, ds, imageAdapter, homeFragment.recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu); // Inflate menu từ toolbar_menu.xml
        return true;
    }

    private void replaceFragment(Fragment fragment, String title){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onMsgFromFragToMain(String sender, String msg) {
        if (sender.equals("MORE-FRAG")) {
            try { // forward blue-data to redFragment using its callback method
                replaceFragment(new AboutFragment(), "About");
            }
            catch (Exception e) { Log.e("ERROR", "onStrFromFragToMain " + e.getMessage()); }
        }
    }

    // Hàm mở camera
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image from Camera");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // Tạo URI cho hình ảnh

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // Đặt URI cho hình ảnh để lưu

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    // Xử lý kết quả chụp hình
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Sau khi chụp hình, ảnh sẽ được lưu vào album
            if (imageUri != null) {
                Toast.makeText(MainActivity.this, "Hình ảnh đã được lưu vào album", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Có lỗi khi lưu hình ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Chụp hình không thành công", Toast.LENGTH_SHORT).show();
        }
    }
}