package com.example.photo_gallery_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {

    private ImageButton btnAddAlbum, btnBack, btnLayout;
    public RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private ArrayList<Album> albumList;
    private DatabaseHandler databaseHandler;
    // Trigger image loading for the selected album
    MainActivity mainActivity;

    private List<String> selectedItems = new ArrayList<>();

    public boolean isViewingPhotos = false;
    private boolean isSelect = false;
    public int currentLayout = 1; // Default to 1-column layout

    int alnumid = -1;
    int status_hide = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        btnAddAlbum = view.findViewById(R.id.btn_add_album);
        btnLayout = view.findViewById(R.id.btn_change_layout);
        recyclerView = view.findViewById(R.id.recyclerView);

        btnBack = view.findViewById(R.id.btn_back_album);
        btnBack.setVisibility(View.GONE); // Hide back button initially
        btnLayout.setVisibility(View.GONE);
        isSelect = false;

        // Initialize DatabaseHandler
        databaseHandler = new DatabaseHandler(requireContext());
        mainActivity = (MainActivity) getActivity();

        // Initialize album list and adapter
        albumList = new ArrayList<>();
        albumList.add(new Album(-1, "All Photos", databaseHandler.getTotalPhoto(), databaseHandler.getFirstPhotoPath())); // "All Photos" album
        albumList.add(new Album(-2, "Favorites", databaseHandler.getTotalFavoritedPhotos(), databaseHandler.getFirstFavoritePhotoPath()));
        albumList.add(new Album(-3, "Hidden", databaseHandler.getTotalHiddenPhotos(), databaseHandler.getFirstHiddenPhotoPath()));
        albumList.addAll(databaseHandler.getAllAlbumsWithFirstPhoto()); // Fetch all albums from SQLite

        // Setup AlbumAdapter with listener for item clicks
        albumAdapter = new AlbumAdapter(getContext(), albumList, new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Album album) {
                onAlbumClicked(album);
                alnumid = album.getId();
            }
        });

        // Set layout manager to GridLayout with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(albumAdapter);

        // Add album button click handler
        btnAddAlbum.setOnClickListener(v -> showAddAlbumDialog());

        // Back button click handler
        btnBack.setOnClickListener(v -> {
            if (isViewingPhotos) {
                showAlbumList(); // Return to album list if viewing photos
            }
        });

        // Thiết lập listener để cập nhật selectedItems khi người dùng chọn album
        albumAdapter.setOnItemSelectedListener((album, isSelected) -> {
            if (isSelected) {
                selectedItems.add(album.getName());  // Thêm album vào danh sách chọn
            } else {
                selectedItems.remove(album.getName());  // Loại bỏ album khỏi danh sách chọn
            }
            //Toast.makeText(mainActivity, String.valueOf(selectedItems.size()), Toast.LENGTH_SHORT).show();
        });

        // Thiết lập listener để cập nhật selectedItems khi người dùng chọn album
        mainActivity.imageAdapter.setOnItemSelectedListener((string, isSelected) -> {
            if (isSelected) {
                selectedItems.add(string);  // Thêm album vào danh sách chọn
            } else {
                selectedItems.remove(string);  // Loại bỏ album khỏi danh sách chọn
            }
            //Toast.makeText(mainActivity, string, Toast.LENGTH_SHORT).show();
            //Toast.makeText(mainActivity, String.valueOf(selectedItems.size()), Toast.LENGTH_SHORT).show();
        });

        // Layout switch button click handler
        btnLayout.setOnClickListener(v -> switchLayout(true));

        return view;
    }

    // Xử lý khi nhấn vào Select
    public void handlerSelect() {
        isSelect = !isSelect;
        enableSelectionMode(isSelect);
    }

    public void loadimg(){
        //Toast.makeText(mainActivity, "loadimg", Toast.LENGTH_SHORT).show();
        if (alnumid == -1){
            showAllPhotos();
        }
        else if (alnumid == -2){
            showFavorPhotos();
        }
        else if (alnumid == -3){
            showHidePhotos();
        }
        else {
            isViewingPhotos = true;

            // Cập nhật giao diện
            btnAddAlbum.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
            btnLayout.setVisibility(View.VISIBLE);

            recyclerView.setAdapter(null);

            if (mainActivity != null) {
                mainActivity.LoadImgInAlbumID(alnumid);
            }
        }
    }

    // Xử lý khi nhấn vào Select
    public void handlerErase() {
        if (isViewingPhotos){
            //Toast.makeText(mainActivity, "call back", Toast.LENGTH_SHORT).show();
            mainActivity.callHandleImageDeletion();
        }
        else {
            //Toast.makeText(mainActivity, "call this", Toast.LENGTH_SHORT).show();
            deleteSelectedAlbums();
            reset();
        }

    }

    public int getAlnumid(){
        return alnumid;
    }

    // Enable selection mode
    public void enableSelectionMode(boolean isEnabled) {
        isSelect = isEnabled;
        if (isEnabled) {
            // Show checkboxes in RecyclerView
            if (isViewingPhotos) {
                mainActivity.imageAdapter.enableSelection(true);
            } else {
                albumAdapter.enableSelection(true);
            }
        } else {
            // Hide checkboxes and clear selected items
            if (isViewingPhotos) {
                mainActivity.imageAdapter.enableSelection(false);
            } else {
                albumAdapter.enableSelection(false);
            }
        }
    }

    // Hàm xóa album
    public void deleteSelectedAlbums() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "No album selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo AlertDialog để xác nhận xóa
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Albums")
                .setMessage("Are you sure you want to delete the selected albums?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Xử lý xóa album
                    for (String albumName : selectedItems) {
                        // Lấy album ID từ tên
                        int albumId = getAlbumIdByName(albumName);
                        if (albumId != -1) {
                            databaseHandler.deleteAlbum(albumId); // Xóa khỏi database
                        }

                        // Xóa khỏi danh sách hiển thị
                        for (int i = 0; i < albumList.size(); i++) {
                            if (albumList.get(i).getName().equals(albumName)) {
                                albumList.remove(i);
                                break;
                            }
                        }
                    }

                    // Làm mới RecyclerView
                    albumAdapter.notifyDataSetChanged();

                    // Dọn danh sách đã chọn
                    selectedItems.clear();
                    //handlerSelect();
                    isSelect = false;
                    enableSelectionMode(isSelect);
                    Toast.makeText(requireContext(), "Albums deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No",(dialog, which) -> {
                    handlerSelect();
                });

        // Hiển thị AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK); // Màu nút "Yes"
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
    }



    private void showAlbumList() {
        isViewingPhotos = false;

        // Show album list
        btnAddAlbum.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);

