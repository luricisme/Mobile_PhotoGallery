package com.example.photo_gallery_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class FavoriteFragment extends Fragment {

    public RecyclerView recyclerView;
    private ImageButton btnLayout;
    public int currentLayout = 1; // Bắt đầu với layout 1 cột


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Sử dụng view để truy cập RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tìm nút chuyển đổi layout
        btnLayout = view.findViewById(R.id.btn_change_layout);

        // Xử lý sự kiện bấm nút
        btnLayout.setOnClickListener(v -> switchLayout());


        setImageLayout();

        // Gọi hàm trong MainActivity
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.LoadImgInFavorite();
        }


        return view;
    }

    private void switchLayout() {
        currentLayout++; // Tăng kiểu layout
        if (currentLayout > 3) currentLayout = 1; // Quay lại 1 cột nếu vượt quá 3 cột

        setImageLayout();
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

    public void enableSelectionMode(boolean isEnabled) {
        if (recyclerView.getAdapter() instanceof ImageAdapter) {
            ((ImageAdapter) recyclerView.getAdapter()).enableSelection(isEnabled);
        }
    }
}