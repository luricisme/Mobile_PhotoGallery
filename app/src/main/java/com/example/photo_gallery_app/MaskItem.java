package com.example.photo_gallery_app;

public class MaskItem {
    private final String name; // Tên bộ lọc
    private final int maskType; // Ảnh đại diện cho bộ lọc

    public MaskItem(String name, int maskType) {
        this.name = name;
        this.maskType = maskType;
    }

    public String getName() {
        return name;
    }

    public int getMaskType() {
        return maskType;
    }
}
