package com.example.photo_gallery_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context context;
    private List<Album> albumList;
    private OnItemClickListener onItemClickListener;

    // Khởi tạo Adapter với OnItemClickListener
    public AlbumAdapter(Context context, List<Album> albumList, OnItemClickListener listener) {
        this.context = context;
        this.albumList = albumList;
        this.onItemClickListener = listener;
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

        // Thiết lập sự kiện click cho item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(album);

            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlbumName, tvPhotoCount;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvAlbumName = itemView.findViewById(R.id.tv_album_name);
            tvPhotoCount = itemView.findViewById(R.id.tv_photo_count);
        }
    }

    // Interface để lắng nghe sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Album album);
    }
}

