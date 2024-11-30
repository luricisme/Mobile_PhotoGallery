package com.example.photo_gallery_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    private ImageButton btnAddAlbum, btnBack;
    public RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private ArrayList<Album> albumList;
    private DatabaseHandler databaseHandler;

    private boolean isViewingPhotos = false;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        btnAddAlbum = view.findViewById(R.id.btn_add_album);
        recyclerView = view.findViewById(R.id.recyclerView);

        btnBack = view.findViewById(R.id.btn_back_album);
        btnBack.setVisibility(View.GONE); // Ẩn nút quay lại ban đầu

        // Khởi tạo đối tượng DatabaseHandler
        databaseHandler = new DatabaseHandler(requireContext());

        // Khởi tạo danh sách album và Adapter
        albumList = new ArrayList<>();
        albumList.add(new Album(-1, "Tất cả ảnh", databaseHandler.getTotalPhoto()));
        albumList.addAll(databaseHandler.getAllAlbums()); // Lấy dữ liệu từ SQLite
        // Khởi tạo AlbumAdapter và truyền listener để xử lý click
        albumAdapter = new AlbumAdapter(getContext(), albumList, new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Album album) {
                // Gọi phương thức trong Fragment để xử lý sự kiện click
                onAlbumClicked(album.getName(), album.getPhotoCount());
            }
        });

        // Cài đặt GridLayoutManager với 2 cột
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(albumAdapter);

        // Thêm album khi bấm nút
        btnAddAlbum.setOnClickListener(v -> showAddAlbumDialog());

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> {
            if (isViewingPhotos) {
                showAlbumList(); // Quay lại danh sách album
            }
        });

        return view;
    }

    private void showAlbumList() {
        isViewingPhotos = false;

        // Hiển thị danh sách album
        btnAddAlbum.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(albumAdapter);
    }

    // Phương thức này sẽ nhận tên album và số ảnh khi item được click
    private void onAlbumClicked(String albumName, int photoCount) {


        isViewingPhotos = true;

        btnAddAlbum.setVisibility(View.GONE);
        btnBack.setVisibility(View.VISIBLE);


        // Xử lý hành động khi album được click
        Toast.makeText(getContext(), "Album: " + albumName + " - " + photoCount + " photos", Toast.LENGTH_SHORT).show();
        // Bạn có thể thay đổi hành động ở đây, ví dụ như mở một màn hình chi tiết của album

        // Thay đổi LayoutManager để hiển thị danh sách ảnh
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Gọi hàm trong MainActivity
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.LoadImgInAlbum();
        }
    }

    private void showAddAlbumDialog() {
        // Inflate layout tùy chỉnh
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_album, null);

        // Ánh xạ các thành phần giao diện
        EditText etAlbumName = dialogView.findViewById(R.id.edt_album_name);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        // Tạo AlertDialog với layout tùy chỉnh
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Hiển thị hộp thoại và hiện bàn phím
        dialog.setOnShowListener(dialogInterface -> {
            etAlbumName.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etAlbumName, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Gán sự kiện cho nút Cancel
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etAlbumName.getWindowToken(), 0);
            }
        });

        // Gán sự kiện cho nút Add
        btnAdd.setOnClickListener(v -> {
            String albumName = etAlbumName.getText().toString().trim();
            if (!albumName.isEmpty()) {
                addAlbum(albumName);
                dialog.dismiss();
            } else {
                etAlbumName.setError("Please enter album name");
            }
        });

        // Hiển thị hộp thoại
        dialog.show();
    }

    private void addAlbum(String albumName) {
        // Kiểm tra tên album hợp lệ
        if (isAlbumNameValid(albumName)) {
            // Thêm album vào cơ sở dữ liệu
            databaseHandler.addAlbum(albumName);

            // Cập nhật lại danh sách album
            albumList.clear();
            albumList.add(new Album(-1, "Tất cả ảnh", databaseHandler.getTotalPhoto())); // Giữ album "Tất cả ảnh" đầu danh sách
            albumList.addAll(databaseHandler.getAllAlbums());
            albumAdapter.notifyDataSetChanged(); // Cập nhật giao diện
        }
    }

    private boolean isAlbumNameValid(String albumName) {
        // Kiểm tra rỗng
        if (albumName.isEmpty()) {
            return false;
        }

        // Kiểm tra trùng lặp trong danh sách album
        for (Album album : albumList) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                return false;
            }
        }

        return true;
    }

    private void onAlbumClicked(Album album) {
        if (album.getId() == -1) {
            // Xử lý khi chọn album "Tất cả ảnh"
            showAllPhotos();
        } else {
            // Xử lý khi chọn album cụ thể
            showPhotosInAlbum(album);
        }
    }

    private void showAllPhotos() {

        // Điều hướng hoặc hiển thị tất cả ảnh
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Hiển thị tất cả ảnh trong album.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showPhotosInAlbum(Album album) {


    }
}
