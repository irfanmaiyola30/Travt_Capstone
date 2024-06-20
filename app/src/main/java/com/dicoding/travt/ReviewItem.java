package com.dicoding.travt;

public class ReviewItem {
    private final String createdAt;
    private final int placeId;
    private final int ratingUser;
    private final String namaTempat;
    private final String photo;
    private final String reviewerName;

    public ReviewItem(String createdAt, int placeId, int ratingUser, String namaTempat, String photo, String reviewerName) {
        this.createdAt = createdAt;
        this.placeId = placeId;
        this.ratingUser = ratingUser;
        this.namaTempat = namaTempat;
        this.photo = photo;
        this.reviewerName = reviewerName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getPlaceId() {
        return placeId;
    }

    public String getNamaTempat() {
        return namaTempat;
    }

    public int getRatingUser() {
        return ratingUser;
    }

    public String getPhoto() {
        return photo;
    }

    public String getReviewerName() {
        return reviewerName;
    }
}
