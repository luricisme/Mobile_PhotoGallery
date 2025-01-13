package com.example.photo_gallery_app;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_gallery_app.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MainCallbacks{
    ActivityMainBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_DETAIL = 2;
    private Uri imageUri;
    public LoadImageFromDevice loadImageFromDevice = new LoadImageFromDevice(this);
    //private RecyclerView recyclerView;
    public ImageAdapter imageAdapter;
    private List<String> ds = new ArrayList<>();

    // Định nghĩa các đối tượng Fragment tĩnh
    private static final HomeFragment homeFragment = new HomeFragment();
    private static final AlbumFragment albumFragment = new AlbumFragment();
    private static final FavoriteFragment favorFragment = new FavoriteFragment();

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load ngôn ngữ thông qua hàm này
        loadLocale();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Xin phép lúc dầu để có toàn quyền
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Kiểm tra xem quyền đã được cấp hay chưa
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to request permission", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION
            );
        }

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
                replaceFragment(homeFragment, getString(R.string.bottom_menu_home));
            }
            else if(itemId == R.id.album)
            {
                replaceFragment(albumFragment, getString(R.string.bottom_menu_album));
            }
            else if(itemId == R.id.favorite){
                replaceFragment(favorFragment, getString(R.string.bottom_menu_favorite));
            }
            else if(itemId == R.id.more){
                replaceFragment(new MoreFragment(), getString(R.string.bottom_menu_more));
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

        // Đăng ký broadcast
        IntentFilter filter = new IntentFilter("ACTION_LOAD");
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(receiver, filter);

        ImageButton btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (currentFragment instanceof HomeFragment) {
                ((HomeFragment) currentFragment).enableSelectionMode(false);
            } else if (currentFragment instanceof FavoriteFragment) {
                ((FavoriteFragment) currentFragment).enableSelectionMode(false);
            }
            else if (currentFragment instanceof AlbumFragment) {
                //((AlbumFragment) currentFragment).enableSelectionMode(false);
                albumFragment.handlerSelect();
            }
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_menu);
            binding.bottomAppBar.setVisibility(View.VISIBLE);
            binding.camera.setVisibility(View.VISIBLE);
            binding.selectedBottom.setVisibility(View.GONE);

            int currentMenuId = R.id.home;
            if (currentFragment instanceof HomeFragment) {
                currentMenuId = R.id.home;
            } else if (currentFragment instanceof AlbumFragment) {
                currentMenuId = R.id.album;
            } else if (currentFragment instanceof FavoriteFragment) {
                currentMenuId = R.id.favorite;
            }
            binding.bottomNavigationView.setSelectedItemId(currentMenuId);
        });

        ImageButton btnErase = findViewById(R.id.btnDelete);
        btnErase.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (currentFragment instanceof HomeFragment) {
                //((HomeFragment) currentFragment).enableSelectionMode(false);
                HomeFragment homeFragment = (HomeFragment) currentFragment;
                handleImageDeletion(homeFragment.recyclerView, this);
            } else if (currentFragment instanceof FavoriteFragment) {
                FavoriteFragment favoriteFragment = (FavoriteFragment) currentFragment;
                handleImageDeletion(favoriteFragment.recyclerView, this);
            }
            else if (currentFragment instanceof AlbumFragment) {
                albumFragment.handlerErase();
            }
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_menu);
            binding.bottomAppBar.setVisibility(View.VISIBLE);
            binding.camera.setVisibility(View.VISIBLE);
            binding.selectedBottom.setVisibility(View.GONE);

            int currentMenuId = R.id.home;
            if (currentFragment instanceof HomeFragment) {
                currentMenuId = R.id.home;
            } else if (currentFragment instanceof AlbumFragment) {
                currentMenuId = R.id.album;
            } else if (currentFragment instanceof FavoriteFragment) {
                currentMenuId = R.id.favorite;
            } else if(currentFragment instanceof MoreFragment){
                currentMenuId = R.id.more;
            }
            binding.bottomNavigationView.setSelectedItemId(currentMenuId);
        });

        ImageButton btnDelFromAlbum = findViewById(R.id.btnDelFromAlbum);
        btnDelFromAlbum.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            callHandleImageDelFromAlbum();

            binding.bottomNavigationView.inflateMenu(R.menu.bottom_menu);
            binding.bottomAppBar.setVisibility(View.VISIBLE);
            binding.camera.setVisibility(View.VISIBLE);
            binding.selectedBottom.setVisibility(View.GONE);

            int currentMenuId = R.id.home;
            if (currentFragment instanceof HomeFragment) {
                currentMenuId = R.id.home;
            } else if (currentFragment instanceof AlbumFragment) {
                currentMenuId = R.id.album;
            } else if (currentFragment instanceof FavoriteFragment) {
                currentMenuId = R.id.favorite;
            } else if(currentFragment instanceof MoreFragment){
                currentMenuId = R.id.more;
            }
            binding.bottomNavigationView.setSelectedItemId(currentMenuId);
        });
    }

    public void callHandleImageDeletion(){
        handleImageDeletion(albumFragment.recyclerView, this);
    }

    public void callHandleImageDelFromAlbum(){
        handleImageDelFromAlbum(albumFragment.recyclerView, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search){
            Intent intent = new Intent(this, SearchActivity.class);
            this.startActivity(intent);
        }
        if (item.getItemId() == R.id.select) {
            if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof AlbumFragment) {
                binding.selectedBottom.findViewById(R.id.btnAdd).setVisibility(View.GONE);

                AlbumFragment albumFragment = (AlbumFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                if (albumFragment.isViewingPhotos()) {
                    // Hiển thị nút DelFromAlbum nếu isViewingPhotos là true
                    binding.selectedBottom.findViewById(R.id.btnDelFromAlbum).setVisibility(View.VISIBLE);
                } else {
                    // Ẩn nút DelFromAlbum nếu isViewingPhotos là false
                    binding.selectedBottom.findViewById(R.id.btnDelFromAlbum).setVisibility(View.GONE);
                }
                albumFragment.handlerSelect();
            }
            if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof HomeFragment) {
                binding.selectedBottom.findViewById(R.id.btnDelFromAlbum).setVisibility(View.GONE);
                binding.selectedBottom.findViewById(R.id.btnAdd).setVisibility(View.VISIBLE);

                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                homeFragment.enableSelectionMode(true);
                //Toast.makeText(this, "Chế độ chọn ảnh được bật", Toast.LENGTH_SHORT).show();
            }
            if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof FavoriteFragment) {
                binding.selectedBottom.findViewById(R.id.btnDelFromAlbum).setVisibility(View.GONE);
                binding.selectedBottom.findViewById(R.id.btnAdd).setVisibility(View.GONE);

                FavoriteFragment favoriteFragment = (FavoriteFragment) getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                favoriteFragment.enableSelectionMode(true);
                //Toast.makeText(this, "Chế độ chọn ảnh được bật", Toast.LENGTH_SHORT).show();
            }

            binding.bottomNavigationView.getMenu().clear();
            binding.bottomAppBar.setVisibility(View.GONE);
            binding.camera.setVisibility(View.GONE);
            binding.selectedBottom.setVisibility(View.VISIBLE);

            if (binding.selectedBottom.getVisibility() == View.VISIBLE) {
                Log.d("DEBUG", "selectedBottom đã được hiển thị.");
            } else {
                Log.d("DEBUG", "selectedBottom không được hiển thị.");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLocale(String langCode, boolean recreateActivity) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        // Lưu lại ngôn ngữ vào SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("Language", langCode);
        editor.apply();

        // Reload lại để áp dụng thay đổi ngôn ngữ
        if (recreateActivity) {
            recreate();
        }
    }

    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("Language", "en"); // Mặc định là tiếng Anh
        setLocale(language, false);
    }

    public void loadImgAfterDelete(){
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof AlbumFragment) {
            //Toast.makeText(this, "album", Toast.LENGTH_SHORT).show();
            albumFragment.loadimg();
            albumFragment.setLayout();
        }
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof HomeFragment) {
            //Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
            Load();
            homeFragment.setImageLayout();
        }
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof FavoriteFragment) {
            //Toast.makeText(this, "favorites", Toast.LENGTH_SHORT).show();
            LoadImgInFavorite();
            favorFragment.setImageLayout();
        }
    }

    private void handleImageDeletion(RecyclerView recyclerView, Context context) {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof ImageAdapter) {
            ImageAdapter imageAdapter = (ImageAdapter) adapter;
            List<String> selectedImages = imageAdapter.getSelectedImages();

            if (selectedImages.isEmpty()) {
                Toast.makeText(context, "Không có ảnh nào được chọn để xóa", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(getString(R.string.dialog_title))
                    .setMessage(getString(R.string.dialog_message))
                    .setPositiveButton(getString(R.string.positive_button), (dialog, which) -> {
                        try {
                            ContentResolver contentResolver = context.getContentResolver();
                            DatabaseHandler db = new DatabaseHandler(context);

                            // Lặp qua từng ảnh và xóa
                            for (String imagePath : selectedImages) {
                                Uri imageUri = Uri.parse(imagePath);
                                int rowsDeleted = contentResolver.delete(imageUri, null, null);

                                if (rowsDeleted > 0) {
                                    int imageId = db.getImageIdFromPath(imagePath);
                                    if (imageId != -1) {
                                        db.deletePhoto(imageId);
                                    }
                                } else {
                                    Toast.makeText(context, "Không thể xóa ảnh: " + imagePath, Toast.LENGTH_SHORT).show();
                                }
                            }
                            loadImgAfterDelete();

                            Toast.makeText(context, "Đã xóa các ảnh đã chọn", Toast.LENGTH_SHORT).show();
                        } catch (SecurityException e) {
                            Toast.makeText(context, "Permission denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Lỗi khi xóa ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getString(R.string.negative_button), (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(context.getResources().getColor(R.color.black));
                negativeButton.setTextColor(context.getResources().getColor(R.color.black));
            });

            dialog.show();
        }
    }


    private void handleImageDelFromAlbum(RecyclerView recyclerView, Context context) {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof ImageAdapter) {
            ImageAdapter imageAdapter = (ImageAdapter) adapter;
            List<String> selectedImages = imageAdapter.getSelectedImages();

            if (selectedImages.isEmpty()) {
                Toast.makeText(context, "Không có ảnh nào được chọn để xóa khỏi album", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(getString(R.string.dialog_title_delfromalbum))
                    .setMessage(getString(R.string.dialog_message_delfromalbum))
                    .setPositiveButton(getString(R.string.positive_button), (dialog, which) -> {
                        try {
                            ContentResolver contentResolver = context.getContentResolver();
                            DatabaseHandler db = new DatabaseHandler(context);

                            // Lặp qua từng ảnh và xóa
                            for (String imagePath : selectedImages) {
                                Uri imageUri = Uri.parse(imagePath);
                                int rowsDeleted = contentResolver.delete(imageUri, null, null);

                                if (rowsDeleted > 0) {
                                    int imageId = db.getImageIdFromPath(imagePath);
                                    if (imageId != -1) {
                                        db.deletePhoto(imageId);
                                    }
                                } else {
                                    Toast.makeText(context, "Không thể xóa ảnh: " + imagePath, Toast.LENGTH_SHORT).show();
                                }
                            }
                            loadImgAfterDelete();

                            Toast.makeText(context, "Đã xóa các ảnh đã chọn", Toast.LENGTH_SHORT).show();
                        } catch (SecurityException e) {
                            Toast.makeText(context, "Permission denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Lỗi khi xóa ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getString(R.string.negative_button), (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(d -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(context.getResources().getColor(R.color.black));
                negativeButton.setTextColor(context.getResources().getColor(R.color.black));
            });

            dialog.show();
        }
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
                //Toast.makeText(this, "Quyền thông qua", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
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
        //imageAdapter.notifyDataSetChanged();
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

    public void LoadImgInAlbumAsHide(){
        //loadImageFromDevice.loadImagesFromDevice((this));
        loadImageFromDevice.loadImagesHiddenFromDatabase(this, ds, imageAdapter, albumFragment.recyclerView);
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
                if (msg.startsWith("LANGUAGE_")) {
                    String language = msg.split("_")[1]; // Lấy phần ngôn ngữ từ thông điệp
                    if (language.equals("en")) {
                        setLocale("en", true); // Chọn Tiếng Anh
                    } else if (language.equals("vi")) {
                        setLocale("vi", true); // Chọn Tiếng Việt
                    }
                    binding.bottomNavigationView.setSelectedItemId(R.id.home);
                }
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

    // Lắng nghe tín hiệu yêu cầu gọi load
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_LOAD".equals(intent.getAction())) {
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                loadImgAfterDelete(); // Gọi hàm Load
            }
        }
    };
    @Override
    protected void onRestart() {
        super.onRestart();
        //Load();
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof AlbumFragment) {
            albumFragment.setLayout();
        }
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof HomeFragment) {
            //Toast.makeText(this, "fffff", Toast.LENGTH_SHORT).show();
            homeFragment.setImageLayout();
        }
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof FavoriteFragment) {
            favorFragment.setImageLayout();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Load();
    }

    @Override
    protected void onResume() {
        //Toast.makeText(this, "tt", Toast.LENGTH_SHORT).show();
        if (getSupportFragmentManager().findFragmentById(R.id.frame_layout) instanceof AlbumFragment) {
            //Toast.makeText(this, "album", Toast.LENGTH_SHORT).show();
            albumFragment.loadimg();
            albumFragment.setLayout();
        }
        super.onResume();
        loadImageFromDevice.loadImagesFromDevice((this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (imageUri != null) {
                    Load();
                    Toast.makeText(this, "Hình đã được lưu", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Có lỗi khi lưu hình ảnh", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (imageUri != null) {
                    getContentResolver().delete(imageUri, null, null);
                    imageUri = null;
                }
                Toast.makeText(this, "Chụp hình không thành công", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_DETAIL) {
            if (resultCode == RESULT_OK) {
                boolean imageDeleted = data.getBooleanExtra("imageDeleted", false);
                if (imageDeleted) {
                    Load(); // Gọi hàm để tải lại danh sách ảnh
                    Toast.makeText(this, "Image deleted success", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}