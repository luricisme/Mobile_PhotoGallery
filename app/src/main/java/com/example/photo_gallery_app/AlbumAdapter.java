package com.example.photo_gallery_app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context context;
    private List<Album> albumList;
    private OnItemClickListener onItemClickListener;
    private boolean isSelectionMode = false;
    private OnItemSelectedListener onItemSelectedListener;
    DatabaseHandler databaseHandler;


    // Khởi tạo Adapter với OnItemClickListener
    public AlbumAdapter(Context context, List<Album> albumList, OnItemClickListener listener) {
        this.context = context;
        this.albumList = albumList;
        this.onItemClickListener = listener;
        this.isSelectionMode = false;
        this.databaseHandler = new DatabaseHandler(context);
    }

    public void setList(List<Album> albumList) {
        this.albumList = albumList;
        //this.isSelectionMode = false;
    }

    // Thiết lập listener cho AlbumFragment
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        holder.tvAlbumName.setText(album.getName());
        holder.tvPhotoCount.setText(album.getPhotoCount() + " photos");

        //Toast.makeText(context, album.getFirstPhotoPath(), Toast.LENGTH_SHORT).show();

        if (album.getFirstPhotoPath() != null && !album.getFirstPhotoPath().isEmpty()){
            Uri photoUri = Uri.parse(album.getFirstPhotoPath()); // Chuyển đổi đường dẫn thành URI
            holder.imgThumbnail.setImageURI(photoUri);
        }
        else{
            // Nếu không có ảnh, hiển thị ảnh placeholder
            holder.imgThumbnail.setImageResource(R.drawable.ic_default_album);
        }

        // Kiểm tra nếu album là "All Photos" hoặc "Favorites"
        if (album.getName().equals("All Photos") || album.getName().equals("Favorites") ||  album.getName().equals("Hidden")) {
            holder.checkbox.setVisibility(View.GONE); // Ẩn checkbox nếu là "All Photos" hoặc "Favorites"
        } else {
            if (isSelectionMode) {
                holder.checkbox.setVisibility(View.VISIBLE); // Hiển thị checkbox nếu ở chế độ chọn
            } else {
                holder.checkbox.setVisibility(View.GONE); // Ẩn checkbox nếu không ở chế độ chọn
                holder.checkbox.setChecked(false);
            }
        }

        // Thiết lập sự kiện click cho item
        holder.itemView.setOnClickListener(v -> {
            if (!isSelectionMode && onItemClickListener != null) {
                onItemClickListener.onItemClick(album);
            }
        });

        // Lắng nghe sự kiện checkbox
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onItemSelectedListener != null) {
                onItemSelectedListener.onItemSelected(album, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlbumName, tvPhotoCount;
        ImageView imgThumbnail;
        CheckBox checkbox;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkBox);
            imgThumbnail = itemView.findViewById(R.id.img_album_thumbnail);
            tvAlbumName = itemView.findViewById(R.id.tv_album_name);
            tvPhotoCount = itemView.findViewById(R.id.tv_photo_count);
        }
    }

    public void enableSelection(boolean isEnabled) {
        isSelectionMode = isEnabled;
        notifyDataSetChanged();
    }

    // Interface để lắng nghe sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Album album);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Album album, boolean isSelected);
    }
}