//        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
//        recyclerView.setAdapter(albumAdapter);
        reset();
    }

    private void onAlbumClicked(Album album) {
        if (album.getId() == -1) {
            showAllPhotos(); // Show all photos when "All Photos" album is clicked
            switchLayout(false);
        } else if (album.getId() == -2) {
            showFavorPhotos(); // Show all photos when "Favorite" album is clicked
            switchLayout(false);
        } else if (album.getId() == -3) {
            showPasswordDialog(); // Show all photos when "Hide" album is clicked
        } else {
            showPhotosInAlbum(album); // Show photos in selected album
            switchLayout(false);
        }
    }

    private void showPasswordDialog() {
        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40); // Padding cho layout chính

        // Tạo ô nhập mật khẩu
        final EditText input = new EditText(getContext());
        input.setHint(getString(R.string.fill_password)); // Thêm gợi ý
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(input); // Thêm ô nhập mật khẩu vào layout

        // Tạo dialog
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.title_fill_password))
                .setView(layout)
                .setPositiveButton(getString(R.string.popup_mask), null) // Xử lý sự kiện sau khi dialog hiển thị
                .setNegativeButton(getString(R.string.btn_cancel), (dialog, which) -> dialog.dismiss())
                .create();

        // Hiển thị dialog
        alertDialog.show();

        // Tùy chỉnh giao diện nút
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (negativeButton != null) {
            negativeButton.setTextColor(Color.BLACK); // Đổi màu chữ nút Hủy
            negativeButton.setBackgroundColor(Color.WHITE); // Đổi màu nền nút Hủy
            negativeButton.setPadding(20, 10, 20, 10); // Thêm padding cho nút Hủy
        }

        if (positiveButton != null) {
            positiveButton.setTextColor(Color.BLACK); // Đổi màu chữ nút OK
            positiveButton.setBackgroundColor(Color.WHITE); // Đổi màu nền nút OK
            positiveButton.setPadding(20, 10, 20, 10); // Thêm padding cho nút OK

            positiveButton.setOnClickListener(v -> {
                String enteredPassword = input.getText().toString().trim();
                DatabaseHandler db = new DatabaseHandler(requireContext());

                // Lấy mật khẩu lưu trữ
                String storedPassword = db.getHiddenAlbumPassword();

                if (storedPassword != null && storedPassword.equals(enteredPassword)) {
                    // Mật khẩu đúng, hiển thị album ẩn
                    showHidePhotos();
                    alertDialog.dismiss();
                } else {
                    // Mật khẩu sai, hiển thị thông báo
                    Toast.makeText(requireContext(), "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }





    private void showAllPhotos() {
        isViewingPhotos = true;
        btnAddAlbum.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(null);

        if (mainActivity != null) {
            mainActivity.LoadImgInAlbum();
        }
    }

    private void showFavorPhotos() {
        isViewingPhotos = true;
        btnAddAlbum.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(null);

        if (mainActivity != null) {
            mainActivity.LoadImgInAlbumAsFavor();
        }
    }

    private void showHidePhotos() {
        isViewingPhotos = true;
        btnAddAlbum.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(null);

        if (mainActivity != null) {
            mainActivity.LoadImgInAlbumAsHide();
        }
    }

    private void showPhotosInAlbum(Album album) {
        isViewingPhotos = true;

        // Cập nhật giao diện
        btnAddAlbum.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.VISIBLE);

        recyclerView.setAdapter(null);

        if (mainActivity != null) {
            mainActivity.LoadImgInAlbumID(album.getId());
        }
    }

    private void showAddAlbumDialog() {
        // Inflate custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_album, null);

        // Initialize dialog components
        EditText etAlbumName = dialogView.findViewById(R.id.edt_album_name);
        RecyclerView recyclerViewPhotos = dialogView.findViewById(R.id.recyclerViewPhotos);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        // Fetch all photo paths for selection
        List<String> photoPaths = databaseHandler.getAllPhotoPaths();
        List<String> selectedPhotos = new ArrayList<>();

        // Setup PhotoAddAdapter for selecting photos
        PhotoAddAdapter photoAddAdapter = new PhotoAddAdapter(requireContext(), photoPaths, selectedPhotos, (imagePath) -> {
            if (selectedPhotos.contains(imagePath)) {
                selectedPhotos.remove(imagePath);
            } else {
                selectedPhotos.add(imagePath);
            }
        });

        recyclerViewPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3)); // Grid 3 columns
        recyclerViewPhotos.setAdapter(photoAddAdapter);

        // Create and show AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Cancel button handler
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Add button handler
        btnAdd.setOnClickListener(v -> {
            String albumName = etAlbumName.getText().toString().trim();
            if (albumName.isEmpty()) {
                etAlbumName.setError("Please enter album name");
                return;
            }

            // Check if album name is valid (not duplicated)
            if (!isAlbumNameValid(albumName)) {
                etAlbumName.setError("Album name already exists");
                return;
            }

            if (selectedPhotos.size() < 3) {
                Toast.makeText(requireContext(), "Please select at least 3 photos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save album and selected photos
            addAlbumWithPhotos(albumName, selectedPhotos);
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    private void addAlbumWithPhotos(String albumName, List<String> selectedPhotos) {
        if (isAlbumNameValid(albumName)) {
            // Add album to database
            databaseHandler.addAlbum(albumName);

            // Get the album ID
            int albumId = getAlbumIdByName(albumName);

            // Add selected photos to album
            for (String photoPath : selectedPhotos) {
                addPhotoToAlbum(photoPath, albumId);
            }

            // Refresh album list and update UI
            albumList.clear();
            albumList.add(new Album(-1, "All Photos", databaseHandler.getTotalPhoto(), databaseHandler.getFirstPhotoPath())); // "All Photos" album
            albumList.add(new Album(-2, "Favorites", databaseHandler.getTotalFavoritedPhotos(), databaseHandler.getFirstFavoritePhotoPath()));
            albumList.add(new Album(-3, "Hidden", databaseHandler.getTotalHiddenPhotos(), databaseHandler.getFirstHiddenPhotoPath()));
            albumList.addAll(databaseHandler.getAllAlbumsWithFirstPhoto()); // Fetch all albums from SQLite
            albumAdapter.notifyDataSetChanged();

            // Notify user
            Toast.makeText(requireContext(), "Album and photos saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Invalid album name!", Toast.LENGTH_SHORT).show();
        }
    }

    private int getAlbumIdByName(String albumName) {
        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        Cursor cursor = db.query("album", new String[]{"id_album"}, "name = ?", new String[]{albumName}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int albumId = cursor.getInt(cursor.getColumnIndex("id_album"));
            cursor.close();
            return albumId;
        }

        cursor.close();
        return -1; // Return -1 if album not found
    }

    private void addPhotoToAlbum(String photoPath, int albumId) {
        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        Cursor cursor = db.query("photos", new String[]{"id"}, "file_path = ?", new String[]{photoPath}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int photoId = cursor.getInt(cursor.getColumnIndex("id"));
            databaseHandler.addPhotoToAlbum(photoId, albumId); // Add photo to album
        }

        cursor.close();
    }

    private boolean isAlbumNameValid(String albumName) {
        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        Cursor cursor = db.query("album", new String[]{"name"}, "name = ?", new String[]{albumName}, null, null, null);
        boolean isValid = cursor.getCount() == 0;
        cursor.close();
        return isValid;
    }

    private void switchLayout(boolean sta) {
        if (sta) {
            currentLayout++; // Tăng kiểu layout
            if (currentLayout > 3) currentLayout = 1; // Quay lại 1 cột nếu vượt quá 3 cột
        }

        setImageLayout();
    }

    public void setLayout() {
        if (isViewingPhotos){
            setImageLayout();
        }
    }

    public void reset() {
        //Toast.makeText(mainActivity, "resetttt", Toast.LENGTH_SHORT).show();
        isSelect = false;
        albumList.clear();
        albumList.add(new Album(-1, "All Photos", databaseHandler.getTotalPhoto(), databaseHandler.getFirstPhotoPath())); // "All Photos" album
        albumList.add(new Album(-2, "Favorites", databaseHandler.getTotalFavoritedPhotos(), databaseHandler.getFirstFavoritePhotoPath()));
        albumList.add(new Album(-3, "Hidden", databaseHandler.getTotalHiddenPhotos(), databaseHandler.getFirstHiddenPhotoPath()));
        albumList.addAll(databaseHandler.getAllAlbumsWithFirstPhoto()); // Fetch all albums from SQLite

        albumAdapter.setList(albumList);
        albumAdapter.notifyDataSetChanged();


        // Set layout manager to GridLayout with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(albumAdapter);
    }

    public void setImageLayout() {
        switch (currentLayout) {
            case 1:
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                btnLayout.setImageResource(R.drawable.ic_layout_1);
                break;
            case 2:
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                btnLayout.setImageResource(R.drawable.ic_layout_2);
                break;
            case 3:
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                btnLayout.setImageResource(R.drawable.ic_layout_3);
                break;
        }
    }

    public boolean isViewingPhotos() {
        return isViewingPhotos;
    }
}
