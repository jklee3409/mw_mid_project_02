package com.example.photoviewer;

public class Post {
    private String title;
    private String text;
    private String imageUrl;
    private String createdDate;

    public Post(String title, String text, String imageUrl, String createdDate) {
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        this.createdDate = createdDate;
    }

    public String getTitle() {
        return title;
    }
    public String getText() {
        return text;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getCreatedDate() {
        return createdDate;
    }
}
