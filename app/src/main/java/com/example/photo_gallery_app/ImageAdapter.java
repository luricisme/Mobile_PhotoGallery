package com.example.photo_gallery_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final Context context;
    private final List<String> ds;

    public ImageAdapter(Context context, List<String> ds) {
        this.context = context;
        this.ds = ds;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.image_show, parent, false);
        Toast.makeText(context, "Đã thêm ảnh thủ công", Toast.LENGTH_SHORT).show();
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        String imagePath = ds.get(position);
        Toast.makeText(context, "Đã thêm " + imagePath, Toast.LENGTH_SHORT).show();

        holder.imvThem.setImageURI(Uri.parse(imagePath));  // Nếu là URI
        //holder.imvThem.setImageBitmap(BitmapFactory.decodeFile(imagePath)); // Nếu là đường dẫn file

        holder.imvThem.setOnClickListener(v -> {
            // Ở đây mở một activity mới để hiển thị ảnh chi tiết
            Intent intent = new Intent(context, ImageDetailActivity.class);
            intent.putExtra("imagePath", imagePath);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return ds.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imvThem;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imvThem = itemView.findViewById(R.id.imv_them);
        }
    }
}
