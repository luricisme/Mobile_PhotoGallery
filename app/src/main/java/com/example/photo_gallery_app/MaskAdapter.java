package com.example.photo_gallery_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MaskAdapter extends RecyclerView.Adapter<MaskAdapter.MaskViewHolder> {

    private List<MaskItem> maskList;
    private Context context;
    private OnItemClickListener onItemSelectedListener;

    public interface OnItemClickListener {
        void onItemClick(MaskItem mask);
    }

    public MaskAdapter(Context context, List<MaskItem> maskList, OnItemClickListener listener) {
        this.context = context;
        this.maskList = maskList;
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public MaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mask, parent, false);
        return new MaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaskViewHolder holder, int position) {
        MaskItem mask = maskList.get(position); // Lấy item theo vị trí
        holder.maskName.setText(mask.getName());

        switch (position) {
            case 0:
                holder.maskImage.setImageResource(R.drawable.img); // Không áp dụng loc
                break;
            case 1:
                holder.maskImage.setImageResource(R.drawable.img_grey); // Bộ lọc trắng đen
                break;
            case 2:
                holder.maskImage.setImageResource(R.drawable.img_cozy); // Bộ lọc ấm áp
                break;
            case 3:
                holder.maskImage.setImageResource(R.drawable.img_reverse); // Bộ lọc ngược màu
                break;
            case 4:
                holder.maskImage.setImageResource(R.drawable.img_vintage); // Bộ lọc cổ điển
                break;
            case 5:
                holder.maskImage.setImageResource(R.drawable.img_blur); // Bộ lọc làm mờ
                break;
            case 6:
                holder.maskImage.setImageResource(R.drawable.img_contrast); // Bộ lọc tương phản cao
                break;
            case 7:
                holder.maskImage.setImageResource(R.drawable.img_neutral); // Bộ lọc trung hòa
                break;
            default:
                holder.maskImage.setImageResource(R.drawable.img); // Không áp dụng bộ lọc
        }


        // Khi click vào 1 item thì kích hoạt sự kiện onClick của item
        holder.itemView.setOnClickListener(v -> onItemSelectedListener.onItemClick(mask));
    }

    @Override
    public int getItemCount() {
        return maskList.size();
    }

    static class MaskViewHolder extends RecyclerView.ViewHolder {
        ImageView maskImage;
        TextView maskName;

        public MaskViewHolder(@NonNull View itemView) {
            super(itemView);
            maskImage = itemView.findViewById(R.id.imageViewMask);
            maskName = itemView.findViewById(R.id.txtNameMask);
        }
    }
}
