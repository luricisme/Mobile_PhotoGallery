package com.example.photo_gallery_app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {
    private List<String> imagePathList;
    private Context context;

    public ImagePagerAdapter(Context context, List<String> imagePathList) {
        this.context = context;
        this.imagePathList = imagePathList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = imagePathList.get(position);

        // Gán tag cho ViewHolder để nhận diện khi cần
        holder.itemView.setTag("view_" + position);

        Glide.with(context)
                .load(Uri.parse(imagePath))
                .error(R.drawable.error_image)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imagePathList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
