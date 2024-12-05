package com.example.photo_gallery_app;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
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
    public ImageAdapter imageAdapter;
    private List<String> ds = new ArrayList<>();

    // Định nghĩa các đối tượng Fragment tĩnh
    private static final HomeFragment homeFragment = new HomeFragment();
    private static final AlbumFragment albumFragment = new AlbumFragment();
    private static final FavoriteFragment favorFragment = new FavoriteFragment();
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

        imageAdapter = new ImageAdapter(MainActivity.this, ds);

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
            int itemId = item.getItemId();
            if(itemId == R.id.home){
                replaceFragment(homeFragment, "Home");
            }
            else if(itemId == R.id.album)
            {
                replaceFragment(albumFragment, "Album");
            }
            else if(itemId == R.id.favorite){
                replaceFragment(favorFragment, "Favorite");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.select) {
            // Kiểm tra xem fragment hiện tại có phải là AlbumFragment không
            if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof AlbumFragment) {
                albumFragment.handlerSelect();
            }
            return true;
        } else if (item.getItemId() == R.id.delete) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Lấy item ở trong menu gắn vào toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
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
        //Toast.makeText(this, "choose", Toast.LENGTH_SHORT).show();
        loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesFromDatabase(this, ds, imageAdapter, homeFragment.recyclerView);
    }

    public void LoadImgInAlbum(){
        //loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesFromDatabase(this, ds, imageAdapter, albumFragment.recyclerView);
    }

    public void LoadImgInAlbumID(int id){
        //loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesInAlbumFromDatabase(this, ds, imageAdapter, albumFragment.recyclerView, id);
    }

    public void LoadImgInAlbumAsFavor(){
        //loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesFavoriteFromDatabase(this, ds, imageAdapter, albumFragment.recyclerView);
    }

    public void LoadImgInFavorite(){
        //loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesFavoriteFromDatabase(this, ds, imageAdapter, favorFragment.recyclerView);
    }

    private void replaceFragment(Fragment fragment, String title){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onMsgFromFragToMain(String sender, String msg) {
        if (sender.equals("MORE-FRAG")) {
            try {
                if(msg.equals("ABOUT")){ replaceFragment(new AboutFragment(), "About"); }
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