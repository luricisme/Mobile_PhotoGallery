package com.example.photo_gallery_app;

public class Album {
    private int id;
    private String name;
    private int photoCount;
    private String firstPhotoPath; // Đường dẫn ảnh đầu tiên

    public Album(int id, String name, int photoCount, String firstPhotoPath) {
        this.id = id;
        this.name = name;
        this.photoCount = photoCount;
        this.firstPhotoPath = firstPhotoPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public String getFirstPhotoPath() {
        return firstPhotoPath;
    }

    public void setFirstPhotoPath(String firstPhotoPath) {
        this.firstPhotoPath = firstPhotoPath;
    }
}

