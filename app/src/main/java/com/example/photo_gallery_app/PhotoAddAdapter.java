package com.example.photo_gallery_app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotoAddAdapter extends RecyclerView.Adapter<PhotoAddAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<String> photoPaths; // Danh sách đường dẫn ảnh
    private final List<String> selectedPhotos; // Danh sách ảnh đã chọn
    private final OnPhotoClickListener onPhotoClickListener; // Giao diện để xử lý sự kiện click vào ảnh

    public PhotoAddAdapter(Context context, List<String> photoPaths, List<String> selectedPhotos, OnPhotoClickListener onPhotoClickListener) {
        this.context = context;
        this.photoPaths = photoPaths;
        this.selectedPhotos = selectedPhotos;
        this.onPhotoClickListener = onPhotoClickListener;  // Khởi tạo listener
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.photo_add_album, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String photoPath = photoPaths.get(position);

        // Tải ảnh vào ImageView bằng setImageURI
        holder.imageView.setImageURI(Uri.parse(photoPath));

        // Cập nhật trạng thái checkbox nếu ảnh đã được chọn
        holder.checkBox.setChecked(selectedPhotos.contains(photoPath));

        // Lắng nghe sự kiện click vào ảnh để thay đổi trạng thái checkbox
        holder.itemView.setOnClickListener(v -> {
            if (onPhotoClickListener != null) {
                onPhotoClickListener.onPhotoClick(photoPath);
            }
        });

        // Lắng nghe sự kiện thay đổi trạng thái checkbox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedPhotos.contains(photoPath)) {
                    selectedPhotos.add(photoPath); // Thêm ảnh vào danh sách chọn
                }
            } else {
                selectedPhotos.remove(photoPath); // Bỏ ảnh khỏi danh sách chọn
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoPaths.size();
    }

    // ViewHolder cho một ảnh trong RecyclerView
    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imv_them);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    // Giao diện để xử lý sự kiện click vào ảnh
    public interface OnPhotoClickListener {
        void onPhotoClick(String photoPath);
    }
}
