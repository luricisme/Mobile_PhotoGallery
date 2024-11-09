package com.example.photo_gallery_app;

import static android.content.ContentValues.TAG;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import com.example.photo_gallery_app.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements MainCallbacks{
    ActivityMainBinding binding;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

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
        replaceFragment(new HomeFragment(), "Home");
        binding.bottomNavigationView.setBackground(null);

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
                replaceFragment(new HomeFragment(), "Home");
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
                        new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                openCamera();
            }
        });
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    // Xử lý kết quả yêu cầu quyền truy cập
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(MainActivity.this, "Cần quyền camera để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }
}