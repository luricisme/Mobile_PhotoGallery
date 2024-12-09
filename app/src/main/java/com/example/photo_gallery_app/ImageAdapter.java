package com.example.photo_gallery_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context context;
    private List<String> ds;
    private boolean isSelectionMode = false;
    private ImageAdapter.OnItemSelectedListener onItemSelectedListener;

    private List<String> selectedImages = new ArrayList<>();

    public ImageAdapter(Context context, List<String> ds) {
        this.context = context;
        this.ds = ds;
        this.isSelectionMode = false;
    }

    // Thiết lập listener cho AlbumFragment
    public void setOnItemSelectedListener(ImageAdapter.OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.image_show, parent, false);
//        Toast.makeText(context, "Đã thêm ảnh thủ công", Toast.LENGTH_SHORT).show();
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        String imagePath = ds.get(position);
//        Toast.makeText(context, "Đã thêm " + imagePath, Toast.LENGTH_SHORT).show();

        holder.imvThem.setImageURI(Uri.parse(imagePath));  // Nếu là URI
        //holder.imvThem.setImageBitmap(BitmapFactory.decodeFile(imagePath)); // Nếu là đường dẫn file

        if (isSelectionMode) {
            holder.checkbox.setVisibility(View.VISIBLE);
        } else {
            holder.checkbox.setVisibility(View.GONE);
            holder.checkbox.setChecked(false);
        }

        holder.imvThem.setOnClickListener(v -> {
            if (!isSelectionMode) {
                // Ở đây mở một activity mới để hiển thị ảnh chi tiết
                Intent intent = new Intent(context, ImageDetailActivity.class);
                intent.putExtra("imagePath", imagePath);
                context.startActivity(intent);
            }
        });

        // Lắng nghe sự kiện checkbox
//        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (onItemSelectedListener != null) {
//                DatabaseHandler db = new DatabaseHandler(context);
//                int imageId = db.getImageIdFromPath(imagePath);
//                onItemSelectedListener.onItemSelected(String.valueOf(imageId), isChecked);
//            }
//        });

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedImages.contains(imagePath)) {
                    selectedImages.add(imagePath);
                }
            } else {
                selectedImages.remove(imagePath);
            }
            if (onItemSelectedListener != null) {
                DatabaseHandler db = new DatabaseHandler(context);
                int imageId = db.getImageIdFromPath(imagePath);
                onItemSelectedListener.onItemSelected(String.valueOf(imageId), isChecked);
            }
        });

    }

//    public void enableSelection(boolean isEnabled) {
//        isSelectionMode = isEnabled;
//        notifyDataSetChanged();
//    }

    public void enableSelection(boolean isEnabled) {
        isSelectionMode = isEnabled;
        if (!isSelectionMode) {
            clearSelectedImages();
        }
        notifyDataSetChanged();
    }

    public void setDs(Context _context, List<String> _ds){
        context = _context;
        ds = _ds;
        isSelectionMode = false;
    }

    @Override
    public int getItemCount() {
        return ds.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imvThem;
        CheckBox checkbox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkBox);
            imvThem = itemView.findViewById(R.id.imv_them);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(String string, boolean isSelected);
    }

    public List<String> getSelectedImages() {
        return selectedImages;
    }

    public void clearSelectedImages() {
        selectedImages.clear();
    }
}
